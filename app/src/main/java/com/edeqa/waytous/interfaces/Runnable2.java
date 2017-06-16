package com.edeqa.waytous.interfaces;

import com.edeqa.waytous.helpers.MyUser;

/**
 * Created 5/18/17.
 */
public interface Runnable2<T,U> {
    void call(T arg1, U arg2);
}
