/**
 * Created 2/8/17.
 */
function DrawerHolder(main) {

    const CREATE_DRAWER = "create_drawer";


    var drawer = new Drawer();

    var drawerLayout = u.create("div", {className:"drawer", tabindex: 1, onblur: function(){
        drawerLayout.classList.remove("drawer-open");
        return true;
    }}, main.layout, "first");

    var actionbar = u.create("div", {className:"actionbar"}, main.right);
    u.create("span", {innerHTML:"menu", className:"drawer-button", onclick: function(){
        try {
            drawerLayout.classList.add("drawer-open");
            drawerLayout.focus();
        } catch(e) {
            console.err(e);
        }
    }}, actionbar);
    u.create("div", {className:"title", innerHTML:"Waytogo"}, actionbar);

    u.create("a", { href: "/", className:"drawer-header" }, drawerLayout);

    var menu = u.create("div", {className:"menu"}, drawerLayout);
    var sections = [];
    for(var i=0;i<10;i++){
        sections[i] = u.create("div", {className:"hidden" + (i==9 ? "" : " divider")}, menu);
    }


    function start() {

        main.fire(CREATE_DRAWER, drawer);

        drawer.add(9,EVENTS.TRACKING_STOP,"Exit group","clear",function(){
            main.fire(EVENTS.TRACKING_STOP);
        });

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

        var th = u.create("div", { className:"drawer-footer"}, drawerLayout);
        u.create("div", "Waytogo", th);
        u.create("div", "&copy; 2017, White Tiger Group", th);
        u.create("div", "Build " + data.version, th);

    }

    function Drawer() {
        var items = {};

        function add(section,id,name,icon,callback) {
            items[id] = {
                name:name,
                icon:icon,
                callback:callback
            };
            var th = u.create("div", {className:"menu-item"}, sections[section]);
            u.create("i", { className:"material-icons md-14", innerHTML: icon }, th);
            u.create("div", { onclick: function() {
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

    function onEvent(EVENT,object){
        switch (EVENT){

        }
        return true;
    }

    return {
        type:"drawer",
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
    }
}

