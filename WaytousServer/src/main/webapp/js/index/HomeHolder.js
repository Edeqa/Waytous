/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 4/25/17.
 */

function HomeHolder(main) {

    this.type = "home";
    this.category = "main";
    this.title = u.lang.home;
    this.menu = u.lang.home;
    this.icon = "home";

    this.start = function() {
        console.log("INDEX HOME");

    }

    this.onEvent = function(event, object) {
        return true;
    }

}