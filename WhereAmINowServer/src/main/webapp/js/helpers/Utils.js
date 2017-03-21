/**
 * Created 1/19/17.
 */
window.HTML = {
    DIV: "div",
    LINK:"link",
    A:"a",
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
    INPUT:"input",
    TEXT:"text",
    TEXTAREA:"textarea",
    HIDDEN:"hidden",
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
};
window.ERRORS = {
    NOT_EXISTS: 1,
    NOT_AN_OBJECT: 2,
};

function Utils() {

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

    function normalizeName(name) {
        if(name == HTML.CLASSNAME){
            name = "class";
        } else if(name.toLowerCase() == "frameborder") {
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
                        el[x] = properties[x];
                    } else if(x == HTML.CONTENT && properties[x].constructor !== String) {
                        el.appendChild(properties[x]);
//                    } else if(x == "async") {
//                        el.appendChild(properties[x]);
                    } else if(x.indexOf("on") == 0) {
                        var action = x.substr(2).toLowerCase();
                        var call = properties[x];
                        el.addEventListener(action, call/*function(){
                            console.log(call);

                        }*/, false);
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
            if(appendTo.children.length > 0) {
                if(position == "first") {
                    appendTo.insertBefore(el,appendTo.children[0]);
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

    function createPage(holder) {
        window.history.pushState({}, null, "/admin/" + holder.page);

        var div = document.getElementsByClassName("right")[0];
        clear(div);

        var menu = document.getElementsByClassName("menu")[0];
        var h1 = create(HTML.DIV,{className:"actionbar"}, div);
        create(HTML.SPAN, {innerHTML:"menu", className:"menu-button", onclick: function(){
            try {
                menu.classList.add("menu-open");
                menu.focus();
            } catch(e) {
                console.err(e);
            }
        }}, h1);
        create(HTML.SPAN, {innerHTML:holder.title, className:"title"}, h1);

        create(HTML.DIV, {className:"alert"}, div);

        div = create(HTML.DIV, {className:HTML.CONTENT}, div);
        return div;
    }

    function showAlert(text) {
        var div = document.getElementsByClassName("alert")[0];
        div.innerHTML = text;
        div.style.display = HTML.BLOCK;
    }

    function byId(id) {
        return document.getElementById(id);
    }

    function getHexColor(number){
        return "#"+((number)>>>0).toString(16).slice(-6);
    }

    function require(name, onload, onerror, context){
        var parts = name.split("/");
        var filename = parts[parts.length-1];
        var onlyname = filename.split(".")[0];
        var needInstantiate = false;
        if(onerror && onerror.constructor !== Function) {
            context = onerror;
            onerror = function(){};
        }
        if(!filename.match(/\.js$/)) {
            needInstantiate = true;
            name += ".js";
        }
        create(HTML.SCRIPT, {src: name, dataName: needInstantiate ? onlyname : null, onload: function(e) {
            if (onload) {
                var a;
                if(needInstantiate) {
                    if(window[this.dataset.name] && window[this.dataset.name].constructor === Function) {
                        a = new window[this.dataset.name](context);
                        a.moduleName = this.dataset.name;
                    } else {
                        onerror(ERRORS.NOT_AN_OBJECT, this.dataset.name, e);
                    }
                }
                onload(a);
            }
        }, onerror: function(e) {
            if(onerror) {
                onerror(ERRORS.NOT_EXISTS, this.dataset.name, e);
            }
        }, async:"", defer:""}, document.head);
    }

    function save(name, value) {
        if(value) {
            localStorage["WAIN:" + name] = JSON.stringify(value);
        } else {
            delete localStorage["WAIN:" + name];
        }
    }

    function load(name) {
        var value = localStorage["WAIN:"+name];
        if(value) {
            return JSON.parse(value);
        } else {
            return null;
        }
    }

    function getUuid(callback) {
        var uuid = load("uuid");

        if(!uuid) {
            if(callback) {
                new Fingerprint2().get(function(result, components){
                    console.log(result); //a hash, representing your device fingerprint
                    console.log(components); // an array of FP components
                    if(!result) {
                        var d = new Date().getTime();
                        result = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                            var r = (d + Math.random()*16)%16 | 0;
                            d = Math.floor(d/16);
                            return (c=='x' ? r : (r&0x3|0x8)).toString(16);
                        });
                    }
                    save("uuid", result);
                    callback();
                });
            } else {
                console.error("UUID not defined.");
            }
        }
        if(callback) callback();
        return uuid;
    }

    function jsonToLocation(json) {
        var loc = {};
        loc.coords = {};
        loc.provider = json[USER.PROVIDER];
        loc.coords.latitude = json[USER.LATITUDE];
        loc.coords.longitude = json[USER.LONGITUDE];
        loc.coords.altitude = json[USER.ALTITUDE] || null;
        loc.coords.accuracy = json[USER.ACCURACY] || null;
        loc.coords.heading = json[USER.BEARING] || null;
        loc.coords.speed = json[USER.SPEED] || null;
        loc.timestamp = json[REQUEST.TIMESTAMP];
        return loc;
    }

    function locationToJson(location) {
        var json = {};
        json[USER.PROVIDER] = location.provider || "fused";
        location.coords = location.coords || {};
        json[USER.LATITUDE] = location.coords.latitude;
        json[USER.LONGITUDE] = location.coords.longitude;
        json[USER.ALTITUDE] = location.coords.altitude || 0;
        json[USER.ACCURACY] = location.coords.accuracy || 50;
        json[USER.BEARING] = location.coords.heading || 0;
        json[USER.SPEED] = location.coords.speed || 0;
        json[REQUEST.TIMESTAMP] = location.timestamp;
        return json;
    }

    function latLng(location) {
        if(!location || !location.coords) return null;
        return new google.maps.LatLng(location.coords.latitude, location.coords.longitude);
    }

    function smoothInterpolated(duration, callback, postCallback) {
        var start = new Date().getTime();

        // var a = setInterval(function(){
        //     var t,v,elapsed;
        //     elapsed = new Date().getTime() - start;
        //     t = elapsed / duration;
        //     v = elapsed / duration;
        //     callback(t,v);
        // }, 16);
        // setTimeout(function() {
        //     clearInterval(a);
        //     if(postCallback) postCallback();
        // }, duration+1);
        postCallback();
        var a = null;
        return a;
    }

    function getEncryptedHash(s) {
        function L(k, d) {
            return (k << d) | (k >>> (32 - d))
        }

        function K(G, k) {
            var I, d, F, H, x;
            F = (G & 2147483648);
            H = (k & 2147483648);
            I = (G & 1073741824);
            d = (k & 1073741824);
            x = (G & 1073741823) + (k & 1073741823);
            if (I & d) {
                return (x ^ 2147483648 ^ F ^ H)
            }
            if (I | d) {
                if (x & 1073741824) {
                    return (x ^ 3221225472 ^ F ^ H)
                } else {
                    return (x ^ 1073741824 ^ F ^ H)
                }
            } else {
                return (x ^ F ^ H)
            }
        }

        function r(d, F, k) {
            return (d & F) | ((~d) & k)
        }

        function q(d, F, k) {
            return (d & k) | (F & (~k))
        }

        function p(d, F, k) {
            return (d ^ F ^ k)
        }

        function n(d, F, k) {
            return (F ^ (d | (~k)))
        }

        function u(G, F, aa, Z, k, H, I) {
            G = K(G, K(K(r(F, aa, Z), k), I));
            return K(L(G, H), F)
        }

        function f(G, F, aa, Z, k, H, I) {
            G = K(G, K(K(q(F, aa, Z), k), I));
            return K(L(G, H), F)
        }

        function D(G, F, aa, Z, k, H, I) {
            G = K(G, K(K(p(F, aa, Z), k), I));
            return K(L(G, H), F)
        }

        function t(G, F, aa, Z, k, H, I) {
            G = K(G, K(K(n(F, aa, Z), k), I));
            return K(L(G, H), F)
        }

        function e(G) {
            var Z;
            var F = G.length;
            var x = F + 8;
            var k = (x - (x % 64)) / 64;
            var I = (k + 1) * 16;
            var aa = Array(I - 1);
            var d = 0;
            var H = 0;
            while (H < F) {
                Z = (H - (H % 4)) / 4;
                d = (H % 4) * 8;
                aa[Z] = (aa[Z] | (G.charCodeAt(H) << d));
                H++
            }
            Z = (H - (H % 4)) / 4;
            d = (H % 4) * 8;
            aa[Z] = aa[Z] | (128 << d);
            aa[I - 2] = F << 3;
            aa[I - 1] = F >>> 29;
            return aa
        }

        function B(x) {
            var k = "", F = "", G, d;
            for (d = 0; d <= 3; d++) {
                G = (x >>> (d * 8)) & 255;
                F = "0" + G.toString(16);
                k = k + F.substr(F.length - 2, 2)
            }
            return k
        }

        function J(k) {
            k = k.replace(/rn/g, "n");
            var d = "";
            for (var F = 0; F < k.length; F++) {
                var x = k.charCodeAt(F);
                if (x < 128) {
                    d += String.fromCharCode(x)
                } else {
                    if ((x > 127) && (x < 2048)) {
                        d += String.fromCharCode((x >> 6) | 192);
                        d += String.fromCharCode((x & 63) | 128)
                    } else {
                        d += String.fromCharCode((x >> 12) | 224);
                        d += String.fromCharCode(((x >> 6) & 63) | 128);
                        d += String.fromCharCode((x & 63) | 128)
                    }
                }
            }
            return d
        }

        var C = Array();
        var P, h, E, v, g, Y, X, W, V;
        var S = 7, Q = 12, N = 17, M = 22;
        var A = 5, z = 9, y = 14, w = 20;
        var o = 4, m = 11, l = 16, j = 23;
        var U = 6, T = 10, R = 15, O = 21;
        s = J(s);
        C = e(s);
        Y = 1732584193;
        X = 4023233417;
        W = 2562383102;
        V = 271733878;
        for (P = 0; P < C.length; P += 16) {
            h = Y;
            E = X;
            v = W;
            g = V;
            Y = u(Y, X, W, V, C[P + 0], S, 3614090360);
            V = u(V, Y, X, W, C[P + 1], Q, 3905402710);
            W = u(W, V, Y, X, C[P + 2], N, 606105819);
            X = u(X, W, V, Y, C[P + 3], M, 3250441966);
            Y = u(Y, X, W, V, C[P + 4], S, 4118548399);
            V = u(V, Y, X, W, C[P + 5], Q, 1200080426);
            W = u(W, V, Y, X, C[P + 6], N, 2821735955);
            X = u(X, W, V, Y, C[P + 7], M, 4249261313);
            Y = u(Y, X, W, V, C[P + 8], S, 1770035416);
            V = u(V, Y, X, W, C[P + 9], Q, 2336552879);
            W = u(W, V, Y, X, C[P + 10], N, 4294925233);
            X = u(X, W, V, Y, C[P + 11], M, 2304563134);
            Y = u(Y, X, W, V, C[P + 12], S, 1804603682);
            V = u(V, Y, X, W, C[P + 13], Q, 4254626195);
            W = u(W, V, Y, X, C[P + 14], N, 2792965006);
            X = u(X, W, V, Y, C[P + 15], M, 1236535329);
            Y = f(Y, X, W, V, C[P + 1], A, 4129170786);
            V = f(V, Y, X, W, C[P + 6], z, 3225465664);
            W = f(W, V, Y, X, C[P + 11], y, 643717713);
            X = f(X, W, V, Y, C[P + 0], w, 3921069994);
            Y = f(Y, X, W, V, C[P + 5], A, 3593408605);
            V = f(V, Y, X, W, C[P + 10], z, 38016083);
            W = f(W, V, Y, X, C[P + 15], y, 3634488961);
            X = f(X, W, V, Y, C[P + 4], w, 3889429448);
            Y = f(Y, X, W, V, C[P + 9], A, 568446438);
            V = f(V, Y, X, W, C[P + 14], z, 3275163606);
            W = f(W, V, Y, X, C[P + 3], y, 4107603335);
            X = f(X, W, V, Y, C[P + 8], w, 1163531501);
            Y = f(Y, X, W, V, C[P + 13], A, 2850285829);
            V = f(V, Y, X, W, C[P + 2], z, 4243563512);
            W = f(W, V, Y, X, C[P + 7], y, 1735328473);
            X = f(X, W, V, Y, C[P + 12], w, 2368359562);
            Y = D(Y, X, W, V, C[P + 5], o, 4294588738);
            V = D(V, Y, X, W, C[P + 8], m, 2272392833);
            W = D(W, V, Y, X, C[P + 11], l, 1839030562);
            X = D(X, W, V, Y, C[P + 14], j, 4259657740);
            Y = D(Y, X, W, V, C[P + 1], o, 2763975236);
            V = D(V, Y, X, W, C[P + 4], m, 1272893353);
            W = D(W, V, Y, X, C[P + 7], l, 4139469664);
            X = D(X, W, V, Y, C[P + 10], j, 3200236656);
            Y = D(Y, X, W, V, C[P + 13], o, 681279174);
            V = D(V, Y, X, W, C[P + 0], m, 3936430074);
            W = D(W, V, Y, X, C[P + 3], l, 3572445317);
            X = D(X, W, V, Y, C[P + 6], j, 76029189);
            Y = D(Y, X, W, V, C[P + 9], o, 3654602809);
            V = D(V, Y, X, W, C[P + 12], m, 3873151461);
            W = D(W, V, Y, X, C[P + 15], l, 530742520);
            X = D(X, W, V, Y, C[P + 2], j, 3299628645);
            Y = t(Y, X, W, V, C[P + 0], U, 4096336452);
            V = t(V, Y, X, W, C[P + 7], T, 1126891415);
            W = t(W, V, Y, X, C[P + 14], R, 2878612391);
            X = t(X, W, V, Y, C[P + 5], O, 4237533241);
            Y = t(Y, X, W, V, C[P + 12], U, 1700485571);
            V = t(V, Y, X, W, C[P + 3], T, 2399980690);
            W = t(W, V, Y, X, C[P + 10], R, 4293915773);
            X = t(X, W, V, Y, C[P + 1], O, 2240044497);
            Y = t(Y, X, W, V, C[P + 8], U, 1873313359);
            V = t(V, Y, X, W, C[P + 15], T, 4264355552);
            W = t(W, V, Y, X, C[P + 6], R, 2734768916);
            X = t(X, W, V, Y, C[P + 13], O, 1309151649);
            Y = t(Y, X, W, V, C[P + 4], U, 4149444226);
            V = t(V, Y, X, W, C[P + 11], T, 3174756917);
            W = t(W, V, Y, X, C[P + 2], R, 718787259);
            X = t(X, W, V, Y, C[P + 9], O, 3951481745);
            Y = K(Y, h);
            X = K(X, E);
            W = K(W, v);
            V = K(V, g)
        }
        var i = B(Y) + B(X) + B(W) + B(V);
        return i.toLowerCase()
    }

    function dialog(options) {
        var dialog = create(HTML.DIV, {className:"modal shadow hidden"+(options.className ? " "+options.className : ""), tabindex:999},
            document.getElementsByClassName("right")[0]);

        dialog.opened = false;

        dialog.clearItems = function() {
            clear(dialog.itemsLayout);
            dialog.items = [];
        };

        dialog.addItem = function(item) {
            item = item || {};

            var x;
            if(item.type == HTML.DIV) {
                if(item.enclosed) {
                    x = create(HTML.DIV, {
                        className: "dialog-item-enclosed" + (item.className ? " " + item.className : "")
                    }, dialog.itemsLayout);
                    var enclosedButton, enclosedIcon;
                    enclosedButton = u.create(HTML.DIV, {className:"dialog-item-enclosed-button", onclick: function(){
                        if(x.body.classList.contains("hidden")) {
                            enclosedIcon.innerHTML = "expand_less";
                            x.body.classList.remove("hidden");
                        } else {
                            enclosedIcon.innerHTML = "expand_more";
                            x.body.classList.add("hidden");
                        }
                    }}, x);
                    enclosedIcon = u.create(HTML.DIV, {className:"dialog-item-enclosed-icon", innerHTML:"expand_more"}, enclosedButton);
                    u.create(HTML.DIV, {className:"dialog-item-enclosed-label", innerHTML: item.label || "Show more information"}, enclosedButton);
                    x.body = u.create(HTML.DIV, {className:"dialog-item-enclosed-body hidden", innerHTML:item.body || ""}, x);
                } else {
                    x = create(HTML.DIV, {
                        className: "dialog-item" + (item.className ? " " + item.className : ""),
                        innerHTML: item.label || item.title || item.innerHTML || ""
                    }, dialog.itemsLayout);
                }
            } else if(item.type == "hidden") {
                x = create(HTML.INPUT, {type:HTML.HIDDEN, value:item.value || ""}, dialog.itemsLayout);
            } else {
                var div = create(HTML.DIV, {className:"dialog-item dialog-item-input"}, dialog.itemsLayout);
                create(HTML.DIV, {
                    className:"dialog-item-label"+(item.className ? " "+item.className : ""),
                    innerHTML:item.label || ""
                }, div);
                var type = HTML.INPUT;
                if(item.type.toLowerCase() == HTML.TEXTAREA) type = HTML.TEXTAREA;

                x = create(type, {
                    type:item.type,
                    className:"dialog-item-input-"+type,
                    tabindex: i,
                    value:item.value || "",
                    onclick: function() { this.focus() },
                    onkeyup:function(e){
                        if(e.keyCode == 13 && this.type != HTML.TEXTAREA) {
                            dialog.onclose();
                            if(options.positive && options.positive.onclick) options.positive.onclick.call(dialog,items);
                        } else if(e.keyCode == 27) {
                            dialog.onclose();
                            if(options.negative && options.negative.onclick) options.negative.onclick.call(dialog,items);
                        }
                    }
                }, div);
            }
            items.push(x);
            return x;
        }

        dialog.onopen = function(){
            clearInterval(dialog.intervalTask);
            dialog.classList.remove("hidden");
            dialog.opened = true;

            var left = load("dialog:left:"+(options.id || (options.title && options.title.label)));
            var top = load("dialog:top:"+(options.id || (options.title && options.title.label)));
            if(left && top) {
                dialog.style.left = left;
                dialog.style.top = top;
                dialog.style.right = HTML.AUTO;
                dialog.style.bottom = HTML.AUTO;
            } else {
                left = dialog.offsetLeft;
                var leftMain = window.innerWidth;

                if(left >= leftMain || left == 0) {
                    dialog.style.left = ((window.innerWidth - dialog.offsetWidth) /2)+"px";
                    dialog.style.top = ((window.innerHeight - dialog.offsetHeight) /2)+"px";
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
            if(options.onopen) options.onopen.call(dialog,items);
            if(options.timeout) {
                var atom = options.timeout / 16;
                var current = 0;
                dialog.intervalTask = setInterval(function(){
                    current += 16;
                    progress.style.width = (current / options.timeout * 100) + "%";
                    if(current >= options.timeout) {
                        clearInterval(dialog.intervalTask);
                        dialog.onclose();
                    }
                }, 16);
            }
            return dialog;
        };

        dialog.onclose = function (){
            dialog.classList.add("hidden");
            dialog.opened = false;

            clearInterval(dialog.intervalTask);

            if(options.onclose) options.onclose.call(dialog,items);
        };
        dialog.addEventListener("keyup", function(e) {
            if(e.keyCode == 27) {
                dialog.onclose();
                if(options.negative && options.negative.onclick) options.negative.onclick.call(dialog,items);
            }
        });

        options = options || {};
        var items = [];

        var defaultCloseButton = {
             icon: "clear",
             className: "",
             onclick: function(e){
                 dialog.onclose();
                 if(options.negative && options.negative.onclick) options.negative.onclick.call(dialog,items);
             }
         };
        if(options.title) {
            if(options.title.constructor === String) {
                options.title = {
                    label: options.title,
                    className: "",
                    button: defaultCloseButton
                }
            } else {
                if(options.title.className) options.title.className = " " + options.title.className;
                options.title.button = options.title.button || defaultCloseButton;
                if(options.title.button.className) options.title.button.className = " " + options.title.button.className;
                options.title.button.onclick = options.title.button.onclick || function(){};

            }
            var titleLayout = create(HTML.DIV, {
                className:"dialog-title" + options.title.className,
                onmousedown: function(e) {
                    if(e.button != 0) return;
                    var position = dialog.getBoundingClientRect();
                    var offset = [ e.clientX, e.clientY ];
                    var moved = false;
                    function mouseup(e){
                        window.removeEventListener(HTML.MOUSEUP, mouseup, false);
                        window.removeEventListener(HTML.MOUSEMOVE, mousemove, false);
                        if(options.title && moved) {
                            save("dialog:left:"+(options.id || options.title.label), dialog.style.left);
                            save("dialog:top:"+(options.id || options.title.label), dialog.style.top);
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
                }
            }, dialog);
            dialog.titleLayout = create(HTML.DIV, {className:"dialog-title-label", innerHTML: options.title.label }, titleLayout);

            if(options.title.button && options.title.button.icon) {
                create(HTML.DIV, {className:"dialog-title-button"+ options.title.button.className, innerHTML:options.title.button.icon, onclick:options.title.button.onclick}, titleLayout);
            }
        }
        dialog.itemsLayout = create(HTML.DIV, {className:"dialog-items" +(options.itemsClassName ? " "+options.itemsClassName : "")}, dialog);
        for(var i in options.items) {
            var item = options.items[i];
            dialog.addItem(item);
        }
        dialog.items = items;
        var buttons = create(HTML.DIV, {className:"dialog-buttons hidden"}, dialog);
        if(options.positive && options.positive.label) {
            dialog.positive = create(HTML.BUTTON, {className:"dialog-button-positive", tabindex:98, onclick:function(){
                dialog.onclose();
                if(options.positive.onclick) options.positive.onclick.call(dialog,items);
            }, innerHTML: options.positive.label}, buttons);
            buttons.classList.remove("hidden");
        }
        if(options.negative && options.negative.label) {
            dialog.negative = create(HTML.BUTTON, {className:"dialog-button-negative", tabindex:99, onclick:function(){
                dialog.onclose();
                if(options.negative.onclick) options.negative.onclick.call(dialog,items);
            }, innerHTML: options.negative.label}, buttons);
            buttons.classList.remove("hidden");
        }
        if(options.neutral && options.neutral.label) {
            dialog.neutral = create("button", {className:"dialog-button-neutral", tabindex:100, onclick:function(){
                dialog.onclose();
                if(options.neutral.onclick) options.neutral.onclick.call(dialog,items);
            }, innerHTML: options.neutral.label}, buttons);
            buttons.classList.remove("hidden");
        }
        if(options.help) {
            create(HTML.BUTTON, {className:"dialog-help-button", onclick:options.help, innerHTML:"help_outline"}, dialog);
        }

        if(options.timeout) {
            var progressBar = create(HTML.DIV, {className:"dialog-progress-bar"}, dialog);
            var progress = create(HTML.DIV, {className:"dialog-progress-value"}, progressBar);
            progress.style.width = "0%";
        }

        return dialog;
    }

    function formatLengthToLocale(meters) {
        if(navigator.language && navigator.language.toLowerCase() == "en-us") {
            meters = meters * 3.2808399;
            if(meters < 530) {
                return sprintf.call("%s %s", meters.toFixed(0), "ft");
            } else {
                meters = meters / 5280;
                return sprintf.call("%s %s", meters.toFixed(1), "mi");
            }
        } else {
            var unit = "m";
            if (meters < 1) {
                meters *= 1000;
                unit = "mm";
            } else if (meters > 1000) {
                meters /= 1000;
                unit = "km";
            }
            return sprintf.call("%s %s", meters.toFixed(1), unit);
        }
    }

    function sprintf() {
        var a = this, b;
        for(b in arguments){
            a = a.replace(/%[a-z]/,arguments[b]);
        }
        return a; // Make chainable
    }

    var label = function(options, node) {
        // Initialization
        this.setValues(options);

        // Label specific
        if(!node) {
            node = u.create(HTML.DIV, {className:options.className});
            if(options.style) {
                if(options.style.constructor !== String) {
                    var s = "";
                    for(var x in options.style) {
                        if(options.style.hasOwnProperty(x)) {
                            s += normalizeName(x) + ":" + options.style[x] + ";";
                        }
                    }
                    options.style = s;
                }
                node.setAttribute("style", options.style);
            }
        }
        this.span_ = node;
        var div = this.div_ = u.create(HTML.DIV, {style: "position: absolute; display: none"});
        div.appendChild(node);


        this.onAdd = function() {
             var pane = this.getPanes().overlayLayer;
             pane.appendChild(this.div_);

             // Ensures the label is redrawn if the text or position is changed.
             var me = this;
             this.listeners_ = [
                 google.maps.event.addListener(this, "position_changed", function() { me.draw(); }),
                 google.maps.event.addListener(this, "text_changed", function() { me.draw(); })
             ];
         };
        this.draw = function() {
             var projection = this.getProjection();
             var position = projection.fromLatLngToDivPixel(this.get('position'));

            if(position) {
                 var div = this.div_;
                 div.style.left = position.x + "px";
                 div.style.top = position.y + "px";
                 div.style.display = HTML.BLOCK;

                 this.span_.innerHTML = this.get("text").toString();
             }
         };
         this.onRemove = function() {
             this.div_.parentNode.removeChild(this.div_);

             // Label is removed from the map, stop updating its position/text.
             for (var i = 0, I = this.listeners_.length; i < I; ++i) {
                 google.maps.event.removeListener(this.listeners_[i]);
             }
         };
    };

    function findPoint(points, fraction) {
        var length = 0;
        fraction = fraction || .5;
        for(var i in points) {
            if(i == 0) continue;
            length += google.maps.geometry.spherical.computeDistanceBetween(points[i-1], points[i]);
        }
        length = length * fraction;

        for(var i in points) {
            if(i == 0) continue;
            var current = google.maps.geometry.spherical.computeDistanceBetween(points[i-1], points[i]);
            if(length - current < 0) {
                return google.maps.geometry.spherical.interpolate(points[i-1], points[i], length / current);
            } else {
                length -= current;
            }

        }
        return google.maps.geometry.spherical.interpolate(points[0], points[points.length -1], fraction);
    }

    function reduce(bounds, fraction) {
        var newNortheast = google.maps.geometry.spherical.interpolate(bounds.getNorthEast(), bounds.getSouthWest(), (1+fraction)/2);
        var newSouthwest = google.maps.geometry.spherical.interpolate(bounds.getSouthWest(), bounds.getNorthEast(), (1+fraction)/2);
        bounds = new google.maps.LatLngBounds();
        bounds.extend(newNortheast, newSouthwest);
        return bounds;
    }


    return {
        create: create,
        clear: clear,
        keys: keys,
        createPage: createPage,
        showAlert: showAlert,
        byId: byId,
        getHexColor: getHexColor,
        require:require,
        save:save,
        load:load,
        getUuid:getUuid,
        getEncryptedHash:getEncryptedHash,
        latLng:latLng,
        jsonToLocation:jsonToLocation,
        locationToJson:locationToJson,
        smoothInterpolated:smoothInterpolated,
        dialog:dialog,
        destroy:destroy,
        formatLengthToLocale:formatLengthToLocale,
        sprintf:sprintf,
        label:label,
        findPoint:findPoint,
        reduce:reduce,
    }
}
