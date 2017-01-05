package ru.wtg.whereaminow.helpers;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;

/**
 * Created 12/1/16.
 */

public class SmoothInterpolated {
    public static final int TIME_ELAPSED = 0;
    public static final int CURRENT_VALUE = 1;
    private int duration;
    private boolean cancel;

    private SimpleCallback<Float[]> callback;

    public SmoothInterpolated(SimpleCallback<Float[]> callback){
        this.callback = callback;
        duration = LOCATION_UPDATES_DELAY;
    }

    public void execute(){
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();

        new Thread(new Runnable(){
            float t,v,elapsed;

            @Override
            public void run(){
                if(cancel){
                    cancel = false;
                    return;
                }

                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / duration;
                v = interpolator.getInterpolation(t);

                try {
                    callback.call(new Float[]{t, v});
                } catch(Exception e){
                    e.printStackTrace();
                }

                if (t<1) {
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    this.run();
                }
            }
        }).start();

    }

    public SmoothInterpolated setDuration(int millis){
        duration = millis;
        return this;
    }

    public void cancel() {
        cancel = true;
    }
}
