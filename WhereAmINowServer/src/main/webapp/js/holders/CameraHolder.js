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

    var menuFitToScreen;
    var task;

    function start() {

    }

    function onEvent(EVENT,object){
        // if(!this || !this.views || !this.views.camera) return true;
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                menuFitToScreen = object.add(2,type+"_1","Fit to screen","fullscreen",function(){
                    main.users.forAllUsers(function (number, user) {
                        user.fire(EVENTS.SELECT_USER);
                    });
                });
                menuFitToScreen.classList.add("disabled");
                break;
            case EVENTS.TRACKING_ACTIVE:
                menuFitToScreen.classList.remove("disabled");
                break;
            case EVENTS.TRACKING_DISABLED:
                menuFitToScreen.classList.add("disabled");
                break;
            case EVENTS.MARKER_CLICK:
                onEvent.call(this,EVENTS.CAMERA_NEXT_ORIENTATION);
                break;
            case EVENTS.CAMERA_NEXT_ORIENTATION:
                console.log("NEXTORIENTATION",this);
                var camera = this.views.camera;
                if(this.views.camera.orientation > CAMERA_ORIENTATION_LAST) {
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
                onChangeLocation.call(this, this.location);
                menuFitToScreen.classList.remove("disabled");
                break;
            case EVENTS.CAMERA_ZOOM:
                if(main.users.getCountSelected()==1) {
                    main.users.forAllUsers(function (number, user) {
                        if(user.properties.selected) {
                            if(!object) {
                                var z = user.views.camera.zoom;
                                var zooms = [CAMERA_DEFAULT_ZOOM, CAMERA_DEFAULT_ZOOM + 2, CAMERA_DEFAULT_ZOOM + 4, CAMERA_DEFAULT_ZOOM - 2, CAMERA_DEFAULT_ZOOM - 4, CAMERA_DEFAULT_ZOOM];
                                var index = zooms.indexOf(z);
                                object = zooms[index+1];
                            }
                            user.views.camera.zoom = object;
                        }
                    });
                }
                if(main.map.getZoom() != object) {
                    main.map.setZoom(Math.round(object));
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
        update.call();
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

    function update() {
        if(main.users.getCountSelected() > 1) {
            var finalBounds = new google.maps.LatLngBounds();
            for(var i in main.users.users) {
                var user = main.users.users[i];
                if(user.properties && user.properties.selected && user.location && user.location.coords) {
                    finalBounds.extend(u.latLng(user.location));
                }
            }
            main.map.fitBounds(finalBounds);
/*            var startBounds = main.map.getBounds();
            var startNE = startBounds.getNorthEast();
            var startSW = startBounds.getSouthWest();
            var finalNE = finalBounds.getNorthEast();
            var finalSW = finalBounds.getSouthWest();
            clearInterval(task);
            task = u.smoothInterpolated(1000, function(time,value) {

                var currentNE = new google.maps.LatLng(
                    startNE.lat()*(1-time) + finalNE.lat()*time,
                    startNE.lng()*(1-time) + finalNE.lng()*time
                );
                var currentSW = new google.maps.LatLng(
                    startSW.lat()*(1-time) + finalSW.lat()*time,
                    startSW.lng()*(1-time) + finalSW.lng()*time
                );
                var currentBounds = new google.maps.LatLngBounds(currentNE, currentSW);

                main.map.fitBounds(currentBounds);
            }, function(){
                main.map.fitBounds(finalBounds);
            });*/
        } else {
            main.users.forAllUsers(function(number,user){
                if(user.properties && user.properties.selected) {
                    var finalCenter = u.latLng(user.location);
                    if (finalCenter) {
                        var startCenter = main.map.getCenter();


                        var startZoom = main.map.getZoom();
                        var finalZoom = user.views.camera.zoom || CAMERA_DEFAULT_ZOOM;

                        if(startCenter.lat() == finalCenter.lat()
                            && startCenter.lng() == finalCenter.lng()
                            && startZoom == finalZoom) return;

                        clearInterval(task);
                        task = u.smoothInterpolated(1000, function(time,value) {

                            var currentCenter = new google.maps.LatLng(
                                startCenter.lat()*(1-time) + finalCenter.lat()*time,
                                startCenter.lng()*(1-time) + finalCenter.lng()*time
                            );
                            var currentZoom = startZoom*(1-time) + finalZoom*time;

                            main.map.setZoom(currentZoom);
                            main.map.setCenter(currentCenter);

                        }, function(){
                            main.map.setZoom(finalZoom);
                            main.map.setCenter(finalCenter);
                        });
                    }

                    // main.map.setZoom(finalZoom);
                    // main.map.panTo(finalCenter);

                }
            });
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