/**
 * Created 1/20/17.
 */

function Main() {
    var menu;

    var holders = {};
    var holderFiles = [
        "https://code.jquery.com/jquery-3.1.1.min.js",
        "https://www.gstatic.com/firebasejs/3.6.8/firebase-app.js",
        "https://www.gstatic.com/firebasejs/3.6.8/firebase-auth.js",
        "https://www.gstatic.com/firebasejs/3.6.8/firebase-database.js",
        "Home",
        "/js/helpers/Constants",
        "Create",
        "Group",
        "Groups",
        "Settings",
        "Help",
        "User"
    ];


    function start() {
    //        if(window.location.href == origin){
        var a = document.createElement("script");
        a.setAttribute("src","/js/helpers/Utils.js");
        a.setAttribute("onload","preloaded()");
        document.head.appendChild(a);
    //        }
    }


    preloaded = function() {
        if(!data.page) {
            window.location.href = "/admin/home";
            return;
        }

        window.u = new Utils();

        var loaded = 0;
        for(var i in holderFiles) {
            var file = holderFiles[i];
            if(!file.match(/^(https?:)|\//i)) file = "/js/admin/"+file;
            u.require(file, function(e) {
                loaded++;
                if(e && e.moduleName) {
                    holders[e.moduleName.toLowerCase()] = e;
                }
                if(loaded == u.keys(holderFiles).length) {
                    console.log("Preload finished: "+loaded+" files done.");
                    initializeFirebase();
                }
            });
        }

    }

    function initializeFirebase() {

        u.create(HTML.META, {name:"viewport", content:"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"}, document.head);
        u.create(HTML.LINK, {rel:"stylesheet", href:"https://fonts.googleapis.com/icon?family=Material+Icons"}, document.head);
        u.create(HTML.LINK, {rel:"stylesheet", href:"/css/admin.css"}, document.head);

        var config = {
            apiKey: "AIzaSyCRH9g5rmQdvShE4mI2czumO17u_hwUF8Q",
            authDomain: "where-am-i-now-1373.firebaseapp.com",
            databaseURL: "https://where-am-i-now-1373.firebaseio.com",
            storageBucket: "where-am-i-now-1373.appspot.com",
            messagingSenderId: "365115596478"
        };
        firebase.initializeApp(config);
        database = firebase.database();

        firebase.auth().signInWithCustomToken(sign.token)
        .then(function(e){
            resume();
//            console.log("AAA",e)
        }).catch(function(error) {
                          // Handle Errors here.
          var errorCode = error.code;
          var errorMessage = error.message;
          // ...
        });

        var a = u.byId("sign");
        a.parentNode.removeChild(a);

    }

    function resume() {
        try {
        window.addEventListener("load",function() { setTimeout(function(){ // This hides the address bar:
            window.scrollTo(0, 1); }, 0);
        });

        var out = u.create("div", {className:"layout"}, document.body);
        menu = u.create("div", {className:"menu", tabindex: 1, onblur: function(){
            menu.classList.remove("menu-open");
            return true;
        }}, out);
        var right = u.create("div", {className:"right"}, out);

        u.create("a", { href: "/", className:"logo" }, menu);

        for(var i in holderFiles) {
            var x = holderFiles[i].toLowerCase();
            if(holders[x] && holders[x].menu) {
                var th = u.create("div", {className:"menu-item"}, menu);
                u.create("i", { className:"material-icons md-14", innerHTML: holders[x].icon }, th);
                u.create("div", { dataStart: x, onclick: function(){
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
        u.create("div", "Waytogo &copy;2017 WTG", th);
        u.create("div", "Build " + data.version, th);

        holders[data.page].start();
//        u.create("iframe", { name: "set", src: "/admin/"+data.page, frameBorder: 0 }, content);

        } catch(e) {
            console.error(e);
        }
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

    var switchTo = function(to) {
        var parts = to.split("/");
        if(parts[1] == "admin") {
            holders[parts[2]].start(parts);
        }
    }

    return {
        start: start,
        switchTo: switchTo,
    }
}
//document.addEventListener("DOMContentLoaded", (window.WAIN = new Main()).start);
document.addEventListener("readystatechange", function(){if(document.readyState == "complete"){(window.WAIN = new Main()).start()}});
