/**
 * Created 1/19/17.
 */

function TrackingFB(main) {

	var link;
	var trackingListener;
	var json;
	var newTracking;
	var token;
	var status;
	var serverUri;
	var ref;

    var start = function() {
        status = EVENTS.TRACKING_DISABLED;
        var uri;
        if(this.link) {
            uri = new URL(this.link);
        } else {
            newTracking = true;
        }
        var path = uri.pathname.replace("/group/","/track/");
        serverUri = "wss://" + uri.hostname + ":8001" + path;

        if(newTracking) {
            setStatus(EVENTS.TRACKING_CONNECTING);
            trackingListener.onCreating();
        } else {
            setStatus(EVENTS.TRACKING_RECONNECTING);
            trackingListener.onJoining()
        }
        webSocketListener = webSocketListener(serverUri);
    };

    var webSocketListener = function (link) {
        var a = new WebSocket(link);

        a.onopen = function(event) {
            if(newTracking) {
                put(REQUEST.REQUEST, REQUEST.NEW_TOKEN);
            } else {
                var parts = link.split("/");
                var groupId = parts[parts.length-1];
                setToken(groupId);

                put(REQUEST.REQUEST, REQUEST.JOIN_TOKEN);
                put(REQUEST.TOKEN, groupId);
                put(REQUEST.DEVICE_ID, u.getUuid());
            }
            put(REQUEST.MODEL, navigator.appCodeName );
            put(REQUEST.MANUFACTURER, navigator.appCodeName);
            put(REQUEST.OS, navigator.platform);
            // put("aaa", navigator.appVersion);
            var name = u.load(USER.NAME);
            if(name) put(USER.NAME, name);

            send();
        };

        a.onmessage = function(event) {
            var o = JSON.parse(event.data);
            if(!o[RESPONSE.STATUS]) return;
            switch (o[RESPONSE.STATUS]) {
                case RESPONSE.STATUS_CHECK:
                    if(RESPONSE.CONTROL) {
                        var control = o[RESPONSE.CONTROL];
                        var deviceId = u.getUuid();
                        var hash = u.getEncryptedHash(control +":"+ deviceId);
                        put(REQUEST.REQUEST, REQUEST.CHECK_USER);
                        put(REQUEST.HASH, hash + 1);
                        send();
                    }
                    break;
                case RESPONSE.STATUS_ACCEPTED:
                    newTracking = false;
                    if(o[RESPONSE.SIGN]) {
                        var authToken = o[RESPONSE.SIGN];
                        delete o[RESPONSE.SIGN];

                        // console.log("SIGN WITH",authToken);
                        firebase.auth().signInWithCustomToken(authToken)
                            .then(function(e){

                                setStatus(EVENTS.TRACKING_ACTIVE);
                                if (o[RESPONSE.TOKEN]) {
                                    setToken(o[RESPONSE.TOKEN]);
                                }
                                if (o[RESPONSE.NUMBER]) {
                                    console.log("SETNUMBER", o[RESPONSE.NUMBER]);
                                    var old = main.me.number;
                                    main.me.number = o[RESPONSE.NUMBER];
                                    main.users.users[o[RESPONSE.NUMBER]] = main.me;
                                    if (old) delete main.users.users[old];
                                }
                                o[RESPONSE.INITIAL] = true;

                                ref = database.ref().child(getToken());

                                registerChildListener(ref.child(DATABASE.SECTION_USERS_DATA), usersDataListener, -1);
                                for (var i in main.holders) {
                                    if (main.holders[i] && main.holders.saveable) {
                                        try {
                                            registerChildListener(ref.child(DATABASE.SECTION_PRIVATE).child(i).child(main.me.number), userPrivateDataListener, -1);
                                        } catch(e){
                                            console.error(e.message);
                                        }
                                    }
                                }
                                try {
                                    trackingListener.onAccept(o);
                                } catch(e){
                                    console.error(e.message);
                                }
                            }).catch(function(error) {
                                setStatus(EVENTS.TRACKING_DISABLED);
                                trackingListener.onReject(error.message);
                        });
                    } else {
                        setStatus(EVENTS.TRACKING_DISABLED);
                        console.log("REJECTED");
                        trackingListener.onReject("Old version of server");
                    }
                    break;
                case RESPONSE.STATUS_ERROR:
                    setStatus(EVENTS.TRACKING_DISABLED);
                    trackingListener.onReject(o[RESPONSE.MESSAGE] ? o[RESPONSE.MESSAGE] : "");
                    break;
                default:
                    trackingListener.onMessage(o);
                    break;
            }
        };

        a.onclose = function(event) {
        };

        a.onerror = function(event) {
            console.log("ONERROR-RECONNECT-SHOULDBEDONE")
            if(status == EVENTS.TRACKING_DISABLED) return;
        };

        return a;
    };

    var put = function(name, value){
        if(!json) json = {};
        json[name] = value;
    };

    var send = function () {
        // console.log("SEND",json);
        put(REQUEST.TIMESTAMP, new Date().getTime());
        webSocketListener.send(JSON.stringify(json));
        json = {};
    };

    var setLink = function(link) {
        this.link = link;
    };

    var setTrackingListener = function(callback) {
        trackingListener = callback;
    };

    var setToken = function(id){
        this.token = id;
    };

    var getToken = function(){
        return this.token;
    }

    var setStatus = function(status){
        this.status = status;
    };

    var getTrackingUri = function(){
        return "http://" + serverUri.host + ":" + 8080 + "/track/" + token;
    };

    var registerChildListener = function(ref, listener, limit) {
        if(limit >= 0){
            ref.limitToLast(limit).on("child_added", listener);
        } else {
            ref.on("child_added", listener);
        }
        // refs[ref] = listener;
    };

    var registerValueListener = function(ref, listener) {
        ref.on("value", listener);
        // refs[ref] = listener;
    };

    var usersDataListener = function(data){
        if(main.me.number != parseInt(data.getKey())) {
            try{
                var o = data.val();
                o[RESPONSE.NUMBER] = parseInt(data.getKey());
                o[RESPONSE.INITIAL] = true;
                delete o.active;

                var user = main.users.addUser(o);
                user.user = true;

                //registers
                registerValueListener(ref.child(DATABASE.SECTION_USERS_DATA).child(user.number).child("name"), usersDataNameListener);
                registerValueListener(ref.child(DATABASE.SECTION_USERS_DATA).child(user.number).child("active"), usersDataActiveListener);

                usersDataNameListener(data.child("name"));
                usersDataActiveListener(data.child("active"));

                for(var i in main.holders) {
                    if(main.holders[i] && main.holders.saveable) {
                        registerChildListener(ref.child(DATABASE.SECTION_PUBLIC).child(i).child(user.number), userPublicDataListener, 1);
                    }
                }

                trackingListener.onAccept(o);
            } catch(e) {
                console.error(e.message);
            }
        }
        // console.log(data);
    };

    var userPublicDataListener = function(data) {
        try {
            var o = data.val();
            debugger;
            o[RESPONSE.NUMBER] = parseInt(data.getRef().getParent().getKey());
            o[RESPONSE.STATUS] = data.getRef().getParent().getParent().getKey();
            o["key"] = data.getKey();

            trackingListener.onMessage(o);
        } catch(e) {
            console.error(e.message);
        }

    }

    var userPrivateDataListener = function(data) {
        try{
            var o = data.val();
            var from = parseInt(o["from"]);
            delete o["from"];
            o[RESPONSE.NUMBER] = from;
            o[RESPONSE.STATUS] = data.getRef().parent().parent().getKey();
            o["key"] = data.getKey();
debugger;
            // trackingListener.onMessage(o);
            // data.getRef().remove();

        } catch(e) {
            console.error(e.message);
        }
        console.log(data);
    };

    var usersDataNameListener = function(data) {
        try {
            var number = parseInt(data.ref.parent.getKey());
            var name = data.val();
            main.users.forUser(number, function(number, user, name){
                if(user.properties && name != user.properties.name) {
                    user.fire(EVENTS.CHANGE_NAME, name);
                }
            }, name);
        } catch(e) {
            console.error(e.message);
        }
    }

    var usersDataActiveListener = function(data) {
        try {
            var number = parseInt(data.ref.parent.getKey());
            var active = data.val();
            var user = main.users.users[number];
            if(user && user.properties && active != user.properties.active) {
                var o = {};
                o[RESPONSE.STATUS] = RESPONSE.STATUS_UPDATED;
                o[active ? USER.JOINED : USER.DISMISSED] = number;
                o[RESPONSE.NUMBER] = number;
                trackingListener.onMessage(o);
            }
        } catch(e) {
            console.error(e.message);
        }
    }

    return {
        start: start,
        title: "Group",
        menu: true,
        setLink:setLink,
        setTrackingListener:setTrackingListener,
        getTrackingUri:getTrackingUri,
    }
}