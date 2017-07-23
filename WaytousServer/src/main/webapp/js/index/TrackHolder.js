/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 7/23/17.
 */

EVENTS.TRACK = "track";

function TrackHolder(main) {

    this.type = "track";
    this.category = "main";
    this.title = u.lang.track;
    this.menu = u.lang.track;

    var drawerItemNewIconSvg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"0 0 24 24",
        version:"1.1",
        className: "menu-item"
    };
    var drawerItemNewIconPath = {
        xmlns:"http://www.w3.org/2000/svg",
        fill:"darkslategray",
        d: "M10,2l-6.5,15 0.5,0.5L9,15L12.29,7.45z M14,5.5l-6.5,15 0.5,0.5 6,-3l6,3 0.5,-0.5z"
    };
    this.icon =  u.create(HTML.PATH, drawerItemNewIconPath, u.create(HTML.SVG, drawerItemNewIconSvg)).parentNode;

    this.start = function() {
        console.log("INDEX TRACK");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.TRACK:
                console.log("INDEX TRACK");
                u.progress.show(u.lang.loading.innerHTML);
                u.get("/texts/track.txt").then(function(xhr){
                    //            u.clear(main.content);

                    u.byId("content").innerHTML = xhr.response;
                    u.byId("content").classList.add("content-track");
                    u.byId("content").parentNode.scrollTop = 0;
                    u.progress.hide();
                }).catch(function(error, json) {
                    u.byId("content").innerHTML = "Error";
                    u.byId("content").classList.add("content-feedback");
                    u.byId("content").parentNode.scrollTop = 0;
                    u.progress.hide();
                });
                break;
        }
        return true;
    }

}