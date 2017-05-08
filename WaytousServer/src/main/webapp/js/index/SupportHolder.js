/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 4/24/17.
 */

function SupportHolder(main) {

    function start() {
        console.log("INDEX SUPPORT")
    }


    return {
        type: "support",
        category: "about",
        title: "Support",
        start:start,
        menu: "Support",
        icon: "add"
    }
}