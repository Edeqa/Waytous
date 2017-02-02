package ru.wtg.whereaminow.holders;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.wtg.whereaminow.helpers.IntroRule;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.interfaces.EntityHolder;

/**
 * Created 11/18/16.
 */
abstract public class AbstractViewHolder<T extends AbstractView> implements EntityHolder<AbstractView> {

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
}
