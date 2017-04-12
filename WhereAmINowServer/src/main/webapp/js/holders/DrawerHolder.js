/**
 * Created 2/8/17.
 */
EVENTS.UPDATE_ACTIONBAR_SUBTITLE = "update_actionbar_subtitle";

DRAWER = {
    SECTION_PRIMARY: 0,
    SECTION_COMMUNICATION: 2,
    SECTION_NAVIGATION: 3,
    SECTION_VIEWS: 4,
    SECTION_MAP: 7,
    SECTION_MISCELLANEOUS: 8,
    SECTION_LAST: 9
};

function DrawerHolder(main) {

    var drawer;
    var headerName;
    var sections;
    var title;
    var headerTitle;
    var subtitle;
    var menu;
    var alphaDialog;
    var backButtonAction;
    var footerButton;
    var collapsed;
    var footerButtonCollapseDiv;
    var footerButtonExpandDiv;


    var footerButtonSvg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"2 2 14 14",
        fit: "",
        version:"1.1",
        width: 24,
        height: 24,
        preserveAspectRatio: "xMidYMid meet",
        className: "drawer-menu-item-icon drawer-footer-button",
        onclick: function(e) {
            u.save("drawer:collapsed", !collapsed);
            this.replaceChild(collapsed ? footerButtonExpandDiv : footerButtonCollapseDiv, this.firstChild);
        }
    };
    var footerButtonCollapsePath = {
        xmlns:"http://www.w3.org/2000/svg",
        d: "M5.46 8.846l3.444-3.442-1.058-1.058-4.5 4.5 4.5 4.5 1.058-1.057L5.46 8.84zm7.194 4.5v-9h-1.5v9h1.5z",
    };
    var footerButtonExpandPath = {
        xmlns:"http://www.w3.org/2000/svg",
        d: "M5.46 8.846l3.444-3.442-1.058-1.058-4.5 4.5 4.5 4.5 1.058-1.057L5.46 8.84zm7.194 4.5v-9h-1.5v9h1.5z"
    };


    var target = window; // this can be any scrollable element
    var last_y = 0;

    var start = function() {

        collapsed = u.load("drawer:collapsed");
        drawer = new Drawer({collapsed: collapsed}, main.layout);

        var actionbar = u.create(HTML.DIV, {className:"actionbar"}, main.right);
        u.create(HTML.SPAN, {innerHTML:"menu", className:"actionbar-button", onclick: function(){
            try {
                drawer.open();
            } catch(e) {
                console.error(e);
            }
        },onfocus:function(){}}, actionbar);
        var label = u.create(HTML.DIV, {className:"actionbar-label"}, actionbar);
        title = u.create(HTML.DIV, {className:"actionbar-label-title", innerHTML:main.appName}, label);
        subtitle = u.create(HTML.DIV, {className:"actionbar-label-subtitle"}, label);

        setTimeout(function(){
            main.fire(EVENTS.CREATE_DRAWER, drawer);
        },0);

        window.history.pushState(null, document.title, location.href);
        backButtonAction = function (event) {
           window.history.pushState(null, document.title, location.href);
           drawer.toggle();
       }


////// FIXME - remove when no alpha
        alphaDialog = alphaDialog || u.dialog({
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
        });
        main.alpha.addEventListener("click", function(){
            alphaDialog.open();
        });



    };

    function Drawer(options, insertTo) {
        collapsed = options.collapsed;
        var layout = u.create(HTML.DIV, {
            className:"drawer changeable" + (collapsed ? " drawer-collapsed" : ""),
            tabindex: -1,
            onblur: function(){
                 this.close();
                 return true;
            },
            open: function() {
                 this.classList.add("drawer-open");
                 this.scrollTop = 0;
                 this.menu.scrollTop = 0;
                 this.focus();
            },
            close: function(){
                 this.classList.remove("drawer-open");
            },
            toggle: function() {
                if(this.classList.contains("drawer-open")) {
                    this.blur();
                } else {
                    this.open();
                }
            }
         });
         insertTo.insertBefore(layout,insertTo.firstChild);


         layout.frame = u.create("iframe", {width:"100%",height:"1%", style:"position:absolute;z-index:-1"}, layout);
         layout.frame.contentWindow.addEventListener("resize",function(){
            if(!layout.resizeTask) layout.resizeTask = setTimeout(function(){
                main.fire(EVENTS.CAMERA_UPDATE);
                delete layout.resizeTask;
            }, 500);
         });

         layout.header = u.create(HTML.DIV, { className:"drawer-header changeable" }, layout);
         u.create(HTML.IMG, {
            className:"drawer-header-logo changeable",
            src:"/images/logo.svg",
            onclick: function(){
                alphaDialog.open();
            }
         }, layout.header);
         headerName = u.create(HTML.DIV, {className:"drawer-header-name changeable", onclick: function(evt){
                layout.blur();
                main.me.fire(EVENTS.SELECT_SINGLE_USER);
            }}, layout.header);
         headerTitle = u.create(HTML.DIV, {className:"drawer-header-title changeable", innerHTML:main.appName}, layout.header);
         u.create(HTML.DIV, {className:"drawer-header-subtitle changeable", innerHTML: u.lang.be_always_on_the_same_way }, layout.header);


        layout.items = {};


        layout.menu = u.create(HTML.DIV, {className:"drawer-menu changeable"}, layout);
        sections = [];
        for(var i=0;i<10;i++){
            sections[i] = u.create(HTML.DIV, {className:"changeable hidden" + (i==9 ? "" : " divider")}, layout.menu);
        }

        layout.add = function(section,id,name,icon,callback) {
            layout.items[id] = {
                name:name,
                icon:icon,
                callback:callback
            };
            var th = u.create(HTML.DIV, {
                className:"drawer-menu-item",
                onclick: function (event) {
                    setTimeout(function () {
                        layout.blur();
                        callback(event);
                    }, 100);
                },
                hide: function() {
                    this.classList.add("hidden");
                    this.fixShowing();
                    return this;
                },
                show: function() {
                    this.classList.remove("hidden");
                    this.fixShowing();
                    return this;
                },
                enable: function() {
                    this.classList.remove("disabled");
                    return this;
                },
                disable: function() {
                    this.classList.add("disabled");
                    return this;
                },
                fixShowing: function() {
                    var parent = th.parentNode;
                    var shown = false;
                    for(var i in parent.childNodes) {
                        if(parent.childNodes.hasOwnProperty(i)) {
                            if(!parent.childNodes[i].classList.contains("hidden")) shown = true;
                        }
                    }
                    if(shown) parent.show();
                    else parent.hide();
                },
                increaseBadge: function() {
                    var val = parseInt(this.badge.innerHTML || "0");
                    val ++;
                    this.badge.innerHTML = val;
                    this.showBadge();
                },
                showBadge: function() {
                    this.badge.show();
                },
                hideBadge: function() {
                    this.badge.hide();
                    this.badge.innerHTML = "0";
                },

            }, sections[section]);

            if(icon) {
                if(icon.constructor === String) {
                    u.create(HTML.DIV, { className:"drawer-menu-item-icon notranslate", innerHTML: icon }, th);
                } else {
                    th.appendChild(icon);
                }
            }
            if(callback) {
                u.create(HTML.DIV, {
                    className: "drawer-menu-item-label",
                    innerHTML: name
                }, th);
            }
            th.badge = u.create(HTML.DIV, { className:"drawer-menu-item-badge hidden", innerHTML: "0" }, th);
            sections[section].show();
            return th;
        }

        layout.toggleCollapse = function(force) {
            collapsed = !collapsed;
            if(force != undefined) collapsed = force;
            u.save("drawer:collapsed", collapsed);
            layout.toggleButton.innerHTML = collapsed ? "last_page" : "first_page";
            layout.classList[collapsed ? "add" : "remove"]("drawer-collapsed");
            main.fire(EVENTS.CAMERA_UPDATE);
        }

        layout.footer = u.create(HTML.DIV, { className:"drawer-footer"}, layout);

        footerButtonCollapseDiv = u.create(HTML.PATH, footerButtonCollapsePath);
        footerButtonExpandDiv = u.create(HTML.PATH, footerButtonExpandPath);
//        footerButton = u.create(HTML.SVG, footerButtonSvg, layout.footer);

        layout.toggleButton = u.create(HTML.DIV, {className: "drawer-menu-item-icon drawer-footer-button notranslate", innerHTML: collapsed ? "last_page" : "first_page", onclick: function(e){
            layout.toggleCollapse();
        }}, layout.footer);
        u.create(HTML.DIV, main.appName + " &copy;2017 WTG", layout.footer);
        u.create(HTML.DIV, "Build " + data.version, layout.footer);

        return layout;
    }

    var onEvent = function(EVENT,object){
        switch (EVENT){
            case EVENTS.UPDATE_ADDRESS:
                if(main.users.getCountSelected() == 1 && this.properties.selected) {
                    subtitle.innerHTML = object;
                    subtitle.show();
                } else {
                    subtitle.hide();
                }
                break;
            case EVENTS.TRACKING_ACTIVE:
                title.innerHTML = main.appName;
                headerTitle.innerHTML = main.appName;
                break;
            case EVENTS.TRACKING_DISABLED:
                title.innerHTML = main.appName;
                headerTitle.innerHTML = main.appName;
                window.removeEventListener("popstate", backButtonAction);
                break;
            case EVENTS.TRACKING_CONNECTING:
            case EVENTS.TRACKING_RECONNECTING:
                u.lang.updateNode(title, u.lang.connecting);
                u.lang.updateNode(headerTitle, u.lang.connecting);
                window.addEventListener("popstate", backButtonAction);
                break;
            case EVENTS.CHANGE_NAME:
            case USER.JOINED:
                if(main.me.properties && main.me.properties.getDisplayName) {
                    headerName.innerHTML = main.me.properties.getDisplayName();
                }
                break;
            case EVENTS.SELECT_USER:
            case EVENTS.SELECT_SINGLE_USER:
                onChangeLocation.call(this, this.location)
                break;
        }
        return true;
    };

    function createView(user) {
        return {};
    }

    function onChangeLocation(location) {
        if(this && this.properties && this.properties.selected && main.users.getCountSelected() == 1) {
            subtitle.show();
            this.fire(EVENTS.UPDATE_ACTIONBAR_SUBTITLE, subtitle);
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
                            id:"drawer:collapsed",
                            itemClassName: "media-hidden",
                            type: HTML.CHECKBOX,
                            label: u.lang.collapsed_drawer,
                            checked: u.load("drawer:collapsed"),
                            onaccept: function(e, event) {
                                drawer.toggleCollapse(this.checked);
                            },
                        }
                    ]
                }
            ]
        }
    }


    return {
        type:"drawer",
        start:start,
        onEvent:onEvent,
        createView:createView,
        onChangeLocation:onChangeLocation,
        options:options,
    }
}

