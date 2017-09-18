package com.edeqa.waytous.helpers;

import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.State;

import org.junit.Before;
import org.junit.Test;

import static com.edeqa.waytous.helpers.UserMessage.TYPE_PRIVATE;
import static junit.framework.Assert.assertEquals;

/**
 * Created 9/11/2017.
 */
public class SystemMessageTest {

    private static String USERNAME_FROM = "TestuserFrom";
    private static String USERNAME_TO = "TestuserTo";
    private static String TEXT = "test text";

    private SystemMessage systemMessage;
    private MyUser userFrom;
    private MyUser userTo;

    @Before
    public void setUp() throws Exception {

        userFrom = new MyUser();
        userFrom.setUser(true);
        userFrom.getProperties().setName(USERNAME_FROM);

        userTo = new MyUser();
        userTo.setUser(true);
        userTo.getProperties().setName(USERNAME_TO);

        systemMessage = new SystemMessage(State.getInstance())
                .setFromUser(userFrom)
                .setText(TEXT)
                .setDelivery(Utils.getUnique())
                .setToUser(userTo)
                .setOnClickListener(new Runnable1<String>() {
                    @Override
                    public void call(String arg) {
                        assertEquals("setOnClickListener:" + TEXT, arg);
                    }
                })
                .setType(TYPE_PRIVATE).setCallback(new Runnable1<String>() {
                    @Override
                    public void call(String arg) {
                        assertEquals(TEXT, arg);
                    }
                });
    }

    @Test
    public void getText() throws Exception {
        assertEquals(TEXT, systemMessage.getText());
    }

    @Test
    public void setText() throws Exception {
        systemMessage.setText(TEXT + TEXT);
        assertEquals(TEXT + TEXT, systemMessage.getText());
    }

    @Test
    public void getTitle() throws Exception {
        assertEquals(null, systemMessage.getTitle());
    }

    @Test
    public void setAction() throws Exception {
        systemMessage.setAction("TITLE", new Runnable1<String>() {
            @Override
            public void call(String arg) {
                assertEquals("TITLE", systemMessage.getTitle());
                assertEquals(TEXT, arg);
            }
        });
        systemMessage.getAction().call(TEXT);
    }

    @Test
    public void getDuration() throws Exception {
        assertEquals(0, systemMessage.getDuration());
    }

    @Test
    public void setDuration() throws Exception {
        systemMessage.setDuration(100);
        assertEquals(100, systemMessage.getDuration());
    }

    @Test
    public void getOnClickListener() throws Exception {
        systemMessage.getOnClickListener().call("setOnClickListener:" + TEXT);

    }

    @Test
    public void setOnClickListener() throws Exception {
        systemMessage.setOnClickListener(new Runnable1<String>() {
            @Override
            public void call(String arg) {
                assertEquals("setOnClickListener", arg);
            }
        });
        systemMessage.getOnClickListener().call("setOnClickListener");

    }

    @Test
    public void showSnack() throws Exception {
        systemMessage.showSnack();

    }

    @Test
    public void getAction() throws Exception {
        systemMessage.getCallback().call(TEXT);
    }

    @Test
    public void getFromUser() throws Exception {
        assertEquals(userFrom.getProperties().getDisplayName(), systemMessage.getFromUser().getProperties().getDisplayName());
    }

    @Test
    public void setFromUser() throws Exception {
        systemMessage.setFromUser(userTo);
        assertEquals(userTo.getProperties().getDisplayName(), systemMessage.getFromUser().getProperties().getDisplayName());
    }

    @Test
    public void getToUser() throws Exception {
        assertEquals(userTo.getProperties().getDisplayName(), systemMessage.getToUser().getProperties().getDisplayName());
    }

    @Test
    public void setToUser() throws Exception {
        systemMessage.setToUser(userFrom);
        assertEquals(userFrom.getProperties().getDisplayName(), systemMessage.getToUser().getProperties().getDisplayName());
    }

    @Test
    public void getDelivery() throws Exception {
        assertEquals(true, systemMessage.getDelivery() != null && systemMessage.getDelivery().length() > 0);
    }

}