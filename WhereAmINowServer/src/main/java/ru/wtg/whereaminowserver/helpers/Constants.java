package ru.wtg.whereaminowserver.helpers;

import ru.wtg.whereaminowserver.interfaces.SensitiveDataInterface;

/**
 * Created 10/14/16.
 */
public class Constants {
    public final static int SERVER_BUILD = 29;

    //    public final static SensitiveDataInterface SENSITIVE = new SensitiveDataInternetDebug();
//    public final static SensitiveDataInterface SENSITIVE = new SensitiveDataInternetRelease();
    public final static SensitiveDataInterface SENSITIVE = new SensitiveDataDeveloper();

// debug local version
//    public final static String WSS_SERVER_HOST = "https://10.0.0.96";
//    public final static boolean DEBUGGING = true;
//    public final static String HTTP_SERVER_HOST = "10.0.0.96";
//    public final static String WEB_ROOT_DIRECTORY = "WhereAmINowServer/src/main/webapp";
//    public final static String KEYSTORE = "debug.jks";
// public internet version
/*
    public final static String WSS_SERVER_HOST = "https://inchem.kstu.ru";
    public final static boolean DEBUGGING = false;
    public final static String HTTP_SERVER_HOST = "inchem.kstu.ru";
//    public final static String WEB_ROOT_DIRECTORY = "WhereAmINowServer/build/exploded-app";
    public final static String WEB_ROOT_DIRECTORY = "../..";
    public final static String KEYSTORE = "../../inchem.jks";
*/
// debug internet version
/*
    public final static String WSS_SERVER_HOST = "https://inchem.kstu.ru";
    public final static boolean DEBUGGING = true;
    public final static String HTTP_SERVER_HOST = "inchem.kstu.ru";
//    public final static String WEB_ROOT_DIRECTORY = "WhereAmINowServer/build/exploded-app";
    public final static String WEB_ROOT_DIRECTORY = "../..";
    public final static String KEYSTORE = "../../inchem.jks";
*/

    public final static String BROADCAST = "ru.wtg.whereaminow.whereaminowservice";
    public final static String BROADCAST_MESSAGE = "message";

    public final static int LOCATION_UPDATES_DELAY = 1000;


//    public final static int HTTP_PORT = 8080;
//    public final static int HTTPS_PORT = 8100;
//    public final static int WS_FB_PORT = 8081;
//    public final static int WSS_FB_PORT = 8001;
//    public final static int WS_PORT = 8082;
//    public final static int WSS_PORT = 8002;
//    public final static int WSS_PORT = 443;

    // client constants
    public static final String REQUEST = "client";
    public static final String REQUEST_TIMESTAMP = "timestamp";
    public static final String REQUEST_UPDATE = "update";
    public static final String REQUEST_JOIN_TOKEN = "join";
    public static final String REQUEST_NEW_TOKEN = "create";
    public static final String REQUEST_CHECK_USER = "check";
    public static final String REQUEST_TOKEN = "token";
    public static final String REQUEST_HASH = "hash";
    public static final String REQUEST_PUSH = "push";
    public static final String REQUEST_ADMIN = "admin";

    public static final String REQUEST_DEVICE_ID = "device_id";
    public static final String REQUEST_MODEL = "model";
    public static final String REQUEST_MANUFACTURER = "manufacturer";
    public static final String REQUEST_OS = "os";

// instances to/from server
    public static final String REQUEST_TRACKING = "tracking";
    public static final String REQUEST_MESSAGE = "message";
    public static final String REQUEST_CHANGE_NAME = "change_name";
    public static final String REQUEST_WELCOME_MESSAGE = "welcome_message";
    public static final String REQUEST_LEAVE = "leave";
    public static final String REQUEST_SAVED_LOCATION = "saved_location";

// server constants
    public static final String RESPONSE_STATUS = "server";
    public static final String RESPONSE_STATUS_ACCEPTED = "accepted";
    public static final String RESPONSE_STATUS_UPDATED = "updated";
    public static final String RESPONSE_STATUS_CHECK = "check";
//    public static final String RESPONSE_STATUS_DISCONNECTED = "disconnected";
    public static final String RESPONSE_STATUS_ERROR = "error";

    public static final String REQUEST_DELIVERY_CONFIRMATION = "delivery";


    public static final String RESPONSE_MESSAGE = "message";
    public static final String RESPONSE_TOKEN = "token";
    public static final String RESPONSE_CONTROL = "control";
    public static final String RESPONSE_NUMBER = "number";
    public static final String RESPONSE_INITIAL = "initial";
    public static final String RESPONSE_PRIVATE = "to";
    public static final String RESPONSE_SIGN = "sign";

    public static final String USER_JOINED = "joined";
    public static final String USER_DISMISSED = "dismissed";
    public static final String USER_LEFT = "left";

    public static final String USER_PROVIDER = "pr";
    public static final String USER_LATITUDE = "la";
    public static final String USER_LONGITUDE = "lo";
    public static final String USER_ALTITUDE = "al";
    public static final String USER_ACCURACY = "ac";
    public static final String USER_BEARING = "be";
    public static final String USER_TILT = "ti";
    public static final String USER_SPEED = "sp";

    public static final String USER_NUMBER = "number";
    public static final String USER_COLOR = "color";
    public static final String USER_NAME = "name";
    public static final String USER_MESSAGE = "user_message";
    public static final String USER_ADDRESS = "address";
    public static final String USER_DESCRIPTION = "description";

    public static final String DATABASE_SECTION_GROUPS = "_groups";
    public static final String DATABASE_SECTION_PUBLIC = "b"; // public
    public static final String DATABASE_SECTION_PRIVATE = "p"; // private
    public static final String DATABASE_SECTION_USERS_DATA = "u/b"; // users/data-public
    public static final String DATABASE_USER_NAME = "name";
    public static final String DATABASE_USER_ACTIVE = "active";
    public static final String DATABASE_USER_COLOR = "color";
    public static final String DATABASE_USER_CREATED = "created";
    public static final String DATABASE_USER_CHANGED = "changed";
    public static final String DATABASE_SECTION_USERS_DATA_PRIVATE = "u/p"; // users/data-private
    public static final String DATABASE_SECTION_USERS_KEYS = "u/k"; // users/keys
    public static final String DATABASE_SECTION_OPTIONS = "o"; // options
    public static final String DATABASE_SECTION_OPTIONS_PERSISTENT = "o/persistent";
    public static final String DATABASE_SECTION_OPTIONS_TIME_TO_LIVE_IF_EMPTY = "o/time-to-live-if-empty";
    public static final String DATABASE_SECTION_OPTIONS_DELAY_TO_DISMISS = "o/delay-to-dismiss";
    public static final String DATABASE_SECTION_OPTIONS_DISMISS_INACTIVE = "o/dismiss-inactive";
    public static final String DATABASE_SECTION_OPTIONS_REQUIRES_PASSWORD = "o/requires-password";
    public static final String DATABASE_SECTION_OPTIONS_WELCOME_MESSAGE = "o/welcome-message";
    public static final String DATABASE_SECTION_OPTIONS_DATE_CREATED = "o/date-created";
    public static final String DATABASE_SECTION_OPTIONS_DATE_CHANGED = "o/date-changed";


    public static final int LIFETIME_INACTIVE_TOKEN = 30;
    public static final int LIFETIME_REQUEST_TIMEOUT = 10;
    public final static int LIFETIME_INACTIVE_USER = 600;

}
