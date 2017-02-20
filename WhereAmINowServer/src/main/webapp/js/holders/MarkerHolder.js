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

        var view = user.views[type] = new google.maps.Marker({
            position: u.latLng(user.getLocation()),
            map: main.map,
            title: "Hello World!"
        });
        return view;
    }



    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        dependsOnUser:true,
        createView:createView,
    }
}