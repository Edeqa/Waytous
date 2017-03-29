/**
 * Created 2/10/17.
 */
function PropertiesHolder(main) {

    this.type = "properties";
    this.saveable = true;

    var dialog;

    this.start = function() {
        dialog = u.dialog({
            title: u.lang.set_my_name,
            items: [
                { type: HTML.INPUT, label: u.lang.name }
            ],
            positive: {
                label: u.lang.ok,
                onclick: function(args) {
                    if(args[0].value) {
                        var name = args[0].value;
                        u.save("properties:name", name);
                        u.save("properties:name_asked", true);
                        main.me.fire(EVENTS.CHANGE_NAME, name);
                    }
                    console.log(args);
                }
            },
            negative: {
                label: "Cancel"
            }
        });
    };

    this.onEvent = function(EVENT,object){
        var self = this;
        switch (EVENT){
            case EVENTS.TRACKING_ACTIVE:
                if(!u.load("properties:name") && !u.load("properties:name_asked")) {
                    var askIfNameNotDefinedDialog = u.dialog({
                        items: [
                            { type: HTML.DIV, label: u.lang.your_name_is_not_defined }
                        ],
                        positive: {
                            label: u.lang.yes,
                            onclick: function(args) {
                                setMyName.call(main.me);
                            }
                        },
                        negative: {
                            label: u.lang.no,
                            onclick: function(){
                                u.save("properties:name_asked", true);
                            }
                        },
                        neutral: {
                            label: u.lang.maybe_later
                        },
                        timeout: 10000
                    }).open();
                }
                var name = main.me.name;
                if(!name && main.me.properties) name = main.me.properties.name;
                if(name) {
                    main.me.fire(EVENTS.CHANGE_NAME, name);
                }
                break;
            case EVENTS.CREATE_CONTEXT_MENU:
                var user = this;
                if(user.number == main.me.number) {
                    object.add(MENU.SECTION_PRIMARY, self.type + "_set_my_name", u.lang.set_my_name, "face", function () {
                        setMyName.call(user);
                    });
                }
                break;
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
                if(this.number == main.me.number) {
                    main.tracking.put(USER.NAME, object);
                    main.tracking.send(REQUEST.CHANGE_NAME);
                }
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
            case EVENTS.MAP_READY:
                main.me.createViews();
                main.me.fire(EVENTS.SELECT_USER);
                main.me.fire(EVENTS.CHANGE_COLOR, "#0000FF");
                main.me.fire(EVENTS.MAKE_ACTIVE);
                break;
            default:
                break;
        }
        return true;
    };

    this.createView = function(myUser) {
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
    };

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

    function setMyName(name){
        if(dialog) dialog.close();
        dialog.items[0].value = main.me.properties.name || "";
        dialog.open();
    }

}