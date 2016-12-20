package ru.wtg.whereaminow.holders;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.view.View;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.SnackbarMessage;

import static ru.wtg.whereaminow.State.CONNECTION_DISCONNECTED;
import static ru.wtg.whereaminow.State.CONNECTION_ERROR;
import static ru.wtg.whereaminow.State.TOKEN_CREATED;
import static ru.wtg.whereaminow.State.TRACKING_ACCEPTED;
import static ru.wtg.whereaminow.State.TRACKING_JOIN;
import static ru.wtg.whereaminow.State.TRACKING_NEW;
import static ru.wtg.whereaminow.State.TRACKING_STOP;
import static ru.wtg.whereaminow.holders.MessagesHolder.NEW_MESSAGE;
import static ru.wtg.whereaminow.holders.MessagesHolder.WELCOME_MESSAGE;
import static ru.wtg.whereaminow.holders.MessagesViewHolder.SETUP_WELCOME_MESSAGE;
import static ru.wtg.whereaminow.holders.MessagesViewHolder.SHOW_MESSAGES;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;

/**
 * Created 11/27/16.
 */
public class SnackbarViewHolder extends AbstractViewHolder<SnackbarViewHolder.SnackbarView> {

    public static final String TYPE = "snackbar";
    public static final String CUSTOM_SNACK = "custom_snack";
    public static final String CUSTOM_SNACK_MESSAGE = "custom_snack_message";
    public static final String CUSTOM_SNACK_BUTTON_TITLE = "custom_snack_button_text";
    public static final String CUSTOM_SNACK_BUTTON_CALLBACK = "custom_snack_button_callback";

    private final Context context;
    private Snackbar snackbar;

    private boolean tokenCreatedShown = false;

    public SnackbarViewHolder(Context context) {
        this.context = context;
    }

    @Override
    public String getType() {
        return TYPE;
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
            case TRACKING_NEW:
                snackbar.setText("Starting tracking...").setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("snackbar.onClick");
                        Intent intent = new Intent(context, WhereAmINowService.class);
                        intent.putExtra("mode", "stop");
                        context.startService(intent);
                        State.getInstance().fire(TRACKING_STOP);
                    }
                }).show();
                break;
            case TRACKING_JOIN:
                snackbar.setText("Joining tracking...").setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, WhereAmINowService.class);
                        intent.putExtra("mode", "stop");
                        context.startService(intent);
                        State.getInstance().fire(TRACKING_STOP);
                    }
                }).show();
                break;
            case TOKEN_CREATED:
                tokenCreatedShown = true;
                snackbar.setText("You have created the group").setDuration(Snackbar.LENGTH_LONG).setDuration(10000).setAction("Set welcome message", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(SETUP_WELCOME_MESSAGE);
                    }
                }).show();
                break;
            case TRACKING_ACCEPTED:
                if(tokenCreatedShown && State.getInstance().getMe().getProperties().getNumber() == 0) {
                    tokenCreatedShown = false;
                } else {
                    snackbar.setText("You are joined to the group").setDuration(Snackbar.LENGTH_LONG).setAction(context.getString(android.R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    }).show();
                }
                break;
            case WELCOME_MESSAGE:
                String message = (String) object;
                snackbar.setText(message).setDuration(Snackbar.LENGTH_LONG).setAction("Show", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(SHOW_MESSAGES);
                    }
                }).show();
                break;
            case CONNECTION_DISCONNECTED:
                message = (String) object;
                snackbar.setText(message != null ? message : "Disconnected. Trying to reconnect").setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(TRACKING_STOP);
                    }
                }).show();
                break;
            case CONNECTION_ERROR:
                message = (String) object;
                snackbar.setText(message).setAction("New tracking", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(TRACKING_NEW);
                    }
                }).show();
                break;
            case USER_JOINED:
                user = (MyUser) object;
                if(user.isUser()) {
                    snackbar.setText(user.getProperties().getDisplayName() + " has joined the group.").setDuration(Snackbar.LENGTH_LONG).setAction("Send message", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            user.fire(NEW_MESSAGE);
                        }
                    }).show();
                }
                break;
            case USER_DISMISSED:
                user = (MyUser) object;
                if(user.isUser()) {
                    snackbar.setText(user.getProperties().getDisplayName() + " left the group.").setDuration(Snackbar.LENGTH_LONG).setAction(context.getString(android.R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    }).show();
                }
                break;
            case CUSTOM_SNACK:
                final SnackbarMessage m = (SnackbarMessage) object;
                if(m != null){
                    snackbar.setText(m.getText()).setDuration(m.getDuration()).setAction(m.getTitle(), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //noinspection unchecked
                            m.getAction().call(SnackbarViewHolder.this);
                        }
                    }).show();
                    if(m.getOnClickListener() != null) {
                        snackbar.getView().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                                //noinspection unchecked
                                m.getOnClickListener().call(SnackbarViewHolder.this);
                            }
                        });
                    }
                }
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
                /*case USER_MESSAGE:
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
                    break;*/
            }
            return true;
        }
    }

}
