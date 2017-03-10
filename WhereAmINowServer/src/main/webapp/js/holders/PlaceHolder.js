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
                var item = object.add(DRAWER.SECTION_NAVIGATION,type+"_1","Search","search",function(){console.log("PLACEHOLDERDRAWERCALLBACK")});
                item.classList.add("disabled");
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