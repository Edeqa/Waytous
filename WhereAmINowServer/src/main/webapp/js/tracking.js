/**
 * Created 1/19/17.
 */

function Tracking() {

	var socket;

    var start = function() {
        if(window.name == "content") {
            window.parent.history.pushState({}, null, "/admin/summary");
        }

        renderInterface();
        alertArea.hide();

        messaging.getToken().then(function(currentToken) {
            if (currentToken) {
                firebaseToken = currentToken;
                console.log(currentToken);
                connectWss();
            } else {
                messaging.requestPermission().then(function(){
                    start();
                    console.log('Notification permission granted.')
                }).catch(function(err){
                    start();
                    console.log('Unable to get permission to notify. ', err)
                });
            }
        }).catch(function(err) {
            console.log('An error occurred while retrieving token. ', err);
        });
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
        title: "Group",
        menu: true,
    }
}
document.addEventListener("DOMContentLoaded", (new Tracking()).start);