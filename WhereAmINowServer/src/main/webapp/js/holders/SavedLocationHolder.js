/**
 * Created 3/16/17.
 */

EVENTS.SAVE_LOCATION = "save_location";
EVENTS.SHOW_SAVED_LOCATION = "show_saved_location";
EVENTS.EDIT_SAVED_LOCATION = "edit_saved_locations";
EVENTS.HIDE_SAVED_LOCATION = "hide_saved_location";
EVENTS.NAVIGATE_SAVED_LOCATION = "navigate_saved_location";
EVENTS.DELETE_SAVED_LOCATION = "delete_saved_location";
EVENTS.SHOW_SAVED_LOCATIONS = "show_saved_locations";
EVENTS.SHARE_SAVED_LOCATION = "share_saved_locations";
EVENTS.SEND_SAVED_LOCATION = "send_saved_locations";

function SavedLocationHolder(main) {

    var type = "saved_location";

    var locationSavedDialog;
    var locationEditDialog;
    var locationShareDialog;
    var locationDeleteDialog;
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
                    if(locationsDialog && locationsDialog.opened) {
                        locationsDialog.onclose();
                    } else {
                        main.fire(EVENTS.SHOW_SAVED_LOCATIONS);
                    }
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
                    if(locationsDialog && locationsDialog.opened) main.fire(EVENTS.SHOW_SAVED_LOCATIONS);

                    locationSavedDialog = locationSavedDialog || u.dialog({
                        items: [
                            { type: HTML.HIDDEN },
                            { type: HTML.DIV, innerHTML: "You have added location. It could be found under \"Saved locations\" menu." },
                            { type: HTML.DIV, innerHTML: "Would you like to do something else?" }
                        ],
                        positive: {
                            label: "Show",
                            onclick: function(items) {
                                main.fire(EVENTS.SHOW_SAVED_LOCATION, items[0].value);
                            }
                        },
                        negative: {
                            label: "Edit",
                            onclick: function(items) {
                                main.fire(EVENTS.EDIT_SAVED_LOCATION, items[0].value);
                            }
                        },
                        neutral: {
                            label: "Maybe later"
                        },
                        timeout: 5000
                    });
                    locationSavedDialog.items[0].value = last;
                    locationSavedDialog.onopen();
                }
                break;
            case EVENTS.SHOW_SAVED_LOCATION:
                var loc = u.load("saved_location:"+parseInt(object));
                console.log("SHOW",loc);
                if(loc) {
                }//TODO
                break;
            case EVENTS.NAVIGATE_SAVED_LOCATION:
                var loc = u.load("saved_location:"+parseInt(object));
                console.log("NAVIGATE",loc);
                if(loc) {
                }//TODO
                break;
            case EVENTS.EDIT_SAVED_LOCATION:
                locationEditDialog && locationEditDialog.onclose();
                locationShareDialog && locationShareDialog.onclose();
                locationDeleteDialog && locationDeleteDialog.onclose();
                var number = parseInt(object);
                var loc = u.load("saved_location:"+number);
                locationEditDialog = locationEditDialog || u.dialog({
                    title: "Edit location",
                    items: [
                        { type: HTML.HIDDEN },
                        { type: HTML.INPUT, label: "Name" },
                        { type: "textarea", label: "Description" },
                    ],
                    className: "saved-location-edit",
                    positive: {
                        label: "OK",
                        onclick: function(items) {
                            var number = items[0].value;
                            var name = items[1].value || "";
                            var description = items[2].value || "";
                            var loc = u.load("saved_location:"+number);
                            loc.n = name;
                            loc.d = description;
                            u.save("saved_location:"+number, loc);
                            if(locationsDialog && locationsDialog.opened) main.fire(EVENTS.SHOW_SAVED_LOCATIONS);
                        }
                    },
                    negative: {
                        label: "Cancel"
                    },
                });

                console.log("EDIT",loc);
                if(loc) {
                    locationEditDialog.items[0].value = number;
                    locationEditDialog.items[1].value = loc.n;
                    locationEditDialog.items[2].value = loc.d;
                    locationEditDialog.onopen();
                }
                break;
            case EVENTS.SHARE_SAVED_LOCATION:
                locationEditDialog && locationEditDialog.onclose();
                locationShareDialog && locationShareDialog.onclose();
                locationDeleteDialog && locationDeleteDialog.onclose();
                var loc = u.load("saved_location:"+parseInt(object));
                console.log("SHARE",loc);
                if(loc) {
                }//TODO

                break;
            case EVENTS.DELETE_SAVED_LOCATION:
                locationEditDialog && locationEditDialog.onclose();
                locationShareDialog && locationShareDialog.onclose();
                locationDeleteDialog && locationDeleteDialog.onclose();
                var number = parseInt(object);
                var loc = u.load("saved_location:"+number);
                locationDeleteDialog = locationDeleteDialog || u.dialog({
                    title: "Delete location",
                    items: [
                        { type: HTML.HIDDEN },
                        { type: HTML.DIV, innerHTML: "Delete this location?" },
                    ],
                    className: "saved-location-delete",
                    positive: {
                        label: "Yes",
                        onclick: function(items) {
                            var number = items[0].value;
                            u.save("saved_location:"+number);
                            if(locationsDialog && locationsDialog.opened) main.fire(EVENTS.SHOW_SAVED_LOCATIONS);
                        }
                    },
                    negative: {
                        label: "No"
                    },
                });

                console.log("EDIT",loc);
                if(loc) {
                    locationDeleteDialog.items[0].value = number;
                    locationDeleteDialog.onopen();
                }
                break;
            case EVENTS.SHOW_SAVED_LOCATIONS:
                locationsDialog = locationsDialog || u.dialog({
                    title: "Saved locations",
                    items: [],
                    className: "saved-location",
                    itemsClassName: "saved-location-items",
                    onopen: function(){},
                    onclose: function(){},
                });
                locationsDialog.clearItems();
                var last = u.load("saved_location:counter") || 0;
                for(var i = 1; i <= last; i++) {
                    var loc = u.load("saved_location:"+i);
                    if(loc) {
                        var div = locationsDialog.addItem({
                            type: HTML.DIV,
                            className: "saved-location-item",
                        });

                        var url = "http://maps.google.com/maps/api/staticmap?center=" +loc.la + "," + loc.lo + "&zoom=15&size=200x200&sensor=false" + "&markers=color:darkgreen|"+loc.la+","+loc.lo;

                        u.create("img", {
                            src: url,
                            className: "saved-location-item-image",
                            onload: function(e) {
//                                console.log(e);
                            },
                            innerHTML:"update",
                        }, div);

                        var content = u.create(HTML.DIV, { className: "saved-location-item-text" }, div);
                        u.create(HTML.DIV, { className: "saved-location-item-label", innerHTML:loc.n }, content);
                        u.create(HTML.DIV, { className: "saved-location-item-timestamp", innerHTML:new Date(loc.t).toLocaleString() }, content);
                        u.create(HTML.DIV, { className: "saved-location-item-address", innerHTML:loc.a }, content);
                        if(!loc.a) fetchAddressFor(i);
                        u.create(HTML.DIV, { className: "saved-location-item-description", innerHTML:loc.d }, content);
                        u.create(HTML.BUTTON, { className: "saved-location-item-button saved-location-item-show", dataNumber: i, innerHTML:"remove_red_eye", title:"Show location", onclick:function(){
                            locationsDialog.onclose();
                            main.fire(EVENTS.SHOW_SAVED_LOCATION, this.dataset.number);
                        } }, content);
                        u.create(HTML.BUTTON, { className: "saved-location-item-button saved-location-item-navigate", dataNumber: i, innerHTML:"navigation", title:"Show direction to location", onclick:function(){
                            locationsDialog.onclose();
                            main.fire(EVENTS.NAVIGATE_SAVED_LOCATION, this.dataset.number);
                        } }, content);
                        u.create(HTML.BUTTON, { className: "saved-location-item-button saved-location-item-edit", dataNumber: i, innerHTML:"mode_edit", title:"Edit location", onclick:function(){
                            main.fire(EVENTS.EDIT_SAVED_LOCATION, this.dataset.number);
                        } }, content);
                        u.create(HTML.BUTTON, { className: "saved-location-item-button saved-location-item-share", dataNumber: i, innerHTML:"share", title:"Share location", onclick:function(){
                            main.fire(EVENTS.SHARE_SAVED_LOCATION, this.dataset.number);
                        } }, content);
                        u.create(HTML.BUTTON, { className: "saved-location-item-button saved-location-item-delete", dataNumber: i, innerHTML:"clear", title:"Delete location", onclick:function(){
                            main.fire(EVENTS.DELETE_SAVED_LOCATION, this.dataset.number);
                        } }, content);
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
                    if(locationsDialog && locationsDialog.opened) main.fire(EVENTS.SHOW_SAVED_LOCATIONS);
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