package ru.wtg.whereaminow.holders;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.InviteSender;
import ru.wtg.whereaminow.helpers.MyTracking;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.Utils;

import static ru.wtg.whereaminow.State.CHANGE_NAME;
import static ru.wtg.whereaminow.State.MAKE_ACTIVE;
import static ru.wtg.whereaminow.State.MAKE_INACTIVE;
import static ru.wtg.whereaminow.State.SELECT_USER;
import static ru.wtg.whereaminow.State.TOKEN_CREATED;
import static ru.wtg.whereaminow.State.TRACKING_ACTIVE;
import static ru.wtg.whereaminow.State.TRACKING_CONNECTING;
import static ru.wtg.whereaminow.State.TRACKING_DISABLED;
import static ru.wtg.whereaminow.State.TRACKING_ERROR;
import static ru.wtg.whereaminow.State.TRACKING_JOIN;
import static ru.wtg.whereaminow.State.TRACKING_NEW;
import static ru.wtg.whereaminow.State.TRACKING_RECONNECTING;
import static ru.wtg.whereaminow.State.TRACKING_STOP;
import static ru.wtg.whereaminow.helpers.MyTracking.TRACKING_URI;
import static ru.wtg.whereaminow.holders.MessagesHolder.WELCOME_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_INITIAL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_PRIVATE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_WELCOME_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;

/**
 * Created 11/30/16.
 */
public class TrackingHolder extends AbstractPropertyHolder {
    private static final String TYPE = "Tracking";

    private final Context context;
//    private final Intent intentService;
    private MyTracking tracking;

    public TrackingHolder(Context context) {
        this.context = context;
//        intentService = new Intent(context, WhereAmINowService.class);
        tracking = null;
    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

    @Override
    public AbstractProperty create(MyUser myUser) {
        return null;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) throws URISyntaxException {
        Log.i(TYPE,event+":"+object);
        switch (event) {
            case TRACKING_NEW:
                tracking = new MyTracking();
                State.getInstance().setTracking(tracking);
                tracking.setTrackingListener(onTrackingListener);
                tracking.start();

                break;
            case TRACKING_JOIN:
                String link  = (String) object;
                if(link != null) {
                    State.getInstance().setPreference(TRACKING_URI, link);
                    System.out.println("LINKS:"+State.getInstance().getStringPreference(TRACKING_URI, null)+":"+link);
                    if(!link.equals(State.getInstance().getStringPreference(TRACKING_URI, null)) || State.getInstance().tracking_disabled()) {
                        if(State.getInstance().getTracking() != null && !TRACKING_DISABLED.equals(State.getInstance().getTracking().getStatus())) {
                            State.getInstance().fire(TRACKING_STOP);
                        }
                        tracking = new MyTracking(link);
                        State.getInstance().setTracking(tracking);
                        tracking.setTrackingListener(onTrackingListener);
                        tracking.start();
                    } else if(State.getInstance().tracking_active()){
                        State.getInstance().fire(TRACKING_ACTIVE);
                    }
                }
                break;
            case TRACKING_STOP:
                State.getInstance().getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.removeViews();
                    }
                });
                State.getInstance().setPreference(TRACKING_URI, null);

                if(tracking != null) {
                    tracking.stop();
                    tracking = null;
                }
                State.getInstance().setToken(null);
                break;
//            case TRACKING_DISABLED:
//                State.getInstance().setPreference(TRACKING_URI, null);
//                break;
            case TRACKING_ACTIVE:

                LocationParams.Builder builder = new LocationParams.Builder().setAccuracy(LocationAccuracy.HIGH).setDistance(1).setInterval(1000);
                SmartLocation.with(State.getInstance()).location().continuous().config(builder.build()).start(locationUpdatedListener);

                break;
            case TRACKING_DISABLED:
                try {
                    SmartLocation.with(State.getInstance()).location().stop();

                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                State.getInstance().getUsers().removeAllUsersExceptMe();
                State.getInstance().getMe().fire(SELECT_USER);

                break;
            case TRACKING_ERROR:
                System.out.println("TRACKING_ERROR");
                break;
            case TOKEN_CREATED:
                new InviteSender(context).send(tracking.getTrackingUri());
                break;
        }
        return true;
    }

    public interface TrackingListenerInterface {
        void onCreating();
        void onJoining(String tokenId);
        void onReconnecting();
        void onAccept();
//        void onExpire();
        void onReject(String reason);
        void onStop();

        void onClose();

//        void onOpen();
        void onMessage(JSONObject message);
//        void onError();
    }

    private TrackingListenerInterface onTrackingListener = new TrackingListenerInterface() {

        @Override
        public void onCreating() {
            State.getInstance().setPreference(TRACKING_URI, null);
            State.getInstance().fire(TRACKING_CONNECTING);
        }

        @Override
        public void onJoining(String tokenId) {
            State.getInstance().fire(TRACKING_RECONNECTING, "Joining group...");
        }

        @Override
        public void onReconnecting() {
            State.getInstance().fire(TRACKING_RECONNECTING, "Reconnecting group...");
        }

        @Override
        public void onClose() {

        }

        @Override
        public void onAccept() {
            State.getInstance().setPreference(TRACKING_URI, tracking.getTrackingUri());
            State.getInstance().fire(TRACKING_ACTIVE);
        }

//        @Override
//        public void onExpire() {
//
//        }

        @Override
        public void onReject(String reason) {
            State.getInstance().setPreference(TRACKING_URI, null);
            State.getInstance().getService().stopForeground(true);
            State.getInstance().fire(TRACKING_ERROR, reason);
        }

        @Override
        public void onStop() {
            State.getInstance().setPreference(TRACKING_URI, null);
            State.getInstance().fire(TRACKING_DISABLED);
        }

        @Override
        public void onMessage(final JSONObject o) {
            try {
                switch (o.getString(RESPONSE_STATUS)) {
//                    case RESPONSE_STATUS_DISCONNECTED:
//                        if (TRACKING_CONNECTING.equals(getStatus())) {
//                            setStatus(TRACKING_ERROR);
//                            state.fire(TRACKING_ERROR, o.has(RESPONSE_MESSAGE) ? o.getString(RESPONSE_MESSAGE) : null);
//                        } else if (TRACKING_RECONNECTING.equals(getStatus())) {
//                            state.fire(TRACKING_RECONNECTING, o.has(RESPONSE_MESSAGE) ? o.getString(RESPONSE_MESSAGE) : null);
//                        } else if(TRACKING_ACTIVE.equals(getStatus())) {
//                            setStatus(TRACKING_RECONNECTING);
//                            state.fire(TRACKING_RECONNECTING, o.has(RESPONSE_MESSAGE) ? o.getString(RESPONSE_MESSAGE) : null);
//
//                            webClient.reconnect();
//
//
//                        }
//
//                        state.getMe().fire(SELECT_USER);
//                        break;
                    case RESPONSE_STATUS_ACCEPTED:
                        if (o.has(RESPONSE_TOKEN)) {
                            State.getInstance().fire(TOKEN_CREATED, o.getString(RESPONSE_TOKEN));
                        }
                        if (o.has(RESPONSE_NUMBER)) {
                            State.getInstance().getUsers().setMyNumber(o.getInt(RESPONSE_NUMBER));
                        }
//
                        if (o.has(RESPONSE_INITIAL)) {
                            JSONArray initialUsers = o.getJSONArray(RESPONSE_INITIAL);
                            for (int i = 0; i < initialUsers.length(); i++) {
                                JSONObject u = initialUsers.getJSONObject(i);
                                State.getInstance().getUsers().addUser(u);
                            }
                        }
                        if (o.has(RESPONSE_WELCOME_MESSAGE)) {
//                            setWelcomeMessage(text);
                            State.getInstance().fire(WELCOME_MESSAGE, o.getString(RESPONSE_WELCOME_MESSAGE));
                        }
                        break;
//                    case RESPONSE_STATUS_ERROR:
//                        String message = o.getString(RESPONSE_MESSAGE);
//                        if(message == null) message = "Failed join to tracking_active.";
//
//                        SmartLocation.with(state).location().stop();
//                        stop();
//                        state.fire(TRACKING_ERROR, message);
//                        break;
                    case RESPONSE_STATUS_UPDATED:
                        if (o.has(USER_DISMISSED)) {
                            int number = o.getInt(USER_DISMISSED);
                            State.getInstance().getUsers().forUser(number,new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, final MyUser myUser) {
                                    myUser.fire(MAKE_INACTIVE);
                                    State.getInstance().fire(USER_DISMISSED,myUser);
                                }
                            });
                        } else if (o.has(USER_JOINED)) {
                            int number = o.getInt(USER_JOINED);
                            State.getInstance().getUsers().addUser(o);
                            State.getInstance().getUsers().forUser(number,new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
                                    myUser.fire(MAKE_ACTIVE);
                                    State.getInstance().fire(USER_JOINED,myUser);
                                }
                            });
                        }
                        if (o.has(USER_PROVIDER)) {
                            final Location location = Utils.jsonToLocation(o);
                            int number = o.getInt(USER_NUMBER);
                            State.getInstance().getUsers().forUser(number,new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
                                    myUser.addLocation(location);
                                }
                            });
                        }
                        if (o.has(USER_NAME)) {
                            int number = o.getInt(USER_NUMBER);
                            final String name = o.getString(USER_NAME);
                            State.getInstance().getUsers().forUser(number,new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
                                    myUser.fire(CHANGE_NAME,(name != null && name.length()>0) ? name : null);
                                }
                            });
                        }
                        if (o.has(USER_MESSAGE)) {
                            int number = o.getInt(USER_NUMBER);
                            final String text = o.getString(USER_MESSAGE);
                            State.getInstance().getUsers().forUser(number,new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
                                    if(o.has(RESPONSE_PRIVATE)){
                                        myUser.fire(MessagesHolder.PRIVATE_MESSAGE, text);
                                    } else {
                                        myUser.fire(MessagesHolder.USER_MESSAGE, text);
                                    }
                                }
                            });
                        }
                        break;
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
            State.getInstance().sendBroadcast(new Intent(BROADCAST).putExtra(BROADCAST_MESSAGE, o.toString()));

        }

    };

    private OnLocationUpdatedListener locationUpdatedListener = new OnLocationUpdatedListener() {
        @Override
        public void onLocationUpdated(Location location) {
            System.out.println("Service:onLocationChanged");

            if(TRACKING_ACTIVE.equals(tracking.getStatus())) {
                location = Utils.normalizeLocation(State.getInstance().getGpsFilter(), location);

                try {
                    JSONObject message = Utils.locationToJson(location);
                    tracking.sendMessage(message);

                    message.put(RESPONSE_STATUS,RESPONSE_STATUS_UPDATED);
                    message.put(RESPONSE_NUMBER, State.getInstance().getUsers().getMyNumber());
                    onTrackingListener.onMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
