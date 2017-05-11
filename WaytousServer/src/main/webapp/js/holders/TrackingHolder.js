/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/9/17.
 */
EVENTS.SHARE_LINK = "share_link";

function TrackingHolder(main) {

    var type ="tracking";

    var TRACKING_URI = "uri";
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
    var sound;
    var sounds;
    var joinSound;
    var defaultSound = "oringz-w427.mp3";

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
            className: "progress-dialog",
            items: [
                { type: HTML.DIV, className: "progress-dialog-circle" },
                { type: HTML.DIV, className: "progress-dialog-title" },
            ]
        }, main.right);

        joinSound = u.load("tracking:sound_on_join") || defaultSound;
        sound = u.create(HTML.AUDIO, {className:"hidden", preload:"", src:"/sounds/"+joinSound, last:0, playButLast:function(){
            var current = new Date().getTime();
            if(current - this.last > 10) {
                this.last = current;
                this.play();
            }
        }}, main.right);
        progressTitle = progress.items[1];
        noSleep = new NoSleep();
        wakeLockEnabled = false;

    }

    function perform(json){
        var loc = utils.jsonToLocation(json);
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
                drawerItemNew = object.add(DRAWER.SECTION_PRIMARY,EVENTS.TRACKING_NEW, u.lang.create_group, drawerItemNewIcon,function(){
                    main.fire(EVENTS.TRACKING_NEW);
                });
                drawerItemNew.hide();
                drawerItemExit = object.add(DRAWER.SECTION_LAST,EVENTS.TRACKING_STOP, u.lang.exit_group,"clear",function(){
                    main.fire(EVENTS.TRACKING_STOP);
                });
                drawerItemExit.hide();
                drawerItemShare = object.add(DRAWER.SECTION_COMMUNICATION,EVENTS.SHARE_LINK, u.lang.share_group, "share",function(e){
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
//                var groupOld = u.loadForContext("group");
                if(group) {
                    u.context = group;
                    var self = this;
                    setTimeout(function(){
                        u.require("/js/helpers/TrackingFB.js").then(startTracking.bind(self));
                    }, 0);
                }
                break;
            case EVENTS.TRACKING_NEW:
                var self = this;
                setTimeout(function(){
                    u.require("/js/helpers/TrackingFB.js").then(startTracking.bind(self));
                }, 0);
                break;
            case EVENTS.TRACKING_ACTIVE:
                u.context = main.tracking.getToken();
                document.title = u.lang.s_s.format(main.appName, main.tracking.getToken()).innerHTML;
                if(main.tracking.getStatus() == EVENTS.TRACKING_ACTIVE && drawerItemShare) {
                    drawerItemShare.enable();
                }
                u.notification({
                    title: u.lang.waytous_online.innerText,
                    body: u.lang.you_have_joined_to_the_group_s.format(main.tracking.getToken()).innerText,
                    icon: "/icons/favicon-256x256.png",
                    duration: 10000,
                    onclick: function(e){
                        console.log(this,e)
                    }
                });

                if (!wakeLockEnabled) {
                    noSleep.enable(); // keep the screen on!
                    wakeLockEnabled = true;
                }
                break;
            case EVENTS.TRACKING_CONNECTING:
//                window.onbeforeunload = beforeunload;

                document.title = u.lang.connecting_s.format(main.appName).innerHTML;
                drawerItemNew.hide();
                drawerItemShare.show();
                drawerItemShare.enable();
                drawerItemExit.show();
                if (!wakeLockEnabled) {
                    noSleep.enable(); // keep the screen on!
                    wakeLockEnabled = true;
                }
                break;
            case EVENTS.TRACKING_RECONNECTING:
//                window.onbeforeunload = beforeunload;

                document.title = u.lang.connecting_s.format(main.appName).innerHTML;
                drawerItemNew.hide();
                drawerItemShare.show();
                drawerItemShare.enable();
                drawerItemExit.show();
                if (!wakeLockEnabled) {
                    noSleep.enable(); // keep the screen on!
                    wakeLockEnabled = true;
                }
                break;
            case EVENTS.TRACKING_DISABLED:
                window.onbeforeunload = null;

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
                    u.saveForContext("group");
                }
                break;
            case EVENTS.SHARE_LINK:
                if(shareDialog) shareDialog.close();
                shareDialog = shareDialog || u.dialog({
                    items: [
                        {type:HTML.DIV, innerHTML: u.lang.tracking_share_link_dialog },
                        {type:HTML.INPUT, className: "dialog-item-input-link", value: main.tracking.getTrackingUri() }
                    ],
                    positive: {
                        label: u.lang.mail,
                        onclick: function() {
                            var popup = window.open("mailto:?subject=Way%20to%20us&body="+main.tracking.getTrackingUri(),"_blank");
                            utils.popupBlockerChecker.check(popup, function() {
                                shareBlockedDialog = shareBlockedDialog || u.dialog({
                                    items: [
                                        {type:HTML.DIV, innerHTML: u.lang.popup_blocked_dialog_1 },
                                        {type:HTML.DIV, enclosed:true, innerHTML: u.lang.popup_blocked_dialog_2 },
                                        {type:HTML.DIV, innerHTML: u.lang.popup_blocked_dialog_3 },
                                        {type:HTML.DIV, innerHTML: main.tracking.getTrackingUri()}
                                    ],
                                    positive: {
                                        label: u.lang.close
                                    }
                                }, main.right);
                                shareBlockedDialog.open();
                            });
                        }
                    },
                    neutral: {
                        label: u.lang.copy,
                        dismiss: false,
                        onclick: function(items) {
                            if(u.copyToClipboard(items[1])) {
                                main.toast.show(u.lang.link_was_copied_into_clipboard, 3000);
                            }
                            shareDialog.close();
                        }
                    },
                    negative: {
                        label: u.lang.cancel
                    },
                    timeout: 20000
                }, main.right);
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

        var a = window.location.pathname.split("/");
        if(a[2]) {
            a[2] = a[2].toUpperCase();

//            var groupOld = u.loadForContext("group");
            window.history.pushState({}, null, a.join("/"));
//            window.history.pushState({}, null, "/track/" + token);

            main.fire(EVENTS.TRACKING_JOIN, window.location.href);
            this.tracking.setLink(window.location.href);
            u.saveForContext("group",a[2]);
        } else {
            progressTitle.innerHTML = u.lang.creating_group;
            console.log("NEW")
        }
        this.tracking.setTrackingListener(onTrackingListener);
        this.tracking.start();

    }

    var onTrackingListener = {
        onCreating: function(){
            // console.log("ONCREATING");
            u.lang.updateNode(progressTitle, u.lang.connecting);
            //progressTitle.innerHTML = u.lang.connecting;
            progress.open();

            u.saveForContext(TRACKING_URI, null);
            main.fire(EVENTS.TRACKING_CONNECTING);
        },
        onJoining: function(){
            // console.log("ONJOINING");
            u.lang.updateNode(progressTitle, u.lang.joining_group);
//            progressTitle.innerHTML = u.lang.joining_group;
            progress.open();
            main.fire(EVENTS.TRACKING_RECONNECTING, u.lang.joining_group);
        },
        onReconnecting: function(){
            // console.log("ONRECONNECTING");
            u.lang.updateNode(progressTitle, u.lang.reconnecting);
//            progressTitle.innerHTML = u.lang.reconnecting;
            progress.open();
            main.fire(EVENTS.TRACKING_RECONNECTING, u.lang.reconnecting);
        },
        onClose: function(){
            console.log("ONCLOSE");
        },
        onAccept: function(o){
            // console.log("ONACCEPT",o);
            //FIXME
//            u.saveForContext(TRACKING_URI, this.tracking.getTrackingUri());
            try {
                if(main.tracking.getStatus() != EVENTS.TRACKING_ACTIVE) {
                    main.tracking.setStatus(EVENTS.TRACKING_ACTIVE);
                    main.fire(EVENTS.TRACKING_ACTIVE);
                }
                if (o[RESPONSE.TOKEN]) {
                    var token = o[RESPONSE.TOKEN];
                    main.fire(EVENTS.TOKEN_CREATED, token);
                    u.saveForContext("group", token);
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
            u.saveForContext(TRACKING_URI);
            main.fire(EVENTS.TRACKING_DISABLED);
            main.fire(EVENTS.TRACKING_ERROR, reason);

            progress.close();
            u.saveForContext("group");

             u.dialog({
                className: "alert-dialog",
                modal: true,
                items: [
                    { type: HTML.DIV, innerHTML: reason || u.lang.sorry_you_have_requested_the_expired_group },
                    { type: HTML.DIV, enclosed:true, body: u.lang.expired_explanation },
                ],
                positive: {
                    label: u.lang.ok,
                },
                onclose: function() {
                    window.location = "/track/";
                }
            }, main.right).open();

//            window.history.pushState({}, null, "/track/" );

        },
        onStop: function(){
            console.log("ONSTOP");
            u.saveForContext(TRACKING_URI);
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

                            if(user.properties && !user.properties.active) {
                                if(!user.changed || new Date().getTime() - 15 * 60 * 1000 > user.changed) {
                                    main.toast.show(u.lang.user_s_has_joined.format(user.properties.getDisplayName()), 10000);
                                    sound.playButLast();
                                }
                            }

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

    function beforeunload(evt) {
        return u.lang.beforeunload;
    }

    function help(){
        return {
            title: u.lang.tracking_help_title,
            1: {
                ignore: true,
                title: u.lang.tracking_help_title_1,
                body: u.lang.tracking_help_body_1
            }
        }
    }

    function options(){
        return {
            id: "general",
            title: u.lang.general,
            categories: [
                {
                    id: "general:notifications",
                    title: u.lang.notifications,
                    items: [
                        {
                            id:"tracking:sound_on_join",
                            type: HTML.SELECT,
                            label: u.lang.sound_on_join,
                            default: u.load("tracking:sound_on_join") || defaultSound,
                            onaccept: function(e, event) {
                                u.save("tracking:sound_on_join", this.value);
                                sound.src = "/sounds/" + this.value;
                            },
                            onchange: function(e, event) {
                                var sample = u.create(HTML.AUDIO, {className:"hidden", preload:true, src:"/sounds/"+this.value}, main.right);
                                sample.addEventListener("load", function() {
                                    sample.play();
                                }, true);
                                sample.play();
                            },
                            onshow: function(e) {
                                if(sounds) {
                                } else {
                                    u.getJSON("/xhr/v1/getSounds").then(function(json){
                                        sounds = {};
                                        u.clear(e);
                                        var selected = 0;
                                        for(var i in json.files) {
                                            var file = json.files[i];
                                            var name = (file.replace(/\..*$/,"").replace(/[\-_]/g," ")).toUpperCaseFirst();
                                            sounds[file] = name;
                                            u.create(HTML.OPTION, {value:file, innerHTML:name}, e);
                                            if((joinSound || defaultSound) == file) selected = i;
                                        }
                                        e.selectedIndex = selected;
                                    });
                                }
                            },
                            values: {"":u.lang.loading.innerText}
                        }
                    ]
                }
            ]
        }
    }


    return {
        type:type,
        start:start,
        onEvent:onEvent,
        perform:perform,
        saveable:true,
        help:help,
        options:options,
    }
}