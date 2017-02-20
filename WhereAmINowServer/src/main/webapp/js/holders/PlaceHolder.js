/**
 * Created 2/10/17.
 */
function PlaceHolder(main) {

    var type = "place";

    function start() {
        // console.log("PLACEHOLDER",main);
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                object.add(1,type+"_1","Search","search",function(){console.log("PLACEHOLDERDRAWERCALLBACK")});
                break;
            default:
                break;
        }
        return true;
    }

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
    }
}