/**
 * Created 2/11/17.
 */
EVENTS.SHOW_BADGE = "show_badge";
EVENTS.HIDE_BADGE = "hide_badge";
EVENTS.INCREASE_BADGE = "increase_badge";
EVENTS.HIDE_MENU_SUBTITLE = "hide_menu_subtitle";
EVENTS.SHOW_MENU_SUBTITLE = "show_menu_subtitle";

MENU = {
    SECTION_PRIMARY: 0,
    SECTION_COMMUNICATION: 2,
    SECTION_VIEWS: 3,
    SECTION_NAVIGATION: 4,
    SECTION_MAP: 8,
    SECTION_EXIT: 9
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
        buttons = u.create(HTML.DIV, {className:"user-buttons shadow hidden"}, main.right);
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
                var user = this;
                if(user.number == main.me.number) {
                    var itemMinimize = contextMenu.add(MENU.SECTION_VIEWS, type + "_1", "Minimize menu", "view_headline", function () {
                        u.save("button:minimized", true);
                        main.users.forAllUsers(function(number,user){
                            user.views.button.subtitle.classList.add("hidden");
                            itemMinimize.classList.add("hidden");
                            itemMaximize.classList.remove("hidden");
                        });
                    });
                    itemMinimize.classList.add("hideable");

                    var itemMaximize = contextMenu.add(MENU.SECTION_VIEWS, type + "_1", "Restore menu", "view_stream", function () {
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
                }
                break;
            case EVENTS.TRACKING_ACTIVE:
                buttons.classList.remove("hidden");
                break;
            case EVENTS.TRACKING_DISABLED:
                buttons.classList.add("hidden");
                break;
            case EVENTS.SELECT_USER:
                this.views.button.button.classList.add("selected");
                break;
            case EVENTS.UNSELECT_USER:
                this.views.button.button.classList.remove("selected");
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
            case EVENTS.MAKE_ACTIVE:
                if(this.views && this.views.button && this.views.button.button && this.views.button.button.classList) this.views.button.button.classList.remove("hidden");
                break;
            case EVENTS.MAKE_INACTIVE:
                if(this.views && this.views.button && this.views.button.button && this.views.button.button.classList) this.views.button.button.classList.add("hidden");
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
            default:
                break;
        }
        return true;
    }

    var clicked = false;
    function createView(user){
        if(!user || !user.properties) return;
        var color = user.properties.color || "#0000FF";
        color = color.replace("#","").split("");
        var r = parseInt(color[0]+color[1],16);
        var g = parseInt(color[2]+color[3],16);
        var b = parseInt(color[4]+color[5],16);
        color = "rgba("+r+", "+g+", "+b+", 0.4)";

        var task;
        var b = u.create(HTML.DIV, {className:"user-button" +(user.properties.active ? "" : " hidden"), style:{backgroundColor:color},
            onmousedown: function(){
                startTime = new Date().getTime();
                task = setTimeout(function(){
                    openContextMenu(user);
                }, 500);
                // console.log(user);
            },
            onmouseup: function(){
                var delay = new Date().getTime() - startTime;
                if(delay < 500) {
                    if(clicked) {
                        user.fire(EVENTS.CAMERA_ZOOM);
                        clicked = false;
                    } else {
                        user.fire(EVENTS.SELECT_SINGLE_USER);
                        clicked = true;
                        setTimeout(function(){
                            clicked = false;
                        }, 500);
                        openContextMenu(user);
                    }
                }
                clearTimeout(task);
            }
        }, buttons);
        u.create(HTML.I, {className:"material-icons", innerHTML:"person"}, b);
        var badge = u.create(HTML.DIV, {className:"user-button-badge hidden"}, b);
//        console.log(user)
        var div = u.create(HTML.DIV, {className:"user-button-label"}, b);
        var title = u.create(HTML.DIV, {className:"user-button-title",innerHTML:user.properties.getDisplayName()}, div);
        var subtitle = u.create(HTML.DIV, {className:"user-button-subtitle hidden",innerHTML:""}, div);

        return {
            button: b,
            title: title,
            subtitle: subtitle,
            badge:badge,
        };
    }

    function openContextMenu(user) {
        // console.log(user);
        u.clear(contextMenuLayout);
        sections = [];
        for(var i = 0; i < 10; i ++) {
            sections[i] = u.create(HTML.DIV, {className:"user-context-menu-section hidden"}, contextMenuLayout);
        }
        user.fire(EVENTS.CREATE_CONTEXT_MENU, contextMenu);
        var size = user.views.button.button.getBoundingClientRect();

        contextMenuLayout.style.right = Math.floor(document.body.offsetWidth - size.left + 10) + "px";
        contextMenuLayout.style.top = Math.floor(size.top) + "px";
        contextMenuLayout.classList.remove("hidden");
        clearTimeout(delayDismiss);
        delayDismiss = setTimeout(function(){
            contextMenuLayout.classList.add("hidden");
        },2000);
    }

    function ContextMenu() {

        function add(section,id,name,icon,callback) {
            var th = u.create(HTML.DIV, {className:"user-context-menu-item", onclick: function() {
                setTimeout(function(){
                    contextMenuLayout.focus();
                    contextMenuLayout.blur();
                    callback();
                }, 300);
            }}, sections[section]);
            u.create(HTML.I, { className:"material-icons md-14", innerHTML: icon }, th);
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
        dependsOnEvent:true,
        dependsOnUser:true,
        onEvent:onEvent,
        createView:createView,
    }
}