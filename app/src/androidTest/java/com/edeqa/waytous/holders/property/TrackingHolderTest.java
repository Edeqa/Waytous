package com.edeqa.waytous.holders.property;

import android.util.Log;

import com.edeqa.waytous.State;
import com.edeqa.waytous.helpers.MyTrackingFB;
import com.edeqa.waytous.helpers.MyTrackingFBTest;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.TrackingCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS;
import static com.edeqa.waytous.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytous.helpers.Events.TRACKING_DISABLED;
import static com.edeqa.waytous.helpers.Events.TRACKING_ERROR;
import static com.edeqa.waytous.helpers.Events.TRACKING_JOIN;
import static com.edeqa.waytous.helpers.Events.TRACKING_NEW;
import static com.edeqa.waytous.helpers.Events.TRACKING_STOP;
import static junit.framework.Assert.assertEquals;

/**
 * Created 9/11/2017.
 */
public class TrackingHolderTest {
    private static final String USERNAME = "User";
    private TrackingHolder holder;
    private MyUser user;

    @Before
    public void setUp() throws Exception {

        user = new MyUser();
        user.setUser(true);
        user.getProperties().setName(USERNAME);

        holder = new TrackingHolder(State.getInstance());

        State.getInstance().getTracking().setTrackingListener(new MyTrackingFBTest().onTrackingListener);

    }

    @Test
    public void getType() throws Exception {
        assertEquals("tracking", holder.getType());
    }

    @Test
    public void dependsOnUser() throws Exception {
        assertEquals(false, holder.dependsOnUser());
    }

    @Test
    public void dependsOnEvent() throws Exception {
        assertEquals(true, holder.dependsOnEvent());
    }

    @Test
    public void perform() throws Exception {
    }

    @Test
    public void isSaveable() throws Exception {
        assertEquals(true, holder.isSaveable());

    }

    @Test
    public void onEvent() throws Exception {
        holder.onEvent(TRACKING_NEW, null);
        holder.onEvent(TRACKING_JOIN, "http://localhost:8080/track/TEST");
        holder.onEvent(TRACKING_STOP, null);
        holder.onEvent(TRACKING_DISABLED, null);
        holder.onEvent(TRACKING_ERROR, "Error");
    }


}