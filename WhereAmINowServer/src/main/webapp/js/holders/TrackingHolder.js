/**
 * Created 2/9/17.
 */
function TrackingHolder(main) {

    var type ="tracking";

    const TRACKING_URI = "uri";
    var tracking;
    var progress;
    var progressTitle;


    function start(){

        progress = u.create("div", {className:"progress shadow hidden"}, main.right);
        u.create("div", {className:"progress-circle"}, progress);
        progressTitle = u.create("div", {className:"progress-title"}, progress);
        var group = window.location.pathname.split("/")[2];
        var groupOld = u.load("group");
        if(group) {
            progress.classList.remove("hidden");
        }

        u.require("/js/tracking/TrackingFB.js", startTracking);

    }

    function perform(json){
        console.log("JSON",json);
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                break;
            default:
                break;
        }
        return true;
    }

    var startTracking = function(){

        tracking = new TrackingFB(main);
        // console.log("LOADED", tracking);
        // tracking.start();

        var group = window.location.pathname.split("/")[2];
        var groupOld = u.load("group");
        if(group) {
            main.fire(EVENTS.TRACKING_JOIN, window.location.href);
            tracking.setLink(window.location.href);
            tracking.setTrackingListener(onTrackingListener);
            u.save("group",group);
            tracking.start();
        }

    };

    var onTrackingListener = {
        onCreating: function(){
            // console.log("ONCREATING");
            progressTitle.innerHTML = "Connecting...";
            progress.classList.remove("hidden");
            u.save(TRACKING_URI, null);
            main.fire(EVENTS.TRACKING_CONNECTING);
        },
        onJoining: function(){
            // console.log("ONJOINING");
            progressTitle.innerHTML = "Joining group...";
            progress.classList.remove("hidden");
            main.fire(EVENTS.TRACKING_RECONNECTING, "Joining group...");
        },
        onReconnecting: function(){
            // console.log("ONRECONNECTING");
            progressTitle.innerHTML = "Reconnecting...";
            progress.classList.remove("hidden");
            main.fire(EVENTS.TRACKING_RECONNECTING, "Reconnecting...");
        },
        onClose: function(){
            console.log("ONCLOSE");
        },
        onAccept: function(o){
            // console.log("ONACCEPT",o);
            progress.classList.add("hidden");
            u.save(TRACKING_URI, tracking.getTrackingUri());
            main.fire(EVENTS.TRACKING_ACTIVE);
            if(o[RESPONSE.TOKEN]) {
                main.fire(EVENTS.TOKEN_CREATED, o[RESPONSE.TOKEN]);
            }
            if(o[REQUEST.WELCOME_MESSAGE]) {
                main.fire(EVENTS.WELCOME_MESSAGE, o[RESPONSE.WELCOME_MESSAGE]);
            }
            if(o[RESPONSE.NUMBER]) {
                main.users.forMe(function(number,user){
                    user.createViews();
                })
            }
            if(o[RESPONSE.INITIAL]) {
                main.users.forAllUsersExceptMe(function(number,user){
                    user.createViews();
                })
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
            var response = o[RESPONSE.STATUS];
            switch (response){
                case RESPONSE.STATUS_UPDATED:
                    if(o[USER.DISMISSED]) {
                        var number = o[USER.DISMISSED];
                        // console.log("DISMISSED",number);
                        var user = main.users.users[number];
                        user.fire(EVENTS.MAKE_INACTIVE);
                        main.fire(USER.DISMISSED, user);
                    } else if(o[USER.JOINED]){
                        var number = o[USER.JOINED];
                        var user = main.users.users[number];
                        user.fire(EVENTS.MAKE_ACTIVE);
                        main.fire(USER.JOINED, user);

                        // console.log("JOINED",number);
                    }
                    break;
                case RESPONSE.LEAVE:
                    if(o[USER.NUMBER]) {
                        console.log("LEAVE",o[USER.NUMBER]);
                    }
                    break;
                case RESPONSE.CHANGE_NAME:
                    if(o[USER.NAME]) {
                        console.log("CHANGENAME",o[USER.NUMBER],o[USER.NAME]);
                    }
                    break;
                default:
                    var holder = main.holders[response];
                    if(holder && holder.perform){
                        holder.perform(o);
                    }
                    break;
            }
        }
    };

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
        dependsOnUser:false,
    }
}