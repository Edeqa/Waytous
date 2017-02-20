/**
 * Created by tujger on 2/12/17.
 */
function MyUser(main) {

    var locations = [];
    var entities = {};
    var views = {};
    var location;
    var user;
    var properties;


    var fire = function(EVENT,object) {
        // console.log("EVENT",EVENT, object);

        for(var i in this.views) {
            if(main.holders[i] && main.holders[i].dependsOnUser && main.holders[i].onEvent) {
                if(!main.holders[i].onEvent.call(this, EVENT, object)) break;
            }
        }
    }

    var createViews = function() {
        if(this.number) {
            for (var i in main.holders) {
                if (main.holders[i] && main.holders[i].dependsOnUser && !views[i] && main.holders[i].createView) {
                    try {
                        var view = main.holders[i].createView(this);
                        if (view) views[i] = view;
                    } catch (e) {
                        console.error(i,e.message);
                    }
                }
            }
        }
    }

    var getLocation = function(){
        return location;
    }

    return {
        user:user,
        fire:fire,
        createViews:createViews,
        views:views,
        getLocation:getLocation,
    }
}
