package com.edeqa.waytous.helpers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edeqa.waytous.abstracts.AbstractSavedItem;
import com.edeqa.waytous.interfaces.SimpleCallback;

import java.util.Date;


import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created 12/9/16.
 */

public class UserMessage extends AbstractSavedItem {

    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_PRIVATE = 1;
    public static final int TYPE_JOINED = 2;
    public static final int TYPE_USER_JOINED = 3;
    public static final int TYPE_USER_DISMISSED = 4;
    static final long serialVersionUID = -6395904747332820028L;
    private static final String MESSAGE = "message";

    private String key;
    private String from;
    private String to;
    private String body;
    private String delivery;
    private long timestamp;
    private int type;

    public UserMessage(Context context) {
        super(context, MESSAGE);
        timestamp = new Date().getTime();
        type = TYPE_MESSAGE;
    }

    public UserMessage(SystemMessage systemMessage) {
        super(systemMessage.getContext(), MESSAGE);

        if(systemMessage.getFromUser() != null) {
            from = systemMessage.getFromUser().getProperties().getDisplayName();
        }
        if(systemMessage.getToUser() != null) {
            to = systemMessage.getToUser().getProperties().getDisplayName();
        }
        if(systemMessage.getText() != null) {
            body = systemMessage.getText();
        }
        if(systemMessage.getDelivery() != null) {
            delivery = systemMessage.getDelivery();
        }
        timestamp = new Date().getTime();
        type = systemMessage.getType();

    }

    protected UserMessage(Context context, String itemType) {
        super(context, itemType);
    }

    public static void init(Context context) {
        init(context, UserMessage.class, MESSAGE);
    }

    public static DBHelper getDb(){
        return getDb(MESSAGE);
    }

    public static int getCount(){
        return getCount(MESSAGE);
    }

    public static void clear(){
        clear(MESSAGE);
    }

    public static UserMessage getItemByPosition(int position) {
        return (UserMessage) getItemByPosition(MESSAGE, position);
    }

    public static UserMessage getItemByNumber(long number) {
        return (UserMessage) getItemByNumber(MESSAGE, number);
    }

    public static UserMessage getItemByCursor(Cursor cursor) {
        return (UserMessage) getItemByCursor(MESSAGE, cursor);
    }

    public static UserMessage getItemByFieldValue(String field, String value) {
        return (UserMessage) getItemByFieldValue(MESSAGE, field, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getTimestamp() {
        return new Date(timestamp);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(MyUser from) {
        this.from = from.getProperties().getDisplayName();
    }

    public String getTo() {
        return to;
    }

    public void setTo(MyUser to) {
        this.to = to.getProperties().getDisplayName();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    public String toString() {
        return "{ timestamp: " + new Date(timestamp).toString()
                + ", number: " + getNumber()
                + ", type: " + type
                + (from != null ? ", from: "+from : "")
                + (to != null ? ", to: "+to : "")
                + (body != null ? ", body: ["+body + "]" : "")
                + (key != null ? ", key: ["+key + "]" : "")
                + " }";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    static public class UserMessagesAdapter extends AbstractSavedItemsAdapter {

        private Integer fontSize;
        private SimpleCallback<Integer> onItemDeleteListener;
        private SimpleCallback<Integer> onItemShareListener;
        private SimpleCallback<Integer> onItemReplyListener;

        public UserMessagesAdapter(Context context, RecyclerView list) {
            super(context, list);
        }

        @Override
        public UserMessage.UserMessagesAdapter.UserMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message, parent, false);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.call(view);
                }
            });
            return new UserMessageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final Cursor cursor) {
            try {
                final UserMessage.UserMessagesAdapter.UserMessageViewHolder holder = (UserMessage.UserMessagesAdapter.UserMessageViewHolder) viewHolder;

                final UserMessage item = UserMessage.getItemByCursor(cursor);
                System.out.println("MESSAGE:"+item);
                String title = "";
                String from = item.getFrom();
                if(from != null && item.getTo() != null) {
                    title = from + " → " + item.getTo();
                } else if(from != null) {
                    title = from;
                }
                holder.itemView.setTag(cursor.getPosition());

                holder.tvUsername.setText(title);
                holder.tvUsername.setTextSize(fontSize);
                holder.tvTimestamp.setText(item.getTimestamp().toString());

                holder.tvMessageBody.setText(item.getBody());
                Linkify.addLinks(holder.tvMessageBody, Linkify.ALL);
                holder.tvMessageBody.setLinksClickable(true);
                holder.tvMessageBody.setTextSize(fontSize);


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int v = holder.layoutMessageButtons.getVisibility();

                        ViewGroup parent = (ViewGroup) view.getParent();
                        for(int i = 0; i < parent.getChildCount(); i++) {
                            parent.getChildAt(i).findViewById(R.id.layout_message_buttons).setVisibility(GONE);
                        }

                        switch(v){
                            case VISIBLE:
                                holder.layoutMessageButtons.setVisibility(View.GONE);
                                break;
                            case GONE:
                                holder.layoutMessageButtons.setVisibility(View.VISIBLE);
                                break;
                        }
//                        onItemClickListener.call(item);
                    }
                });
                holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        onItemTouchListener.call(motionEvent);
                        return false;
                    }
                });
                holder.ibMessageShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.layoutMessageButtons.setVisibility(GONE);
                        int position = (int) holder.itemView.getTag();
                        onItemShareListener.call(position);
                    }
                });
                holder.ibMessageReply.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.layoutMessageButtons.setVisibility(GONE);
                        int position = (int) holder.itemView.getTag();
                        onItemReplyListener.call(position);
                    }
                });
                holder.ibMessageDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.layoutMessageButtons.setVisibility(GONE);
                        int position = (int) holder.itemView.getTag();
                        onItemDeleteListener.call(position);
                    }
                });

                if(from != null && from.equals(State.getInstance().getMe().getProperties().getDisplayName())){
                    holder.itemView.setBackgroundColor(Color.argb(32,0,0,255));
                    holder.ivMessageDelivered.setColorFilter("delivered".equals(item.getDelivery()) ? Color.rgb(0,150,0) : Color.LTGRAY);
                    holder.ivMessageDelivered.setVisibility(VISIBLE);
                } else {
                    holder.ivMessageDelivered.setVisibility(GONE);
                    holder.itemView.setBackgroundColor(android.R.color.background_light);
                }

            }catch(Exception e){e.printStackTrace();}
        }

        public void setFontSize(Integer fontSize) {
            this.fontSize = fontSize;
        }

        public void setOnItemDeleteListener(SimpleCallback<Integer> onItemDeleteListener) {
            this.onItemDeleteListener = onItemDeleteListener;
        }

        public void setOnItemShareListener(SimpleCallback<Integer> onItemShareListener) {
            this.onItemShareListener = onItemShareListener;
        }

        public void setOnItemReplyListener(SimpleCallback<Integer> onItemReplyListener) {
            this.onItemReplyListener = onItemReplyListener;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new SavedItemCursorLoader(context, MESSAGE);
        }

        class UserMessageViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvTimestamp;
            private final TextView tvMessageBody;
            private final TextView tvUsername;
            private final LinearLayout layoutMessageButtons;
            private final ImageButton ibMessageShare;
            private final ImageButton ibMessageReply;
            private final ImageButton ibMessageDelete;
            private final ImageView ivMessageDelivered;

            private UserMessageViewHolder(View view) {
                super(view);
                tvUsername = (TextView) view.findViewById(R.id.tv_username);
                tvTimestamp = (TextView) view.findViewById(R.id.tvTimestamp);
                tvMessageBody = (TextView) view.findViewById(R.id.tv_message_body);
                layoutMessageButtons = (LinearLayout) view.findViewById(R.id.layout_message_buttons);
                ibMessageShare = (ImageButton) view.findViewById(R.id.ib_message_share);
                ibMessageReply = (ImageButton) view.findViewById(R.id.ib_message_reply);
                ibMessageDelete = (ImageButton) view.findViewById(R.id.ib_message_delete);
                ivMessageDelivered = (ImageView) view.findViewById(R.id.iv_message_delivered);
            }
        }

    }


}
