/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 4/24/17.
 */

function Main() {
    var self = this;
    var defaultResources = "/locales/index.en.json";


    var holders = {};
    var holderFiles = [
        "/js/helpers/Utils.js",
        "/js/helpers/Constants",
        "/js/index/HomeHolder",
//        "/js/index/BlablaHolder",
//        "/js/index/SupportHolder",
        "/js/index/AboutHolder",
    ];


    self.start = function() {
        var a = document.createElement("script");
        a.setAttribute("src","/js/helpers/Edequate.js");
        a.setAttribute("onload","preloaded()");
        document.head.appendChild(a);
    }

    preloaded = function() {

        window.u = new Edequate({exportConstants:true, origin:"waytous"});

        u.loading("Loading resources...");

        document.head
            .place(HTML.META, {name:"viewport", content:"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"})
            .place(HTML.LINK, {rel:"stylesheet", href:"https://fonts.googleapis.com/icon?family=Material+Icons", async:"", defer:""})
            .place(HTML.LINK, {rel:"stylesheet", href:"/css/common.css", async:"", defer:""})
            .place(HTML.LINK, {rel:"stylesheet", href:"/css/index.css", async:"", defer:""})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"192x192", href:"/icons/android-chrome-192x192.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"32x32", href:"/icons/favicon-32x32.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"16x16", href:"/icons/favicon-16x16.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"194x194", href:"/icons/favicon-194x194.png"});

        var loaded = 0;
        for(var i in holderFiles) {
            var file = holderFiles[i];
            if(!file.match(/^(https?:)|\//i)) file = "/js/admin/"+file;
            u.require(file, self).then(function(e) {
                loaded++;
                if(e && e.moduleName) {
                    holders[e.type.toLowerCase()] = e;
                }
                if(loaded == u.keys(holderFiles).length) {
                    console.log("Preload finished: "+loaded+" files done.");
                    window.utils = new Utils();

                    loadResources(resume);
                }
            });
        }
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
                u.require(file).then(function(e) {
                    if(failed) return;
                    loaded++;
                    progress.innerHTML = Math.ceil(loaded / files.length * 100) + "%";
                    if(loaded == u.keys(files).length) {
                        initialize.call(main);
                    }
                });
            }

            return;
        }

        try {
            firebase.initializeApp(data.firebase_config);
            database = firebase.database();
        } catch(e) {
            console.error(e);
            resign(resume);
        }

        resign(resume);

        var a = u.byId("sign");
        a.parentNode.removeChild(a);

    }

    function resign(callback){

        u.loading(u.lang.signing_in);
        firebase.auth().signInWithCustomToken(sign.token).then(function(e){
            callback();
        //            console.log("AAA",e)
        }).catch(function(error) {
                          // Handle Errors here.
          var errorCode = error.code;
          var errorMessage = error.message;
          // ...
        });
    }

    function resume() {
        try {

var type = "home";

            window.addEventListener("load",function() { setTimeout(function(){ // This hides the address bar:
                window.scrollTo(0, 1); }, 0);
            });

            var out = u.create("div", {className:"layout"}, "layout");

            self.actionbar = u.actionBar({
                title: holders[type].title,
                onbuttonclick: function(){
                     try {
                         self.drawer.open();
                     } catch(e) {
                         console.error(e);
                     }
                 }
            }, "actionbar");
            var selectLang = u.create(HTML.SELECT, { className: "actionbar-select-lang changeable", onchange: function(e, event) {
                var lang = (this.value || navigator.language).toLowerCase().slice(0,2);
                u.save("lang", lang);
                var resources = "/locales/index."+lang+".json";
                u.lang.overrideResources({"default":defaultResources, resources: resources});
            }}, self.actionbar).place(HTML.OPTION, { name: u.lang.loading, value:"" });

            u.getJSON("/xhr/v1/getResources/index").then(function(json){
                u.clear(selectLang);
                var count = 1;
                selectLang.place(HTML.OPTION, { innerHTML: "Default", value: "" });
                for(var i in json.files) {
                    var a = json.files[i].split(".");
                    selectLang.place(HTML.OPTION, { innerHTML: a[1].toUpperCase(), value: a[1] });
                    if(u.load("lang") == a[1]) selectLang.selectedIndex = count;
                    count++;
                }
            })

            self.drawer = new u.drawer({
                title: "${APP_NAME}",
                collapsed: false,
                logo: {
                    src:"/images/logo.svg",
                },
                onprimaryclick: function(){
                    console.log("onprimaryclick");
                },
                footer: {
                    className: "drawer-footer-label",
                    content: u.create(HTML.DIV)
                        .place(HTML.SPAN, { className: "drawer-footer-link", innerHTML: u.lang.privacy, onclick: showPrivacy})
                        .place(HTML.SPAN, { className: "drawer-footer-link" ,innerHTML: u.lang.terms, onclick: showTerms})
                        .place(HTML.SPAN, { className: "drawer-footer-link", innerHTML: "${APP_NAME} &copy;2017 Edeqa", onclick: function(e){
                            dialogAbout.open();
                            e.preventDefault();
                            e.stopPropagation;
                            return false;
                        }})
                },
                sections: {
                    [DRAWER.SECTION_PRIMARY]: "Main",
                    [DRAWER.SECTION_LAST]: "About",
                }
            }, "drawer");

            var dialogAbout = utils.dialogAbout();

            var switchFullDrawer = function(){
               if(this.parentNode.scrollTop) {
                   self.drawer.toggleSize(true);
                   self.actionbar.toggleSize(true);
               } else {
                   self.drawer.toggleSize(false);
                   self.actionbar.toggleSize(false);
               }
            }
            self.content = u.create(HTML.DIV, {className: "content", onwheel: switchFullDrawer, ontouchmove: switchFullDrawer }, "content");
            u.create(HTML.DIV, {className:"alert"}, out);

            for(var x in holders) {
                if(holders[x] && holders[x].menu) {

                    var categories = {
                        "main": DRAWER.SECTION_PRIMARY,
                        "about": DRAWER.SECTION_LAST
                    }

                    var item = self.drawer.add(categories[holders[x].category], x, holders[x].menu, holders[x].icon, function(){
                        var holder = holders[this.instance];

                        holder.start();
                        return false;
                  });
                  item.instance = x;
                }
            }

            self.actionbar.titleNode.innerHTML = holders[type].title;
            self.drawer.headerPrimary.innerHTML = holders[type].title;
            holders[type].start();
            u.loading.hide();
        } catch(e) {
            console.error(e);
        }
    }

    var logout = function() {
        var to_url = window.location.protocol + "//" + window.location.host + "/";// + window.location.pathname;
        u.get(window.location.href.replace(/:\/\//, '://log:out@')).then(function() {
            window.location = to_url;
        }).catch(function() {
            window.location = to_url;
        });
    }

    function showPrivacy(e) {
        showPrivacy.dialog = showPrivacy.dialog || u.dialog({
            title: u.lang.privacy_policy,
            items: [
                { type: HTML.DIV, className: "privacy-body", innerHTML: u.lang.privacy_policy_body }
            ],
            positive: {
                label: "Close"
            }
        });

        showPrivacy.dialog.open();
        e.preventDefault();
        e.stopPropagation;
        return false;
    }

    function showTerms(e) {
        showTerms.dialog = showTerms.dialog || u.dialog({
            title: u.lang.terms_and_conditions,
            items: [
                { type: HTML.DIV, className: "terms-body", innerHTML: u.lang.terms_and_conditions_body }
            ],
            positive: {
                label: "Close"
            }
        });

        showTerms.dialog.open();
        e.preventDefault();
        e.stopPropagation;
        return false;
    }

    function loadResources(callback) {

        u.lang.overrideResources({"default":defaultResources, callback: callback});

        var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
        var resources = "/locales/index."+lang+".json";

        if(resources != defaultResources) u.lang.overrideResources({"default":defaultResources, resources: resources});
    }


}
//document.addEventListener("DOMContentLoaded", (window.WTU = new Main()).start);
document.addEventListener("readystatechange", function(){if(document.readyState == "complete"){(window.WTU = new Main()).start()}});
