/**
 * Created 3/10/17.
 */
function StreetViewHolder(main) {

    var type = "street";
    var show = u.load("streetview:show");
    var view;
    var drawerShow,drawerHide;
    var panorama;
    var placeholder;
    var streetviewService;
    var streetview;

    function start() {
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                drawerShow = object.add(DRAWER.SECTION_VIEWS,type+"_1","Show street view","streetview",function(){
                    view.onopen();
                });
                drawerHide = object.add(DRAWER.SECTION_VIEWS,type+"_1","Hide street view","streetview",function(){
                    view.onclose();
                });
                drawerShow.classList.add("hidden");
                drawerHide.classList.add("hidden");
                break;
            case EVENTS.SELECT_USER:
            case EVENTS.SELECT_SINGLE_USER:
                update();
                break;
            case EVENTS.MAP_READY:
                if(show) {
                    drawerHide.classList.remove("hidden");
                } else {
                    drawerShow.classList.remove("hidden");
                }

                streetviewService = new google.maps.StreetViewService();

                view = u.dialog({
                    title: {
                        label: "Street view",
                        className: "hideable"
                    },
                    className: "streetview",
                    tabindex: 1,
                    items: [
                        { type: HTML.DIV, className: "streetview-placeholder", innerHTML: "Loading..." },
                        { type: HTML.DIV, className: "streetview-view hidden", id: "streetview" },
                    ],
                    onclose: function(){
                        u.save("streetview:show");
                        show = false;
                        drawerShow.classList.remove("hidden");
                        drawerHide.classList.add("hidden");
                        google.maps.event.trigger(main.map, 'resize');
                        main.fire(EVENTS.CAMERA_UPDATE);
                    },
                    onopen: function() {
                        u.save("streetview:show",true);
                        show = true;
                        drawerShow.classList.add("hidden");
                        drawerHide.classList.remove("hidden");
                        google.maps.event.trigger(main.map, 'resize');
                        main.fire(EVENTS.CAMERA_UPDATE);
                        update();
                    }
                });
                placeholder = view.items[0];
                streetview = view.items[1];
                if(show) view.onopen();
                break;
            default:
                break;
        }
        return true;
    }

    function createView(myUser){

        var view = {};
        return view;
        // console.log("SAMPLECREATEVIEW",user);
    }

    function onChangeLocation(location) {
        if(this.properties.selected) {
            update();
        }
        // console.log("SAMPLEONCHANGELOCATION",this,location);

    }

    function update() {
        if(show && main.users.getCountSelected() == 1) {
            main.users.forAllUsers(function(number, user){
                if(!user.properties.selected || !user.location) return;

                panorama = panorama || new google.maps.StreetViewPanorama(streetview, {
                    panControl: true,
                        zoomControl: true,
                        addressControl: false,
                        fullscreenControl: true,
                        motionTrackingControl: true,
                        linksControl: true,
                        enableCloseButton: false
                });
                streetviewService.getPanorama({
                    location: u.latLng(user.location),
                    radius: 50
                }, function(data, status) {
                    if (status == google.maps.StreetViewStatus.OK) {
                        panorama.setPano(data.location.pano);
                        panorama.setPov({
                            heading: user.location.coords.heading || 0,
                            pitch: 0
                        });
                        panorama.setVisible(true);
                        placeholder.classList.add("hidden");
                        streetview.classList.remove("hidden");
                    } else {
                        placeholder.innerHTML = "Street view is still not available for this place.";
                        placeholder.classList.remove("hidden");
                        streetview.classList.add("hidden");
                    }
                });
            });
        } else if (show) {
            placeholder.innerHTML = "Street view available only for one point.";
            placeholder.classList.remove("hidden");
            streetview.classList.add("hidden");
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