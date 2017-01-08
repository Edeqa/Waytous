package ru.wtg.whereaminow.holders;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.net.URISyntaxException;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.helpers.InviteSender;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.service_helpers.MyTracking;

import static ru.wtg.whereaminow.State.TOKEN_CREATED;
import static ru.wtg.whereaminow.State.TRACKING_ACTIVE;
import static ru.wtg.whereaminow.State.TRACKING_ERROR;
import static ru.wtg.whereaminow.State.TRACKING_JOIN;
import static ru.wtg.whereaminow.State.TRACKING_NEW;
import static ru.wtg.whereaminow.State.TRACKING_STOP;
import static ru.wtg.whereaminow.service_helpers.MyTracking.TRACKING_URI;

/**
 * Created 11/30/16.
 */
public class TrackingHolder extends AbstractPropertyHolder {
    private static final String TYPE = "Tracking";

    private final Context context;
    private final Intent intentService;
    private MyTracking tracking;

    public TrackingHolder(Context context) {
        this.context = context;
        intentService = new Intent(context, WhereAmINowService.class);
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
                State.getInstance().setToken(null);
                tracking = new MyTracking();

                State.getInstance().setTracking(tracking);
                tracking.start();

                break;
            case TRACKING_JOIN:
                Uri uri = (Uri) object;
                if(uri != null) {
                    State.getInstance().setPreference(TRACKING_URI, uri.toString());
                    String tokenId = uri.getEncodedPath().replaceFirst("/track/", "");
                    if (!tokenId.equals(State.getInstance().getToken())) {
                        if(State.getInstance().getTracking() == null || State.getInstance().getToken() != null) {
                            tracking = new MyTracking(uri.getHost());
                            tracking.join(tokenId);

                        } else {
                            if(tracking == null) {
                                tracking = new MyTracking(uri.getHost());
                            }
                            tracking.join(tokenId);
                        }
                        State.getInstance().setTracking(tracking);
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
                State.getInstance().setPreference(TRACKING_URI, "https://" + State.getInstance().getTracking().getHost() + ":8080/track/" + State.getInstance().getToken());
                break;
            case TRACKING_ERROR:
                State.getInstance().setPreference(TRACKING_URI, null);
                tracking.stop();
//                intentService.putExtra("mode", "stop");
//                context.startService(intentService);
                break;
            case TOKEN_CREATED:
                new InviteSender(context).send("https://" + State.getInstance().getTracking().getHost() + ":8080/track/" + State.getInstance().getToken());
                break;
//            case TOKEN_CHANGED:
//                break;
        }
        return true;
    }

}
