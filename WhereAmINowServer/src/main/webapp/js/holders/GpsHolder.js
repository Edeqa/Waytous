/**
 * Created 2/16/17.
 */
function GpsHolder(main) {

    var type = "gps";
    var geoTrackFilter = new GeoTrackFilter();

    function start() {
        console.log("GPSHOLDER",this);

        /*var latLong;
        $.getJSON("https://ipinfo.io", function(ipinfo){
            console.log("Found location ["+ipinfo.loc+"] by ipinfo.io");
            latLong = ipinfo.loc.split(",");
        });*/

        navigator.geolocation.getCurrentPosition(function(location){
            console.log("GPS ALLOWED",location);
            locationUpdateListener(location);
            navigator.geolocation.watchPosition(locationUpdateListener);
        },function(error){
            var message;
            switch(error.code) {
                case error.PERMISSION_DENIED:
                    message = "You have denied geolocation.";
                    break;
                case error.POSITION_UNAVAILABLE:
                    message = "Geolocation is unavailable.";
                    break;
                case error.TIMEOUT:
                    message = "The request to geolocation timed out.";
                    break;
                default:
                    message = "An unknown error occurred while requesting geolocation.";
                    break;
            }
            var alert = u.create("div", {className:"alert-dialog shadow"}, main.right);
            u.create("div", message + " Please resolve this problem and try again. Note that geolocation is required for working this service properly.<br>", alert);
            u.create("div", "&nbsp;", alert);
            u.create("button", {type:"button", innerHTML:"OK", onclick:function(){
                icon.classList.remove("hidden");
                alert.classList.add("hidden");
            }}, alert);
            var icon = u.create("button", {className:"material-icons alert-icon shadow hidden", type: "button", innerHTML:"warning", onclick: function(){
                icon.classList.add("hidden");
                alert.classList.remove("hidden");
            }}, main.right);
        }, {
            enableHighAccuracy: true
        });
    }

    function onEvent(EVENT,object){
        // console.log("SAMPLEEVENT",EVENT,object)
        switch (EVENT){
            default:
                break;
        }
        return true;
    }

    function locationUpdateListener(position) {
        console.log("POSITION",position.coords.latitude, position.coords.longitude, position);

        if(!position) return;
        // position = geoTrackFilter.normalizeLocation(position);
        var last = main.me.location;
        if(last && last.coords && last.coords.latitude == position.coords.latitude && last.coords.longitude == position.coords.longitude) {
            return;
        }

        var message = u.locationToJson(position);
        main.tracking.sendMessage(REQUEST.TRACKING, message);
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

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
    }
}