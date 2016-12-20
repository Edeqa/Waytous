package ru.wtg.whereaminow.interfaces;

/**
 * Created 12/19/16.
 */

public interface TypedCallback<T,U,V> {
    T call(U arg1, V arg2);
}
