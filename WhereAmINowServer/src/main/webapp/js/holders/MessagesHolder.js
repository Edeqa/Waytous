/**
 * Created 2/9/17.
 */
EVENTS.NEW_MESSAGE = "new_message";
EVENTS.SEND_MESSAGE = "send_message";
EVENTS.PRIVATE_MESSAGE = "private";
EVENTS.USER_MESSAGE = "user_message";
EVENTS.SHOW_MESSAGES = "show_messages";
EVENTS.WELCOME_MESSAGE = "welcome_message";

function MessagesHolder(main) {

    var type = "message";
    var chat;
    var messages;
    var reply;
    var replyTo;
    var replyInput;
    var replyButton;
    var lastReadTimestamp;
    var drawerItemChat;
    var incomingMessageSounds;
    var incomingMessageSound;
    var defaultIncomingMessageSound = "youve-been-informed.mp3";
    var sound;

    function start() {
        // console.log("MESSAGESHOLDER",main);

        chat = u.dialog({
            title: {
                label: u.lang.chat,
                filter: true
            },
            className: "chat",
            itemsClassName: "chat-messages",
            tabindex: 3,
            resizeable: true,
            items: [
            ],
            negative: {
                onclick: function(){
                    u.saveForGroup("message:chat");
                }
            },
            onopen: function() {
                lastReadTimestamp = new Date().getTime();
                u.saveForGroup("message:lastread", lastReadTimestamp);
            },
            footer: {
                type: HTML.DIV,
                className: "chat-reply"
            }
        });

//        messages = chat.items[0];
        reply = chat.footer;
        replyTo = u.create(HTML.INPUT, {type:HTML.HIDDEN, value:""}, reply);
        replyInput = u.create(HTML.INPUT, {className: "chat-reply-input", tabindex:5, onkeyup:function(e){
            if(e.keyCode == 13) {
                replyButton.click();
            }
        }, onclick: function(){
            this.focus();
        }}, reply);
        replyButton = u.create(HTML.BUTTON, {className: "chat-reply-button", innerHTML:"send", onclick:sendUserMessage}, reply);

        incomingMessageSound = u.load("messages:incoming") || defaultIncomingMessageSound;
        sound = u.create(HTML.AUDIO, {className:"hidden", preload:"", src:"/sounds/"+incomingMessageSound, last:0, playButLast:function(){
            var current = new Date().getTime();
            if(current - this.last > 10) {
                this.last = current;
                this.play();
            }
        }}, main.right);

    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                drawerItemChat = object.add(DRAWER.SECTION_COMMUNICATION, type+"_1", u.lang.chat, "chat", function(){
                    if(chat.classList.contains("hidden")) {
                        main.fire(EVENTS.SHOW_MESSAGES);
                    } else {
                        u.saveForGroup("message:chat");
                        chat.close();
                    }
                });
                drawerItemChat.hide();
                break;
            case EVENTS.TRACKING_ACTIVE:
                if(u.loadForGroup("message:chat")) chat.open();
                lastReadTimestamp = u.loadForGroup("message:lastread");

                drawerItemChat.show();
                break;
            case EVENTS.TRACKING_DISABLED:
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user.type == "user" && user != main.me) {
                    object.add(MENU.SECTION_COMMUNICATION, type + "_1", u.lang.private_message, "chat", function () {
                        chat.open();
                        replyTo.value = user.properties.number;
                        replyInput.focus();
                    });
                }
                break;
            case EVENTS.SHOW_MESSAGES:
                u.saveForGroup("message:chat", true);
                chat.open();
                chat.focus();
                replyInput.focus();
                main.users.forAllUsers(function(number,user){
                    user.fire(EVENTS.HIDE_BADGE);
                    drawerItemChat && drawerItemChat.hideBadge();
                });
                break;
            case EVENTS.USER_MESSAGE:
                var div = chat.addItem({
                    type:HTML.DIV,
                    className:"chat-message" + (object.private ? " chat-message-private" : ""),
                    order: object.timestamp,
                });
                u.create(HTML.DIV, {className:"chat-message-timestamp", innerHTML: new Date(object.timestamp).toLocaleString()}, div);

                var toUser = null;
                if(object.private) {
                    toUser = main.users.users[object.to] || main.me;
                }

                var divName = u.create(HTML.DIV, {
                    className:"chat-message-name",
                    style: {color: this.properties.color},
                    innerHTML:this.properties.getDisplayName() + (object.private ? " &rarr; " + toUser.properties.getDisplayName() : "") + ":"}, div);
                u.create(HTML.DIV, {className:"chat-message-body", innerHTML: object.body}, div);

                div.scrollIntoView();

                if(object.timestamp > lastReadTimestamp) {
                    sound.playButLast();

                    u.notification({
                        title: divName.innerHTML,
                        body: object.body,
                        icon: "/icons/android-chrome-256x256.png",
                        duration: 5000,
                        onclick: function(e){
                            main.fire(EVENTS.SHOW_MESSAGES);
                        }
                    });

                    if(chat.classList.contains("hidden")) {
                        this.fire(EVENTS.SHOW_BADGE, EVENTS.INCREASE_BADGE);
                        drawerItemChat && drawerItemChat.increaseBadge();
                    } else {
                        u.saveForGroup("message:lastread", object.timestamp);
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    function createView(user){
        return {
            user:user,
            messages:[],
        }
    }

    function sendUserMessage(){
        try {
            var text = replyInput.value;
            if(!text) return;
            replyInput.value = "";

            main.tracking.put(USER.MESSAGE, text);
            if(replyTo.value) {
                main.tracking.put(RESPONSE.PRIVATE, parseInt(replyTo.value));
                main.me.fire(EVENTS.USER_MESSAGE, {body: text, timestamp: new Date().getTime(), private: true, to: parseInt(replyTo.value)});
                replyTo.value = "";
            } else {
                main.me.fire(EVENTS.USER_MESSAGE, {body: text, timestamp: new Date().getTime()});
            }
            main.tracking.put(REQUEST.DELIVERY_CONFIRMATION, true);
            main.tracking.put(REQUEST.MESSAGE, text);
            main.tracking.send(REQUEST.MESSAGE);

        } catch(e) {
            console.error(e);
        }
    }

    function perform(json) {
        var number = json[USER.NUMBER];
        var text = json[USER.MESSAGE];
        var time = json[REQUEST.TIMESTAMP];
        var key = json["key"];
        var privateMessage = json[EVENTS.PRIVATE_MESSAGE] || false;

        main.users.forUser(number, function(number,user){
            user.fire(EVENTS.USER_MESSAGE, {body: text, timestamp: time, key: key, private: privateMessage});
        });
    }


    function options(){
        return {
            id: "general",
            title: u.lang.general,
            categories: [
                {
                    id: "general:main",
                    title: u.lang.main,
                    items: [
                        {
                            id:"messages:incoming",
                            type: HTML.SELECT,
                            label: "Incoming message",
                            default: u.load("messages:incoming") || defaultIncomingMessageSound,
                            onaccept: function(e, event) {
                                u.save("messages:incoming", this.value);
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
                                if(incomingMessageSounds) {
                                } else {
                                    u.getRemoteJSON({
                                        url: "/xhr/getSounds",
                                        onsuccess: function(json){
                                            incomingMessageSounds = {};
                                            u.clear(e);
                                            var selected = 0;
                                            for(var i in json.files) {
                                                var file = json.files[i];
                                                var name = u.toUpperCaseFirst(file.replace(/\..*$/,"").replace(/[\-_]/g," "));
                                                incomingMessageSounds[file] = name;
                                                u.create(HTML.OPTION, {value:file, innerHTML:name}, e);
                                                if((incomingMessageSound || defaultIncomingMessageSound) == file) selected = i;
                                            }
                                            e.selectedIndex = selected;
                                        },
                                        onerror: function(code,xhr){
                                            console.error(code,xhr)
                                        }
                                    });
                                }
                            },
                            values: {"": u.lang.loading.innerText}
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
        createView:createView,
        perform:perform,
        saveable:true,
        loadsaved:-1,
        options:options,
    }
}