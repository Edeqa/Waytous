package com.edeqa.waytous.helpers;

import android.location.Location;

import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.State;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by eduardm on 008, 9/8/2017.
 */
public class AddressResolverTest {

    private static String USERNAME = "testuser";

    private MyUser user;
    private Location location;
    private AddressResolver resolver;

    @Before
    public void setUp() throws Exception {

        user = new MyUser();
        user.setUser(true);
        user.getProperties().setName(USERNAME);
        location = new Location("fused");
        location.setLongitude(-77.822);
        location.setLatitude(37.75101);
        user.addLocation(location);

        resolver = new AddressResolver(State.getInstance());
        resolver.setUser(user);


    }

    @Test
    public void testResolve() throws Exception {
        final Object syncObject = new Object();

        resolver.setCallback(new Runnable1<String>() {
            @Override
            public void call(String arg) {

                String compare = "Leatherwood Road, Perkinsville, Goochland County, Virginia, 23102, United States of America";

                assertEquals(compare, arg);

                synchronized (syncObject){
                    syncObject.notify();
                }
            }
        });

        resolver.resolve();
        synchronized (syncObject){
            syncObject.wait();
        }

    }

    @Test
    public void resolve1() throws Exception {

    }

}