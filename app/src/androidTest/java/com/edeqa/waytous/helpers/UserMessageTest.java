package com.edeqa.waytous.helpers;

import android.database.Cursor;

import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.State;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created 9/21/2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserMessageTest {


    private UserMessage userMessage;
    private UserMessage userMessageRegular;
    private UserMessage userMessagePrivate;
    private Object sync;
    private MyUser userFrom;
    private MyUser userTo1;
    private MyUser userTo2;
    private MyUser user;

    @Before
    public void setUp() throws Exception {
        System.out.println("=== INITIALIZING");

        userFrom = new MyUser();
        userFrom.setUser(true);
        userFrom.getProperties().setName("user-from");

        userTo1 = new MyUser();
        userTo1.setUser(true);
        userTo1.getProperties().setName("user-to-1");

        userTo2 = new MyUser();
        userTo2.setUser(true);
        userTo2.getProperties().setName("user-to-2");

        user = new MyUser();
        user.setUser(true);
        user.getProperties().setName("user");


            UserMessage.init(State.getInstance());

            sync = new Object();

            userMessageRegular = new UserMessage(State.getInstance());

            userMessageRegular.setType(UserMessage.TYPE_MESSAGE);
            userMessageRegular.setBody("body-regular");
            userMessageRegular.setKey("key-regular");
            userMessageRegular.setDelivery("delivery-regular");
//            userMessageRegular.setNumber(10000);
            userMessageRegular.setFrom(userFrom);
            userMessageRegular.setTo(userTo1);
            userMessageRegular.save(new Runnable1() {
                @Override
                public void call(Object arg) {
                    synchronized (sync) {
                        sync.notify();
                    }
                }
            });
//            synchronized (sync) {
//                sync.wait();
//            }
        userMessagePrivate = new UserMessage(State.getInstance());

        userMessagePrivate.setType(UserMessage.TYPE_PRIVATE);
        userMessagePrivate.setBody("body-private");
        userMessagePrivate.setKey("key-private");
        userMessagePrivate.setDelivery("delivery-private");
//        userMessagePrivate.setNumber(20000);
        userMessagePrivate.setFrom(userFrom);
        userMessagePrivate.setTo(userTo2);
        userMessagePrivate.save(new Runnable1() {
            @Override
            public void call(Object arg) {
                synchronized (sync) {
                    sync.notify();
                }
            }
        });
//        synchronized (sync) {
//            sync.wait();
//        }
        userMessage = UserMessage.getItemByPosition(0);

        System.out.println(">>> BEGIN TEST");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("<<< END TEST");

        System.out.println("=== FINALIZING");
    }

    @Test
    public void init() throws Exception {
        // TODO
    }

    @Test
    public void getDb() throws Exception {
        assertEquals("DBHelper", UserMessage.getDb().getClass().getSimpleName());
    }

    @Test
    public void getCount() throws Exception {
        assertTrue(UserMessage.getCount() > 0);
    }

    @Test
    public void clear() throws Exception {
        // TODO
        UserMessage.clear();
        assertEquals(0, UserMessage.getCount());
    }

    @Test
    public void getItemByPosition() throws Exception {
        assertEquals("body-private", UserMessage.getItemByPosition(1).getBody());
    }

    @Test
    public void getItemByNumber() throws Exception {

        assertEquals("body-private", UserMessage.getItemByNumber(UserMessage.getItemByFieldValue("key", "key-private").getNumber()).getBody());
    }

    @Test
    public void getItemByCursor() throws Exception {
        Cursor cursor = UserMessage.getDb().getAll();
        cursor.moveToFirst();
        assertEquals("body-regular", UserMessage.getItemByCursor(cursor).getBody());
    }

    @Test
    public void getItemByFieldValue() throws Exception {
        assertEquals("body-regular", UserMessage.getItemByFieldValue("key", "key-regular").getBody());
    }

    @Test
    public void getBody() throws Exception {
        assertEquals("body-regular", userMessage.getBody());
    }

    @Test
    public void setBody() throws Exception {
        userMessage.setBody("body-updated");
        assertEquals("body-updated", userMessage.getBody());
    }

    @Test
    public void getTimestamp() throws Exception {
        assertTrue(userMessage.getTimestamp().getTime() > 1506026425036L);
    }

    @Test
    public void getFrom() throws Exception {
        assertEquals("user-from", userMessage.getFrom());
    }

    @Test
    public void setFrom() throws Exception {
        userMessage.setFrom(user);
        assertEquals("user", userMessage.getFrom());
    }

    @Test
    public void getTo() throws Exception {
        assertEquals("user-to-1", userMessage.getTo());
    }

    @Test
    public void setTo() throws Exception {
        userMessage.setTo(user);
        assertEquals("user", userMessage.getTo());
    }

    @Test
    public void getType() throws Exception {
        assertEquals(0, userMessage.getType());
    }

    @Test
    public void setType() throws Exception {
        userMessage.setType(UserMessage.TYPE_JOINED);
        assertEquals(2, userMessage.getType());
    }

    @Test
    public void getDelivery() throws Exception {
        assertEquals("delivery-regular", userMessage.getDelivery());
    }

    @Test
    public void setDelivery() throws Exception {
        userMessage.setDelivery("delivered");
        assertEquals("delivered", userMessage.getDelivery());
    }

    @Test
    public void getKey() throws Exception {
        assertEquals("key-regular", userMessage.getKey());
    }

    @Test
    public void setKey() throws Exception {
        userMessage.setKey("key-updated");
        assertEquals("key-updated", userMessage.getKey());
    }



}