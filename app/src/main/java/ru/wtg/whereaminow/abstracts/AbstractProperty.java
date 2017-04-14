package ru.wtg.whereaminow.abstracts;

import android.location.Location;

import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.interfaces.Entity;

/**
 * Created 11/24/16.
 */

abstract public class AbstractProperty implements Entity {
    protected final MyUser myUser;

    protected AbstractProperty(MyUser myUser){
        this.myUser = myUser;
    }

    @Override
    public void remove() {
    }

    @Override
    public void onChangeLocation(Location location){
    }

}