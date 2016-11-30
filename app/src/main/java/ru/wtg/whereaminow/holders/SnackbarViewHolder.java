package ru.wtg.whereaminow.holders;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.HashMap;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.helpers.MyUser;

import static ru.wtg.whereaminow.State.ACCEPTED;
import static ru.wtg.whereaminow.State.DISCONNECTED;
import static ru.wtg.whereaminow.State.ERROR;
import static ru.wtg.whereaminow.State.JOIN_TRACKING;
import static ru.wtg.whereaminow.State.NEW_MESSAGE;
import static ru.wtg.whereaminow.State.NEW_TRACKING;
import static ru.wtg.whereaminow.State.SHOW_MESSAGES;
import static ru.wtg.whereaminow.State.STOP_TRACKING;
import static ru.wtg.whereaminow.State.USER_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;

/**
 * Created 11/27/16.
 */
public class SnackbarViewHolder extends AbstractViewHolder<SnackbarViewHolder.SnackbarView> {
    private static SnackbarViewHolder instance;
    private final Context context;
    private View layout;
    private Snackbar snackbar;

    public SnackbarViewHolder(Context context) {
        this.context = context;
    }

    @Override
    public String getType() {
        return "snackbar";
    }

    @Override
    public String[] getOwnEvents() {
        return new String[0];
    }

    @Override
    public boolean dependsOnUser() {
        return true;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public SnackbarView create(MyUser myUser) {
        if (myUser == null) return null;
        return new SnackbarView(myUser);
    }

    public SnackbarViewHolder setLayout(final View layout) {
        this.layout = layout;

        snackbar = Snackbar.make(layout, "Starting...", Snackbar.LENGTH_INDEFINITE);

        snackbar.getView().setAlpha(.8f);
        snackbar.setAction("Action", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("SNACKBAR ACTION");
            }
        });
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                setLayout(layout);
            }
        });
        return this;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        final MyUser user;
        switch(event){
            case NEW_TRACKING:
                snackbar.setText("Starting tracking...").setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("snackbar.onClick");
                        Intent intent = new Intent(context, WhereAmINowService.class);
                        intent.putExtra("mode", "stop");
                        context.startService(intent);
                        State.getInstance().fire(STOP_TRACKING);
                    }
                }).show();
                break;
            case JOIN_TRACKING:
                snackbar.setText("Joining tracking...").setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, WhereAmINowService.class);
                        intent.putExtra("mode", "stop");
                        context.startService(intent);
                        State.getInstance().fire(STOP_TRACKING);
                    }
                }).show();
                break;
            case DISCONNECTED:
                snackbar.setText("Disconnected. Trying to reconnect").setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(STOP_TRACKING);
                    }
                }).show();
                break;
            /*case USER_MESSAGE:
                String text = (String) object;
                snackbar.setText(text).setDuration(10000).setAction("Reply", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        snackbar.dismiss();
                        State.getInstance().fire(NEW_MESSAGE);
                    }
                }).show();
                break;*/
            case ACCEPTED:
                snackbar.dismiss();
                break;
            case ERROR:
                String message = (String) object;
                snackbar.setText(message).setAction("New tracking", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(NEW_TRACKING);
//                        onFabClick(-1);
                    }
                }).show();
                break;
            case USER_JOINED:
                user = (MyUser) object;
                snackbar.setText(user.getProperties().getName() + " has joined the group.").setDuration(Snackbar.LENGTH_LONG).setAction("Send message", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        user.fire(NEW_MESSAGE);
                    }
                }).show();
                break;
            case USER_DISMISSED:
                user = (MyUser) object;
                snackbar.setText(user.getProperties().getName() + " left the group.").setDuration(Snackbar.LENGTH_LONG).setAction(context.getString(android.R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                }).show();
                break;

        }
        return true;
    }

    public class SnackbarView extends AbstractView {

        SnackbarView(MyUser myUser) {
            super(myUser);
        }

        @Override
        public boolean dependsOnLocation() {
            return false;
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch (event) {
                case USER_MESSAGE:
                    String text = (String) object;
                    snackbar.setText(myUser.getProperties().getName() + ": " + text).setDuration(10000).setAction("Reply", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            myUser.fire(NEW_MESSAGE);
                        }
                    }).show();
                    snackbar.getView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                            State.getInstance().fire(SHOW_MESSAGES);
                        }
                    });
                    break;
            }
            return true;
        }

    }
}
