package ru.wtg.whereaminow;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import java.net.URISyntaxException;

import ru.wtg.whereaminow.helpers.GlobalExceptionHandler;
import ru.wtg.whereaminow.helpers.State;
import ru.wtg.whereaminow.service_helpers.MyTracking;

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
        if(state.getApplication() == null) state.setApplication(getApplicationContext());
        state.setService(this);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        id = startId;
        String mode = "initial";
        if(intent != null && intent.hasExtra("mode")) mode = intent.getStringExtra("mode");

        if("start".equals(mode)){
            try {
                state.myTracking = new MyTracking();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Toast.makeText(this,"Error: "+e.getReason(),Toast.LENGTH_SHORT).show();
                return super.onStartCommand(intent, flags, startId);
            }
            state.myTracking.start();
        } else if("join".equals(mode)){
            if(state.tracking()) {
                state.myTracking.stop();
            }
            try {
                state.myTracking = new MyTracking(intent.getStringExtra("host"));
            } catch (URISyntaxException e) {
                e.printStackTrace();
                Toast.makeText(this,"Error: "+e.getReason(),Toast.LENGTH_SHORT).show();
                return super.onStartCommand(intent, flags, startId);
            }
            String token = intent.getStringExtra("token");
            state.myTracking.join(token);
        } else if("stop".equals(mode)){
            state.myTracking.stop();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        String mode = intent.getStringExtra("mode");
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
