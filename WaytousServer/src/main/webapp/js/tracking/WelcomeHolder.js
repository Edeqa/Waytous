/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 3/18/17.
 */

function WelcomeHolder(main) {

    var type = "welcome";

    function start() {
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.MAP_READY:
                if(!main.tracking) {
                    main.fire(EVENTS.SHOW_HELP, {
                        module:main.eventBus.holders[type],
                        article:1
                    })
                }
                break;
            default:
                break;
        }
        return true;
    }

    function help(){
        return {
            1: {
                title: "Article 1",
                body: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras pellentesque aliquam tellus, quis finibus odio faucibus sed. Nunc nec dictum ipsum, a efficitur sem. Nullam suscipit quis neque in cursus. Etiam tempus imperdiet scelerisque. Integer ut nisi at est varius rutrum quis eget urna. Morbi blandit vehicula laoreet. Curabitur tincidunt turpis dui, at venenatis risus volutpat et. Donec cursus molestie ligula eu convallis. Curabitur sed quam id ex tristique ultricies. Duis id felis eget massa venenatis vehicula. Aenean eget varius dui. "
            },
            2: {
                title: "Article 2",
                body: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras pellentesque aliquam tellus, quis finibus odio faucibus sed. Nunc nec dictum ipsum, a efficitur sem. Nullam suscipit quis neque in cursus. Etiam tempus imperdiet scelerisque. Integer ut nisi at est varius rutrum quis eget urna. Morbi blandit vehicula laoreet. Curabitur tincidunt turpis dui, at venenatis risus volutpat et. Donec cursus molestie ligula eu convallis. Curabitur sed quam id ex tristique ultricies. Duis id felis eget massa venenatis vehicula. Aenean eget varius dui. "
            }
        }
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        help:help,
    }
}