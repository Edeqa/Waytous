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
                drawerItemShow = object.add(DRAWER.SECTION_VIEWS,EVENTS.SHOW_DISTANCE,"Show distances","settings_ethernet",function(){
                    main.users.forAllUsers(function (number, user) {
                        user.fire(EVENTS.SHOW_DISTANCE);
                        drawerPopulate();
                    });
                });
                drawerItemHide = object.add(DRAWER.SECTION_VIEWS,EVENTS.HIDE_DISTANCE,"Hide distances","code",function(){
                    main.users.forAllUsers(function (number, user) {
                        user.fire(EVENTS.HIDE_DISTANCE);
                        drawerPopulate();
                    });
                });
                drawerPopulate();
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user && user.location && !user.views.distance.show) {
                    object.add(MENU.SECTION_VIEWS,EVENTS.SHOW_DISTANCE,"Show distance","settings_ethernet",function(){
                        user.fire(EVENTS.SHOW_DISTANCE);
                        drawerPopulate();
                    });
                } else if(user.views.distance.show) {
                    object.add(MENU.SECTION_VIEWS,EVENTS.HIDE_DISTANCE,"Hide distance","code",function(){
                        user.fire(EVENTS.HIDE_DISTANCE);
                        drawerPopulate();
                    });
                }
                break;
            case EVENTS.SHOW_DISTANCE:
                this.views.distance.show = true;
                u.save("distance:show:" + this.number, true);
                show.call(this);
                break;
            case EVENTS.HIDE_DISTANCE:
                this.views.distance.show = false;
                u.save("distance:show:" + this.number);
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

        view.show = u.load("distance:show:" + myUser.number);

        if(view.show) {
            show.call(myUser);
        }
        drawerPopulate();
        return view;
        // console.log("SAMPLECREATEVIEW",user);
    }

    function drawerPopulate() {
        drawerItemHide.classList.add("hidden");
        drawerItemShow.classList.add("hidden");
        main.users.forAllUsers(function (number, user) {
            if(user.views.distance) {
                if (user.views.distance.show) {
                    drawerItemHide.classList.remove("hidden");
                } else {
                    drawerItemShow.classList.remove("hidden");
                }
            }
        })

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

    var help = {
        title: "Distances",
        1: {
            title: "Article 1",
            body: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras pellentesque aliquam tellus, quis finibus odio faucibus sed. Nunc nec dictum ipsum, a efficitur sem. Nullam suscipit quis neque in cursus. Etiam tempus imperdiet scelerisque. Integer ut nisi at est varius rutrum quis eget urna. Morbi blandit vehicula laoreet. Curabitur tincidunt turpis dui, at venenatis risus volutpat et. Donec cursus molestie ligula eu convallis. Curabitur sed quam id ex tristique ultricies. Duis id felis eget massa venenatis vehicula. Aenean eget varius dui. "
        },
        2: {
            title: "Article 2",
            body: "Maecenas vel mauris sit amet erat porta feugiat. Donec facilisis viverra enim, congue tristique lacus pharetra eleifend. Curabitur ac convallis quam. Sed sollicitudin eros vel elit tincidunt tincidunt. Nulla a sapien eget dolor ultrices ullamcorper non vel velit. In faucibus eros in dolor mollis ultricies. Interdum et malesuada fames ac ante ipsum primis in faucibus. Suspendisse potenti. Ut ultrices tellus quis odio condimentum mattis. Cras nec dui ut purus ultrices pharetra sed non lacus. Phasellus pellentesque, turpis nec placerat volutpat, odio metus sollicitudin nunc, ac sollicitudin magna risus id nisl. In nisl sem, venenatis ut tortor vitae, vestibulum dictum turpis."
        },
        3: {
            title: "Article 3",
            body: "Integer quis nulla at dui consequat condimentum. Curabitur laoreet augue molestie feugiat egestas. Fusce quis fermentum augue, nec venenatis magna. Phasellus feugiat diam vulputate efficitur condimentum. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nullam tortor libero, posuere sit amet sem pharetra, lobortis mollis dui. Quisque auctor sem id odio imperdiet, interdum cursus nulla maximus. Praesent facilisis, quam eget lobortis gravida, leo lectus rhoncus nunc, nec varius ex turpis vel mi. Nam nec orci felis. Sed pharetra dui id tellus sollicitudin euismod sit amet eu elit. Aenean congue non enim a porta. Vestibulum ultricies mattis ipsum eget tristique. Curabitur ut risus urna. Maecenas eget aliquet massa, a rhoncus nisi. Duis et luctus arcu. Fusce in tortor orci. <p>Phasellus et ipsum bibendum, lacinia quam ac, rhoncus felis. Fusce augue tortor, cursus at pharetra quis, tristique vitae sapien. Vivamus sodales elementum eros sit amet tincidunt. Morbi sed felis quis enim tempus consequat eu non lacus. Quisque ac justo enim. Integer fermentum accumsan magna at varius. Duis in diam scelerisque sapien ultricies volutpat. "
        },
        4: {
            title: "Article 4",
            body: "Sed commodo gravida rhoncus. Nulla ac neque mauris. Nulla efficitur, ligula commodo posuere vehicula, enim purus elementum elit, eget pellentesque nibh augue non dolor. Nulla accumsan nisi vitae molestie mattis. Duis tincidunt tellus id massa porta consequat at ut nisl. Mauris id erat imperdiet, pulvinar ligula venenatis, egestas purus. Aenean faucibus, est molestie convallis maximus, ipsum risus molestie sapien, vel congue lorem sapien sit amet lorem. Maecenas tincidunt lorem eget vehicula molestie. Pellentesque sit amet convallis arcu. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nunc vehicula facilisis blandit."
        }
    };


    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
        dependsOnUser:true,
        createView:createView,
        onChangeLocation:onChangeLocation,
        help:help,
    }
}