/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 3/9/17.
 */
EVENTS.HIDE_NAVIGATIONS = "hide_navigations";
EVENTS.SHOW_NAVIGATION = "show_navigation";
EVENTS.HIDE_NAVIGATION = "hide_navigation";

function NavigationHolder(main) {

    var REBUILD_TRACK_IF_LOCATION_CHANGED_IN_METERS = 10;
    var HIDE_TRACK_IF_DISTANCE_LESS_THAN = 10;
    var SHOW_TRACK_IF_DISTANCE_BIGGER_THAN = 20;
    var NAVIGATION_MODE_DRIVING = "car";
    var NAVIGATION_MODE_WALKING = "walk";
    var NAVIGATION_MODE_BICYCLING = "bike";

    var type = "navigation";
    var view;
    var drawerItemShow;
    var drawerItemHide;
    var navigation_outline_drawer, navigation_outline_menu;
    var panTask;
    var modeButtons;
    var modeDialog;
    var installation;

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
                /*drawerItemShow = object.add(DRAWER.SECTION_VIEWS,EVENTS.SHOW_NAVIGATION,"Show navigations","navigation",function(){
                    main.users.forAllUsers(function (number, user) {
                        user.fire(EVENTS.SHOW_NAVIGATION);
                        drawerPopulate();
                    });
                });*/
                navigation_outline_drawer = navigation_outline_drawer || u.create(HTML.PATH, navigation_outline_path, u.create(HTML.SVG, navigation_outline_svg)).parentNode;
                drawerItemHide = object.add(DRAWER.SECTION_VIEWS, EVENTS.HIDE_NAVIGATIONS, u.lang.hide_navigations, navigation_outline_drawer, function(){
                    main.users.forAllUsers(function (number, user) {
                        user.fire(EVENTS.HIDE_NAVIGATION);
                        drawerPopulate();
                    });
                });
                drawerPopulate();
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user && user != main.me && !user.views.navigation.show) {
                    var menuItemShow = object.add(MENU.SECTION_VIEWS, EVENTS.SHOW_NAVIGATION, u.lang.show_navigation, "navigation", function(){
                        user.fire(EVENTS.SHOW_NAVIGATION);
                        menuItemShow.hide();
                        drawerPopulate();
                    });
                    if(!main.me.location || !user.location) {
                        menuItemShow.hide();
                    }
                    if(main.me.location && user.location) {
                        var distance = google.maps.geometry.spherical.computeDistanceBetween(utils.latLng(main.me.location), utils.latLng(user.location))
                        if(distance < 20) {
                            menuItemShow.hide();
                        }
                    }
                } else if(user.views.navigation.show) {
                    navigation_outline_menu = navigation_outline_menu || u.create(HTML.PATH, navigation_outline_path, u.create(HTML.SVG, navigation_outline_svg)).parentNode;
                    object.add(MENU.SECTION_VIEWS, EVENTS.HIDE_NAVIGATION, u.lang.hide_navigation, navigation_outline_menu, function(){
                        user.fire(EVENTS.HIDE_NAVIGATION);
                        drawerPopulate();
                    });
                }
                if(user && user != main.me && user.location && main.me.location) {
                    object.add(MENU.SECTION_VIEWS, "gmap", u.lang.navigate_with_google_maps, "directions", function(){
                        var req = "https://maps.google.com/?saddr=" + main.me.location.coords.latitude + "," + main.me.location.coords.longitude + "&daddr=" + + user.location.coords.latitude + "," + user.location.coords.longitude;

                        window.open(req, "_blank");
                    });
                }
                break;
            case EVENTS.SHOW_NAVIGATION:
                installation = true;
                this.views.navigation.show = true;
                u.saveForContext("navigation:show:" + this.number, true);
                main.toast.show(u.lang.setting_up_direction, -1);
                update.call(this);
                break;
            case EVENTS.HIDE_NAVIGATION:
                removeView(this);
                break;
            default:
                break;
        }
        return true;
    }

    function createView(user){
        var view = {};
        view.user = user;

        view.show = u.loadForContext("navigation:show:" + user.number);

        if(view.show) {
            update.call(user);
        }
        drawerPopulate();
        return view;
    }

    function removeView(user){
        if(!user) return;
        main.toast.hide();
        user.views.navigation.show = false;
        u.saveForContext("navigation:show:" + user.number);
        if(user.views && user.views.navigation && user.views.navigation.track) {
            user.views.navigation.track.setMap(null);
            user.views.navigation.track = null;
            user.views.navigation.trackCenter.setMap(null);
            user.views.navigation.trackCenter = null;
            user.views.navigation.marker.setMap(null);
            user.views.navigation.marker = null;
            user.views.navigation.label.setMap(null);
            user.views.navigation.label = null;
        }
        drawerPopulate();
    }

    function drawerPopulate() {
        setTimeout(function(){
            drawerItemHide && drawerItemHide.hide();
            modeButtons && modeButtons.close();
            main.users.forAllUsersExceptMe(function (number, user) {
                if(user.views.navigation) {
                    if (user.views.navigation.show) {
                        drawerItemHide && drawerItemHide.show();
                        modeButtons && modeButtons.open();
                    } else {
    //                    drawerItemShow.show();
                    }
                }
            })
        },0);
    }

    function update() {

        if(!this || !this.views || !this.views.navigation || !this.views.navigation.show || !main.me.location || !this.location) return;

        var user = this;
        var req = "https://crossorigin.me/https://maps.googleapis.com/maps/api/directions/json?"
            + "origin=" + main.me.location.coords.latitude + "," + main.me.location.coords.longitude + "&"
            + "destination=" + this.location.coords.latitude + "," + this.location.coords.longitude + "&"
            + "alternatives=false&"
            + "mode=";

        var mode = u.load("navigation:mode") || NAVIGATION_MODE_DRIVING;

        switch (mode) {
            case NAVIGATION_MODE_DRIVING:
                req += "driving";
                break;
            case NAVIGATION_MODE_WALKING:
                req += "walking";
                break;
            case NAVIGATION_MODE_BICYCLING:
                req += "bicycling";
                break;
        }

        if(u.load("navigation:avoid_highways")) {
            req += "&avoid=highways";
        }
        if(u.load("navigation:avoid_tolls")) {
            req += "&avoid=tolls";
        }
        if(u.load("navigation:avoid_ferries")) {
            req += "&avoid=ferries";
        }

        console.log(type,req);

        u.getJSON(req).then(function(json){
            installation = false;
            updateTrack.call(user,json);
        }).catch(function(code,xhr){
            console.error(xhr);
            if(installation) {
                user.fire(EVENTS.HIDE_NAVIGATION);
                setTimeout(function(){
                    main.toast.show(u.lang.sorry_direction_request_was_failed);
                },0);
            }
        })

    }

    function updateTrack(o) {
        var user = this;
        if(!this || !this.views || !this.views.navigation || !this.views.navigation.show) return;

        if(!this.views.navigation.track) {
            createTrack.call(this);
        }

        if(!o.routes || !o.routes[0]) {
//            main.toast.show("Route ");
            console.error("NO ROUTE",o);
            return;
        }
        var text = o.routes[0].overview_polyline.points;
        var points = google.maps.geometry.encoding.decodePath(text);

        var distanceText = o.routes[0].legs[0].distance.text;
        var durationText = o.routes[0].legs[0].duration.text;
        var title = distanceText + "\n" + durationText;

        this.views.navigation.distance = o.routes[0].legs[0].distance.value;

        if (this.views.navigation.distance <= HIDE_TRACK_IF_DISTANCE_LESS_THAN) {
            this.views.navigation.previousDistance = this.views.navigationdistance;
                return;
        } else if (this.views.navigation.distance > SHOW_TRACK_IF_DISTANCE_BIGGER_THAN
            && this.views.navigation.previousDistance
            && this.views.navigation.previousDistance < SHOW_TRACK_IF_DISTANCE_BIGGER_THAN
            && this.views.navigation.track == null) {
                this.views.navigation.previousDistance = this.views.navigation.distance;
        } else if (this.views.navigation.distance > HIDE_TRACK_IF_DISTANCE_LESS_THAN
            && this.views.navigation.distance <= SHOW_TRACK_IF_DISTANCE_BIGGER_THAN
            && this.views.navigation.track == null) {
                this.views.navigation.previousDistance = this.views.navigation.distance;
                return;
        }
        this.views.navigation.previousDistance = this.views.navigation.distance;

        if (this.views.navigation.track != null) {
            this.views.navigation.track.setPath(points);
            this.views.navigation.trackCenter.setPath(points);

            var mePosition = utils.latLng(main.me.location);
            var userPosition = utils.latLng(this.location);


            var markerPosition = utils.findPoint(points);
            var bounds = utils.reduce(main.map.getBounds(), 0.8);

            if(!bounds.contains(markerPosition) && (bounds.contains(mePosition) || bounds.contains(userPosition))) {
                if(!bounds.contains(markerPosition)) {
                    var fract = 0.5;
                    while(!bounds.contains(markerPosition)) {
                        fract = fract + (bounds.contains(mePosition) ? -1 : +1) * .01;
                        if (fract < 0 || fract > 1) break;
                        markerPosition = utils.findPoint(points, fract);
                    }
                }
            }

            this.views.navigation.marker.setPosition(markerPosition);
//            var title = utils.formatLengthToLocale(google.maps.geometry.spherical.computeDistanceBetween(points[0], points[points.length-1]));
            title = this.properties.getDisplayName() + "\n" + title;
            this.views.navigation.label.set("text", title);

        }

        if(!modeButtons) {
            modeButtons = u.dialog({
                className: "navigation-mode",
                itemsClassName: "navigation-mode-items",
                items: [
                    {
                        type: HTML.DIV,
                        className: "navigation-mode-item",
                        innerHTML: "directions_car",
                        ondblclick: function(){modeDialog.open(NAVIGATION_MODE_DRIVING)},
                        onclick: function(){
                            if(modeDialog.preventClick) {
                                modeDialog.preventClick = false;
                            } else {
                                u.save("navigation:mode", NAVIGATION_MODE_DRIVING);
                                main.toast.show(u.lang.setting_up_direction, -1);
                                update.call(user);
                            }
                        },
                        onmousedown: function() {
                            this.longTapTask = setTimeout(function(){
                                modeDialog.preventClick = true;
                                modeDialog.open(NAVIGATION_MODE_DRIVING);
                            }, 500);
                        },
                        onmouseup: function() {
                            clearTimeout(this.longTapTask);
                        },
                        ontouchstart: function() {
                            this.longTapTask = setTimeout(function(){
                                modeDialog.preventClick = true;
                                modeDialog.open(NAVIGATION_MODE_DRIVING);
                            }, 500);
                        },
                        ontouchend: function() {
                            clearTimeout(this.longTapTask);
                        }
                    },
                    {
                        type: HTML.DIV,
                        className: "navigation-mode-item",
                        innerHTML: "directions_walk",
                        ondblclick: function(){modeDialog.open(NAVIGATION_MODE_WALKING)},
                        onclick: function(){
                            if(modeDialog.preventClick) {
                                modeDialog.preventClick = false;
                            } else {
                                u.save("navigation:mode", NAVIGATION_MODE_WALKING);
                                main.toast.show(u.lang.setting_up_direction, -1);
                                update.call(user);
                            }
                        },
                        onmousedown: function() {
                            this.longTapTask = setTimeout(function(){
                                modeDialog.preventClick = true;
                                modeDialog.open(NAVIGATION_MODE_WALKING);
                            }, 500);
                        },
                        onmouseup: function() {
                            clearTimeout(this.longTapTask);
                        },
                        ontouchstart: function() {
                            this.longTapTask = setTimeout(function(){
                                modeDialog.preventClick = true;
                                modeDialog.open(NAVIGATION_MODE_WALKING);
                            }, 500);
                        },
                        ontouchend: function() {
                            clearTimeout(this.longTapTask);
                        }
                    },
                    {
                        type: HTML.DIV,
                        className: "navigation-mode-item",
                        innerHTML: "directions_bike",
                        ondblclick: function(){modeDialog.open(NAVIGATION_MODE_BICYCLING)},
                        onclick: function(){
                            if(modeDialog.preventClick) {
                                modeDialog.preventClick = false;
                            } else {
                                u.save("navigation:mode", NAVIGATION_MODE_BICYCLING);
                                main.toast.show(u.lang.setting_up_direction, -1);
                                update.call(user);
                            }
                        },
                         onmousedown: function() {
                             this.longTapTask = setTimeout(function(){
                                modeDialog.preventClick = true;
                                 modeDialog.open(NAVIGATION_MODE_BICYCLING);
                             }, 500);
                         },
                         onmouseup: function() {
                             clearTimeout(this.longTapTask);
                         },
                         ontouchstart: function() {
                             this.longTapTask = setTimeout(function(){
                                modeDialog.preventClick = true;
                                 modeDialog.open(NAVIGATION_MODE_BICYCLING);
                             }, 500);
                         },
                         ontouchend: function() {
                             clearTimeout(this.longTapTask);
                         }
                   },
                ]
            }, main.right);

            modeDialog = u.dialog({
                title: u.lang.navigation_options,
                className: "navigations-options-dialog",
                items: [
                    { type: HTML.HIDDEN },
                    { type: HTML.CHECKBOX, label: u.lang.avoid_highways },
                    { type: HTML.CHECKBOX, label: u.lang.avoid_tolls },
                    { type: HTML.CHECKBOX, label: u.lang.avoid_ferries }
                ],
                positive: {
                    label: u.lang.ok,
                    onclick: function(items) {
                        u.save("navigation:mode", items[0].value);
                        switch(items[0].value) {
                            case NAVIGATION_MODE_DRIVING:
                                u.save("navigation:avoid_highways", items[1].checked);
                                u.save("navigation:avoid_tolls", items[2].checked);
                                u.save("navigation:avoid_ferries", items[3].checked);
                                break;
                            case NAVIGATION_MODE_WALKING:
                                u.save("navigation:avoid_ferries", items[3].checked);
                                break;
                            case NAVIGATION_MODE_BICYCLING:
                                u.save("navigation:avoid_ferries", items[3].checked);
                                break;
                        }
                        main.toast.show(u.lang.setting_up_direction, -1);
                        update.call(user);
                    }
                },
                negative: {
                    label: u.lang.cancel
                },
                onopen: function(items,mode) {
                    items[0].value = mode;
                    switch(mode) {
                        case NAVIGATION_MODE_DRIVING:
                            items[1].checked = u.load("navigation:avoid_highways");
                            items[2].checked = u.load("navigation:avoid_tolls");
                            items[3].checked = u.load("navigation:avoid_ferries");
                            modeDialog.itemsLayout.childNodes[1].show();
                            modeDialog.itemsLayout.childNodes[2].show();
                            break;
                        case NAVIGATION_MODE_WALKING:
                            items[3].checked = u.load("navigation:avoid_ferries");
                            modeDialog.itemsLayout.childNodes[1].hide();
                            modeDialog.itemsLayout.childNodes[2].hide();
                            break;
                        case NAVIGATION_MODE_BICYCLING:
                            items[3].checked = u.load("navigation:avoid_ferries");
                            modeDialog.itemsLayout.childNodes[1].hide();
                            modeDialog.itemsLayout.childNodes[2].hide();
                            break;
                    }
                }
            }, main.right);
        }

        main.toast.hide();

        modeButtons.open();

        modeButtons.itemsLayout.childNodes[0].classList.remove("navigation-mode-item-selected");
        modeButtons.itemsLayout.childNodes[1].classList.remove("navigation-mode-item-selected");
        modeButtons.itemsLayout.childNodes[2].classList.remove("navigation-mode-item-selected");
        var mode = u.load("navigation:mode") || NAVIGATION_MODE_DRIVING;
        switch (mode) {
            case NAVIGATION_MODE_DRIVING:
                modeButtons.itemsLayout.childNodes[0].classList.add("navigation-mode-item-selected");
                break;
            case NAVIGATION_MODE_WALKING:
                modeButtons.itemsLayout.childNodes[1].classList.add("navigation-mode-item-selected");
                break;
            case NAVIGATION_MODE_BICYCLING:
                modeButtons.itemsLayout.childNodes[2].classList.add("navigation-mode-item-selected");
                break;
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

        var points = [utils.latLng(main.me.location), utils.latLng(this.location)];

        this.views.navigation.track = new google.maps.Polyline({
            geodesic: true,
            strokeColor: this.properties.color,
            strokeOpacity: 0.6,
            strokeWeight: 15,
            zIndex: 100,
            map: main.map
        });
        this.views.navigation.trackCenter = new google.maps.Polyline({
            geodesic: true,
            strokeColor: "white",
//            strokeOpacity: 0.6,
            strokeWeight: 5,
            zIndex: 100,
            map: main.map
        });
        this.views.navigation.marker = new google.maps.Marker({
            map: main.map,
            visible: false
        });
        this.views.navigation.label = new utils.label({
            map:main.map,
            className:"navigation-label",
            style: {backgroundColor:this.properties.color},
        });
        this.views.navigation.label.bindTo("position", this.views.navigation.marker, "position");

    }

    function onChangeLocation(location) {
        if(this == main.me) {
            main.users.forAllUsersExceptMe(function(number,user){
                if(user.views.navigation && user.views.navigation.show) {
                    update.call(user);
                }
            })
        } else if(this.views.navigation.show && location) {
            update.call(this);
        }
    }


    function options(){
        return {
            id: "navigation",
            title: u.lang.navigation,
            categories: [
                {
                    id: "navigation:options",
                    title: u.lang.navigation_options,
                    items: [
                        {
                            id:"navigation:avoid_highways",
                            type: HTML.CHECKBOX,
                            label: u.lang.avoid_highways,
                            default: u.load("navigation:avoid_highways") || "",
                            onaccept: function(e, event) {
                                u.save("navigation:avoid_highways", this.checked);
                                main.me.onChangeLocation(main.me.location);
//                                console.log("navigation:avoid_highways:",this.value);
                            },
                        },
                        {
                            id:"navigation:avoid_tolls",
                            type: HTML.CHECKBOX,
                            label: u.lang.avoid_tolls,
                            default: u.load("navigation:avoid_tolls") || "",
                            onaccept: function(e, event) {
                                u.save("navigation:avoid_tolls", this.checked);
                                main.me.onChangeLocation(main.me.location);
//                                console.log("navigation:avoid_tolls:",this.value);
                            },
                        },
                        {
                            id:"navigation:avoid_ferries",
                            type: HTML.CHECKBOX,
                            label: u.lang.avoid_ferries,
                            default: u.load("navigation:avoid_ferries") || "",
                            onaccept: function(e, event) {
                                u.save("navigation:avoid_ferries", this.checked);
                                main.me.onChangeLocation(main.me.location);
//                                console.log("navigation:avoid_ferries:",this.value);
                            },
                        }
                    ]
                }
            ]
        }
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        createView:createView,
        removeView:removeView,
        onChangeLocation:onChangeLocation,
        options:options,
    }
}