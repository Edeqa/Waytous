package com.edeqa.waytous.helpers;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;

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

import static com.edeqa.waytousserver.helpers.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytousserver.helpers.Constants.USER_ACCURACY;
import static com.edeqa.waytousserver.helpers.Constants.USER_ALTITUDE;
import static com.edeqa.waytousserver.helpers.Constants.USER_BEARING;
import static com.edeqa.waytousserver.helpers.Constants.USER_LATITUDE;
import static com.edeqa.waytousserver.helpers.Constants.USER_LONGITUDE;
import static com.edeqa.waytousserver.helpers.Constants.USER_PROVIDER;
import static com.edeqa.waytousserver.helpers.Constants.USER_SPEED;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

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
    public void getEncryptedHash() throws Exception {
        assertEquals("033bd94b1168d7e4f0d644c3c95e35bf", Utils.getEncryptedHash("TEST"));
    }

    @Test
    public void getEncryptedHash1() throws Exception {
        assertEquals("984816fd329622876e14907634264e6f332e9fb3", Utils.getEncryptedHash("TEST", 1));
        assertEquals("033bd94b1168d7e4f0d644c3c95e35bf", Utils.getEncryptedHash("TEST", 5));
        assertEquals("917ecca24f3e6ceaf52375d8083381f1f80a21e6e49fbadc40afeb8e", Utils.getEncryptedHash("TEST", 224));
        assertEquals("94ee059335e587e501cc4bf90613e0814f00a7b08bc7c648fd865a2af6a22cc2", Utils.getEncryptedHash("TEST", 256));
        assertEquals("4f37c49c0024445f91977dbc47bd4da9c4de8d173d03379ee19c2bb15435c2c7e624ea42f7cc1689961cb7aca50c7d17", Utils.getEncryptedHash("TEST", 384));
        assertEquals("7bfa95a688924c47c7d22381f20cc926f524beacb13f84e203d4bd8cb6ba2fce81c57a5f059bf3d509926487bde925b3bcee0635e4f7baeba054e5dba696b2bf", Utils.getEncryptedHash("TEST", 512));
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
    public void getUrl() throws Exception {
        String res = Utils.getUrl("https://www.waytous.net/rest/v1/getVersion");
        Log.d("TEST", res);
        JSONObject json = new JSONObject(res);
        assertEquals("success", json.getString("status"));
    }

    @Test
    public void getUrl1() throws Exception {
        String res = Utils.getUrl("https://www.waytous.net/rest/v1/getVersion", "CP-1251");
        Log.d("TEST", res);
        JSONObject json = new JSONObject(res);
        assertEquals("success", json.getString("status"));
    }

    @Test
    public void getUnique() throws Exception {
        Log.d("TEST", Utils.getUnique());
        assertTrue(Utils.getUnique() != null && Utils.getUnique().length() > 0);
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
        String coded = "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVz\naG9sZHhwP0AAAAAAAAN3CAAAAAQAAAABdAABYXQAAWJ4\n";
        Map deserialized = (Map) Utils.deserializeFromString(coded);
        assertEquals("b", (String) deserialized.get("a"));

    }

    @Test
    public void serializeToString() throws Exception {

        String coded = "rO0ABXNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVz\naG9sZHhwP0AAAAAAAAN3CAAAAAQAAAABdAABYXQAAWJ4\n";
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
        String text = Utils.formatLengthToLocale(10);
        Log.d("TEST", text);
        assertEquals("  33 ft", text);
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

}