/**
 * Created 1/19/17.
 */
function Admin() {

    var alertArea;
    var tokens;
    var ipToUser;
    var ipToToken;
    var ipToCheck;
    var user;
    var firebaseToken;

    var u = new Utils();

    var startSummary = function() {

        renderInterface();
        alertArea.hide();

        messaging.getToken().then(function(currentToken) {
            if (currentToken) {
                firebaseToken = currentToken;
                console.log(currentToken);
                connectWss();
            } else {

                messaging.requestPermission()
                    .then(() => {
                        startSummary();
                        console.log('Notification permission granted.')
                })
                .catch((err) => {
                    startSummary();
                    console.log('Unable to get permission to notify. ', err)
                });
            }
          })
          .catch(function(err) {
            console.log('An error occurred while retrieving token. ', err);
                alertArea.show("Data will not be updated. For instant updating you must allow notifications for this page.")
          });
    }

    var renderInterface = function() {

        u.clear($("body")[0]);

        $("body").append(renderAlertArea());

        $("body").append(renderInterfaceTokensHeader());

        var tt = u.create("div", {
            className: "two_tables",
        }, $("body")[0]);

        tt.appendChild(renderInterfaceIpToUserHeader());
        tt.appendChild(renderInterfaceIpToTokenHeader());

        $("body").append(renderInterfaceIpToCheckHeader());


        renderInterfaceTokens();
        renderInterfaceIpToUser();
        renderInterfaceIpToToken();
        renderInterfaceIpToCheck();
    }

    var renderAlertArea = function() {
        alertArea = u.create("table", { style: "width:100%; background-color: red; color: white;" });
        alertArea.content = u.create("td", {}, u.create("tr", {}, alertArea));
        alertArea.show = function(text) {
            alertArea.content.innerHTML = text;
            alertArea.style.display = "";
        }
        alertArea.hide = function() {
            alertArea.style.display = "none";
        }
        return alertArea;
    }


    var renderInterfaceTokensHeader = function () {

       var div = u.create("div");
        u.create("h1", { innerHTML: "Tokens" }, div);

        var table = u.create("table", {id: "tokens"}, div);

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

        var trhead = u.create("tr",{},thead);

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

        tokens = u.create("tbody", {}, table);

        return div;

    }

   var renderInterfaceIpToSome = function(node,some,columnCounter) {
        u.clear(node)
        if(some && some.length > 0) {
            for(var i in some) {
                var tr = u.create("tr", {}, node);
                for(var j in some[i]) {
                    u.create("td", { innerHTML: some[i][j] }, tr);
                }
            }
        } else {
            console.log("node");
            u.create("td", {
                colspan: columnCounter,
                align: "center",
                innerHTML: "No data"
            }, u.create("tr", {}, node));
        }
    }

    var renderInterfaceTokens = function() {
        u.clear(tokens);

        if(data && data.tokens && data.tokens.length > 0) {
            for(var i in data.tokens) {
                var users = data.tokens[i].users;

                var tr = u.create("tr", {}, tokens);

                u.create("a", { innerHTML: data.tokens[i].id, href: "/track/" + data.tokens[i].id, target: "_blank" }, u.create("td", { rowspan: users.length }, tr));
                u.create("td", { rowspan: users.length, innerHTML: data.tokens[i].owner }, tr);
                u.create("td", { rowspan: users.length, innerHTML: data.tokens[i].created }, tr);
                u.create("td", { rowspan: users.length, innerHTML: data.tokens[i].changed }, tr);

                var indent = 0;

                for(var j in users) {
                    if(indent > 0) tr = u.create("tr", {}, tokens);
                    u.create("td", { innerHTML: users[j].number }, tr);

                    u.create("a", { innerHTML: users[j].model, href: "/?list=user&token=" + data.tokens[i].id + "&number=" + j }, u.create("td", {}, tr));

                    u.create("td", { innerHTML: users[j].address }, tr);
                    u.create("td", { innerHTML: users[j].created }, tr);
                    u.create("td", { innerHTML: users[j].changed }, tr);
                    u.create("td", { innerHTML: users[j].control }, tr);
                    u.create("td", { innerHTML: "a" }, tr);
                    u.create("a", { innerHTML: "Del", href: "/?action=del&token=" + data.tokens[i].id + "&number=" + j }, u.create("td", {}, tr));

                    indent ++;
                }
            }
        } else {
            u.create("td", {
                colspan: 12,
                align: "center",
                innerHTML: "No data"
            }, u.create("tr", {}, tokens));
        }

    }

    var renderInterfaceIpToUserHeader = function () {

        var div = u.create("div", {className: "table_ip_to_user"});
        u.create("h1", { innerHTML: "IP to User corresponds" }, div);
        var table = u.create("table", {}, div);

        var thead = u.create("thead", {}, table);
        var trhead = u.create("tr",{},thead);

        u.create("th",{
            innerHTML: "IP"
        }, trhead);
        u.create("th",{
            innerHTML: "Device ID"
        }, trhead);

        table.appendChild(thead);

        ipToUser = u.create("tbody", {}, table);

        return div;

    }

    var renderInterfaceIpToUser = function () {
        renderInterfaceIpToSome(ipToUser, data.ipToUser, 2);
    }

    var renderInterfaceIpToTokenHeader = function () {

       var div = u.create("div", {className: "table_ip_to_token"});
        u.create("h1", { innerHTML: "IP to Token corresponds" }, div);
        var table = u.create("table", {}, div);

        var thead = u.create("thead", {}, table);
        var trhead = u.create("tr",{},thead);

        u.create("th",{
            innerHTML: "IP"
        }, trhead);
        u.create("th",{
            innerHTML: "Token ID"
        }, trhead);

        table.appendChild(thead);

        ipToToken = u.create("tbody", {}, table);

        return div;

    }

    var renderInterfaceIpToToken = function () {
        renderInterfaceIpToSome(ipToToken, data.ipToToken, 2);
    }

    var renderInterfaceIpToCheckHeader = function () {

       var div = u.create("div");
        u.create("h1", { innerHTML: "Checks" }, div);
        var table = u.create("table", {}, div);

        var thead = u.create("thead", {}, table);
        var trhead = u.create("tr",{},thead);

        u.create("th",{
            innerHTML: "IP"
        }, trhead);
        u.create("th",{
            innerHTML: "Token"
        }, trhead);
        u.create("th",{
            innerHTML: "Control"
        }, trhead);
        u.create("th",{
            innerHTML: "Timestamp"
        }, trhead);

        table.appendChild(thead);

        ipToCheck = u.create("tbody", {}, table);

        return div;

    }

    var renderInterfaceIpToCheck = function () {
        renderInterfaceIpToSome(ipToCheck, data.ipToCheck, 4);
    }

    var connectWss = function () {
        socket = new WebSocket(data.general.uri);

        socket.onmessage = function(event) {
            console.log("OPEN",event);

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
        startSummary: startSummary,
        tokens: tokens,

    }
}
document.addEventListener("DOMContentLoaded", (new Admin()).startSummary);