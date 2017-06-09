package com.edeqa.waytous.holders;

import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.view.View;


import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SystemMessage;

import static com.edeqa.waytous.helpers.Events.TOKEN_CREATED;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_ERROR;
import static com.edeqa.waytous.helpers.Events.TRACKING_JOIN;
import static com.edeqa.waytous.helpers.Events.TRACKING_NEW;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;
import static com.edeqa.waytous.helpers.Events.TRACKING_STOP;
import static com.edeqa.waytous.holders.MessagesHolder.NEW_MESSAGE;
import static com.edeqa.waytous.holders.MessagesHolder.WELCOME_MESSAGE;
import static com.edeqa.waytous.holders.MessagesViewHolder.SETUP_WELCOME_MESSAGE;
import static com.edeqa.waytous.holders.MessagesViewHolder.SHOW_MESSAGES;
import static com.edeqa.waytousserver.helpers.Constants.USER_DISMISSED;
import static com.edeqa.waytousserver.helpers.Constants.USER_JOINED;

/**
 * Created 11/27/16.
 */
public class SnackbarViewHolder extends AbstractViewHolder {

    public static final String TYPE = "snackbar";
    public static final String CUSTOM_SNACK = "custom_snack";

    private Snackbar snackbar;

    private boolean tokenCreatedShown = false;

    public SnackbarViewHolder(MainActivity context) {
        super(context);

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

        snackbar = Snackbar.make(layout, R.string.starting, Snackbar.LENGTH_INDEFINITE);

        snackbar.getView().setAlpha(.8f);
        snackbar.setAction(R.string.action, new View.OnClickListener() {
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
                snackbar.setText(context.getString(R.string.creating_group)).setAction(R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(TRACKING_STOP);
                    }
                }).show();
                break;
            case TRACKING_JOIN:
                snackbar.setText(context.getString(R.string.joining_group)).setAction(context.getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(TRACKING_STOP);
                    }
                }).show();
                break;
            case TOKEN_CREATED:
                tokenCreatedShown = true;
                snackbar.setText(R.string.you_have_created_the_group).setDuration(10000).setAction(context.getString(R.string.set_welcome_message), new View.OnClickListener() {
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
                    snackbar.setText(R.string.you_are_joined_to_the_group).setDuration(Snackbar.LENGTH_LONG).setAction(context.getString(android.R.string.ok), new View.OnClickListener() {
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
                snackbar.setText(message).setDuration(Snackbar.LENGTH_LONG).setAction(context.getString(R.string.show), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(SHOW_MESSAGES);
                    }
                }).show();
                break;
            case TRACKING_RECONNECTING:
//                tokenCreatedShown = true;
                message = (String) object;
                snackbar.setText((message != null && message.length() > 0) ? message : context.getString(R.string.disconnected_trying_to_reconnect)).setAction(context.getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State.getInstance().fire(TRACKING_STOP);
                    }
                }).show();
                break;
            case TRACKING_ERROR:
                message = (String) object;
                snackbar.setText(message).setAction(R.string.new_group, new View.OnClickListener() {
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
                    snackbar.setText(context.getString(R.string.s_has_joined, user.getProperties().getDisplayName())).setDuration(Snackbar.LENGTH_LONG).setAction(context.getString(R.string.send_message), new View.OnClickListener() {
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
                    snackbar.setText(context.getString(R.string.s_has_left, user.getProperties().getDisplayName())).setDuration(Snackbar.LENGTH_LONG).setAction(context.getString(android.R.string.ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        }
                    }).show();
                }
                break;
            case CUSTOM_SNACK:
                final SystemMessage m = (SystemMessage) object;
                if(m != null){
                    snackbar.dismiss();
                    new Handler(Looper.myLooper()).post(new Runnable() {
                        @Override
                        public void run() {
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
                    });
                }
                break;
        }
        return true;
    }

}
