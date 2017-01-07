package ru.wtg.whereaminow.holders;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.ArrayList;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.helpers.IntroRule;
import ru.wtg.whereaminow.helpers.InviteSender;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.service_helpers.MyTracking;

import static ru.wtg.whereaminow.State.PREPARE_FAB;
import static ru.wtg.whereaminow.State.TOKEN_CHANGED;
import static ru.wtg.whereaminow.State.TOKEN_CREATED;
import static ru.wtg.whereaminow.State.TRACKING_ACTIVE;
import static ru.wtg.whereaminow.State.TRACKING_DISABLED;
import static ru.wtg.whereaminow.State.TRACKING_ERROR;
import static ru.wtg.whereaminow.State.TRACKING_JOIN;
import static ru.wtg.whereaminow.State.TRACKING_NEW;
import static ru.wtg.whereaminow.State.TRACKING_STOP;
import static ru.wtg.whereaminow.service_helpers.MyTracking.TRACKING_URI;

/**
 * Created 11/30/16.
 */
public class TrackingHolder extends AbstractPropertyHolder {
    private static final String TYPE = "tracking_active";

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
        System.out.println("TRACKING:ONSYSTEMEVENT:"+event+":"+object);
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
                    if (!tokenId.equals(State.getInstance().getToken())) {
                        if(State.getInstance().getTracking() == null || State.getInstance().getToken() != null) {
                            intentService.putExtra("mode", "join");
                            intentService.putExtra("token", tokenId);
                            intentService.putExtra("host", uri.getHost());
                            context.startService(intentService);
                        } else {
                            State.getInstance().getTracking().join(tokenId);
                        }
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
                intentService.putExtra("mode", "stop");
                context.startService(intentService);

/*
                if("start".equals(mode)){
                    try {
                        state.setTracking(new MyTracking());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        Toast.makeText(this,"Error: "+e.getReason(),Toast.LENGTH_SHORT).show();
                        return super.onStartCommand(intent, flags, startId);
                    }
                    state.getTracking().start();
                } else if("join".equals(mode)){
                    try {
                        assert intent != null;
                        state.setTracking(new MyTracking(intent.getStringExtra("host")));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        Toast.makeText(this,"Error: "+e.getReason(),Toast.LENGTH_SHORT).show();
                        return super.onStartCommand(intent, flags, startId);
                    }
                    String token = intent.getStringExtra("token");
                    state.getTracking().join(token);
                } else if("stop".equals(mode)){
                    if(state.getTracking() != null) {
                        state.getTracking().stop();
                    }
                }

                */
                break;
            case TRACKING_DISABLED:
//                State.getInstance().setPreference(TRACKING_URI, null);
                break;
            case TRACKING_ACTIVE:
                State.getInstance().setPreference(TRACKING_URI, "https://" + State.getInstance().getTracking().getHost() + ":8080/track/" + State.getInstance().getToken());
                break;
            case TRACKING_ERROR:
                State.getInstance().setPreference(TRACKING_URI, null);
                intentService.putExtra("mode", "stop");
                context.startService(intentService);
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
