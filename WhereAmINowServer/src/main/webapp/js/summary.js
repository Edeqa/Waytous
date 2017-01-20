/**
 * Created 1/19/17.
 */
function Admin() {

    var u = new Utils();

    var startSummary = function() {

        renderInterface();
        connectWss();
    }

    var renderInterface = function() {

        var table = u.create("table", {id: "tokens1"});

        var thead = u.create("thead", {}, table);
        var trhead = u.create("tr",{},thead);

        u.create("th",{
            rowspan: 2,
            innerHTML: "Token"
        }, trhead);
        u.create("th",{
            rowspan: 2,
            innerHTML: "Owner"
        }, trhead);
        u.create("th",{
            rowspan: 2,
            innerHTML: "Created"
        }, trhead);
        u.create("th",{
            rowspan: 2,
            innerHTML: "Changed"
        }, trhead);
        u.create("th",{
            colspan: 8,
            innerHTML: "Users"
        }, trhead);

        trhead = u.create("tr",{},thead);

        u.create("th",{
            innerHTML: "#"
        }, trhead);
        u.create("th",{
            innerHTML: "Device"
        }, trhead);
        u.create("th",{
            innerHTML: "Address"
        }, trhead);
        u.create("th",{
            innerHTML: "Created"
        }, trhead);
        u.create("th",{
            innerHTML: "Changed"
        }, trhead);
        u.create("th",{
            innerHTML: "Control"
        }, trhead);
        u.create("th",{
            innerHTML: "Pos"
        }, trhead);
        u.create("th",{
            innerHTML: "X"
        }, trhead);

        table.appendChild(thead);


        $("body").append(table);


    }



    var connectWss = function () {
        var socket = new WebSocket("ws://localhost:8081");

        document.forms.publish.onsubmit = function() {
          var outgoingMessage = this.message.value;

          socket.send(outgoingMessage);
          return false;
        };

        socket.onmessage = function(event) {
          var incomingMessage = event.data;
          showMessage(incomingMessage);
        };

        socket.onopen = function(event) {
            console.log("OPEN",event);
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
        startSummary: startSummary,

    }
}
document.addEventListener("DOMContentLoaded", (new Admin()).startSummary);