/**
 * Created by tujger on 2/12/17.
 */
function MyUser(main) {

    function fire(EVENT,object) {
        var self = this;
        setTimeout(function(){
            for(var i in self.views) {
                if(main.holders[i] && main.holders[i].dependsOnUser && main.holders[i].onEvent) {
    //                if(i == "properties") continue;
                    try {
                        if (!main.holders[i].onEvent.call(self, EVENT, object)) break;
                    } catch(e) {
                        console.error(i,EVENT,e);
                    }
                }
            }
        }, 0);
    }

    function createViews() {
        var user = this;
//        setTimeout(function(){
            if(user.number) {
                for (var i in main.holders) {
                    if (main.holders[i] && main.holders[i].dependsOnUser && !user.views[i] && main.holders[i].createView) {
                        try {
                            var view = main.holders[i].createView(user);
                            if (view) user.views[i] = view;
                        } catch (e) {
                            console.error(i,e);
                        }
                    }
                }
            }
//        }, 0);
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
        var user = this;
        setTimeout(function(){
            for(var i in user.views) {
                if(main.holders[i] && main.holders[i].onChangeLocation) {
                    try {
                        main.holders[i].onChangeLocation.call(user, user.location);
                    } catch(e) {
                        console.error(i,e);
                    }
                }
            }
        }, 0);
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