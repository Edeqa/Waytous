package com.edeqa.waytous.abstracts;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created 11/18/16.
 */
abstract public class AbstractViewHolder<T extends AbstractView> extends AbstractPropertyHolder {

    protected transient MainActivity context;

    protected AbstractViewHolder(MainActivity context) {
        super(context);
        this.context = context;
    }

    public void setContext(MainActivity context){
        this.context = context;
    }

    abstract public T create(MyUser myUser);

    @Override
    public boolean dependsOnUser(){
        return true;
    }

    @Override
    public boolean dependsOnEvent() {
        return false;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        return true;
    }

    public ArrayList<IntroRule> getIntro(){
        return null;
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
