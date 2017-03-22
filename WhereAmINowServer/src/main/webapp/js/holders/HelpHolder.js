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
                object.add(DRAWER.SECTION_LAST,EVENTS.SHOW_HELP, "Help", "help_outline", function(){
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
                if(dialog.opened) break;
                dialog.clearItems();
                if(object) {
                    if(object["module"].help) {
                        dialog.addItem({
                            type:HTML.DIV,
                            className:"help-item-title",
                            innerHTML:object["module"].help()[object.article].title
                        });
                        dialog.addItem({
                            type:HTML.DIV,
                            className:"help-item-body",
                            innerHTML:object["module"].help()[object.article].body
                        });
                    }
                } else {
                    var modules = main.holders;
                    modules.main = main;
                    for(var i in modules) {
                        console.log(i);
                        if(modules[i] && modules[i].help && modules[i].help().title) {
                            var help = modules[i].help();
                            var title = help.title;
                            if(title && title instanceof HTMLElement) {
                                title = title.outerHTML;
                            }
                            title = title || modules[i].type;
                            dialog.addItem({
                                type:HTML.DIV,
                                className:"help-module-title",
                                innerHTML: title
                            });
                            for(var j in help) {
                                if(j == "title" || help[j].ignore) continue;
                                var title = help[j].title;
                                if(title && title instanceof HTMLElement) {
                                    title = title.outerHTML;
                                }
                                title = title || "";
                                var body = help[j].body;
                                if(body && body instanceof HTMLElement) {
                                    body = body.outerHTML;
                                }
                                body = body || "";

                                dialog.addItem({
                                    type:HTML.DIV,
                                    className:"help-module-item",
                                    enclosed:true,
                                    label:j + ". " + title,
                                    body:body
                                });
                            }
                        }
                    }
                }
                dialog.open();
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