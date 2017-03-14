/**
 * Created 2/8/17.
 */
DRAWER = {
    SECTION_PRIMARY: 0,
    SECTION_COMMUNICATION: 2,
    SECTION_VIEWS: 3,
    SECTION_NAVIGATION: 4,
    SECTION_MAP: 8,
    SECTION_LAST: 9
};

function DrawerHolder(main) {


    var drawer;
    var headerName;
    var sections;
    var title;
    var headerTitle;
    var subtitle;
    var drawerLayout;

    var target = window; // this can be any scrollable element
    var last_y = 0;

    var start = function() {

        drawer = new Drawer();
        target.addEventListener("touchmove", preventPullToRefresh);

        drawerLayout = u.create(HTML.DIV, {className:"drawer", tabindex: 1, onblur: function(){
            drawerLayout.classList.remove("drawer-open");
            return true;
        }}, main.layout, "first");

        var actionbar = u.create(HTML.DIV, {className:"actionbar"}, main.right);
        u.create(HTML.SPAN, {innerHTML:"menu", className:"actionbar-button", onclick: function(){
            try {
                drawerLayout.classList.add("drawer-open");
                drawerLayout.focus();
            } catch(e) {
                console.err(e);
            }
        }}, actionbar);
        var label = u.create(HTML.DIV, {className:"actionbar-label"}, actionbar);
        title = u.create(HTML.DIV, {className:"actionbar-label-title", innerHTML:"Waytogo"}, label);
        subtitle = u.create(HTML.DIV, {className:"actionbar-label-subtitle hidden"}, label);

        var header = u.create(HTML.DIV, { className:"drawer-header" }, drawerLayout);
        headerName = u.create(HTML.DIV, {className:"drawer-header-name"}, header);
        headerTitle = u.create(HTML.DIV, {className:"drawer-header-title", innerHTML:"Waytogo"}, header);
        u.create(HTML.DIV, {className:"drawer-header-subtitle", innerHTML:"Be always on the same way\nwith your friends"}, header);

        var menu = u.create(HTML.DIV, {className:"menu"}, drawerLayout);
        sections = [];
        for(var i=0;i<10;i++){
            sections[i] = u.create(HTML.DIV, {className:"hidden" + (i==9 ? "" : " divider")}, menu);
        }

        main.fire(EVENTS.CREATE_DRAWER, drawer);

        /*for(var i in holderFiles) {
            var x = holderFiles[i].toLowerCase();
            if(holders[x].menu) {
                var th = u.create("div", {className:"menu-item"}, menu);
                u.create("i", { className:"material-icons md-14", innerHTML: holders[x].icon }, th);
                u.create("div", { dataStart: x, onclick: function(){
                    menu.blur();
                    holders[this.dataset.start].start();
                    return false;
                }, innerHTML: holders[x].menu}, th);
            }
        }*/

        /*var th = u.create("div", {className:"menu-item"}, menu);
        u.create("i", { className:"material-icons md-14", innerHTML:"exit_to_app" }, th);
        // u.create("div", { onclick: logout, innerHTML: "Log out" }, th);*/

        var th = u.create(HTML.DIV, { className:"drawer-footer"}, drawerLayout);
        u.create(HTML.DIV, "Waytogo", th);
        u.create(HTML.DIV, "&copy;2017 White Tiger Group", th);
        u.create(HTML.DIV, "Build " + data.version, th);

    };

    function Drawer() {
        var items = {};

        function add(section,id,name,icon,callback) {
            items[id] = {
                name:name,
                icon:icon,
                callback:callback
            };
            var th = u.create(HTML.DIV, {className:"menu-item"}, sections[section]);
            if(icon) {
                if(icon.constructor === String) {
                    u.create(HTML.I, { className:"material-icons md-14", innerHTML: icon }, th);
                } else {
                    th.appendChild(icon);
                }
            }
            if(callback) {
                u.create(HTML.DIV, {
                    onclick: function (event) {
                        setTimeout(function () {
                            drawerLayout.blur();
                            callback(event);
                        }, 300);
                    }, innerHTML: name
                }, th);
            }
            sections[section].classList.remove("hidden");
            return th;
        }
        function getDrawer(){
            console.log("GETDRAWER:",items);
        }

        return {
            add:add,
            getDrawer:getDrawer,
        }
    }

    var onEvent = function(EVENT,object){
        switch (EVENT){
            case EVENTS.UPDATE_ADDRESS:
                if(main.users.getCountSelected() == 1 && this.properties.selected) {
                    subtitle.innerHTML = object;
                    subtitle.classList.remove("hidden");
                } else {
                    subtitle.classList.add("hidden");
                }
                break;
            case EVENTS.TRACKING_ACTIVE:
            case EVENTS.TRACKING_DISABLED:
                title.innerHTML = "Waytogo";
                headerTitle.innerHTML = "Waytogo";
                break;
            case EVENTS.TRACKING_CONNECTING:
            case EVENTS.TRACKING_RECONNECTING:
                title.innerHTML = "Connecting...";
                headerTitle.innerHTML = "Connecting...";
                break;
            case EVENTS.CHANGE_NAME:
            case USER.JOINED:
                if(main.me.properties && main.me.properties.getDisplayName) {
                    headerName.innerHTML = main.me.properties.getDisplayName();
                }
                break;
        }
        return true;
    };

    function createView(user) {
        return {};
    }

    function preventPullToRefresh(e){
        var scrolly = target.pageYOffset || target.scrollTop || 0;
        var direction = e.changedTouches[0].pageY > last_y ? 1 : -1;
        if(direction>0 && scrolly===0){
           e.preventDefault();
        }
        last_y = e.changedTouches[0].pageY;
    }

    return {
        type:"drawer",
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
        dependsOnUser:true,
        createView:createView,
    }
}

