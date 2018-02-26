package com.edeqa.waytous.interfaces;

import com.edeqa.waytous.helpers.MyUser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created 11/18/16.
 */

public interface PropertyHolder<T extends Property> {

    String getType();

    boolean dependsOnUser();

    boolean dependsOnEvent();

    T create(MyUser myUser);

    boolean onEvent(String event, Object object) throws Exception;

    void perform(JSONObject o) throws JSONException;

    boolean isSaveable();

    boolean isEraseable();

}
