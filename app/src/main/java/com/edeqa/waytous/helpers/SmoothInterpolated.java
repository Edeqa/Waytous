package com.edeqa.waytous.helpers;

import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.edeqa.waytous.interfaces.Callable1;

import static com.edeqa.waytousserver.helpers.Constants.LOCATION_UPDATES_DELAY;

/**
 * Created 12/1/16.
 */

public class SmoothInterpolated {
    public static final int TIME_ELAPSED = 0;
    public static final int CURRENT_VALUE = 1;
    private int duration;
    private boolean cancel;

    private Callable1<Float[]> callback;

    public SmoothInterpolated(Callable1<Float[]> callback){
        this.callback = callback;
        duration = LOCATION_UPDATES_DELAY;
    }

    public void execute(){
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();

        handler.post(new Runnable(){
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

                callback.call(new Float[]{t,v});

                if (t<1) {
                    handler.postDelayed(this, 16);
                }
            }

        });

    }

    public SmoothInterpolated setDuration(int millis){
        duration = millis;
        return this;
    }

    public void cancel() {
        cancel = true;
    }
}
