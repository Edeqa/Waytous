/**
 * Created 1/19/17.
 */
window.HTML = {
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
    INPUT:"input",
    CHECKBOX:"checkbox",
    TEXT:"text",
    TEXTAREA:"textarea",
    HIDDEN:"hidden",
    SELECT:"select",
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

function Utils(main) {

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

    HTMLDivElement.prototype.show = function() {
        this.classList.remove("hidden");
        return this;
    }
    HTMLDivElement.prototype.hide = function() {
        this.classList.add("hidden");
        return this;
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
                        if(properties[x]) {
                            if(properties[x] instanceof HTMLElement) {
                                el.appendChild(properties[x]);
                            } else {
                                el[x] = properties[x];
                            }
                        }
                    } else if(x == HTML.CONTENT && properties[x].constructor !== String) {
                        el.appendChild(properties[x]);
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

    function getHexColor(color){
        color >>>= 0;
        var b = color & 0xFF,
            g = (color & 0xFF00) >>> 8,
            r = (color & 0xFF0000) >>> 16;

        r = r.toString(16);if(r.length == 1) r = "0"+r;
        g = g.toString(16);if(g.length == 1) g = "0"+g;
        b = b.toString(16);if(b.length == 1) b = "0"+b;

        return "#"+r+g+b;
    }

    function getRGBAColor(color, alpha) {
            if(!color) return;
            if(color.constructor === String) {
                if(color.match(/^#/)) {
                    color = color.replace("#","").split("");
                    var r = parseInt(color[0]+color[1],16);
                    var g = parseInt(color[2]+color[3],16);
                    var b = parseInt(color[4]+color[5],16);
                    color = (r*256 + g)*256 + b;
                    if(alpha) {
                        color = "rgba("+r+", "+g+", "+b+", "+alpha+")";
                    } else {
                        color = "rgb("+r+", "+g+", "+b+")";
                    }
                }
            } else if (color.constructor === Number) {
                color >>>= 0;
                var b = color & 0xFF,
                    g = (color & 0xFF00) >>> 8,
                    r = (color & 0xFF0000) >>> 16,
                    a = (( (color & 0xFF000000) >>> 24 ) / 255) || 1;
                    if(alpha) a = alpha;
                color = "rgba(" + [r, g, b, a].join(",") + ")";
            }
            return color;
        }

    function getDecimalColor(color, alpha) {
        if(!color) return;
        if(color.constructor === String) {
            if(color.match(/^#/)) {
                color = color.replace("#","");
                color = parseInt(color,16);
            }
        }
        return color;
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
        if(!filename.match(/\.js$/) && parts[1] == "js") {
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
                        lang.overrideResources(a);
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

    function saveWith(token, name, value) {
        if(!token) {
            save(name, value);
            return;
        }
        if(value) {
            localStorage["WAIN$" + token +":" + name] = JSON.stringify(value);
        } else {
            delete localStorage["WAIN$" + token +":" + name];
        }
    }

    function loadWith(token, name) {
        if(!token) {
            return load(name);
        }
        var value = localStorage["WAIN$" + token +":"+name];
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
        var dialog = create(HTML.DIV, {
            className:"modal shadow hidden"+(options.className ? " "+options.className : ""),
            tabindex:-1,
            onblur: options.onblur,
            onfocus: options.onfocus
        }, main.right);

        dialog.opened = false;

        dialog.clearItems = function() {
            clear(dialog.itemsLayout);
            dialog.items = [];
        };

        dialog.addItem = function(item, appendTo) {
            item = item || {};
            appendTo = appendTo || dialog.itemsLayout;

            var x;
            if(item.type == HTML.DIV || item.type == HTML.A) {
                if(item.enclosed) {
                    x = create(item.type, {
                        className: "dialog-item-enclosed" + (item.className ? " " + item.className : "")
                    }, appendTo);
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
                    enclosedIcon = create(HTML.DIV, {className:"dialog-item-enclosed-icon", innerHTML:"expand_more"}, enclosedButton);
                    create(HTML.DIV, {className:"dialog-item-enclosed-label", innerHTML: item.label || "Show more information"}, enclosedButton);
                    x.body = create(HTML.DIV, {className:"dialog-item-enclosed-body hidden", innerHTML:item.body || ""}, x);
                } else {
                    item.className = "dialog-item" + (item.className ? " " + item.className : "");
                    item.innerHTML = item.label || item.title || item.innerHTML || "";
                    delete item.label;
                    delete item.title;
                    var type = item.type;
                    delete item.type;
                    x = create(type, item, appendTo);
                }
            } else if(item.type == HTML.HIDDEN) {
                x = create(HTML.INPUT, {type:HTML.HIDDEN, value:item.value || ""}, appendTo);
            } else if(item.type == HTML.SELECT) {
                var div = create(HTML.DIV, {className:"dialog-item dialog-item-input", onclick: function(){this.firstChild.nextSibling.click();}}, appendTo);

                if(item.label) {
                    create(HTML.DIV, {
                        className:"dialog-item-label" + (item.labelClassName ? " " + item.labelClassName : ""),
                        innerHTML:item.label
                    }, div);
                }

                x = create(HTML.SELECT, {
                    type:item.type,
                    className:"dialog-item-input-select" + (item.className ? " "+item.className : ""),
                    tabindex: i,
                    value:item.value || "",
//                    onclick: function(e) { this.focus(); e.stopPropagation(); },
//                    onkeyup:function(e){
//                        if(e.keyCode == 13 && this.type != HTML.TEXTAREA) {
//                            dialog.close();
//                            if(options.positive && options.positive.onclick) options.positive.onclick.call(dialog,items);
//                        } else if(e.keyCode == 27) {
//                            dialog.close();
//                            if(options.negative && options.negative.onclick) options.negative.onclick.call(dialog,items);
//                        }
//                    },

                }, div);
                for(var y in item.values) {
                    u.create("option", {value:y, innerHTML:item.values[y], selected: item.default == y}, x);
                }

            } else {
                var div = create(HTML.DIV, {className:"dialog-item dialog-item-input", onclick: function(){this.firstChild.nextSibling.click();}}, appendTo);

                if(item.label) {
                    create(HTML.DIV, {
                        className:"dialog-item-label" + (item.labelClassName ? " " + item.labelClassName : ""),
                        innerHTML:item.label
                    }, div);
                }
                var type = HTML.INPUT;
                if(item.type.toLowerCase() == HTML.TEXTAREA) type = HTML.TEXTAREA;

                x = create(type, {
                    type:item.type,
                    className:"dialog-item-input-"+item.type + (item.className ? " "+item.className : ""),
                    tabindex: i,
                    value:item.value || "",
                    onclick: function(e) { this.focus(); e.stopPropagation(); },
                    onkeyup:function(e){
                        if(e.keyCode == 13 && this.type != HTML.TEXTAREA) {
                            dialog.close();
                            if(options.positive && options.positive.onclick) options.positive.onclick.call(dialog,items);
                        } else if(e.keyCode == 27) {
                            dialog.close();
                            if(options.negative && options.negative.onclick) options.negative.onclick.call(dialog,items);
                        }
                    },

                }, div);
            }
            items.push(x);
            return x;
        }

        dialog.adjustPosition = function() {
            var left = load("dialog:left:"+(options.id || (options.title && options.title.label)));
            var top = load("dialog:top:"+(options.id || (options.title && options.title.label)));
            var width = load("dialog:width:"+(options.id || (options.title && options.title.label)));
            var height = load("dialog:height:"+(options.id || (options.title && options.title.label)));
            if(left || top || width || height) {
                if(left) dialog.style.left = left;
                if(top) dialog.style.top = top;
                if(width) dialog.style.width = width;
                if(height) dialog.style.height = height;
                dialog.style.right = HTML.AUTO;
                dialog.style.bottom = HTML.AUTO;
            } else {
                left = dialog.offsetLeft;
                var leftMain = main.right.offsetWidth;

                if(left >= leftMain || left == 0) {
                    dialog.style.left = ((main.right.offsetWidth - dialog.offsetWidth) /2)+"px";
                    dialog.style.top = ((main.right.offsetWidth - dialog.offsetHeight) /2)+"px";
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

        dialog.open = function(event){
            clearInterval(dialog.intervalTask);
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
            return dialog;
        };

        dialog.close = function (event){
            dialog.hide();
            dialog.opened = false;

            clearInterval(dialog.intervalTask);

            if(options.onclose) options.onclose.call(dialog,items,event);
        };
        dialog.addEventListener("keyup", function(e) {
            if(e.keyCode == 27) {
                dialog.close();
                if(options.negative && options.negative.onclick) options.negative.onclick.call(dialog,items);
            }
        });

        options = options || {};
        var items = [];

        var defaultCloseButton = {
             icon: "clear",
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
//                    var position = dialog.getBoundingClientRect();
                    var position = { left: dialog.offsetLeft, top: dialog.offsetTop, width: dialog.offsetWidth, height: dialog.offsetHeight };
                    var offset = [ e.clientX, e.clientY ];
                    var moved = false;
                    function mouseup(e){
                        window.removeEventListener(HTML.MOUSEUP, mouseup, false);
                        window.removeEventListener(HTML.MOUSEMOVE, mousemove, false);
                        if((options.id || options.title.label) && moved) {
                            if(dialog.style.left) save("dialog:left:"+(options.id || options.title.label), dialog.style.left);
                            if(dialog.style.top) save("dialog:top:"+(options.id || options.title.label), dialog.style.top);
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
                    save("dialog:left:"+(options.id || options.title.label));
                    save("dialog:top:"+(options.id || options.title.label));
                    save("dialog:width:"+(options.id || options.title.label));
                    save("dialog:height:"+(options.id || options.title.label));
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
            dialog.positive = create(HTML.BUTTON, {className:"dialog-button-positive", tabindex:98, onclick:function(event){
                dialog.close();
                if(options.positive.onclick) options.positive.onclick.call(dialog,items,event);
            }, innerHTML: options.positive.label}, buttons);
            buttons.show();
        }
        if(options.neutral && options.neutral.label) {
            dialog.neutral = create("button", {className:"dialog-button-neutral", tabindex:100, onclick:function(event){
                dialog.close();
                if(options.neutral.onclick) options.neutral.onclick.call(dialog,items,event);
            }, innerHTML: options.neutral.label}, buttons);
            buttons.show();
        }
        if(options.negative && options.negative.label) {
            dialog.negative = create(HTML.BUTTON, {className:"dialog-button-negative", tabindex:99, onclick:function(event){
                dialog.close();
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
                            if(dialog.style.width) save("dialog:width:"+(options.id || options.title.label), dialog.style.width);
                            if(dialog.style.height) save("dialog:height:"+(options.id || options.title.label), dialog.style.height);
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
            a = a.replace(/%[a-z]/,arguments[b][0].constructor === HTMLSpanElement ?
                (arguments[b][0].dataset && arguments[b][0].dataset.lang
                    && lang.$origin[arguments[b][0].dataset.lang] ? lang[arguments[b][0].dataset.lang].outerText : arguments[b][0].outerText)
                : arguments[b][0]);
        }
        return a; // Make chainable
    }

    var label = function(options, node) {
        // Initialization
        this.setValues(options);

        // Label specific
        if(!node) {
            node = create(HTML.DIV, {className:options.className});
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
        var div = this.div_ = create(HTML.DIV, {style: "position: absolute; display: none"});
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

    var popupBlockerChecker = {
        check: function(popup_window, onblocked){
            var _scope = this;
            if(onblocked) this._displayError = onblocked;
            if (popup_window) {
                if(/chrome/.test(navigator.userAgent.toLowerCase())){
                    setTimeout(function () {
                        _scope._is_popup_blocked(_scope, popup_window);
                     },200);
                }else{
                    popup_window.onload = function () {
                        _scope._is_popup_blocked(_scope, popup_window);
                    };
                }
            }else{
                _scope._displayError();
            }
        },
        _is_popup_blocked: function(scope, popup_window){
            if ((popup_window.innerHeight > 0)==false){ scope._displayError(); }
        },
        _displayError: function(){
            console.log("Popup Blocker is enabled! Please add this site to your exception list.");
        }
    };

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
                            dataLang:string,
                        });
                        var a = lang.$nodes[string].cloneNode();
                        a.format = function() {
                              lang.$arguments[this.dataset.lang] = arguments;
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

            var xhr = new XMLHttpRequest();
            xhr.open("GET", resourcesFile, true);
            xhr.onreadystatechange = function() { // (3)
                if (xhr.readyState != 4) return;
                if (xhr.status != 200) {
                    console.warn("Error fetching resources for \""+holder+"\":",xhr.status + ': ' + xhr.statusText);
                    if(holder != "en-us"){
                        console.warn("Switching to default resources \"en-us\".")
                        lang.overrideResources("en-us");
                    }
                } else {
                    try {
                        var json = JSON.parse(xhr.responseText);
                        var nodes = document.getElementsByTagName(HTML.SPAN);
                        console.warn("Switching to resources \""+holder+"\".");
                        for(var x in json) {
//                            if(lang.$origin[x]) {
//                                console.warn("Overrided resource: " + x + ":", json[x] ? (json[x].length > 30 ? json[x].substr(0,30)+"..." : json[x]) : "" );
//                            }
                            lang(x, json[x]);
                        }
                        for(var i = 0; i < nodes.length; i++) {
                            if(nodes[i].dataset && nodes[i].dataset.lang) {
                                nodes[i].parentNode.replaceChild(lang[nodes[i].dataset.lang],nodes[i]);
                            }
                        }
                    } catch(e) {
                        console.warn("Incorrect, empty or damaged resource file for \""+holder+"\":",e);
                        if(holder != "en-us"){
                            console.warn("Switching to default resources \"en-us\".")
                            lang.overrideResources("en-us");
                        }
                    }
                }
            }
            xhr.send();
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
        if(node && lang && lang.dataset && lang.dataset.lang) {
            node.innerHTML = lang.innerHTML;
            node.dataset.lang = lang.dataset.lang;
        }
    }

    return {
        create: create,
        clear: clear,
        keys: keys,
        createPage: createPage,
        showAlert: showAlert,
        byId: byId,
        getHexColor: getHexColor,
        getRGBAColor:getRGBAColor,
        getDecimalColor:getDecimalColor,
        require:require,
        save:save,
        load:load,
        saveWith:saveWith,
        loadWith:loadWith,
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
        popupBlockerChecker:popupBlockerChecker,
        cloneAsObject:cloneAsObject,
        lang:lang,
    }
}
