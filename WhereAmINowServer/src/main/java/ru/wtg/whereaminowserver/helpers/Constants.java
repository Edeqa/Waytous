package ru.wtg.whereaminowserver.helpers;

/**
 * Created by tujger on 10/14/16.
 */
public class Constants {
    public final static int SERVER_BUILD = 3;

// device debug local version
/*
    public final static String WSS_SERVER_URL = "wss://10.0.0.96:8081";
    public final static boolean DEBUGGING = true;
    public final static String HTTP_SERVER_URL = "https://10.0.0.96:8080";
*/

// emulator local version
    public final static String WSS_SERVER_URL = "wss://192.168.56.1:8081";
    public final static boolean DEBUGGING = false;
    public final static String HTTP_SERVER_URL = "https://10.0.0.96:8080";

// emulator internet version
/*
    public final static String WSS_SERVER_URL = "wss://inchem.kstu.ru:8081";
    public final static boolean DEBUGGING = false;
    public final static String HTTP_SERVER_URL = "https://inchem.kstu.ru:8080";
*/

// device debug internet version
/*
    public final static String WSS_SERVER_URL = "wss://inchem.kstu.ru:8081";
    public final static boolean DEBUGGING = true;
    public final static String HTTP_SERVER_URL = "https://inchem.kstu.ru:8080";
*/

    public final static String BROADCAST = "ru.wtg.whereaminow.whereaminowservice";
    public final static String BROADCAST_MESSAGE = "message";

    public final static int REQUEST_PERMISSION_LOCATION = 1;
    public final static int REQUEST_PERMISSION_LOCATION_ONRESUME = 2;

    public final static int LOCATION_UPDATES_DELAY = 1000;

    public final static int CAMERA_ORIENTATION_NORTH = 0;
    public final static int CAMERA_ORIENTATION_DIRECTION = 1;
    public final static int CAMERA_ORIENTATION_PERSPECTIVE = 2;
    public final static int CAMERA_ORIENTATION_STAY = 3;
    public final static int CAMERA_ORIENTATION_USER = 4;
    public static final float CAMERA_DEFAULT_ZOOM = 15.f;
    public static final float CAMERA_DEFAULT_TILT = 0.f;
    public static final float CAMERA_DEFAULT_BEARING = 0.f;
    public final static int CAMERA_ORIENTATION_LAST = 2;

    public static final int TRACKING_DISABLED = 0;
    public static final int TRACKING_CONNECTING = 1;
    public static final int TRACKING_ACTIVE = 2;
    public static final int TRACKING_GPS_REJECTED = 3;

// client constants
    public static final String REQUEST = "client";
    public static final String REQUEST_UPDATE = "update";
    public static final String REQUEST_JOIN_TOKEN = "join";
    public static final String REQUEST_NEW_TOKEN = "create";
    public static final String REQUEST_CHECK_USER = "check";
    public static final String REQUEST_TOKEN = "token";
    public static final String REQUEST_HASH = "hash";
    public static final String REQUEST_DEVICE_ID = "device_id";
    public static final String REQUEST_MODEL = "model";
    public static final String REQUEST_MANUFACTURER = "manufacturer";
    public static final String REQUEST_OS = "os";

// server constants
    public static final String RESPONSE_STATUS = "server";
    public static final String RESPONSE_STATUS_CONNECTED = "connected";
    public static final String RESPONSE_STATUS_ACCEPTED = "accepted";
    public static final String RESPONSE_STATUS_UPDATED = "updated";
    public static final String RESPONSE_STATUS_CHECK = "check";
    public static final String RESPONSE_STATUS_DISCONNECTED = "disconnected";
    public static final String RESPONSE_STATUS_ERROR = "error";

    public static final String RESPONSE_MESSAGE = "message";
    public static final String RESPONSE_TOKEN = "token";
    public static final String RESPONSE_CONTROL = "control";
    public static final String RESPONSE_NUMBER = "number";
    public static final String RESPONSE_INITIAL = "initial";

    public static final String USER_DISMISSED = "dismissed";
    public static final String USER_JOINED = "joined";
    public static final String USER_LATITUDE = "lat";
    public static final String USER_LONGITUDE = "lng";
    public static final String USER_ALTITUDE = "alt";
    public static final String USER_ACCURACY = "acc";
    public static final String USER_BEARING = "brn";
    public static final String USER_TIMESTAMP = "time";
    public static final String USER_PROVIDER = "provider";
    public static final String USER_TILT = "tilt";
    public static final String USER_SPEED = "spd";
    public static final String USER_NUMBER = "number";
    public static final String USER_COLOR = "color";

    public static final int LIFETIME_INACTIVE_TOKEN = 60;

}
