package ru.wtg.whereaminow.holders;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.view.SoundEffectConstants;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.helpers.InviteSender;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;

import static android.R.attr.data;
import static android.R.attr.stackFromBottom;
import static ru.wtg.whereaminow.State.CONNECTION_DISCONNECTED;
import static ru.wtg.whereaminow.State.CONNECTION_ERROR;
import static ru.wtg.whereaminow.State.TOKEN_CHANGED;
import static ru.wtg.whereaminow.State.TOKEN_CREATED;
import static ru.wtg.whereaminow.State.TRACKING_ACCEPTED;
import static ru.wtg.whereaminow.State.TRACKING_JOIN;
import static ru.wtg.whereaminow.State.TRACKING_NEW;
import static ru.wtg.whereaminow.State.TRACKING_STARTED;
import static ru.wtg.whereaminow.State.TRACKING_STOP;
import static ru.wtg.whereaminow.State.TRACKING_STOPPED;
import static ru.wtg.whereaminow.service_helpers.MyTracking.TRACKING_URI;

/**
 * Created 11/30/16.
 */
public class TrackingHolder extends AbstractPropertyHolder {
    private static final String TYPE = "tracking";

    private final Context context;
    private final Intent intentService;

    public TrackingHolder(Context context) {
        this.context = context;
        intentService = new Intent(context, WhereAmINowService.class);
    }

    @Override
    public String getType(){
        return TYPE;
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
    public boolean onEvent(String event, Object object) {
        System.out.println("LOGGER:ONSYSTEMEVENT:"+event+":"+object);
        switch (event) {
            case TRACKING_NEW:
                State.getInstance().setToken(null);
                intentService.putExtra("mode", "start");
                context.startService(intentService);
                break;
            case TRACKING_JOIN:
                Uri uri = (Uri) object;
                if(uri != null) {
                    State.getInstance().setPreference(TRACKING_URI, uri.toString());
                    String tokenId = uri.getEncodedPath().replaceFirst("/track/", "");
                    if(!tokenId.equals(State.getInstance().getToken())) {
                        intentService.putExtra("mode", "join");
                        intentService.putExtra("token", tokenId);
                        intentService.putExtra("host", uri.getHost());
                        context.startService(intentService);
                    }
                }
                break;
            case TRACKING_STARTED:
                break;
            case TRACKING_STOP:
                State.getInstance().getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.removeViews();
                    }
                });
                intentService.putExtra("mode", "stop");
                context.startService(intentService);
                break;
            case TRACKING_STOPPED:
                State.getInstance().setPreference(TRACKING_URI, null);
                break;
            case TRACKING_ACCEPTED:
                State.getInstance().setPreference(TRACKING_URI, "https://" + State.getInstance().getTracking().getHost() + ":8080/track/" + State.getInstance().getToken());
                break;
            case CONNECTION_DISCONNECTED:
                break;
            case CONNECTION_ERROR:
                break;
            case TOKEN_CREATED:
                new InviteSender(context).send("https://" + State.getInstance().getTracking().getHost() + ":8080/track/" + State.getInstance().getToken());
                break;
            case TOKEN_CHANGED:
                break;
        }
        return true;
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

}
