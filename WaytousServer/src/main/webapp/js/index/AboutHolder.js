/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 4/24/17.
 */

EVENTS.ABOUT = "about";

function AboutHolder(main) {

    this.type = "about";
    this.category = "about";
    this.title = u.lang.about;
    this.menu = u.lang.about;
    this.icon = "info_outline";

    this.start = function() {
    }

    this.onEvent = function(event, object) {

        switch(event) {
            case EVENTS.ABOUT:
                console.log("INDEX ABOUT");

                u.progress.show(u.lang.loading);
                u.get("/texts/lorem.txt").then(function(xhr){
        //            u.clear(main.content);

                    u.byId("content").innerHTML = xhr.response;
                    u.byId("content").classList.add("content-about");
                    u.byId("content").parentNode.scrollTop = 0;
                    u.progress.hide();
                });
                break;
        }

    }

}