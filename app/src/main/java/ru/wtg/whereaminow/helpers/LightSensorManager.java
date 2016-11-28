package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import ru.wtg.whereaminow.interfaces.SimpleCallback;

/**
 * Created 11/26/16.
 */

public class LightSensorManager implements SensorEventListener {

    private SimpleCallback environmentChangeCallback;

    private enum Environment {DAY, NIGHT}

    private static final float SMOOTHING = 10;
    private static final int THRESHOLD_DAY_LUX = 50;
    private static final int THRESHOLD_NIGHT_LUX = 40;
    private static final String TAG = "LightSensorManager";

    private final SensorManager sensorManager;
    private final Sensor lightSensor;
    private Environment currentEnvironment;
    private final LowPassFilter lowPassFilter;

    public LightSensorManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        lowPassFilter = new LowPassFilter(SMOOTHING);
    }

    public void enable() {
        if (lightSensor != null){
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
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

//    public void setEnvironmentChangedListener(EnvironmentChangedListener environmentChangedListener) {
//        this.environmentChangedListener = environmentChangedListener;
//    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float luxLevel = event.values[0];
        luxLevel = lowPassFilter.submit(luxLevel);
        Environment oldEnvironment = currentEnvironment;
        if (luxLevel < THRESHOLD_NIGHT_LUX){
            currentEnvironment = Environment.NIGHT;
        } else if (luxLevel > THRESHOLD_DAY_LUX){
            currentEnvironment = Environment.DAY;
        }
        if (oldEnvironment != currentEnvironment && environmentChangeCallback != null){
            switch (currentEnvironment) {
                case DAY:
                    environmentChangeCallback.call(1);
                    break;
                case NIGHT:
                    environmentChangeCallback.call(2);
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}


