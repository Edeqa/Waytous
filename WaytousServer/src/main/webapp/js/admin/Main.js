/**
 * Created 1/20/17.
 */

function Main() {
    var firebaseVersion = "3.8.0";
    var drawer;
    var content;

    var holders = {};
    var holderFiles = [
        "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-app.js",
        "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-auth.js",
        "https://www.gstatic.com/firebasejs/"+firebaseVersion+"/firebase-database.js",
        "Home",
        "/js/helpers/Utils.js",
        "/js/helpers/Constants",
        "Create",
        "Group",
        "Groups",
        "Settings",
        "Help",
        "User"
    ];


    function start() {
        var a = document.createElement("script");
        a.setAttribute("src","/js/helpers/Edequate.js");
        a.setAttribute("onload","preloaded()");
        document.head.appendChild(a);
    }


    preloaded = function() {
        if(!data.page) {
            window.location.href = "/admin/home";
            return;
        }

        window.u = new Edequate({exportConstants:true, origin:"waytous"});

        u.loading("Loading resources...");

        u.create(HTML.META, {name:"viewport", content:"width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"}, document.head);
        u.create(HTML.LINK, {rel:"stylesheet", href:"https://fonts.googleapis.com/icon?family=Material+Icons", async:"", defer:""}, document.head);
        u.create(HTML.LINK, {rel:"stylesheet", href:"/css/tracking.css", async:"", defer:""}, document.head);
        u.create(HTML.LINK, {rel:"stylesheet", href:"/css/admin.css", async:"", defer:""}, document.head);


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
                    window.utils = new Utils();

                    initialize();

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
                u.require(file, function(e) {
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

        firebase.initializeApp(data.firebase_config);
        database = firebase.database();

        resign(resume);

        var a = u.byId("sign");
        a.parentNode.removeChild(a);

    }

    function resign(callback){

        u.loading("Signing in...");
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
            window.addEventListener("load",function() { setTimeout(function(){ // This hides the address bar:
                window.scrollTo(0, 1); }, 0);
            });

            var out = u.create("div", {className:"layout"}, document.body);

            drawer = new u.drawer({
                title: "${APP_NAME}",
                subtitle: "Admin",
                collapsed: "admin:drawer:collapsed",
                logo: {
                    src:"/images/logo.svg",
                },
                onprimaryclick: function(){
                    console.log("onprimaryclick");
                },
                footer: {
                    className: "drawer-footer-label",
                    content: u.create(HTML.DIV).place(HTML.A, {innerHTML: "${APP_NAME} &copy;2017 Edeqa", href: "", onclick: function(e){
                        dialogAbout.open();
                        e.preventDefault();
                        e.stopPropagation;
                        return false;
                    }}).place(HTML.SPAN, "\nBuild " + data.version)
                }
            }, out);

            var right = u.create("div", {className:"right"}, out);
            var dialogAbout = utils.dialogAbout(right);

            actionbar = u.actionBar({
                title: holders[data.page].title,
                onbuttonclick: function(){
                     try {
                         drawer.open();
                     } catch(e) {
                         console.error(e);
                     }
                 }
            }, right);
            content = u.create(HTML.DIV, {className: "content"}, right);
          u.create(HTML.DIV, {className:"alert"}, right);

            for(var i in holderFiles) {
                var x = holderFiles[i].toLowerCase();
                if(holders[x] && holders[x].menu) {

                    var item = drawer.add(u.DRAWER.SECTION_PRIMARY, x, holders[x].menu, holders[x].icon, function(){
                        var holder = holders[this.instance];

                    if(holder.move) {
                          window.history.pushState({}, null, "/admin/" + holder.page);
                          actionbar.titleNode.innerHTML = holder.title;
                          drawer.headerPrimary.innerHTML = holder.title;
                      }


                      holder.start();
                      return false;
                  });
                  item.instance = x;
                }
            }

            drawer.add(u.DRAWER.SECTION_LAST, "exit", "Log out", "exit_to_app", logout);

            actionbar.titleNode.innerHTML = holders[data.page].title;
            drawer.headerPrimary.innerHTML = holders[data.page].title;
            holders[data.page].start();
            u.loading.hide();
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
            u.clear(content);
            actionbar.titleNode.innerHTML = holders[parts[2]].title;
            drawer.headerPrimary.innerHTML = holders[parts[2]].title;
            holders[parts[2]].start(parts);
            if(holders[parts[2]].move) {
                window.history.pushState({}, null, "/admin/" + holders[parts[2]].page);
            }
        }
    }

    return {
        start: start,
        switchTo: switchTo,
        resign: resign,
    }
}
//document.addEventListener("DOMContentLoaded", (window.WTU = new Main()).start);
document.addEventListener("readystatechange", function(){if(document.readyState == "complete"){(window.WTU = new Main()).start()}});
