package ru.wtg.whereaminowserver.helpers;

/**
 * Created 10/14/16.
 */
public class Constants {
    public final static int SERVER_BUILD = 9;

// emulator debug local version
/*
    public final static String WSS_SERVER_HOST = "10.0.0.96";
    public final static boolean DEBUGGING = true;
    public final static String HTTP_SERVER_HOST = "10.0.0.96";
*/
// emulator local version
/*
    public final static String WSS_SERVER_HOST = "192.168.56.1";
    public final static boolean DEBUGGING = false;
    public final static String HTTP_SERVER_HOST = "10.0.0.96";
*/

// device internet version
    public final static String WSS_SERVER_HOST = "inchem.kstu.ru";
    public final static boolean DEBUGGING = false;
    public final static String HTTP_SERVER_HOST = "inchem.kstu.ru";

// device debug internet version
 /*   public final static String WSS_SERVER_HOST = "inchem.kstu.ru";
    public final static boolean DEBUGGING = true;
    public final static String HTTP_SERVER_HOST = "inchem.kstu.ru";
*/

    public final static String BROADCAST = "ru.wtg.whereaminow.whereaminowservice";
    public final static String BROADCAST_MESSAGE = "message";

    public final static int REQUEST_PERMISSION_LOCATION = 1;

    public final static int LOCATION_UPDATES_DELAY = 1000;

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
    public static final String RESPONSE_STATUS_STOPPED = "stopped";

    public static final String RESPONSE_MESSAGE = "message";
    public static final String RESPONSE_TOKEN = "token";
    public static final String RESPONSE_CONTROL = "control";
    public static final String RESPONSE_NUMBER = "number";
    public static final String RESPONSE_INITIAL = "initial";
    public static final String RESPONSE_PRIVATE = "to";

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
    public static final String USER_NAME = "name";
    public static final String USER_MESSAGE = "user_message";
    public static final String RESPONSE_WELCOME_MESSAGE = "welcome_message";

    public static final int LIFETIME_INACTIVE_TOKEN = 600;

}
