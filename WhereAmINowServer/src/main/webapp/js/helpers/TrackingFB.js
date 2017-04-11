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
    var updateTask;

    function start() {
        status = EVENTS.TRACKING_DISABLED;
        var uri;
        if(this.link) {
            uri = new URL(this.link);
        } else {
            newTracking = true;
            uri = new URL(window.location.href);
        }

        var path = uri.pathname.replace("/group/","/track/");
        serverUri = "wss://" + uri.hostname + ":"+ data.WSS_FB_PORT + path;
//         serverUri = "ws://" + uri.hostname + ":" + data.WS_FB_PORT + path;

        if(newTracking) {
            setStatus(EVENTS.TRACKING_CONNECTING);
            trackingListener.onCreating();
        } else {
            setStatus(EVENTS.TRACKING_RECONNECTING);
            trackingListener.onJoining()
        }
        webSocketListener = webSocketListener(serverUri);

    }

    function stop(){
        status = EVENTS.TRACKING_DISABLED;

        var updates = {};
        clearInterval(updateTask);
        updates[DATABASE.USER_ACTIVE] = false;
        updates[DATABASE.USER_CHANGED] = firebase.database.ServerValue.TIMESTAMP;

//console.log("UPDATE",DATABASE.SECTION_USERS_DATA + "/" + main.me.number,updates);
        ref.child(DATABASE.SECTION_USERS_DATA + "/" + main.me.number).update(updates);

        firebase.auth().signOut();
        trackingListener.onStop();

        var uri = new URL(this.link);
        window.location.href = "https://" + uri.hostname + ":"+ data.HTTPS_PORT + "/track/";
    }

    function webSocketListener(link) {

        var sendOriginal = send;
        var onopen =  function(event) {
            opened = true;
            if(newTracking) {
                put(REQUEST.REQUEST, REQUEST.NEW_TOKEN);
                put(REQUEST.DEVICE_ID, u.getUuid());
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

        var onmessage = function(event) {
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
                        try {
                            firebase.auth().signInWithCustomToken(authToken).then(function (e) {

                                // setStatus(EVENTS.TRACKING_ACTIVE);
                                if (o[RESPONSE.TOKEN]) {
                                    setToken(o[RESPONSE.TOKEN]);
                                }
                                if (o[RESPONSE.NUMBER]) {
                                    console.warn("Joined with number",o[RESPONSE.NUMBER]);
                                    main.users.setMyNumber(o[RESPONSE.NUMBER]);
                                }
                                o[RESPONSE.INITIAL] = true;

                                ref = database.ref().child(getToken());

                                updateTask = setInterval(updateActive, 60000);
                                registerChildListener(ref.child(DATABASE.SECTION_USERS_DATA), usersDataListener, -1);
                                for (var i in main.holders) {
                                    if (main.holders[i] && main.holders[i].saveable) {
                                        try {
                                            registerChildListener(ref.child(DATABASE.SECTION_PRIVATE).child(i).child(main.me.number), userPrivateDataListener, -1);
                                        } catch (e) {
                                            console.error(e.message);
                                        }
                                    }
                                }
                                try {
                                    trackingListener.onAccept(o);
                                } catch (e) {
                                    console.error(e.message);
                                }
                            }).catch(function (error) {
                                setStatus(EVENTS.TRACKING_DISABLED);
                                trackingListener.onReject(error.message);
                            });
                        } catch(e) {
                            console.error(e);
                            debugger;
                            main.initialize();
                        }
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

        var onclose = function(event) {
//            console.log("CLOSE",opened,event.code,event.reason,event.wasClean);
            if(!opened) {
                console.error("Websocket processing closed unexpectedly, will try to use XHR instead of " + link + " (error " + event.code + (event.reason?": "+event.reason:")")+".");
                xhrModeStart(link);
            }
        };

        var onerror = function(event) {
                console.error("Websocket processing failed, will try to use XHR instead of " + link + ".");
            if(status == EVENTS.TRACKING_DISABLED) return;
            xhrModeStart(link);
        };

        var xhrModeStart = function(link) {
            var uri = new URL(link);
            link = "https://" + uri.hostname + ":" + data.HTTPS_PORT + "/xhr/join"/* + uri.pathname*/;

            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() { //
                if (xhr.readyState != 4) return;
                xhrModeCheck(link,xhr.response);
            };
            send = function(jsonMessage){
                if(!jsonMessage) {
                    send(json);
                    json = {};
                    return;
                }
                put(REQUEST.TIMESTAMP, new Date().getTime());
                xhr.send(JSON.stringify(json));
                json = {};
            };
            xhr.open("POST", link, true);
            onopen();
        };

        var xhrModeCheck = function(link, check) {
            // var check = JSON.parse(check);

            var xhr = new XMLHttpRequest();
            xhr.onreadystatechange = function() { //
                if (xhr.readyState != 4) return;
                send = sendOriginal;
                onmessage({data:xhr.response});
            };
            send = function(jsonMessage){
                if(!jsonMessage) {
                    send(json);
                    json = {};
                    return;
                }
                put(REQUEST.TIMESTAMP, new Date().getTime());
                xhr.send(JSON.stringify(json));
                json = {};
            };
            xhr.open("POST", link, true);
            onmessage({data:check});
        };

        var a = {};
        try {
            link = link.replace(/#.*/,"");
            a = new WebSocket(link);
            setTimeout(function(){
                if(a instanceof WebSocket && a.readyState != WebSocket.OPEN) {
                    a.close();
                }
            }, 15000);
        } catch(e){
            console.warn(e);
            xhrModeStart(link);
        }
        var opened = false;

        a.onopen = onopen;
        a.onmessage = onmessage;
        a.onclose = onclose;
        a.onerror = onerror;

        return a;
    }


    function put(name, value){
        if(!json) json = {};
        json[name] = value;
    }

    function send(jsonMessage) {
        var updates;

        if(!jsonMessage) {
//            put(REQUEST.TIMESTAMP, new Date().getTime());
            send(json);
            json = {};
            return;
        }

        if(jsonMessage.constructor === String) {
            put(REQUEST.REQUEST, jsonMessage);
            send();
            return;
        }

        jsonMessage[REQUEST.TIMESTAMP] = new Date().getTime();
        var type = jsonMessage[REQUEST.REQUEST];
        if(type == REQUEST.NEW_TOKEN || type == REQUEST.JOIN_TOKEN || type == REQUEST.CHECK_USER) {
            // console.error("WRONG WAY");
            // switch (webSocketListener.status) {
            webSocketListener.send(JSON.stringify(json));
            //
            // }
        } else if(ref) {
            if(type == REQUEST.CHANGE_NAME) {
                updates = {};
                updates[USER.NAME] = jsonMessage[USER.NAME];
                updates[DATABASE.USER_CHANGED] = firebase.database.ServerValue.TIMESTAMP;

//console.log("UPDATE1",DATABASE.SECTION_USERS_DATA + "/" + main.me.number,updates);
                ref.child(DATABASE.SECTION_USERS_DATA).child(main.me.number).update(updates);

                return;
            } else if(type == REQUEST.WELCOME_MESSAGE) {
                console.error("WELCOMEMESSAGE");

                return;
            }

            var holder = main.holders[type];
            if(!holder || !holder.saveable) return;

            delete jsonMessage[REQUEST.REQUEST];
            delete jsonMessage[REQUEST.PUSH];
            delete jsonMessage[REQUEST.DELIVERY_CONFIRMATION];

            var path,refPath;
            if(jsonMessage.to) {
                var to = jsonMessage.to;
                delete jsonMessage.to;
                jsonMessage.from = main.me.number;
                path = DATABASE.SECTION_PRIVATE + "/" + type + "/" + to;
            } else {
                path = DATABASE.SECTION_PUBLIC + "/" + type + "/" + main.me.number;
            }
            var key = ref.push().key;

            updates = {};
            updates[path + "/" + key] = jsonMessage;
            updates[DATABASE.SECTION_USERS_DATA + "/" + main.me.number + "/" + DATABASE.USER_CHANGED] = firebase.database.ServerValue.TIMESTAMP;

//console.log("UPDATE2",updates);
            ref.update(updates);

        }
    }

    function sendMessage(type, jsonMessage) {
        json = json || {};
        for(var x in jsonMessage) {
            json[x] = jsonMessage[x];
        }
        json[REQUEST.REQUEST] = type;
        sendUpdate();
    }

    function sendUpdate() {
        json = json || {};
        if(!json[REQUEST.REQUEST]) json[REQUEST.REQUEST] = REQUEST.UPDATE;
        send();
    }

    function setLink(link) {
        this.link = link;
    }

    function setTrackingListener(callback) {
        trackingListener = callback;
    }

    function setToken(id){
        token = id;
    }

    function getToken(){
        return token;
    }

    function setStatus(currentStatus){
        status = currentStatus;
    }

    function getStatus(){
        return status;
    }

    function getTrackingUri(){
        var uri = new URL(serverUri);
        return "http://" + uri.hostname + (data.HTTP_PORT != 80 ? ":"+data.HTTP_PORT  : "") + "/track/" + token;
    }

    function registerChildListener(ref, listener, limit) {
        if(limit > 0){
            ref.limitToLast(limit).on("child_added", listener);
        } else {
            ref.on("child_added", listener);
        }
        // refs[ref] = listener;
    }

    function registerValueListener(ref, listener) {
        ref.on("value", listener);
        // refs[ref] = listener;
    }

    function usersDataListener(data){
        if(main.me.number != parseInt(data.getKey())) {
            try{
                var o = data.val();
                o[RESPONSE.NUMBER] = parseInt(data.getKey());
                o[RESPONSE.INITIAL] = true;
                delete o.active;

                var user = main.users.addUser(o);
                user.type = "user";

                //registers
                registerValueListener(ref.child(DATABASE.SECTION_USERS_DATA).child(user.number).child(DATABASE.USER_NAME), usersDataNameListener);
                registerValueListener(ref.child(DATABASE.SECTION_USERS_DATA).child(user.number).child(DATABASE.USER_ACTIVE), usersDataActiveListener);

                usersDataNameListener(data.child(DATABASE.USER_NAME));
                usersDataActiveListener(data.child(DATABASE.USER_ACTIVE));

                for(var i in main.holders) {
                    if(main.holders[i] && main.holders[i].saveable) {
                        var loadSaved = main.holders[i].loadsaved || 1;
                        registerChildListener(ref.child(DATABASE.SECTION_PUBLIC).child(i).child(user.number), userPublicDataListener, loadSaved);
                    }
                }

                trackingListener.onAccept(o);
            } catch(e) {
                console.error(e.message);
            }
        }
        // console.log(data);
    }

    function userPublicDataListener(data) {
        try {
            var o = data.val();
            o[RESPONSE.NUMBER] = parseInt(data.ref.parent.getKey());
            o[RESPONSE.STATUS] = data.ref.parent.parent.getKey();
            o["key"] = data.getKey();

            trackingListener.onMessage(o);
        } catch(e) {
            console.error(e.message);
        }
    }

    function userPrivateDataListener(data) {
        try{
            var o = data.val();
            var from = parseInt(o["from"]);
            delete o["from"];

            o[RESPONSE.NUMBER] = from;
            o[RESPONSE.STATUS] = data.ref.parent.parent.getKey();
            o["key"] = data.getKey();
            o[EVENTS.PRIVATE_MESSAGE] = true;

            trackingListener.onMessage(o);
            // data.ref.remove();

        } catch(e) {
            console.error(e.message);
        }
    }

    function usersDataNameListener(data) {
        try {
            var number = parseInt(data.ref.parent.getKey());
            var name = data.val();
            main.users.forUser(number, function(number, user, name){
                if(user.number != main.me.number && user.properties && name != user.properties.name) {
                    user.fire(EVENTS.CHANGE_NAME, name);
                }
            }, name);
        } catch(e) {
            console.error(e.message);
        }
    }

    function usersDataActiveListener(data) {
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

    function updateActive() {
        try {
            if(main.me && main.me.number != undefined) {
                ref.child(DATABASE.SECTION_USERS_DATA).child(main.me.number).child(DATABASE.USER_CHANGED).set(firebase.database.ServerValue.TIMESTAMP);
            }
        } catch(e) {
            console.error(e.message);
        }
    }

    return {
        start: start,
        stop:stop,
        title: "Group",
        menu: true,
        setLink:setLink,
        setTrackingListener:setTrackingListener,
        getTrackingUri:getTrackingUri,
        getStatus:getStatus,
        setStatus:setStatus,
        getToken:getToken,
        sendMessage:sendMessage,
        put:put,
        sendUpdate:sendUpdate,
        send:send,
    }
}