/**
 * Created 2/9/17.
 */
function MessagesHolder(main) {

    var type = "message";
    var chat;
    var messages;
    var reply;
    var replyButton;


    EVENTS.NEW_MESSAGE = "new_message";
    EVENTS.SEND_MESSAGE = "send_message";
    EVENTS.PRIVATE_MESSAGE = "private";
    EVENTS.USER_MESSAGE = "user_message";
    EVENTS.WELCOME_MESSAGE = "welcome_message";

    function start() {
        // console.log("MESSAGESHOLDER",main);

        chat = u.create(HTML.DIV, {className: "modal chat shadow hidden", tabindex: 3}, main.right);
        u.create(HTML.BUTTON, {className:"material-icons dialog-button-close", innerHTML:"clear", onclick:function(){
            chat.classList.add("hidden");
        }}, chat);
        messages = u.create(HTML.DIV, {className: "chat-messages"}, chat);
        reply = u.create(HTML.DIV, {className: "chat-reply"}, chat);
        u.create(HTML.INPUT, {className: "chat-reply-input", onkeyup:function(e){
            if(e.keyCode == 13) {
                replyButton.click();
            }
        }}, reply);
        replyButton = u.create(HTML.BUTTON, {className: "material-icons chat-reply-button", innerHTML:"send", onclick:sendUserMessage}, reply);

        if(u.load("message:chat")) chat.classList.remove("hidden");

    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                var item = object.add(1,type+"_1","Chat","chat",function(){
                    if(chat.classList.contains("hidden")) {
                        u.save("message:chat", true);
                        chat.classList.remove("hidden");
                    } else {
                        u.save("message:chat");
                        chat.classList.add("hidden");
                    }
                });
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user != main.me) {
                    object.add(8, type + "_1", "Private message", "chat", function () {
                        console.log("PRIVATEMESSAGETO", user)
                    });
                }
                // object.add(8,type+"_2","Private message","chat",function(){console.log("PRIVATEMESSAGETO",user)});
                // object.add(8,type+"_3","Private message","chat",function(){console.log("PRIVATEMESSAGETO",user)});
                // object.add(8,type+"_4","Private message","chat",function(){console.log("PRIVATEMESSAGETO",user)});
                break;
            case EVENTS.USER_MESSAGE:
                var div = u.create(HTML.DIV, {className:"chat-message"}, messages);
                u.create(HTML.DIV, {className:"chat-message-timestamp", innerHTML: new Date(object.timestamp).toLocaleString()}, div);
                u.create(HTML.DIV, {
                    className:"chat-message-from",
                    style: {color: this.properties.color},
                    innerHTML:this.properties.getDisplayName() + ":"}, div);
                u.create(HTML.DIV, {className:"chat-message-body", innerHTML: object.body}, div);

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
            var text = reply.children[0].value;
            reply.children[0].value = "";

            main.tracking.put(USER.MESSAGE, text);
            main.tracking.put(REQUEST.DELIVERY_CONFIRMATION, true);
            main.tracking.put(REQUEST.MESSAGE, text);

            main.tracking.send(REQUEST.MESSAGE);

        } catch(e) {
            console.error(e);
        }
    }

    function perform(json) {
        // var loc = u.jsonToLocation(json);
        console.log(json);
        var number = json[USER.NUMBER];
        var text = json[USER.MESSAGE];
        var time = json[REQUEST.TIMESTAMP];
        var key = json["key"];
        main.users.forUser(number, function(number,user){
            user.fire(EVENTS.USER_MESSAGE, {body: text, timestamp: time, key: key});
        });
    }

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
        dependsOnUser:true,
        createView:createView,
        perform:perform,
        saveable:true,
    }
}