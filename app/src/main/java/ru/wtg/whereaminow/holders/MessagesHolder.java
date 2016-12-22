package ru.wtg.whereaminow.holders;

import android.content.Context;

import java.util.ArrayList;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.UserMessage;

import static ru.wtg.whereaminow.State.CHANGE_NUMBER;
import static ru.wtg.whereaminow.State.TOKEN_CHANGED;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_JOINED;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_PRIVATE;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_USER_DISMISSED;
import static ru.wtg.whereaminow.helpers.UserMessage.TYPE_USER_JOINED;
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

    private ArrayList<UserMessage> messages = new ArrayList<>();

    public MessagesHolder(Context context) {
        this.context = context;
        UserMessage.init(context);
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
                    String text = (String) object;
                    UserMessage m = new UserMessage(context);
                    m.setBody(text);
                    m.setFrom(myUser);
                    m.save(null);
                    messages.add(m);
                    break;
                case PRIVATE_MESSAGE:
                    text = (String) object;
                    m = new UserMessage(context);
                    m.setBody(text);
                    m.setFrom(myUser);
                    m.setTo(State.getInstance().getMe());
                    m.setType(TYPE_PRIVATE);
                    m.save(null);
                    messages.add(m);
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
