/**
 * Created 3/14/17.
 */

EVENTS.SHOW_HELP = "show_help";

function HelpHolder(main) {

    var type = "help";

    var dialog;

    function start() {
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

                dialog.clearItems();
                if(object) {
                    if(object["module"].help) {
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
                    var modules = main.holders;
                    modules.main = main;
                    for(var i in modules) {
                        console.log(i);
                        if(modules[i] && modules[i].help && modules[i].help.title) {
                            var help = modules[i].help;
                            dialog.addItem({
                                type:HTML.DIV,
                                className:"help-module-title",
                                innerHTML: help.title || modules[i].type
                            });
                            for(var j in help) {
                                if(j == "title") continue;
                                dialog.addItem({
                                    type:HTML.DIV,
                                    className:"help-module-item",
                                    enclosed:true,
                                    label:j + ". " + (help[j].title || ""),
                                    body:help[j].body
                                });
                            }
                        }
                    }
                }
                dialog.onopen();
                break;
            default:
                break;
        }
        return true;
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
    }
}