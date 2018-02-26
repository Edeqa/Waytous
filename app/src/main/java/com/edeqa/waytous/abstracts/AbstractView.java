package com.edeqa.waytous.abstracts;

import android.content.Context;
import android.location.Location;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.Property;

/**
 * Created 11/24/16.
 */

abstract public class AbstractView extends AbstractProperty implements Property<Context> {
    protected MainActivity context;

    protected AbstractView(MyUser myUser){
        super(myUser);
    }

    protected AbstractView(MainActivity context, MyUser myUser){
        super(myUser);
        this.context = context;
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