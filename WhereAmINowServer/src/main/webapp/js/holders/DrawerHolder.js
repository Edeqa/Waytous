/**
 * Created 2/8/17.
 */
function DrawerHolder(main) {

    const CREATE_DRAWER = "create_drawer";

    var drawer;
    var nameInHeader;
    var sections;
    var subtitle;
    var drawerLayout;

    var start = function() {

        drawer = new Drawer();

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
        u.create(HTML.DIV, {className:"actionbar-label-title", innerHTML:"Waytogo"}, label);
        subtitle = u.create(HTML.DIV, {className:"actionbar-label-subtitle hidden"}, label);

        var header = u.create(HTML.DIV, { className:"drawer-header" }, drawerLayout);
        nameInHeader = u.create(HTML.DIV, {className:"drawer-header-name"}, header);
        u.create(HTML.DIV, {className:"drawer-header-title", innerHTML:"Waytogo"}, header);
        u.create(HTML.DIV, {className:"drawer-header-subtitle", innerHTML:"Be always on the same way\nwith your friends"}, header);

        var menu = u.create(HTML.DIV, {className:"menu"}, drawerLayout);
        sections = [];
        for(var i=0;i<10;i++){
            sections[i] = u.create(HTML.DIV, {className:"hidden" + (i==9 ? "" : " divider")}, menu);
        }

        main.fire(CREATE_DRAWER, drawer);

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
            u.create(HTML.I, { className:"material-icons md-14", innerHTML: icon }, th);
            u.create(HTML.DIV, { onclick: function() {
                setTimeout(function(){
                    drawerLayout.blur();
                    callback();
                }, 300);
            }, innerHTML: name}, th);
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
            case EVENTS.CHANGE_NAME:
            case USER.JOINED:
                if(main.me.properties && main.me.properties.getDisplayName) {
                    nameInHeader.innerHTML = main.me.properties.getDisplayName();
                }
                break;
        }
        return true;
    };

    function createView(user) {
        return {};
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

