/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 7/23/17.
 */

EVENTS.CONTACT = "contact";

function ContactHolder(main) {

    this.type = "contact";
    this.category = "about";
    this.title = u.lang.contact;
    this.menu = u.lang.contact;
    this.icon = "mail_outline";

    this.start = function() {
        console.log("INDEX CONTACT");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.CONTACT:
                console.log("INDEX CONTACT");

                u.progress.show(u.lang.loading);

                u.get("/texts/index-contact.txt").then(function(xhr){
                    //            u.clear(main.content);

                    u.byId("content").innerHTML = xhr.response;
                    u.byId("content").classList.add("content-contact");
                    u.byId("content").parentNode.scrollTop = 0;
                    u.progress.hide();
                }).catch(function(error, json) {
                    u.byId("content").innerHTML = "Error";
                    u.byId("content").classList.add("content-contact");
                    u.byId("content").parentNode.scrollTop = 0;
                    u.progress.hide();
                });
                break;
        }
        return true;
    }

}