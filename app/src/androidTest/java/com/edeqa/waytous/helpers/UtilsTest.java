package com.edeqa.waytous.helpers;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import com.edeqa.helpers.Misc;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.Constants.USER_ACCURACY;
import static com.edeqa.waytous.Constants.USER_ALTITUDE;
import static com.edeqa.waytous.Constants.USER_BEARING;
import static com.edeqa.waytous.Constants.USER_LATITUDE;
import static com.edeqa.waytous.Constants.USER_LONGITUDE;
import static com.edeqa.waytous.Constants.USER_PROVIDER;
import static com.edeqa.waytous.Constants.USER_SPEED;
import static junit.framework.Assert.assertEquals;

/**
 * Created 9/8/2017.
 */
public class UtilsTest {

    private Location location;
    private JSONObject jsonLocation;
    private long timestamp;

    @Before
    public void setUp() throws Exception {

        location = new Location("fused");
        jsonLocation = new JSONObject();
        timestamp = new Date().getTime();
        jsonLocation.put(USER_PROVIDER, "fused");
        jsonLocation.put(USER_LATITUDE, 37.75101);
        jsonLocation.put(USER_LONGITUDE, -77.822);
        jsonLocation.put(USER_ALTITUDE, 0.D);
        jsonLocation.put(USER_ACCURACY, 20.F);
        jsonLocation.put(USER_BEARING, 45.F);
        jsonLocation.put(USER_SPEED, 10.F);
        jsonLocation.put(REQUEST_TIMESTAMP, timestamp);

        location.setLongitude(-77.822);
        location.setLatitude(37.75101);
        location.setAltitude(0.D);
        location.setAccuracy(20.F);
        location.setBearing(45.F);
        location.setSpeed(10.F);
        location.setTime(timestamp);

    }

    @Test
    public void getColorMatrix() throws Exception {
        assertEquals(1.0F, Utils.getColorMatrix(Color.BLACK)[0]);
    }

    @Test
    public void jsonToLocation() throws Exception {
        Location loc = Utils.jsonToLocation(jsonLocation);
        Log.d("TEST", loc.toString());
        assertEquals(location.getLatitude(), loc.getLatitude());
        assertEquals(location.getLongitude(), loc.getLongitude());
        assertEquals(location.getAltitude(), loc.getAltitude());
        assertEquals(location.getAccuracy(), loc.getAccuracy());
        assertEquals(location.getBearing(), loc.getBearing());
        assertEquals(location.getSpeed(), loc.getSpeed());
        assertEquals(location.getTime(), loc.getTime());
        assertEquals(location.getProvider(), loc.getProvider());
    }

    @Test
    public void locationToJson() throws Exception {
        JSONObject json = Utils.locationToJson(location);
        Log.d("TEST", json.toString());
        assertEquals(jsonLocation.getDouble(USER_LATITUDE), json.getDouble(USER_LATITUDE));
        assertEquals(jsonLocation.getDouble(USER_LONGITUDE), json.getDouble(USER_LONGITUDE));
        assertEquals(jsonLocation.getDouble(USER_ALTITUDE), json.getDouble(USER_ALTITUDE));
        assertEquals(jsonLocation.getDouble(USER_ACCURACY), json.getDouble(USER_ACCURACY));
        assertEquals(jsonLocation.getDouble(USER_BEARING), json.getDouble(USER_BEARING));
        assertEquals(jsonLocation.getDouble(USER_SPEED), json.getDouble(USER_SPEED));
        assertEquals(jsonLocation.getDouble(REQUEST_TIMESTAMP), json.getDouble(REQUEST_TIMESTAMP));
        assertEquals(jsonLocation.getString(USER_PROVIDER), json.getString(USER_PROVIDER));

    }

    @Test
    public void normalizeLocation() throws Exception {

        GeoTrackFilter filter = new GeoTrackFilter(1.);
        Log.d("TEST", Utils.normalizeLocation(filter, location).toString());

        Location normalized = Utils.normalizeLocation(filter, location);
        assertEquals(location.getLatitude(), normalized.getLatitude());
        assertEquals(location.getLongitude(), normalized.getLongitude());
        assertEquals(location.getAltitude(), normalized.getAltitude());
        assertEquals(location.getAccuracy(), normalized.getAccuracy());
        assertEquals(location.getProvider(), normalized.getProvider());
        assertEquals(location.getTime(), normalized.getTime());
        assertEquals(location.getSpeed(), normalized.getSpeed());
        assertEquals(location.getBearing(), normalized.getBearing());
    }

    @Test
    public void deserializeFromString() throws Exception {
        String coded = "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwABRgAKbG9hZEZhY3RvcnhwP0AAAHcI\nAAAABAAAAAF0AAFhdAABYng=\n";
        Map deserialized = (Map) Utils.deserializeFromString(coded);
        assertEquals("b", (String) deserialized.get("a"));

    }

    @Test
    public void serializeToString() throws Exception {

        String coded = "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwABRgAKbG9hZEZhY3RvcnhwP0AAAHcI\nAAAABAAAAAF0AAFhdAABYng=\n";
        Map map = new HashMap<>();
        map.put("a","b");

        String serialized = Utils.serializeToString((Serializable) map);
        Log.d("TEST", serialized);
        assertEquals(coded, serialized);
    }

    @Test
    public void findPoint() throws Exception {

        Location location2 = Utils.jsonToLocation(jsonLocation);
        location2.setLatitude(location2.getLatitude() + 1);
        location2.setLongitude(location2.getLongitude() + 1);
        List<LatLng> points = new ArrayList<>();
        points.add(Utils.latLng(location));
        points.add(Utils.latLng(location2));
        LatLng point = Utils.findPoint(points, 0.5);
        Log.d("TEST", point.toString());
        assertEquals(38.25207066798488, point.latitude);
        assertEquals(-77.32544006596333, point.longitude);
    }

    @Test
    public void latLng() throws Exception {

        LatLng latLng = Utils.latLng(location);
        Log.d("TEST", latLng.toString());

        assertEquals(location.getLatitude(), latLng.latitude);
        assertEquals(location.getLongitude(), latLng.longitude);
    }

    @Test
    public void formatLengthToLocale() throws Exception {
        String text = Misc.distanceToString(10);
        assertEquals("33 ft", text);
    }

    @Test
    public void reduce() throws Exception {
        Location location2 = Utils.jsonToLocation(jsonLocation);
        location2.setLatitude(location2.getLatitude() + 1);
        location2.setLongitude(location2.getLongitude() + 1);
        LatLngBounds latLngBounds = new LatLngBounds(Utils.latLng(location), Utils.latLng(location2));

        Log.d("TEST", latLngBounds.toString());
        Log.d("TEST", Utils.reduce(latLngBounds, 1.2).toString());

        LatLngBounds reducedLatLngBounds = Utils.reduce(latLngBounds, 1.2);

        assertEquals(38.8505373278318, reducedLatLngBounds.northeast.latitude);
        assertEquals(-76.72046962493853, reducedLatLngBounds.northeast.longitude);
        assertEquals(37.65054916754206, reducedLatLngBounds.southwest.latitude);
        assertEquals(-77.9205028848438, reducedLatLngBounds.southwest.longitude);

    }


    @Test
    public void getWrappedHttpPort() throws Exception {
        assertEquals(":" + OPTIONS.getHttpPort(), Utils.getWrappedHttpPort());
    }

    @Test
    public void getWrappedHttpsPort() throws Exception {
        assertEquals(":" + OPTIONS.getHttpsPort(), Utils.getWrappedHttpsPort());
    }

    @Test
    public void restrictPrecision() throws Exception {

        double valueShorter = 0.123;
        double valueExact = 0.12345;
        double valueLonger = 0.123456789;

        assertEquals(.123, Utils.restrictPrecision(valueShorter, 5));
        assertEquals(.12345, Utils.restrictPrecision(valueExact, 5));
        assertEquals(.12345, Utils.restrictPrecision(valueLonger, 5));

    }

}