package com.edeqa.waytous.helpers;

import android.content.Context;
import android.support.design.widget.Snackbar;

import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.State;
import com.edeqa.waytous.holders.view.SnackbarViewHolder;

/**
 * Created 12/4/16.
 */

@SuppressWarnings("WeakerAccess")
public class SystemMessage<T> {
    private final Context context;
    private String text;
    private String title;
    private MyUser fromUser;
    private MyUser toUser;
    private String delivery;
    private int type;
    private int duration = Snackbar.LENGTH_LONG;
    private Consumer<T> callback;
    private Consumer<T> onClickListener;
    private Consumer<T> action;

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

    public SystemMessage setAction(String title, Consumer<T> action) {
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

    public Consumer<T> getOnClickListener() {
        return onClickListener;
    }

    public SystemMessage setOnClickListener(Consumer<T> callback) {
        this.onClickListener = callback;
        return this;
    }

    public void showSnack(){
        SnackbarViewHolder holder = (SnackbarViewHolder) State.getInstance().getEntityHolder(SnackbarViewHolder.TYPE);
        if(holder != null) {
            holder.onEvent(SnackbarViewHolder.CUSTOM_SNACK, this);
        }
    }

    public Consumer getAction() {
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

    public Consumer<T> getCallback() {
        return callback;
    }

    public SystemMessage setCallback(Consumer<T> callback) {
        this.callback = callback;
        return this;
    }
}
