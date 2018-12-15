package com.edeqa.waytous.holders.view;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Function;
import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.CustomListDialog;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.ShareSender;
import com.edeqa.waytous.helpers.SmoothInterpolated;
import com.edeqa.waytous.helpers.SystemMessage;
import com.edeqa.waytous.helpers.UserMessage;
import com.edeqa.waytous.helpers.Utils;

import java.util.ArrayList;

import static com.edeqa.waytous.Constants.REQUEST_WELCOME_MESSAGE;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.CREATE_CONTEXT_MENU;
import static com.edeqa.waytous.helpers.Events.CREATE_DRAWER;
import static com.edeqa.waytous.helpers.Events.CREATE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.Events.PREPARE_FAB;
import static com.edeqa.waytous.helpers.Events.PREPARE_OPTIONS_MENU;
import static com.edeqa.waytous.helpers.Events.TRACKING_ACTIVE;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_RECONNECTING;
import static com.edeqa.waytous.helpers.Events.TRACKING_STOP;
import static com.edeqa.waytous.helpers.SmoothInterpolated.CURRENT_VALUE;
import static com.edeqa.waytous.helpers.UserMessage.TYPE_PRIVATE;
import static com.edeqa.waytous.holders.property.MessagesHolder.NEW_MESSAGE;
import static com.edeqa.waytous.holders.property.MessagesHolder.PRIVATE_MESSAGE;
import static com.edeqa.waytous.holders.property.MessagesHolder.SEND_MESSAGE;
import static com.edeqa.waytous.holders.property.MessagesHolder.USER_MESSAGE;
import static com.edeqa.waytous.holders.property.MessagesHolder.WELCOME_MESSAGE;
import static com.edeqa.waytous.holders.property.NotificationHolder.HIDE_CUSTOM_NOTIFICATION;


/**
 * Created 11/27/16.
 */
@SuppressWarnings("WeakerAccess")
public class MessagesViewHolder extends AbstractViewHolder {

    public static final String SHOW_MESSAGES = "show_messages"; //NON-NLS
    public static final String SETUP_WELCOME_MESSAGE = "setup_welcome_message"; //NON-NLS

    private static final String PREFERENCE_HIDE_SYSTEM_MESSAGES = "messages_hide_system_messages"; //NON-NLS
    private static final String PREFERENCE_FONT_SIZE = "messages_font_size"; //NON-NLS
    private static final String PREFERENCE_NOT_TRANSPARENT = "messages_not_transparent"; //NON-NLS

    private static final int MESSAGE_MAX_LENGTH = 1024;


    private UserMessage.UserMessagesAdapter adapter;
    private SmoothInterpolated action;
    private RecyclerView list;
    private CustomListDialog dialog;

    private String filterMessage;
    private Integer fontSize;
    private boolean hideSystemMessages;
    private boolean donotscroll;
    private boolean notTransparentWindow;

    public MessagesViewHolder(final MainActivity context) {
        super(context);
        this.dialog = new CustomListDialog(context);
        filterMessage = "";

        context.findViewById(R.id.toolbar).setOnTouchListener(new View.OnTouchListener() {
            float x1,x2;
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getY();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        x2 = event.getY();
                        float deltaX = x2 - x1;
                        if(deltaX > 10) {
                            showMessages();
                        }
                        break;
                }
                return false;
            }
        });
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
            case ACTIVITY_RESUME:
                if(dialog != null && dialog.isShowing()) {
                    dialog.getFooter().setVisibility(State.getInstance().tracking_active() ? View.VISIBLE: View.GONE);
                    dialog.setContext((MainActivity) object);
                    dialog.resize();
                }
                break;
            case TRACKING_ACTIVE:
                if(dialog != null && dialog.getFooter() != null) {
                    dialog.getFooter().setVisibility(State.getInstance().tracking_active() ? View.VISIBLE: View.GONE);
                }
                break;
            case TRACKING_DISABLED:
            case TRACKING_RECONNECTING:
            case TRACKING_STOP:
                if(dialog != null && dialog.getFooter() != null) {
                    dialog.getFooter().setVisibility(State.getInstance().tracking_active() ? View.VISIBLE: View.GONE);
                }
                break;
            case NEW_MESSAGE:
                MyUser to = null;
                if(object instanceof Integer) {
                    to = State.getInstance().getUsers().getUsers().get(object);
                } else if (object instanceof MyUser) {
                    to = (MyUser) object;
                }
                newMessage(to,false,"");
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                assert notificationManager != null;
                notificationManager.cancel(1977);
                break;
            case SHOW_MESSAGES:
                State.getInstance().fire(HIDE_CUSTOM_NOTIFICATION);
                showMessages();
                break;
            case CREATE_DRAWER:
                DrawerViewHolder.ItemsHolder adder = (DrawerViewHolder.ItemsHolder) object;
                adder.add(R.id.drawer_section_communication, R.string.chat, R.string.chat, R.drawable.ic_chat_black_24dp)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                State.getInstance().fire(SHOW_MESSAGES);
                                return false;
                            }
                        });
                break;
            case PREPARE_FAB:
                final FabViewHolder fab = (FabViewHolder) object;
                if(State.getInstance().tracking_active()) {
                    fab.add(R.string.new_message,R.drawable.ic_chat_black_24dp).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fab.close(true);
                            State.getInstance().fire(NEW_MESSAGE);
                        }
                    });
                }
                break;
            case CREATE_OPTIONS_MENU:
                Menu optionsMenu = (Menu) object;
                optionsMenu.add(Menu.NONE, R.string.set_welcome_message, Menu.NONE, R.string.set_welcome_message).setVisible(false).setOnMenuItemClickListener(onMenuItemSetWelcomeMessageClickListener);
                break;
            case PREPARE_OPTIONS_MENU:
                optionsMenu = (Menu) object;
                optionsMenu.findItem(R.string.set_welcome_message).setVisible(State.getInstance().tracking_active() && State.getInstance().getMe().getProperties().getNumber() == 0);
                break;
            case SETUP_WELCOME_MESSAGE:
                onMenuItemSetWelcomeMessageClickListener.onMenuItemClick(null);
                break;
        }
        return true;
    }

    private void newMessage(final MyUser toUser, final boolean privateMessage, String text) {

        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        if (toUser == null) {
            dialog.setTitle(context.getString(R.string.send_message));
        } else {
            dialog.setTitle(context.getString(privateMessage ? R.string.private_message_to_s : R.string.reply_to_s, toUser.getProperties().getDisplayName()));
        }

        @SuppressLint("InflateParams") final View content = context.getLayoutInflater().inflate(R.layout.dialog_new_message, null); //NON-NLS

        final EditText etMessage = content.findViewById(R.id.et_message);
        etMessage.setText(text);

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (etMessage.getText().toString().length() > 0) {
                    if(State.getInstance().tracking_active()) {
                        if (privateMessage && toUser != null) {
                            SystemMessage mm = new SystemMessage(context)
                                    .setFromUser(State.getInstance().getMe())
                                    .setText(etMessage.getText().toString())
                                    .setDelivery(Misc.getUnique())
                                    .setToUser(toUser)
                                    .setType(TYPE_PRIVATE);
                            toUser.fire(SEND_MESSAGE, mm);
                        } else {
                            SystemMessage mm = new SystemMessage(context)
                                    .setFromUser(State.getInstance().getMe())
                                    .setText(etMessage.getText().toString())
                                    .setDelivery(Misc.getUnique());

                            State.getInstance().fire(SEND_MESSAGE, mm);
                        }
                        reloadCursor();
                    } else {
                        new SystemMessage(context).setText(context.getString(R.string.cannot_send_message_because_of_network_is_unavailable)).showSnack();
                    }
                }
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.log(MessagesViewHolder.this, "newMessage:", "Cancel"); //NON-NLS
            }
        });
        if(toUser != null && !privateMessage) {
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.private_string), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    newMessage(toUser, true, etMessage.getText().toString());
                }
            });
        }
        if(privateMessage) {
            dialog.setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.not_private), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    newMessage(toUser, false, etMessage.getText().toString());
                }
            });
        }
        dialog.setView(content);
        dialog.show();
    }

    @SuppressWarnings("unchecked")
    public void showMessages() {
        State.getInstance().fire(HIDE_CUSTOM_NOTIFICATION);

        dialog = new CustomListDialog(context);

        list = dialog.getList();
        adapter = new UserMessage.UserMessagesAdapter(context, list);
        adapter.setEmptyView(dialog.getLayout().findViewById(R.id.tv_placeholder));

        dialog.setAdapter(adapter);
        dialog.setMenu(R.menu.dialog_messages_menu);
        dialog.setOnMenuItemClickListener(onDialogMenuItemClickListener);
        dialog.setFlat(true);

        dialog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                onTouchListener.accept(motionEvent);
                return false;
            }
        });

        final LinearLayout layoutFooter = (LinearLayout) context.getLayoutInflater().inflate(R.layout.view_message_send, null);
        dialog.setFooter(layoutFooter);

        final Consumer<EditText> sender = new Consumer<EditText>() {
            @Override
            public void accept(EditText et) {
                if (et.getText().toString().length() > 0) {
                    if(et.getText().toString().length() > MESSAGE_MAX_LENGTH) {
                        Toast.makeText(context, R.string.too_long_message, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(State.getInstance().tracking_active()) {
                        SystemMessage mm = new SystemMessage(context)
                                .setFromUser(State.getInstance().getMe())
                                .setText(et.getText().toString())
                                .setDelivery(Misc.getUnique());
                        State.getInstance().fire(SEND_MESSAGE, mm);

                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        assert imm != null;
                        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

                        reloadCursor();
                    } else {
                        new SystemMessage(context).setText(context.getString(R.string.cannot_send_message_because_of_network_is_unavailable)).showSnack();
                    }
                }
                et.setText("");
            }
        };

        layoutFooter.findViewById(R.id.ib_message_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sender.accept((EditText)layoutFooter.findViewById(R.id.et_message_send));
            }
        });
        layoutFooter.findViewById(R.id.ib_message_send).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                sender.accept((EditText)layoutFooter.findViewById(R.id.et_message_send));
                dialog.dismiss();
                dialog = null;
                return true;
            }
        });

        hideSystemMessages = State.getInstance().getBooleanPreference(PREFERENCE_HIDE_SYSTEM_MESSAGES, false);
        notTransparentWindow = State.getInstance().getBooleanPreference(PREFERENCE_NOT_TRANSPARENT, false);
        fontSize = State.getInstance().getIntegerPreference(PREFERENCE_FONT_SIZE, 12);

        if(hideSystemMessages) {
            UserMessage.getDb().addRestriction("user", "type_ = ? or type_ = ?", new String[]{""+UserMessage.TYPE_MESSAGE,""+ TYPE_PRIVATE}); //NON-NLS
        }
        context.getSupportLoaderManager().initLoader(2, null, adapter);

        layoutFooter.setVisibility(View.VISIBLE);

        dialog.setSearchListener(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String query) {
                filterMessage = query;
                setFilterAndReload(query);
                return false;
            }
        });

        adapter.setFontSize(fontSize);
        adapter.setOnRightSwipeListener(new Consumer<Integer>() {
            @Override
            public void accept(final Integer position) {
                UserMessage.getDb().deleteByPosition(position);
                adapter.notifyItemRemoved(position);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        donotscroll = true;
                        reloadCursor();
                    }
                }, 500);
            }
        });
        adapter.setOnItemClickListener(new Consumer<UserMessage>() {
            @Override
            public void accept(UserMessage message) {
                reloadCursor();
            }
        });
        adapter.setOnItemShareListener(new Consumer<Integer>() {
            @Override
            public void accept(final Integer position) {
                UserMessage item = UserMessage.getItemByCursor(UserMessage.getDb().getByPosition(position));
                Utils.log(MessagesViewHolder.this, "showMessages:", "item="+item); //NON-NLS

                new ShareSender(context).send(context.getString(R.string.share_the_message), item.getFrom(), item.getFrom() + ":\n" + item.getBody());
            }
        });
        adapter.setOnItemReplyListener(new Consumer<Integer>() {
            @Override
            public void accept(final Integer position) {
                UserMessage item = UserMessage.getItemByCursor(UserMessage.getDb().getByPosition(position));

                MyUser to = State.getInstance().getUsers().findUserByName(item.getFrom());
                if(to != null) {
                    ((EditText) dialog.getFooter().findViewById(R.id.et_message_send)).setText(String.format("> %s", item.getBody()));
                } else {
                    ((EditText) dialog.getFooter().findViewById(R.id.et_message_send)).setText(String.format("> %s:\n> %s", item.getFrom(), item.getBody()));
                }
            }
        });
        adapter.setOnItemDeleteListener(new Consumer<Integer>() {
            @Override
            public void accept(final Integer position) {
                UserMessage.getDb().deleteByPosition(position);
                adapter.notifyItemRemoved(position);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        donotscroll = true;
                        reloadCursor();
                    }
                }, 500);
            }
        });

        adapter.setOnItemTouchListener(onTouchListener);

        adapter.setOnCursorReloadListener(new Consumer<Cursor>() {
            @Override
            public void accept(Cursor cursor) {
                dialog.setTitle(context.getString(R.string.chat_d, cursor.getCount()) + (filterMessage != null && filterMessage.length() > 0 ? " ["+filterMessage+"]" : ""));
                if(!donotscroll) list.scrollToPosition(cursor.getCount() - 1);
                donotscroll = false;
            }
        });
        dialog.show();

        dialog.getFooter().setVisibility(State.getInstance().tracking_active() ? View.VISIBLE : View.GONE);

        prepareToolbarMenu();

        makeDialogTransparent();

        setFilterAndReload(filterMessage);
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void setFilterAndReload(String filter) {
        if(filter != null && filter.length() > 0) {
            UserMessage.getDb().addRestriction("search","from_ LIKE ? OR to_ LIKE ? OR body_ LIKE ?", new String[]{"%"+filter+"%", "%"+filter+"%", "%"+filter+"%"});
        } else {
            UserMessage.getDb().removeRestriction("search");
        }
        Utils.log(MessagesViewHolder.this, "setFilterAndReload:", "Counter="+adapter.getItemCount());
        reloadCursor();
    }

    private void makeDialogTransparent() {
        if(notTransparentWindow) {
            dialog.setAlpha(255);
        } else {
            new SmoothInterpolated(new Consumer<Float[]>() {
                @Override
                public void accept(final Float[] value) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        public void run() {
                        dialog.setAlpha((int) (255 - 155 * value[CURRENT_VALUE]));
                        }
                    });
                }
            }).execute();
        }
    }

    private void reloadCursor(){
        if(context.getSupportLoaderManager().getLoader(2) != null) {
            context.getSupportLoaderManager().getLoader(2).forceLoad();
        }
    }

    private void prepareToolbarMenu() {
        dialog.getMenu().findItem(R.id.hide_system_messages).setVisible(!hideSystemMessages);
        dialog.getMenu().findItem(R.id.show_system_messages).setVisible(hideSystemMessages);
        dialog.getMenu().findItem(R.id.smaller_font).setVisible(fontSize >= 12);
        dialog.getMenu().findItem(R.id.bigger_font).setVisible(fontSize <= 24);
        dialog.getMenu().findItem(R.id.transparent).setVisible(notTransparentWindow);
        dialog.getMenu().findItem(R.id.not_transparent).setVisible(!notTransparentWindow);
    }

    @Override
    public ArrayList<IntroRule> getIntro() {

        ArrayList<IntroRule> rules = new ArrayList<>();
        //noinspection HardCodedStringLiteral
        rules.add(new IntroRule().setEvent(PREPARE_FAB).setId("fab_messages").setViewId(R.string.new_message).setTitle("Here you can").setDescription("Write and send message to the group or private message to anybody."));
//        rules.put(new IntroRule().setEvent(PREPARE_OPTIONS_MENU).setId("menu_set_welcome").setLinkTo(IntroRule.LINK_TO_OPTIONS_MENU).setViewId(R.string.set_welcome_message).setTitle("Here you can").setDescription("Set welcome message to this group."));

        return rules;
    }

    private class MessagesView extends AbstractView {

        MessagesView(MyUser myUser) {
            super(MessagesViewHolder.this.context, myUser);
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
                    if(dialog != null && dialog.isShowing()) {
                        reloadCursor();
                    } else {
                        UserMessage m = (UserMessage) object;

                        //noinspection unchecked
                        new SystemMessage(context).setText(myUser.getProperties().getDisplayName() + ": " + m.getBody()).setDuration(10000).setAction(context.getString(R.string.reply),new Consumer() {
                            @Override
                            public void accept(Object arg) {
                                newMessage(myUser, false,"");
                            }
                        }).setOnClickListener(new Consumer() {
                            @Override
                            public void accept(Object arg) {
                                State.getInstance().fire(SHOW_MESSAGES);
                            }
                        }).showSnack();

                    }
                    break;
                case PRIVATE_MESSAGE:
                    if(dialog != null && dialog.isShowing()) {
                        reloadCursor();
                        return false;
                    } else {
                        String text = (String) object;

                        //noinspection unchecked
                        new SystemMessage(context).setText(String.format(context.getString(R.string.private_s_s), myUser.getProperties().getDisplayName(), text)).setDuration(10000).setAction(context.getString(R.string.reply),new Consumer() {
                            @Override
                            public void accept(Object arg) {
                                newMessage(myUser, true, "");
                            }
                        }).setOnClickListener(new Consumer() {
                            @Override
                            public void accept(Object arg) {
                                State.getInstance().fire(SHOW_MESSAGES);
                            }
                        }).showSnack();
                    }
                    break;
                case NEW_MESSAGE:
                    newMessage(myUser, false, "");
                    break;
                case CREATE_CONTEXT_MENU:
                    Menu menu = (Menu) object;
                    if(myUser != State.getInstance().getMe()) {
                        menu.add(0, R.string.private_message, Menu.NONE, R.string.private_message).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                newMessage(myUser, true, "");
                                return false;
                            }
                        }).setIcon(R.drawable.ic_chat_black_24dp);
                    }
                    break;
            }
            return true;
        }
    }

    private final Consumer<MotionEvent> onTouchListener = new Consumer<MotionEvent>() {
        @Override
        public void accept(MotionEvent motionEvent) {
            if(action != null) action.cancel();
            if(!notTransparentWindow) {
                switch (motionEvent.getAction()) {
                    case 0:
                        dialog.setAlpha(255);
                        break;
                    case 1:
                        action = new SmoothInterpolated(new Consumer<Float[]>() {
                            @Override
                            public void accept(final Float[] value) {
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    public void run() {
                                        dialog.setAlpha((int) (255 - 155 * value[CURRENT_VALUE]));
                                    }
                                });
                            }
                        }).setDuration(320);
                        action.execute();
                        break;
                }
            }
        }
    };

    private final MenuItem.OnMenuItemClickListener onMenuItemSetWelcomeMessageClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();
            dialog.setTitle(context.getString(R.string.set_welcome_message));

            View view = context.getLayoutInflater().inflate(R.layout.dialog_welcome_message, null);

            final EditText etMessage = view.findViewById(R.id.et_welcome_message);
            final CheckBox cbSaveAsDefault = view.findViewById(R.id.cb_save_as_default);

            etMessage.setText(State.getInstance().getStringPreference(WELCOME_MESSAGE, ""));

            dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if(State.getInstance().tracking_active() && etMessage.getText().toString().length()>0) {
                        State.getInstance().getTracking().put(REQUEST_WELCOME_MESSAGE, etMessage.getText().toString()).send(REQUEST_WELCOME_MESSAGE);
                        if(cbSaveAsDefault.isChecked()) {
                            State.getInstance().setPreference(WELCOME_MESSAGE, etMessage.getText().toString());
                        }
                    }
                }
            });

            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Utils.log(MessagesViewHolder.this, "onMenuItemSetWelcomeMessageClickListener:", "Cancel"); //NON-NLS
                }
            });

            dialog.setView(view);

            dialog.show();

            return false;
        }
    };

    private final Toolbar.OnMenuItemClickListener onDialogMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch(menuItem.getItemId()) {
                case R.id.hide_system_messages:
                    State.getInstance().setPreference(PREFERENCE_HIDE_SYSTEM_MESSAGES, true);
                    hideSystemMessages = true;
                    UserMessage.getDb().addRestriction("user", "type_ = ? or type_ = ?", new String[]{""+UserMessage.TYPE_MESSAGE,""+ TYPE_PRIVATE}); //NON-NLS
                    reloadCursor();
                    break;
                case R.id.show_system_messages:
                    UserMessage.getDb().removeRestriction("user"); //NON-NLS
                    hideSystemMessages = false;
                    State.getInstance().setPreference(PREFERENCE_HIDE_SYSTEM_MESSAGES, false);
                    reloadCursor();
                    break;
                case R.id.transparent:
                    State.getInstance().setPreference(PREFERENCE_NOT_TRANSPARENT, false);
                    notTransparentWindow = false;
                    makeDialogTransparent();
                    break;
                case R.id.not_transparent:
                    State.getInstance().setPreference(PREFERENCE_NOT_TRANSPARENT, true);
                    notTransparentWindow = true;
                    makeDialogTransparent();
                    break;
                case R.id.smaller_font:
                    fontSize -= 2;
                    State.getInstance().setPreference(PREFERENCE_FONT_SIZE, fontSize);
                    adapter.setFontSize(fontSize);
                    adapter.notifyDataSetChanged();
                    dialog.openMenu();
                    break;
                case R.id.bigger_font:
                    fontSize += 2;
                    State.getInstance().setPreference(PREFERENCE_FONT_SIZE, fontSize);
                    adapter.setFontSize(fontSize);
                    adapter.notifyDataSetChanged();
                    dialog.openMenu();
                    break;
                case R.id.clear_messages:
                    AlertDialog dialog = new AlertDialog.Builder(context).create();
                    dialog.setTitle(context.getString(R.string.clear_all_messages));
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            UserMessage.clear();
                            reloadCursor();
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
            prepareToolbarMenu();
            return false;
        }
    };
}
