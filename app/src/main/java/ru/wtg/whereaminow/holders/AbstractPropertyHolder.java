package ru.wtg.whereaminow.holders;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import ru.wtg.whereaminow.interfaces.EntityHolder;

/**
 * Created 11/18/16.
 */
abstract class AbstractPropertyHolder implements EntityHolder<AbstractProperty> {

    AbstractPropertyHolder(){}

    public void init() {
    }

    @Override
    public boolean onEvent(String event, Object object) throws URISyntaxException {
        return true;
    }

    @Override
    public boolean dependsOnUser() {
        return true;
    }

    @Override
    public boolean dependsOnEvent() {
        return false;
    }

    @Override
    public void perform(JSONObject o) throws JSONException {

    }

    @Override
    public boolean isSaveable() {
        return false;
    }
}
