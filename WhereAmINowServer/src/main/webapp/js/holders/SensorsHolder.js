/**
 * Created 2/10/17.
 */
function SensorsHolder(main) {

    var type = "sensors";

    function start() {
        // console.log("SENSORSHOLDER",main);
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

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
    }
}