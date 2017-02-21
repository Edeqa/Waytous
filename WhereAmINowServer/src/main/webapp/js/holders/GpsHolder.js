/**
 * Created 2/16/17.
 */
function GpsHolder(main) {

    var type = "gps";
    var user;

    function start() {
        // console.log("GPSHOLDER",this);

        navigator.geolocation.getCurrentPosition(function(location){
            console.log("GPS ALLOWED",location);
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
        main.me.addLocation(position);
    }

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
    }
}