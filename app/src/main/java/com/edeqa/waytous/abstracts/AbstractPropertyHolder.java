package com.edeqa.waytous.abstracts;

import android.content.Context;

import com.edeqa.eventbus.AbstractEntityHolder;
import com.edeqa.waytous.interfaces.EntityHolder;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created 11/18/16.
 */
abstract public class AbstractPropertyHolder extends AbstractEntityHolder implements EntityHolder<AbstractProperty> {

    protected Context context;

    protected AbstractPropertyHolder(Context context){
        super();
        this.context = context;
    }

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
