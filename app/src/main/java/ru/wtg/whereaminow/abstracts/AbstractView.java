package ru.wtg.whereaminow.abstracts;

import android.location.Location;

import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.interfaces.Entity;

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
        System.out.println("onChangeLocation:"+(this.getClass().getSimpleName()));
    }

/*
    public View infoWindow() {
        return null;
    }
*/
}