package com.edeqa.helpers;

import android.location.Location;
import android.util.Log;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.USER_ACCURACY;
import static com.edeqa.waytous.Constants.USER_ALTITUDE;
import static com.edeqa.waytous.Constants.USER_BEARING;
import static com.edeqa.waytous.Constants.USER_LATITUDE;
import static com.edeqa.waytous.Constants.USER_LONGITUDE;
import static com.edeqa.waytous.Constants.USER_PROVIDER;
import static com.edeqa.waytous.Constants.USER_SPEED;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created 9/17/2017.
 */
public class MiscTest {
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
        assertEquals("033bd94b1168d7e4f0d644c3c95e35bf", Misc.getEncryptedHash("TEST"));
    }

    @Test
    public void getEncryptedHash1() throws Exception {
        assertEquals("984816fd329622876e14907634264e6f332e9fb3", Misc.getEncryptedHash("TEST", 1));
        assertEquals("033bd94b1168d7e4f0d644c3c95e35bf", Misc.getEncryptedHash("TEST", 5));
        assertEquals("94ee059335e587e501cc4bf90613e0814f00a7b08bc7c648fd865a2af6a22cc2", Misc.getEncryptedHash("TEST", 256));
        assertEquals("4f37c49c0024445f91977dbc47bd4da9c4de8d173d03379ee19c2bb15435c2c7e624ea42f7cc1689961cb7aca50c7d17", Misc.getEncryptedHash("TEST", 384));
        assertEquals("7bfa95a688924c47c7d22381f20cc926f524beacb13f84e203d4bd8cb6ba2fce81c57a5f059bf3d509926487bde925b3bcee0635e4f7baeba054e5dba696b2bf", Misc.getEncryptedHash("TEST", 512));
    }


    @Test
    public void getUrl() throws Exception {
        String res = Misc.getUrl("https://www.waytous.net/rest/v1/getVersion");
        Log.d("TEST", res);
        JSONObject json = new JSONObject(res);
        assertEquals("success", json.getString("status"));
    }

    @Test
    public void getUrl1() throws Exception {
        String res = Misc.getUrl("https://www.waytous.net/rest/v1/getVersion", "CP-1251");
        Log.d("TEST", res);
        JSONObject json = new JSONObject(res);
        assertEquals("success", json.getString("status"));
    }

    @Test
    public void getUnique() throws Exception {
        Log.d("TEST", Misc.getUnique());
        assertTrue(Misc.getUnique() != null && Misc.getUnique().length() > 0);
    }

}