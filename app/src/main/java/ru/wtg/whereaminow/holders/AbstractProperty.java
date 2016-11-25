package ru.wtg.whereaminow.holders;

import android.location.Location;

import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.interfaces.Entity;

/**
 * Created 11/24/16.
 */

abstract public class AbstractProperty implements Entity {
    protected MyUser myUser;

    AbstractProperty(MyUser myUser){
        this.myUser = myUser;
    }

    @Override
    public void remove() {
    }

    @Override
    abstract public boolean dependsOnLocation();

    @Override
    public void onChangeLocation(Location location){
    }

    @Override
    public boolean isView(){
        return false;
    }

    @Override
    abstract public void onEvent(int event, Object object);

}