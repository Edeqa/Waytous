/**
 * Created 2/9/17.
 */
function MessagesHolder(main) {

    var type = "message";

    function start() {
        // console.log("MESSAGESHOLDER",main);
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                object.add(1,type+"_1","Chat","chat",function(){console.log("CALLBACKFORMESSAGES")});
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                object.add(8,type+"_1","Private message","chat",function(){console.log("PRIVATEMESSAGETO",user)});
                object.add(8,type+"_2","Private message","chat",function(){console.log("PRIVATEMESSAGETO",user)});
                object.add(8,type+"_3","Private message","chat",function(){console.log("PRIVATEMESSAGETO",user)});
                object.add(8,type+"_4","Private message","chat",function(){console.log("PRIVATEMESSAGETO",user)});
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

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
        dependsOnUser:true,
        createView:createView,
    }
}