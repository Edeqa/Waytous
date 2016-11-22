package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.location.Location;

/**
 * Created by tujger on 11/18/16.
 */

public interface ViewHolder<T extends AbstractView> {

    String getType();

    T createView(MyUser myUser);

}
