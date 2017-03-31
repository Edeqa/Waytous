/**
 * Created 3/9/17.
 */

EVENTS.SHOW_DISTANCE = "show_distance";
EVENTS.HIDE_DISTANCE = "hide_distance";

function DistanceHolder(main) {

    var type = "distance";
    var view;
    var drawerItemShow;
    var drawerItemHide;


    function start() {
        view = {};
    }

    function onEvent(EVENT,object){
        // console.log("SAMPLEEVENT",EVENT,object)
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                drawerItemShow = object.add(DRAWER.SECTION_VIEWS, EVENTS.SHOW_DISTANCE, u.lang.show_distances, "settings_ethernet", function(){
                    main.users.forAllUsers(function (number, user) {
                        user.fire(EVENTS.SHOW_DISTANCE);
                        drawerPopulate();
                    });
                });
                drawerItemHide = object.add(DRAWER.SECTION_VIEWS, EVENTS.HIDE_DISTANCE, u.lang.hide_distances, "code", function(){
                    main.users.forAllUsers(function (number, user) {
                        user.fire(EVENTS.HIDE_DISTANCE);
                        drawerPopulate();
                    });
                });
                drawerPopulate();
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user && user != main.me && user.location && !user.views.distance.show) {
                    object.add(MENU.SECTION_VIEWS, EVENTS.SHOW_DISTANCE, u.lang.show_distance, "settings_ethernet", function(){
                        user.fire(EVENTS.SHOW_DISTANCE);
                        drawerPopulate();
                    });
                } else if(user && user != main.me && user.views.distance.show) {
                    object.add(MENU.SECTION_VIEWS, EVENTS.HIDE_DISTANCE, u.lang.hide_distance, "code", function(){
                        user.fire(EVENTS.HIDE_DISTANCE);
                        drawerPopulate();
                    });
                }
                break;
            case EVENTS.SHOW_DISTANCE:
                if(this != main.me) {
                    this.views.distance.show = true;
                    u.saveForGroup("distance:show:" + this.number, true);
                    show.call(this);
                }
                break;
            case EVENTS.HIDE_DISTANCE:
                this.views.distance.show = false;
                u.saveForGroup("distance:show:" + this.number);
                if(this.views && this.views.distance && this.views.distance.distance) {
                    this.views.distance.distance.setMap(null);
                    this.views.distance.distance = null;

                    this.views.distance.marker.setMap(null);
                    this.views.distance.marker = null;
                    this.views.distance.label.setMap(null);
                    this.views.distance.label = null;

                }
                break;
            default:
                break;
        }
        return true;
    }

    function createView(myUser){
        var view = {};
        view.user = myUser;

        view.show = u.loadForGroup("distance:show:" + myUser.number);

        if(view.show) {
            show.call(myUser);
        }
        drawerPopulate();
        return view;
        // console.log("SAMPLECREATEVIEW",user);
    }

    function removeView(user) {
        if(user && user.views && user.views.distance & user.views.distance.distance) {

            user.views.distance.distance.setMap(null);
            user.views.distance.distance = null;
            user.views.distance.marker.setMap(null);
            user.views.distance.marker = null;
            user.views.distance.label.setMap(null);
            user.views.distance.label = null;
        }
    }

    function drawerPopulate() {
        setTimeout(function(){
            drawerItemHide.hide();
            drawerItemShow.hide();
            main.users.forAllUsersExceptMe(function (number, user) {
                if(user.views.distance) {
                    if (user.views.distance.show) {
                        drawerItemHide.show();
                    } else {
                        drawerItemShow.show();
                    }
                }
            })
        },0);
    }

    function show() {
        if (!this || !this.views || !this.views.distance || !this.views.distance.show) return;
        if (this.location && main.me.location && google) {
            if (!this.views.distance.distance) {
                this.views.distance.distance = new google.maps.Polyline({
                    geodesic: true,
                    strokeColor: "rgb(100,100,100)",
                    strokeOpacity: 0.6,
                    strokeWeight: 2,
                    map: main.map,
                });

                this.views.distance.marker = new google.maps.Marker({
                    map: main.map,
                    visible: false
                });
                this.views.distance.label = new u.label({map:main.map, className:"distance-label"});
                this.views.distance.label.bindTo("position", this.views.distance.marker, "position");
            }

            var points = [
                u.latLng(main.me.location),
                u.latLng(this.location)
            ];
            this.views.distance.distance.setPath(points);

            var markerPosition = google.maps.geometry.spherical.interpolate(points[0], points[1], .5);
            this.views.distance.marker.setPosition(markerPosition);
            var title = u.formatLengthToLocale(google.maps.geometry.spherical.computeDistanceBetween(points[0], points[1]));
            title = this.properties.getDisplayName() + "\n" + title;
            this.views.distance.label.set("text", title);
        }
    }

    function onChangeLocation(location) {
        show.call(this);
        // console.log("SAMPLEONCHANGELOCATION",this,location);
    }

    function Label(opt_options, node) {
        // Initialization
        this.setValues(opt_options);

        // Label specific
        if(!node) {
            node = u.create(HTML.DIV, {className:"distance-label"});
        }
        this.span_ = node;
        var div = this.div_ = u.create(HTML.DIV, {style: "position: absolute; display: none"});
        div.appendChild(node);
    };

    function help(){
        return {
            title: u.lang.distance_help_title,
            1: {
                title: u.lang.distance_article_1_title,
                body: u.lang.distance_article_1_body
            },
        }
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        createView:createView,
        removeView:removeView,
        onChangeLocation:onChangeLocation,
        help:help,
    }
}