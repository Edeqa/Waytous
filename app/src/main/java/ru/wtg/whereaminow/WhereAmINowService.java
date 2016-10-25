package ru.wtg.whereaminow;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import ru.wtg.whereaminow.helpers.State;
import ru.wtg.whereaminow.service_helpers.MyTracking;

public class WhereAmINowService extends Service {

    private ServiceBinder binder = new ServiceBinder();
    private MyTracking tracking;
    private State state;

    private int id;

    public WhereAmINowService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        tracking = new MyTracking(WhereAmINowService.this);
        System.out.println("Service:onCreate");
//to be deleted
        state = State.getInstance();
        state.myTracking = tracking;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        id = startId;
        String mode = "initial";
        if(intent != null && intent.hasExtra("mode")) mode = intent.getStringExtra("mode");
        System.out.println("Service:onStartCommand:"+startId+":"+mode);

        if("start".equals(mode)){
            tracking.start();
        } else if("join".equals(mode)){
            if(state.tracking()) {
                tracking.stop();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(!state.disconnected()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    }
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                            String token = intent.getStringExtra("token");
                            tracking.join(token);
                        }
                    });
                }
            }).start();
        } else if("stop".equals(mode)){
            tracking.stop();
        } else if("cancel".equals(mode)){
            tracking.cancel();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        String mode = intent.getStringExtra("mode");
        System.out.println("Service:onBind:"+mode);
        return binder;
    }

    class ServiceBinder extends Binder {
        WhereAmINowService getService() {
            return WhereAmINowService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println("Service:onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Service:onDestroy");
//        tracking.stop();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        System.out.println("Service:onRebind");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        System.out.println("Service:onTaskRemoved");
    }

    public int getId(){
        return id;
    }


}
