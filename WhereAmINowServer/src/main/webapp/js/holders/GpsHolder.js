/**
 * Created 2/16/17.
 */
function GpsHolder(main) {

    var type = "gps";
    var geoTrackFilter = new GeoTrackFilter();
    var locationRequiredDialog;
    var drawerEnableGeoposition;
    var initialized;
    var alphaDialog;

    function start() {

        /*var latLong;
        $.getJSON("https://ipinfo.io", function(ipinfo){
            console.log("Found location ["+ipinfo.loc+"] by ipinfo.io");
            latLong = ipinfo.loc.split(",");
        });*/

//        u.save("gps:asked");
//        u.save("gps:allowed");

////// FIXME - remove when no alpha

        main.alpha.addEventListener("click", function(){
            alphaDialog = alphaDialog || u.dialog({
                className: "alert-dialog",
                items: [
                    { type: HTML.DIV, innerHTML:u.lang.gps_alpha_1 },
                    { type: HTML.DIV, innerHTML:u.lang.gps_alpha_2 },
                    { type: HTML.DIV, innerHTML:u.lang.gps_alpha_3 },
                    { type: HTML.DIV, innerHTML:u.lang.gps_alpha_4 },
                    { type: HTML.DIV, innerHTML:u.lang.gps_alpha_5 },
                    { type: HTML.DIV, innerHTML:u.lang.gps_alpha_6 },
                    { type: HTML.DIV, innerHTML:u.lang.gps_alpha_7 },
                    { type: HTML.DIV, innerHTML:u.lang.gps_alpha_8 },
                ],
                positive: {
                    label: u.lang.ok,
                    onclick: function(){
                        alphaDialog.close();
                    }
                },
            });

            alphaDialog.open();
        });


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
                            { type: HTML.DIV, innerHTML: u.lang.gps_location_required_3 },
                            { type: HTML.DIV, innerHTML: u.lang.gps_location_required_4 },
                            { type: HTML.DIV, innerHTML: u.lang.gps_location_required_5 },
                        ],
                        positive: {
                            label: u.lang.gps_ok_go_ahead,
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
            u.save("gps:allowed", true);
            locationUpdateListener(location);
            navigator.geolocation.watchPosition(locationUpdateListener, function(error){
                console.error(error);
            }, {
                enableHighAccuracy: true,
                maximumAge: 1000,
                timeout: 30000
            });
        },function(error){
            var message;
            switch(error.code) {
                case error.PERMISSION_DENIED:
                    message = u.lang.gps_you_have_denied_geolocation;
                    u.save("gps:asked");
                    u.save("gps:allowed");
                    break;
                case error.PERMISSION_DENIED_TIMEOUT:
                    message = u.lang.gps_user_took_too_long_to_grant_deny_geolocation_permission;
                    break;
                case error.POSITION_UNAVAILABLE:
                    message = u.lang.gps_geolocation_is_unavailable;
                    break;
                case error.TIMEOUT:
                    message = u.lang.gps_request_to_geolocation_is_timed_out;
                    break;
                default:
                    message = u.lang.gps_unknown_error_occurred_while_requesting_geolocation;
                    break;
            }

            var icon;
            var alert = u.dialog({
                className: "alert-dialog",
                items: [
                    { type: HTML.DIV, label: message + " " + u.lang.gps_please_resolve_this_problem_and_try_again },
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
                alert.classList.remove("hidden");
            }}, main.right);
        }, {
            enableHighAccuracy: true,
            maximumAge: 60000,
            timeout: 30000
        });
    }


    function locationUpdateListener(position) {

        if(!position) return;
        // position = geoTrackFilter.normalizeLocation(position);
        var last = main.me.location;
        if(last && last.coords && last.coords.latitude == position.coords.latitude && last.coords.longitude == position.coords.longitude) {
            return;
        }
        console.log("POSITION",position.coords.latitude, position.coords.longitude, position);

        u.save("gps:last",u.cloneAsObject(position));
        var message = u.locationToJson(position);
        if(main.tracking && main.tracking.getStatus() == EVENTS.TRACKING_ACTIVE) main.tracking.sendMessage(REQUEST.TRACKING, message);
        main.me.addLocation(position);
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
            }
        }
    }



    var resources = {
        enable_geolocation: "Enable geolocation",

        gps_ok_go_ahead: "OK, go ahead",

        gps_alpha_1: "Thank you for using the",
        gps_alpha_2: "ALPHA version of Waytogo.",
        gps_alpha_3: "&nbsp;",
        gps_alpha_4: "Please if you found some errors,",
        gps_alpha_5: "weird behaviour, new great idea",
        gps_alpha_6: "or just because -",
        gps_alpha_7: "feel free to send us an e-mail:",
        gps_alpha_8: "<a href=\"mailto:support@waytogo.us\">support@waytogo.us</a>.",

        gps_location_required_1: "The purpose of this service is to help friends find each other.",
        gps_location_required_2: "To do this, send your location to your friends.",
        gps_location_required_3: "Now the browser should ask you about using your location information.",
        gps_location_required_4: "Answer him \"Allow\", otherwise your friends",
        gps_location_required_5: "will not be able to see where you are.",

        gps_you_have_denied_geolocation: "You have denied geolocation.",
        gps_user_took_too_long_to_grant_deny_geolocation_permission: "User took too long to grant/deny geolocation permission.",
        gps_geolocation_is_unavailable: "Geolocation is unavailable.",
        gps_request_to_geolocation_is_timed_out: "The request to geolocation is timed out.",
        gps_unknown_error_occurred_while_requesting_geolocation: "An unknown error occurred while requesting geolocation.",
        gps_please_resolve_this_problem_and_try_again: "Please resolve this problem and try again. Note that geolocation is required for working this service properly.",

        gps_help_title: "Geolocation",
        gps_help_1_title: "Allow geolocation",
        gps_help_1_body: "Instruction how to allow geolocation"
    }




    return {
        type:type,
        start:start,
        onEvent:onEvent,
        help:help,
        resources:resources,
    }
}