package ru.wtg.whereaminow.holders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.AbstractSavedItem;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.SmoothInterpolated;
import ru.wtg.whereaminow.helpers.SnackbarMessage;
import ru.wtg.whereaminow.helpers.UserMessage;
import ru.wtg.whereaminow.helpers.Utils;
import ru.wtg.whereaminow.interfaces.SimpleCallback;
import ru.wtg.whereaminow.interfaces.TypedCallback;

import static ru.wtg.whereaminow.State.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.CREATE_DRAWER;
import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.PREPARE_DRAWER;
import static ru.wtg.whereaminow.State.PREPARE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.TOKEN_CHANGED;
import static ru.wtg.whereaminow.helpers.SmoothInterpolated.CURRENT_VALUE;
import static ru.wtg.whereaminow.holders.MessagesHolder.NEW_MESSAGE;
import static ru.wtg.whereaminow.holders.MessagesHolder.PRIVATE_MESSAGE;
import static ru.wtg.whereaminow.holders.MessagesHolder.SEND_MESSAGE;
import static ru.wtg.whereaminow.holders.MessagesHolder.TYPE;
import static ru.wtg.whereaminow.holders.MessagesHolder.USER_MESSAGE;
import static ru.wtg.whereaminow.holders.MessagesHolder.WELCOME_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_PRIVATE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_WELCOME_MESSAGE;

/**
 * Created 11/27/16.
 */
public class MessagesViewHolder extends AbstractViewHolder {

    public static final String SHOW_MESSAGES = "show_messages";
    public static final String SETUP_WELCOME_MESSAGE = "setup_welcome_message";

    private static final String PREFERENCE_HIDE_SYSTEM_MESSAGES = "messagesView_hide_system_messages";

    private final Activity context;
    private final MessagesHolder messagesHolder;
    private UserMessage.UserMessagesAdapter adapter;
    private AlertDialog dialog;
    private View toolbar;
    private ColorDrawable drawable;
//    private LinearLayoutManager layoutManager;
    private RecyclerView list;

    public MessagesViewHolder(Activity context) {
        this.context = context;
        messagesHolder = ((MessagesHolder)State.getInstance().getEntityHolder(MessagesHolder.TYPE));
    }

    @Override
    public String getType() {
        return "messagesView";
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public MessagesView create(MyUser myUser) {
        if (myUser == null) return null;
        return new MessagesView(myUser);
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch (event) {
            case NEW_MESSAGE:
                newMessage(null,false);
                break;
            case SHOW_MESSAGES:
                showMessages();
                break;
            case TOKEN_CHANGED:
                if(adapter != null){
                    adapter.notifyDataSetChanged();
                }
                break;
            case CREATE_DRAWER:
                MenuItem menuItem = (MenuItem) object;
                Menu generalMenu = menuItem.getSubMenu();
                MenuItem item = generalMenu.findItem(R.string.chat);
                if(item == null) {
                    item = generalMenu.add(Menu.NONE, R.string.chat, Menu.NONE, context.getString(R.string.chat));
                }
                item.setIcon(R.drawable.ic_chat_black_24dp)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                State.getInstance().fire(SHOW_MESSAGES);
                                return false;
                            }
                        });
                break;
            case PREPARE_DRAWER:
                menuItem = (MenuItem) object;
                generalMenu = menuItem.getSubMenu();
                int count = UserMessage.getCount(context);
                generalMenu.findItem(R.string.chat).setVisible(count > 0);
                if(count>0) {
                    menuItem.setVisible(true);
                }
                break;
            case CREATE_OPTIONS_MENU:
                Menu optionsMenu = (Menu) object;
                optionsMenu.add(Menu.NONE, R.string.set_welcome_message, Menu.NONE, R.string.set_welcome_message).setVisible(false).setOnMenuItemClickListener(onMenuItemSetWelcomeMessageClickListener);
                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                optionsMenu.findItem(R.string.set_welcome_message).setVisible(State.getInstance().tracking() && State.getInstance().getMe().getProperties().getNumber() == 0);
                break;
            case SETUP_WELCOME_MESSAGE:
                onMenuItemSetWelcomeMessageClickListener.onMenuItemClick(null);
                break;
        }
        return true;
    }

    private void newMessage(final MyUser toUser, final boolean privateMessage) {

        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        if (toUser == null) {
            dialog.setTitle("Send message");
        } else {
            dialog.setTitle((privateMessage ? "Private message to " : "Reply to ") + toUser.getProperties().getDisplayName());
        }

        @SuppressLint("InflateParams") View content = context.getLayoutInflater().inflate(R.layout.dialog_new_message, null);

        final EditText etMessage = (EditText) content.findViewById(R.id.et_message);

            dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (etMessage.getText().toString().length() > 0) {
                        if (privateMessage && toUser != null) {
                            JSONObject o = new JSONObject();
                            try {
                                o.put(RESPONSE_PRIVATE, toUser.getProperties().getNumber());
                                o.put(ru.wtg.whereaminowserver.helpers.Constants.USER_MESSAGE, etMessage.getText().toString());
                                State.getInstance().getTracking().sendMessage(o);
                                toUser.fire(SEND_MESSAGE, etMessage.getText().toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            State.getInstance().getTracking().sendMessage(ru.wtg.whereaminowserver.helpers.Constants.USER_MESSAGE, etMessage.getText().toString());
                            State.getInstance().fire(SEND_MESSAGE, etMessage.getText().toString());
                        }
                    }
                }
            });


        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                System.out.println("cancel");
            }
        });

        if(toUser != null && !privateMessage) {
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Private", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    newMessage(toUser, true);
                }
            });
        }

        dialog.setView(content);

        dialog.show();
    }

    private void showMessages() {

        dialog = new AlertDialog.Builder(context).create();

        @SuppressLint("InflateParams") View content = context.getLayoutInflater().inflate(R.layout.dialog_items, null);

        list = (RecyclerView) content.findViewById(R.id.list_items);

        adapter = new UserMessage.UserMessagesAdapter(context, list);

        toolbar = context.getLayoutInflater().inflate(R.layout.dialog_items_toolbar, null);
        final ImageButton ibMenu = (ImageButton) toolbar.findViewById(R.id.ib_dialog_items_menu);

        final boolean hideSystemMessages = State.getInstance().getBooleanPreference(PREFERENCE_HIDE_SYSTEM_MESSAGES, false);
        if(hideSystemMessages) {
            UserMessage.setRestrictions(context, new TypedCallback<Boolean, Integer, UserMessage>() {
                @Override
                public Boolean call(Integer i, UserMessage msg) {
//                            UserMessage msg = (UserMessage) Utils.deserializeFromString(arg);
                            if(msg.getType() == UserMessage.TYPE_PRIVATE || msg.getType() == UserMessage.TYPE_MESSAGE) return true;
                            return false;
//                    return true;
                }
            });
        }

        ibMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(context, view);
                context.getMenuInflater().inflate(R.menu.dialog_messages_menu, popup.getMenu());

                popup.getMenu().findItem(R.id.hide_system_messages).setVisible(!hideSystemMessages);
                popup.getMenu().findItem(R.id.show_system_messages).setVisible(hideSystemMessages);

                popup.show();
                popup.setOnMenuItemClickListener(onDialogMenuItemClickListener);
            }
        });


        dialog.setCustomTitle(toolbar);




        adapter.notifyDataSetChanged();
        adapter.setOnItemClickListener(new SimpleCallback<UserMessage>() {
            @Override
            public void call(UserMessage message) {
                System.out.println(message);


//                dialog.dismiss();
//                dialog = null;
            }
        });
        adapter.setOnItemTouchListener(onTouchListener);

        updateDialogTitle();

        if(State.getInstance().tracking()) {
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "New message", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog = null;
                    newMessage(null, false);
                }
            });
        }

        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog = null;
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialog = null;
            }
        });

        dialog.setView(content);

        drawable = new ColorDrawable(Color.WHITE);
        if(dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(drawable);
        }

        dialog.show();

        new SmoothInterpolated(new SimpleCallback<Float[]>() {
            @Override
            public void call(Float[] value) {
                drawable.setAlpha((int) (255 - 155 * value[CURRENT_VALUE]));
            }
        }).execute();

        dialog.getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                onTouchListener.call(motionEvent);
                return false;
            }
        });
//        list.setOnTouchListener(onTouchListener);

        System.out.println("MESSCOUNTL:"+adapter.getItemCount());
        list.scrollToPosition(adapter.getItemCount()-1);

    }

    private void updateDialogTitle(){
        if(toolbar != null) {
            ((TextView) toolbar.findViewById(R.id.tv_dialog_items_title)).setText("Chat (" + adapter.getItemCount() + ")");
        }
    }

    private SmoothInterpolated action;
    private SimpleCallback<MotionEvent> onTouchListener = new SimpleCallback<MotionEvent>() {
        @Override
        public void call(MotionEvent motionEvent) {
            if(action != null) action.cancel();
            switch(motionEvent.getAction()){
                case 0:
                    drawable.setAlpha(255);
                    break;
                case 1:
                    action = new SmoothInterpolated(new SimpleCallback<Float[]>() {
                        @Override
                        public void call(Float[] value) {
                            drawable.setAlpha((int) (255 - 155 * value[CURRENT_VALUE]));
                        }
                    }).setDuration(320);
                    action.execute();
                    break;
            }
        }
    };


    private class MessagesView extends AbstractView {

        MessagesView(MyUser myUser) {
            super(myUser);
        }

        @Override
        public boolean dependsOnLocation() {
            return false;
        }

        @Override
        public boolean onEvent(String event, Object object) {
            if(myUser.getLocation() != null && !myUser.isUser()) return true;
            switch (event) {
                case USER_MESSAGE:
                    if(dialog != null) {
                        adapter.notifyDataSetChanged();
                        list.scrollToPosition(adapter.getItemCount()-1);
                        updateDialogTitle();
                    } else {
                        String text = (String) object;

                        //noinspection unchecked
                        new SnackbarMessage().setText(myUser.getProperties().getDisplayName() + ": " + text).setDuration(10000).setAction("Reply",new SimpleCallback() {
                            @Override
                            public void call(Object arg) {
                                newMessage(myUser, false);
                            }
                        }).setOnClickListener(new SimpleCallback() {
                            @Override
                            public void call(Object arg) {
                                State.getInstance().fire(SHOW_MESSAGES);
                            }
                        }).show();

                    }
                    break;
                case PRIVATE_MESSAGE:
                    if(dialog != null) {
                        adapter.notifyDataSetChanged();
                        list.scrollToPosition(adapter.getItemCount()-1);
                        updateDialogTitle();
                        return false;
                    } else {
                        String text = (String) object;

                        //noinspection unchecked
                        new SnackbarMessage().setText("(private) " + myUser.getProperties().getDisplayName() + ": " + text).setDuration(10000).setAction("Reply",new SimpleCallback() {
                            @Override
                            public void call(Object arg) {
                                newMessage(myUser, true);
                            }
                        }).setOnClickListener(new SimpleCallback() {
                            @Override
                            public void call(Object arg) {
                                State.getInstance().fire(SHOW_MESSAGES);
                            }
                        }).show();
                    }
                    break;
                case NEW_MESSAGE:
                    newMessage(myUser, false);
                    break;
                case CREATE_CONTEXT_MENU:
                    ContextMenu menu = (ContextMenu) object;
                    if(myUser != State.getInstance().getMe()) {
                        menu.add("Private message").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                newMessage(myUser, true);
                                return false;
                            }
                        });
                    }
                    break;
            }
            return true;
        }

    }

    MenuItem.OnMenuItemClickListener onMenuItemSetWelcomeMessageClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();
            dialog.setTitle("Set welcome message");

            @SuppressLint("InflateParams") View content = context.getLayoutInflater().inflate(R.layout.dialog_welcome_message, null);

            final EditText etMessage = (EditText) content.findViewById(R.id.et_welcome_message);
            final CheckBox cbSaveAsDefault = (CheckBox) content.findViewById(R.id.cb_save_as_default);

            etMessage.setText(State.getInstance().getStringPreference(WELCOME_MESSAGE, ""));

            dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(State.getInstance().tracking() && etMessage.getText().toString().length()>0) {
                        State.getInstance().getTracking().sendMessage(RESPONSE_WELCOME_MESSAGE, etMessage.getText().toString());
                        if(cbSaveAsDefault.isChecked()) {
                            State.getInstance().setPreference(WELCOME_MESSAGE, etMessage.getText().toString());
                        }
                    }
                }
            });

            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    System.out.println("cancel");
                }
            });

            dialog.setView(content);

            dialog.show();

            return false;
        }
    };

    PopupMenu.OnMenuItemClickListener onDialogMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch(menuItem.getItemId()) {
                case R.id.hide_system_messages:
                    State.getInstance().setPreference(PREFERENCE_HIDE_SYSTEM_MESSAGES, true);
                    UserMessage.setRestrictions(context, new TypedCallback<Boolean,Integer,UserMessage>() {
                        @Override
                        public Boolean call(Integer i,UserMessage msg) {
//                            UserMessage msg = (UserMessage) Utils.deserializeFromString(arg);
                            if(msg.getType() == UserMessage.TYPE_PRIVATE || msg.getType() == UserMessage.TYPE_MESSAGE) return true;
                            return false;
//                            return true;
                        }
                    });
                    adapter.notifyDataSetChanged();
                    updateDialogTitle();
                    break;
                case R.id.show_system_messages:
                    State.getInstance().setPreference(PREFERENCE_HIDE_SYSTEM_MESSAGES, false);
                    UserMessage.setRestrictions(context, null);
                    adapter.notifyDataSetChanged();
                    updateDialogTitle();
                    break;
                case R.id.clear_messages:
                    AlertDialog dialog = new AlertDialog.Builder(context).create();
                    dialog.setTitle("Clear all messages?");
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            UserMessage.clear(context);
                            if(adapter != null) adapter.notifyDataSetChanged();
                            updateDialogTitle();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                        }
                    });
                    dialog.show();
                    break;
            }
            return false;
        }
    };

}
