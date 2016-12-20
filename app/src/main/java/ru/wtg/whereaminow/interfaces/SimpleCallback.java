package ru.wtg.whereaminow.interfaces;

import android.os.Parcelable;

/**
 * Created 11/20/16.
 */

public interface SimpleCallback<T> {
    void call(T arg);
}
