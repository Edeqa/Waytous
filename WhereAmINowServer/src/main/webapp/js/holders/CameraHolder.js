/**
 * Created 2/9/17.
 */
function CameraHolder(main) {

    var type = "camera";

    EVENTS.UPDATE_CAMERA = "update_camera";
    EVENTS.CAMERA_UPDATED = "camera_updated";
    EVENTS.CAMERA_ZOOM_IN = "camera_zoom_in";
    EVENTS.CAMERA_ZOOM_OUT = "camera_zoom_out";
    EVENTS.CAMERA_ZOOM = "camera_zoom";

    function start() {
        // console.log("CameraHOLDER",main);
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                object.add(2,type+"_1","Fit to screen","fullscreen",function(){console.log("FITTOSCREEN")});
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