/**
 * Created 1/20/17.
 */

/**
 * Created 1/19/17.
 */
function Utils() {

    var normalizeName = function(name) {
        if(name == "className"){
            name = "class";
        } else if(name.toLowerCase() == "frameborder") {
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

    var create = function(name, properties, appendTo) {
        var el = document.createElement(name);

        if(properties) {
            if(properties instanceof HTMLElement) {
                el.appendChild(properties);
            } else if(properties.constructor === Object) {
                for(var x in properties) {
                    if(x == "innerHTML" || x == "innerText") {
                        el[x] = properties[x];
                    } else if(x == "content") {
                        el.appendChild(properties[x]);
                    } else if(x.indexOf("on") >= 0) {
                        var action = x.substr(2).toLowerCase();
                        var call = properties[x];
                        el.addEventListener(action, call/*function(){
                            console.log(call);

                        }*/);
//                        el.add
                    } else {
                        var name = x, value = properties[x];
                        if(value != undefined) {
                            if(value.constructor === Object) {
                                var v = "";
                                for(var x in value) {
                                    v += normalizeName(x) + ": " + value[x] + "; ";
                                }
                                value = v;
                            }
                            el.setAttribute(normalizeName(name), value);
                        }
                    }
                }
            } else if (properties.constructor === String || properties.constructor === Number) {
                el.innerHTML = properties;
            }
        }
        if(appendTo) {
            appendTo.appendChild(el);
        }

        return el;
    }

    var clear = function(node) {
        if(!node) return;
        for(var i = node.children.length-1; i>=0; i--) {
            node.removeChild(node.children[i]);
        }
    }

    var keys = function(o) {
        var keys = [];
        for(var x in o) {
            keys.push(x);
        }
        return keys;
    }

    var createPage = function(holder) {
        window.history.pushState({}, null, "/admin/" + holder.page);

        var div = document.getElementsByClassName("right")[0];
        clear(div);

        var menu = document.getElementsByClassName("menu")[0];
        var h1 = create("div", {}, create("div",{className:"title"}, div));
        create("span", {innerHTML:"menu", className:"menu-button", onclick: function(){
            try {
                menu.classList.add("menu-open");
                this.classList.add("menu-button-open");
                menu.focus();
            } catch(e) {
                console.err(e);
            }
        }}, h1);
        create("span", {innerHTML:holder.title, className:"title"}, h1);

        create("div", {className:"alert"}, div);

        div = create("div", {className:"content"}, div);
        return div;
    }

    var showAlert = function(text) {
        var div = document.getElementsByClassName("alert")[0];
        div.innerHTML = text;
        div.style.display = "block";
    }

    return {
        create: create,
        clear: clear,
        keys: keys,
        createPage: createPage,
        showAlert: showAlert,
    }
}
