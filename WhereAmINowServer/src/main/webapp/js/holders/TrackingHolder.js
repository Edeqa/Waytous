/**
 * Created 2/9/17.
 */
EVENTS.SHARE_LINK = "share_link";

function TrackingHolder(main) {

    var type ="tracking";

    const TRACKING_URI = "uri";
//    var tracking;
    var progress;
    var progressTitle;
    var drawerItemShare;
    var drawerItemNew;
    var drawerItemExit;
    var noSleep;
    var wakeLockEnabled;
    var shareDialog;
    var shareBlockedDialog;
    var drawerItemNewIcon;

    var drawerItemNewIconSvg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"0 0 24 24",
        version:"1.1",
        className: "menu-item"
    };
    var drawerItemNewIconPath = {
        xmlns:"http://www.w3.org/2000/svg",
        fill:"darkslategray",
        d: "M10,2l-6.5,15 0.5,0.5L9,15L12.29,7.45z M14,5.5l-6.5,15 0.5,0.5 6,-3l6,3 0.5,-0.5z"
    };

    function start(){

        progress = u.dialog({
            className: "progress",
            items: [
                { type: HTML.DIV, className: "progress-circle" },
                { type: HTML.DIV, className: "progress-title" },
            ]
        });
        progressTitle = progress.items[1];
        noSleep = new NoSleep();
        wakeLockEnabled = false;

    }

    function perform(json){
//        console.log("JSON",json);
        var loc = u.jsonToLocation(json);
        var number = json[USER.NUMBER];
        main.users.forUser(number, function(number,user){
            user.addLocation(loc);
        });

        // final Location location = Utils.jsonToLocation(o);
        // int number = o.getInt(USER_NUMBER);
        //
        // State.getInstance().getUsers().forUser(number,new MyUsers.Callback() {
        // @Override
        //     public void call(Integer number, MyUser myUser) {
        //         myUser.addLocation(location);
        //     }
        // });

    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                drawerItemNewIcon = drawerItemNewIcon || u.create(HTML.PATH, drawerItemNewIconPath, u.create(HTML.SVG, drawerItemNewIconSvg)).parentNode;
                drawerItemNew = object.add(DRAWER.SECTION_PRIMARY,EVENTS.TRACKING_NEW,"Create group",drawerItemNewIcon,function(){
                    main.fire(EVENTS.TRACKING_NEW);
                });
                drawerItemNew.hide();
                drawerItemExit = object.add(DRAWER.SECTION_LAST,EVENTS.TRACKING_STOP,"Exit group","clear",function(){
                    main.fire(EVENTS.TRACKING_STOP);
                });
                drawerItemExit.hide();
                drawerItemShare = object.add(DRAWER.SECTION_COMMUNICATION,EVENTS.SHARE_LINK,"Share group","share",function(e){
                    if(EVENTS.TRACKING_ACTIVE) {
                        main.fire(EVENTS.SHARE_LINK,e);
                    }
                });
                drawerItemShare.hide();
                drawerItemShare.disable();
                break;
            case EVENTS.MAP_READY:
                drawerItemNew.show();
                var group = window.location.pathname.split("/")[2];
                var groupOld = u.load("group");
                if(group) {
                    var self = this;
                    setTimeout(function(){
                        u.require("/js/helpers/TrackingFB.js", startTracking.bind(self));
                    }, 0);
                }
                break;
            case EVENTS.TRACKING_NEW:
                var self = this;
                setTimeout(function(){
                    u.require("/js/helpers/TrackingFB.js", startTracking.bind(self));
                }, 0);
                break;
            case EVENTS.TRACKING_ACTIVE:
                document.title = main.appName + " - " + main.tracking.getToken();
                if(main.tracking.getStatus() == EVENTS.TRACKING_ACTIVE && drawerItemShare) {
                    drawerItemShare.enable();
                }
                if (!wakeLockEnabled) {
                    noSleep.enable(); // keep the screen on!
                    wakeLockEnabled = true;
                }
                break;
            case EVENTS.TRACKING_CONNECTING:
                document.title = "Connecting - " + main.appName;
                drawerItemNew.hide();
                drawerItemShare.show().enable();
                drawerItemExit.show();
                if (!wakeLockEnabled) {
                    noSleep.enable(); // keep the screen on!
                    wakeLockEnabled = true;
                }
                break;
            case EVENTS.TRACKING_RECONNECTING:
                document.title = "Connecting - " + main.appName;
                drawerItemNew.hide();
                drawerItemShare.show().enable();
                drawerItemExit.show();
                if (!wakeLockEnabled) {
                    noSleep.enable(); // keep the screen on!
                    wakeLockEnabled = true;
                }
                break;
            case EVENTS.TRACKING_DISABLED:
                document.title = main.appName;
                drawerItemShare.disable();
                drawerItemExit.hide();
                if (wakeLockEnabled) {
                    noSleep.disable(); // let the screen turn off.
                    wakeLockEnabled = false;
                }
                break;
            case EVENTS.TRACKING_STOP:
                if(main.tracking.getStatus() != EVENTS.TRACKING_DISABLED) {
                    main.users.forAllUsersExceptMe(function (number, user) {
                        user.removeViews();
                    });
                    main.tracking && main.tracking.stop();
                    u.save("group");
                }
                break;
            case EVENTS.SHARE_LINK:
                if(shareDialog) shareDialog.close();
                shareDialog = shareDialog || u.dialog({
                    items: [
                        {type:HTML.DIV, innerHTML:"Let your e-mail client compose the message with link to this group?"},
                        {type:HTML.DIV, innerHTML:"You can add there all your friends you'd like."},
//                        {type:HTML.DIV, innerHTML:"Note: may be your browser locks pop-ups. If so please unlock this ability for calling e-mail properly."}
                    ],
                    positive: {
                        label: "OK",
                        onclick: function() {
                            var popup = window.open("mailto:?subject=Follow%20me%20at%20Waytogo.us&body="+main.tracking.getTrackingUri(),"_blank");
                            u.popupBlockerChecker.check(popup, function() {
                                shareBlockedDialog = shareBlockedDialog || u.dialog({
                                    items: [
                                        {type:HTML.DIV, innerHTML:"Perhaps, your browser blocks pop-ups."},
                                        {type:HTML.DIV, innerHTML:"If so please unlock this ability for calling e-mail properly."},
                                        {type:HTML.DIV, enclosed:true, innerHTML:"Detailed information how to unblock this feature"},
                                        {type:HTML.DIV, innerHTML:"Also, you can send the link manually. Copy it clicking on the button below."},
                                        {type:HTML.DIV, innerHTML:main.tracking.getTrackingUri()}
                                    ],
                                    positive: {
                                        label: "Close"
                                    },
                                });
                                shareBlockedDialog.open();
                            });
                        }
                    },
                    negative: {
                        label: "Cancel"
                    },
                    timeout: 20000
                });
                shareDialog.open();
                break;
            default:
                break;
        }
        return true;
    }

    function startTracking(){

        progress.open();

        this.tracking = main.tracking = new TrackingFB(main);
        // console.log("LOADED", tracking);
        // tracking.start();

        var group = window.location.pathname.split("/")[2];
        var groupOld = u.load("group");
        if(group) {
            main.fire(EVENTS.TRACKING_JOIN, window.location.href);
            this.tracking.setLink(window.location.href);
            u.save("group",group);
        } else {
            progressTitle.innerHTML = "Creating group...";
            console.log("NEW")
        }
        this.tracking.setTrackingListener(onTrackingListener);
        this.tracking.start();

    }

    var onTrackingListener = {
        onCreating: function(){
            // console.log("ONCREATING");
            progressTitle.innerHTML = "Connecting...";
            progress.open();
            u.save(TRACKING_URI, null);
            main.fire(EVENTS.TRACKING_CONNECTING);
        },
        onJoining: function(){
            // console.log("ONJOINING");
            progressTitle.innerHTML = "Joining group...";
            progress.open();
            main.fire(EVENTS.TRACKING_RECONNECTING, "Joining group...");
        },
        onReconnecting: function(){
            // console.log("ONRECONNECTING");
            progressTitle.innerHTML = "Reconnecting...";
            progress.open();
            main.fire(EVENTS.TRACKING_RECONNECTING, "Reconnecting...");
        },
        onClose: function(){
            console.log("ONCLOSE");
        },
        onAccept: function(o){
            // console.log("ONACCEPT",o);
            //FIXME
//            u.save(TRACKING_URI, this.tracking.getTrackingUri());
            try {
                if(main.tracking.getStatus() != EVENTS.TRACKING_ACTIVE) {
                    main.tracking.setStatus(EVENTS.TRACKING_ACTIVE);
                    main.fire(EVENTS.TRACKING_ACTIVE);
                }
                if (o[RESPONSE.TOKEN]) {
                    var token = o[RESPONSE.TOKEN];
                    main.fire(EVENTS.TOKEN_CREATED, token);
                    u.save("group", token);
                    window.history.pushState({}, null, "/track/" + token);
                    main.fire(EVENTS.SHOW_HELP, {module: main.holders.tracking, article: 1});
                    main.me.fire(EVENTS.SELECT_USER);
                }
                if (o[REQUEST.WELCOME_MESSAGE]) {
                    main.fire(EVENTS.WELCOME_MESSAGE, o[RESPONSE.WELCOME_MESSAGE]);
                }
                if (o[RESPONSE.NUMBER] != undefined) {
                    main.users.forMe(function (number, user) {
                        user.createViews();
                        progress.close();
                    })
                }
//                if (o[RESPONSE.INITIAL]) {
//                    main.users.forAllUsersExceptMe(function (number, user) {
//                        user.createViews();
//                    })
//                }
            } catch(e) {
                console.error(e);
            }
        },
        onReject: function(reason){
            console.error("ONREJECT",reason);
            u.save(TRACKING_URI);
            main.fire(EVENTS.TRACKING_DISABLED);
            main.fire(EVENTS.TRACKING_ERROR, reason);
        },
        onStop: function(){
            console.log("ONSTOP");
            u.save(TRACKING_URI);
            main.fire(EVENTS.TRACKING_DISABLED);
        },
        onMessage: function(o){
            // console.log("ONMESSAGE",o);
            try {
                var response = o[RESPONSE.STATUS];
                switch (response) {
                    case RESPONSE.STATUS_UPDATED:
                        if (o[USER.DISMISSED] != undefined) {
                            var number = o[USER.DISMISSED];
                            // console.log("DISMISSED",number);
                            var user = main.users.users[number];
                            user.fire(EVENTS.MAKE_INACTIVE);
                            main.fire(USER.DISMISSED, user);
                        } else if (o[USER.JOINED] != undefined) {
                            var number = o[USER.JOINED];
                            var user = main.users.users[number];
                            user.fire(EVENTS.MAKE_ACTIVE);
                            main.fire(USER.JOINED, user);
                            // console.log("JOINED",number);
                        }
                        break;
                    case RESPONSE.LEAVE:
                        if (o[USER.NUMBER]) {
                            console.log("LEAVE", o[USER.NUMBER]);
                        }
                        break;
                    case RESPONSE.CHANGE_NAME:
                        if (o[USER.NAME]) {
                            console.log("CHANGENAME", o[USER.NUMBER], o[USER.NAME]);
                        }
                        break;
                    default:
                        // console.log(type,response,o);
                        var holder = main.holders[response];
                        if (holder && holder.perform) {
                            holder.perform(o);
                        }
                        break;
                }
            } catch(e) {
                console.error(e);
            }
        }
    };

    function help(){
        return {
            title: "Tracking",
            1: {
                title: "You have created new group",
                body: "You have created the new tracking group. Now, you may invite your friends to follow you using their Waytogo client or mobile or desktop browser. Click the main menu item to share the link. Or you may use it yourself for some reasons. <p>Note: the group will be deleted after 15 minutes of inactivity. "
            }
        }
    }


    return {
        type:type,
        start:start,
        onEvent:onEvent,
        perform:perform,
        saveable:true,
        help:help,
    }
}