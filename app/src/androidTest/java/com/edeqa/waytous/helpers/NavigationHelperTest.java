package com.edeqa.waytous.helpers;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.edeqa.eventbus.EventBus;
import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.helpers.interfaces.Runnable2;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created 9/18/2017.
 */
public class NavigationHelperTest {

    private static String USERNAME = "testuser";

    private final Object syncObject = new Object();

//    private MyUser user;
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

        navigationHelper = new NavigationHelper();
        navigationHelper.setAvoidTolls(true);
        navigationHelper.setMode(NavigationHelper.Mode.DRIVING);
        navigationHelper.setStartLocation(startLocation);
        navigationHelper.setEndLocation(destinationLocation);

        navigationHelper.setOnStart(new Runnable() {
            @Override
            public void run() {
                assertEquals(true, navigationHelper.isActive());
            }
        });
        navigationHelper.setOnRequest(new Runnable1<String>() {
            @Override
            public void call(String address) {
                assertEquals("https://maps.googleapis.com/maps/api/directions/json?origin=37.7510,-77.8220&destination=38.9423,-77.3247&mode=driving&alternatives=true&avoid=tolls", address);
            }
        });
        navigationHelper.setOnUpdate(new Runnable2<NavigationHelper.Type, Object>() {
            @Override
            public void call(NavigationHelper.Type type, Object object) {
                switch(type) {
                    case UPDATED:
                        assertEquals(null, object);
                        break;
                    case DISTANCE:
                        assertEquals("130.3 mi", object.toString());
                        break;
                    case DURATION:
                        assertEquals("2h 9m", object.toString());
                        break;
                    case POINTS:
                        assertEquals(275, ((List)object).size());
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
        navigationHelper.setOnStop(new Runnable() {
            @Override
            public void run() {
                assertEquals(false, navigationHelper.isActive());
                synchronized (syncObject){
                    syncObject.notifyAll();
                }
            }
        });

        final Handler handler = new Handler(Looper.getMainLooper());
        EventBus.Runner runner = new EventBus.Runner() {
            @Override
            public void post(Runnable runnable) {
                handler.post(runnable);
            }
        };
        navigationHelper.setRunner(runner);

    }


    @Test
    public void start() throws Exception {
        navigationHelper.start();
        synchronized (syncObject){
            syncObject.wait();
        }

        navigationHelper.updateCurrentLocation(startLocation);
        synchronized (syncObject){
            syncObject.wait();
        }

        navigationHelper.stop();
        synchronized (syncObject){
            syncObject.wait();
        }
        assertEquals(false, navigationHelper.isActive());

        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(navigationHelper.getLastUpdate());
        assertEquals(2017, cal.get(Calendar.YEAR));
    }

    @Test
    public void stop() throws Exception {

    }

    @Test
    public void isAvoidHighways() throws Exception {
        assertEquals(false, navigationHelper.isAvoidHighways());
    }

    @Test
    public void setAvoidHighways() throws Exception {
        navigationHelper.setAvoidHighways(true);
        assertEquals(true, navigationHelper.isAvoidHighways());
    }

    @Test
    public void isAvoidTolls() throws Exception {
        assertEquals(true, navigationHelper.isAvoidTolls());
    }

    @Test
    public void setAvoidTolls() throws Exception {
        navigationHelper.setAvoidTolls(false);
        assertEquals(false, navigationHelper.isAvoidTolls());
    }

    @Test
    public void isAvoidFerries() throws Exception {
        assertEquals(false, navigationHelper.isAvoidFerries());
    }

    @Test
    public void setAvoidFerries() throws Exception {
        navigationHelper.setAvoidFerries(true);
        assertEquals(true, navigationHelper.isAvoidFerries());
    }

    @Test
    public void getMode() throws Exception {
        assertEquals(NavigationHelper.Mode.DRIVING, navigationHelper.getMode());
    }

    @Test
    public void setMode() throws Exception {
        navigationHelper.setMode(NavigationHelper.Mode.WALKING);
        assertEquals(NavigationHelper.Mode.WALKING, navigationHelper.getMode());
    }

    @Test
    public void getOnUpdate() throws Exception {
    }

    @Test
    public void setOnUpdate() throws Exception {
    }

    @Test
    public void getOnStart() throws Exception {
    }

    @Test
    public void setOnStart() throws Exception {
    }

    @Test
    public void isActive() throws Exception {
        assertEquals(false, navigationHelper.isActive());
    }

    @Test
    public void setActive() throws Exception {
        navigationHelper.setActive(true);
        assertEquals(true, navigationHelper.isActive());
        navigationHelper.setActive(false);
        assertEquals(false, navigationHelper.isActive());
    }

    @Test
    public void getStartLocation() throws Exception {
        assertEquals(37.75101, navigationHelper.getStartLocation().getLatitude(), .0001);
    }

    @Test
    public void setStartLocation() throws Exception {
        navigationHelper.setStartLocation(null);
        assertEquals(null, navigationHelper.getStartLocation());
    }

    @Test
    public void getEndLocation() throws Exception {
        assertEquals(38.942308, navigationHelper.getEndLocation().getLatitude(), .0001);
    }

    @Test
    public void setEndLocation() throws Exception {
        navigationHelper.setEndLocation(null);
        assertEquals(null, navigationHelper.getEndLocation());
    }

    @Test
    public void getCurrentLocation() throws Exception {
        assertEquals(null, navigationHelper.getCurrentLocation());
    }

    @Test
    public void setCurrentLocation() throws Exception {
        navigationHelper.setCurrentLocation(null);
        assertEquals(null, navigationHelper.getCurrentLocation());
    }

    @Test
    public void getOnRequest() throws Exception {
    }

    @Test
    public void setOnRequest() throws Exception {
    }

    @Test
    public void getOnStop() throws Exception {
    }

    @Test
    public void setOnStop() throws Exception {
    }

    @Test
    public void getLastUpdate() throws Exception {
        assertEquals(0, navigationHelper.getLastUpdate());
    }

    @Test
    public void getApiKey() throws Exception {
        assertEquals(null, navigationHelper.getApiKey());
    }

    @Test
    public void setApiKey() throws Exception {
        navigationHelper.setApiKey("AAA");
        assertEquals("AAA", navigationHelper.getApiKey());
    }

    @Test
    public void getOnError() throws Exception {
    }

    @Test
    public void setOnError() throws Exception {
    }

    @Test
    public void getOnErrorThrowable() throws Exception {
    }

    @Test
    public void setOnError1() throws Exception {
    }

    @Test
    public void updateCurrentLocation() throws Exception {
    }

}