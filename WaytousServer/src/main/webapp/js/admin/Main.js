/**
 * Created 1/20/17.
 */

function Main() {
    var drawer;
    var content;

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
        var a = document.createElement("script");
        a.setAttribute("src","/js/helpers/Utils.js");
        a.setAttribute("onload","preloaded()");
        document.head.appendChild(a);
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
        u.create(HTML.LINK, {rel:"stylesheet", href:"/css/tracking.css"}, document.head);
        u.create(HTML.LINK, {rel:"stylesheet", href:"/css/admin.css"}, document.head);

        firebase.initializeApp(data.firebase_config);
        database = firebase.database();

        resign(resume);

        var a = u.byId("sign");
        a.parentNode.removeChild(a);

    }

    function resign(callback){
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
            var dialogAbout = u.dialogAbout(right);

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

                    var item = drawer.add(DRAWER.SECTION_PRIMARY, x, holders[x].menu, holders[x].icon, function(){
                        var holder = holders[this.instance];
                      u.clear(content);

                      window.history.pushState({}, null, "/admin/" + holder.page);

                      actionbar.titleNode.innerHTML = holder.title;
                      drawer.headerPrimary.innerHTML = holder.title;

                      holder.start();
                      return false;
                  });
                  item.instance = x;
                }
            }

            drawer.add(DRAWER.SECTION_LAST, "exit", "Log out", "exit_to_app", logout);

            actionbar.titleNode.innerHTML = holders[data.page].title;
            drawer.headerPrimary.innerHTML = holders[data.page].title;
            holders[data.page].start();

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
            window.history.pushState({}, null, "/admin/" + holders[parts[2]].page);
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
