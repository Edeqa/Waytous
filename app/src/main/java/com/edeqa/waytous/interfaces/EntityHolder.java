package com.edeqa.waytous.interfaces;

import com.edeqa.waytous.helpers.MyUser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created 11/18/16.
 */

public interface EntityHolder<T extends Entity> {

    String getType();

    boolean dependsOnUser();

    boolean dependsOnEvent();

    T create(MyUser myUser);

    boolean onEvent(String event, Object object);

    void perform(JSONObject o) throws JSONException;

    boolean isSaveable();

    boolean isEraseable();

}
