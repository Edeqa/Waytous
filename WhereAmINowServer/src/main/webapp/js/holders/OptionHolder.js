/**
 * Created 3/29/17.
 */
EVENTS.SHOW_OPTIONS = "show_options";

function OptionHolder(main) {

    var type = "options";
    var optionsDialog;
    var options;

    function start() {
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                object.add(DRAWER.SECTION_MISCELLANEOUS, EVENT.SHOW_OPTIONS, u.lang.options, "settings", function(){
                    main.fire(EVENTS.SHOW_OPTIONS);
                });
                break;
            case EVENTS.SHOW_OPTIONS:
                initOptionsDialog();
                populateOptionsDialog();
                optionsDialog.open();
                break;
            default:
                break;
        }
        return true;
    }

    function initOptionsDialog() {
        if(optionsDialog) return;


        optionsDialog = u.dialog({
            title: "Options",
            className: "option",
            positive: {
                label: u.lang.ok,
                onclick: function(e, event) {
                    for(var i in options) {
                        options[i].accept(e, event);
                    }
                }
            },
            neutral: {
                label: u.lang.apply
            },
            negative: {
                label: u.lang.cancel
            }
        });

        options = {};

        var modules = main.holders;
        modules.main = main;

        for(var i in modules) {
            if(modules[i] && modules[i].options && modules[i].options().title) {
                var option = modules[i].options();
                var title = option.title;
                if(title && title instanceof HTMLElement) {
                    title = title.outerHTML;
                }
                title = title || modules[i].type;
                optionsDialog.addItem({
                    type:HTML.DIV,
                    className:"option-title",
                    innerHTML: title
                });
                for(var j in option.categories) {
                    if(j == "title" || option.categories[j].ignore) continue;
                    var title = option.categories[j].title;
                    if(title && title instanceof HTMLElement) {
                        title = title.outerHTML;
                    }
                    title = title || "";

                    var cat = optionsDialog.addItem({
                        type: HTML.DIV,
                        className: "option-item",
                        enclosed: true,
                        label: title,
                    });
                    for(var k in option.categories[j].items) {
                        var item = option.categories[j].items[k];
                        var id = item.id;
                        options[id] = optionsDialog.addItem(item, cat.lastChild);
                        options[id].accept = item.onaccept;
                    }
                }
            }
        }
    }

    function populateOptionsDialog() {
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
    }
}