/**
 * Created 3/9/17.
 */
EVENTS.SHOW_NAVIGATION = "show_navigation";
EVENTS.HIDE_NAVIGATION = "hide_navigation";

function NavigationHolder(main) {

    const REBUILD_TRACK_IF_LOCATION_CHANGED_IN_METERS = 10;
    const HIDE_TRACK_IF_DISTANCE_LESS_THAN = 10;
    const SHOW_TRACK_IF_DISTANCE_BIGGER_THAN = 20;

    var type = "navigation";
    var view;
    var drawerItemShow;
    var drawerItemHide;
    var navigation_outline_drawer, navigation_outline_menu;

    var navigation_outline_svg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"0 0 24 24",
        version:"1.1",
        className: "menu-item"
    };
    var navigation_outline_path = {
        xmlns:"http://www.w3.org/2000/svg",
        strokeWidth:"2",
        fill:"transparent",
        d: "M12,2L4.5,20.29l0.71,0.71L12,18l6.79,3 0.71,-0.71z"
    };


//<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" x="5" y="5" viewBox="1 1.029998779296875 16 16" style="red:transparent; fill:red; ">
//    <path d="M9 1.03c-4.42 0-8 3.58-8 8s3.58 8 8 8 8-3.58 8-8-3.58-8-8-8zM10 13H8v-2h2v2zm0-3H8V5h2v5z"></path>
//</svg>

    function start() {
        view = {};
    }

    function onEvent(EVENT,object){
        // console.log("SAMPLEEVENT",EVENT,object)
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                drawerItemShow = object.add(DRAWER.SECTION_VIEWS,EVENTS.SHOW_NAVIGATION,"Show navigations","navigation",function(){
                    main.users.forAllUsers(function (number, user) {
                        user.fire(EVENTS.SHOW_NAVIGATION);
                        drawerPopulate();
                    });
                });
                navigation_outline_drawer = navigation_outline_drawer || u.create(HTML.PATH, navigation_outline_path, u.create(HTML.SVG, navigation_outline_svg)).parentNode;
                drawerItemHide = object.add(DRAWER.SECTION_VIEWS,EVENTS.HIDE_NAVIGATION,"Hide navigations",navigation_outline_drawer,function(){
                    main.users.forAllUsers(function (number, user) {
                        user.fire(EVENTS.HIDE_NAVIGATION);
                        drawerPopulate();
                    });
                });
                drawerPopulate();
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user) {
                    var menuItemShow = object.add(MENU.SECTION_VIEWS,EVENTS.SHOW_NAVIGATION,"Show navigation","navigation",function(){
                        user.fire(EVENTS.SHOW_NAVIGATION);
                        menuItemShow.classList.add("hidden");
                        menuItemHide.classList.remove("hidden");
                        drawerPopulate();
                    });
                    navigation_outline_menu = navigation_outline_menu || u.create(HTML.PATH, navigation_outline_path, u.create(HTML.SVG, navigation_outline_svg)).parentNode;
                    var menuItemHide = object.add(MENU.SECTION_VIEWS,EVENTS.HIDE_NAVIGATION,"Hide navigation",navigation_outline_menu,function(){
                        user.fire(EVENTS.HIDE_NAVIGATION);
                        menuItemShow.classList.remove("hidden");
                        menuItemHide.classList.add("hidden");
                        drawerPopulate();
                    });
                    if(user.views.navigation.show) {
                        menuItemShow.classList.add("hidden");
                    } else {
                        menuItemHide.classList.add("hidden");
                    }
                }
                // object.add(8,type+"_2","Private message","chat",function(){console.log("PRIVATEMESSAGETO",user)});
                // object.add(8,type+"_3","Private message","chat",function(){console.log("PRIVATEMESSAGETO",user)});
                // object.add(8,type+"_4","Private message","chat",function(){console.log("PRIVATEMESSAGETO",user)});
                break;
            case EVENTS.SHOW_NAVIGATION:
                this.views.navigation.show = true;
                u.save("navigation:show:" + this.number, true);
                update.call(this);
                break;
            case EVENTS.HIDE_NAVIGATION:
                this.views.navigation.show = false;
                u.save("navigation:show:" + this.number);
                if(this.views && this.views.navigation && this.views.navigation.track) {
                    this.views.navigation.track.setMap(null);
                    this.views.navigation.track = null;
                    this.views.navigation.marker.setMap(null);
                    this.views.navigation.marker = null;
                    this.views.navigation.label.setMap(null);
                    this.views.navigation.label = null;
                }
                break;
            default:
                break;
        }
        return true;
    }

    function createView(user){
        var view = {};
        view.user = user;

        view.show = u.load("navigation:show:" + user.number);

        if(view.show) {
            update.call(user);
        }
        drawerPopulate();
        return view;
        // console.log("SAMPLECREATEVIEW",user);
    }

    function drawerPopulate() {
        drawerItemHide.classList.add("hidden");
        drawerItemShow.classList.add("hidden");
        main.users.forAllUsers(function (number, user) {
            if(user.views.navigation) {
                if (user.views.navigation.show) {
                    drawerItemHide.classList.remove("hidden");
                } else {
                    drawerItemShow.classList.remove("hidden");
                }
            }
        })

    }

    function update() {

        if(!this || !this.views || !this.views.navigation || !this.views.navigation.show) return;

        var user = this;
        var req = "https://crossorigin.me/https://maps.googleapis.com/maps/api/directions/json?"
            + "origin=" + main.me.location.coords.latitude + "," + main.me.location.coords.longitude + "&"
            + "destination=" + this.location.coords.latitude + "," + this.location.coords.longitude + "&"
            + "alternatives=false&"
            + "mode=";

//        switch (mode) {
//            case NAVIGATION_MODE_DRIVING:
//                req += "driving";
//                break;
//            case NAVIGATION_MODE_WALKING:
//                req += "walking";
//                break;
//            case NAVIGATION_MODE_BICYCLING:
//                req += "bicycling";
//                break;
//        }

//        if (State.getInstance().getBooleanPreference(PREFERENCE_AVOID_HIGHWAYS, false))
//            req += "&avoid=highways";
//        if (State.getInstance().getBooleanPreference(PREFERENCE_AVOID_TOLLS, false))
//            req += "&avoid=tolls";
//        if (State.getInstance().getBooleanPreference(PREFERENCE_AVOID_FERRIES, false))
//            req += "&avoid=ferries";

        console.log(type,req);

        var xhr = new XMLHttpRequest();
        xhr.open("GET", req, true);
        xhr.onreadystatechange = function() { //
            if(xhr.readyState != 4) return;
            if(xhr.status == 200) {
                var res = JSON.parse(xhr.response);
                updateTrack.call(user,res);
            } else {
                console.log(xhr);
                console.error("error")
            }
        };
        xhr.send();

    }

    function updateTrack(o) {

        if(!this.views.navigation.track) {
            createTrack.call(this);
        }

        var text = o.routes[0].overview_polyline.points;
        var points = google.maps.geometry.encoding.decodePath(text);

        var distanceText = o.routes[0].legs[0].distance.text;
        var durationText = o.routes[0].legs[0].duration.text;
        var title = distanceText + "\n" + durationText;

        this.views.navigation.distance = o.routes[0].legs[0].distance.value;

        console.log("NAV",title,this.views.navigation.distance,points)

        if (this.views.navigation.distance <= HIDE_TRACK_IF_DISTANCE_LESS_THAN) {
            console.log("remove path");
            this.views.navigation.previousDistance = this.views.navigationdistance;
            return;
        } else if (this.views.navigation.distance > SHOW_TRACK_IF_DISTANCE_BIGGER_THAN
            && this.views.navigation.previousDistance
            && this.views.navigation.previousDistance < SHOW_TRACK_IF_DISTANCE_BIGGER_THAN
            && this.views.navigation.track == null) {

            this.views.navigation.previousDistance = this.views.navigation.distance;
        } else if (this.views.navigation.distance > HIDE_TRACK_IF_DISTANCE_LESS_THAN
            && this.views.navigation.distance <= SHOW_TRACK_IF_DISTANCE_BIGGER_THAN
            && track == null) {
            this.views.navigation.previousDistance = this.views.navigation.distance;
            return;
        }
        this.views.navigation.previousDistance = this.views.navigation.distance;

        if (this.views.navigation.track != null) {
            this.views.navigation.track.setPath(points);
            this.views.navigation.trackCenter.setPath(points);

            var markerPosition = google.maps.geometry.spherical.interpolate(points[0], points[points.length-1], .5);
            this.views.navigation.marker.setPosition(markerPosition);
//            var title = u.formatLengthToLocale(google.maps.geometry.spherical.computeDistanceBetween(points[0], points[points.length-1]));
            title = this.properties.getDisplayName() + "\n" + title;
            this.views.navigation.label.set("text", title);

        }



        /*
                if(this.locations && this.locations.length > 1) {
                    if(!this.views.navigation.track) {
                        var points = [];
                        for(var i in this.locations) {
                            points.push(u.latLng(this.locations[i]));
                        }
                        this.views.navigation.track = new google.maps.Polyline({
                            path: points,
                            geodesic: true,
                            strokeColor: this.properties.color,
                            strokeOpacity: 0.6,
                            strokeWeight: 8,
                            map: main.map
                        });
                    } else {
                        this.views.navigation.track.getPath().push(u.latLng(this.location));
                    }

                }*/
    }

    function createTrack() {

        var points = [u.latLng(main.me.location), u.latLng(this.location)];
        this.views.navigation.track = new google.maps.Polyline({
            geodesic: true,
            strokeColor: this.properties.color,
            strokeOpacity: 0.6,
            strokeWeight: 8,
            zIndex: 100,
            map: main.map
        });
        this.views.navigation.trackCenter = new google.maps.Polyline({
            geodesic: true,
            strokeColor: "white",
//            strokeOpacity: 0.6,
            strokeWeight: 2,
            zIndex: 100,
            map: main.map
        });
        this.views.navigation.marker = new google.maps.Marker({
            map: main.map,
            visible: false
        });
        this.views.navigation.label = new u.label({
            map:main.map,
            style: {backgroundColor:this.properties.color, opacity:0.7, color:"white"},

        });
        this.views.navigation.label.bindTo("position", this.views.navigation.marker, "position");

    }

    function onChangeLocation(location) {
        update.call(this);
        // console.log("SAMPLEONCHANGELOCATION",this,location);
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