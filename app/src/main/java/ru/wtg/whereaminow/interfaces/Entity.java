package ru.wtg.whereaminow.interfaces;

import android.location.Location;

/**
 * Created 11/18/16.
 */

public interface Entity {

    void remove();

    void onEvent(int event, Object object);

    boolean dependsOnLocation();

    void onChangeLocation(Location location);

    boolean isView();
}
