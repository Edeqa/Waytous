/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/10/17.
 */

EVENTS.UPDATE_ADDRESS = "update_address";

function AddressHolder(main) {

    var type = "address";

    var delayInError = 10000;
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

//                u.getJSON("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + location.coords.latitude + "&lon=" + location.coords.longitude + "&zoom=18&addressdetails=1")
//                .then()
//                .catch();

                u.getJSON("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + location.coords.latitude + "&lon=" + location.coords.longitude + "&zoom=18&addressdetails=1")
                    .then(function(json){
                        user.fire(EVENTS.UPDATE_ADDRESS, json["display_name"]);
                    }).catch(function(code, xhr) {
                        user.fire(EVENTS.UPDATE_ADDRESS);
                        delayStart = new Date().getTime();
                    });

            }
        }, 0);
    }

    function createView(user) {
        return {};
    }

    function updateAddress(node) {
        var user = this;
        if(user.location && user.location.coords && node) {
            if(delayStart) {
                if(new Date().getTime() - (delayStart||0) < delayInError) return;
                delayStart = 0;
            }

            u.getJSON("https://nominatim.openstreetmap.org/reverse?format=json&lat=" + user.location.coords.latitude + "&lon=" + user.location.coords.longitude + "&zoom=18&addressdetails=1")
                .then(function(json){
                    node.innerHTML = json["display_name"];
                });
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