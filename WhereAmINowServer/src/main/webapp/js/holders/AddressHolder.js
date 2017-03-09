/**
 * Created 2/10/17.
 */
function AddressHolder(main) {

    var type = "address";

    EVENTS.UPDATE_ADDRESS = "update_address";

    function start() {
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.SELECT_USER:
                onChangeLocation.call(this,this.location);
                break;
            default:
                break;
        }
        return true;
    }

    function onChangeLocation(location) {
        var user = this;
        // if (main.users.getCountSelected() == 1){// && this.properties && this.properties.selected) {
        if(location) {
            var xhr = new XMLHttpRequest();
            xhr.open("GET", "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + location.coords.latitude + "&lon=" + location.coords.longitude + "&zoom=18&addressdetails=1", true);

            xhr.onreadystatechange = function () {
                if (xhr.readyState != 4) return;

                var address = JSON.parse(xhr.response);
                user.fire(EVENTS.UPDATE_ADDRESS, address["display_name"]);
            };

            xhr.send();
        }
        // }
    }

    function createView(user) {
        var view = {

        }
        return view;
    }

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        dependsOnUser:true,
        onEvent:onEvent,
        createView:createView,
        onChangeLocation:onChangeLocation,
    }
}