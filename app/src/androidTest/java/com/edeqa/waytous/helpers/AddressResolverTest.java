package com.edeqa.waytous.helpers;

import android.location.Location;

import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.State;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created 9/8/2017.
 */
public class AddressResolverTest {

    private static String USERNAME = "testuser";

    private MyUser user;
    private MyUser user2;
    private Location location;
    private Location location2;
    private AddressResolver resolver;
    final Object syncObject = new Object();

    String address1 = "Leatherwood Road, Perkinsville, Goochland County, Virginia, 23102, United States of America";
    String address2 = "Reston Family Dental Center, 1801, Robert Fulton Drive, Reston, Fairfax County, Virginia, 20191, United States of America";


    @Before
    public void setUp() throws Exception {

        user = new MyUser();
        user.setUser(true);
        user.getProperties().setName(USERNAME);
        location = new Location("fused");
        location.setLongitude(-77.822);
        location.setLatitude(37.75101);
        user.addLocation(location);

        user2 = new MyUser();
        user2.setUser(true);
        user2.getProperties().setName(USERNAME + 2);
        location2 = new Location("fused");
        location2.setLongitude(-77.324682);
        location2.setLatitude(38.942308);
        user2.addLocation(location2);

        resolver = new AddressResolver(State.getInstance());
        resolver.setUser(user);
        resolver.setCallback(new Consumer<String>() {
            @Override
            public void accept(String arg) {
                assertEquals("Leatherwood Road, Perkinsville, Goochland County, Virginia, 23102, United States of America", arg);
                synchronized (syncObject){
                    syncObject.notify();
                }
            }
        });

    }

    @Test
    public void resolve() throws Exception {

        resolver.setCallback(new Consumer<String>() {
            @Override
            public void accept(String arg) {

                String compare = address1;

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

    @Test
    public void setUser() throws Exception {
        resolver.setUser(user2);
        resolver.setCallback(new Consumer<String>() {
            @Override
            public void accept(String arg) {
                assertEquals(address2, arg);
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
    public void setLatLng() throws Exception {
        resolver.setLatLng(Utils.latLng(location2));
        resolver.setCallback(new Consumer<String>() {
            @Override
            public void accept(String arg) {
                assertEquals(address2, arg);
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
    public void setLocation() throws Exception {
        resolver.setLocation(location2);
        resolver.setCallback(new Consumer<String>() {
            @Override
            public void accept(String arg) {
                assertEquals(address2, arg);
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
    public void setCallback() throws Exception {
        resolver.setLatLng(Utils.latLng(location2));
        resolver.setCallback(new Consumer<String>() {
            @Override
            public void accept(String arg) {
                assertEquals("Reston Family Dental Center, 1801, Robert Fulton Drive, Reston, Fairfax County, Virginia, 20191, United States of America", arg);
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

}