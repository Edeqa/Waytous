package ru.wtg.whereaminow.holders;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyTracking;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.ShareSender;
import ru.wtg.whereaminow.helpers.Utils;
import ru.wtg.whereaminow.interfaces.EntityHolder;
import ru.wtg.whereaminow.interfaces.TrackingCallback;

import static ru.wtg.whereaminow.State.EVENTS.CHANGE_NAME;
import static ru.wtg.whereaminow.State.EVENTS.MAKE_ACTIVE;
import static ru.wtg.whereaminow.State.EVENTS.MAKE_INACTIVE;
import static ru.wtg.whereaminow.State.EVENTS.SELECT_USER;
import static ru.wtg.whereaminow.State.EVENTS.TOKEN_CREATED;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_ACTIVE;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_CONNECTING;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_DISABLED;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_ERROR;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_JOIN;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_NEW;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_RECONNECTING;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_STOP;
import static ru.wtg.whereaminow.helpers.MyTracking.TRACKING_URI;
import static ru.wtg.whereaminow.holders.MessagesHolder.WELCOME_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_CHANGE_NAME;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_TRACKING;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_WELCOME_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_INITIAL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NUMBER;

/**
 * Created 11/30/16.
 */
public class TrackingHolder extends AbstractPropertyHolder {
    private static final String TYPE = REQUEST_TRACKING;

    private final Context context;
//    private final Intent intentService;
    private MyTracking tracking;
    private TrackingCallback onTrackingListener = new TrackingCallback() {

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
        public void onAccept(JSONObject o) {
            State.getInstance().setPreference(TRACKING_URI, tracking.getTrackingUri());
            State.getInstance().fire(TRACKING_ACTIVE);

            try {
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
                if (o.has(REQUEST_WELCOME_MESSAGE)) {
//                            setWelcomeMessage(text);
                    State.getInstance().fire(WELCOME_MESSAGE, o.getString(REQUEST_WELCOME_MESSAGE));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            State.getInstance().sendBroadcast(new Intent(BROADCAST).putExtra(BROADCAST_MESSAGE, o.toString()));
        }

//        @Override
//        public void onExpire() {
//
//        }

        @Override
        public void onReject(String reason) {
            State.getInstance().setPreference(TRACKING_URI, null);
            State.getInstance().getService().stopForeground(true);
            State.getInstance().fire(TRACKING_DISABLED);
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
                System.out.println(o);
                String responseStatus = o.getString(RESPONSE_STATUS);
                switch (responseStatus) {
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
                        break;
                    case REQUEST_CHANGE_NAME:
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
                        break;
                    default:
                        EntityHolder holder = State.getInstance().getEntityHolder(responseStatus);
                        if(holder != null) {
                            holder.perform(o);
                        }

                }
            }catch(JSONException e){
                e.printStackTrace();
            }
            State.getInstance().sendBroadcast(new Intent(BROADCAST).putExtra(BROADCAST_MESSAGE, o.toString()));

        }

    };

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
    public void perform(JSONObject o) throws JSONException {

//        if (o.has(USER_PROVIDER)) {
            final Location location = Utils.jsonToLocation(o);
            int number = o.getInt(USER_NUMBER);
            State.getInstance().getUsers().forUser(number,new MyUsers.Callback() {
                @Override
                public void call(Integer number, MyUser myUser) {
                    myUser.addLocation(location);
                }
            });
//        }

    }

    @Override
    public boolean onEvent(String event, Object object) throws URISyntaxException {
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
                    if(!link.equals(State.getInstance().getStringPreference(TRACKING_URI, null)) || State.getInstance().tracking_disabled()) {
                        State.getInstance().setPreference(TRACKING_URI, link);
                        if(State.getInstance().getTracking() != null && !TRACKING_DISABLED.equals(State.getInstance().getTracking().getStatus())) {
                            State.getInstance().fire(TRACKING_STOP);
                        }
                        tracking = new MyTracking(link);
                        State.getInstance().setTracking(tracking);
                        tracking.setTrackingListener(onTrackingListener);
                        tracking.start();
                    } else if(State.getInstance().tracking_active()){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                State.getInstance().fire(TRACKING_ACTIVE);
                            }
                        },0);
                        return false;
                    }
//                } else {
//                    State.getInstance().fire(TRACKING_ACTIVE);
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
            case TRACKING_DISABLED:
                State.getInstance().getUsers().removeAllUsersExceptMe();
                State.getInstance().getMe().fire(SELECT_USER);

                break;
            case TRACKING_ERROR:
                System.out.println("TRACKING_ERROR");
                break;
            case TOKEN_CREATED:
                new ShareSender(context).sendLink(tracking.getTrackingUri());
                break;
        }
        return true;
    }
}
