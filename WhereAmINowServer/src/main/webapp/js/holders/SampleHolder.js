/**
 * Created 2/10/17.
 */
function SampleHolder(main) {

    var type = "sample";

    function start() {
        console.log("SAMPLEHOLDER",this);
    }

    function onEvent(EVENT,object){
        // console.log("SAMPLEEVENT",EVENT,object)
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                var menuItem = object.add(DRAWER.SECTION_PRIMARY,type+"_1","Sample item","ac_unit",function(){console.log("SAMPLEEVENTDRAWERCALLBACK",EVENT)});
                menuItem.classList.add("disabled");
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user) {
                    object.add(MENU.SECTION_PRIMARY, type + "_1", "Sample menu", "ac_unit", function () {
                        u.save("sample:show:"+user.number, true);
                        console.log("SAMPLEEVENTMENUCALLBACK",user);
                    });
                }
                break;
            default:
                break;
        }
        return true;
    }

    function createView(user){
        var view = {
            user: user,
            show: u.load("sample:user:"+user.number)
        };
        return view;
        // console.log("SAMPLECREATEVIEW",user);
    }

    function onChangeLocation(location) {
        // console.log("SAMPLEONCHANGELOCATION",this,location);

    }

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
        dependsOnUser:true,
        createView:createView,
        onChangeLocation:onChangeLocation,
    }
}