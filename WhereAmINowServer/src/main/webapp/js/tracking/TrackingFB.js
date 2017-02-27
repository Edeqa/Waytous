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
	var webSocketListener;

    function start() {
        status = EVENTS.TRACKING_DISABLED;
        var uri;
        if(this.link) {
            uri = new URL(this.link);
        } else {
            newTracking = true;
        }
        var path = uri.pathname.replace("/group/","/track/");
        serverUri = "wss://" + uri.hostname + ":8001" + path;
//        serverUri = "ws://" + uri.hostname + ":8081" + path;

        if(newTracking) {
            setStatus(EVENTS.TRACKING_CONNECTING);
            trackingListener.onCreating();
        } else {
            setStatus(EVENTS.TRACKING_RECONNECTING);
            trackingListener.onJoining()
        }
        webSocketListener = webSocketListener(serverUri);

    }

    function webSocketListener(link) {

        var onopen =  function(event) {
            opened = true;
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

        var onclose = function(event) {
//            console.log("CLOSE",opened,event.code,event.reason,event.wasClean);
            if(!opened) {
                console.error("Error processing websocket, try to use XHR on",link," (error ",event.code,event.reason?" "+event.reason+")":")");
                xhrMode(link);
            }
        };

        var onerror = function(event) {
            console.log("ONERROR-RECONNECT-SHOULDBEDONE",event)
            if(status == EVENTS.TRACKING_DISABLED) return;
        };

        var xhrMode = function(link) {
            console.log("XHRMODE", link);
            var xhr = new XMLHttpRequest();

            var uri = new URL(link);
            link = "https://" + uri.hostname + ":8000/join" + uri.pathname;

            var onreadystatechange = function() { // (3)
            console.log("XHR",xhr.readyState,xhr);
                switch(xhr.readyState){
                    case 4:
                        onmessage({data:xhr.response});
                        break;
                }
            }
            xhr.onreadystatechange = onreadystatechange;

            send = function(){
                put(REQUEST.TIMESTAMP, new Date().getTime());
                if(xhr.readyState != 1) {
                    xhr = new XMLHttpRequest();
                    xhr.onreadystatechange = onreadystatechange;
                    xhr.open('POST', link, false);
                }

                xhr.send(JSON.stringify(json));
                json = {};
            }

            xhr.open('POST', link, true);
            onopen();

        }

        function onCheckControl(e) {
            var json = JSON.parse(e);
        console.log("CHECKCONTROL",json);

//            var xhr = new XMLHttpRequest();
//
//            var uri = new URL(link);
//            link = "https://" + uri.hostname + ":8100/join" + uri.pathname;
//
//            xhr.onreadystatechange = function() { // (3)
//                switch(xhr.readyState){
//                    case 4:
//                        onCheckControl(xhr.response);
//                        break;
//                }
//            }
//
//            send = function(){
//                put(REQUEST.TIMESTAMP, new Date().getTime());
//                xhr.send(JSON.stringify(json));
//                json = {};
//            }
//
//            xhr.open('POST', link, true);
//            onopen();

        }



        var a = {};
        try {
            a = new WebSocket(link);
        } catch(e){
            console.error(e);
            xhrMode(link);
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

    function send() {
        // console.log("SEND",json);
        put(REQUEST.TIMESTAMP, new Date().getTime());
        webSocketListener.send(JSON.stringify(json));
        json = {};
    }

    function sendMessage(type, json) {
        

    }

    function setLink(link) {
        this.link = link;
    }

    function setTrackingListener(callback) {
        trackingListener = callback;
    }

    function setToken(id){
        this.token = id;
    }

    function getToken(){
        return this.token;
    }

    function setStatus(status){
        this.status = status;
    }

    function getTrackingUri(){
        return "http://" + serverUri.host + ":" + 8080 + "/track/" + token;
    }

    function registerChildListener(ref, listener, limit) {
        if(limit >= 0){
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
                user.user = true;

                //registers
                registerValueListener(ref.child(DATABASE.SECTION_USERS_DATA).child(user.number).child("name"), usersDataNameListener);
                registerValueListener(ref.child(DATABASE.SECTION_USERS_DATA).child(user.number).child("active"), usersDataActiveListener);

                usersDataNameListener(data.child("name"));
                usersDataActiveListener(data.child("active"));

                for(var i in main.holders) {
                    if(main.holders[i] && main.holders[i].saveable) {
                        registerChildListener(ref.child(DATABASE.SECTION_PUBLIC).child(i).child(user.number), userPublicDataListener, 1);
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
debugger;
            o[RESPONSE.NUMBER] = from;
            o[RESPONSE.STATUS] = data.ref.parent.parent.getKey();
            o["key"] = data.getKey();
            // trackingListener.onMessage(o);
            // data.getRef().remove();

        } catch(e) {
            console.error(e.message);
        }
        console.log(data);
    }

    function usersDataNameListener(data) {
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

    return {
        start: start,
        title: "Group",
        menu: true,
        setLink:setLink,
        setTrackingListener:setTrackingListener,
        getTrackingUri:getTrackingUri,
        sendMessage:sendMessage,
    }
}