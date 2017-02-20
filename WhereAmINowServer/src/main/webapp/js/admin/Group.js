/**
 * Created 1/19/17.
 */
function Group() {

    var title = "Group";

    var alertArea;
    var tbody;
    var user;
    var firebaseToken;
    var div;
    var groupId;
    var summary;
    var buttons;

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
        u.create("div", {className: "th", innerHTML: "Loading..."}, u.create("div", {className:"thead"}, summary));

        u.create("h2", "Users", div);

        var table = u.create("div", {id:"users", className:"table"}, div);

        var thead = u.create("div", {className: "thead"}, table);
        var trhead = u.create("div", {className: "tr"}, thead);

        u.create("div", {className: "th", innerHTML:"#", width:"5%"}, trhead);
        u.create("div", {className: "th", innerHTML:"Name"}, trhead);
        u.create("div", {className: "th", innerHTML:"Color", width:"5%"}, trhead);
        u.create("div", {className: "th hideable", innerHTML:"Created"}, trhead);
        u.create("div", {className: "th", innerHTML:"Updated"}, trhead);
        u.create("div", {className: "th hideable", innerHTML:"Platform"}, trhead);
        u.create("div", {className: "th hideable", innerHTML:"Device"}, trhead);

        tbody = u.create("div", {className:"tbody changeable"}, table);
        u.create("div", {
            className:"th",
            colspan: trhead.childElementCount,
            align: "center",
            innerHTML: "Loading..."
        }, u.create("div", {className:"tr"}, tbody));

        u.create("br", null, div);
        buttons = u.create("div", {className:"buttons"}, div);
        renderButtons(buttons);

        return div;
    }

    function updateSummary() {
        var ref = database.ref();

        ref.child(groupId).child("o").once("value").then(function(snapshot) {
            if(!snapshot || !snapshot.val()) return;
            u.clear(summary);

            var tr = u.create("div", {className: "tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"ID"}, tr);

            var td = u.create("div", {className: "td option"}, tr);
            u.create("a", { href:"/track/"+groupId, innerHTML:groupId, target:"_blank"}, td);
            u.create("span", " ", td);
            u.create("a", { href:"/group/"+groupId, innerHTML:"(Open in browser)", target:"_blank"}, td);


            tr = u.create("div", {className: "tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Requires password"}, tr);
            u.create("div", {className: "td option", innerHTML:snapshot.val()["requires-password"] ? "Yes" : "No"}, tr);

            tr = u.create("div", {className: "tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Welcome message"}, tr);
            u.create("div", {className: "td option", innerHTML:snapshot.val()["welcome-message"]}, tr);

            tr = u.create("div", {className: "tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Persistent group"}, tr);
            u.create("div", {className: "td option", innerHTML:snapshot.val().persistent ? "Yes" : "No"}, tr);

            tr = u.create("tr", { style: { display: (snapshot.val().persistent ? "none" : "")}}, summary);
            u.create("div", {className: "th option", innerHTML:"Time to live, min"}, tr);
            u.create("div", {className: "td option", innerHTML:snapshot.val()["time-to-live-if-empty"]}, tr);

            tr = u.create("div", {className: "tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Dismiss inactive after, sec"}, tr);
            u.create("div", {className: "td option", innerHTML:snapshot.val()["dismiss-inactive"] ? snapshot.val()["delay-to-dismiss"] : "&#150;"}, tr);

            tr = u.create("div", {className: "tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Created"}, tr);
            u.create("div", {className: "td option", innerHTML:snapshot.val()["date-created"] ? new Date(snapshot.val()["date-created"]).toLocaleString() : "&#150;"}, tr);

            tr = u.create("div", {className: "tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Changed"}, tr);
            var changedNode = u.create("div", {className: "td option", innerHTML:"..."}, tr);

            tr = u.create("div", {className: "tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"Users total"}, tr);
            var usersNode = u.create("div", {className: "td option", innerHTML:"..."}, tr);

            tr = u.create("div", {className: "tr"}, summary);
            u.create("div", {className: "th option", innerHTML:"&#150; online"}, tr);
            var activeUsersNode = u.create("div", {className: "td option", innerHTML:"..."}, tr);

            ref.child(groupId).child("u/b").on("value", function(snapshot){
                if(!snapshot.val()) return;
                usersNode.innerHTML = snapshot.val().length;
                var changed = 0;var active = 0;
                for(var i in snapshot.val()) {
                    var c = parseInt(snapshot.val()[i].changed);
                    if(c > changed) changed = c;
                    if(snapshot.val()[i].active) active ++;
                }
                changedNode.innerHTML = new Date(changed).toLocaleString();
                activeUsersNode.innerHTML = active;
            });
        }).catch(function(error){
            u.clear(summary);
            u.create("div", { className:"th", align: "center", innerHTML: "Error loading data, try to refresh page."}, u.create("div", {className:"tr"}, summary));
        });

    }

    function updateData(){

        var ref = database.ref();
        u.clear(tbody);

        ref.child(groupId).child("u/b").on("child_added", function(snapshot) {
            if(!snapshot || !snapshot.val()) return;

            var tr = u.create("div", { className: "clickable tr " + (snapshot.val().active ? "" : "inactive"), onclick: function(){
               WAIN.switchTo("/admin/user/"+groupId+"/"+snapshot.key);
               return false;
            }}, tbody);

            u.create("div", {className: "td", innerHTML:snapshot.key}, tr);
            u.create("div", {className: "td", innerHTML:snapshot.val().name}, tr);
            u.create("div", {className: "td", style: { backgroundColor: u.getHexColor(snapshot.val().color), opacity: 0.5 } }, tr);
            u.create("div", {className: "td hideable", innerHTML:snapshot.val().created ? new Date(snapshot.val().created).toLocaleString() : "&#150;"}, tr);
            var userChanged = u.create("div", {className: "td", innerHTML:"..."}, tr);
            var userOs = u.create("div", {className: "td hideable", innerHTML:"..."}, tr);
            var userDevice = u.create("div", {className: "td hideable", innerHTML:"..."}, tr);

            ref.child(groupId).child("u/b").child(snapshot.key).child("changed").on("value", function(snapshot){
                if(!snapshot.val()) return;
                userChanged.innerHTML = new Date(snapshot.val()).toLocaleString();
                tr.classList.add("changed");
                setTimeout(function(){tr.classList.remove("changed")}, 2000);
            });
            ref.child(groupId).child("u/p").child(snapshot.key).once("value").then(function(snapshot){
                if(!snapshot.val()) return;
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
        u.create("button", { type: "button", innerHTML:"Delete group", onclick: deleteGroupQuestion}, div);
    }

    function deleteGroupQuestion(e){
        u.clear(buttons);

        u.create("div",{className:"question", innerHTML: "Are you sure you want to delete group "+groupId+"?"}, buttons);
        u.create("button",{className:"question", type: "button", innerHTML:"Yes", onclick: deleteGroup}, buttons);

        u.create("button",{ type: "button", innerHTML:"No", onclick: function(){
            renderButtons(buttons);
        }}, buttons);
    }

    function deleteGroup() {
        database.ref().child(groupId).remove();
        database.ref("_groups").child(groupId).remove();
        WAIN.switchTo("/admin/groups");
    }

    return {
        start: function(request) {
            if(request) {
                this.page = request[2] + "/" + request[3];
                groupId = request[3];
            } else {
                this.page = data.request[2] + "/" + data.request[3];
                groupId = data.request[3];
            }
            div = u.createPage(this);

            renderInterface();

            updateData();
        },
        page: "group",
//        icon: "list",
        title: title,
//        menu: title,
    }
}
document.addEventListener("DOMContentLoaded", (new Group()).start);


