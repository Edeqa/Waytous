package com.edeqa.waytous.helpers;

import android.location.Location;

import com.edeqa.waytous.holders.property.PropertiesHolder;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created 9/8/2017.
 */
public class MyUserTest {

    private static String USERNAME = "testuser";

    private MyUser user;
    private Location location;

    @Before
    public void setUp() throws Exception {
        user = new MyUser();
        user.setUser(true);
        user.getProperties().setName(USERNAME);
        location = new Location("fused");
        location.setLongitude(-77.822);
        location.setLatitude(37.75101);
        user.addLocation(location);
    }

    @Test
    public void isUser() throws Exception {
        assertEquals(true, user.isUser());

    }

    @Test
    public void isShown() throws Exception {
        assertEquals(false, user.isShown());

    }

    @Test
    public void addLocation() throws Exception {
        assertEquals(user, user.addLocation(new Location("fused")));
    }

    @Test
    public void getLocation() throws Exception {
        assertEquals(location, user.getLocation());
    }

    @Test
    public void getProperty_Name() throws Exception {
        assertEquals(USERNAME, user.getProperties().getName());
    }

    @Test
    public void getProperty_DisplayName() throws Exception {
        assertEquals(USERNAME, user.getProperties().getDisplayName());
    }

    @Test
    public void getLocations() throws Exception {
        // TODO
    }

    @Test
    public void createViews() throws Exception {
    }

    @Test
    public void fire() throws Exception {
        // TODO

    }

    @Test
    public void fire1() throws Exception {
        // TODO
    }

    @Test
    public void onChangeLocation() throws Exception {
        // TODO
    }

    @Test
    public void removeViews() throws Exception {
    }

    @Test
    public void getProperties() throws Exception {
        assertEquals("testuser", user.getProperties().getDisplayName());
    }

    @Test
    public void getProperty() throws Exception {
        assertEquals("testuser", ((PropertiesHolder.Properties)user.getProperty(PropertiesHolder.TYPE)).getDisplayName());
    }

    @Test
    public void getView() throws Exception {
//        assertEquals("A", user.getView(NavigationViewHolder.TYPE));
    }

    @Test
    public void setUser() throws Exception {
        assertEquals(true, user.isUser());
        user.setUser(false);
        assertEquals(false, user.isUser());
    }

}

