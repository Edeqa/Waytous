package ru.wtg.whereaminow.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.util.Log;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.holders.InfoViewHolder.SHOW_INFO;

/**
 * Created 11/26/16.
 */

public class LightSensorManager implements SensorEventListener {

    public static final String DAY = "day";
    public static final String NIGHT = "night";
    public static final String SATELLITE = "satellite";

    private static final float SMOOTHING = 10;
    private static final int THRESHOLD_DAY_LUX = 30;
    private static final int THRESHOLD_NIGHT_LUX = 1;
    private static final String TAG = "LightSensorManager";

    private final SensorManager sensorManager;
    private final Sensor lightSensor;
    private final LowPassFilter lowPassFilter;

    private SimpleCallback environmentChangeCallback;
    private Environment currentEnvironment;

    public LightSensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        lowPassFilter = new LowPassFilter(SMOOTHING);
    }

    public void enable() {
        if (lightSensor != null){
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                sensorManager.requestTriggerSensor(new TriggerEventListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onTrigger(TriggerEvent triggerEvent) {
                        System.out.println("TRIGGEREVENT:"+triggerEvent.toString());
                        onLuxValue(triggerEvent.values[0]);
                    }
                }, lightSensor);
            }
        } else {
            Log.w(TAG, "Light sensor in not supported");
        }
    }

    public void disable() {
        sensorManager.unregisterListener(this);
    }

    public void setOnEnvironmentChangeListener(SimpleCallback callback){
        environmentChangeCallback = callback;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        onLuxValue(event.values[0]);
    }

    private void onLuxValue(float luxLevel) {

        luxLevel = lowPassFilter.submit(luxLevel);

//        State.getInstance().fire(SHOW_INFO, "lux="+luxLevel);


        Environment oldEnvironment = currentEnvironment;
        if (luxLevel < THRESHOLD_NIGHT_LUX){
            currentEnvironment = Environment.NIGHT;
        } else if (luxLevel > THRESHOLD_DAY_LUX){
            currentEnvironment = Environment.DAY;
        }
        if (oldEnvironment != currentEnvironment && environmentChangeCallback != null){
            Log.i(TAG, "switch on luxLevel=" + luxLevel);
            switch (currentEnvironment) {
                case DAY:
                    //noinspection unchecked
                    environmentChangeCallback.call(DAY);
                    break;
                case NIGHT:
                    //noinspection unchecked
                    environmentChangeCallback.call(NIGHT);
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private enum Environment {DAY, NIGHT}

}


