package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Date;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created 12/9/16.
 */

public class UserMessage extends AbstractSavedItem {

    static final long serialVersionUID =-6395904747332820028L;

    private static final String LAST = "last";
    private static final String MESSAGE = "message";

    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_PRIVATE = 1;
    public static final int TYPE_JOINED = 2;
    public static final int TYPE_USER_JOINED = 3;
    public static final int TYPE_USER_DISMISSED = 4;

    private String from;
    private String to;
    private String body;
    private long timestamp;
    private int type;

    public UserMessage(Context context) {
        super(context, MESSAGE);
        timestamp = new Date().getTime();
        type = TYPE_MESSAGE;
    }

    public static void init(Context context) {
        init(context, UserMessage.class, MESSAGE);
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

    public static int getCount(Context context){
        return getCount(context, MESSAGE);
    }

    public static UserMessage getItemByPosition(Context context, int position) {
        return (UserMessage) getItemByPosition(context, MESSAGE, position);
    }

    public static void clear(Context context){
        clear(context, MESSAGE);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    static public class UserMessagesAdapter extends AbstractSavedItemsAdapter {

        public UserMessagesAdapter(Context context, RecyclerView list) {
            super(context, MESSAGE, list);
        }

        @Override
        public UserMessage.UserMessagesAdapter.UserMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message, parent, false);
//            v.setOnClickListener(onItemClickListener);
            return new UserMessageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            try {
                final UserMessage message = UserMessage.getItemByPosition(context, position);
                if(message == null) return;
                UserMessage.UserMessagesAdapter.UserMessageViewHolder holder = (UserMessage.UserMessagesAdapter.UserMessageViewHolder) viewHolder;
                String title = "";
                boolean privateMessage = false;
                if(message.getFrom() != null && message.getTo() != null) {
                    title = message.getFrom() + " → " + message.getTo();
                } else if(message.getFrom() != null) {
                    title = message.getFrom();
                }

                holder.tvUsername.setText(title);
                holder.tvTimestamp.setText(message.getTimestamp().toString());
                holder.tvMessageBody.setText(message.getBody());

                switch(message.getType()){
                    case TYPE_MESSAGE:
                        holder.ibMessage.setVisibility(View.VISIBLE);
                        holder.ibPrivateMessage.setVisibility(View.INVISIBLE);
                        holder.ibJoined.setVisibility(View.INVISIBLE);
                        holder.ibUserJoined.setVisibility(View.INVISIBLE);
                        holder.ibUserDismissed.setVisibility(View.INVISIBLE);
                        break;
                    case TYPE_PRIVATE:
                        holder.ibMessage.setVisibility(View.INVISIBLE);
                        holder.ibPrivateMessage.setVisibility(View.VISIBLE);
                        holder.ibJoined.setVisibility(View.INVISIBLE);
                        holder.ibUserJoined.setVisibility(View.INVISIBLE);
                        holder.ibUserDismissed.setVisibility(View.INVISIBLE);
                        break;
                    case TYPE_JOINED:
                        holder.ibMessage.setVisibility(View.INVISIBLE);
                        holder.ibPrivateMessage.setVisibility(View.INVISIBLE);
                        holder.ibJoined.setVisibility(View.VISIBLE);
                        holder.ibUserJoined.setVisibility(View.INVISIBLE);
                        holder.ibUserDismissed.setVisibility(View.INVISIBLE);
                        break;
                    case TYPE_USER_JOINED:
                        holder.ibMessage.setVisibility(View.INVISIBLE);
                        holder.ibPrivateMessage.setVisibility(View.INVISIBLE);
                        holder.ibJoined.setVisibility(View.INVISIBLE);
                        holder.ibUserJoined.setVisibility(View.VISIBLE);
                        holder.ibUserDismissed.setVisibility(View.INVISIBLE);
                        break;
                    case TYPE_USER_DISMISSED:
                        holder.ibMessage.setVisibility(View.INVISIBLE);
                        holder.ibPrivateMessage.setVisibility(View.INVISIBLE);
                        holder.ibJoined.setVisibility(View.INVISIBLE);
                        holder.ibUserJoined.setVisibility(View.INVISIBLE);
                        holder.ibUserDismissed.setVisibility(View.VISIBLE);
                        break;
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickListener.call(message);
                    }
                });
                holder.itemView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        onItemTouchListener.call(motionEvent);
                        return false;
                    }
                });

                if(message.getFrom() != null && message.getFrom().equals(State.getInstance().getMe().getProperties().getDisplayName())){
                    holder.itemView.setBackgroundColor(Color.argb(32,0,0,255));
                } else {
                    holder.itemView.setBackgroundColor(android.R.color.background_light);
                }

            }catch(Exception e){e.printStackTrace();}
        }

        class UserMessageViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvTimestamp;
            private final TextView tvMessageBody;
            private final TextView tvUsername;
            private final ImageButton ibMessage;
            private final ImageButton ibUserJoined;
            private final ImageButton ibUserDismissed;
            private final ImageButton ibJoined;
            private final ImageButton ibPrivateMessage;

            private UserMessageViewHolder(View view) {
                super(view);
                tvUsername = (TextView) view.findViewById(R.id.tv_username);
                tvTimestamp = (TextView) view.findViewById(R.id.tvTimestamp);
                tvMessageBody = (TextView) view.findViewById(R.id.tv_message_body);

                ibMessage = (ImageButton) view.findViewById(R.id.ib_message);
                ibPrivateMessage = (ImageButton) view.findViewById(R.id.ib_private);
                ibUserJoined = (ImageButton) view.findViewById(R.id.ib_user_joined);
                ibUserDismissed = (ImageButton) view.findViewById(R.id.ib_user_dismissed);
                ibJoined = (ImageButton) view.findViewById(R.id.ib_joined);
            }
        }

    }

    public String toString() {
        return "{ timestamp: " + new Date(timestamp).toString()
                + ", type: " + type
                + (from != null ? ", from: "+from : "")
                + (to != null ? ", to: "+to : "")
                + (body != null ? ", body: ["+body + "]" : "")
                + " }";
    }

}