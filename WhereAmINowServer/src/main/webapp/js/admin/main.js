/**
 * Created 1/20/17.
 */

function Main() {
    var u = new Utils();

    var holders = {};
    var holderFiles = [
        "Home.js",
        "Create.js",
        "Summary.js",
        "Settings.js",
        "Help.js",
        "User.js"
    ]

    var start = function() {
        if(window.name == "content") {
            window.location.href = "/admin/home/set";
            return;
        }

        var loaded = 0;
        for(var i in holderFiles) {
            u.create("script", {src: "/js/admin/"+holderFiles[i], dataStart: holderFiles[i],  onload: function(e) {
                loaded++;
                var holder = this.dataset.start.split(".")[0];
                holder = new window[holder]();
                holders[holder.page] = holder;
                if(loaded == u.keys(holderFiles).length) {
                    resume();
                }
            }}, document.body);
        }
    }

    var resume = function() {
        var out = u.create("table", {className:"layout"}, document.body);
        var outtr = u.create("tr", {}, out);
        var outmenu = u.create("td", {className:"menu"}, outtr);
        var outcontent = u.create("td", {}, outtr);

        u.create("div", { className: "logo"}, u.create("a", { href: "/" }, outmenu));

        var table = u.create("table", {className:"menu"}, outmenu);

        for(var x in holders) {
            if(holders[x].menu) {
                var th = u.create("th", null, u.create("tr", null, table));
                u.create("i", { className:"material-icons md-14", innerHTML: holders[x].icon }, th);
                u.create("a", { href: "/admin/"+x+"/set", innerHTML: holders[x].title, target:"content"}, th);
            }
        }

        th = u.create("th", null, u.create("tr", null, table));
        u.create("i", { className:"material-icons md-14", innerHTML:"exit_to_app" }, th);
        u.create("a", { href: "#", onclick: logout, innerHTML: "Log out" }, th);

        u.create("iframe", { name: "content", src: "/admin/"+data.page+"/set", frameBorder: 0 }, outcontent);


    }

    var logout = function() {
        var to_url;
        var out = window.location.href.replace(/:\/\//, '://log:out@');

        if(!to_url){
            to_url = window.location.protocol + "//" + window.location.host + "/";// + window.location.pathname;
            console.log("TO:",to_url);
        }

        jQuery.get(out).then(function() {
            window.location = to_url;
        }).catch(function() {
            window.location = to_url;
        });
    }

    return {
        start: start,
    }
}
document.addEventListener("DOMContentLoaded", (new Main()).start);