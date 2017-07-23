/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 7/23/17.
 */

EVENTS.FEEDBACK = "feedback";

function FeedbackHolder(main) {

    this.type = "feedback";
    this.category = "about";
    this.title = u.lang.feedback;
    this.menu = u.lang.feedback;
    this.icon = "feedback";

    this.start = function() {
        console.log("INDEX FEEDBACK");
    };

    this.onEvent = function(event, object) {
        switch(event) {
            case EVENTS.FEEDBACK:
                console.log("INDEX FEEDBACK");

                u.progress.show(u.lang.loading);

                u.byId("content").innerHTML = u.lang.feedback_body.innerHTML;
                u.byId("content").classList.add("content-feedback");
                u.byId("content").parentNode.scrollTop = 0;
                u.progress.hide();
                break;
        }
        return true;
    }

}