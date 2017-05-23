/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 1/23/17.
 */
function User() {

    var title = "User";

    var positions;
    var div;
    var groupId;
    var userNumber;
    var tableSummary;

    var renderInterface = function() {

        var ref = database.ref();

        u.create(HTML.H2, "Summary", div);

        tableSummary = u.table({
            className: "option",
            placeholder: "Loading..."
        }, div);

       tableSummary.userNumberNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Number" },
            { className: "option", innerHTML: userNumber }
        ] });

        tableSummary.userNameNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Name" },
            { className: "option", innerHTML: "..." }
        ] });

        tableSummary.userActiveNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Active" },
            { className: "option highlight", innerHTML: "..." }
        ] });

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

        tableSummary.userKeyNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Key" },
            { className: "option", innerHTML: "..." }
        ] });

        tableSummary.userColorNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Color" },
                { style: { /*backgroundColor: utils.getHexColor(snapshot.val().color), */opacity: 0.5 } }
        ]});

        tableSummary.userCreatedNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Created" },
            { className: "option", innerHTML: "..." }
        ]});

        tableSummary.userUpdatedNode = tableSummary.add({
//            style: { display: (snapshot.val().persistent ? "none" : "")},
            cells: [
                { className: "th", innerHTML: "Updated" },
                { className: "highlight option", innerHTML: "..." }
            ]
        });

        tableSummary.userOsNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Platform" },
            { className: "option", innerHTML: "..." }
        ]});

        tableSummary.userDeviceNode = tableSummary.add({ cells: [
            { className: "th", innerHTML: "Device" },
            { className: "option", innerHTML: "..." }
        ]});


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

        u.create(HTML.BR, null, div);
        buttons = u.create({className:"buttons"}, div);
        renderButtons(buttons);

        return div;

    }

    function updateSummary() {
        var ref = database.ref();
        tableSummary.placeholder.show();

        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA_PRIVATE).off();
        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA_PRIVATE).child(userNumber).once("value").then(function(snapshot){
            if(!snapshot || !snapshot.val()) return;
            tableSummary.userOsNode.lastChild.innerHTML = snapshot.val().os;
            tableSummary.userDeviceNode.lastChild.innerHTML = snapshot.val().model;
            tableSummary.userKeyNode.lastChild.innerHTML = snapshot.val().key;
        }).catch(function(error){
            console.warn("Resign because of",error.message);
            WTU.resign(updateSummary);
        });


        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(userNumber).off();
        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(userNumber).on("value",function(snapshot) {
            if(!snapshot || !snapshot.val()) return;

            tableSummary.placeholder.hide();

            tableSummary.userNameNode.lastChild.innerHTML = snapshot.val().name;
            tableSummary.userActiveNode.lastChild.innerHTML = snapshot.val().active ? "Yes" : "No";

            tableSummary.userColorNode.lastChild.style.backgroundColor = utils.getHexColor(snapshot.val().color);

            tableSummary.userCreatedNode.lastChild.innerHTML = snapshot.val().created ? new Date(snapshot.val().created).toLocaleString() : "&#150;"

            tableSummary.userUpdatedNode.lastChild.innerHTML = snapshot.val().created ? new Date(snapshot.val().changed).toLocaleString() : "&#150;"
//                userActiveNode.lastChild.classList.add("changed");
//                setTimeout(function(){userActiveNode.lastChild.classList.remove("changed")}, 5000);

        },function(error){
              console.warn("Resign because of",error.message);
              WTU.resign(updateSummary);
          });
    }

    function updateData(){

        var ref = database.ref();
        u.clear(tbody);

        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).off();
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
            u.create("td", { style: { backgroundColor: utils.getHexColor(snapshot.val().color), opacity: 0.5 } }, tr);
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
        u.create(HTML.BUTTON, { innerHTML:"Remove", onclick: removeUser}, div);
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
        }}, buttons);
        u.create(HTML.BUTTON,{innerHTML:"Cancel", onclick: function(){
            renderButtons(buttons);
        }}, buttons);
    }

    function switchActive(number, active) {
        u.progress.show("Switching...")
        var ref = database.ref();
        u.post("/admin/rest/v1/user/switch", JSON.stringify({group_id:groupId, user_number:userNumber,property:DATABASE.USER_ACTIVE,value:active}))
        .then(function(){
            u.progress.hide();
            if(!active) {
                u.toast.show("User #"+userNumber+" is offline.");
                WTU.switchTo("/admin/group/" + groupId);
            } else {
                u.toast.show("User #"+userNumber+" is online.");
            }
        }).catch(function(code,xhr){
           u.progress.hide();
           console.warn("Resign because of",code,xhr);
           WTU.resign(updateSummary);
           var res = JSON.parse(xhr.responseText) || {};
           u.toast.show(res.message || xhr.statusText);
           renderButtons(buttons);
         });

    }

    function removeUser() {
        u.clear(buttons);
        u.create({className:"question", innerHTML: "Are you sure you want to remove user "+userNumber+" from group "+groupId+"?"}, buttons);
        u.create(HTML.BUTTON,{ className:"question", innerHTML:"Yes", onclick: function() {
            u.progress.show("Removing...");
            u.post("/admin/rest/v1/user/remove", JSON.stringify({group_id:groupId, user_number:userNumber}))
            .then(function(){
                u.progress.hide();
               u.toast.show("User #"+userNumber+" was removed.");
            }).catch(function(code,xhr){
                u.progress.hide();
               console.warn("Resign because of",code,xhr);
               WTU.resign(updateSummary);
               var res = JSON.parse(xhr.responseText) || {};
               u.toast.show(res.message || xhr.statusText);
               renderButtons(buttons);
             });
        }}, buttons);
        u.create(HTML.BUTTON,{ innerHTML:"No", onclick: function(){
            renderButtons(buttons);
        }}, buttons);

    }

    return {
        start: function(request) {
           if(request) {
               groupId = request[3];
               userNumber = request[4];
           } else {
                var parts = window.location.pathname.split("/");
               groupId = parts[3];
               userNumber = parts[4];
           }
           this.page = "user" + "/" + groupId + "/" + userNumber;
           div = document.getElementsByClassName("layout")[0];
            u.clear(div);

           renderInterface();
            updateSummary();
//           updateData();
        },
        page: "user",
        icon: "navigation",
        title: "User",
        move:true,
    }
}
