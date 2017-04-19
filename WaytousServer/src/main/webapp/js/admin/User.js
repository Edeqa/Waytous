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
    var tableSummary;

    var u = new Utils();

    var renderInterface = function() {

//        div.appendChild(renderAlertArea());

        u.create("h2", "Summary", div);

        tableSummary = u.table({
            className: "option",
            placeholder: "Loading..."
        }, div);

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
        tableSummary.placeholder.show();

        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(userNumber).once("value").then(function(snapshot) {
            if(!snapshot || !snapshot.val()) return;

            tableSummary.add({ cells: [
                { className: "th", innerHTML: "Number" },
                { className: "option", innerHTML: userNumber }
            ] });

            tableSummary.add({ cells: [
                { className: "th", innerHTML: "Name" },
                { className: "option", innerHTML: snapshot.val().name }
            ] });

            var userActiveNode = tableSummary.add({ cells: [
                { className: "th", innerHTML: "Active" },
                { className: "option highlight", innerHTML: "..." }
            ] });

            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(userNumber).child("active").on("value", function(snapshot){
                userActiveNode.lastChild.innerHTML = snapshot.val() ? "Yes" : "No";
                userActiveNode.lastChild.classList.add("changed");
                setTimeout(function(){userActiveNode.lastChild.classList.remove("changed")}, 5000);
            });

            tableSummary.add({
                onclick: function(){
                     WTU.switchTo("/admin/group/"+groupId);
                     return false;
                 },
                cells: [
                    { className: "th", innerHTML: "Group" },
                    { className: "option", innerHTML: groupId }
                ]
            });

            var userKey = tableSummary.add({ cells: [
                { className: "th", innerHTML: "Key" },
                { className: "option", innerHTML: "..." }
            ] });

            tableSummary.add({ cells: [
                { className: "th", innerHTML: "Color" },
                { style: { backgroundColor: u.getHexColor(snapshot.val().color), opacity: 0.5 } }
            ]});

            tableSummary.add({ cells: [
                { className: "th", innerHTML: "Created" },
                { className: "option", innerHTML: snapshot.val().created ? new Date(snapshot.val().created).toLocaleString() : "&#150;" }
            ]});

            var userUpdatedNode = tableSummary.add({
                style: { display: (snapshot.val().persistent ? "none" : "")},
                cells: [
                    { className: "th", innerHTML: "Updated" },
                    { className: "highlight option", innerHTML: "..." }
                ]
            });

            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(userNumber).child("changed").on("value", function(snapshot){
                userUpdatedNode.lastChild.innerHTML = new Date(snapshot.val()).toLocaleString();
                userUpdatedNode.lastChild.classList.add("changed");
                setTimeout(function(){userUpdatedNode.lastChild.classList.remove("changed")}, 5000);
            });

            var userOs = tableSummary.add({ cells: [
                { className: "th", innerHTML: "Platform" },
                { className: "option", innerHTML: "..." }
            ]});

            var userDevice = tableSummary.add({ cells: [
                { className: "th", innerHTML: "Device" },
                { className: "option", innerHTML: "..." }
            ]});

            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA_PRIVATE).child(userNumber).once("value").then(function(snapshot){
                userOs.lastChild.innerHTML = snapshot.val().os;
                userDevice.lastChild.innerHTML = snapshot.val().model;
                userKey.lastChild.innerHTML = snapshot.val().key;
            }).catch(function(error){
                console.error("ERROR, RESIGNING",error);
                WTU.resign(updateSummary);
            });

        }).catch(function(error){
            tableSummary.placeholder.show("No data, try to refresh page.");
        });

    }

    function updateData(){

        var ref = database.ref();
        u.clear(tbody);

        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).on("child_added", function(snapshot) {
            if(!snapshot || !snapshot.val()) return;
            var tr = u.create("tr", { className: "highlight " + ((snapshot.val().active ? "" : "inactive"))}, tbody);

            u.create(HTML.A, { href:"#", onclick: function(){
                WTU.switchTo("/admin/user/"+groupId+"/"+snapshot.key);
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

            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(snapshot.key).child("changed").on("value", function(snapshot){
                userChanged.innerHTML = new Date(snapshot.val()).toLocaleString();
                tr.classList.add("changed");
                setTimeout(function(){tr.classList.remove("changed")}, 2000);
            });
            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA_PRIVATE).child(snapshot.key).once("value").then(function(snapshot){
                userOs.innerHTML = snapshot.val().os;
                userDevice.innerHTML = snapshot.val().model;
            });
        });

    }

    function renderButtons(div) {
        u.clear(div);
        u.create(HTML.BUTTON, { innerHTML:"Switch activity", onclick: switchActivity}, div);
        u.create(HTML.BUTTON, { innerHTML:"Remove", onclick: removeUserQuestion}, div);
    }

    function switchActivity(e){
        var ref = database.ref();
        u.clear(buttons);

        u.create(HTML.BUTTON,{innerHTML:"Active", onclick: function(){
            switchActive(userNumber, true);
            renderButtons(buttons);
        }}, buttons);
        u.create(HTML.BUTTON,{innerHTML:"Inactive", onclick: function(){
            switchActive(userNumber, false);
            renderButtons(buttons);
        }}, buttons);
        u.create(HTML.BUTTON,{innerHTML:"Cancel", onclick: function(){
            renderButtons(buttons);
        }}, buttons);
    }

    function switchActive(number, active) {
        var ref = database.ref();
        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(number).child("active").set(active).catch(function(e){
            WTU.resign(function(){
                switchActive(number, active);
            })
        });
    }

    function removeUserQuestion(e) {
        u.clear(buttons);
        u.create(HTML.DIV,{className:"question", innerHTML: "Are you sure you want to remove user "+userNumber+" from group "+groupId+"?"}, buttons);
        u.create(HTML.BUTTON,{className:"question",innerHTML:"Yes", onclick: removeUser}, buttons);

        u.create(HTML.BUTTON,{ innerHTML:"No", onclick: function(){
            renderButtons(buttons);
        }}, buttons);
    }

    function removeUser() {
        // database.ref().child(groupId).remove();
        // database.ref("_groups").child(groupId).remove();

        var ref = database.ref();
        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA_PRIVATE).child(userNumber).remove();
        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(userNumber).remove();

        ref.child(groupId).child(DATABASE.SECTION_USERS_KEYS).once("value").then(function(snapshot){
            var val = snapshot.val();
            if(!val) return;
            for(var i in val) {
                if(""+val[i] == ""+userNumber) {
                    ref.child(groupId).child(DATABASE.SECTION_USERS_KEYS).child(i).remove();
                }
            }
        });

        WTU.switchTo("/admin/group/" + groupId);
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
           div = document.getElementsByClassName("content")[0];

           renderInterface();
            updateSummary();
//           updateData();
        },
        page: "user",
        icon: "navigation",
        title: "User",
    }
}
document.addEventListener("DOMContentLoaded", (new User()).start);
