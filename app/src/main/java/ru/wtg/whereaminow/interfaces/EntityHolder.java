package ru.wtg.whereaminow.interfaces;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import ru.wtg.whereaminow.helpers.MyUser;

/**
 * Created 11/18/16.
 */

public interface EntityHolder<T extends Entity> {

    String getType();

    boolean dependsOnUser();

    boolean dependsOnEvent();

    T create(MyUser myUser);

    boolean onEvent(String event, Object object) throws URISyntaxException;

    void perform(JSONObject o) throws JSONException;

    boolean isSaveable();

    boolean isEraseable();

}
