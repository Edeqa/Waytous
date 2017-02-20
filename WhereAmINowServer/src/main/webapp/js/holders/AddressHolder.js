/**
 * Created 2/10/17.
 */
function AddressHolder(main) {

    var type = "address";

    function start() {
        // console.log("ADDRESSHOLDER",this);
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            default:
                break;
        }
        return true;
    }

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        dependsOnUser:true,
        onEvent:onEvent,
    }
}