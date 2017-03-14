/**
 * Created 3/14/17.
 */

EVENTS.SHOW_HELP = "show_help";

function HelpHolder(main) {

    var type = "help";

    var dialog;

    function start() {
        console.log("SAMPLEHOLDER",this);

    }

    function onEvent(EVENT,object){
        // console.log("SAMPLEEVENT",EVENT,object)
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                var menuItem = object.add(DRAWER.SECTION_LAST,EVENTS.SHOW_HELP, "Help", "help_outline", function(){
                    main.fire(EVENTS.SHOW_HELP);
                });
                break;
            case EVENTS.SHOW_HELP:

                dialog = dialog || u.dialog({
                    title: "Help",
                    className: "help",
                    negative: {
                        label: "Close"
                    }
                });

                if(object) {
                    if(object["module"].help) {
                        dialog.clearItems();
                        dialog.addItem({
                            type:HTML.DIV,
                            className:"help-item-title",
                            innerHTML:object["module"].help[object.article].title
                        });
                        dialog.addItem({
                            type:HTML.DIV,
                            className:"help-item-body",
                            innerHTML:object["module"].help[object.article].body
                        });
                    }
                } else {
//                items: [
//                                                        { type: HTML.DIV, className: "streetview-placeholder", innerHTML: "Loading..." },
//                                                        { type: HTML.DIV, className: "streetview-view hidden", id: "streetview" },
//                                                    ],
                }

                dialog.onopen();
                console.log("SHOWHELP",object);
                break;
            default:
                break;
        }
        return true;
    }

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
    }
}