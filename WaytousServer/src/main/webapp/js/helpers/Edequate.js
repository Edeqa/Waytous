/**
 * Edequate - javascript DOM and interface routines
 */

function Edequate(options) {

    var HTML = {
        DIV: "div",
        LINK:"link",
        A:"a",
        IMG:"img",
        META:"meta",
        STYLE:"style",
        CLASS:"className",
        CLASSNAME:"className",
        SCRIPT:"script",
        TITLE:"title",
        ID:"id",
        SRC:"src",
        HTTP_EQUIV: "http-equiv",
        CONTENT:"content",
        TABLE:"table",
        TR:"tr",
        TH:"th",
        TD:"td",
        H1:"h1",
        H2:"h2",
        H3:"h3",
        H4:"h4",
        H5:"h5",
        H6:"h6",
        H7:"h7",
        I:"i",
        BORDER:"border",
        COLSPAN:"colspan",
        ROWSPAN:"rowspan",
        HREF:"href",
        TARGET:"target",
        SMALL:"small",
        REL:"rel",
        STYLESHEET:"stylesheet",
        TYPE:"type",
        BR:"br",
        FORM:"form",
        NAME:"name",
        LABEL:"label",
        INPUT:"input",
        CHECKBOX:"checkbox",
        TEXT:"text",
        TEXTAREA:"textarea",
        HIDDEN:"hidden",
        PASSWORD:"password",
        SELECT:"select",
        OPTION:"option",
        SUBMIT:"submit",
        TEXT:"text",
        VALUE:"value",
        MANIFEST:"manifest",
        SPAN:"span",
        BUTTON:"button",
        CLICK:"click",
        STYLESHEET:"stylesheet",
        SVG:"svg",
        PATH:"path",
        MOUSEOVER:"mouseover",
        MOUSEOUT:"mouseout",
        MOUSEUP:"mouseup",
        MOUSEDOWN:"mousedown",
        MOUSEMOVE:"mousemove",
        MOUSEENTER:"mouseenter",
        MOUSELEAVE:"mouseleave",
        VIEWBOX:"viewBox",
        INNERHTML:"innerHTML",
        INNERTEXT:"innerText",
        BLOCK:"block",
        AUTO:"auto",
        AUDIO:"audio",
    };
    var ERRORS = {
        NOT_EXISTS: 1,
        NOT_AN_OBJECT: 2,
        INCORRECT_JSON: 4,
        ERROR_LOADING: 8
    };
    var DRAWER = {
        SECTION_PRIMARY: 0,
        SECTION_COMMUNICATION: 2,
        SECTION_NAVIGATION: 3,
        SECTION_VIEWS: 4,
        SECTION_MAP: 7,
        SECTION_MISCELLANEOUS: 8,
        SECTION_LAST: 9
    };

    var self = this;

    URL = function(link) {
        var href = link;
        var p = link.split("://");
        var protocol = "http:";
        if(p.length > 1) protocol = p.shift() +":";
        p = p.join("//").split("/");
        var host = p.shift();
        var pathname = "/" + p.join("/");
        p = host.split(":");
        var hostname = p.shift();
        var port = p.shift();
        if(!port) port = "";
        var origin = protocol + "//" + host;

        return {
            hash: "",
            host: host,
            hostname: hostname,
            href: href,
            origin: origin,
            pathname: pathname,
            password: "",
            port: port,
            protocol: protocol,
            search: "",
            username: ""
        }
    };

    HTMLDivElement.prototype.show = function() {
        this.classList.remove("hidden");
        this.isHidden = false;
        return this;
    }
    HTMLDivElement.prototype.hide = function() {
        this.classList.add("hidden");
        this.isHidden = true;
        return this;
    }
    HTMLDivElement.prototype.place = function(type, args) {
        create(type, args, this);
        return this;
    }

    if(!Object.assign) {
        Object.defineProperty(Object.prototype, "assign", {
            enumerable: false,
            value: function(target, first, second) {
                for(var x in first) {
                    if(first.hasOwnProperty(x)) target[x] = first[x];
                }
                for(var x in second) {
                    if(second.hasOwnProperty(x)) target[x] = second[x];
                }
                return target;
            }
        });
    }

    if(!String.prototype.toUpperCaseFirst) {
        Object.defineProperty(String.prototype, "toUpperCaseFirst", {
            enumerable: false,
            value: function() {
                return this.substring(0,1).toUpperCase() + this.substring(1);
            }
        });
    }

    function byId(id) {
        return document.getElementById(id);
    }

    function normalizeName(name) {
        if(name == HTML.CLASSNAME){
            name = "class";
        } else if(name in attributable) {
        } else if(name.toLowerCase() == "viewbox") {
            name = HTML.VIEWBOX;
        } else if(name != name.toLowerCase()) {
            var ps = name.split(/([A-Z])/);
            name = ps[0];
            for(var i = 1; i < ps.length; i++) {
                if(i % 2 != 0) name += "-";
                name += ps[i].toLowerCase();
            }
        }
        return name;
    }

    var attributable = {
        "frameBorder":1,
        "xmlns":1,
        "strokeWidth":1,
        "version":1,
        "fill":1,
        "d":1,
        "tabindex":1
    };

    function create(name, properties, appendTo, position) {
        var el,namespace;
        if(properties && properties.xmlns) {
            el = document.createElementNS(properties.xmlns, name);
            namespace = properties.xmlns;
        } else {
            el = document.createElement(name);
        }

        if(properties) {
            if(properties instanceof HTMLElement) {
                el.appendChild(properties);
            } else if(properties.constructor === Object) {
                for(var x in properties) {
                    if(x == HTML.INNERHTML || x == HTML.INNERTEXT) {
                        if(properties[x]) {
                            if(properties[x] instanceof HTMLElement) {
                                el.appendChild(properties[x]);
                            } else {
                                el[x] = properties[x];
                            }
                        }
                    } else if(x == HTML.CONTENT && properties[x].constructor !== String) {
                        el.appendChild(properties[x]);
                    } else if(properties[x] instanceof HTMLElement) {
                        el.appendChild(properties[x]);
                        el[x] = properties[x];
                    } else if(x.toLowerCase() == "onlongclick" && properties[x]) {
                        var mousedown,mouseup;
                        el.longclickFunction = properties[x];
                        mousedown = function(evt){
                            clearTimeout(el.longTask);
                            el.addEventListener("mouseup", mouseup);
                            el.addEventListener("touchend", mouseup);
                            el.longTask = setTimeout(function(){
                                el.removeEventListener("mouseup", mouseup);
                                el.removeEventListener("touchend", mouseup);
                                el.longTask = -1;
                                el.longclickFunction(evt);
                            }, 500);
                        };
                        mouseup = function(){
                            clearTimeout(el.longTask);
                        }
                        el.addEventListener("mousedown", mousedown, false);
                        el.addEventListener("touchstart", mousedown, false);
                        el.addEventListener("contextmenu", function(evt){
                            evt.preventDefault();
                            evt.stopPropagation();
                        }, false);
                    } else if(x.toLowerCase() == "onclick") {
                        el.clickFunction = properties[x];
                        if(el.clickFunction) {
                            var call = function(evt) {
                                if(el.longTask && el.longTask < 0) return;
                                el.clickFunction(evt);
                            }
                            el.addEventListener("click", call, false);
                        }
                    } else if(x.indexOf("on") == 0) {
                        var action = x.substr(2).toLowerCase();
                        var call = properties[x];
                        if(call) {
                            el.addEventListener(action, call, false);
                        }
                    } else {
                        var name = normalizeName(x), value = properties[x];
                        if(value != undefined) {
                            if(value.constructor === Object) {
                                var v = "";
                                for(var y in value) {
                                    v += normalizeName(y) + ": " + value[y] + "; ";
                                }
                                value = v;
                            }
                            if(x == "hide" || x == "show") {
                                el[x] = value;
                            } else if(x in el || name in el || name.substr(0,5) == "data-" || x in attributable) {
                                el.setAttribute(name, value);
                            } else {
                                el[x] = value;
                            }
                        }
                    }
                }
            } else if (properties.constructor === String || properties.constructor === Number) {
                el.innerHTML = properties;
            }
        }
        if(appendTo) {
            if(appendTo.childNodes.length > 0) {
                if(position == "first") {
                    appendTo.insertBefore(el,appendTo.firstChild);
//                    appendTo.insertBefore(el,appendTo.childNodes[0]);
                } else {
                    appendTo.appendChild(el);
                }
            } else {
                appendTo.appendChild(el);
            }
        }

        return el;
    }

    function clear(node) {
        if(!node) return;
        for(var i = node.children.length-1; i>=0; i--) {
            node.removeChild(node.children[i]);
        }
    }

    function destroy(node) {
        try {
            clear(node);
            if(node.parentNode) node.parentNode.removeChild(node);
            node = null;
        } catch(e) {
            console.error(e);
        }
    }

    function keys(o) {
        var keys = [];
        for(var x in o) {
            keys.push(x);
        }
        return keys;
    }

    function require(name, onload, onerror, context, viaXhr){
        var parts = name.split("/");
        var filename = parts[parts.length-1];
        var onlyname = filename.split(".")[0];
        var needInstantiate = false;
        if(onerror && onerror.constructor !== Function) {
            context = onerror;
            onerror = function(){};
        }
        if(!filename.match(/\.js$/) && parts[1] == "js") {
            needInstantiate = true;
            name += ".js";
        }
        if(viaXhr) {
            getRemote(name, function(xhr){
                create(HTML.SCRIPT, {innerHTML: xhr.responseText }, document.head);
                if (onload) {
                    var a;
                    console.log(needInstantiate,onlyname,filename)
                    if(needInstantiate) {
                        if(onlyname && window[onlyname] && window[onlyname].constructor === Function) {
                            a = new window[onlyname](context);
                            a.moduleName = onlyname;
                            lang.overrideResources(a);
                        } else {
                            onerror(ERRORS.NOT_AN_OBJECT, onlyname, xhr);
                        }
                    }
                    onload(a);
                }
            }, function(code,xhr){
                if(onerror) {
                    onerror(ERRORS.NOT_EXISTS, onlyname, xhr);
                }
            });
        } else {
            create(HTML.SCRIPT, {src: name, async:"", defer:"", instance: needInstantiate ? onlyname : null, onload: function(e) {
                if (onload) {
                    var a;
                    if(needInstantiate) {
                        if(this.instance && window[this.instance] && window[this.instance].constructor === Function) {
                            a = new window[this.instance](context);
                            a.moduleName = this.instance;
                            lang.overrideResources(a);
                        } else {
                            onerror(ERRORS.NOT_AN_OBJECT, this.instance, e);
                        }
                    }
                    onload(a);
                }
            }, onerror: function(e) {
                if(onerror) {
                    onerror(ERRORS.NOT_EXISTS, this.instance, e);
                }
            }, async:"", defer:""}, document.head);
        }


    }

    function save(name, value) {
        if(value) {
            localStorage[this.origin + ":" + name] = JSON.stringify(value,
                function(key, value) {
                    return typeof value === "function" ? value.toString() : value;
                }
            );
        } else {
            delete localStorage[this.origin + ":" + name];
        }
    }

    function load(name) {
        var value = localStorage[this.origin + ":" + name];
        if(value) {
            return JSON.parse(value,
                function(key, value) {
                    if (typeof value === "string" && /^function.*?\([\s\S]*?\)\s*\{[\s\S]*\}[\s\S]*$/.test(value)) {
                        var args = value
                            .replace(/\/\/.*$|\/\*[\s\S]*?\*\//mg, "") //strip comments
                            .match(/\([\s\S]*?\)/m)[0]                      //find argument list
                            .replace(/^\(|\)$/g, "")                    //remove parens
                            .match(/[^\s(),]+/g) || [],                //find arguments
                            body = value.replace(/\n/mg, "").match(/\{([\s\S]*)\}/)[1]          //extract body between curlies
                        return Function.apply(0, args.concat(body));
                    } else {
                        return value;
                    }
                }
            );
        } else {
            return null;
        }
    }

    function saveForContext(name, value) {
        if(!this.context) {
            save(name, value);
            return;
        }
        if(value) {
            localStorage[this.origin + "$" + this.context +":" + name] = JSON.stringify(value);
        } else {
            delete localStorage[this.origin + "$" + this.context +":" + name];
        }
    }

    function loadForContext(name) {
        if(!this.context) {
            return load(name);
        }
        var value = localStorage[this.origin + "$" + this.context +":"+name];
        if(value) {
            return JSON.parse(value);
        } else {
            return null;
        }
    }

    var modalBackground;
    function dialog(options, appendTo) {
        if(!appendTo) throw new Error("Parent node is not defined.");

        var dialog = create(HTML.DIV, {
            className:"modal shadow hidden"+(options.className ? " "+options.className : ""),
            tabindex:-1,
            onblur: options.onblur,
            onfocus: options.onfocus
        }, appendTo);

        dialog.opened = false;

        dialog.clearItems = function() {
            clear(dialog.itemsLayout);
            dialog.items = [];
        };

        dialog.addItem = function(item, appendTo) {
            item = item || {};
            appendTo = appendTo || dialog.itemsLayout;

            var div,x;
            if(item.type == HTML.DIV || item.type == HTML.A) {
                if(item.enclosed) {
                    div = x = create(item.type, {
                        className: "dialog-item-enclosed" + (item.className ? " " + item.className : "")
                    });
                    var enclosedButton, enclosedIcon;
                    enclosedButton = create(HTML.DIV, {className:"dialog-item-enclosed-button", onclick: function(){
                        if(x.body.classList.contains("hidden")) {
                            enclosedIcon.innerHTML = "expand_less";
                            x.body.classList.remove("hidden");
                        } else {
                            enclosedIcon.innerHTML = "expand_more";
                            x.body.classList.add("hidden");
                        }
                    }}, x);
                    enclosedIcon = create(HTML.DIV, {className:"dialog-item-enclosed-icon notranslate", innerHTML:"expand_more"}, enclosedButton);
                    create(HTML.DIV, {className:"dialog-item-enclosed-label", innerHTML: item.label || "Show more information"}, enclosedButton);
                    x.body = create(HTML.DIV, {className:"dialog-item-enclosed-body hidden", innerHTML:item.body || ""}, x);
                } else {
                    item.className = "dialog-item" + (item.className ? " " + item.className : "");
                    item.innerHTML = item.label || item.title || item.innerHTML || "";
                    delete item.label;
                    delete item.title;
                    var type = item.type;
                    delete item.type;
                    div = x = create(type, item);
                }
            } else if(item.type == HTML.HIDDEN) {
                div = x = create(HTML.INPUT, {type:HTML.HIDDEN, value:item.value || ""});
            } else if(item.type == HTML.SELECT) {
                div = create(HTML.DIV, {className:"dialog-item dialog-item-input", onclick: function(){this.firstChild.nextSibling.click();}});

                if(item.label) {
                    var labelOptions = {
                        className:"dialog-item-label" + (item.labelClassName ? " " + item.labelClassName : ""),
                        innerHTML:item.label
                    };
                    if(item.id){
                        labelOptions["for"] = item.id;
//                    } else {
//                        create(HTML.DIV, labelOptions , div);
                    }
                    create(HTML.LABEL, labelOptions , div);
                }

                x = create(HTML.SELECT, {
                    type:item.type,
                    className:"dialog-item-input-select" + (item.className ? " "+item.className : ""),
                    tabindex: i,
                    value:item.value || "",
                }, div);
                for(var y in item.values) {
                    u.create(HTML.OPTION, {value:y, innerHTML:item.values[y], selected: item.default == y}, x);
                }

            } else {
                div = create(HTML.DIV, {className:"dialog-item dialog-item-input" + (item.itemClassName ? " " + item.itemClassName : ""), onclick: function(){this.firstChild.nextSibling.click();}});

                if(item.label) {
                    var labelOptions = {
                        className:"dialog-item-label" + (item.labelClassName ? " " + item.labelClassName : ""),
                        innerHTML:item.label
                    };
                    if(item.id){
                        labelOptions["for"] = item.id;
                    }
                    create(HTML.LABEL, labelOptions , div);
                }

                var type = HTML.INPUT;
                if(item.type.toLowerCase() == HTML.TEXTAREA) type = HTML.TEXTAREA;

                item.tabindex = i;
                item.className = "dialog-item-input-"+item.type + (item.className ? " "+item.className : "");
                item.onclick = function(e) { this.focus(); e.stopPropagation(); };
                item.onkeyup = function(e){
                   if(e.keyCode == 13 && this.type != HTML.TEXTAREA) {
                       dialog.close();
                       if(options.positive && options.positive.onclick) options.positive.onclick.call(dialog,items);
                   } else if(e.keyCode == 27) {
                       dialog.close();
                       if(options.negative && options.negative.onclick) options.negative.onclick.call(dialog,items);
                   }
               };
                item.value = item.value || "";
                delete item.label;
                delete item.labelClassName;

                x = create(type, item, div);
            }
            items.push(x);

            if(item.order) {
                var appended = false;
                for(var i in appendTo.childNodes) {
                    if(!appendTo.childNodes.hasOwnProperty(i)) continue;
                    if(appendTo.childNodes[i].order > item.order) {
                        appendTo.insertBefore(div, appendTo.childNodes[i]);
                        appended = true;
                        break;
                    }
                }
                if(!appended) appendTo.appendChild(div);

            } else {
                appendTo.appendChild(div);
            }

            return x;
        }

        dialog.adjustPosition = function() {
            var left,top,width,height;
            var id = options.id || (options.title && options.title.label && (options.title.label.lang ? options.title.label.lang : options.title.label));
            if(id) {
                left = load("dialog:left:"+id);
                top = load("dialog:top:"+id);
                width = load("dialog:width:"+id);
                height = load("dialog:height:"+id);
            }
            if(left || top || width || height) {
                if(left) dialog.style.left = left;
                if(top) dialog.style.top = top;
                if(width) dialog.style.width = width;
                if(height) dialog.style.height = height;
                dialog.style.right = HTML.AUTO;
                dialog.style.bottom = HTML.AUTO;
            } else {
//                left = dialog.offsetLeft;
                var outWidth = appendTo.offsetWidth;

                if((dialog.offsetLeft + dialog.offsetWidth) >= outWidth || left == 0) {
                    dialog.style.left = ((outWidth - dialog.offsetWidth) /2)+"px";
                    dialog.style.top = ((appendTo.offsetHeight - dialog.offsetHeight) /2)+"px";
                    dialog.style.right = "auto";
                    dialog.style.bottom = "auto";
                }
            }
            dialog.focus();
            var focused = false;
            for(var i in items) {
                if(items[i].constructor === HTMLInputElement && items[i].type == HTML.TEXT) {
                    focused = true;
                    items[i].focus();
                    break;
                }
            }
            if(!focused) {
                if(dialog.positive && !options.timeout) dialog.positive.focus();
                else if(dialog.negative && options.timeout) dialog.negative.focus();
            }
        }

        if(options.modal) {
            modalBackground = modalBackground || u.create(HTML.DIV, {className:"dim"}, appendTo);
            dialog.modal = modalBackground;
        }

        var backButtonAction = function(event) {
            window.history.pushState(null, document.title, location.href);
            event.preventDefault();
            event.stopPropagation();
            dialog.close();
        }

        dialog.open = function(event){
            clearInterval(dialog.intervalTask);
            dialog.modal && dialog.modal.show();
            dialog.show();
            dialog.opened = true;
            dialog.adjustPosition();
            if(options.onopen) options.onopen.call(dialog,items,event);
            if(options.timeout) {
                var atom = options.timeout / 16;
                var current = 0;
                dialog.intervalTask = setInterval(function(){
                    current += 16;
                    progress.style.width = (current / options.timeout * 100) + "%";
                    if(current >= options.timeout) {
                        clearInterval(dialog.intervalTask);
                        dialog.close();
                    }
                }, 16);
            }

//            window.history.pushState(null, document.title, location.href);
            if(options.title && options.title.button == defaultCloseButton) {
                window.addEventListener("popstate", backButtonAction);
            }

            return dialog;
        };

        dialog.close = function (event){
            dialog.hide();
            dialog.modal && dialog.modal.hide();
            dialog.opened = false;

            window.removeEventListener("popstate", backButtonAction);

            clearInterval(dialog.intervalTask);

            if(options.onclose) options.onclose.call(dialog,items,event);
        };
        dialog.addEventListener("keyup", function(e) {
            if(e.keyCode == 27) {
                if(options.negative && options.negative.onclick) {
                    dialog.close();
                    options.negative.onclick.call(dialog,items);
                }
            }
        });

        options = options || {};
        var items = [];

        var defaultCloseButton = {
             icon: " ",
             className: "",
             onclick: function(e){
                 dialog.close();
                 if(options.negative && options.negative.onclick) options.negative.onclick.call(dialog,items);
             }
         };
        if(options.title) {
            if(options.title.constructor === String || options.title instanceof HTMLElement) {
                options.title = {
                    label: options.title,
                    className: "",
                    button: defaultCloseButton,
                }
            } else {
                if(options.title.className) options.title.className = " " + options.title.className;
                options.title.button = options.title.button || defaultCloseButton;
                if(options.title.button.className) options.title.button.className = " " + options.title.button.className;
                options.title.button.onclick = options.title.button.onclick || function(){};

            }
            var titleLayout = create(HTML.DIV, {
                className:"dialog-title" + (options.title.className || ""),
                onmousedown: function(e) {
                    if(e.button != 0) return;
//                    var position = dialog.getBoundingClientRect();
                    var position = { left: dialog.offsetLeft, top: dialog.offsetTop, width: dialog.offsetWidth, height: dialog.offsetHeight };
                    var offset = [ e.clientX, e.clientY ];
                    var moved = false;
                    function mouseup(e){
                        window.removeEventListener(HTML.MOUSEUP, mouseup, false);
                        window.removeEventListener(HTML.MOUSEMOVE, mousemove, false);
                        var id = options.id || (options.title.label && (options.title.label.lang ? options.title.label.lang : options.title.label));
                        if(id && moved) {
                            if(dialog.style.left) save("dialog:left:"+id, dialog.style.left);
                            if(dialog.style.top) save("dialog:top:"+id, dialog.style.top);
//                            if(dialog.style.width) save("dialog:width:"+(options.id || options.title.label), dialog.style.width);
//                            if(dialog.style.height) save("dialog:height:"+(options.id || options.title.label), dialog.style.height);
                        }
                    }
                    function mousemove(e){
                        var deltaX = e.clientX - offset[0];
                        var deltaY = e.clientY - offset[1];
                        if(deltaX || deltaY) {
                            moved = true;
                            dialog.style.left = (position.left + deltaX) + "px";
                            dialog.style.top = (position.top + deltaY ) + "px";
                            dialog.style.right = "auto";
                            dialog.style.bottom = "auto";
                        }
                    }
                    window.addEventListener(HTML.MOUSEUP, mouseup);
                    window.addEventListener(HTML.MOUSEMOVE, mousemove);
                    e.preventDefault();
                },
                ondblclick: function(e) {
                    var id = options.id || (options.title.label && (options.title.label.lang ? options.title.label.lang : options.title.label));
                    save("dialog:left:"+id);
                    save("dialog:top:"+id);
                    save("dialog:width:"+id);
                    save("dialog:height:"+id);
                    dialog.style.left = "";
                    dialog.style.top = "";
                    dialog.style.width = "";
                    dialog.style.height = "";
                    dialog.style.right = "";
                    dialog.style.bottom = "";
                    dialog.adjustPosition();
                }
            }, dialog);
            dialog.titleLayout = create(HTML.DIV, {className:"dialog-title-label", innerHTML: options.title.label }, titleLayout);

            if(options.title.button && options.title.button.icon) {
                create(HTML.DIV, {className:"dialog-title-button notranslate"+ options.title.button.className, innerHTML:options.title.button.icon, onclick:options.title.button.onclick}, titleLayout);
            }

            if(options.title.filter) {
                dialog.filterPlaceholder = u.create(HTML.DIV, {className:"dialog-items hidden", innerHTML:"Nothing found"}, dialog);
                dialog.filterLayout = u.create(HTML.DIV, {
                    className: "dialog-filter"
                }, titleLayout);
                dialog.filterButton = u.create(HTML.DIV, {
                    className: "dialog-filter-button notranslate",
                    innerHTML: "search",
                    onclick: function() {
                        dialog.filterButton.hide();
                        dialog.filterInput.classList.remove("hidden");
                        dialog.filterInput.focus();
                    }
                }, dialog.filterLayout);
                dialog.filterInput = u.create(HTML.INPUT, {
                    className: "dialog-filter-input hidden",
                    tabindex: -1,
                    onkeyup: function(evt) {
                        if(evt.keyCode == 27) {
                            evt.preventDefault();
                            evt.stopPropagation();
                            if(this.value) {
                                this.value = "";
                            } else {
                                dialog.focus();
                            }
                        }
                        this.apply();
                    },
                    onblur: function() {
                        if(!this.value) {
                            dialog.filterInput.classList.add("hidden");
                            dialog.filterButton.show();
                        }
                    },
                    onclick: function() {
                        this.focus();
                    },
                    apply: function() {
                        if(this.value) {
                            dialog.filterClear.show();
                        } else {
                            dialog.filterClear.hide();
                        }
                        var counter = 0;
                        for(var i in dialog.itemsLayout.childNodes) {
                            if(!dialog.itemsLayout.childNodes.hasOwnProperty(i)) continue;
                            if(!this.value || (dialog.itemsLayout.childNodes[i].innerText && dialog.itemsLayout.childNodes[i].innerText.toLowerCase().match(this.value.toLowerCase()))) {
                                dialog.itemsLayout.childNodes[i].show();
                                counter++;
                            } else {
                                dialog.itemsLayout.childNodes[i].hide();
                            }
                        }
                        if(counter) {
                            dialog.filterPlaceholder.hide();
                            dialog.itemsLayout.show();
                        } else {
                            dialog.filterPlaceholder.show();
                            dialog.itemsLayout.hide();
                        }
                    }
                }, dialog.filterLayout);
                dialog.filterClear = u.create(HTML.DIV, {
                    className: "dialog-filter-clear hidden notranslate",
                    innerHTML: "clear",
                    onclick: function() {
                        dialog.filterInput.value = "";
                        dialog.filterInput.focus();
                        dialog.filterInput.apply();
                    }
                }, dialog.filterLayout);
            }

        }

        if(options.header) {
            var item = options.header;
            item.className = "dialog-header" + (item.className ? " " + item.className : "");
            item.innerHTML = item.label || item.title || item.innerHTML || "";
            delete item.label;
            delete item.title;
            var type = item.type;
            delete item.type;
            dialog.header = create(type, item, dialog);
        }

        dialog.itemsLayout = create(HTML.DIV, {className:"dialog-items" +(options.itemsClassName ? " "+options.itemsClassName : "")}, dialog);
        for(var i in options.items) {
            var item = options.items[i];
            dialog.addItem(item);
        }
        dialog.items = items;

        if(options.footer) {
            var item = options.footer;
            item.className = "dialog-footer" + (item.className ? " " + item.className : "");
            item.innerHTML = item.label || item.title || item.innerHTML || "";
            delete item.label;
            delete item.title;
            var type = item.type;
            delete item.type;
            dialog.footer = create(type, item, dialog);
        }

        var buttons = create(HTML.DIV, {className:"dialog-buttons hidden" + (options.buttonsClassName ? " " + options.buttonsClassName : "")}, dialog);
        if(options.positive && options.positive.label) {
            dialog.positive = create(HTML.BUTTON, {className:"dialog-button-positive", tabindex:98, onclick:function(event){
                if(options.positive.dismiss == undefined || options.positive.dismiss) dialog.close();
                if(options.positive.onclick) options.positive.onclick.call(dialog,items,event);
            }, innerHTML: options.positive.label}, buttons);
            buttons.show();
        }
        if(options.neutral && options.neutral.label) {
            dialog.neutral = create("button", {className:"dialog-button-neutral", tabindex:100, onclick:function(event){
                if(options.neutral.dismiss == undefined || options.neutral.dismiss) dialog.close();
                if(options.neutral.onclick) options.neutral.onclick.call(dialog,items,event);
            }, innerHTML: options.neutral.label}, buttons);
            buttons.show();
        }
        if(options.negative && options.negative.label) {
            dialog.negative = create(HTML.BUTTON, {className:"dialog-button-negative", tabindex:99, onclick:function(event){
                if(options.negative.dismiss == undefined || options.negative.dismiss) dialog.close();
                if(options.negative.onclick) options.negative.onclick.call(dialog,items,event);
            }, innerHTML: options.negative.label}, buttons);
            buttons.show();
        }
        if(options.help) {
            create(HTML.BUTTON, {className:"dialog-help-button", onclick:options.help, innerHTML:"help_outline"}, dialog);
        }
        if(options.resizeable) {
            create(HTML.DIV, {
                className:"dialog-resize",
                onmousedown: function(e) {
                    if(e.button != 0) return;
//                    var position = dialog.getBoundingClientRect();
                    var position = { left: dialog.offsetLeft, top: dialog.offsetTop, width: dialog.offsetWidth, height: dialog.offsetHeight };
                    var offset = [ e.clientX, e.clientY ];
                    var moved = false;
                    function mouseup(e){
                        window.removeEventListener(HTML.MOUSEUP, mouseup, false);
                        window.removeEventListener(HTML.MOUSEMOVE, mousemove, false);
                        if((options.id || options.title.label) && moved) {
//                            if(dialog.style.left) save("dialog:left:"+(options.id || options.title.label), dialog.style.left);
//                            if(dialog.style.top) save("dialog:top:"+(options.id || options.title.label), dialog.style.top);
                            var id = options.id || (options.title.label && (options.title.label.lang ? options.title.label.lang : options.title.label));
                            if(dialog.style.width) save("dialog:width:"+id, dialog.style.width);
                            if(dialog.style.height) save("dialog:height:"+id, dialog.style.height);
                        }
                    }
                    function mousemove(e){
                        var deltaX = e.clientX - offset[0];
                        var deltaY = e.clientY - offset[1];
                        if(deltaX || deltaY) {
                            moved = true;
                            dialog.style.width = (position.width + deltaX)+"px";
                            dialog.style.height = (position.height + deltaY)+"px";
                        }
                    }
                    window.addEventListener(HTML.MOUSEUP, mouseup);
                    window.addEventListener(HTML.MOUSEMOVE, mousemove);
                    e.preventDefault();
                }
            }, dialog);
        }

        if(options.timeout) {
            var progressBar = create(HTML.DIV, {className:"dialog-progress-bar"}, dialog);
            var progress = create(HTML.DIV, {className:"dialog-progress-value"}, progressBar);
            progress.style.width = "0%";
        }

        return dialog;
    }

    function sprintf() {
        var a = this, b;
        var args = arguments;
        if(arguments[0].constructor === Array || arguments[0].constructor === Object) {
            args = arguments[0];
        }

        for(b in args){
            var value = args[b];
            var replace = "";
            if (value.constructor === String || value.constructor === Number) {
                replace = value;
            } else if(value.constructor === HTMLSpanElement) {
                if(value.lang && lang.$origin[value.lang]) {
                    replace = lang[value.lang].outerText;
                } else {
                    replace = value.outerText;
                }
            } else if (value.constructor === Array) {
                replace = value;
            } else if (value.constructor === Object) {
                replace = value;
            } else {
                console.error("Replace failed.")
            }
            a = a.replace(/%[a-z]/,replace);
        }
        return a; // Make chainable
    }

    function cloneAsObject(object) {
        var o = {};
        for(var x in object) {
            if(!object[x] || object[x].constructor === String || object[x].constructor === Number) {
                o[x] = object[x] || "";
            } else {
                o[x] = cloneAsObject(object[x]);
            }
        }
        return o;
    }

    function lang(string, value) {
        if(value) {
            var prev = lang.$origin[string];
            lang.$origin[string] = value;
            lang.$origin[string] = value;
            if(!prev) {
                Object.defineProperty(lang, string, {
                    get: function() {
                        lang.$nodes[string] = lang.$nodes[string] || create(HTML.SPAN, {
                            lang:string,
                        });
                        var a = lang.$nodes[string].cloneNode();
                        a.format = function() {
                              lang.$arguments[this.lang] = arguments;
                              this.innerHTML = sprintf.call(this.innerHTML, arguments);
                              return this;
                          }
                        a.innerHTML = lang.$origin[string] || (string ? string.substr(0,1).toUpperCase() + string.substr(1) : "");

                        if(lang.$arguments[string]){
                            a.innerHTML = sprintf.call(a.innerHTML, lang.$arguments[string]);
                        }
                        return a;
                    }
                });
            }
        }
        var res = (lang.$origin[string] && lang[string]) || (string ? string.substr(0,1).toUpperCase() + string.substr(1) : "");
        return res;
    }

    lang.$nodes = lang.$nodes || {};
    lang.$origin = lang.$origin || {};
    lang.$arguments = lang.$arguments || {};


    lang.overrideResources = function(holder) {
        if(holder.constructor === String) {

            var resourcesFile = "/locales/resources." + holder + ".json";

            getJSON({
                url: resourcesFile,
                onsuccess: function(json){
                    var nodes = document.getElementsByTagName(HTML.SPAN);
                    console.warn("Switching to resources \""+holder+"\".");
                    for(var x in json) {
    //                            if(lang.$origin[x]) {
    //                                console.warn("Overrided resource: " + x + ":", json[x] ? (json[x].length > 30 ? json[x].substr(0,30)+"..." : json[x]) : "" );
    //                            }
                        lang(x, json[x]);
                    }
                    for(var i = 0; i < nodes.length; i++) {
                        if(nodes[i].lang) {
                            nodes[i].parentNode.replaceChild(lang[nodes[i].lang],nodes[i]);
                        }
                    }

                },
                onerror: function(code, xhr){
                    switch(code) {
                        case ERRORS.ERROR_LOADING:
                            console.warn("Error fetching resources for \""+holder+"\":",xhr.status + ': ' + xhr.statusText);
                            if(holder != "en-us"){
                                console.warn("Switching to default resources \"en-us\".")
                                lang.overrideResources("en-us");
                            }
                            break;
                        case ERRORS.INCORRECT_JSON:
                            console.warn("Incorrect, empty or damaged resource file for \""+holder+"\":",xhr);
                            if(holder != "en-us"){
                                console.warn("Switching to default resources \"en-us\".")
                                lang.overrideResources("en-us");
                            }
                            break;
                    }
                }
            });

        } else if(holder.resources) {
            for(var x in holder.resources) {
                if(lang[x]) {
//                    console.warn("Overrided resource: " + x + ":", holder.resources[x] ? (holder.resources[x].length > 30 ? holder.resources[x].substr(0,30)+"..." : holder.resources[x]) : "" );
                }
                lang(x, holder.resources[x]);
            }
        }
    }

    lang.updateNode = function(node, lang) {
        if(node && lang && lang.lang) {
            node.innerHTML = lang.innerHTML;
            node.lang = lang.lang;
        }
    }

    /**
        options:
            url,
            post - for send as body (optional),
            onsuccess - function,
            onerror - function (optional)
    */
    function getRemote(options) {
        options = options || {};
        var xhr = new XMLHttpRequest();
        xhr.open("GET", options.url, true);
//        xhr.overrideMimeType("text/plain; charset=x-user-defined");
        xhr.onreadystatechange = function() { // (3)
            if (xhr.readyState != 4) return;
            if (xhr.status != 200) {
                if(options.onerror) options.onerror(ERRORS.ERROR_LOADING, xhr);
                else console.error("Error loading resource",xhr);
            } else {
                if(options.onsuccess) options.onsuccess(xhr);
                else console.warn("Resource loaded, define onsuccess",xhr);
            }
        }
        try {
            xhr.send(options.post);
        } catch(e) {
            console.error("Error sending request",xhr);
        }
    }

    /**
        options:
            url,
            post - for send as body (optional),
            onsuccess - function,
            onerror - function (optional)
    */
    function getJSON(options) {
        var args = options || {};
        var onsuccess = options.onsuccess;
        options.onsuccess = function(xhr){
            if(onsuccess) {
                try {
                    var text = xhr.responseText;
                    text = text.replace(/\/\*[\s\S]*?\*\//g, "");
                    var json = JSON.parse(text);
                    onsuccess(json, xhr);
                } catch(e) {
                    if(options.onerror) options.onerror(ERRORS.INCORRECT_JSON, xhr);
                    else console.error(e);
                }
            }
        };
        getRemote(options);

        /*var xhr = new XMLHttpRequest();
        xhr.open("GET", url, true);
        xhr.onreadystatechange = function() { // (3)
            if (xhr.readyState != 4) return;
            if (xhr.status != 200) {
                if(fallback) fallback(ERRORS.ERROR_LOADING, xhr);
                else console.error("Error loading resource",xhr);
            } else {
                if(callback) {
                    try {
                        var json = JSON.parse(xhr.responseText);
                        callback(json, xhr);
                    } catch(e) {
                        if(fallback) fallback(ERRORS.INCORRECT_JSON, xhr);
                        else console.error(e);
                    }
                }
            }
        }
        try {
            xhr.send();
        } catch(e) {
//            if(fallback) fallback(ERRORS.ERROR_LOADING, xhr);
//            else console.error("Error loading resource",xhr);
            console.error("Error sending request",xhr);
        }*/
    }

    function drawer(options, appendTo) {
//        collapsed = options.collapsed;
        options.collapsed = options.collapsed || "drawer:collapsed";
        var collapsed = load(options.collapsed);

        var footerButton;
        var footerButtonCollapseDiv;
        var footerButtonExpandDiv;

        var footerButtonSvg = {
            xmlns:"http://www.w3.org/2000/svg",
            viewbox:"2 2 14 14",
            fit: "",
            version:"1.1",
            width: 24,
            height: 24,
            preserveAspectRatio: "xMidYMid meet",
            className: "drawer-menu-item-icon drawer-footer-button",
            onclick: function(e) {
                u.save(options.collapsed, !collapsed);
                this.replaceChild(collapsed ? footerButtonExpandDiv : footerButtonCollapseDiv, this.firstChild);
            }
        };
        var footerButtonCollapsePath = {
            xmlns:"http://www.w3.org/2000/svg",
            d: "M5.46 8.846l3.444-3.442-1.058-1.058-4.5 4.5 4.5 4.5 1.058-1.057L5.46 8.84zm7.194 4.5v-9h-1.5v9h1.5z",
        };
        var footerButtonExpandPath = {
            xmlns:"http://www.w3.org/2000/svg",
            d: "M5.46 8.846l3.444-3.442-1.058-1.058-4.5 4.5 4.5 4.5 1.058-1.057L5.46 8.84zm7.194 4.5v-9h-1.5v9h1.5z"
        };

        var layout = u.create(HTML.DIV, {
            className:"drawer changeable" + (collapsed ? " drawer-collapsed" : ""),
            tabindex: -1,
            onblur: function(){
                 this.close();
                 return true;
            },
            open: function() {
                 this.classList.add("drawer-open");
                 this.scrollTop = 0;
                 this.menu.scrollTop = 0;
                 this.focus();
            },
            close: function(){
                 this.classList.remove("drawer-open");
            },
            toggle: function() {
                if(this.classList.contains("drawer-open")) {
                    this.blur();
                } else {
                    this.open();
                }
            },
            items: {},
            sections: [],
            toggleWidth: function(force) {
                collapsed = !collapsed;
                if(force != undefined) collapsed = force;
                u.save("drawer:collapsed", collapsed);
                layout.toggleButton.innerHTML = collapsed ? "last_page" : "first_page";
                layout.classList[collapsed ? "add" : "remove"]("drawer-collapsed");
                if(options.onwidthtoggle) options.onwidthtoggle();
            }
         });
         appendTo.insertBefore(layout,appendTo.firstChild);


         layout.frame = u.create("iframe", {width:"100%",height:"1%", className:"drawer-iframe"}, layout);
         layout.frame.contentWindow.addEventListener("resize",function(){
            if(!layout.resizeTask) layout.resizeTask = setTimeout(function(){
                if(options.ontoggle) options.ontoggle();
                delete layout.resizeTask;
            }, 500);
         });

         layout.header = u.create(HTML.DIV, { className:"drawer-header changeable" }, layout);
         if(options.logo) {
             u.create(HTML.IMG, {
                className:"drawer-header-logo changeable",
                src:options.logo.src,
                onclick: options.logo.onclick
             }, layout.header);
         }
         layout.headerPrimary = u.create(HTML.DIV, {className:"drawer-header-name changeable", onclick: function(evt){
                layout.blur();
                if(options.onprimaryclick) options.onprimaryclick();
            }}, layout.header);
         layout.headerTitle = u.create(HTML.DIV, {className:"drawer-header-title changeable", innerHTML:options.title}, layout.header);
         u.create(HTML.DIV, {className:"drawer-header-subtitle changeable", innerHTML: options.subtitle }, layout.header);


        layout.menu = u.create(HTML.DIV, {className:"drawer-menu changeable"}, layout);
        for(var i=0;i<10;i++){
            layout.sections[i] = u.create(HTML.DIV, {className:"hidden" + (i==9 ? "" : " divider")}, layout.menu);
        }

        layout.add = function(section,id,name,icon,callback) {
            layout.items[id] = {
                name:name,
                icon:icon,
                callback:callback
            };
            var th = u.create(HTML.DIV, {
                className:"drawer-menu-item",
                onclick: function (event) {
                    var self = this;
                    setTimeout(function () {
                        layout.blur();
                        callback.call(self,event);
                    }, 100);
                },
                hide: function() {
                    this.classList.add("hidden");
                    this.fixShowing();
                    return this;
                },
                show: function() {
                    this.classList.remove("hidden");
                    this.fixShowing();
                    return this;
                },
                enable: function() {
                    this.classList.remove("disabled");
                    return this;
                },
                disable: function() {
                    this.classList.add("disabled");
                    return this;
                },
                fixShowing: function() {
                    var parent = th.parentNode;
                    var shown = false;
                    for(var i in parent.childNodes) {
                        if(parent.childNodes.hasOwnProperty(i)) {
                            if(!parent.childNodes[i].classList.contains("hidden")) shown = true;
                        }
                    }
                    if(shown) parent.show();
                    else parent.hide();
                },
                increaseBadge: function() {
                    var val = parseInt(this.badge.innerHTML || "0");
                    val ++;
                    this.badge.innerHTML = val;
                    this.showBadge();
                },
                showBadge: function() {
                    this.badge.show();
                },
                hideBadge: function() {
                    this.badge.hide();
                    this.badge.innerHTML = "0";
                },

            }, layout.sections[section]);

            if(icon) {
                if(icon.constructor === String) {
                    u.create(HTML.DIV, { className:"drawer-menu-item-icon notranslate", innerHTML: icon }, th);
                } else {
                    th.appendChild(icon);
                }
            }
            if(callback) {
                u.create(HTML.DIV, {
                    className: "drawer-menu-item-label",
                    innerHTML: name
                }, th);
            }
            th.badge = u.create(HTML.DIV, { className:"drawer-menu-item-badge hidden", innerHTML: "0" }, th);
            layout.sections[section].show();
            return th;
        }

        layout.footer = u.create(HTML.DIV, { className:"drawer-footer"}, layout);

        footerButtonCollapseDiv = u.create(HTML.PATH, footerButtonCollapsePath);
        footerButtonExpandDiv = u.create(HTML.PATH, footerButtonExpandPath);

        layout.toggleButton = u.create(HTML.DIV, {className: "drawer-menu-item-icon drawer-footer-button notranslate", innerHTML: collapsed ? "last_page" : "first_page", onclick: function(e){
            layout.toggleWidth();
        }}, layout.footer);
        if(options.footer) {
            u.create(HTML.DIV, options.footer, layout.footer);
        }

        return layout;
    }

    function toast() {
        var toast = create(HTML.DIV, {className:"toast shadow hidden", onclick: function(){ this.hide(); }});
        toast.show = function(text,delay){
           clearTimeout(toast.hideTask);
           lang.updateNode(toast, text);
           toast.classList.remove("hidden");
           delay = delay || 5000;
           if(delay > 0) {
               toast.hideTask = setTimeout(function(){
                   toast.hide();
               },delay);
           }
       };
        return toast;
    }

    function notification(options) {
        if(!options.persistent && !document.hidden) return;
        if(u.load("main:disable_notification")) return;
        if (!("Notification" in window)) {
            console.error("This browser does not support desktop notification");
            return;
        } else if (Notification.permission.toLowerCase() === "granted") { // check if notifications are allowed
            var title = options.title;
            delete options.title;
            var notif = new Notification(title, options);
            notif.onclick = function(e){
                notif.close();
                window.focus();
                if(options.onclick) options.onclick(e);
                else {console.warn("Redefine onclick.")}
            }
            if(options.duration) {
                setTimeout(function(){
                    notif.close();
                }, options.duration);
            }
        } else if (Notification.permission.toLowerCase() !== 'denied') { // request for notifications granted
            Notification.requestPermission(function (permission) {
                if (permission.toLowerCase() === "granted") {
                    notification(options);
                }
            });
        }
    }

    function actionBar(options, appendTo) {

        var actionbar = create(HTML.DIV, {className:"actionbar" + (options.className ? " " + options.className : "")});
        create(HTML.SPAN, {innerHTML:"menu", className:"actionbar-button", onclick: options.onbuttonclick, onfocus:function(){}}, actionbar);
        var label = create(HTML.DIV, {className:"actionbar-label"}, actionbar);
        actionbar.titleNode = create(HTML.DIV, {className:"actionbar-label-title", innerHTML: options.title || ""}, label);
        actionbar.subtitle = create(HTML.DIV, {className:"actionbar-label-subtitle", innerHTML: options.subtitle || ""}, label);

        if(appendTo) appendTo.appendChild(actionbar);
        return actionbar;
    }

    function copyToClipboard(input) {
        if(!input) return false;
        input.focus();
        input.select();

        try {
            return document.execCommand('copy');
        } catch(err) {
            return false;
        }
    }

    function table(options, appendTo) {
        options.className = "table" + (options.className ? " " + options.className : "");
        var table = create(HTML.DIV, {
            className:options.className,
            filter: function() {
                if(!options.caption.items) return;
                for(var i in table.rows) {
                    var valid = true;
                    for(var j in table.filter.options) {
                        if(table.filter.options[j]) {
                            valid = table.filter.options[j].call(table,table.rows[i]);
                        }
                        if(!valid) break;
                    }
                    if(valid) table.rows[i].show();
                    else table.rows[i].hide();
                }
            },
            rows: [],
            saveOption: function(name,value) {
                if(options.id) {
                    delete savedOptions[name];
                    savedOptions[name] = value;
                    save("table:" + options.id, savedOptions);
                }
            },
            add: function(row) {
                 var cells;
                 var res = create(HTML.DIV, {className:"tr"+(row.onclick ? " clickable":"")+(row.className ? " "+row.className : ""), onclick: row.onclick, cells: [] }, table.body);
                 for(var i in row.cells) {
                     var item = row.cells[i];
                     item.className = "td" + (item.className ? " " + item.className : "");
                     item.innerHTML = item.innerHTML || item.label;
                     res.cells.push(create(HTML.DIV, item, res));
                 }
                 table.rows.push(res);
                 table.placeholder.hide();
                table.update();
                 return res;
            },
            update: function() {
                if(!options.caption.items) return;

                clearTimeout(table.updateTask);
                table.updateTask = setTimeout(function(){
                    table.filter();
                    for(var i in table._sorts) {
                         try{
                             var index = table._sorts[i].index;
                             table.head.cells[index].sort = table._sorts[i].mode;
                             table.sort(index);
                         } catch(e) {
                             console.error(e);
                         }
                    }
                }, 0);

            },
            sort: function(index) {
               if(!options.caption.items) return;

               var sort = table.head.cells[index].sort;

               table.head.cells[index].firstChild.show();
               table.head.cells[index].firstChild.classList[sort > 0 ? "add" : "remove"]("table-sort-descend");

               for(var i = 0; i < table.body.childNodes.length-1; i++) {
                   var icrit = i;
                   var crit = table.body.childNodes[i];
                   var sortCrit = crit.cells[index].sort;
                   var textCrit = crit.cells[index].innerText;

                   textCrit = ""+(sortCrit == undefined ? textCrit : sortCrit);
                   var parsedCrit = parseInt(textCrit);
                   for(var j = i + 1; j < table.body.childNodes.length; j++) {
                       var b = table.body.childNodes[j];
                       var textB = ""+(b.cells[index].sort == undefined ? b.cells[index].innerText : b.cells[index].sort);
                       var parsedB = parseInt(textB);
                       var cmp;
                       if(""+parsedCrit == textCrit && ""+parsedB == textB){
                           cmp = (parsedCrit > parsedB ? 1 : (parsedCrit < parsedB ? -1 : 0));
                       } else {
                           cmp = textCrit.toLocaleLowerCase().localeCompare(textB.toLocaleLowerCase());
                       }
                       if(sort == cmp) {
                           crit = b;
                           textCrit = ""+(crit.cells[index].sort == undefined ? crit.cells[index].innerText : crit.cells[index].sort);
                           parsedCrit = parseInt(textCrit);
                           icrit = j;
                       }
                   }
                   if(icrit != i) {
                       table.body.appendChild(table.body.replaceChild(table.body.childNodes[icrit], table.body.childNodes[i]));
                   }
               }
            },
            _sorts: [],
            sorts: function(options) {
                if(!options) return table._sorts;
                for(var i in table._sorts) {
                    if(table._sorts[i].index == options.index) {
                        table._sorts.splice(i,1);
                        break;
                    }
                }
                if(options.mode) table._sorts.push(options);
                table.saveOption("sorts",table._sorts);
            },
        });

        if(appendTo) appendTo.appendChild(table);

        options.caption = options.caption || {};
        options.caption.className = "thead" + (options.caption.className ? " "+options.caption.className : "");
        if(options.caption.items) {
            table.head = create(HTML.DIV, {className:options.caption.className}, table);
            table.head.cells = [];
//            var div = create(HTML.DIV, {className:"tr"}, table.head);
            for(var i in options.caption.items) {
                var item = options.caption.items[i];
                item.className = "th"+(item.className ? " "+item.className : "");
                item.innerHTML = item.innerHTML || item.label;
                item.index = i;
                item.sort = 0;
                item.onclick = function() {
                    this.sort ++;
                    if(this.sort == 0) this.sort ++;
                    else if(this.sort > 1) this.sort = -1;

                    table.sorts({ index: this.index, mode: this.sort });
                    table.sort(this.index);

                };
                item.ondblclick = function() {
                    this.sort = 0;
                    table.sorts({ index: this.index });
                   table.head.cells[this.index].firstChild.hide();
                    table.update();
                }
                var cell = create(HTML.DIV, item, table.head);
                table.head.cells.push(cell);
                cell.insertBefore(create(HTML.DIV,{className:"table-sort hidden", innerHTML:"sort"}),cell.firstChild);
            }

            table.resetButton = u.create(HTML.DIV, {
                className: "table-reset-button notranslate",
                innerHTML: "clear_all",
                title: "Reset customizations",
                onclick: function() {
                    table._sorts = [];
                    table.saveOption("sorts");
                    for(var i in table.head.cells) {
                        table.head.cells[i].sort = 0;
                        table.head.cells[i].firstChild.hide();
                    }
                    table.filter.clear();
                    table.filterInput.value = "";
                    table.filterInput.focus();
                    table.filterInput.apply();
                    table.filterInput.blur();
                    table.update();
                }
            }, table);

            table.filterLayout = u.create(HTML.DIV, {
                className: "table-filter"
            }, table);

            table.filterButton = u.create(HTML.DIV, {
                className: "table-filter-button notranslate",
                innerHTML: "search",
                title: "Filter",
                onclick: function() {
                    table.filterButton.hide();
                    table.filterInput.classList.remove("hidden");
                    table.filterInput.focus();
                }
            }, table.filterLayout);

            table.filterInput = create(HTML.INPUT, {
                className: "table-filter-input hidden",
                tabindex: -1,
                onkeyup: function(evt) {
                    if(evt.keyCode == 27) {
                        evt.preventDefault();
                        evt.stopPropagation();
                        if(this.value) {
                            this.value = "";
                        } else {
                            this.blur();
                        }
                    }
                    this.apply();
                },
                onblur: function() {
                    if(!this.value) {
                        table.filterInput.classList.add("hidden");
                        table.filterButton.show();
                    }
                },
                onclick: function() {
                    this.focus();
                },
                _filter: function(row) {
                    var contains = false;
                    for(var i in row.cells) {
                        if(row.cells[i].innerText.toLowerCase().indexOf(this.filterInput.value.toLowerCase()) >= 0) return true;
                    }
                    return false;
                },
                apply: function() {
                    if(this.value) {
                        table.filterClear.show();
                    } else {
                        table.filterClear.hide();
                    }
                    var counter = 0;
                    table.filter.add(table.filterInput._filter);
                    table.filter();
    //                    for(var i in dialog.itemsLayout.childNodes) {
    //                        if(!dialog.itemsLayout.childNodes.hasOwnProperty(i)) continue;
    //                        if(!this.value || (dialog.itemsLayout.childNodes[i].innerText && dialog.itemsLayout.childNodes[i].innerText.toLowerCase().match(this.value.toLowerCase()))) {
    //                            dialog.itemsLayout.childNodes[i].show();
    //                            counter++;
    //                        } else {
    //                            dialog.itemsLayout.childNodes[i].hide();
    //                        }
    //                    }
    //                    if(counter) {
    //                        dialog.filterPlaceholder.hide();
    //                        dialog.itemsLayout.show();
    //                    } else {
    //                        dialog.filterPlaceholder.show();
    //                        dialog.itemsLayout.hide();
    //                    }
                }
            }, table.filterLayout);
            table.filterClear = create(HTML.DIV, {
                className: "table-filter-clear hidden notranslate",
                innerHTML: "clear",
                onclick: function() {
                    table.filterInput.value = "";
                    table.filterInput.focus();
                    table.filterInput.apply();
                }
            }, table.filterLayout);

            function normalizeFunction(func) {
                if(!func) return null;
                save(":functemp", func);
                func = load(":functemp");
                save(":functemp");
                return func;
            }
            function checkIfFilterInList(filter) {
                if(!filter) return true;
                for(var i in table.filter.options) {
                    if(table.filter.options[i].toString() == filter.toString()) return i;
                }
                return -1;
            }

            table.filter.set = function(filterOption) {
                if(filterOption) {
                    table.filter.options = [normalizeFunction(filterOption)];
                } else {
                    table.filter.options = null;
                }
                table.saveOption("filter",table.filter.options);
                table.filter();
            }
            table.filter.add = function(filterOption) {
                table.filter.options = table.filter.options || [];
                var newFilterOption = normalizeFunction(filterOption);
                if(checkIfFilterInList(newFilterOption) < 0) {
                    table.filter.options.push(newFilterOption);
                }
                table.saveOption("filter",table.filter.options);
                table.filter();
            }

            table.filter.remove = function(filterOption) {
                table.filter.options = table.filter.options || [];
                var newFilterOption = normalizeFunction(filterOption);
                var index = checkIfFilterInList(newFilterOption);
                if(index >= 0) {
                    table.filter.options.splice(index,1);
                }
                table.saveOption("filter",table.filter.options);
                table.filter();
            }
            table.filter.clear = function() {
                table.filter.options = null;
                table.saveOption("filter",table.filter.options);
                table.filter();
            }
        }

        table.body = create(HTML.DIV, {className:"tbody"}, table);


        table.placeholder = create(HTML.DIV, {
            className:"table-placeholder",
            innerHTML: options.placeholder || "No data",
            show: function(text){
                clear(table.body);
                if(text) table.placeholder.innerHTML = text;
                table.placeholder.classList.remove("hidden");
            }
        }, table);

        if(options.id) {
            var savedOptions = load("table:" + options.id) || {};
            table.filter.options = savedOptions.filter;
            table._sorts = savedOptions.sorts || [];

            table.filter();
        }

        return table;
    }

    var loadingHolder;
    function loading(progress) {
        loadingHolder = loadingHolder || create("div", {style:{
            position: "fixed", top: 0, bottom: 0, left: 0, right: 0,
            zIndex: 10000, backgroundColor: "white", display: "flex", flexDirection: "column",
            justifyContent: "center", alignItems: "center", fontFamily: "sans-serif"
        }}, document.body)
            .place(HTML.DIV, {className:"loading-progress-circle"})
            .place(HTML.DIV, {className:"loading-progress-title", innerHTML: "Loading, please wait... "})
            .place(HTML.DIV, {className:"loading-progress-subtitle hidden"});
        if(progress) {
            loadingHolder.lastChild.innerHTML = progress;
            loadingHolder.lastChild.show();
        } else {
            loadingHolder.lastChild.hide();
        }
    }
    loading.hide = function() {
        loadingHolder.hide();
    }

    options = options || {};
    if(options.exportConstants) {
        window.HTML = HTML;
        window.ERRORS = ERRORS;
        window.DRAWER = DRAWER;
    }

    return {
        HTML:HTML,
        ERRORS:ERRORS,
        DRAWER:DRAWER,
        actionBar:actionBar,
        byId:byId,
        clear: clear,
        cloneAsObject:cloneAsObject,
        context:options.context || "",
        copyToClipboard:copyToClipboard,
        create: create,
        destroy:destroy,
        dialog:dialog,
        drawer:drawer,
        getJSON:getJSON,
        keys: keys,
        lang:lang,
        load:load,
        loading:loading,
        loadForContext:loadForContext,
        notification:notification,
        origin:options.origin || "edequate",
        require:require,
        save:save,
        saveForContext:saveForContext,
        sprintf:sprintf,
        table:table,
        toast:new toast(),
    }
}
