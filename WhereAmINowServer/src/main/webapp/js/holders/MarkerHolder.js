/**
 * Created 2/9/17.
 */
function MarkerHolder(main) {

    var type = "marker";

    EVENTS.MARKER_CLICK = "marker_click";

    function start() {
        // console.log("MARKERHOLDER",main);
    }

    function createView(user){
        // console.log("CREATEMARKER",user.location);

        // var h=document.createElement('a');
        // var t=document.createTextNode('Hello World');
        // h.appendChild(t);
        // document.body.appendChild(h);
        if(!user.properties) return;

        var marker = new google.maps.Marker({
            position: u.latLng(user.location),
            title: user.properties ? user.properties.getDisplayName() : "",
            icon:{
                path: 'M0 12 c 0 -11 9 -20 20 -20 c 11 0 20 9 20 20 c 0 11 -9 20 -20 20 c -11 0 -20 -9 -20 -20 M 20 2 l-7.5 18.29 l0.71,0.71 l 6.79 -3 l6.79,3 0.71,-0.71 z',
                fillColor: user.properties ? user.properties.color : "blue",
                fillOpacity: 0.7,
                scale: 1.2,
                strokeColor: "white",
                strokeOpacity: 0.6,
                strokeWeight: 2,
                // size: new google.maps.Size(32, 32),
                // origin: new google.maps.Point(0, 0),
                anchor: new google.maps.Point(40/2, 40/2)
            },
            optimized:false,
        });
        marker.addListener(HTML.CLICK, function(e){
            console.log("MARKERCLICK",this,user.properties.getDisplayName(),e);
            user.fire(EVENTS.MARKER_CLICK, marker);
        });

        // if(icon && icon[0]) {
        //     marker.setIcon(icon[0]);
        // }

        function setBearing(angle){
            icon[0].style.transform = "rotate("+angle+"deg)";
        }

        function setPosition(location) {
            console.log(this,marker,location);
            this.marker.setPosition(location);
        }
        if(user.properties.active) {
            marker.setMap(main.map);
        }

        return {
            marker:marker,
            setBearing:setBearing,
            setPosition:setPosition,
        };
    }


    function onEvent(EVENT,object){
        // console.log(EVENT)
        switch (EVENT){
            case EVENTS.MAKE_ACTIVE:
//                console.log(EVENT,this.properties.name,object);
                    // this.views.marker.icon = makeIcon(this);
                this.views.marker.marker.setMap(main.map);
                break;
            case EVENTS.MAKE_INACTIVE:
//                console.log(EVENT,this.properties.name,object);
                this.views.marker.marker.setMap(null);
                break;
            case EVENTS.CHANGE_NAME:
                this.views.marker.marker;
                break;
        }
        return true;
    }

    function onChangeLocation(location) {
        // var start = this.views.marker.
        if(this.locations && this.locations.length >1) {
            var marker = this.views.marker.marker;
            var prev = this.locations[this.locations.length-2];

            var startPosition = u.latLng(prev);
            var finalPosition = u.latLng(location);

            var startRotation = prev.coords.heading;
            var finalRotation = location.coords.heading;

            u.smoothInterpolated(1000, function(time,value) {

                var currentPosition = new google.maps.LatLng(
                    startPosition.lat()*(1-time) + finalPosition.lat()*time,
                    startPosition.lng()*(1-time) + finalPosition.lng()*time
                );
                var rot = startRotation*(1-time) + finalRotation*time;

                var icon = marker.getIcon();
                icon.rotation = rot;
                marker.setIcon(icon);
                marker.setPosition(currentPosition);

            });

        } else {
            if (location && location.coords && location.coords.heading) {
                var icon = this.views.marker.marker.getIcon();
                icon.rotation = location.coords.heading;
                this.views.marker.marker.setIcon(icon);
            }
            this.views.marker.marker.setPosition(u.latLng(location));
        }
    }

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        dependsOnUser:true,
        onEvent:onEvent,
        createView:createView,
        onChangeLocation:onChangeLocation,
    }
}