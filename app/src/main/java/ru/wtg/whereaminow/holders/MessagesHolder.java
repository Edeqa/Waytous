package ru.wtg.whereaminow.holders;

import java.util.ArrayList;
import java.util.Date;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;

import static ru.wtg.whereaminow.State.PRIVATE_MESSAGE;
import static ru.wtg.whereaminow.State.SEND_MESSAGE;
import static ru.wtg.whereaminow.State.USER_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;

/**
 * Created 11/27/16.
 */
public class MessagesHolder extends AbstractPropertyHolder<MessagesHolder.Messages> {

    public static final String TYPE = "messages";

    private ArrayList<UserMessage> messages = new ArrayList<>();

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String[] getOwnEvents() {
        return new String[0];
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
                UserMessage m = new UserMessage();
                m.setBody(text);
                m.setFrom(State.getInstance().getMe());
                messages.add(m);
                break;
            case USER_JOINED:
                user = (MyUser) object;
                m = new UserMessage();
                m.setBody("User has joined the group.");
                m.setFrom(user);
                m.setSystemMessage(true);
                messages.add(m);
                break;
            case USER_DISMISSED:
                user = (MyUser) object;
                m = new UserMessage();
                m.setBody("User left the group.");
                m.setFrom(user);
                m.setSystemMessage(true);
                messages.add(m);
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

    public class UserMessage {
        private MyUser from;
        private MyUser to;
        private String body;
        private long timestamp;
        private boolean privateMessage;
        private boolean systemMessage;

        UserMessage(){
            timestamp = new Date().getTime();
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

        public MyUser getFrom() {
            return from;
        }

        public void setFrom(MyUser from) {
            this.from = from;
        }

        public MyUser getTo() {
            return to;
        }

        public void setTo(MyUser to) {
            this.to = to;
        }

        public boolean isPrivateMessage() {
            return privateMessage;
        }

        public void setPrivateMessage(boolean privateMessage) {
            this.privateMessage = privateMessage;
        }

        public boolean isSystemMessage() {
            return systemMessage;
        }

        public void setSystemMessage(boolean systemMessage) {
            this.systemMessage = systemMessage;
        }
    }


    public class Messages extends AbstractProperty {

        Messages(MyUser myUser) {
            super(myUser);
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch (event){
                case USER_MESSAGE:
                    String text = (String) object;
                    UserMessage m = new UserMessage();
                    m.setBody(text);
                    m.setFrom(myUser);
                    messages.add(m);
                    break;
                case PRIVATE_MESSAGE:
                    text = (String) object;
                    m = new UserMessage();
                    m.setBody(text);
                    m.setFrom(myUser);
                    m.setTo(State.getInstance().getMe());
                    m.setPrivateMessage(true);
                    messages.add(m);
                    break;
                case SEND_MESSAGE:
                    text = (String) object;
                    m = new UserMessage();
                    m.setBody(text);
                    m.setFrom(State.getInstance().getMe());
                    m.setTo(myUser);
                    m.setPrivateMessage(true);
                    messages.add(m);
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
