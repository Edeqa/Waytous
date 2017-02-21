/**
 * Created 2/9/17.
 */
function MarkerHolder(main) {

    var type = "marker";

    function start() {
        // console.log("MARKERHOLDER",main);
    }

    function createView(user){
        // console.log("CREATEMARKER",user);

        var view = new google.maps.Marker({
            position: u.latLng(user.location),
            title: "Hello World!"
        });
        return view;
    }

    function onEvent(EVENT,object){
        // console.log(EVENT)
        switch (EVENT){
            case EVENTS.MAKE_ACTIVE:
                console.log(EVENT,this.properties.name,object);
                this.views.marker.setMap(main.map);
                break;
            case EVENTS.MAKE_INACTIVE:
                console.log(EVENT,this.properties.name,object);
                this.views.marker.setMap(null);
                break;
        }
        return true;
    }



    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        dependsOnUser:true,
        onEvent:onEvent,
        createView:createView,
    }
}