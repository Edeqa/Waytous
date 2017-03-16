/**
 * Created 3/16/17.
 */

EVENTS.SAVE_LOCATION = "save_location";
EVENTS.SHOW_SAVED_LOCATION = "show_saved_location";
EVENTS.EDIT_SAVED_LOCATION = "edit_saved_locations";
EVENTS.HIDE_SAVED_LOCATION = "hide_saved_location";
EVENTS.DELETE_SAVED_LOCATION = "delete_saved_location";
EVENTS.SHOW_SAVED_LOCATIONS = "show_saved_locations";
EVENTS.SHARE_SAVED_LOCATION = "share_saved_locations";
EVENTS.SEND_SAVED_LOCATION = "send_saved_locations";

function SavedLocationHolder(main) {

    var type = "saved_location";

    var locationSavedDialog;
    var locationsDialog;
    var drawerMenuItem;

    function start() {
        console.log("SAMPLEHOLDER", this);
    }

    function onEvent(EVENT,object){
        // console.log("SAMPLEEVENT",EVENT,object)
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                drawerMenuItem = object.add(DRAWER.SECTION_NAVIGATION, EVENT.SHOW_SAVED_LOCATIONS, "Saved locations", "pin_drop", function(){
                    main.fire(EVENTS.SHOW_SAVED_LOCATIONS);
                });
                var last = u.load("saved_location:counter") || 0;
                var exists = false;
                for(var i = 0; i <= last; i++) {
                    if(u.load("saved_location:"+i)) {
                        exists = true;
                        break;
                    }
                }
                if(!exists) drawerMenuItem.classList.add("hidden");
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user && user.location && !user.saved_location) {
                    object.add(MENU.SECTION_NAVIGATION, EVENT.SAVE_LOCATION, "Save location", "pin_drop", function () {
                        user.fire(EVENTS.SAVE_LOCATION);
                    });
                }

                break;
            case EVENTS.SAVE_LOCATION:
                var user = this;
                if(user) {
                    var loc = {
                        la:user.location.coords.latitude,
                        lo:user.location.coords.longitude,
                        t:new Date().getTime(),
                        n:user.properties.getDisplayName(),
                        a:"",
                        d:"",
                        k:""
                    }
                    var last = u.load("saved_location:counter") || 0;
                    last++;
                    u.save("saved_location:counter",last);
                    u.save("saved_location:"+last, loc);
                    drawerMenuItem.classList.remove("hidden");
                    fetchAddressFor(last);

                    locationSavedDialog = locationSavedDialog || u.dialog({
                        items: [
                            { type: HTML.DIV, innerHTML: "You have added location. It could be found under \"Saved locations\" menu." },
                            { type: HTML.DIV, innerHTML: "Would you like to do something else?" }
                        ],
                        positive: {
                            label: "Show",
                            onclick: function() {
                                main.fire(EVENTS.SHOW_SAVED_LOCATION, last);
                            }
                        },
                        negative: {
                            label: "Edit",
                            onclick: function() {
                                main.fire(EVENTS.EDIT_SAVED_LOCATION, last);
                            }
                        },
                        neutral: {
                            label: "Maybe later"
                        },
                        timeout: 5000
                    });
                    locationSavedDialog.onopen();
                }
                break;
            case EVENTS.SHOW_SAVED_LOCATION:
                var loc = u.load("saved_location:"+parseInt(object));
                console.log("SHOW",loc);
                if(loc) {
                }
            case EVENTS.SHOW_SAVED_LOCATIONS:
                locationsDialog = locationsDialog || u.dialog({
                    title: "Saved locations",
                    items: [],
                    className: "saved-locations",
                    itemsClassName: "saved-location-items",
                    onopen: function(){},
                    onclose: function(){}
                });

                var last = u.load("saved_location:counter") || 0;
                for(var i = 1; i <= last; i++) {
                    var loc = u.load("saved_location:"+i);
                    if(loc) {
                        var div = locationsDialog.addItem({
                            type: HTML.DIV,
                            className: "saved-locations-item",
                        });
                        var content = u.create(HTML.DIV, { className: "saved-locations-item-text" }, div);
                        u.create(HTML.DIV, { className: "saved-locations-item-label", innerHTML:loc.n }, content);
                        u.create(HTML.DIV, { className: "saved-locations-item-address", innerHTML:loc.a }, content);
                        u.create(HTML.DIV, { className: "saved-locations-item-description", innerHTML:loc.d }, content);
                        u.create(HTML.DIV, { className: "saved-locations-item-timestamp", innerHTML:new Date(loc.t).toLocaleString() }, content);
                        u.create(HTML.DIV, { className: "saved-locations-item-image", innerHTML:"image" }, div);
                    }
                }

                locationsDialog.onopen();
                break;
            default:
                break;
        }
        return true;
    }

    function createView(user){
        var view = {
            user: user,
//            show: u.load("sample:user:" + user.number)
        };
        // console.log("SAMPLECREATEVIEW",user);
        return view;
    }

    function onChangeLocation(location) {
        // console.log("SAMPLEONCHANGELOCATION",this,location);
    }

    function fetchAddressFor(number) {
        var loc = u.load("saved_location:"+number);
        if(!loc || loc.a || !loc.la || !loc.lo) return;

        var xhr = new XMLHttpRequest();
        xhr.open("GET", "https://nominatim.openstreetmap.org/reverse?format=json&lat=" + loc.la + "&lon=" + loc.lo + "&zoom=18&addressdetails=1", true);

        xhr.onreadystatechange = function () {
            if (xhr.readyState != 4) return;
            try {
                var address = JSON.parse(xhr.response);
                if(address["display_name"]) {
                    loc.a = address["display_name"];
                    u.save("saved_location:"+number, loc);
                    console.log("Address resolved for",loc.n,loc.a);
                }
            } catch(e) {
                console.log("Address not resolved for",loc.n);
            }
        };
        try {
            xhr.send();
        } catch(e) {
            console.warn(e);
        }
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