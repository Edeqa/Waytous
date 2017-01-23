/**
 * Created 1/20/17.
 */

function Admin() {
    var u = new Utils();

    var holders = {
        home: "home.js",
        create: "create.js",
        summary: "summary.js",
        settings: "settings.js",
        help: "help.js",
    }

    var start = function() {

        var loaded = 0;
        for(var x in holders) {
            var name = x;
            var value = holders[x];
            u.create("script", {src: "/js/admin/"+value, dataStart: name,  onload: function(e) {
                loaded++;
                var name = this.dataset.start;
                var holder = this.dataset.start.substr(0,1).toUpperCase() + this.dataset.start.substr(1);
                holders[name] = new window[holder]();
                if(loaded == u.keys(holders).length) {
                    resume();
                }
            }}, document.body);
        }
    }

    var resume = function() {
        var out = u.create("table", {className:"layout"}, document.body);
        var outtr = u.create("tr", {}, out);
        var outmenu = u.create("td", { style: { width: "150px"}}, outtr);
        var outcontent = u.create("td", { style: { width: "auto"}}, outtr);

        u.create("div", { innerHTML: "WAIN", style: {height: "100px"}}, outmenu);

        var table = u.create("table", {className:"menu", style: {width: "100%" }}, outmenu);

        for(var x in holders) {

            var th = u.create("th", null, u.create("tr", null, table));
            u.create("i", { className:"material-icons md-14", innerHTML: holders[x].icon }, th);
            u.create("a", { href: "/admin/"+x+"/set", innerHTML: holders[x].title, target:"content"}, th);

        }

        th = u.create("th", null, u.create("tr", null, table));
        u.create("i", { className:"material-icons md-14", innerHTML:"exit_to_app" }, th);
        u.create("a", { href: "#", onclick: logout, innerHTML: "Log out" }, th);

        u.create("iframe", { name: "content", src: "/admin/"+data.page+"/set", style: {width: "100%", height: "100%"}, frameBorder: 0 }, outcontent);


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
document.addEventListener("DOMContentLoaded", (new Admin()).start);