package com.edeqa.waytous.interfaces;

import android.location.Location;

/**
 * Created 11/18/16.
 */

public interface Property<T> {

    void setContext(T context);

    void remove();

    boolean onEvent(String event, Object object) throws Exception;

    boolean dependsOnLocation();

    void onChangeLocation(Location location);

}
