/**
 * Created 3/29/17.
 */
EVENTS.SHOW_OPTIONS = "show_options";

function OptionHolder(main) {

    var type = "options";
    var optionsDialog;
    var modules;
    var sections;
    var categories;
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
                        options[i].onaccept(e, event);
                    }
                }
            },
            neutral: {
                dismiss: false,
                label: u.lang.apply,
                onclick: function(e, event) {
                    for(var i in options) {
                        try {
                            options[i].onaccept(e, event);
                        } catch(e) {
                            console.error(e);
                        }
                    }
                }
            },
            negative: {
                label: u.lang.cancel
            }
        });

        sections = {};
        categories = {};
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

                if(!option.id || !sections[option.id]) {
                    sections[option.id] = optionsDialog.addItem({
                        id: option.id || "",
                        type:HTML.DIV,
                        className:"option-title",
                        innerHTML: title
                    });
                }
                for(var j in option.categories) {
                    if(j == "title" || option.categories[j].ignore) continue;
                    var category = option.categories[j];
                    var title = category.title;
                    if(title && title instanceof HTMLElement) {
                        title = title.outerHTML;
                    }
                    title = title || "";

                    categories[category.id || ""] = categories[category.id || ""] || optionsDialog.addItem({
                        id: category.id || "",
                        type: HTML.DIV,
                        className: "option-item",
                        enclosed: true,
                        label: title,
                    });
                    for(var k in category.items) {
                        var item = category.items[k];
                        var id = i + ":" + j + ":" + k;
                        options[id] = optionsDialog.addItem(item, categories[category.id || ""].lastChild);
//                        delete options[id].accept;
                        options[id].onaccept = item.onaccept;
                        if(item.onchange) options[id].addEventListener("change", item.onchange);
                    }
                }
            }
        }
    }

    function populateOptionsDialog() {

        var modules = main.holders;
        modules.main = main;
        for(var i in options) {
            var path = i.split(":");
            var o = modules[path[0]].options();
            var item = o.categories[path[1]].items[path[2]];

            switch(item.type) {
                case HTML.SELECT:
                    if(item.default != undefined) {
                        for(var j = 0; j < options[i].options.length; j++) {
                            if(options[i].options[j].value == item.default) {
                                options[i].options[j].selected = true;
                            } else {
                                options[i].options[j].selected = false;
                            }
                        }
                    }
                    break;
                case HTML.INPUT:
                    if(item.default != undefined) {
                        options[i].value = item.default;
                    }
                    break;
            }

        }

    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
    }
}