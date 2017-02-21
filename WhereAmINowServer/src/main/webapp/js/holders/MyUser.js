/**
 * Created by tujger on 2/12/17.
 */
function MyUser(main) {

    function fire(EVENT,object) {
        // console.log("EVENT",EVENT, object);

        for(var i in this.views) {
            if(main.holders[i] && main.holders[i].dependsOnUser && main.holders[i].onEvent) {
                if(!main.holders[i].onEvent.call(this, EVENT, object)) break;
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
                        console.error(i,e.message);
                    }
                }
            }
        }
    }

    function addLocation(location) {
        this.locations.push(location);
        this.location = location;

    }

    return {
        locations: [],
        views: {},
        location: null,
        user: null,
        properties: null,
        fire:fire,
        createViews:createViews,
        addLocation:addLocation,
    }
}