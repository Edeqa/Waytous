package ru.wtg.whereaminow.helpers;

import android.support.design.widget.Snackbar;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.holders.SnackbarViewHolder;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

/**
 * Created 12/4/16.
 */

public class SnackbarMessage<T> {
    private String text;
    private String title;
    private int duration = Snackbar.LENGTH_LONG;
    private SimpleCallback<T> onClickListener;
    private SimpleCallback<T> action;

    public String getText() {
        return text;
    }

    public SnackbarMessage setText(String message) {
        this.text = message;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public SnackbarMessage setAction(String title, SimpleCallback<T> action) {
        this.title = title;
        this.action = action;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public SnackbarMessage setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public SimpleCallback<T> getOnClickListener() {
        return onClickListener;
    }

    public SnackbarMessage setOnClickListener(SimpleCallback<T> callback) {
        this.onClickListener = callback;
        return this;
    }

    public void show(){
        SnackbarViewHolder holder = (SnackbarViewHolder) State.getInstance().getEntityHolder(SnackbarViewHolder.TYPE);
        if(holder != null) {
            holder.onEvent(SnackbarViewHolder.CUSTOM_SNACK, this);
        }
    }

    public SimpleCallback getAction() {
        return action;
    }
}
