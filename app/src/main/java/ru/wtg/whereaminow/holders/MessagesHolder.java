package ru.wtg.whereaminow.holders;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.WhereAmINowService;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.UserMessage;

import static android.support.v4.app.NotificationCompat.VISIBILITY_SECRET;
import static ru.wtg.whereaminow.State.ACTIVITY_PAUSE;
import static ru.wtg.whereaminow.State.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.CHANGE_NUMBER;
import static ru.wtg.whereaminow.State.TOKEN_CHANGED;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_JOINED;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_PRIVATE;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_USER_DISMISSED;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_USER_JOINED;
import static ru.wtg.whereaminow.holders.MessagesViewHolder.SHOW_MESSAGES;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_WELCOME_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;

/**
 * Created 11/27/16.
 */
public class MessagesHolder extends AbstractPropertyHolder {

    public static final String TYPE = "messages";

    public static final String NEW_MESSAGE = "new_message";
    public static final String SEND_MESSAGE = "send_message";
    public static final String PRIVATE_MESSAGE = "private_message";
    public static final String USER_MESSAGE = "user_message";
    public static final String WELCOME_MESSAGE = "welcome_message";
    private final Context context;
    private final android.support.v4.app.NotificationCompat.Builder notification;

    private ArrayList<UserMessage> messages = new ArrayList<>();
    private boolean showNotifications = true;
    private Notification result;

    public MessagesHolder(Context context) {
        this.context = context;
        UserMessage.init(context);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        notification = new NotificationCompat.Builder(context)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.drawable.ic_message_black_24dp)
                .setAutoCancel(true)
                .setContentTitle("...")
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(null)
                .setVisibility(VISIBILITY_SECRET)
                .setVibrate(new long[]{0L, 0L});

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
    public Messages create(MyUser myUser) {
        if (myUser == null) return null;
        return new Messages(myUser);
    }


    @Override
    public boolean onEvent(String event, Object object) {
        final MyUser user;
        switch(event){
            case SEND_MESSAGE:
                String text = (String) object;
                UserMessage m = new UserMessage(context);
                m.setBody(text);
                m.setFrom(State.getInstance().getMe());
                m.save(null);
                messages.add(m);
                break;
            case USER_JOINED:
                user = (MyUser) object;
                if(!user.isUser()) return true;
                m = new UserMessage(context);
                m.setBody("User has joined the group.");
                m.setFrom(user);
                m.setType(TYPE_USER_JOINED);
                m.save(null);
                messages.add(m);
                break;
            case USER_DISMISSED:
                user = (MyUser) object;
                if(!user.isUser()) return true;
                m = new UserMessage(context);
                m.setBody("User left the group.");
                m.setFrom(user);
                m.setType(TYPE_USER_DISMISSED);
                m.save(null);
                messages.add(m);
                break;
            case WELCOME_MESSAGE:
                text = (String) object;
                m = new UserMessage(context);
                m.setBody(text);
                m.setFrom(State.getInstance().getUsers().getUsers().get(0));
                m.setType(TYPE_JOINED);
                m.save(null);
                messages.add(m);
                break;
            case TOKEN_CHANGED:
                messages.clear();
//                UserMessage.clear(context);
                break;
            case ACTIVITY_RESUME:
                showNotifications = false;
                break;
            case ACTIVITY_PAUSE:
                showNotifications = true;
                break;
        }
        return true;
    }

    public int getCount() {
        return messages.size();
    }

    public ArrayList<UserMessage> getMessages(){
        return messages;
    }


    public class Messages extends AbstractProperty {

        Messages(MyUser myUser) {
            super(myUser);
        }

        @Override
        public boolean onEvent(String event, Object object) {
            if(!myUser.isUser()) return true;
            switch (event){
                case USER_MESSAGE:
                case PRIVATE_MESSAGE:
                    String text = (String) object;
                    UserMessage m = new UserMessage(context);
                    m.setBody(text);
                    m.setFrom(myUser);

                    if(PRIVATE_MESSAGE.equals(event)) {
                        m.setTo(State.getInstance().getMe());
                        m.setType(TYPE_PRIVATE);
                    }

                    m.save(null);
                    messages.add(m);

                    if(showNotifications) {
                        Intent viewIntent = new Intent(context, MainActivity.class);
                        viewIntent.putExtra("action", "fire");
                        viewIntent.putExtra("fire", SHOW_MESSAGES);

                        Intent replyIntent = new Intent(context, MainActivity.class);
                        replyIntent.putExtra("action", "fire");
                        replyIntent.putExtra("fire", NEW_MESSAGE);
                        replyIntent.putExtra("number", myUser.getProperties().getNumber());

                        PendingIntent pendingViewIntent = PendingIntent.getActivity(context, 1978, viewIntent, 0);
                        PendingIntent pendingReplyIntent = PendingIntent.getActivity(context, 1979, replyIntent, 0);

                        notification.mActions.clear();
                        notification.addAction(R.drawable.ic_message_black_24dp, "View", pendingViewIntent);
                        notification.addAction(R.drawable.ic_reply_black_24dp, "Reply", pendingReplyIntent);

                        notification
                                .setContentTitle(myUser.getProperties().getDisplayName())
                                .setContentText(text)
                                .setWhen(new Date().getTime());

                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        notificationManager.notify(1977, notification.build());
                    }

                    break;
                case SEND_MESSAGE:
                    text = (String) object;
                    m = new UserMessage(context);
                    m.setBody(text);
                    m.setFrom(State.getInstance().getMe());
                    m.setTo(myUser);
                    m.setType(TYPE_PRIVATE);
                    m.save(null);
                    messages.add(m);
                    break;
                case CHANGE_NUMBER:
                    if(State.getInstance().tracking() && myUser.getProperties().getNumber() == 0) {
                        text = State.getInstance().getStringPreference(WELCOME_MESSAGE, "");
                        if(text.length()>0) {
                            State.getInstance().getTracking().sendMessage(RESPONSE_WELCOME_MESSAGE, text);
                        }
                    }
                    break;
            }
            return true;
        }

        @Override
        public boolean dependsOnLocation() {
            return false;
        }
    }
}
