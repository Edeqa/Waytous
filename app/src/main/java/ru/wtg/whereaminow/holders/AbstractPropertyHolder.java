package ru.wtg.whereaminow.holders;

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
}
