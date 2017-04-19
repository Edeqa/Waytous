package com.edeqa.waytous.helpers;

import android.content.Context;
import android.support.design.widget.Snackbar;

import com.edeqa.waytous.State;
import com.edeqa.waytous.holders.SnackbarViewHolder;
import com.edeqa.waytous.interfaces.SimpleCallback;

/**
 * Created 12/4/16.
 */

public class SystemMessage<T> {
    private final Context context;
    private String text;
    private String title;
    private MyUser fromUser;
    private MyUser toUser;
    private String delivery;
    private int type;
    private int duration = Snackbar.LENGTH_LONG;
    private SimpleCallback<T> callback;
    private SimpleCallback<T> onClickListener;
    private SimpleCallback<T> action;

    public SystemMessage(Context context) {
        this.context = context;
    }

    public String getText() {
        return text;
    }

    public SystemMessage setText(String message) {
        this.text = message;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public SystemMessage setAction(String title, SimpleCallback<T> action) {
        this.title = title;
        this.action = action;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public SystemMessage setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public SimpleCallback<T> getOnClickListener() {
        return onClickListener;
    }

    public SystemMessage setOnClickListener(SimpleCallback<T> callback) {
        this.onClickListener = callback;
        return this;
    }

    public void showSnack(){
        SnackbarViewHolder holder = (SnackbarViewHolder) State.getInstance().getEntityHolder(SnackbarViewHolder.TYPE);
        if(holder != null) {
            holder.onEvent(SnackbarViewHolder.CUSTOM_SNACK, this);
        }
    }

    public SimpleCallback getAction() {
        return action;
    }

    public MyUser getFromUser() {
        return fromUser;
    }

    public SystemMessage setFromUser(MyUser fromUser) {
        this.fromUser = fromUser;
        return this;
    }

    public MyUser getToUser() {
        return toUser;
    }

    public SystemMessage setToUser(MyUser toUser) {
        this.toUser = toUser;
        return this;
    }

    public String getDelivery() {
        return delivery;
    }

    public SystemMessage setDelivery(String delivery) {
        this.delivery = delivery;
        return this;
    }

    public int getType() {
        return type;
    }

    public SystemMessage setType(int type) {
        this.type = type;
        return this;
    }

    public Context getContext(){
        return context;
    }

    public SimpleCallback<T> getCallback() {
        return callback;
    }

    public SystemMessage setCallback(SimpleCallback<T> callback) {
        this.callback = callback;
        return this;
    }
}
