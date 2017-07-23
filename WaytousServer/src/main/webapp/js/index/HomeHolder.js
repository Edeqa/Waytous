/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 4/25/17.
 */

EVENTS.HOME = "home";

function HomeHolder(main) {

    this.type = "home";
    this.category = "main";
    this.title = u.lang.home;
    this.menu = u.lang.home;
    this.icon = "home";

    this.start = function() {
        console.log("INDEX HOME");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.HOME:
                console.log("INDEX HOME");

                u.progress.show(u.lang.loading);

                u.byId("content").innerHTML = u.lang.home.innerHTML;
                u.byId("content").classList.add("content-home");
                u.byId("content").parentNode.scrollTop = 0;
                u.progress.hide();
                break;
        }
        return true;
    }

}