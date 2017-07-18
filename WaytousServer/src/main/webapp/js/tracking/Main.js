/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 1/20/17.
 */

// git filter-branch --force --index-filter "git rm -r --cached --ignore-unmatch WaytousServer/build" --prune-empty --tag-name-filter cat -- --all

function Main() {
    var firebaseVersion = "4.1.1";
    var holders = {};
    var users;
    var me;
    var layout;
    var right;
    var origin;
    var main = w = this;
    var alert;
    var defaultResources = "/locales/tracking.en.json";

    if ("serviceWorker" in navigator) {
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
        var a = document.createElement("script");
        a.setAttribute("src","/js/helpers/Edequate.js");
        a.setAttribute("onload","preloaded()");
        document.head.appendChild(a);
    }

    preloaded = function(){
        window.u = new Edequate({exportConstants:true, origin:"waytous"});

        main.appName = "${APP_NAME}";
        main.right = main.layout = u.create({className:"layout changeable"}, document.body);

        u.loading("0%");
        u.require("/js/helpers/Constants").then(function(e){

            u.lang.overrideResources({"default":defaultResources, callback: function(){
                initializeHeader();
                initializeProperties();
                loadScripts();
            }});
            var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
            var resources = "/locales/tracking."+lang+".json";
            if(resources != defaultResources) u.lang.overrideResources({"default":defaultResources, resources: resources});

        });

        window.addEventListener("load", function() { window. scrollTo(0, 0); });
//        document.addEventListener("touchmove", function(e) { e.preventDefault() });

        //addConsoleLayer(main.right);

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

        document.head
            .place(HTML.META, {name:"viewport", content:"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"})
            .place(HTML.LINK, {rel:HTML.STYLESHEET, href:"/css/edequate.css"})
            .place(HTML.LINK, {rel:HTML.STYLESHEET, href:"/css/tracking.css"})
            .place(HTML.LINK, {rel:HTML.STYLESHEET, href:"https://fonts.googleapis.com/icon?family=Material+Icons"})
            .place(HTML.LINK, {rel:"apple-touch-icon", href:"/icons/apple-touch-icon.png"})
            .place(HTML.LINK, {rel:"apple-touch-icon", sizes:"60x60", href:"/icons/apple-touch-icon-60x60.png"})
            .place(HTML.LINK, {rel:"apple-touch-icon", sizes:"76x76", href:"/icons/apple-touch-icon-76x76.png"})
            .place(HTML.LINK, {rel:"apple-touch-icon", sizes:"120x120", href:"/icons/apple-touch-icon-120x120.png"})
            .place(HTML.LINK, {rel:"apple-touch-icon", sizes:"152x152", href:"/icons/apple-touch-icon-152x152.png"})
            .place(HTML.LINK, {rel:"apple-touch-icon", sizes:"180x180", href:"/icons/apple-touch-icon.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"192x192", href:"/icons/android-chrome-192x192.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"32x32", href:"/icons/favicon-32x32.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"16x16", href:"/icons/favicon-16x16.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"194x194", href:"/icons/favicon-194x194.png"})
            .place(HTML.LINK, {rel:"manifest", href:"/icons/manifest.json"})
            .place(HTML.LINK, {rel:"mask-icon", href:"/icons/safari-pinned-tab.svg", color:"#00aaaa"})
            .place(HTML.LINK, {rel:"icon",type:"image/x-icon", href:"/icons/favicon.ico"})
            .place(HTML.LINK, {rel:"shortcut icon", href:"/icons/favicon.ico"})
            .place(HTML.LINK, {rel:"apple-touch-startup-image", href: "/icons/apple-touch-icon.png"})
            .place(HTML.META, {name:"apple-mobile-web-app-title", content:main.appName})
            .place(HTML.META, {name:"apple-mobile-web-app-capable", content:"yes"})
            .place(HTML.META, {name:"apple-mobile-web-app-status-bar-style", content:"black-translucent"})
            .place(HTML.META, {name:"mobile-web-app-capable", content:"yes"})
            .place(HTML.META, {name:"application-name", content:main.appName})
            .place(HTML.META, {name:"application-shortName", content:main.appName})
            .place(HTML.META, {name:"msapplication-TileColor", content:"#00aaaa"})
            .place(HTML.META, {name:"msapplication-TileImage", content:"/icons/mstile-144x144.png"})
            .place(HTML.META, {name:"msapplication-config", content:"/icons/browserconfig.xml"})
            .place(HTML.META, {name:"theme-color", content:"#aaeeee"});

    }

    function initializeProperties() {
        main.help = help;
        main.options = options;
        main.me = me;
        main.initialize = initialize;
        main.eventBus = u.eventBus;
        main.fire = u.fire;
        main.toast = u.toast;
        main.right.appendChild(main.toast);
        main.alert = main.alert || u.dialog({
             queue: true,
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
         }, document.body);
        main.alpha = u.create("div", {className:"alpha", innerHTML:"&beta;"}, main.right);
////// FIXME - remove when no alpha
        var alphaDialog = u.dialog({
            className: "alert-dialog",
            items: [
                { type: HTML.DIV, innerHTML: u.lang.alpha_1 },
                { type: HTML.DIV, innerHTML: u.lang.alpha_2 },
                { type: HTML.DIV, innerHTML: u.lang.alpha_3 },
                { type: HTML.DIV, innerHTML: u.lang.alpha_4 },
                { type: HTML.DIV, innerHTML: u.lang.alpha_5 },
                { type: HTML.DIV, innerHTML: u.lang.alpha_6 },
                { type: HTML.DIV, innerHTML: u.lang.alpha_7 },
            ],
            positive: {
                label: u.lang.ok,
                onclick: function(){
                    alphaDialog.close();
                }
            },
        }, main.right);
        main.alpha.addEventListener("click", alphaDialog.open.bind(alphaDialog));
    }

    function loadScripts(){
        var files = [
            "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-app.js", // https://firebase.google.com/docs/web/setup
            "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-auth.js",
            "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-database.js",
            "https://cdnjs.cloudflare.com/ajax/libs/fingerprintjs2/1.5.1/fingerprint2.min.js", // https://cdnjs.com/libraries/fingerprintjs2
//            "/js/all.js",
            "/js/helpers/Utils.js",
            "/js/helpers/MyUser",
            "/js/helpers/MyUsers",
            "/js/helpers/NoSleep.js",
            "/js/tracking/PropertiesHolder", // must be first of holders
            "/js/tracking/AddressHolder",
            "/js/tracking/GpsHolder",
            "/js/tracking/ButtonHolder",
            "/js/tracking/CameraHolder",
            "/js/tracking/DistanceHolder",
            "/js/tracking/DrawerHolder",
            // "/js/tracking/FabHolder",
            "/js/tracking/HelpHolder",
            "/js/tracking/MapHolder",
            "/js/tracking/MarkerHolder",
            "/js/tracking/MessagesHolder",
            "/js/tracking/NavigationHolder",
            "/js/tracking/OptionHolder",
            "/js/tracking/PlaceHolder",
            "/js/tracking/SavedLocationHolder",
            "/js/tracking/ShareHolder",
            "/js/tracking/StreetViewHolder",
            "/js/tracking/TrackingHolder",
            "/js/tracking/TrackHolder",
//            "/js/tracking/WelcomeHolder",
//            "/js/tracking/SampleHolder",
        ];

        u.eventBus.register(files, {
            context: main,
            onprogress: function(loaded) {
                u.loading(Math.ceil(loaded / files.length * 100) + "%");
            },
            onstart: function() {
                window.utils = new Utils(main);
            },
            onsuccess: function() {
                utils.getUuid(initialize);
            },
            onerror: function(code, origin, error) {
                console.error(code, origin, error);
                u.loading.hide();
                u.lang.updateNode(main.alert.items[1].body, u.lang.error_while_loading_s_code_s.format(origin,code));
                main.alert.open();
            }
        });

        // main.right.webkitRequestFullScreen();
        /*window.addEventListener("load",function() { setTimeout(function(){ // This hides the address bar:
            window.scrollTo(0, 1); }, 0);
        });*/

    }

    function initialize() {

        if(!firebase || !firebase.database || !firebase.auth) {
            console.error("Failed firebase loading, trying again...");
//debugger;
            var files = [];
            if(!firebase) files.push("https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-app.js");
            if(!firebase.database) files.push("https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-database.js");
            if(!firebase.auth) files.push("https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-auth.js");

            var loaded = 0;
            var failed = false;
            for(var i in files) {
                var file = files[i];
                u.require(file, main).then(function(e) {
                    if(failed) return;
                    loaded++;
                    u.loading(Math.ceil(loaded / files.length * 100) + "%");
                    if(loaded == u.keys(files).length) {
                        initialize.call(main);
                    }
                }).catch(function(code, moduleName, event) {
                    console.log(code, moduleName, event.srcElement.src);
                    if(failed) return;
                    failed = true;

                    u.lang.updateNode(main.alert.items[1].body, u.lang.error_while_loading_s_code_s.format(moduleName,code));
                    main.alert.open();
                });
            }

            return;
        }
        firebase.initializeApp(data.firebase_config);
        database = firebase.database();

        u.loading.hide();

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

        window.addEventListener("load", hideAddressBar );
        window.addEventListener("orientationchange", hideAddressBar );

    }

    function hideAddressBar() {
        if(!window.location.hash) {
            if(document.height < window.outerHeight) {
                document.body.style.height = (window.outerHeight + 50) + 'px';
            }
            setTimeout( function(){ window.scrollTo(0, 1); }, 50 );
        }
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
                                var lang = (this.value || navigator.language).toLowerCase().slice(0,2);
                                u.save("lang", lang);
                                var resources = "/locales/tracking."+lang+".json";
                                u.lang.overrideResources({"default":defaultResources, resources: resources});
                            },
                            values: {"": u.lang.default, "en": u.lang.english, "ru": u.lang.russian }
                        },
                        {
                            id:"main:reset_dialogs",
                            type: HTML.BUTTON,
                            label: u.lang.dialogs_positions,
                            itemClassName: "media-hidden",
                            innerHTML: u.lang.reset,
                            onclick: function(e, event) {
                                for(var x in localStorage) {
                                    if(x.indexOf(u.origin + ":dialog:") == 0) {
                                        delete localStorage[x];
                                    }
                                }
                                var items = document.getElementsByClassName("modal");
                                for(var i in items) {
                                    items[i].style = "";
                                }
                                this.dialog.close();
                            }
                        }
                    ]
                },
                {
                    id: "general:notifications",
                    title: u.lang.notifications,
                    items: [
                        {
                            id:"notification:disable",
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
                },
                {
                    id: "general:about",
                    title: "About",
                    items: [
                        {
                            id:"about:general",
                            type: HTML.DIV,
                            className: "options-dialog-item--about",
                            innerHTML: "Waytous",
                        },
                        {
                            id:"about:legal",
                            type: HTML.DIV,
                            className: "options-dialog-item-about",
                            innerHTML: "Legal information",
                        },
                        {
                            id:"about:terms",
                            type: HTML.DIV,
                            className: "options-dialog-item-about",
                            innerHTML: "Terms and conditions",
                        },
                        {
                            id:"about:other",
                            type: HTML.DIV,
                            className: "options-dialog-item-about",
                            innerHTML: "Third party components",
                        }
                    ]
                }
            ]
        }
    }

    return {
        start: start,
        main:main,
//        fire:fire,
        initialize:initialize,
        help:help,
        options:options,
    }
}
