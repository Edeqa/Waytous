/**
 * Created 2/10/17.
 */
function SampleHolder(main) {

    var type = "sample";
    var user;

    function start() {
        console.log("SAMPLEHOLDER",this);
    }

    function onEvent(EVENT,object){
        // console.log("SAMPLEEVENT",EVENT,object)
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                object.add(0,type+"_1","Sample","ac_unit",function(){console.log("SAMPLEEVENTDRAWERCALLBACK",EVENT)});
                break;
            default:
                break;
        }
        return true;
    }

    function createView(myUser){
        user = myUser;
        // console.log("SAMPLECREATEVIEW",user);
    }

    function onChangeLocation(location) {
        // console.log("SAMPLEONCHANGELOCATION",this,location);

    }

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
        dependsOnUser:true,
        createView:createView,
        onChangeLocation:onChangeLocation,
    }
}