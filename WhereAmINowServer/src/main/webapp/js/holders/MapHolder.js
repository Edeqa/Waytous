/**
 * Created 2/9/17.
 */
function MapHolder(main) {

    EVENTS.REQUEST_MODE_DAY = "request_mode_day";
    EVENTS.REQUEST_MODE_NIGHT = "request_mode_night";
    EVENTS.REQUEST_MODE_NORMAL = "request_mode_normal";
    EVENTS.REQUEST_MODE_SATELLITE = "request_mode_satellite";
    EVENTS.REQUEST_MODE_TERRAIN = "request_mode_terrain";
    EVENTS.REQUEST_MODE_TRAFFIC = "request_mode_traffic";
    EVENTS.REQUEST_MODE_TRANSIT = "request_mode_transit";
    EVENTS.REQUEST_MODE_BIKE = "request_mode_bike";

    var map;
    var trafficLayer;
    var transitLayer;
    var bikeLayer;

    u.create("div", {id: "map"}, main.right);

    function start() {
        u.create("script", {
            src: "https://maps.googleapis.com/maps/api/js?key=AIzaSyCRH9g5rmQdvShE4mI2czumO17u_hwUF8Q&callback=initMap",
            async: "",
            defer: ""
        }, main.right);
    }

    window.initMap = function() {
        // Create a map object and specify the DOM element for display.
        map = new google.maps.Map(document.getElementById("map"), {
            center: {lat: 38.93421936035156, lng: -77.35877990722656},
            scrollwheel: true,
            zoom: 15,
            panControl: true,
            zoomControl: true,
            mapTypeControl: true,
            scaleControl: true,
            streetViewControl: true,
            overviewMapControl: true,
            rotateControl: true,
        });
        main.map = map;
        main.map.addListener("zoom_changed", function() {
            main.fire(EVENTS.CAMERA_ZOOM, map.getZoom());
        });
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                object.add(8,EVENTS.REQUEST_MODE_TRAFFIC,"Traffic","traffic",function(){
                    main.fire(EVENTS.REQUEST_MODE_TRAFFIC);
                });
                object.add(8,EVENTS.REQUEST_MODE_TRANSIT,"Transit","directions_transit",function(){
                    main.fire(EVENTS.REQUEST_MODE_TRANSIT);
                });
                object.add(8,EVENTS.REQUEST_MODE_BIKE,"Bicycle","directions_bike",function(){
                    main.fire(EVENTS.REQUEST_MODE_BIKE);
                });
                object.add(8,EVENTS.REQUEST_MODE_SATELLITE,"Satellite","satellite",function(){
                    main.fire(EVENTS.REQUEST_MODE_SATELLITE);
                });
                object.add(8,EVENTS.REQUEST_MODE_TERRAIN,"Terrain","terrain",function(){
                    main.fire(EVENTS.REQUEST_MODE_TERRAIN);
                });
                break;
            case EVENTS.REQUEST_MODE_TRAFFIC:
                if(trafficLayer){
                    trafficLayer.setMap(null);
                    trafficLayer = null;
                } else {
                    if(transitLayer) {
                        transitLayer.setMap(null);
                        transitLayer = null;
                    }
                    if(bikeLayer){
                        bikeLayer.setMap(null);
                        bikeLayer = null;
                    }
                    trafficLayer = new google.maps.TrafficLayer();
                    trafficLayer.setMap(map);
                }
                break;
            case EVENTS.REQUEST_MODE_TRANSIT:
                if(transitLayer){
                    transitLayer.setMap(null);
                    transitLayer = null;
                } else {
                    if(trafficLayer) {
                        trafficLayer.setMap(null);
                        trafficLayer = null;
                    }
                    if(bikeLayer){
                        bikeLayer.setMap(null);
                        bikeLayer = null;
                    }

                    transitLayer = new google.maps.TransitLayer();
                    transitLayer.setMap(map);
                }
                break;
            case EVENTS.REQUEST_MODE_BIKE:
                if(bikeLayer){
                    bikeLayer.setMap(null);
                    bikeLayer = null;
                } else {
                    if(transitLayer) {
                        transitLayer.setMap(null);
                        transitLayer = null;
                    }
                    if(trafficLayer){
                        trafficLayer.setMap(null);
                        trafficLayer = null;
                    }
                    bikeLayer = new google.maps.BicyclingLayer();
                    bikeLayer.setMap(map);
                }
                break;
            case EVENTS.REQUEST_MODE_NORMAL:
                if(map){
                    map.setMapTypeId(google.maps.MapTypeId.ROADMAP);
                }
                // State.getInstance().getPropertiesHolder().saveFor(TYPE, null);
                break;
            case EVENTS.REQUEST_MODE_SATELLITE:
                if(map && map.getMapTypeId() != google.maps.MapTypeId.SATELLITE){
                    map.setMapTypeId(google.maps.MapTypeId.SATELLITE);
                    // State.getInstance().getPropertiesHolder().saveFor(TYPE, GoogleMap.MAP_TYPE_SATELLITE);
                } else {
                    main.fire(EVENTS.REQUEST_MODE_NORMAL);
                }
                break;
            case EVENTS.REQUEST_MODE_TERRAIN:
                if(map && map.getMapTypeId() != google.maps.MapTypeId.TERRAIN){
                    map.setMapTypeId(google.maps.MapTypeId.TERRAIN);
                    // State.getInstance().getPropertiesHolder().saveFor(TYPE, GoogleMap.MAP_TYPE_SATELLITE);
                } else {
                    main.fire(EVENTS.REQUEST_MODE_NORMAL);
                }
                break;
            default:
                break;
        }
        return true;
    }

    return {
        type:"map",
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
        map:map,
    }
}