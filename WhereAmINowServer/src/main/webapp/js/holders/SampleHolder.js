/**
 * Created 2/10/17.
 */
EVENT.SAMPLE_EVENT = "sample_event";

function SampleHolder(main) {

    var type = "sample";

    function start() {
        console.log("SAMPLEHOLDER", this);
    }

    function onEvent(EVENT,object){
        // console.log("SAMPLEEVENT",EVENT,object)
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                var menuItem = object.add(DRAWER.SECTION_PRIMARY, EVENT.SAMPLE_EVENT, "Sample item", "ac_unit", function(){
                    console.log("SAMPLEEVENTDRAWERCALLBACK", EVENT);
                });
                menuItem.classList.add("disabled");
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user) {
                    object.add(MENU.SECTION_PRIMARY, EVENT.SAMPLE_EVENT, "Sample menu", "ac_unit", function () {
                        u.save("sample:show:"+user.number, true);
                        console.log("SAMPLEEVENTMENUCALLBACK", user);
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
            show: u.load("sample:user:" + user.number)
        };
        // console.log("SAMPLECREATEVIEW",user);
        return view;
    }

    function onChangeLocation(location) {
        // console.log("SAMPLEONCHANGELOCATION",this,location);
    }

    var help = {
        title: "Sample",
        1: {
            title: "Article 1",
            body: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras pellentesque aliquam tellus, quis finibus odio faucibus sed. Nunc nec dictum ipsum, a efficitur sem. Nullam suscipit quis neque in cursus. Etiam tempus imperdiet scelerisque. Integer ut nisi at est varius rutrum quis eget urna. Morbi blandit vehicula laoreet. Curabitur tincidunt turpis dui, at venenatis risus volutpat et. Donec cursus molestie ligula eu convallis. Curabitur sed quam id ex tristique ultricies. Duis id felis eget massa venenatis vehicula. Aenean eget varius dui. "
        },
        2: {
            title: "Article 2",
            body: "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras pellentesque aliquam tellus, quis finibus odio faucibus sed. Nunc nec dictum ipsum, a efficitur sem. Nullam suscipit quis neque in cursus. Etiam tempus imperdiet scelerisque. Integer ut nisi at est varius rutrum quis eget urna. Morbi blandit vehicula laoreet. Curabitur tincidunt turpis dui, at venenatis risus volutpat et. Donec cursus molestie ligula eu convallis. Curabitur sed quam id ex tristique ultricies. Duis id felis eget massa venenatis vehicula. Aenean eget varius dui. "
        }
    };

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
        dependsOnUser:true,
        createView:createView,
        onChangeLocation:onChangeLocation,
        help:help,
    }
}