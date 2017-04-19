package com.edeqa.waytous.interfaces;

import android.location.Location;

/**
 * Created 11/18/16.
 */

public interface Entity {

    void remove();

    boolean onEvent(String event, Object object);

    boolean dependsOnLocation();

    void onChangeLocation(Location location);

}
