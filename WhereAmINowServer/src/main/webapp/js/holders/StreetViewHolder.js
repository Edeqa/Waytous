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
                drawerShow = object.add(DRAWER.SECTION_VIEWS, type+"_show", u.lang.show_street_view, "streetview", function(){
                    view.open();
                });
                drawerHide = object.add(DRAWER.SECTION_VIEWS, type+"_hide", u.lang.hide_street_view, "streetview", function(){
                    view.close();
                });
                drawerShow.hide();
                drawerHide.hide();
                break;
            case EVENTS.SELECT_USER:
            case EVENTS.SELECT_SINGLE_USER:
                update();
                break;
            case EVENTS.MAP_READY:
                if(show) {
                    drawerHide.show();
                } else {
                    drawerShow.show();
                }

                streetviewService = new google.maps.StreetViewService();

                view = u.dialog({
                    title: {
                        label: u.lang.street_view,
                        className: "mobile-hidden"
                    },
                    className: "streetview",
                    tabindex: 1,
                    items: [
                        { type: HTML.DIV, className: "streetview-placeholder", innerHTML: u.lang.loading },
                        { type: HTML.DIV, className: "streetview-view hidden", id: "streetview" },
                    ],
                    onclose: function(){
                        u.save("streetview:show");
                        show = false;
                        drawerShow.show();
                        drawerHide.hide();
                        google.maps.event.trigger(main.map, 'resize');
                        main.fire(EVENTS.CAMERA_UPDATE);
                    },
                    onopen: function() {
                        u.save("streetview:show",true);
                        show = true;
                        drawerShow.hide();
                        drawerHide.show();
                        google.maps.event.trigger(main.map, 'resize');
                        main.fire(EVENTS.CAMERA_UPDATE);
                        update();
                    }
                });
                placeholder = view.items[0];
                streetview = view.items[1];
                if(show) view.open();
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
                        placeholder.hide();
                        streetview.show();
                    } else {
                        placeholder.innerHTML = u.lang.street_view_is_still_not_available_for_this_place.outerHTML;
                        placeholder.show();
                        streetview.hide();
                    }
                });
            });
        } else if (show) {
            placeholder.innerHTML = u.lang.street_view_available_only_for_one_point.outerHTML;
            placeholder.show();
            streetview.hide();
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