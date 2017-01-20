package ru.wtg.whereaminow.holders;

import android.support.design.widget.Snackbar;
import android.view.View;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.SystemMessage;

import static ru.wtg.whereaminow.State.EVENTS.TOKEN_CREATED;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_ACTIVE;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_ERROR;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_JOIN;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_NEW;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_RECONNECTING;
import static ru.wtg.whereaminow.State.EVENTS.TRACKING_STOP;
import static ru.wtg.whereaminow.holders.MessagesHolder.NEW_MESSAGE;
import static ru.wtg.whereaminow.holders.MessagesHolder.WELCOME_MESSAGE;
import static ru.wtg.whereaminow.holders.MessagesViewHolder.SETUP_WELCOME_MESSAGE;
import static ru.wtg.whereaminow.holders.MessagesViewHolder.SHOW_MESSAGES;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;

/**
 * Created 11/27/16.
 */
public class SnackbarViewHolder extends AbstractViewHolder {

    public static final String TYPE = "snackbar";
    public static final String CUSTOM_SNACK = "custom_snack";

    private final MainActivity context;
    private Snackbar snackbar;

    private boolean tokenCreatedShown = false;

    public SnackbarViewHolder(MainActivity context) {
        this.context = context;

        setLayout(context.findViewById(R.id.fab_layout));
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    public void setLayout(final View layout) {

        snackbar = Snackbar.make(layout, "Starting...", Snackbar.LENGTH_INDEFINITE);

        snackbar.getView().setAlpha(.8f);
        snackbar.setAction("Action", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("SNACKBAR ACTION");
            }
        });
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                setLayout(layout);
            }
        });
    }

    @Override
    public boolean onEvent(String event, Object object) {
        final MyUser user;
        switch(event){
            case TRACKING_NEW:
                snackbar.setText("Creating group...").setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(TRACKING_STOP);
                    }
                }).show();
                break;
            case TRACKING_JOIN:
                snackbar.setText("Joining group...").setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(TRACKING_STOP);
                    }
                }).show();
                break;
            case TOKEN_CREATED:
                tokenCreatedShown = true;
                snackbar.setText("You have created the group").setDuration(10000).setAction("Set welcome message", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(SETUP_WELCOME_MESSAGE);
                    }
                }).show();
                break;
            case TRACKING_ACTIVE:
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
            case TRACKING_STOP:
                snackbar.dismiss();
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
            case TRACKING_RECONNECTING:
//                tokenCreatedShown = true;
                message = (String) object;
                snackbar.setText((message != null && message.length() > 0) ? message : "Disconnected. Trying to reconnect").setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(TRACKING_STOP);
                    }
                }).show();
                break;
            case TRACKING_ERROR:
                message = (String) object;
                snackbar.setText(message).setAction("New group", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
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
                final SystemMessage m = (SystemMessage) object;
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

}
