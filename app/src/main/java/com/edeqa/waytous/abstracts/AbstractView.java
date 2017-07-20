package com.edeqa.waytous.abstracts;

import android.location.Location;

import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.Entity;

/**
 * Created 11/24/16.
 */

abstract public class AbstractView implements Entity {
    protected MyUser myUser;

    protected AbstractView(){
    }

    protected AbstractView(MyUser myUser){
        super();
        this.myUser = myUser;
    }

    @Override
    public void remove() {
    }

    @Override
    public void onChangeLocation(Location location){
        //noinspection HardCodedStringLiteral
        Utils.log(this,"onChangeLocation");
    }

/*
    public View infoWindow() {
        return null;
    }
*/
}