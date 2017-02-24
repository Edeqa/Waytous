/**
 * Created 1/20/17.
 */

function Main() {
    var holders = {};
    var users;
    var me;
    var layout;
    var right;
    var loading;
    var origin;
    var main = w = this;

    function start() {
//        firebase.auth().signInWithCustomToken(sign.token)
//        .then(function(e){
//            window.location.href = "/tracking/home";
//            return;

        origin = window.location.href;
        if (window.redirect && window.redirect.url) {
            window.location.href = redirect.url;

            setTimeout(loadBase,500);

            // loadScripts.call(this);
        } else {
            loadBase.call(main)
        }
    }

    function loadBase() {
        if(window.location.href == origin){
            var a = document.createElement("script");
            a.setAttribute("src","/js/Utils.js");
            a.setAttribute("onload","preload()");
            document.head.appendChild(a);
        }
    }

    preload = function(){
        window.u = new Utils();

        window.addEventListener("load", function() { window. scrollTo(0, 0); });

        u.create("link", {rel:"stylesheet", href:"/css/tracking.css"}, document.head);
        u.create("meta", {name: "mobile-web-app-capable"}, document.head).setAttribute("content","yes");

        main.layout = u.create("div", {className:"layout"}, document.body);

        u.create("div", {className:"alpha", innerHTML:"α"}, main.layout);

        loading = u.create("div", {className:"loading"}, main.layout);
        u.create("div", {className:"progress-circle"}, loading);
        u.create("div", {className:"progress-title", innerHTML:"Service loading, please wait..."}, loading);

        main.right = u.create("div", {className:"right changeable"}, main.layout);
        main.fire = fire;
        main.holders = holders;
        main.me = me;

        // addConsoleLayer(main.right);

        u.require("/js/holders/Constants", function(e){
            loadScripts.call(main);
        });
    };

    function addConsoleLayer(parent) {
        var consoleLayer = u.create("div", {className: "console hidden", innerHTML:"Console:\n",
        onclick:function(){
            this.classList.add("hidden");
            console.log("ABHJSHS");
        }}, parent);
//        a.setAttribute("onclick","console.log(this);");
        var systemConsole = window.console.log;
        var errorConsole = window.console.error;
        window.console.log = function(...args) {
            systemConsole(args);
            for(var i in args) {
                consoleLayer.innerHTML += args[i] + " ";
            }
            consoleLayer.innerHTML += "\n";
            consoleLayer.scrollTop = consoleLayer.scrollHeight;
        };
        window.console.error = function(...args) {
            consoleLayer.classList.remove("hidden");
            errorConsole(args);
            for(var i in args) {
                consoleLayer.innerHTML += args[i] + " ";
            }
            consoleLayer.innerHTML += "\n";
            consoleLayer.scrollTop = consoleLayer.scrollHeight;
        };
        window.onerror = function(errorMsg, url, lineNumber){
            window.console.error(url+": "+lineNumber+", "+errorMsg);
        };
        return consoleLayer;
    }

    function loadScripts(){

        var files = [
            "https://www.gstatic.com/firebasejs/3.6.8/firebase-app.js",
            "https://www.gstatic.com/firebasejs/3.6.8/firebase-auth.js",
            "https://www.gstatic.com/firebasejs/3.6.8/firebase-database.js",
            "https://code.jquery.com/jquery-3.1.1.min.js",
            "MyUser",
            "MyUsers",
            "PropertiesHolder",
            "AddressHolder",
            "ButtonHolder",
            "CameraHolder",
            "DrawerHolder",
            "FabHolder",
            "GpsHolder",
            "MapHolder",
            "MarkerHolder",
            "MessagesHolder",
            "PlaceHolder",
            "SensorsHolder",
            "TrackingHolder",
            "SampleHolder",
        ];
        // u.require("https://code.jquery.com/jquery-3.1.1.min.js");
        u.create("link", {rel:"stylesheet", href:"https://fonts.googleapis.com/icon?family=Material+Icons"},document.head);

        var loaded = 0;
        for(var i in files) {
            var file = files[i];
            if(!file.match(/^https?:/i)) file = "/js/holders/"+file;
            u.require(file, function(e) {
                loaded++;
                if(e && e.type) {
                    holders[e.type] = e;
                }
                if(loaded == u.keys(files).length) {
                    console.log("Preload finished: "+loaded+" files done.");
                    loading.classList.add("hidden");

                    var config = {
                        apiKey: "AIzaSyCRH9g5rmQdvShE4mI2czumO17u_hwUF8Q",
                        authDomain: "where-am-i-now-1373.firebaseapp.com",
                        databaseURL: "https://where-am-i-now-1373.firebaseio.com",
                        storageBucket: "where-am-i-now-1373.appspot.com",
                        messagingSenderId: "365115596478"
                    };
                    firebase.initializeApp(config);
                    database = firebase.database();

                    resume.call(main);
                }
            }, main);
        }

        // main.right.webkitRequestFullScreen();


        /*window.addEventListener("load",function() { setTimeout(function(){ // This hides the address bar:
            window.scrollTo(0, 1); }, 0);
        });*/
        window.scrollTo(0, 1);

    }

    function resume() {

        if(me == null){
            main.me = me = new MyUser(main);
            me.user = true;
            me.color = "#0000FF";

            if(u.load("name")){
                me.name = u.load("name");
            }
            me.createViews();

            me.fire(EVENTS.SELECT_USER, 0);
            me.fire(EVENTS.CHANGE_COLOR, "#0000FF");
        }
        me.fire(EVENTS.MAKE_ACTIVE);

        main.users = users = new MyUsers(main);
        users.setMe();

        for(var x in holders){
            if(holders[x] && holders[x].start) holders[x].start();
        }
    }

    function fire(EVENT,object) {
        // console.log("EVENT",EVENT,object);
        for(var i in holders) {
            if(holders[i] && holders[i].dependsOnEvent && holders[i].onEvent) {
                if(!holders[i].onEvent(EVENT, object)) break;
            }
        }
    }

    return {
        start: start,
        main:main,
        // layout:layout,
        // right:right,
        fire:fire,
        // holders: holders,
        // me:me,
        // users:users,
    }
}
//document.addEventListener("DOMContentLoaded", (window.WAIN = new Main()).start);
document.addEventListener("readystatechange", function(){
    if(document.readyState == "complete"){(
        window.WAIN = new Main()
    ).start()}
});
