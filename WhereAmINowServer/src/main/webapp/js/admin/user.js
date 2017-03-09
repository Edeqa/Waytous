/**
 * Created 1/23/17.
 */
function User() {

    var title = "User";

    var u = new Utils();
    var positions;
    var div;
    var groupId;
    var userNumber;
    var summary;


    /*var start = function() {
        if(!data || !data.user || !data.user.created) {
            window.location.href = "/admin/summary";
            return;
        }
        div = u.createPage(this);

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



    }*/

    var u = new Utils();

    var renderInterface = function() {

//        div.appendChild(renderAlertArea());

        div.appendChild(renderInterfaceHeader());

        updateSummary();

    }

    var renderInterfaceHeader = function () {

        var div = u.create("div", {className:"summary"});

        u.create("h2", "Summary", div);

        var table = u.create("div", {className:"table option"}, div);

        summary = u.create("div", {className:"tbody"}, table);
        u.create("div", {className:"th", align: "center", innerHTML: "Loading..."}, u.create("div", {className:"tr"}, summary));

        /*u.create("h2", "Positions", div);

        var table = u.create("div", {id:"users", className:"summary table"}, div);
        var thead = u.create("div", { className:"thead"}, table);
        trhead = u.create("div", {className:"tr"}, thead);

        u.create("div", {className:"th", innerHTML:"#", width:"5%"}, trhead);
        u.create("div", {className:"th", innerHTML:"Name"}, trhead);
        u.create("div", {className:"th", innerHTML:"Color", width:"5%"}, trhead);
        u.create("div", {className:"th", innerHTML:"Created"}, trhead);
        u.create("div", {className:"th", innerHTML:"Updated"}, trhead);
        u.create("div", {className:"th", innerHTML:"Platform"}, trhead);
        u.create("div", {className:"th", innerHTML:"Device"}, trhead);

        tbody = u.create("tbody", null, table);
        u.create("td", {
            colspan: trhead.childElementCount,
            align: "center",
            innerHTML: "Loading..."
        }, u.create("tr", {}, tbody));
*/

        u.create("br", null, div);
        buttons = u.create("div", {className:"buttons"}, div);
        renderButtons(buttons);

        return div;

    }

    function updateSummary() {
        var ref = database.ref();

        ref.child(groupId).child("u/b").child(userNumber).once("value").then(function(snapshot) {
            if(!snapshot || !snapshot.val()) return;
            u.clear(summary);

            var tr = u.create("div", {className:"tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Number"}, tr);
            u.create("div", {className: "td option", innerHTML:userNumber}, tr);

            tr = u.create("div", {className:"tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Name"}, tr);
            u.create("div", {className: "td option", innerHTML:snapshot.val().name}, tr);

            tr = u.create("div", {className:"tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Active"}, tr);
            var userActiveNode = u.create("div", {className: "td option", innerHTML:"..."}, tr);

            ref.child(groupId).child("u/b").child(userNumber).child("active").on("value", function(snapshot){
                userActiveNode.innerHTML = snapshot.val() ? "Yes" : "No";
                userActiveNode.classList.add("changed");
                setTimeout(function(){userActiveNode.classList.remove("changed")}, 2000);
            });

            tr = u.create("div", {className:"tr clickable", onclick:function(){
                WAIN.switchTo("/admin/group/"+groupId);
                return false;
            }}, summary);
            u.create("div", {className: "th option", innerHTML:"Group"}, tr);
            u.create("div", {className:"td option", innerHTML:groupId}, tr);

            tr = u.create("div", {className:"tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Key"}, tr);
            var userKey = u.create("div", {className: "td option", innerHTML:"..."}, tr);

            tr = u.create("div", {className:"tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Color"}, tr);
            u.create("td", { style: { backgroundColor: u.getHexColor(snapshot.val().color), opacity: 0.5 } }, tr);

            tr = u.create("div", {className:"tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Created"}, tr);
            u.create("div", {className: "td option", innerHTML:snapshot.val().created ? new Date(snapshot.val().created).toLocaleString() : "&#150;"}, tr);

            tr = u.create("div", {className:"tr", style: { display: (snapshot.val().persistent ? "none" : "")}}, summary);
            u.create("div", {className: "th option", innerHTML:"Updated"}, tr);
            var userUpdatedNode = u.create("div", {className: "td option", innerHTML:"..."}, tr);

            ref.child(groupId).child("u/b").child(userNumber).child("changed").on("value", function(snapshot){
                userUpdatedNode.innerHTML = new Date(snapshot.val()).toLocaleString();
                userUpdatedNode.classList.add("changed");
                setTimeout(function(){userUpdatedNode.classList.remove("changed")}, 2000);
            });

            tr = u.create("div", {className:"tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Platform"}, tr);
            var userOs = u.create("div", {className: "td option", innerHTML:"..."}, tr);

            tr = u.create("div", {className:"tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Device"}, tr);
            var userDevice = u.create("div", {className: "td option", innerHTML:"..."}, tr);

            ref.child(groupId).child("u/p").child(userNumber).once("value").then(function(snapshot){
                userOs.innerHTML = snapshot.val().os;
                userDevice.innerHTML = snapshot.val().model;
                userKey.innerHTML = snapshot.val().key;
            });

        }).catch(function(error){
            u.clear(summary);
            u.create("div", {className:"th", align: "center", innerHTML: "Error loading data, try to refresh page."}, u.create("div", {className:"tr"}, summary));
        });

    }

    function updateData(){

        var ref = database.ref();
        u.clear(tbody);

        ref.child(groupId).child("u/b").on("child_added", function(snapshot) {
            if(!snapshot || !snapshot.val()) return;
            var tr = u.create("tr", { className: "changeable " + ((snapshot.val().active ? "" : "inactive"))}, tbody);

            u.create("a", { href:"#", onclick: function(){
                WAIN.switchTo("/admin/user/"+groupId+"/"+snapshot.key);
                return false;
            }, innerHTML:snapshot.key, style:{
                display: "block",
                cursor: "pointer"
            }}, u.create("td", {}, tr));
            u.create("td", snapshot.val().name, tr);
            u.create("td", { style: { backgroundColor: u.getHexColor(snapshot.val().color), opacity: 0.5 } }, tr);
            u.create("td", snapshot.val().created ? new Date(snapshot.val().created).toLocaleString() : "&#150;", tr);
            var userChanged = u.create("td", "...", tr);
            var userOs = u.create("td", "...", tr);
            var userDevice = u.create("td", "...", tr);

            ref.child(groupId).child("u/b").child(snapshot.key).child("changed").on("value", function(snapshot){
                userChanged.innerHTML = new Date(snapshot.val()).toLocaleString();
                tr.classList.add("changed");
                setTimeout(function(){tr.classList.remove("changed")}, 2000);
            });
            ref.child(groupId).child("u/p").child(snapshot.key).once("value").then(function(snapshot){
                userOs.innerHTML = snapshot.val().os;
                userDevice.innerHTML = snapshot.val().model;
            });
        });

//        r.on("value").then(function(snapshot) {
//          snapshot.forEach(function(childSnapshot) {
//            var childKey = childSnapshot.key;
//            var childData = childSnapshot.val();
//            console.log(this,childSnapshot);
//          });
//        });

    }

    function renderButtons(div) {
        u.clear(div);
        u.create("button", { type: "button", innerHTML:"Switch activity", onclick: switchActivity}, div);
        u.create("button", { type: "button", innerHTML:"Remove", onclick: removeUserQuestion}, div);
    }

    function switchActivity(e){
        var ref = database.ref();
        u.clear(buttons);

        u.create("button",{type: "button", innerHTML:"Active", onclick: function(){
            ref.child(groupId).child("u/b").child(userNumber).child("active").set(true);
            renderButtons(buttons);
        }}, buttons);
        u.create("button",{type: "button", innerHTML:"Inactive", onclick: function(){
            ref.child(groupId).child("u/b").child(userNumber).child("active").set(false);
            renderButtons(buttons);
        }}, buttons);
        u.create("button",{type: "button", innerHTML:"Cancel", onclick: function(){
            renderButtons(buttons);
        }}, buttons);
    }

    function removeUserQuestion(e) {
        u.clear(buttons);
        u.create("div",{className:"question", innerHTML: "Are you sure you want to remove user "+userNumber+" from group "+groupId+"?"}, buttons);
        u.create("button",{className:"question", type: "button", innerHTML:"Yes", onclick: removeUser}, buttons);

        u.create("button",{ type: "button", innerHTML:"No", onclick: function(){
            renderButtons(buttons);
        }}, buttons);
    }

    function removeUser() {
        // database.ref().child(groupId).remove();
        // database.ref("_groups").child(groupId).remove();

        var ref = database.ref();
        ref.child(groupId).child("u/p").child(userNumber).remove();
        ref.child(groupId).child("u/b").child(userNumber).remove();

        ref.child(groupId).child("u/k").once("value").then(function(snapshot){
            var val = snapshot.val();
            if(!val) return;
            for(var i in val) {
                if(""+val[i] == ""+userNumber) {
                    ref.child(groupId).child("u/k").child(i).remove();
                }
            }
        });

        WAIN.switchTo("/admin/group/" + groupId);
    }

    return {
        start: function(request) {
           if(request) {
               groupId = request[3];
               userNumber = request[4];
           } else {
               groupId = data.request[3];
               userNumber = data.request[4];
           }
           this.page = "user" + "/" + groupId + "/" + userNumber;
           div = u.createPage(this);

           renderInterface();

//           updateData();
        },
        page: "user",
        icon: "navigation",
        title: "User",
    }
}
document.addEventListener("DOMContentLoaded", (new User()).start);