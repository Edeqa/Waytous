package ru.wtg.whereaminow.interfaces;

import ru.wtg.whereaminow.helpers.MyUser;

/**
 * Created 11/18/16.
 */

public interface EntityHolder<T extends Entity> {

    String getType();

    String[] getOwnEvents();

    boolean dependsOnUser();

    boolean dependsOnEvent();

    T create(MyUser myUser);

    boolean onEvent(String event, Object object);

}
