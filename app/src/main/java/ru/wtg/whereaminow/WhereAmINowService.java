package ru.wtg.whereaminow;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import static ru.wtg.whereaminow.State.EVENTS.TRACKING_JOIN;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_NEW;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_STOP;

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
            state.fire(TRACKING_NEW);
        } else if("join".equals(mode)){
            state.fire(TRACKING_JOIN, intent.getStringExtra("host"));
        } else if("stop".equals(mode)){
            state.fire(TRACKING_STOP);
        }
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
