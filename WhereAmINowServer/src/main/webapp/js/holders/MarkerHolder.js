/**
 * Created 2/9/17.
 */
function MarkerHolder(main) {

    var type = "marker";

    function start() {
        // console.log("MARKERHOLDER",main);
    }

    function createView(user){
        console.log("CREATEMARKER",user);

        // var h=document.createElement('a');
        // var t=document.createTextNode('Hello World');
        // h.appendChild(t);
        // document.body.appendChild(h);

        var marker = new google.maps.Marker({
            position: u.latLng(user.location),
            title: "Hello World!",
            icon:{
                path: 'M0 12c0 -9 7 -16 16 -16c 9 0 16 7 16 16c 0 9 -7 16 -16 16c -9 0 -16 -7 -16 -16M 16 2 l-7.5 18.29 l0.71,0.71 l 6.79 -3 l6.79,3 0.71,-0.71z',
                fillColor: 'blue',
                fillOpacity: 0.6,
                scale: 1.5,
                strokeColor: 'transparent',
                strokeWeight: 0,
                size: new google.maps.Size(32, 32),
                origin: new google.maps.Point(0, 0),
                anchor: new google.maps.Point(32/2, 32/2)
            },
            optimized:false,
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
                console.log(EVENT,this.properties.name,object);
                    // this.views.marker.icon = makeIcon(this);
                this.views.marker.marker.setMap(main.map);
                break;
            case EVENTS.MAKE_INACTIVE:
                console.log(EVENT,this.properties.name,object);
                this.views.marker.marker.setMap(null);
                break;
        }
        return true;
    }

    function onChangeLocation(location) {
        console.log("MARKER");
        this.views.marker.marker.setPosition(u.latLng(location));
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