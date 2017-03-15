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
    var progress;
    var alert;

    function start() {
//        firebase.auth().signInWithCustomToken(sign.token)
//        .then(function(e){
//            window.location.href = "/tracking/home";
//            return;

//        origin = window.location.href;
//        if (window.redirect && window.redirect.url) {
//            window.location.href = redirect.url;
//
//            setTimeout(loadBase,500);
//
//            // loadScripts.call(this);
//        } else {

//        if ('serviceWorker' in navigator) {
//            navigator.serviceWorker.register('/js/tracking/ServiceWorker.js').then(function(reg) {
//            // registered
//if(reg.installing) {
//      console.log('Service worker installing');
//    } else if(reg.waiting) {
//      console.log('Service worker installed');
//    } else if(reg.active) {
//      console.log('Service worker active');
//    }
//
//            }).catch(function(error) {
//            // not registered
//                console.log("ERROR SERVICE WORKER< REGULAR LOADING",error);
//                loadBase.call(main);
//            });
//        } else {
//            loadBase.call(main);
//        }
//        }
//        if(window.location.href == origin){
            var a = document.createElement("script");
            a.setAttribute("src","/js/helpers/Utils.js");
            a.setAttribute("onload","preloaded()");
            document.head.appendChild(a);
//        }
    }

    preloaded = function(){
        window.u = new Utils();

        main.layout = u.create("div", {className:"layout"}, document.body);

        loading = u.create("div", {style:{
            position: "fixed", top: 0, bottom: 0, left: 0, right: 0,
            zIndex: 10000, backgroundColor: "white", display: "flex", flexDirection: "column",
            justifyContent: "center", alignItems: "center", fontFamily: "sans-serif"
        }}, main.layout);
        u.create("div", {className:"progress-circle"}, loading);
        u.create("div", {className:"progress-title", innerHTML:"Service loading, please wait... "}, loading);
        progress = u.create("div", {className:"progress-title", innerHTML:"0%"}, loading);
//        window.onload = function() {
//            window.scrollTo(0, 1);
//            document.addEventListener("touchmove", function(e) { e.preventDefault() });
//        };

        //addConsoleLayer(main.right);

        u.require("/js/helpers/Constants", function(e){
            initializeHeader.call(main);
        });
    };

    function addConsoleLayer(parent) {
        var consoleLayer = u.create("div", {className: "console hidden", innerHTML:"Console:\n",
        onclick:function(){
            this.classList.add("hidden");
        }}, parent);
//        a.setAttribute("onclick","console.log(this);");
        var systemConsole = window.console.log;
        var errorConsole = window.console.error;
        window.console.log = function() {
            systemConsole(arguments);
            for(var i in arguments) {
                consoleLayer.innerHTML += arguments[i] + " ";
            }
            consoleLayer.innerHTML += "\n";
            consoleLayer.scrollTop = consoleLayer.scrollHeight;
        };
        window.console.error = function() {
            consoleLayer.classList.remove("hidden");
            errorConsole(arguments);
            for(var i in args) {
                consoleLayer.innerHTML += arguments[i] + " ";
            }
            consoleLayer.innerHTML += "\n";
            consoleLayer.scrollTop = consoleLayer.scrollHeight;
        };
        window.onerror = function(errorMsg, url, lineNumber){
            window.console.error(url+": "+lineNumber+", "+errorMsg);
        };
        return consoleLayer;
    }

    function initializeHeader() {

        u.create(HTML.META, {name:"viewport", content:"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"}, document.head);
        u.create(HTML.LINK, {rel:HTML.STYLESHEET, href:"/css/tracking.css"}, document.head);
        u.create(HTML.LINK, {rel:HTML.STYLESHEET, href:"https://fonts.googleapis.com/icon?family=Material+Icons"},document.head);

        u.create(HTML.LINK, {rel:"apple-touch-icon", href:"/icons/apple-touch-icon.png"},document.head);
        u.create(HTML.LINK, {rel:"apple-touch-icon", sizes:"60x60", href:"/icons/apple-touch-icon-60x60.png"},document.head);
        u.create(HTML.LINK, {rel:"apple-touch-icon", sizes:"76x76", href:"/icons/apple-touch-icon-76x76.png"},document.head);
        u.create(HTML.LINK, {rel:"apple-touch-icon", sizes:"120x120", href:"/icons/apple-touch-icon-120x120.png"},document.head);
        u.create(HTML.LINK, {rel:"apple-touch-icon", sizes:"152x152", href:"/icons/apple-touch-icon-152x152.png"},document.head);
        u.create(HTML.LINK, {rel:"apple-touch-icon", sizes:"180x180", href:"/icons/apple-touch-icon.png"},document.head);
        u.create(HTML.LINK, {rel:"icon", type:"image/png", sizes:"32x32", href:"/icons/favicon-32x32.png"},document.head);
        u.create(HTML.LINK, {rel:"icon", type:"image/png", sizes:"16x16", href:"/icons/favicon-16x16.png"},document.head);
        u.create(HTML.LINK, {rel:"manifest", href:"/icons/manifest.json"},document.head);
        u.create(HTML.LINK, {rel:"mask-icon", href:"/icons/safari-pinned-tab.svg", color:"#007574"},document.head);
        u.create(HTML.LINK, {rel:"shortcut icon", href:"/icons/favicon.ico"},document.head);
        u.create(HTML.LINK, {rel:"apple-touch-startup-image", href: "/icons/apple-touch-icon.png"},document.head);
        u.create(HTML.META, {name:"apple-mobile-web-app-capable", content:"yes"},document.head);
        u.create(HTML.META, {name:"mobile-web-app-capable", content:"yes"},document.head);
        u.create(HTML.META, {name:"application-name", content:"Waytogo"},document.head);
        u.create(HTML.META, {name:"msapplication-TileColor", content:"#059c9a"},document.head);
        u.create(HTML.META, {name:"msapplication-TileImage", content:"/icons/mstile-144x144.png"},document.head);
        u.create(HTML.META, {name:"msapplication-config", content:"/icons/browserconfig.xml"},document.head);
        u.create(HTML.META, {name:"theme-color", content:"#94e6e5"},document.head);

        loadScripts.call(main);
    }

    function loadScripts(){

        main.right = u.create("div", {className:"right changeable"}, main.layout);
        main.fire = fire;
        main.help = help;
        main.holders = holders;
        main.me = me;
        main.initialize = initialize;

        main.alert = main.alert || u.dialog({
             className: "alert-dialog",
             items: [
                 { type: HTML.DIV, label: "We are sorry but some error occurred while loading the service. Please wait a little time and try again." },
                 { type: HTML.DIV, enclosed: true, body: "" },
             ],
             positive: {
                 label: "Reload",
                 onclick: function(){
                     window.location.reload();
                 }
             },
             help: function() {
                main.fire(EVENTS.SHOW_HELP, {module: main, article: 1});
             }
         });

        var files = [
            "https://www.gstatic.com/firebasejs/3.6.8/firebase-app.js",
            "https://www.gstatic.com/firebasejs/3.6.8/firebase-auth.js",
            "https://www.gstatic.com/firebasejs/3.6.8/firebase-database.js",
            // "https://code.jquery.com/jquery-3.1.1.min.js",
            "https://cdnjs.cloudflare.com/ajax/libs/fingerprintjs2/1.5.0/fingerprint2.min.js",
            "/js/helpers/MyUser",
            "/js/helpers/MyUsers",
            "/js/helpers/NoSleep.min.js",
            "PropertiesHolder",
            "AddressHolder",
            "ButtonHolder",
            "CameraHolder",
            "DistanceHolder",
            "DrawerHolder",
            // "FabHolder",
            "GpsHolder",
            "HelpHolder",
            "MapHolder",
            "MarkerHolder",
            "MessagesHolder",
            "NavigationHolder",
            "PlaceHolder",
            "SocialHolder",
            "StreetViewHolder",
            "TrackingHolder",
            "TrackHolder",
//            "SampleHolder",
        ];
        // u.require("https://code.jquery.com/jquery-3.1.1.min.js");

try{
        var loaded = 0;
        var failed = false;
        for(var i in files) {
            var file = files[i];
            if(!file.match(/^(https?:)|\//i)) file = "/js/holders/"+file;
            u.require(file, function(e) {
                if(failed) return;
                loaded++;
                if(e && e.type) {
                    holders[e.type] = e;
                }
                progress.innerHTML = Math.ceil(loaded / files.length * 100) + "%";
                if(loaded == u.keys(files).length) {
                    console.log("Preload finished: "+loaded+" files done.");

                    u.getUuid(initialize.bind(main));
                }
            },function(code, moduleName, event) {
                console.log(code, moduleName, event.srcElement.src);
                if(failed) return;
                failed = true;
                loading.classList.add("hidden");

                main.alert.items[1].body.innerHTML = "Error with loading "+moduleName+": code " + code;
                main.alert.onopen();

            }, main);
        }
} catch(e) {
    main.alert.items[1].body.innerHTML = "Error with initializing "+e.message;
    main.alert.onopen();
}
        // main.right.webkitRequestFullScreen();


        /*window.addEventListener("load",function() { setTimeout(function(){ // This hides the address bar:
            window.scrollTo(0, 1); }, 0);
        });*/

    }

    function initialize() {

        loading.classList.add("hidden");
        u.create("div", {className:"alpha", innerHTML:"α"}, main.layout);

        var config = {
            apiKey: "AIzaSyCRH9g5rmQdvShE4mI2czumO17u_hwUF8Q",
            authDomain: "where-am-i-now-1373.firebaseapp.com",
            databaseURL: "https://where-am-i-now-1373.firebaseio.com",
            storageBucket: "where-am-i-now-1373.appspot.com",
            messagingSenderId: "365115596478"
        };
        firebase.initializeApp(config);
        if(!firebase || !firebase.database || !firebase.auth) {
            console.error("Failed firebase loading, trying again...");
debugger;
            var files = [];
            if(!firebase) files.push("https://www.gstatic.com/firebasejs/3.6.8/firebase-app.js");
            if(!firebase.database) files.push("https://www.gstatic.com/firebasejs/3.6.8/firebase-auth.js");
            if(!firebase.auth) files.push("https://www.gstatic.com/firebasejs/3.6.8/firebase-database.js");

            var loaded = 0;
            var failed = false;
            for(var i in files) {
                var file = files[i];
                u.require(file, function(e) {
                    if(failed) return;
                    loaded++;
                    progress.innerHTML = Math.ceil(loaded / files.length * 100) + "%";
                    if(loaded == u.keys(files).length) {
                        initialize.bind(main);
                    }
                },function(code, moduleName, event) {
                    console.log(code, moduleName, event.srcElement.src);
                    if(failed) return;
                    failed = true;

                    main.alert.items[1].body.innerHTML = "Error with loading "+moduleName+": code " + code;
                    main.alert.onopen();
                }, main);
            }

            return;
        }
        database = firebase.database();
//throw new Error("A");

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

//        window.addEventListener("load", hideAddressBar );
//        window.addEventListener("orientationchange", hideAddressBar );

    }

    function hideAddressBar() {
        if(!window.location.hash) {
            if(document.height < window.outerHeight) {
                document.body.style.height = (window.outerHeight + 50) + 'px';
            }
            setTimeout( function(){ window.scrollTo(0, 1); }, 50 );
        }
    }

    function fire(EVENT,object) {
        for(var i in holders) {
            if(holders[i] && holders[i].dependsOnEvent && holders[i].onEvent) {
                try {
                    if (!holders[i].onEvent(EVENT, object)) break;
                } catch(e) {
                    console.error(i,EVENT,e);
                }
            }
        }
    }

    var help = {
        title: "General",
        1: {
            title: "Abcdef",
            body: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras pellentesque aliquam tellus, quis finibus odio faucibus sed. Nunc nec dictum ipsum, a efficitur sem. Nullam suscipit quis neque in cursus. Etiam tempus imperdiet scelerisque. Integer ut nisi at est varius rutrum quis eget urna. Morbi blandit vehicula laoreet. Curabitur tincidunt turpis dui, at venenatis risus volutpat et. Donec cursus molestie ligula eu convallis. Curabitur sed quam id ex tristique ultricies. Duis id felis eget massa venenatis vehicula. Aenean eget varius dui. <p>Maecenas vel mauris sit amet erat porta feugiat. Donec facilisis viverra enim, congue tristique lacus pharetra eleifend. Curabitur ac convallis quam. Sed sollicitudin eros vel elit tincidunt tincidunt. Nulla a sapien eget dolor ultrices ullamcorper non vel velit. In faucibus eros in dolor mollis ultricies. Interdum et malesuada fames ac ante ipsum primis in faucibus. Suspendisse potenti. Ut ultrices tellus quis odio condimentum mattis. Cras nec dui ut purus ultrices pharetra sed non lacus. Phasellus pellentesque, turpis nec placerat volutpat, odio metus sollicitudin nunc, ac sollicitudin magna risus id nisl. In nisl sem, venenatis ut tortor vitae, vestibulum dictum turpis. <p>Integer quis nulla at dui consequat condimentum. Curabitur laoreet augue molestie feugiat egestas. Fusce quis fermentum augue, nec venenatis magna. Phasellus feugiat diam vulputate efficitur condimentum. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nullam tortor libero, posuere sit amet sem pharetra, lobortis mollis dui. Quisque auctor sem id odio imperdiet, interdum cursus nulla maximus. Praesent facilisis, quam eget lobortis gravida, leo lectus rhoncus nunc, nec varius ex turpis vel mi. Nam nec orci felis. Sed pharetra dui id tellus sollicitudin euismod sit amet eu elit. Aenean congue non enim a porta. Vestibulum ultricies mattis ipsum eget tristique. Curabitur ut risus urna. Maecenas eget aliquet massa, a rhoncus nisi. Duis et luctus arcu. Fusce in tortor orci. <p>Phasellus et ipsum bibendum, lacinia quam ac, rhoncus felis. Fusce augue tortor, cursus at pharetra quis, tristique vitae sapien. Vivamus sodales elementum eros sit amet tincidunt. Morbi sed felis quis enim tempus consequat eu non lacus. Quisque ac justo enim. Integer fermentum accumsan magna at varius. Duis in diam scelerisque sapien ultricies volutpat. <p>Sed commodo gravida rhoncus. Nulla ac neque mauris. Nulla efficitur, ligula commodo posuere vehicula, enim purus elementum elit, eget pellentesque nibh augue non dolor. Nulla accumsan nisi vitae molestie mattis. Duis tincidunt tellus id massa porta consequat at ut nisl. Mauris id erat imperdiet, pulvinar ligula venenatis, egestas purus. Aenean faucibus, est molestie convallis maximus, ipsum risus molestie sapien, vel congue lorem sapien sit amet lorem. Maecenas tincidunt lorem eget vehicula molestie. Pellentesque sit amet convallis arcu. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nunc vehicula facilisis blandit."
        },
        2: {
            title: "Ghijkl",
            body: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras pellentesque aliquam tellus, quis finibus odio faucibus sed. Nunc nec dictum ipsum, a efficitur sem. Nullam suscipit quis neque in cursus. Etiam tempus imperdiet scelerisque. Integer ut nisi at est varius rutrum quis eget urna. Morbi blandit vehicula laoreet. Curabitur tincidunt turpis dui, at venenatis risus volutpat et. Donec cursus molestie ligula eu convallis. Curabitur sed quam id ex tristique ultricies. Duis id felis eget massa venenatis vehicula. Aenean eget varius dui. <p>Maecenas vel mauris sit amet erat porta feugiat. Donec facilisis viverra enim, congue tristique lacus pharetra eleifend. Curabitur ac convallis quam. Sed sollicitudin eros vel elit tincidunt tincidunt. Nulla a sapien eget dolor ultrices ullamcorper non vel velit. In faucibus eros in dolor mollis ultricies. Interdum et malesuada fames ac ante ipsum primis in faucibus. Suspendisse potenti. Ut ultrices tellus quis odio condimentum mattis. Cras nec dui ut purus ultrices pharetra sed non lacus. Phasellus pellentesque, turpis nec placerat volutpat, odio metus sollicitudin nunc, ac sollicitudin magna risus id nisl. In nisl sem, venenatis ut tortor vitae, vestibulum dictum turpis. <p>Integer quis nulla at dui consequat condimentum. Curabitur laoreet augue molestie feugiat egestas. Fusce quis fermentum augue, nec venenatis magna. Phasellus feugiat diam vulputate efficitur condimentum. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nullam tortor libero, posuere sit amet sem pharetra, lobortis mollis dui. Quisque auctor sem id odio imperdiet, interdum cursus nulla maximus. Praesent facilisis, quam eget lobortis gravida, leo lectus rhoncus nunc, nec varius ex turpis vel mi. Nam nec orci felis. Sed pharetra dui id tellus sollicitudin euismod sit amet eu elit. Aenean congue non enim a porta. Vestibulum ultricies mattis ipsum eget tristique. Curabitur ut risus urna. Maecenas eget aliquet massa, a rhoncus nisi. Duis et luctus arcu. Fusce in tortor orci. <p>Phasellus et ipsum bibendum, lacinia quam ac, rhoncus felis. Fusce augue tortor, cursus at pharetra quis, tristique vitae sapien. Vivamus sodales elementum eros sit amet tincidunt. Morbi sed felis quis enim tempus consequat eu non lacus. Quisque ac justo enim. Integer fermentum accumsan magna at varius. Duis in diam scelerisque sapien ultricies volutpat. <p>Sed commodo gravida rhoncus. Nulla ac neque mauris. Nulla efficitur, ligula commodo posuere vehicula, enim purus elementum elit, eget pellentesque nibh augue non dolor. Nulla accumsan nisi vitae molestie mattis. Duis tincidunt tellus id massa porta consequat at ut nisl. Mauris id erat imperdiet, pulvinar ligula venenatis, egestas purus. Aenean faucibus, est molestie convallis maximus, ipsum risus molestie sapien, vel congue lorem sapien sit amet lorem. Maecenas tincidunt lorem eget vehicula molestie. Pellentesque sit amet convallis arcu. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nunc vehicula facilisis blandit."
        },
        3: {
            title: "Mnopqrs",
            body: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras pellentesque aliquam tellus, quis finibus odio faucibus sed. Nunc nec dictum ipsum, a efficitur sem. Nullam suscipit quis neque in cursus. Etiam tempus imperdiet scelerisque. Integer ut nisi at est varius rutrum quis eget urna. Morbi blandit vehicula laoreet. Curabitur tincidunt turpis dui, at venenatis risus volutpat et. Donec cursus molestie ligula eu convallis. Curabitur sed quam id ex tristique ultricies. Duis id felis eget massa venenatis vehicula. Aenean eget varius dui. <p>Maecenas vel mauris sit amet erat porta feugiat. Donec facilisis viverra enim, congue tristique lacus pharetra eleifend. Curabitur ac convallis quam. Sed sollicitudin eros vel elit tincidunt tincidunt. Nulla a sapien eget dolor ultrices ullamcorper non vel velit. In faucibus eros in dolor mollis ultricies. Interdum et malesuada fames ac ante ipsum primis in faucibus. Suspendisse potenti. Ut ultrices tellus quis odio condimentum mattis. Cras nec dui ut purus ultrices pharetra sed non lacus. Phasellus pellentesque, turpis nec placerat volutpat, odio metus sollicitudin nunc, ac sollicitudin magna risus id nisl. In nisl sem, venenatis ut tortor vitae, vestibulum dictum turpis. <p>Integer quis nulla at dui consequat condimentum. Curabitur laoreet augue molestie feugiat egestas. Fusce quis fermentum augue, nec venenatis magna. Phasellus feugiat diam vulputate efficitur condimentum. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nullam tortor libero, posuere sit amet sem pharetra, lobortis mollis dui. Quisque auctor sem id odio imperdiet, interdum cursus nulla maximus. Praesent facilisis, quam eget lobortis gravida, leo lectus rhoncus nunc, nec varius ex turpis vel mi. Nam nec orci felis. Sed pharetra dui id tellus sollicitudin euismod sit amet eu elit. Aenean congue non enim a porta. Vestibulum ultricies mattis ipsum eget tristique. Curabitur ut risus urna. Maecenas eget aliquet massa, a rhoncus nisi. Duis et luctus arcu. Fusce in tortor orci. <p>Phasellus et ipsum bibendum, lacinia quam ac, rhoncus felis. Fusce augue tortor, cursus at pharetra quis, tristique vitae sapien. Vivamus sodales elementum eros sit amet tincidunt. Morbi sed felis quis enim tempus consequat eu non lacus. Quisque ac justo enim. Integer fermentum accumsan magna at varius. Duis in diam scelerisque sapien ultricies volutpat. <p>Sed commodo gravida rhoncus. Nulla ac neque mauris. Nulla efficitur, ligula commodo posuere vehicula, enim purus elementum elit, eget pellentesque nibh augue non dolor. Nulla accumsan nisi vitae molestie mattis. Duis tincidunt tellus id massa porta consequat at ut nisl. Mauris id erat imperdiet, pulvinar ligula venenatis, egestas purus. Aenean faucibus, est molestie convallis maximus, ipsum risus molestie sapien, vel congue lorem sapien sit amet lorem. Maecenas tincidunt lorem eget vehicula molestie. Pellentesque sit amet convallis arcu. Interdum et malesuada fames ac ante ipsum primis in faucibus. Nunc vehicula facilisis blandit."
        }
    };

    return {
        start: start,
        main:main,
        fire:fire,
        initialize:initialize,
        help:help,
    }
}
//document.addEventListener("DOMContentLoaded", (window.WAIN = new Main()).start);
/*document.addEventListener("readystatechange", function(){
    if(document.readyState == "complete"){(
        window.WAIN = new Main()
    ).start()}
});*/
