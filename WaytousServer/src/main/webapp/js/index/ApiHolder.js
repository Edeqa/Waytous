/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 7/23/17.
 */

EVENTS.API = "api";

function ApiHolder(main) {

    this.type = "api";
    this.category = "docs";
    this.title = u.lang.api;
    this.menu = u.lang.api;
    this.icon = "extension";

    this.start = function() {
        console.log("INDEX API");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.API:
                console.log("INDEX API");

                u.progress.show(u.lang.loading);

                u.byId("content").innerHTML = u.lang.api_body.innerHTML;
                u.byId("content").classList.add("content-api");
                u.byId("content").parentNode.scrollTop = 0;
                u.progress.hide();
                break;
        }
        return true;
    }

}