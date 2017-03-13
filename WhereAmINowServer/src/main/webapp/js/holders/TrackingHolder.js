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
    var noSleep;
    var wakeLockEnabled;


    function start(){

        progress = u.dialog({
            className: "progress",
            items: [
                { type: HTML.DIV, className: "progress-circle" },
                { type: HTML.DIV, className: "progress-title" },
            ]
        });
        progressTitle = progress.items[1];

        var group = window.location.pathname.split("/")[2];
        var groupOld = u.load("group");
        if(group) {
            progress.onopen();
        }

        noSleep = new NoSleep();
        wakeLockEnabled = false;

        u.require("/js/helpers/TrackingFB.js", startTracking.bind(this));

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
                object.add(DRAWER.SECTION_EXIT,EVENTS.TRACKING_STOP,"Exit group","clear",function(){
                    main.fire(EVENTS.TRACKING_STOP);
                });
                drawerItemShare = object.add(DRAWER.SECTION_COMMUNICATION,EVENTS.SHARE_LINK,"Share group","share",function(){
                    main.fire(EVENTS.SHARE_LINK);
                });
                drawerItemShare.classList.add("disabled");
                break;
            case EVENTS.TRACKING_ACTIVE:
                if(main.tracking.getStatus() == EVENTS.TRACKING_ACTIVE && drawerItemShare) {
                    drawerItemShare.classList.remove("disabled");
                }
                if (!wakeLockEnabled) {
                    noSleep.enable(); // keep the screen on!
                    wakeLockEnabled = true;
                }
                break;
            case EVENTS.TRACKING_CONNECTING:
                if(drawerItemShare) {
                    drawerItemShare.classList.add("disabled");
                }
                if (!wakeLockEnabled) {
                    noSleep.enable(); // keep the screen on!
                    wakeLockEnabled = true;
                }
                break;
            case EVENTS.TRACKING_RECONNECTING:
                if(drawerItemShare) {
                    drawerItemShare.classList.add("disabled");
                }
                if (!wakeLockEnabled) {
                    noSleep.enable(); // keep the screen on!
                    wakeLockEnabled = true;
                }
                break;
            case EVENTS.TRACKING_DISABLED:
                if(drawerItemShare) {
                    drawerItemShare.classList.add("disabled");
                }
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
                console.log("share link",main.tracking.getTrackingUri())
                break;
            default:
                break;
        }
        return true;
    }

    function startTracking(){

        this.tracking = new TrackingFB(main);
        main.tracking = this.tracking;
        // console.log("LOADED", tracking);
        // tracking.start();

        var group = window.location.pathname.split("/")[2];
        var groupOld = u.load("group");
        if(group) {
            main.fire(EVENTS.TRACKING_JOIN, window.location.href);
            this.tracking.setLink(window.location.href);
            this.tracking.setTrackingListener(onTrackingListener);
            u.save("group",group);
            this.tracking.start();
        }

    }

    var onTrackingListener = {
        onCreating: function(){
            // console.log("ONCREATING");
            progressTitle.innerHTML = "Connecting...";
            progress.onopen();
            u.save(TRACKING_URI, null);
            main.fire(EVENTS.TRACKING_CONNECTING);
        },
        onJoining: function(){
            // console.log("ONJOINING");
            progressTitle.innerHTML = "Joining group...";
            progress.onopen();
            main.fire(EVENTS.TRACKING_RECONNECTING, "Joining group...");
        },
        onReconnecting: function(){
            // console.log("ONRECONNECTING");
            progressTitle.innerHTML = "Reconnecting...";
            progress.onopen();
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
                    main.fire(EVENTS.TOKEN_CREATED, o[RESPONSE.TOKEN]);
                }
                if (o[REQUEST.WELCOME_MESSAGE]) {
                    main.fire(EVENTS.WELCOME_MESSAGE, o[RESPONSE.WELCOME_MESSAGE]);
                }
                if (o[RESPONSE.NUMBER]) {
                    main.users.forMe(function (number, user) {
                        user.createViews();
                        progress.onclose();
                    })
                }
                if (o[RESPONSE.INITIAL]) {
                    main.users.forAllUsersExceptMe(function (number, user) {
                        user.createViews();
                    })
                }
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
                        if (o[USER.DISMISSED]) {
                            var number = o[USER.DISMISSED];
                            // console.log("DISMISSED",number);
                            var user = main.users.users[number];
                            user.fire(EVENTS.MAKE_INACTIVE);
                            main.fire(USER.DISMISSED, user);
                        } else if (o[USER.JOINED]) {
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

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
        dependsOnUser:false,
        perform:perform,
        saveable:true,
    }
}