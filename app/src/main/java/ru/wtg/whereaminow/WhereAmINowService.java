package ru.wtg.whereaminow;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.nlopez.smartlocation.SmartLocation;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.service_helpers.MyTracking;

import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_INITIAL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_DISCONNECTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ERROR;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_STOPPED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;

public class WhereAmINowService extends Service {

    private ServiceBinder binder = new ServiceBinder();
    private State state;

    private int id;

    public WhereAmINowService() {
//        new GlobalExceptionHandler(WhereAmINowService.this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        state = State.getInstance();
        state.setService(this);

    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        id = startId;
        String mode = "initial";
        if(intent != null && intent.hasExtra("mode")) mode = intent.getStringExtra("mode");

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
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    class ServiceBinder extends Binder {
        WhereAmINowService getService() {
            return WhereAmINowService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        state.setService(null);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    public int getId(){
        return id;
    }


}
