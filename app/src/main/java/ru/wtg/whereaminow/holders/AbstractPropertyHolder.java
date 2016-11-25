package ru.wtg.whereaminow.holders;

import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.interfaces.EntityHolder;

/**
 * Created 11/18/16.
 */
abstract class AbstractPropertyHolder<B extends AbstractProperty> implements EntityHolder<AbstractProperty> {

    AbstractPropertyHolder(){}

    abstract public String getType();

    abstract public B create(MyUser myUser);

}
