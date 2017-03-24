/**
 * Created 2/11/17.
 */
EVENTS.HIDE_MENU_SUBTITLE = "hide_menu_subtitle";
EVENTS.SHOW_MENU_SUBTITLE = "show_menu_subtitle";

MENU = {
    SECTION_PRIMARY: 0,
    SECTION_COMMUNICATION: 2,
    SECTION_VIEWS: 3,
    SECTION_NAVIGATION: 4,
    SECTION_EDIT: 5,
    SECTION_MAP: 8,
    SECTION_LAST: 9
}

function ButtonHolder(main) {

    var type = "button";
    var buttons;
    var contextMenu;
    var sections;
    var contextMenuLayout;
    var delayDismiss;
    var startTime;

    function start() {
        // console.log("BUTTONHOLDER",this);
        buttons = u.dialog({
            id: "button",
            title: {
                label: "Users",
                className: "user-buttons-title",
                button: {
                    icon: "view_headline",
                    className: "user-buttons-title-button",
                    onclick: function() {
                        var mininized = u.load("button:minimized");
                        u.save("button:minimized", !mininized);
                        main.users.forAllUsers(function(number,user){
                            user.views.button.subtitle.classList[mininized ? "remove" : "add"]("hidden");
                        });
                    }
                }
            },
            className: "user-buttons",
            tabindex: 1,
            resizeable: true,
            items: [],
            itemsClassName: "user-buttons-items",
        });

//        buttons = u.create(HTML.DIV, {className:"user-buttons shadow hidden"}, main.right);
        contextMenuLayout = u.create(HTML.DIV, {className:"user-context-menu shadow hidden", tabindex: 2, onblur: function(){
                contextMenuLayout.classList.add("hidden");
            }, onmouseleave: function(){
                contextMenuLayout.classList.add("hidden");
            }, onmouseenter: function(){
                clearTimeout(delayDismiss);
            }
        }, main.right);
        contextMenu = new ContextMenu();
    }

    function onEvent(EVENT,object){
        // console.log(EVENT)
        switch (EVENT){
            case EVENTS.CREATE_CONTEXT_MENU:
                /*var user = this;
                if(user.number == main.me.number) {
                    var itemMinimize = contextMenu.add(MENU.SECTION_LAST, type + "_1", "Minimize menu", "view_headline", function () {
                        u.save("button:minimized", true);
                        main.users.forAllUsers(function(number,user){
                            user.views.button.subtitle.classList.add("hidden");
                            itemMinimize.classList.add("hidden");
                            itemMaximize.classList.remove("hidden");
                        });
                    });
                    itemMinimize.classList.add("hideable");

                    var itemMaximize = contextMenu.add(MENU.SECTION_LAST, type + "_1", "Restore menu", "view_stream", function () {
                        u.save("button:minimized");
                        main.users.forAllUsers(function(number,user){
                            user.views.button.subtitle.classList.remove("hidden");
                            itemMinimize.classList.remove("hidden");
                            itemMaximize.classList.add("hidden");
                        });
                    });
                    itemMaximize.classList.add("hideable");

                    itemMaximize.classList.add("hideable");
                    if(u.load("button:minimized")) {
                        itemMinimize.classList.add("hidden");
                    } else {
                        itemMaximize.classList.add("hidden");
                    }
                }*/
                break;
            case EVENTS.TRACKING_ACTIVE:
                buttons.open();
                break;
            case EVENTS.TRACKING_DISABLED:
                buttons.close();
                break;
            case EVENTS.SELECT_USER:
                this.views.button.button.classList.add("user-button-selected");
                break;
            case EVENTS.UNSELECT_USER:
                this.views.button.button.classList.remove("user-button-selected");
                break;
            case EVENTS.CHANGE_NAME:
/*
                var name;
                if(object){
                    name = object;
                } else {
                    if(this.number == main.me.number) {
                        name = "Me";
                    } else {
                        name = "Friend "+this.number;
                    }
                }
*/
                this.views.button.title.innerHTML = this.properties.getDisplayName();
                break;
            case EVENTS.CHANGE_NUMBER:
                this.views.button.button.dataset.number = parseInt(object);
                break;
            case EVENTS.MAKE_ACTIVE:
                if(this.views && this.views.button && this.views.button.button && this.views.button.button.classList) this.views.button.button.classList.remove("hidden");
                buttons.titleLayout.innerHTML = "Users (" + main.users.getCountActive() +")";
                if(main.users.getCountActive() > 1) {
                    buttons.open();
                } else if(!main.tracking || main.tracking.getStatus() == EVENTS.TRACKING_DISABLED) {
                    buttons.close();
                }
                break;
            case EVENTS.MAKE_INACTIVE:
                if(this.views && this.views.button && this.views.button.button && this.views.button.button.classList) this.views.button.button.classList.add("hidden");
                buttons.titleLayout.innerHTML = "Users (" + main.users.getCountActive() +")";
                if(main.users.getCountActive() < 2 && (!main.tracking || main.tracking.getStatus() == EVENTS.TRACKING_DISABLED)) {
                    buttons.close();
                }
                break;
            case EVENTS.UPDATE_ADDRESS:
                var subtitle = this.views.button.subtitle;
                if(object) {
                    subtitle.innerHTML = object;
                    if(!u.load("button:minimized")) {
                        subtitle.classList.remove("hidden");
                    }
                } else {
                    subtitle.classList.add("hidden");
                }
                break;
            case EVENTS.SHOW_BADGE:
                if(object == EVENTS.INCREASE_BADGE) {
                    var value = parseInt(this.views.button.badge.innerHTML);
                    value = value || 0;
                    this.views.button.badge.innerHTML = ""+(++value);
                    this.views.button.badge.scrollIntoView();
                } else {
                    this.views.button.badge.innerHTML = object || "";
                }
                if(this.views.button.badge.innerHTML) {
                    this.views.button.badge.classList.remove("hidden");
                }
                break;
            case EVENTS.HIDE_BADGE:
                this.views.button.badge.classList.add("hidden");
                this.views.button.badge.innerHTML = "";
                break;
            case EVENTS.MOUSE_OVER:
                this.views.button.button.classList.add("user-button-hover");
                break;
            case EVENTS.MOUSE_OUT:
                this.views.button.button.classList.remove("user-button-hover");
                break;
            case EVENTS.CHANGE_COLOR:
                if(!object && object.constructor === String) {
                    var color = object || "#0000FF";
                    color = color.replace("#","").split("");
                    var r = parseInt(color[0]+color[1],16);
                    var g = parseInt(color[2]+color[3],16);
                    var b = parseInt(color[4]+color[5],16);
                    color = "rgba("+r+", "+g+", "+b+", 0.4)";
                    this.views.button.button.style.backgroundColor = color;
                } else if(object && object.constructor === Number) {
                    console.log("TODO NUMERIC")
                }
                break;
            default:
                break;
        }
        return true;
    }

    var clicked = false;
    function createView(user){

//    if(buttons.itemsLayout.children.length ==1 && user != main.me){
//    debugger;
//    }

        if(!user || !user.properties) return;
        var color = user.color || user.properties.color || "#0000FF";
        color = color.replace("#","").split("");
        var r = parseInt(color[0]+color[1],16);
        var g = parseInt(color[2]+color[3],16);
        var b = parseInt(color[4]+color[5],16);
        color = "rgba("+r+", "+g+", "+b+", 0.4)";

        var task;
        var onlyTouch,clicked;
        var b = u.create(HTML.DIV, {
            className:"user-button" +(user.properties.active ? "" : " hidden"),
            dataNumber:user.number,
            style:{backgroundColor:color},
            onmousedown: function(){
                onlyTouch = true;
                startTime = new Date().getTime();
                task = setTimeout(function(){
                    openContextMenu(user);
                }, 500);
                // console.log(user);
            },
            onmousemove: function(){
                onlyTouch = false;
            },
            onmouseup: function(){
                if(!onlyTouch) return;
                var delay = new Date().getTime() - startTime;
                if(delay < 500) {
                    if(clicked) {
                        user.fire(EVENTS.CAMERA_ZOOM);
                        contextMenuLayout.classList.add("hidden");
                        clicked = false;
                    } else {
                        user.fire(EVENTS.SELECT_SINGLE_USER);
                        openContextMenu(user);
                        clicked = true;
                        setTimeout(function(){
                            clicked = false;
                        }, 500);
                    }
                }
                clearTimeout(task);
            },
            onmouseenter: function(e) {
                user.fire(EVENTS.MOUSE_OVER,e);
            },
            onmouseleave: function(e) {
                user.fire(EVENTS.MOUSE_OUT,e);
            },
            show: function() {
                this.classList.remove("hidden");
            },
            hide: function() {
                this.classList.add("hidden");
            },
        });
        var icon = (user && user.origin && user.origin.buttonIcon) || "person";
        u.create(HTML.DIV, {className:"user-button-icon", innerHTML:icon}, b);
        var badge = u.create(HTML.DIV, {className:"user-button-badge hidden"}, b);
//        console.log(user)
        var div = u.create(HTML.DIV, {className:"user-button-label"}, b);
        var title = u.create(HTML.DIV, {className:"user-button-title",innerHTML:user.properties.getDisplayName()}, div);
        var subtitle = u.create(HTML.DIV, {className:"user-button-subtitle hidden",innerHTML:""}, div);

        buttons.titleLayout.innerHTML = "Users (" + main.users.getCountActive() +")";

        var added = false;
        for(var i =0; i < buttons.itemsLayout.children.length; i++) {
            var number = parseInt(buttons.itemsLayout.children[i].dataset.number);
            if(number != main.me.number && number >= user.number) {
                buttons.itemsLayout.insertBefore(b, buttons.itemsLayout.children[i]);
                added = true;
                break;
            }
        }
        if(!added) {
            buttons.itemsLayout.appendChild(b);
        }

        return {
            button: b,
            title: title,
            subtitle: subtitle,
            badge:badge,
        };
    }

    function removeView(user){
        user.views.button.button.classList.add("hidden");
    }

    function openContextMenu(user) {
        // console.log(user);
        u.clear(contextMenuLayout);
        sections = [];
        for(var i = 0; i < 10; i ++) {
            sections[i] = u.create(HTML.DIV, {className:"user-context-menu-section hidden"}, contextMenuLayout);
        }
        user.fire(EVENTS.CREATE_CONTEXT_MENU, contextMenu);

        setTimeout(function(){
            var size = user.views.button.button.getBoundingClientRect();
            contextMenuLayout.classList.remove("hidden");
            contextMenuLayout.style.top = Math.floor(size.top) + "px";
            if(size.left - main.right.offsetLeft - contextMenuLayout.offsetWidth -10 > 0) {
                contextMenuLayout.style.left = Math.floor(size.left - contextMenuLayout.offsetWidth -10) + "px";
    //            contextMenuLayout.style.right = Math.floor(document.body.offsetWidth - size.left + 10) + "px";
            } else {
                contextMenuLayout.style.left = Math.floor(size.right + 10) + "px";
            }
//            contextMenuLayout.style.bottom = "auto";
            if(main.right.offsetTop + main.right.offsetHeight < contextMenuLayout.offsetTop + contextMenuLayout.offsetHeight) {
//                contextMenuLayout.style.top = "auto";
                contextMenuLayout.style.top = (main.right.offsetTop + main.right.offsetHeight - contextMenuLayout.offsetHeight - 5) + "px";

            }

            clearTimeout(delayDismiss);
            delayDismiss = setTimeout(function(){
                contextMenuLayout.classList.add("hidden");
            },2000);
        },0);
    }

    function ContextMenu() {

        function add(section,id,name,icon,callback) {
            var th = u.create(HTML.DIV, {
                className:"user-context-menu-item",
                onclick: function() {
                    setTimeout(function(){
                        contextMenuLayout.classList.add("hidden");
                        contextMenuLayout.blur();
                        callback();
                    }, 0);
                },
                show: function() {
                    this.classList.remove("hidden");
                    return this;
                },
                hide: function() {
                    this.classList.add("hidden");
                    return this;
                }
            }, sections[section]);
            if(icon) {
                if(icon.constructor === String) {
                    u.create(HTML.DIV, { className:"user-context-menu-item-icon", innerHTML: icon }, th);
                } else {
                    th.appendChild(icon);
                }
            }
            u.create(HTML.DIV, { className:"user-context-menu-item-title", innerHTML: name}, th);
            sections[section].classList.remove("hidden");
            return th;
        }
        function getContextMenu(){
            console.log("GETCONTEXTMENU:",items);
        }

        return {
            add:add,
            getContextMenu:getContextMenu,
        }
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
        createView:createView,
        removeView:removeView,
    }
}