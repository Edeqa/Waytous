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
            assert intent != null;
            state.fire(TRACKING_JOIN, intent.getStringExtra("host"));
        } else if("stop".equals(mode)){
            state.fire(TRACKING_STOP);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
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

    class ServiceBinder extends Binder {
        WhereAmINowService getService() {
            return WhereAmINowService.this;
        }
    }
}
