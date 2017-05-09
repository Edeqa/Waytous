/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 4/24/17.
 */

function AboutHolder(main) {

    function start() {
        console.log("INDEX ABOUT");

        u.progress(u.lang.loading, main.right);
        u.get("/texts/lorem.txt").then(function(xhr){
//            u.clear(main.content);

            u.byId("content").innerHTML = xhr.response;
            u.byId("content").classList.add("content-about");
            u.byId("content").parentNode.scrollTop = 0;
            u.progress.hide();
        });
    }

    return {
        type: "about",
        category: "about",
        title: "About",
        start:start,
        menu: "About",
        icon: "info_outline"
    }
}