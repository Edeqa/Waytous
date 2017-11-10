package com.edeqa.waytous.abstracts;

import android.content.Context;
import android.location.Location;

import com.edeqa.eventbus.AbstractEntityHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.interfaces.Entity;


/**
 * Created 11/24/16.
 */

@SuppressWarnings("unchecked")
abstract public class AbstractProperty extends AbstractEntityHolder implements Entity {
    protected Context context;
    protected MyUser myUser;

    protected AbstractProperty(MyUser myUser){
        super(myUser);
        this.myUser = myUser;
    }

    protected AbstractProperty(Context context, MyUser myUser){
        super(context);
        this.context = context;
        this.myUser = myUser;
    }

    @Override
    public void remove() {
    }

    @Override
    public void onChangeLocation(Location location){
    }

}