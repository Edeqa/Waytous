/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 2/12/17.
 */
function MyUser(main) {

    function fire(EVENT,object) {
        var user = this;
        setTimeout(function(){
            main.eventBus.chain(function(holder){
                if(user.views[holder.type] && holder.onEvent) {
                    return holder.onEvent.call(user, EVENT, object);
                }
            });
        }, 0);
    }

    function createViews() {
        var user = this;
        if(user.number != undefined) {
            main.eventBus.chain(function(holder){
                if (holder.createView && !user.views[holder.type]) {
                    try {
                        var view = holder.createView(user);
                        if (view) user.views[holder.type] = view;
                    } catch (e) {
                        console.error(holder.type,e);
                    }
                }
            });

        }
    }

    function removeViews() {
        var user = this;
        if(user.number != undefined) {
            main.eventBus.chain(function(holder){
                if(holder.removeView) holder.removeView(user);
            });
        }
    }

    function addLocation(location) {
        this.locations.push(location);
        this.location = location;
        this.onChangeLocation();
    }

    function onChangeLocation() {
        var user = this;
        user.changed = new Date().getTime();
//        setTimeout(function(){
            main.eventBus.chain(function(holder){
                if(user.views[holder.type] && holder.onChangeLocation) holder.onChangeLocation.call(user, user.location);
            });
//        }, 0);
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