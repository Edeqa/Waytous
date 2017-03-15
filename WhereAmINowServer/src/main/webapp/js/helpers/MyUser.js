/**
 * Created by tujger on 2/12/17.
 */
function MyUser(main) {

    function fire(EVENT,object) {
        // console.log("EVENT",EVENT, object);

//        if(this.views.properties) main.holders.properties.onEvent.call(this, EVENT, object);
        for(var i in this.views) {
            if(main.holders[i] && main.holders[i].dependsOnUser && main.holders[i].onEvent) {
//                if(i == "properties") continue;
                try {
                    if (!main.holders[i].onEvent.call(this, EVENT, object)) break;
                } catch(e) {
                    console.error(i,EVENT,e);
                }
            }
        }
    }

    function createViews() {
        if(this.number) {
            for (var i in main.holders) {
                if (main.holders[i] && main.holders[i].dependsOnUser && !this.views[i] && main.holders[i].createView) {
                    try {
                        var view = main.holders[i].createView(this);
                        if (view) this.views[i] = view;
                    } catch (e) {
                        console.error(i,e);
                    }
                }
            }
        }
    }

    function removeViews() {
        console.log("REMOVEVIEWS:"+this.number);
    }

    function addLocation(location) {
        this.locations.push(location);
        this.location = location;

        this.onChangeLocation();
    }

    function onChangeLocation() {
        for(var i in this.views) {
            if(main.holders[i] && main.holders[i].onChangeLocation) {
                try {
                    main.holders[i].onChangeLocation.call(this, this.location);
                } catch(e) {
                    console.error(i,e);
                }
            }
        }
    }

    return {
        locations: [],
        views: {},
        location: null,
        user: null,
        properties: null,
        fire:fire,
        createViews:createViews,
        removeViews:removeViews,
        addLocation:addLocation,
        onChangeLocation:onChangeLocation,
    }
}