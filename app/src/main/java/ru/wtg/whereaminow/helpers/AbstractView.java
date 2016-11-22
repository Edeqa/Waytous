package ru.wtg.whereaminow.helpers;

import android.location.Location;

/**
 * Created by tujger on 11/18/16.
 */

interface AbstractView {

    void remove();

    void onEvent(int event, Object object);

    boolean dependsOnLocation();

    void onChangeLocation(Location location);

    void setNumber(int number);

    int getNumber();

}
