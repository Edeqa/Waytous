/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 7/23/17.
 */

EVENTS.SUPPORT = "support";

function SupportHolder(main) {

    this.type = "support";
    this.category = "about";
    this.title = u.lang.support;
    this.menu = u.lang.support;
    this.icon = "live_help";

    this.start = function() {
        console.log("INDEX SUPPORT");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.SUPPORT:
                console.log("INDEX SUPPORT");

                u.progress.show(u.lang.loading);

                u.byId("content").innerHTML = u.lang.support_body.innerHTML;
                u.byId("content").classList.add("content-support");
                u.byId("content").parentNode.scrollTop = 0;
                u.progress.hide();
                break;
        }
        return true;
    }

}