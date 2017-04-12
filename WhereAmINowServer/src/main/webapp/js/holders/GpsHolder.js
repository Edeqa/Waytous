/**
 * Created 2/16/17.
 */
function GpsHolder(main) {

    var type = "gps";
    var geoTrackFilter = new GeoTrackFilter();
    var locationRequiredDialog;
    var drawerEnableGeoposition;
    var initialized;
    var icon;

    function start() {

//        u.save("gps:asked");
//        u.save("gps:allowed");


    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                drawerEnableGeoposition = object.add(DRAWER.SECTION_PRIMARY,type+"_1",u.lang.enable_geolocation,"gps_fixed",function(){
                    u.save("gps:asked");
                    main.fire(EVENTS.MAP_READY);
                });
                if(u.load("gps:allowed")) {
                    drawerEnableGeoposition.hide();
                }
                break;
            case EVENTS.TRACKING_ACTIVE:
                if(main.me.location) {
                    var message = u.locationToJson(main.me.location);
                    main.tracking.sendMessage(REQUEST.TRACKING, message);
                }
                //
                // navigator.geolocation.getCurrentPosition(function(location){
                //     locationUpdateListener(location);
                // });
                break;
            case EVENTS.MAP_READY:
                var last = u.load("gps:last");
                if(last && main.me && !main.me.location && last.coords && last.coords.latitude && last.coords.longitude) {
                console.log("LASTLOC",last);
                    main.me.addLocation(last);
                }

                if(u.load("gps:allowed") && u.load("gps:asked")) {
                    startPositioning();
                } else if(!u.load("gps:asked")) {
                    locationRequiredDialog = locationRequiredDialog || u.dialog({
                        className: "gps-required",
                        items: [
                            { type: HTML.DIV, innerHTML: u.lang.gps_location_required_1 },
                            { type: HTML.DIV, innerHTML: u.lang.gps_location_required_2 },
                            { type: HTML.DIV, enclosed:true, label: u.lang.gps_location_required_3, body: u.lang.gps_location_required_4 },
                        ],
                        positive: {
                            label: u.lang.ok_go_ahead,
                            onclick: function() {
                                u.save("gps:asked", true);
                                startPositioning();
                                if(!initialized) main.fire(EVENTS.MAP_READY);
                            }
                        },
                        negative: {
                            label: u.lang.maybe_later,
                            onclick: function() {
                                u.save("gps:asked", true);
                                if(!initialized) main.fire(EVENTS.MAP_READY);
                            }
                        },
                        help: function() {
                            locationRequiredDialog.close();
                            main.fire(EVENTS.SHOW_HELP, {module:main.holders.gps, article:1})
                        }
                    });
                    locationRequiredDialog.open();
                    return false;
                } /*else if(!u.load("gps:allowed")) {
                    return false;
                }*/
                initialized = true;
                break;
            default:
                break;
        }
        return true;
    }

    function startPositioning() {
        navigator.geolocation.getCurrentPosition(function(location){
            drawerEnableGeoposition.hide();
            icon && icon.hide();
            u.save("gps:asked", true);
            u.save("gps:allowed", true);
//            main.me.location = null;
            locationUpdateListener(location, true);
            navigator.geolocation.watchPosition(locationUpdateListener, function(error){
                console.error(error);
                alternativeGeolocation();
            }, {
                enableHighAccuracy: true,
                maximumAge: 10000,
                timeout: 30000
            });
        },function(error){
            var message;
            switch(error.code) {
                case error.PERMISSION_DENIED:
                    message = u.lang.you_have_denied_geolocation;
                    u.save("gps:asked");
                    u.save("gps:allowed");
                    break;
                case error.PERMISSION_DENIED_TIMEOUT:
                    message = u.lang.user_took_too_long_to_grant_deny_geolocation_permission;
                    break;
                case error.POSITION_UNAVAILABLE:
                    message = u.lang.geolocation_is_unavailable;
                    break;
                case error.TIMEOUT:
                    message = u.lang.request_to_geolocation_is_timed_out;
                    break;
                default:
                    message = u.lang.unknown_error_occurred_while_requesting_geolocation;
                    break;
            }

            drawerEnableGeoposition.show();
            alternativeGeolocation();

            var alert = u.dialog({
                className: "alert-dialog",
                items: [
                    { type: HTML.DIV, label: u.lang.please_resolve_this_problem_and_try_again.format(message) },
                ],
                positive: {
                    label: u.lang.ok,
                    onclick: function(){
                        icon.classList.remove("hidden");
                        alert.close();
                    }
                },
                negative: {
                    onclick: function(){
                        icon.classList.remove("hidden");
                        alert.close();
                    }
                },
                help: function() {
                    main.fire(EVENTS.SHOW_HELP, {module: main.holders.gps, article: 1});
                 }
            }).open();

            icon = u.create(HTML.BUTTON, {className:"alert-icon hidden", type: HTML.BUTTON, innerHTML:"warning", onclick: function(){
                icon.classList.add("hidden");
                alert.open();
            }}, main.right);
        }, {
            enableHighAccuracy: true,
            maximumAge: 60000,
            timeout: 30000
        });
    }


    function locationUpdateListener(position, force) {

        if(!position || !position.coords) return;
        // position = geoTrackFilter.normalizeLocation(position);
        var last = force ? null : (main.me && main.me.location);
        if(last
            && last.coords
            && last.coords.latitude == position.coords.latitude
            && last.coords.longitude == position.coords.longitude) {
                return;
        }
        if(position.coords.accuracy
            && last
            && last.coords
            && last.coords.accuracy
            && position.coords.accuracy > last.coords.accuracy
            && google.maps.geometry.spherical.computeDistanceBetween(u.latLng(last), u.latLng(position)) < position.coords.accuracy) {
                return;
        }
console.log("POSITION",position);
        u.save("gps:last",u.cloneAsObject(position));
        var message = u.locationToJson(position);
        if(main.tracking && main.tracking.getStatus() == EVENTS.TRACKING_ACTIVE) main.tracking.sendMessage(REQUEST.TRACKING, message);
        main.me.addLocation(position);
    }

    function alternativeGeolocation() {
return;
        u.getRemoteJSON({
            url: "https://ipinfo.io/json",
            onsuccess: function(json) {
                console.log("Alternative geolocation applied",json);
                var latlng = json.loc.split(",");
                var position = {
                    coords: {
                        provider: "ipinfo.io",
                        latitude: latlng[0],
                        longitude: latlng[1],
                        accuracy: 10000
                    },
                    timestamp: new Date().getTime()
                }
                locationUpdateListener(position);
            },
            onerror: function(xhr){console.error(xhr)}
        });

    }

    function GeoTrackFilter() {
        return {
            current:0,
            earthRadius: 6371009,
            lastTimeStep: null,
            kalmanFilter: null,

            normalizeLocation: function(position) {
                console.log(this.current,position);
                this.current ++;

                filter.update(position.coords.latitude, position.coords.longitude, position.timestamp);
                var latLng = filter.getLatLng();
                position.coords.latitude = latLng[0];
                position.coords.longitude = latLng[1];
                position.coords.heading = filter.getBearing();
                position.coords.speed = filter.getSpeed(position.coords.altitude)

                return position;
            }
        }
    }


    function help(){
        return {
            title: u.lang.gps_help_title,
            1: {
                title: u.lang.gps_help_1_title,
                body: u.lang.gps_help_1_body
            },
            2: {
                title: u.lang.gps_help_2_title,
                body: u.lang.gps_help_2_body
            },
            3: {
                title: u.lang.gps_help_3_title,
                body: u.lang.gps_help_3_body
            },
            4: {
                title: u.lang.gps_help_4_title,
                body: u.lang.gps_help_4_body
            },
            5: {
                title: u.lang.gps_help_5_title,
                body: u.lang.gps_help_5_body
            },
            6: {
                title: u.lang.gps_help_6_title,
                body: u.lang.gps_help_6_body
            },
            7: {
                title: u.lang.gps_help_7_title,
                body: u.lang.gps_help_7_body
            },
            8: {
                title: u.lang.gps_help_8_title,
                body: u.lang.gps_help_8_body
            },
            9: {
                title: u.lang.gps_help_9_title,
                body: u.lang.gps_help_9_body
            },
            10: {
                title: u.lang.gps_help_10_title,
                body: u.lang.gps_help_10_body
            }
        }
    }


    return {
        type:type,
        start:start,
        onEvent:onEvent,
        help:help,
        //resources:resources,
    }
}