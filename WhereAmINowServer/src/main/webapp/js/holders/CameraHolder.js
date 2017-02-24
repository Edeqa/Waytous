/**
 * Created 2/9/17.
 */
function CameraHolder(main) {

    var type = "camera";

    EVENTS.CAMERA_UPDATE = "camera_update";
    EVENTS.CAMERA_UPDATED = "camera_updated";
    EVENTS.CAMERA_ZOOM_IN = "camera_zoom_in";
    EVENTS.CAMERA_ZOOM_OUT = "camera_zoom_out";
    EVENTS.CAMERA_ZOOM = "camera_zoom";
    EVENTS.CAMERA_NEXT_ORIENTATION = "camera_next_orientation";

    const CAMERA_ORIENTATION_NORTH = 0;
    const CAMERA_ORIENTATION_DIRECTION = 1;
    const CAMERA_ORIENTATION_PERSPECTIVE = 2;
    const CAMERA_ORIENTATION_STAY = 3;
    const CAMERA_ORIENTATION_USER = 4;
    const CAMERA_DEFAULT_ZOOM = 15.;
    const CAMERA_DEFAULT_TILT = 0.;
    const CAMERA_DEFAULT_BEARING = 0.;
    const CAMERA_ORIENTATION_LAST = 2;
    const CAMERA_ORIENTATION_PERSPECTIVE_NORTH = true;

    function start() {
        console.log("CameraHOLDER",main);
    }

    function onEvent(EVENT,object){
        if(!this || !this.views || !this.views.camera) return true;
        var camera = this.views.camera;
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                object.add(2,type+"_1","Fit to screen","fullscreen",function(){console.log("FITTOSCREEN")});
                break;
            case EVENTS.MARKER_CLICK:
                onEvent.call(this,EVENTS.CAMERA_NEXT_ORIENTATION);
                break;
            case EVENTS.CAMERA_NEXT_ORIENTATION:
                console.log("NEXTORIENTATION",this);
                if(camera.orientation > CAMERA_ORIENTATION_LAST) {
                    camera.orientation = camera.previousOrientation;
                } else if(camera.orientation == CAMERA_ORIENTATION_LAST){
                    camera.orientation = CAMERA_ORIENTATION_NORTH;
                } else {
                    camera.orientation++;
                }
                if(camera.orientation == CAMERA_ORIENTATION_DIRECTION && this.location.coords.heading == 0) {
                    camera.orientation++;
                }
                camera.orientationChanged = true;
                camera.previousOrientation = camera.orientation;
                onChangeLocation.call(this,this.location);
                break;
            case EVENTS.SELECT_USER:
                var center = u.latLng(this.location);
                if(center) {
                    WAIN.main.map.panTo(center);
                }
                break;
            default:
                break;
        }
        return true;
    }

    function onChangeLocation(location){
        if(!this || !this.views || !this.views.camera) return;
        var camera = this.views.camera;
        this.location = location;
        switch (camera.orientation){
            /*case CAMERA_ORIENTATION_NORTH:
//                    if(orientationChanged) {
//                    }
                position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                position.bearing(0);
                position.tilt(0);
                break;
            case CAMERA_ORIENTATION_DIRECTION:
//                    if(orientationChanged) {
//                    }
                position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                position.bearing(location.getBearing());
                position.tilt(0);
                break;
            case CAMERA_ORIENTATION_PERSPECTIVE:
                if(orientationChanged) {
                    position.tilt(60);
                }
                position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                position.bearing(location.getBearing());

                /!*DisplayMetrics metrics = new DisplayMetrics();
                 context.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                 Projection projection = map.getProjection();

                 Point cameraCenter = projection.toScreenLocation(Utils.latLng(location));

                 float tiltFactor = (90 - map.getCameraPosition().tilt) / 90;

                 System.out.println("METRICS:"+metrics);
                 System.out.println("VISIBLE:"+projection.getVisibleRegion());
                 System.out.println("POINT:"+cameraCenter);

                 cameraCenter.x -= metrics.widthPixels / 2;// - cameraCenter.x;
                 cameraCenter.y -= metrics.heightPixels *.2;// / 2 * tiltFactor;

                 System.out.println("POINT2:"+cameraCenter);

                 LatLng fixLatLng = projection.fromScreenLocation(cameraCenter);
                 position.target(fixLatLng);*!/

                break;
            case CAMERA_ORIENTATION_STAY:
                position.target(map.getCameraPosition().target);
                break;*/
        }

    }

    function createView(user){
        if(!user || !user.properties) return;

        var b = {
            bearing: CAMERA_DEFAULT_BEARING,
            zoom: CAMERA_DEFAULT_ZOOM,
            orientation: CAMERA_ORIENTATION_NORTH,
            previousOrientation: CAMERA_ORIENTATION_NORTH,
            perspectiveNorth: true,
            location:user.location,
            // latitude: user.getLocation().getLatitude();
            // longitude: myUser.getLocation().getLongitude();
        };
        return b;
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