package com.edeqa.waytous.holders.property;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractProperty;
import com.edeqa.waytous.abstracts.AbstractPropertyHolder;
import com.edeqa.waytous.helpers.MyTrackingFB;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.ShareSender;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.Runnable2;
import com.edeqa.waytous.interfaces.Tracking;
import com.edeqa.waytous.interfaces.TrackingCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

import static com.edeqa.waytous.helpers.Events.CHANGE_NAME;
import static com.edeqa.waytous.helpers.Events.MAKE_ACTIVE;
import static com.edeqa.waytous.helpers.Events.MAKE_INACTIVE;
import static com.edeqa.waytous.helpers.Events.SELECT_USER;
import static com.edeqa.waytous.helpers.Events.TOKEN_CREATED;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_CONNECTING;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_ERROR;
import static com.edeqa.waytous.helpers.Events.TRACKING_JOIN;
import static com.edeqa.waytous.helpers.Events.TRACKING_NEW;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;
import static com.edeqa.waytous.helpers.Events.TRACKING_STOP;
import static com.edeqa.waytous.holders.view.TrackingViewHolder.TRACKING_TERMS_OF_SERVICE;
import static com.edeqa.waytous.interfaces.Tracking.TRACKING_URI;
import static com.edeqa.waytousserver.helpers.Constants.BROADCAST;
import static com.edeqa.waytousserver.helpers.Constants.BROADCAST_MESSAGE;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_CHANGE_NAME;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_LEAVE;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_TRACKING;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_WELCOME_MESSAGE;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_STATUS;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_TOKEN;
import static com.edeqa.waytousserver.helpers.Constants.USER_DISMISSED;
import static com.edeqa.waytousserver.helpers.Constants.USER_JOINED;
import static com.edeqa.waytousserver.helpers.Constants.USER_LEFT;
import static com.edeqa.waytousserver.helpers.Constants.USER_NAME;
import static com.edeqa.waytousserver.helpers.Constants.USER_NUMBER;

/**
 * Created 11/30/16.
 */
public class TrackingHolder extends AbstractPropertyHolder {
    private static final String TYPE = REQUEST_TRACKING;

    public static final String PREFERENCE_TERMS_OF_SERVICE_CONFIRMED = "terms_of_service_confirmed"; //NON-NLS

    private Tracking tracking;

    public TrackingHolder(Context context) {
        super(context);
//        this.context = context;
//        intentService = new Intent(context, WaytousService.class);
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
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public void perform(JSONObject o) throws JSONException {
        final Location location = Utils.jsonToLocation(o);
        int number = o.getInt(USER_NUMBER);

        State.getInstance().getUsers().forUser(number,new Runnable2<Integer, MyUser>() {
            @Override
            public void call(Integer number, MyUser myUser) {
                myUser.addLocation(location);
            }
        });
    }

    @Override
    public boolean isSaveable() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch (event) {
            case TRACKING_NEW:
                if(State.getInstance().getBooleanPreference(PREFERENCE_TERMS_OF_SERVICE_CONFIRMED, false)) {
                    tracking = new MyTrackingFB();
                    State.getInstance().setTracking(tracking);
                    tracking.setTrackingListener(onTrackingListener);
                    tracking.start();
                } else {
                    State.getInstance().fire(TRACKING_TERMS_OF_SERVICE);
                }
                break;
            case TRACKING_JOIN:
                String link  = (String) object;

                if(State.getInstance().getBooleanPreference(PREFERENCE_TERMS_OF_SERVICE_CONFIRMED, false)) {
                    if(link != null) {
                        if(State.getInstance().tracking_disabled()) {
                            Utils.log(TYPE, "onEvent:", "join to", link); //NON-NLS
                            tracking = new MyTrackingFB(link);
                            State.getInstance().setTracking(tracking);
                            tracking.setTrackingListener(onTrackingListener);
                            tracking.start();
                        } else {
                            try {
                                URI currentUri = new URI(State.getInstance().getTracking().getTrackingUri());
                                URI newUri = new URI(link);
                                if(!currentUri.getPath().equals(newUri.getPath())) {
                                    Utils.log(TYPE, "onEvent:", "same group, skipping", link); //NON-NLS
                                    State.getInstance().fire(TRACKING_STOP);
                                    State.getInstance().fire(TRACKING_JOIN, link);
                                }
                            } catch(Exception e) {
                                Utils.log(TYPE, "onEvent:", "reconnecting to", link); //NON-NLS
                                State.getInstance().fire(TRACKING_STOP);
                                State.getInstance().fire(TRACKING_JOIN, link);
                            }
                        }
/*
                        if(!link.equals(State.getInstance().getStringPreference(TRACKING_URI, null)) || State.getInstance().tracking_disabled()) {
                            State.getInstance().setPreference(TRACKING_URI, link);
                            if(State.getInstance().getTracking() != null && !TRACKING_DISABLED.equals(State.getInstance().getTracking().getStatus())) {
                                State.getInstance().fire(TRACKING_STOP);
                            }
                        }
*/
                        /* else if(State.getInstance().tracking_active()){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                State.getInstance().fire(TRACKING_ACTIVE);
                            }
                        },0);
                        return false;
                    }*/
//                } else {
//                    State.getInstance().fire(TRACKING_ACTIVE);
                    }
                } else {
                    State.getInstance().fire(TRACKING_TERMS_OF_SERVICE, link);
                }
                break;
            case TRACKING_STOP:
                State.getInstance().getUsers().forAllUsersExceptMe(new Runnable2<Integer, MyUser>() {
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
                Utils.err(TYPE, "onEvent:", "TRACKING_ERROR", object); //NON-NLS
                break;
            case TOKEN_CREATED:
                new ShareSender(context).sendLink(tracking.getTrackingUri());
                break;
        }
        return true;
    }

    @Override
    public AbstractProperty create(MyUser myUser) {
        return null;
    }

    private TrackingCallback onTrackingListener = new TrackingCallback() {

        @Override
        public void onCreating() {
            State.getInstance().setPreference(TRACKING_URI, null);
            State.getInstance().fire(TRACKING_CONNECTING);
        }

        @Override
        public void onJoining(String tokenId) {
            State.getInstance().fire(TRACKING_RECONNECTING, context.getString(R.string.joining_group));
        }

        @Override
        public void onReconnecting() {
            State.getInstance().fire(TRACKING_RECONNECTING, context.getString(R.string.reconnecting));
        }

        @Override
        public void onClose() {
        }

        @Override
        public void onAccept(JSONObject o) {
            try {
                State.getInstance().setPreference(TRACKING_URI, tracking.getTrackingUri());
                State.getInstance().fire(TRACKING_ACTIVE);

                if (o.has(RESPONSE_TOKEN)) {
                    State.getInstance().fire(TOKEN_CREATED, o.getString(RESPONSE_TOKEN));
                }
                if (o.has(REQUEST_WELCOME_MESSAGE)) {
                    State.getInstance().fire(MessagesHolder.WELCOME_MESSAGE, o.getString(REQUEST_WELCOME_MESSAGE));
                }
                State.getInstance().sendBroadcast(new Intent(BROADCAST).putExtra(BROADCAST_MESSAGE, o.toString()));
            } catch (JSONException e) {
                State.getInstance().fire(TRACKING_STOP);
                e.printStackTrace();
            }

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
                Log.i(TYPE, "onMessage: " + o.toString()); //NON-NLS
                String responseStatus = o.getString(RESPONSE_STATUS);
                switch (responseStatus) {
                    case RESPONSE_STATUS_UPDATED:
                        if (o.has(USER_DISMISSED)) {
                            int number = o.getInt(USER_DISMISSED);
                            State.getInstance().getUsers().forUser(number,new Runnable2<Integer, MyUser>() {
                                @Override
                                public void call(Integer number, final MyUser user) {
                                    user.fire(MAKE_INACTIVE);
                                    State.getInstance().fire(USER_DISMISSED,user);
                                }
                            });
                        } else if (o.has(USER_JOINED)) {
                            int number = o.getInt(USER_JOINED);
                            State.getInstance().getUsers().addUser(o);
                            State.getInstance().getUsers().forUser(number,new Runnable2<Integer, MyUser>() {
                                @Override
                                public void call(Integer number, MyUser user) {
                                    if(!user.getProperties().isActive()) {
                                        user.fire(MAKE_ACTIVE);
                                        State.getInstance().fire(USER_JOINED, user);
                                    }
                                }
                            });
                        }
                        break;
                    case REQUEST_LEAVE:
                        if (o.has(USER_NUMBER)) {
                            int number = o.getInt(USER_NUMBER);
                            State.getInstance().getUsers().forUser(number, new Runnable2<Integer, MyUser>() {
                                @Override
                                public void call(Integer number, final MyUser user) {
                                    user.fire(MAKE_INACTIVE);
                                    State.getInstance().fire(USER_LEFT,user);
                                }
                            });
                        }
                        break;
                    case REQUEST_CHANGE_NAME:
                        if (o.has(USER_NAME)) {
                            int number = o.getInt(USER_NUMBER);
                            final String name = o.getString(USER_NAME);
                            State.getInstance().getUsers().forUser(number,new Runnable2<Integer, MyUser>() {
                                @Override
                                public void call(Integer number, MyUser user) {
                                    user.fire(CHANGE_NAME,(name != null && name.length()>0) ? name : null);
                                }
                            });
                        }
                        break;
                    default:
                        AbstractPropertyHolder holder = (AbstractPropertyHolder) State.getInstance().getEntityHolder(responseStatus);
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

}
