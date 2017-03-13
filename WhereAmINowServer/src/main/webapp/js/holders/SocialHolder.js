/**
 * Created 3/10/17.
 */

function SocialHolder(main) {

    var type = "social";

    function start() {
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                // var drawerItem = object.add(DRAWER.SECTION_EXIT,type+"_1");
                //
                // drawerItem.classList.add("menu-item-social");
                //
                //
                //
                // u.create(HTML.DIV, { className: "menu-item-twitter", innerHTML: "twitter" }, drawerItem);

                break;
            default:
                break;
        }
        return true;
    }

    function createView(myUser){
        var view = {};
        view.user = myUser;

        view.show = u.load("track:show:" + myUser.number);

        if(view.show) {
            show.call(myUser);
        }
        drawerPopulate();
        return view;
        // console.log("SAMPLECREATEVIEW",user);
    }


    function onChangeLocation(location) {
        show.call(this);
        // console.log("SAMPLEONCHANGELOCATION",this,location);
    }

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
    }
}