/**
 * Created 2/10/17.
 */
function PropertiesHolder(main) {

    var type = "properties";

    function start() {
        // console.log("PROPERTIESHOLDER",this);
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.SELECT_USER:
                this.properties.selected = true;
                break;
            case EVENTS.UNSELECT_USER:
                this.properties.selected = false;
                break;
            case EVENTS.SELECT_SINGLE_USER:
                main.users.forAllUsers(function(number,user){
                    user.properties.selected = false;
                });
                var myUser = this;
                myUser.fire(EVENTS.SELECT_USER);
                main.users.forAllUsers(function(number,user){
                    if(myUser != user) {
                        user.fire(EVENTS.UNSELECT_USER);
                    }
                });
                break;
            case EVENTS.CHANGE_NAME:
                if(this.properties)this.properties.name = object;
                break;
            case EVENTS.CHANGE_NUMBER:
                if(this.properties)this.properties.number = object;
                break;
            case EVENTS.CHANGE_COLOR:
                if(this.properties)this.properties.color = object;
                break;
            case EVENTS.MAKE_ACTIVE:
                if(this.properties)this.properties.active = true;
                break;
            case EVENTS.MAKE_INACTIVE:
                if(this.properties)this.properties.active = false;
                break;
            default:
                break;
        }
        return true;
    }

    function createView(myUser) {
        var view = {
            user: myUser,
            color: myUser.color,
            name: myUser.name,
            number: myUser.number,
            active: myUser.active,
            selected: myUser.selected,
            getDisplayName: getDisplayName.bind(myUser),
        };
        delete myUser.color;
        delete myUser.name;
        delete myUser.active;
        delete myUser.selected;
        myUser.properties = view;
        return view;
    }

    function getDisplayName(){
        var name = this.properties.name;
        if(!name){
            if(this.number == main.me.number) {
                name = "Me";
            } else {
                name = "Friend "+this.number;
            }
        }
        return name;
    }

    return {
        type:type,
        start:start,
        dependsOnEvent:true,
        onEvent:onEvent,
        dependsOnUser:true,
        createView:createView,
        saveable:true,
    }
}