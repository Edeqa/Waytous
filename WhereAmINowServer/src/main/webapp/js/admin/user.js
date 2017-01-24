/**
 * Created 1/23/17.
 */
function User() {

    var u = new Utils();
    var positions;

    var start = function() {
        if(!data || !data.user || !data.user.created) {
            window.location.href = "/admin/summary" + (window.name == "content" ? "/set" : "");
        }
        if(window.name == "content") {
            window.parent.history.pushState({}, null, window.location.href.replace("/set",""));
        }

        renderInterface();

        messaging.getToken().then(function(currentToken) {
            if (currentToken) {
                firebaseToken = currentToken;
                console.log(currentToken);
                connectWss();
            } else {
                messaging.requestPermission()
                    .then(function(){
                        start();
                        console.log('Notification permission granted.')
                })
                .catch(function(err){
                    start();
                    console.log('Unable to get permission to notify. ', err)
                });
            }
          })
          .catch(function(err) {
            console.log('An error occurred while retrieving token. ', err);
            window.location.href = "/admin/summary" + (window.name == "content" ? "/set" : "");
          });



    }

    var renderInterface = function() {

        u.clear(document.body);

        u.create("h1", "User", document.body);


        document.body.appendChild(renderInterfaceUserHeader());
        renderInterfaceUser();
    }

    var renderInterfaceUserHeader = function () {

        var div = u.create("div");
        u.create("h2", "Summary", div);

        var table = u.create("table", {className:"user"}, div);

        var tr;
        tr = u.create("tr", null, table);
        u.create("th","IP", tr);
        u.create("td",data.user.ip, tr);

        tr = u.create("tr", null, table);
        u.create("th","Name", tr);
        u.create("td",data.user.name, tr);

        tr = u.create("tr", null, table);
        u.create("th","Device ID", tr);
        u.create("td",data.user.deviceId, tr);

        tr = u.create("tr", null, table);
        u.create("th","Group ID", tr);
        u.create("a", {href:"/admin/group/"+data.user.token, innerHTML: data.user.token}, u.create("td", null, tr));

        tr = u.create("tr", null, table);
        u.create("th","Number in group", tr);
        u.create("td",""+data.user.number, tr);

        tr = u.create("tr", null, table);
        u.create("th","Color", tr);
        u.create("td",data.user.color, tr);

        tr = u.create("tr", null, table);
        u.create("th","Model", tr);
        u.create("td",data.user.model, tr);

        tr = u.create("tr", null, table);
        u.create("th","Created", tr);
        u.create("td",data.user.created, tr);

        tr = u.create("tr", null, table);
        u.create("th","Changed", tr);
        u.create("td",data.user.changed, tr);


        u.create("h2", "Positions", div);

        table = u.create("table", {className:"summary"}, div);

        var thead = u.create("thead", {}, table);
        var tr = u.create("tr", null, thead);
        u.create("th", "#", tr);
        u.create("th", "Time", tr);
        u.create("th", "Latitude", tr);
        u.create("th", "Longitude", tr);
        u.create("th", "Altitude", tr);
        u.create("th", "Accuracy", tr);
        u.create("th", "Bearing", tr);
        u.create("th", "Speed", tr);

        positions = u.create("tbody", {}, table);

        return div;

    }

    var renderInterfaceUser = function() {
        u.clear(positions);

        if(data && data.user && data.user.positions.length > 0) {
            for(var i in data.user.positions) {
                var position = data.user.positions[i];

                var tr = u.create("tr", {}, positions);

                u.create("a", { innerHTML: 1, href: "http://maps.google.com/?q="+position[2]+"+"+position[3]+"&z=13", target: "_blank" }, u.create("td", null, tr));
                u.create("td", position[1], tr);
                u.create("td", position[2], tr);
                u.create("td", position[3], tr);
                u.create("td", position[4], tr);
                u.create("td", position[5], tr);
                u.create("td", position[6], tr);
                u.create("td", position[7], tr);
          }
        } else {
            u.create("td", {
                colspan: 8,
                align: "center",
                innerHTML: "No data"
            }, u.create("tr", {}, positions));
        }
    }

    var connectWss = function () {
        socket = new WebSocket(data.general.uri);

        socket.onmessage = function(event) {
            console.log("MESSAGE",event);

            var incomingMessage = event.data;
            showMessage(incomingMessage);
        };

        socket.onopen = function(event) {
            console.log("OPEN",event);
            var o = { "client":"admin" };
            socket.send(JSON.stringify(o));
        };

        socket.onclose = function(event) {
            console.log("CLOSE",event);
        };

        socket.onerror = function(event) {
            console.log("ERROR",event);
        };

        function showMessage(message) {
            console.log("MESSAGE",event);
          var messageElem = document.createElement('div');
          messageElem.appendChild(document.createTextNode(message));
          document.getElementById('subscribe').appendChild(messageElem);
        }
    };

    return {
        start: start,
        page: "user",
        icon: "navigation",
        title: "User",
        menu: false,
    }
}
document.addEventListener("DOMContentLoaded", (new User()).start);