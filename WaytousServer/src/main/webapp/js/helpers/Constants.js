/**
 * Created 2/9/17.
 */
function Constants() {

    window.EVENTS = {
        SELECT_USER: "select",
        SELECT_SINGLE_USER: "select_single",
        UNSELECT_USER: "unselect",
        MAKE_ACTIVE: "make_active",
        MAKE_INACTIVE: "make_inactive",
        CHANGE_NAME: "change_name",
        CHANGE_NUMBER: "change_number",
        CHANGE_COLOR: "change_color",
        SYSTEM_MESSAGE: "system_message",
        MAP_MY_LOCATION_BUTTON_CLICKED: "map_my_location_button_clicked",

        CREATE_CONTEXT_MENU: "create_context_menu",
        CREATE_OPTIONS_MENU: "create_options_menu",
        PREPARE_OPTIONS_MENU: "prepare_options_menu",
        PREPARE_FAB: "prepare_fab",
        CREATE_DRAWER: "create_drawer",
        PREPARE_DRAWER: "prepare_drawer",
        DROPPED_TO_USER: "dropped_to_user",

        ACTIVITY_CREATE: "activity_create",
        ACTIVITY_PAUSE: "activity_pause",
        ACTIVITY_RESUME: "activity_resume",
        ACTIVITY_DESTROY: "activity_destroy",
        ACTIVITY_RESULT: "activity_result",

        TRACKING_NEW: "tracking_new",
        TRACKING_JOIN: "tracking_join",
        TRACKING_STOP: "tracking_stop",
        TRACKING_DISABLED: "tracking_disabled",
        TRACKING_CONNECTING: "tracking_connecting",
        TRACKING_ACTIVE: "tracking_active",
        TRACKING_RECONNECTING: "tracking_reconnecting",
        TRACKING_EXPIRED: "tracking_expired",
        TRACKING_ERROR: "tracking_error",
        TOKEN_CREATED: "token_created",


        MAP_READY: "map_ready",

        MOVING_CLOSE_TO: "moving_close_to",
        MOVING_AWAY_FROM: "moving_away_from",

        MOUSE_OVER: "mouse_over",
        MOUSE_OUT: "mouse_out",

        SHOW_BADGE: "show_badge",
        HIDE_BADGE: "hide_badge",
        INCREASE_BADGE: "increase_badge"

    };
    window.REQUEST = {
        REQUEST: "client",
        TIMESTAMP : "timestamp",
        UPDATE : "update",
        JOIN_TOKEN : "join",
        NEW_TOKEN : "create",
        CHECK_USER : "check",
        TOKEN : "token",
        HASH : "hash",
        PUSH : "push",
        ADMIN : "admin",

        DEVICE_ID : "device_id",
        MODEL : "model",
        MANUFACTURER : "manufacturer",
        OS : "os",
        KEY : "key",

        TRACKING : "tracking",
        MESSAGE : "message",
        CHANGE_NAME : "change_name",
        WELCOME_MESSAGE : "welcome_message",
        LEAVE : "leave",
        SAVED_LOCATION : "saved_location",

        DELIVERY_CONFIRMATION : "delivery"
    };
    window.RESPONSE = {
        STATUS : "server",
        STATUS_ACCEPTED : "accepted",
        STATUS_UPDATED : "updated",
        STATUS_CHECK : "check",
        STATUS_ERROR : "error",


        MESSAGE : "message",
        TOKEN : "token",
        CONTROL : "control",
        NUMBER : "number",
        INITIAL : "initial",
        PRIVATE : "to",
        SIGN : "sign"
    };
    window.USER = {
        JOINED : "joined",
        DISMISSED : "dismissed",
        LEFT : "left",

        PROVIDER : "pr",
        LATITUDE : "la",
        LONGITUDE : "lo",
        ALTITUDE : "al",
        ACCURACY : "ac",
        BEARING : "be",
        TILT : "ti",
        SPEED : "sp",

        NUMBER : "number",
        COLOR : "color",
        NAME : "name",
        MESSAGE : "user_message",
        ADDRESS : "address",
        DESCRIPTION : "description"
    };
    window.DATABASE = {
        SECTION_GROUPS : "_groups",
        SECTION_PUBLIC : "b", // public
        SECTION_PRIVATE : "p", // private
        SECTION_USERS_DATA : "u/b", // users/data-public
        USER_NAME : "name", // name
        USER_ACTIVE : "active", // active
        USER_CREATED : "created", // created
        USER_CHANGED : "changed", // changed
        SECTION_USERS_DATA_PRIVATE : "u/p", // users/data-private
        SECTION_USERS_KEYS : "u/k", // users/keys
        SECTION_OPTIONS : "o", // options
        SECTION_OPTIONS_PERSISTENT : "o/persistent",
        SECTION_OPTIONS_TIME_TO_LIVE_IF_EMPTY : "o/time-to-live-if-empty",
        SECTION_OPTIONS_DELAY_TO_DISMISS : "o/delay-to-dismiss",
        SECTION_OPTIONS_DISMISS_INACTIVE : "o/dismiss-inactive",
        SECTION_OPTIONS_REQUIRES_PASSWORD : "o/requires-password",
        SECTION_OPTIONS_WELCOME_MESSAGE : "o/welcome-message",
        SECTION_OPTIONS_DATE_CREATED : "o/date-created",
        SECTION_OPTIONS_DATE_CHANGED : "o/date-changed"
    };

    return {
        type:"constants",
        start:function(){}
    };
}