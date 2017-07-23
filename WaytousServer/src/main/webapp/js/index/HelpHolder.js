/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 7/23/17.
 */

EVENTS.HELP = "help";

function HelpHolder(main) {

    this.type = "help";
    this.category = "about";
    this.title = u.lang.help;
    this.menu = u.lang.help;
    this.icon = "help";

    this.start = function() {
        console.log("INDEX HELP");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.HELP:
                console.log("INDEX HELP");

                u.progress.show(u.lang.loading);

                u.byId("content").innerHTML = u.lang.help.innerHTML;
                u.byId("content").classList.add("content-help");
                u.byId("content").parentNode.scrollTop = 0;
                u.progress.hide();
                break;
        }
        return true;
    }

}