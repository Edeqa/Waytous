package com.edeqa.waytous.helpers;

import android.location.Location;

import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.helpers.interfaces.Runnable2;

import org.junit.Before;
import org.junit.Test;

import static com.edeqa.waytous.helpers.NavigationHelper.TYPE_DISTANCE;
import static com.edeqa.waytous.helpers.NavigationHelper.TYPE_DURATION;
import static com.edeqa.waytous.helpers.NavigationHelper.TYPE_STARTED;
import static org.junit.Assert.assertEquals;

/**
 * Created 9/18/2017.
 */
public class NavigationHelperTest {

    private static String USERNAME = "testuser";

    final Object syncObject = new Object();

    private MyUser user;
    private Location startLocation;
    private Location destinationLocation;
    private NavigationHelper navigationHelper;


    @Before
    public void setUp() throws Exception {

        startLocation = new Location("fused");
        startLocation.setLongitude(-77.822);
        startLocation.setLatitude(37.75101);

        destinationLocation = new Location("fused");
        destinationLocation.setLongitude(-77.324682);
        destinationLocation.setLatitude(38.942308);

        user = new MyUser();
        user.setUser(true);
        user.getProperties().setName(USERNAME);
        user.addLocation(startLocation);

        navigationHelper = new NavigationHelper();
        navigationHelper.setAvoidTolls(true);
        navigationHelper.setMode(NavigationHelper.Mode.DRIVING);
        navigationHelper.setStartLocation(startLocation);
        navigationHelper.setEndLocation(destinationLocation);

        navigationHelper.setOnStart(new Runnable() {
            @Override
            public void run() {
                assertEquals(true, navigationHelper.isActive());
//                synchronized (syncObject){
//                    syncObject.notify();
//                }
            }
        });
        navigationHelper.setOnRequest(new Runnable1<String>() {
            @Override
            public void call(String address) {
                assertEquals("https://maps.googleapis.com/maps/api/directions/json?origin=37.7510,-77.8220&destination=38.9423,-77.3247&mode=driving&alternatives=true&avoid=tolls", address);
            }
        });
        navigationHelper.setOnUpdate(new Runnable2<Integer, String>() {
            @Override
            public void call(Integer type, String text) {

                switch(type) {
                    case TYPE_STARTED:
                        assertEquals(null, text);
                        break;
                    case TYPE_DISTANCE:
                        assertEquals("130.3 mi", text);
                        break;
                    case TYPE_DURATION:
                        assertEquals("2h 9m", text);
                        synchronized (syncObject) {
                            syncObject.notify();
                        }
                        break;
                    default:
                        assertEquals(true, false);
                }
            }
        });
        navigationHelper.setOnError(new Runnable1<Throwable>() {
            @Override
            public void call(Throwable error) {
                assertEquals("", error.getMessage());
                synchronized (syncObject){
                    syncObject.notify();
                }
            }
        });

    }

    @Test
    public void update() throws Exception {
        // TODO

    }

    @Test
    public void start() throws Exception {
        navigationHelper.start();
        synchronized (syncObject){
            syncObject.wait();
        }

    }

    @Test
    public void stop() throws Exception {
        // TODO

    }

    @Test
    public void isAvoidHighways() throws Exception {
        // TODO

    }

    @Test
    public void setAvoidHighways() throws Exception {
        // TODO

    }

    @Test
    public void isAvoidTolls() throws Exception {
        // TODO

    }

    @Test
    public void setAvoidTolls() throws Exception {
        // TODO

    }

    @Test
    public void isAvoidFerries() throws Exception {
        // TODO

    }

    @Test
    public void setAvoidFerries() throws Exception {
        // TODO

    }

    @Test
    public void getMode() throws Exception {
        // TODO

    }

    @Test
    public void setMode() throws Exception {
        // TODO

    }

    @Test
    public void getOnUpdate() throws Exception {
        // TODO

    }

    @Test
    public void setOnUpdate() throws Exception {
        // TODO

    }

    @Test
    public void getOnStart() throws Exception {
        // TODO

    }

    @Test
    public void setOnStart() throws Exception {
        // TODO

    }

    @Test
    public void isActive() throws Exception {
        // TODO

    }

    @Test
    public void setActive() throws Exception {
        // TODO

    }

    @Test
    public void getStartLocation() throws Exception {
        // TODO

    }

    @Test
    public void setStartLocation() throws Exception {
        // TODO

    }

    @Test
    public void getEndLocation() throws Exception {
        // TODO

    }

    @Test
    public void setEndLocation() throws Exception {
        // TODO

    }

    @Test
    public void getCurrentLocation() throws Exception {
        // TODO

    }

    @Test
    public void setCurrentLocation() throws Exception {
        // TODO

    }

    @Test
    public void getOnRequest() throws Exception {
        // TODO

    }

    @Test
    public void setOnRequest() throws Exception {
        // TODO

    }

    @Test
    public void getOnStop() throws Exception {
        // TODO

    }

    @Test
    public void setOnStop() throws Exception {
        // TODO

    }

    @Test
    public void getLastUpdate() throws Exception {
        // TODO

    }

    @Test
    public void setLastUpdate() throws Exception {
        // TODO

    }

    @Test
    public void getApiKey() throws Exception {
        // TODO

    }

    @Test
    public void setApiKey() throws Exception {
        // TODO

    }

    @Test
    public void getOnError() throws Exception {
        // TODO

    }

    @Test
    public void setOnError() throws Exception {
        // TODO

    }

    @Test
    public void getOnErrorThrowable() throws Exception {
        // TODO

    }

    @Test
    public void setOnError1() throws Exception {
        // TODO

    }

}