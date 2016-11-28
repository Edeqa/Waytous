package ru.wtg.whereaminow.holders;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.Utils;

import static ru.wtg.whereaminow.State.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.NEW_MESSAGE;
import static ru.wtg.whereaminow.State.PRIVATE_MESSAGE;
import static ru.wtg.whereaminow.State.SEND_MESSAGE;
import static ru.wtg.whereaminow.State.SHOW_MESSAGES;
import static ru.wtg.whereaminow.State.USER_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_PRIVATE;

/**
 * Created 11/27/16.
 */
public class MessagesViewHolder extends AbstractViewHolder {

    private final Activity context;
    private final MessagesHolder messagesHolder;

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
    public String[] getOwnEvents() {
        return new String[0];
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
        }
        return true;
    }


    private void newMessage(final MyUser toUser, final boolean privateMessage) {

        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        if (toUser == null) {
            dialog.setTitle("Send message");
        } else {
            dialog.setTitle((privateMessage ? "Private message to " : "Reply to ") + toUser.getProperties().getName());
        }

        @SuppressLint("InflateParams") View content = context.getLayoutInflater().inflate(R.layout.dialog_new_message, null);

        final EditText etMessage = (EditText) content.findViewById(R.id.etMessage);

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(privateMessage){
                    JSONObject o = new JSONObject();
                    try {
                        o.put(RESPONSE_PRIVATE, toUser.getProperties().getNumber());
                        o.put(ru.wtg.whereaminowserver.helpers.Constants.USER_MESSAGE, etMessage.getText().toString());
                        State.getInstance().myTracking.sendMessage(o);
                        toUser.fire(SEND_MESSAGE, etMessage.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    State.getInstance().myTracking.sendMessage(ru.wtg.whereaminowserver.helpers.Constants.USER_MESSAGE, etMessage.getText().toString());
                    State.getInstance().fire(SEND_MESSAGE, etMessage.getText().toString());
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

        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle("Chat");

        @SuppressLint("InflateParams") View content = context.getLayoutInflater().inflate(R.layout.dialog_show_messages, null);

        RecyclerView list = (RecyclerView) content.findViewById(R.id.listMessages);
        MessagesAdapter adapter = new MessagesAdapter(context);
        /*adapter.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });*/
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        list.setItemAnimator(itemAnimator);

        list.setLayoutManager(new LinearLayoutManager(context));


        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "New message", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                newMessage(null,false);
            }
        });

        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        dialog.setView(content);

        ColorDrawable drawable = new ColorDrawable(Color.WHITE);
        drawable.setAlpha(100);
        dialog.getWindow().setBackgroundDrawable(drawable);


        dialog.show();
    }

    private class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder>{

        private final Context context;

        public MessagesAdapter(Context context){
            this.context = context;
        }

        @Override
        public MessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message, parent, false);
//            v.setOnClickListener(onItemClickListener);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MessagesAdapter.ViewHolder holder, int position) {
            MessagesHolder.UserMessage message = messagesHolder.getMessages().get(position);
            holder.tvUserName.setText(message.getFrom().getProperties().getName());
            holder.tvTimestamp.setText(message.getTimestamp().toLocaleString());
            holder.tvMessageBody.setText(message.getBody());
        }

        @Override
        public int getItemCount() {
            return messagesHolder.getCount();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvUserName;
            private final TextView tvTimestamp;
            private final TextView tvMessageBody;

            private ViewHolder(View view) {
                super(view);
                tvUserName = (TextView) view.findViewById(R.id.tvUserName);
                tvTimestamp = (TextView) view.findViewById(R.id.tvTimestamp);
                tvMessageBody = (TextView) view.findViewById(R.id.tvMessageBody);
            }
        }
    }

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
            switch (event) {
//                case USER_MESSAGE:
//                    String text = (String) object;
//                    State.getInstance().fire(USER_MESSAGE, myUser.getProperties().getName() + ": " + text);
//                    break;
                case NEW_MESSAGE:
                    newMessage(myUser, false);
                    break;
                case CREATE_CONTEXT_MENU:
                    ContextMenu menu = (ContextMenu) object;

                    MenuItem item = menu.findItem(R.id.action_private_message);
                    if(myUser == State.getInstance().getMe()) {
                        item.setVisible(false);
                    } else {
                        item.setVisible(true);
                        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                newMessage(myUser, true);
                                return false;
                            }
                        });
                    }
                    break;
                case CREATE_OPTIONS_MENU:
                    Menu optionsMenu = (Menu) object;

                    item = optionsMenu.findItem(R.id.action_show_messages);
                    if(messagesHolder.getCount() > 0) {
                        item.setVisible(true);
                        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                State.getInstance().fire(SHOW_MESSAGES);
                                return false;
                            }
                        });
                    } else {
                        item.setVisible(false);
                    }
                    break;
            }
            return true;
        }

    }

}
