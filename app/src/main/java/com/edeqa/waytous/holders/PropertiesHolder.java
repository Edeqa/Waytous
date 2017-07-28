package com.edeqa.waytous.holders;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractProperty;
import com.edeqa.waytous.abstracts.AbstractPropertyHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.Runnable2;
import com.google.maps.android.SphericalUtil;

import java.io.Serializable;
import java.util.HashMap;


import static android.content.Context.MODE_PRIVATE;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_PAUSE;
import static com.edeqa.waytous.helpers.Events.CHANGE_COLOR;
import static com.edeqa.waytous.helpers.Events.CHANGE_NAME;
import static com.edeqa.waytous.helpers.Events.CHANGE_NUMBER;
import static com.edeqa.waytous.helpers.Events.MAKE_ACTIVE;
import static com.edeqa.waytous.helpers.Events.MAKE_INACTIVE;
import static com.edeqa.waytous.helpers.Events.MOVING_AWAY_FROM;
import static com.edeqa.waytous.helpers.Events.MOVING_CLOSE_TO;
import static com.edeqa.waytous.helpers.Events.SELECT_SINGLE_USER;
import static com.edeqa.waytous.helpers.Events.SELECT_USER;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_STOP;
import static com.edeqa.waytous.helpers.Events.UNSELECT_USER;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_CHANGE_NAME;
import static com.edeqa.waytousserver.helpers.Constants.USER_NAME;

/**
 * Created 11/18/16.
 */
@SuppressWarnings("unchecked")
public class PropertiesHolder extends AbstractPropertyHolder {

    public static final String TYPE = "properties";

    public static final String PREFERENCE_MY_NAME = "my_name";

    private static final String SELECTED = "selected";
    private static final String IMAGE_RESOURCE = "image_resource";
    private static final int DISTANCE_MOVING_CLOSE = 50;
    private static final int DISTANCE_MOVING_AWAY = 100;
    private final SharedPreferences sharedPreferences;
    private final Context context;
    private HashMap<String, Serializable> external = new HashMap<>();

    public PropertiesHolder(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("tracking_active", MODE_PRIVATE);
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case TRACKING_ACTIVE:
                State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        HashMap<String, Serializable> value = (HashMap<String, Serializable>) myUser.getProperties().loadFor(TYPE);
                        if(value != null) {
                            if(value.containsKey(SELECTED)) {
                                if(value.get(SELECTED) != null && (boolean)value.get(SELECTED)) {
                                    myUser.getProperties().selected = true;
//                                    myUser.fire(SELECT_USER, 0);
                                }
                            }
                            if(value.containsKey(IMAGE_RESOURCE)) {
                                if(value.get(IMAGE_RESOURCE) != null) {
                                    myUser.getProperties().imageResource = (int) value.get(IMAGE_RESOURCE);
//                                    myUser.getProperties().setButtonView((int) value.get(IMAGE_RESOURCE));
                                }
                            }
                        }
                    }
                });
                /*State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        HashMap<String, Serializable> value = (HashMap<String, Serializable>) myUser.getProperties().loadFor(TYPE);
                        if(value != null) {
                            if(value.containsKey(SELECTED)) {
                                if(value.get(SELECTED) != null && !(boolean)value.get(SELECTED)) {
                                    myUser.fire(UNSELECT_USER, 0);
                                }
                            }
                        }
                    }
                });*/

                break;
            case ACTIVITY_PAUSE:
                State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        HashMap<String, Serializable> m = new HashMap<>();
                        m.put(SELECTED, myUser.getProperties().isSelected());
                        m.put(IMAGE_RESOURCE, myUser.getProperties().getImageResource());
                        myUser.getProperties().saveFor(TYPE,m);
                    }
                });
                break;
            case TRACKING_STOP:
                sharedPreferences.edit().clear().apply();
                break;
        }
        return true;
    }

    public void saveFor(String type, Serializable props) {
        if (props == null) {
            external.remove(type);
            sharedPreferences.edit().remove(type).apply();
        } else {
            external.put(type, props);
            sharedPreferences.edit().putString(type, Utils.serializeToString(props)).apply();
        }
    }

    public Serializable loadFor(String type){
        if(external.containsKey(type)){
            return external.get(type);
        } else {
            String saved = sharedPreferences.getString(type, null);
            if(saved != null){
                return (Serializable) Utils.deserializeFromString(saved);
            }
        }
        return null;
    }

    @Override
    public Properties create(MyUser myUser) {
        if (myUser == null) return null;
        return new Properties(myUser);
    }

    public class Properties extends AbstractProperty {
        private HashMap<String, Serializable> external = new HashMap<>();
        private String name;
        private int number;
        private int color;
        private int imageResource;
        private boolean selected;
        private boolean active;
        private double previousDistance;

        Properties(MyUser myUser) {
            super(myUser);
        }

        @Override
        public boolean dependsOnLocation(){
            return true;
        }

        @Override
        public void onChangeLocation(final Location location) {
            if(location != null && myUser.getProperties().isActive()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(myUser == State.getInstance().getMe()) {
                            State.getInstance().getUsers().forAllUsersExceptMe(new Runnable2<Integer, MyUser>() {
                                @Override
                                public void call(Integer number, MyUser user) {
                                    if(user.getProperties().isActive()) {
                                        checkDistance(myUser, user);
                                    }
                                }
                            });
                        } else {
                            checkDistance(State.getInstance().getMe(), myUser);
                        }
                    }
                }).start();
            }
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch(event){
                case SELECT_USER:
                    selected = true;
                    break;
                case SELECT_SINGLE_USER:
                    State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            myUser.getProperties().selected = false;
                        }
                    });
                    myUser.fire(SELECT_USER);
                    State.getInstance().getUsers().forAllUsers(new Runnable2<Integer, MyUser>() {
                        @Override
                        public void call(Integer number, MyUser user) {
                            if(user != myUser) user.fire(UNSELECT_USER);
                        }
                    });
                    break;
                case UNSELECT_USER:
                    selected = false;
                    break;
                case MAKE_ACTIVE:
                    active = true;
                    break;
                case MAKE_INACTIVE:
                    active = false;
                    selected = false;
                    break;
                case CHANGE_NUMBER:
                    number = (int) object;
                    break;
                case CHANGE_COLOR:
                    color = (int) object;
                    break;
                case CHANGE_NAME:
                    name = (String) object;
                    if(myUser == State.getInstance().getMe()){
                        State.getInstance().setPreference(PREFERENCE_MY_NAME, name);
                        if(State.getInstance().getTracking() != null){
                            if(name == null) name = "";
                            State.getInstance().getTracking().put(USER_NAME,name).send(REQUEST_CHANGE_NAME);
                        }
                    }
                    break;
//                case CHANGE_TYPE:
//                    type = (String) object;
//                    break;
            }
            return true;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName(){
            String res = name;
            if(name == null || name.length()==0 || "null".equals(name)){
                if(myUser == State.getInstance().getMe()){
                    res = context.getString(R.string.me);
                } else if (getNumber() == 0) {
                    res = context.getString(R.string.leader);
                } else {
                    res = context.getString(R.string.friend_d, getNumber());
                }
            }
            return res;
        }

        public int getNumber() {
            return number;
        }

        public boolean isSelected() {
            return selected;
        }

        public boolean isActive() {
            return active;
        }

        public int getColor() {
            return color;
        }

        public void saveFor(String type, Serializable props) {
            if(props == null) {
                external.remove(type);
                sharedPreferences.edit().remove(type + "_" + myUser.getProperties().getNumber()).apply();
            } else {
                external.put(type, props);
                sharedPreferences.edit().putString(type + "_" + myUser.getProperties().getNumber(), Utils.serializeToString(props)).apply();
            }
        }

        public Serializable loadFor(String type){
            if(external.containsKey(type)){
                return external.get(type);
            } else {
                String saved = sharedPreferences.getString(type + "_" + myUser.getProperties().getNumber(), null);
                if(saved != null){
                    return (Serializable) Utils.deserializeFromString(saved);
                }
            }
            return null;
        }

        public void setButtonView(int imageResource) {
            this.imageResource = imageResource;
        }

        public int getImageResource() {
            return imageResource;
        }

        private void checkDistance(MyUser me, MyUser user) {
            if(me.getLocation() == null || user.getLocation() == null) return;
            try {
                double distance = SphericalUtil.computeDistanceBetween(Utils.latLng(me.getLocation()), Utils.latLng(user.getLocation()));
                if (distance <= DISTANCE_MOVING_CLOSE && user.getProperties().previousDistance > DISTANCE_MOVING_CLOSE) {
                    user.fire(MOVING_CLOSE_TO);
                } else if (distance > DISTANCE_MOVING_AWAY && user.getProperties().previousDistance > 0 && user.getProperties().previousDistance < DISTANCE_MOVING_AWAY) {
                    user.fire(MOVING_AWAY_FROM);
                }
                user.getProperties().previousDistance = distance;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
