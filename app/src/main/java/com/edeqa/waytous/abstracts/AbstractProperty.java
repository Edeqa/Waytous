package com.edeqa.waytous.abstracts;

import android.location.Location;

import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.interfaces.Entity;


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