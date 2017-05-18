package com.edeqa.waytousserver.helpers;

import java.io.Serializable;

/**
 * Created 10/14/16.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class Constants {
    public final static int SERVER_BUILD = 33;

    public static SensitiveData SENSITIVE;

    public final static String BROADCAST = "com.edeqa.waytous.waytousservice";
    public final static String BROADCAST_MESSAGE = "message";

    public final static int LOCATION_UPDATES_DELAY = 1000;

// client constants
    public static final String REQUEST = "client";
    public static final String REQUEST_TIMESTAMP = "timestamp";
    public static final String REQUEST_UPDATE = "update";
    public static final String REQUEST_JOIN_GROUP = "join";
    public static final String REQUEST_NEW_GROUP = "create";
    public static final String REQUEST_CHECK_USER = "check";
    public static final String REQUEST_TOKEN = "token";
    public static final String REQUEST_HASH = "hash";
    public static final String REQUEST_PUSH = "push";
    public static final String REQUEST_ADMIN = "admin";

    public static final String REQUEST_DEVICE_ID = "device_id";
    public static final String REQUEST_MODEL = "model";
    public static final String REQUEST_MANUFACTURER = "manufacturer";
    public static final String REQUEST_OS = "os";
    public static final String REQUEST_KEY = "key";

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
    public static final String DATABASE_SECTION_USERS_ORDER = "u/n"; // users/order
    public static final String DATABASE_SECTION_OPTIONS = "o"; // options
    public static final String DATABASE_OPTION_PERSISTENT = "persistent";
    public static final String DATABASE_OPTION_TIME_TO_LIVE_IF_EMPTY = "time-to-live-if-empty";
    public static final String DATABASE_OPTION_DELAY_TO_DISMISS = "delay-to-dismiss";
    public static final String DATABASE_OPTION_DISMISS_INACTIVE = "dismiss-inactive";
    public static final String DATABASE_OPTION_REQUIRES_PASSWORD = "requires-password";
    public static final String DATABASE_OPTION_WELCOME_MESSAGE = "welcome-message";
    public static final String DATABASE_OPTION_DATE_CREATED = "date-created";
    public static final String DATABASE_OPTION_DATE_CHANGED = "date-changed";

    public static final int LIFETIME_REQUEST_TIMEOUT = 10;
    public static final int LIFETIME_INACTIVE_GROUP = 300;
    public final static int LIFETIME_INACTIVE_USER = 600;

    public static class REST {
        public static final String STATUS = "status",
            SUCCESS = "success",
            ERROR = "error",
            MESSAGE = "message",
            REQUEST = "request",
            GROUP_ID = "group_id",
            VALUE = "value",
            PROPERTY = "property",
            OLD_VALUE = "old_value",
            USER_NUMBER = "user_number";
    }

    public static class MIME {
        public static final String APPLICATION_JSON = "application/json",
            TEXT_PLAIN = "text/plain",
            TEXT_HTML = "text/html",
            APPLICATION_OCTET_STREAM = "application/octet-stream",
            APPLICATION_JAVASCRIPT = "application/javascript",
            TEXT_CSS = "text/css",
            APPLICATION_X_WEB_APP_MANIFEST_JSON = "application/x-web-app-manifest+json",
            AUDIO_MP3 = "audio/mp3",
            AUDIO_OGG = "audio/ogg",
            AUDIO_AAC = "audio/aac",
            APPLICATION_XML = "application/xml",
            IMAGE_GIF = "image/png",
            IMAGE_PNG = "image/png",
            IMAGE_JPG = "image/jpg",
            IMAGE_ICO = "image/ico",
            IMAGE_SVG_XML = "image/svg+xml";
    }

}
