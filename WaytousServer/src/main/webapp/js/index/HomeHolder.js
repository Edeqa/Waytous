/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 4/25/17.
 */

function HomeHolder(main) {

    function start() {
        console.log("INDEX HOME");

    }


    return {
        type: "home",
        category: "main",
        title: "Home",
        start:start,
        menu: "Home",
        icon: "home"
    }
}