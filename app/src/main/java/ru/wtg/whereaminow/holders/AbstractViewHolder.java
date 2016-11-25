package ru.wtg.whereaminow.holders;

import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.interfaces.EntityHolder;

/**
 * Created 11/18/16.
 */
abstract public class AbstractViewHolder<B extends AbstractView> implements EntityHolder<AbstractView> {

    AbstractViewHolder(){}

    abstract public String getType();

    abstract public B create(MyUser myUser);

}
