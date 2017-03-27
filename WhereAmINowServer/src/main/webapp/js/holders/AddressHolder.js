/**
 * Created 2/10/17.
 */
function AddressHolder(main) {

    var type = "address";

    EVENTS.UPDATE_ADDRESS = "update_address";

    const delayInError = 10000;
    var delayStart;

    function start() {
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.SELECT_USER:
                onChangeLocation.call(this,this.location);
                break;
            case EVENTS.UPDATE_MENU_SUBTITLE:
                updateAddress.call(this, object);
                break;
            case EVENTS.UPDATE_ACTIONBAR_SUBTITLE:
                updateAddress.call(this, object);
                break;
            default:
                break;
        }
        return true;
    }

    function onChangeLocation(location) {
        return;
        var user = this;
        setTimeout(function(){
            if(location) {
                if(delayStart) {
                    if(new Date().getTime() - (delayStart||0) < delayInError) return;
                    delayStart = 0;
                }

                var xhr = new XMLHttpRequest();
                xhr.open("GET", "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + location.coords.latitude + "&lon=" + location.coords.longitude + "&zoom=18&addressdetails=1", true);

                xhr.onreadystatechange = function () {
                    if (xhr.readyState != 4) return;
                    if(xhr.status == 0) {
                        user.fire(EVENTS.UPDATE_ADDRESS);
                        delayStart = new Date().getTime();
                        return;
                    }

                    var address = JSON.parse(xhr.response);
                    user.fire(EVENTS.UPDATE_ADDRESS, address["display_name"]);
                };
                try {
                    xhr.send();
                } catch(e) {
                    console.warn(e);
                }
            }
            // }
        }, 0);
    }

    function createView(user) {
        var view = {

        }
        return view;
    }

    function updateAddress(node) {
        var user = this;
        if(user.location && user.location.coords && node) {
            if(delayStart) {
                if(new Date().getTime() - (delayStart||0) < delayInError) return;
                delayStart = 0;
            }

            var xhr = new XMLHttpRequest();
            xhr.open("GET", "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + user.location.coords.latitude + "&lon=" + user.location.coords.longitude + "&zoom=18&addressdetails=1", true);

            xhr.onreadystatechange = function () {
                if (xhr.readyState != 4) return;
                if(xhr.status == 0) {
//                    updateAddress.call(user,node);
//                    delayStart = new Date().getTime();
                    return;
                }

                var address = JSON.parse(xhr.response);
                node.innerHTML = address["display_name"];
            };
            try {
                xhr.send();
            } catch(e) {
                console.warn(e);
            }
        }
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        createView:createView,
        onChangeLocation:onChangeLocation,
    }
}