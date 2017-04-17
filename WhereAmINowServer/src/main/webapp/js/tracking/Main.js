/**
 * Created 1/20/17.
 */

// git filter-branch --force --index-filter "git rm -r --cached --ignore-unmatch WhereAmINowServer/build" --prune-empty --tag-name-filter cat -- --all

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
    var defaultLang = "en-us";

    if (false && "serviceWorkers" in navigator) {
        window.addEventListener("load", function() {
            navigator.serviceWorker.register("/ServiceWorker.js")
            .then(function(registration) {
                console.log("ServiceWorker registration successful with scope:", registration);
            })
            .catch(function(err) {
                console.error("ServiceWorker registration failed:", err);
                throw new Error("ServiceWorker error:",err);
            });
        });
    }

    function start() {
//        firebase.auth().signInWithCustomToken(sign.token)
//        .then(function(e){
//            window.location.href = "/tracking/home";
//            return;

            var a = document.createElement("script");
            a.setAttribute("src","/js/helpers/Utils.js");
            a.setAttribute("onload","preloaded()");
            document.head.appendChild(a);
//        }
    }

    preloaded = function(){
        window.u = new Utils(main);

        main.appName = "Waytogo";
        main.layout = document.body;
        main.layout.classList.add("layout");
//        main.layout = u.create("div", {className:"layout"}, document.body);

        setTimeout(function(){
            loadResources();
        },0);

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
        var consoleLayer = u.create(HTML.DIV, {className: "console hidden", innerHTML:"Console:\n",
        onclick:function(){
            this.hide();
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
            consoleLayer.show();
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
        u.create(HTML.LINK, {rel:"icon", type:"image/png", sizes:"192x192", href:"/icons/android-chrome-192x192.png"},document.head);
        u.create(HTML.LINK, {rel:"icon", type:"image/png", sizes:"32x32", href:"/icons/favicon-32x32.png"},document.head);
        u.create(HTML.LINK, {rel:"icon", type:"image/png", sizes:"16x16", href:"/icons/favicon-16x16.png"},document.head);
        u.create(HTML.LINK, {rel:"icon", type:"image/png", sizes:"194x194", href:"/icons/favicon-194x194.png"},document.head);
        u.create(HTML.LINK, {rel:"manifest", href:"/icons/manifest.json"},document.head);
        u.create(HTML.LINK, {rel:"mask-icon", href:"/icons/safari-pinned-tab.svg", color:"#00aaaa"},document.head);
        u.create(HTML.LINK, {rel:"icon",type:"image/x-icon", href:"/icons/favicon.ico"},document.head);
        u.create(HTML.LINK, {rel:"shortcut icon", href:"/icons/favicon.ico"},document.head);
        u.create(HTML.LINK, {rel:"apple-touch-startup-image", href: "/icons/apple-touch-icon.png"},document.head);
        u.create(HTML.META, {name:"apple-mobile-web-app-title", content:main.appName},document.head);
        u.create(HTML.META, {name:"apple-mobile-web-app-capable", content:"yes"},document.head);
//        u.create(HTML.META, {name:"apple-mobile-web-app-status-bar-style", content:"black-translucent"},document.head);
        u.create(HTML.META, {name:"mobile-web-app-capable", content:"yes"},document.head);
        u.create(HTML.META, {name:"application-name", content:main.appName},document.head);
        u.create(HTML.META, {name:"application-shortName", content:main.appName},document.head);
        u.create(HTML.META, {name:"msapplication-TileColor", content:"#00aaaa"},document.head);
        u.create(HTML.META, {name:"msapplication-TileImage", content:"/icons/mstile-144x144.png"},document.head);
        u.create(HTML.META, {name:"msapplication-config", content:"/icons/browserconfig.xml"},document.head);
        u.create(HTML.META, {name:"theme-color", content:"#aaeeee"},document.head);

        loadScripts.call(main);
    }

    function loadScripts(){

        main.right = u.create("div", {className:"right changeable"}, main.layout);
        main.fire = fire;
        main.help = help;
        main.options = options;
        main.holders = holders;
        main.me = me;
        main.initialize = initialize;

        main.alert = main.alert || u.dialog({
             className: "alert-dialog",
             items: [
                 { type: HTML.DIV, label: u.lang.error_while_loading_service },
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
        main.toast = u.toast;
        main.right.appendChild(main.toast);

        var files = [
            "https://www.gstatic.com/firebasejs/3.7.4/firebase-app.js", // https://firebase.google.com/docs/web/setup
            "https://www.gstatic.com/firebasejs/3.7.4/firebase-auth.js",
            "https://www.gstatic.com/firebasejs/3.7.4/firebase-database.js",
            // "https://code.jquery.com/jquery-3.1.1.min.js",
            "https://cdnjs.cloudflare.com/ajax/libs/fingerprintjs2/1.5.1/fingerprint2.min.js", // https://cdnjs.com/libraries/fingerprintjs2
            "/js/helpers/MyUser",
            "/js/helpers/MyUsers",
            "/js/helpers/NoSleep.js",
            "PropertiesHolder", // must be first of holders
            "AddressHolder",
            "GpsHolder",
            "ButtonHolder",
            "CameraHolder",
            "DistanceHolder",
            "DrawerHolder",
            // "FabHolder",
            "HelpHolder",
            "MapHolder",
            "MarkerHolder",
            "MessagesHolder",
            "NavigationHolder",
            "OptionHolder",
            "PlaceHolder",
            "SavedLocationHolder",
//            "SocialHolder",
            "StreetViewHolder",
            "TrackingHolder",
            "TrackHolder",
//            "WelcomeHolder",
//            "SampleHolder",
        ];
        // u.require("https://code.jquery.com/jquery-3.1.1.min.js");

        var ordered = [];
        for(var i in files) {
            if(!files[i].match(/[.\/]/)) ordered.push(files[i]);
        }

        try{
            var loaded = 0;
            var failed = false;
            var inordered = {};
            for(var i in files) {
                var file = files[i];
                if(!file.match(/^(https?:)|\//i)) file = "/js/holders/"+file;
                var viaXhr = false;// ! file.match(/^https?:/i);

                u.require(file, function(e) {
                    if(failed) return;
                    loaded++;
                    if(e && e.type) {
                        inordered[e.moduleName] = e;
                    }
                    progress.innerHTML = Math.ceil(loaded / files.length * 100) + "%";
                    if(loaded == u.keys(files).length) {
                        console.warn("Preload finished: "+loaded+" files done.");

                        for(var i in ordered) {
                            holders[inordered[ordered[i]].type] = inordered[ordered[i]];
                        }

                        u.getUuid(initialize.bind(main));
                    }
                },function(code, moduleName, event) {
                    console.log(code, moduleName, event.srcElement.src);
                    if(failed) return;
                    failed = true;
                    loading.hide();

                    u.lang.updateNode(main.alert.items[1].body, u.lang.error_while_loading_s_code_s.format(moduleName,code));
//                    main.alert.items[1].body.innerHTML = u.lang.error_while_loading_s_code_d.format(moduleName,code);
                    main.alert.open();

                }, main, viaXhr);
            }
        } catch(e) {
            u.lang.updateNode(main.alert.items[1].body, u.lang.error_while_initializing_s.format(e.message));
            main.alert.open();
        }
        // main.right.webkitRequestFullScreen();


        /*window.addEventListener("load",function() { setTimeout(function(){ // This hides the address bar:
            window.scrollTo(0, 1); }, 0);
        });*/

    }

    function initialize() {

        loading.classList.add("hidden");
        main.alpha = u.create("div", {className:"alpha", innerHTML:"&alpha;"}, main.layout);

        if(!firebase || !firebase.database || !firebase.auth) {
            console.error("Failed firebase loading, trying again...");
//debugger;
            var files = [];
            if(!firebase) files.push("https://www.gstatic.com/firebasejs/3.6.8/firebase-app.js");
            if(!firebase.database) files.push("https://www.gstatic.com/firebasejs/3.6.8/firebase-database.js");
            if(!firebase.auth) files.push("https://www.gstatic.com/firebasejs/3.6.8/firebase-auth.js");

            var loaded = 0;
            var failed = false;
            for(var i in files) {
                var file = files[i];
                u.require(file, function(e) {
                    if(failed) return;
                    loaded++;
                    progress.innerHTML = Math.ceil(loaded / files.length * 100) + "%";
                    if(loaded == u.keys(files).length) {
                        initialize.call(main);
                    }
                },function(code, moduleName, event) {
                    console.log(code, moduleName, event.srcElement.src);
                    if(failed) return;
                    failed = true;

                    u.lang.updateNode(main.alert.items[1].body, u.lang.error_while_loading_s_code_s.format(moduleName,code));
                    main.alert.open();
                }, main);
            }

            return;
        }
        firebase.initializeApp(data.firebase_config);
        database = firebase.database();
//throw new Error("A");

        for(var x in holders){
            try {
                if(holders[x] && holders[x].start) holders[x].start();
            } catch (e) {
                u.lang.updateNode(main.alert.items[1].body, u.lang.error_while_initializing_s_s.format(x,e.stack));
                main.alert.open();
                return;
            }
        }

        setTimeout(function(){
            main.users = users = new MyUsers(main);

            if(me == null){
                main.me = me = new MyUser(main);
                me.user = true;
                me.color = "#0000FF";
                me.number = 0;
                me.active = true;
                me.selected = true;

                if(u.load("properties:name")){
                    me.name = u.load("properties:name");
                }
            }

            users.setMe();
        },0);

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
        if(!EVENT) return;
        setTimeout(function(){
            for(var i in holders) {
                if(holders[i] && holders[i].onEvent) {
                    try {
                        if (!holders[i].onEvent(EVENT, object)) break;
                    } catch(e) {
                        console.error(i,EVENT,e);
                    }
                }
            }
        }, 0);
    }

    function loadResources() {
        u.lang.overrideResources(defaultLang);
        var lang = (u.load("lang") || navigator.language || defaultLang).toLowerCase();
        if(lang != defaultLang) u.lang.overrideResources(lang);
    }

    function help(){
        return {
            title: u.lang.general,
            1: {
                title: "Abcdef",
                body: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras pellentesque aliquam tellus, quis finibus odio faucibus sed. Nunc nec dictum ipsum, a efficitur sem. Nullam suscipit quis neque in cursus. Etiam tempus imperdiet scelerisque. Integer ut nisi at est varius rutrum quis eget urna. "
            }
        }
    }

    function options(){
        return {
            id: "general",
            title: u.lang.general,
            categories: [
                {
                    id: "general:main",
                    title: u.lang.main,
                    items: [
                        {
                            id:"main:lang",
                            type: HTML.SELECT,
                            label: u.lang.language,
                            default: u.load("lang") || "",
                            onaccept: function(e, event) {
                                var res = (this.value || navigator.language || defaultLang).toLowerCase();
                                u.save("lang", res);
                                u.lang.overrideResources(res);
                            },
                            values: {"": u.lang.default, "en-us": u.lang.english, "ru-ru": u.lang.russian }
                        },
                    ]
                },
                {
                    id: "general:notifications",
                    title: u.lang.notifications,
                    items: [
                        {
                            id:"main:notification",
                            type: HTML.CHECKBOX,
                            label: u.lang.onscreen_notifications,
                            checked: !u.load("main:disable_notification"),
                            onaccept: function(e, event) {
                                u.save("main:disable_notification", !this.checked);
                            },
                            onshow: function(e) {
                                if (!("Notification" in window) || Notification.permission.toLowerCase() === 'denied') {
                                    e.parentNode.hide();
                                }
                            }
                        }
                    ]
                }
            ]
        }
    }

    return {
        start: start,
        main:main,
        fire:fire,
        initialize:initialize,
        help:help,
        options:options,
    }
}
//document.addEventListener("DOMContentLoaded", (window.WAIN = new Main()).start);
/*document.addEventListener("readystatechange", function(){
    if(document.readyState == "complete"){(
        window.WAIN = new Main()
    ).start()}
});*/
