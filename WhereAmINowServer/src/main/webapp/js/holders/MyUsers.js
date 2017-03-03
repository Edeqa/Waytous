/**
 * Created by tujger on 2/12/17.
 */
function MyUsers(main) {

    var users = {};
    var myNumber = 0;

    function addUser (json) {
        var user;// = new MyUser();
        if (!users[json[RESPONSE.NUMBER]]) {
            user = new MyUser(main);
            user.number = json[RESPONSE.NUMBER];
            if (json[USER.COLOR]){
                user.color = u.getHexColor(json[USER.COLOR]);
            }
            if (json[USER.NAME]){
                user.name = json[USER.NAME];
            }
            if (json[USER.PROVIDER]) {
                var location = null;
                user.addLocation(location);
            }
            users[json[RESPONSE.NUMBER]] = user;
            user.fire(EVENTS.CHANGE_NUMBER, json[RESPONSE.NUMBER]);
            user.createViews();
        } else {
            user = users[json[RESPONSE.NUMBER]];
            if (json[USER.COLOR]) user.fire(EVENTS.CHANGE_COLOR, json[USER.COLOR]);
        }
        return user;
    };

    function setMe() {
        delete users[myNumber];
        main.me.number = myNumber;
        main.me.fire(EVENTS.CHANGE_NUMBER, myNumber);
        var name = u.load("properties:name");
        if(name) {
            main.me.name = name;
            main.me.fire(EVENTS.CHANGE_NAME, name);
        }
        users[myNumber] = main.me;
        return main.me;
    }

    function forAllUsers(callback){
        forMe(callback);
        forAllUsersExceptMe(callback);
    }

    function forSelectedUsers(callback){
        for(var i in users){
            if(users[i] && users[i].selected) forUser(i, callback);
        }
    }

    function forMe(callback) {
        forUser(myNumber, callback);
    }

    function forAllUsersExceptMe(callback){
        for(var i in users){
            if(users[i] && users[i] != main.me) forUser(i, callback);
        }
    }

    function forUser(number,callback, arguments){
        if(users[number]) callback(number, users[number], arguments);
    }

    function getCountSelected(){
        var count = 0;
        for(var i in users) {
            if(users[i] && i == users[i].number && users[i].properties && users[i].properties.selected) {
                count ++;
            }
        }
        return count;
    }

    return {
        addUser:addUser,
        setMe:setMe,
        forAllUsers:forAllUsers,
        forSelectedUsers:forSelectedUsers,
        forMe:forMe,
        forAllUsersExceptMe:forAllUsersExceptMe,
        forUser:forUser,
        users:users,
        getCountSelected:getCountSelected,

    }
}
