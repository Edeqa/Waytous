package com.edeqa.waytous.abstracts;

import com.edeqa.waytous.interfaces.EntityHolder;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created 11/18/16.
 */
abstract public class AbstractPropertyHolder implements EntityHolder<AbstractProperty> {

    protected AbstractPropertyHolder(){}

    public void init() {
    }

    @Override
    public boolean onEvent(String event, Object object) {
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

    @Override
    public boolean isEraseable() {
        return true;
    }
}
