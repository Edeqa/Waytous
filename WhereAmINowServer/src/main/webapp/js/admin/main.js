/**
 * Created 1/20/17.
 */

function Main() {
    var u = new Utils();
    var menu;

    var holders = {};
    var holderFiles = [
        "Home",
        "Create",
        "Summary",
        "Settings",
        "Help",
        "User"
    ]

    var start = function() {
        if(!data.page) {
            window.location.href = "/admin/home";
            return;
        }

        var loaded = 0;
        for(var i in holderFiles) {
            u.create("script", {src: "/js/admin/"+holderFiles[i]+".js", dataStart: holderFiles[i],  onload: function(e) {
                loaded++;
                var holder = this.dataset.start;
                holder = new window[holder]();
                holders[holder.page] = holder;
                if(loaded == u.keys(holderFiles).length) {
                    resume();
                }
            }}, document.head);
        }
    }

    var resume = function() {

        window.addEventListener("load",function() { setTimeout(function(){ // This hides the address bar:
            window.scrollTo(0, 1); }, 0);
        });

        var out = u.create("div", {className:"layout"}, document.body);
        menu = u.create("div", {className:"menu", tabindex: 1, onblur: function(){
            menu.classList.remove("menu-open");
            document.getElementsByClassName("menu-button")[0].classList.remove("menu-button-open");
            return true;
        }}, out);
        var right = u.create("div", {className:"right"}, out);

        u.create("a", { href: "/", className:"logo" }, menu);

        for(var i in holderFiles) {
            var x = holderFiles[i].toLowerCase();
            if(holders[x].menu) {
                var th = u.create("div", {className:"menu-item"}, menu);
                u.create("i", { className:"material-icons md-14", innerHTML: holders[x].icon }, th);
                u.create("div", { dataStart: x, onclick: function(){
                    console.log("AAA",this.dataset.start);
                    menu.blur();
                    holders[this.dataset.start].start();
                    return false;
                }, innerHTML: holders[x].menu}, th);
            }
        }

        th = u.create("div", {className:"menu-item"}, menu);
        u.create("i", { className:"material-icons md-14", innerHTML:"exit_to_app" }, th);
        u.create("div", { onclick: logout, innerHTML: "Log out" }, th);

        th = u.create("div", { className:"menu-bottom"}, menu);
        u.create("div", "On The Right Way", th);
        u.create("div", "&copy; 2017, someeee", th);
        u.create("div", "Build " + data.version, th);

        holders[data.page].start();
//        u.create("iframe", { name: "set", src: "/admin/"+data.page, frameBorder: 0 }, content);


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