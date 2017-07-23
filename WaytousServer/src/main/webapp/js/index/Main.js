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
    var files = [
        "/js/helpers/Utils.js",
//        "/js/helpers/Constants",
        "/js/index/HomeHolder",
        "/js/index/TrackHolder",
        "/js/index/HelpHolder",
        "/js/index/SupportHolder",
        "/js/index/FeedbackHolder",
//        "/js/index/BlablaHolder",
        "/js/index/AboutHolder"
    ];

    EVENTS = {
    };

    self.start = function() {
        var a = document.createElement("script");
        a.setAttribute("src","/js/helpers/Edequate.js");
        a.setAttribute("onload","preloaded()");
        document.head.appendChild(a);
    };

    preloaded = function() {

        window.u = new Edequate({exportConstants:true, origin:"waytous"});

        u.loading("Loading resources...");

        document.head
            .place(HTML.META, {name:"viewport", content:"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"})
            .place(HTML.LINK, {rel:"stylesheet", href:"https://fonts.googleapis.com/icon?family=Material+Icons", async:"", defer:""})
            .place(HTML.LINK, {rel:"stylesheet", href:"/css/edequate.css", async:"", defer:""})
            .place(HTML.LINK, {rel:"stylesheet", href:"/css/index.css", async:"", defer:""})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"192x192", href:"/icons/android-chrome-192x192.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"32x32", href:"/icons/favicon-32x32.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"16x16", href:"/icons/favicon-16x16.png"})
            .place(HTML.LINK, {rel:"icon", type:"image/png", sizes:"194x194", href:"/icons/favicon-194x194.png"});

        u.require("/js/helpers/Constants").then(function(e){
            u.lang.overrideResources({"default":defaultResources, callback: function(){
                u.eventBus.register(files, {
                    context: self,
                    onprogress: function(loaded) {
                        u.loading(Math.ceil(loaded / files.length * 100) + "%");
                    },
                    onstart: function() {
                        window.utils = new Utils(self);
                    },
                    onsuccess: function() {
                        holders = u.eventBus.holders;
                        resume();
                    },
                    onerror: function(code, origin, error) {
                        console.error(code, origin, error);
                        u.loading.hide();
                        u.lang.updateNode(main.alert.items[1].body, u.lang.error_while_loading_s_code_s.format(origin,code));
                        main.alert.open();
                    }
                });
            }});
            var lang = (u.load("lang") || navigator.language).toLowerCase().slice(0,2);
            var resources = "/locales/index."+lang+".json";
            if(resources != defaultResources) u.lang.overrideResources({"default":defaultResources, resources: resources});
        });
    };

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

            u.getJSON("/rest/v1/getResources",JSON.stringify({type:"index"})).then(function(json){
                u.clear(selectLang);
                var count = 1;
                selectLang.place(HTML.OPTION, { innerHTML: "Default", value: "" });
                for(var i in json.files) {
                    var a = json.files[i].split(".");
                    selectLang.place(HTML.OPTION, { innerHTML: a[1].toUpperCase(), value: a[1] });
                    if(u.load("lang") == a[1]) selectLang.selectedIndex = count;
                    count++;
                }
            });

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
                            e.stopPropagation();
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
                    self.buttonScrollTop.show(HIDING.OPACITY);
                    clearTimeout(self.buttonScrollTop.hideTimeout);
                    self.buttonScrollTop.hideTimeout = setTimeout(function(){
                        self.buttonScrollTop.hide(HIDING.OPACITY);
                    }, 1500);
                } else {
                    self.drawer.toggleSize(false);
                    self.actionbar.toggleSize(false);
                    self.buttonScrollTop.hide(HIDING.OPACITY);
                }
            };
            self.content = u.create(HTML.DIV, {className: "content", onwheel: switchFullDrawer, ontouchmove: switchFullDrawer }, "content");
            u.create(HTML.DIV, {className:"alert"}, out);
            self.buttonScrollTop = u.create(HTML.BUTTON, {
                className: "button-scroll-top changeable hidden",
                onclick: function() {
                    self.content.scrollIntoView({block:"start", behaviour: "smooth"});
                    switchFullDrawer.call(self.content);
                },
                innerHTML: "keyboard_arrow_up"
            }, out);

            for(var x in holders) {
                if(holders[x] && holders[x].menu) {

                    var categories = {
                        "main": DRAWER.SECTION_PRIMARY,
                        "about": DRAWER.SECTION_LAST
                    };

                    var item = self.drawer.add(categories[holders[x].category], x, holders[x].menu, holders[x].icon, function(){
                        var holder = holders[this.instance];
                        self.drawer.toggleSize(false);
                        self.actionbar.toggleSize(false);
                        self.actionbar.setTitle(holder.title);
                        u.fire(holder.type);
//                        holder.start();
                        return false;
                    });
                    item.instance = x;
                }
            }

            u.lang.updateNode(self.actionbar.titleNode, holders[type].title);
            u.lang.updateNode(self.drawer.headerPrimary, holders[type].title);
            holders[type].start();
            u.loading.hide();
        } catch(e) {
            console.error(e);
        }
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
        e.stopPropagation();
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
        e.stopPropagation();
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
document.addEventListener("readystatechange", function(){if(document.readyState === "complete"){(window.WTU = new Main()).start()}});
