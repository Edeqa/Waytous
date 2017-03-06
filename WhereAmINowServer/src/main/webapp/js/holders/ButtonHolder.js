/**
 * Created 2/11/17.
 */
function ButtonHolder(main) {

    var type = "button";
    var buttons;
    var contextMenu;
    var sections;
    var contextMenuLayout;
    var delayDismiss;

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
                this.views.button.button.children[1].innerHTML = this.properties.getDisplayName();
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
                    subtitle.classList.remove("hidden");
                } else {
                    subtitle.classList.add("hidden");
                }
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

        var b = u.create(HTML.DIV, {className:"user-button" +(user.properties.active ? "" : " hidden"), style:{backgroundColor:color}, onclick: function(){
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
            // console.log(user);
        }}, buttons);
        u.create(HTML.I, {className:"material-icons", innerHTML:"person"}, b);
//        console.log(user)
        var div = u.create(HTML.DIV, {className:"user-button-label"}, b);
        u.create(HTML.DIV, {className:"user-button-title",innerHTML:user.properties.getDisplayName()}, div);
        var subtitle = u.create(HTML.DIV, {className:"user-button-subtitle hidden",innerHTML:""}, div);

        return {
            button: b,
            subtitle: subtitle
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