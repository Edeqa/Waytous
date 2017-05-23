/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 1/19/17.
 */
function Group() {

    var title = "Group";

    var alertArea;
    var user;
    var firebaseToken;
    var div;
    var groupId;
    var summary;
    var buttons;
    var tableSummary;
    var tableUsers;

    var renderInterface = function() {

        var ref = database.ref();
        u.clear(div);
        u.create(HTML.H2, "Summary", div);

        tableSummary = u.table({
            className: "option",
            placeholder: "Loading..."
        }, div);

        var url = new URL(window.location.href);
        url = "https://" + url.hostname + (data.HTTPS_PORT == 443 ? "" : ":"+ data.HTTPS_PORT);

        var td = u.create()
            .place(HTML.A, { href: url + "/track/"+groupId, innerHTML:groupId, target:"_blank"})
            .place(HTML.SPAN, " ")
            .place(HTML.A, { href: url + "/group/"+groupId, innerHTML:"(Force open in browser)", target:"_blank"});

         tableSummary.add({ cells: [
                { className: "th", innerHTML: "ID" },
                { className: "option", content: td }
        ]});

        /*var requiresPasswordNode = tableSummary.add({
            onclick: function() {
                this.lastChild.innerHTML += " ...wait";
                u.post("/admin/rest/v1/group/switch", JSON.stringify({group_id:groupId, property:DATABASE.OPTION_REQUIRES_PASSWORD}))
                .catch(function(code,xhr){
                   console.error(code,xhr);
                   WTU.resign(updateSummary);
                   var res = JSON.parse(xhr.responseText) || {};
                   u.toast.show(res.message || xhr.statusText);
               });
            },
            cells: [
                { className: "th", innerHTML: "Requires password" },
                { className: "option", innerHTML: snapshot.val()[DATABASE.OPTION_REQUIRES_PASSWORD] ? "Yes" : "No" }
        ]});


        var passwordNode = tableSummary.add({
            className: snapshot.val()[DATABASE.OPTION_REQUIRES_PASSWORD] ? "" : "hidden",
            cells: [
                { className: "th", innerHTML: "&#150; password" },
                { className: "option", innerHTML: u.create(HTML.DIV, {innerHTML: "Not set"}).place(HTML.BUTTON, {className:"group-button-set-password", innerHTML:"Set password", onclick: function(e){
                      e.stopPropagation();
                      passwordNode.childNodes[1].firstChild.firstChild.nodeValue = "Not set "+Math.random()
                  }}) }
        ]});*/


        tableSummary.welcomeMessageNode = tableSummary.add({
            onclick: function() {
                ref.child(groupId).child(DATABASE.SECTION_OPTIONS).child(DATABASE.OPTION_WELCOME_MESSAGE).once("value").then(function(snapshot){
                    u.dialog({
                        title: "Welcome message",
                        items: [
                            { type:HTML.INPUT, className:"welcome-input", value: tableSummary.welcomeMessageNode.lastChild.innerHTML }
                        ],
                        positive: {
                            label: "OK",
                            onclick: function(items) {
                                var newValue = items[0].value;
                                if(tableSummary.welcomeMessageNode.lastChild.innerHTML != newValue) {
                                    tableSummary.welcomeMessageNode.lastChild.innerHTML += " ...wait";
                                    u.post("/admin/rest/v1/group/modify", JSON.stringify({group_id:groupId, property:DATABASE.OPTION_WELCOME_MESSAGE, value:newValue}))
                                    .catch(function(code,xhr){
                                       console.error(code,xhr);
                                       WTU.resign(updateSummary);
                                       var res = JSON.parse(xhr.responseText) || {};
                                       u.toast.show(res.message || xhr.statusText);
                                   });
                               }
                            }
                        },
                        negative: { label:"Cancel"}
                    }).open();
                }).catch(function(error){
                      console.warn("Resign because of",error);
                      WTU.resign(updateSummary);
                  });
            },
            cells: [
                { className: "th", innerHTML: "Welcome message" },
                { className: "option", innerHTML: "..." }
        ]});

        tableSummary.persistentNode = tableSummary.add({
            onclick: function() {
                this.lastChild.innerHTML += " ...wait";
                u.post("/admin/rest/v1/group/switch", JSON.stringify({group_id:groupId, property:DATABASE.OPTION_PERSISTENT}))
                .catch(function(code,xhr){
                   console.warn("Resign because of",code,xhr);
                   WTU.resign(updateSummary);
                   var res = JSON.parse(xhr.responseText) || {};
                   u.toast.show(res.message || xhr.statusText);
               });
            },
            cells: [
                { className: "th", innerHTML: "Persistent group" },
                { className: "option", innerHTML: "..." }
        ]});

        tableSummary.timeToLiveNode = tableSummary.add({
            className: "hidden",
            onclick: function() {
                u.dialog({
                    title: "Time to live",
                    items: [
                        { type:HTML.DIV, innerHTML:"Set time to live (in minutes) if the group is empty (i.e. all users are offline)." },
                        { type:HTML.NUMBER, label:"Time to live, min", value:parseInt(tableSummary.timeToLiveNode.lastChild.innerHTML || 15) }
                    ],
                    positive: {
                        label: "OK",
                        dismiss: false,
                        onclick: function(items) {
                            var newValue = items[1].value;
                            if(tableSummary.timeToLiveNode.lastChild.innerHTML != newValue) {
                                tableSummary.timeToLiveNode.lastChild.innerHTML += " ...wait";
                                u.post("/admin/rest/v1/group/modify", JSON.stringify({group_id:groupId, property:DATABASE.OPTION_TIME_TO_LIVE_IF_EMPTY, value:newValue}))
                                .catch(function(code,xhr){
                                   console.warn("Resign because of",code,xhr);
                                   WTU.resign(updateSummary);
                                   var res = JSON.parse(xhr.responseText) || {};
                                   u.toast.show(res.message || xhr.statusText);
                               });
                           }
                        }
                    },
                    negative: { label:"Cancel"}
                }).open();
            },
            cells: [
                { className: "th", innerHTML: "&#150; time to live, min" },
                { className: "option", innerHTML: "..." }
            ]
        });

        tableSummary.dismissInactiveNode = tableSummary.add({
            onclick: function() {
                this.lastChild.innerHTML += " ...wait";
                 u.post("/admin/rest/v1/group/switch", JSON.stringify({group_id:groupId, property:DATABASE.OPTION_DISMISS_INACTIVE}))
                .catch(function(code,xhr){
                   console.warn("Resign because of",code,xhr);
                   WTU.resign(updateSummary);
                   var res = JSON.parse(xhr.responseText) || {};
                   u.toast.show(res.message || xhr.statusText);
                 });
            },
            cells: [
                { className: "th", innerHTML: "Dismiss inactive" },
                { className: "option", innerHTML: "..." }
        ]});

        tableSummary.delayToDismissNode = tableSummary.add({
            className: "hidden",
            onclick: function() {
                u.dialog({
                    title: "Delay to dismiss",
                    items: [
                        { type:HTML.DIV, innerHTML:"Switch user offline if he is not active at least (in seconds)." },
                        { type:HTML.NUMBER, label:"Delay to dismiss, sec", value:parseInt(tableSummary.delayToDismissNode.lastChild.innerHTML || 300) }
                    ],
                    positive: {
                        label: "OK",
                        onclick: function(items) {
                            var newValue = items[1].value;
                            if(tableSummary.delayToDismissNode.lastChild.innerHTML != newValue) {
                                tableSummary.delayToDismissNode.lastChild.innerHTML += " ...wait";
                                u.post("/admin/rest/v1/group/modify", JSON.stringify({group_id:groupId, property:DATABASE.OPTION_DELAY_TO_DISMISS, value:newValue}))
                                .catch(function(code,xhr){
                                   console.warn("Resign because of",code,xhr);
                                   WTU.resign(updateSummary);
                                   var res = JSON.parse(xhr.responseText) || {};
                                   u.toast.show(res.message || xhr.statusText);
                               });
                            }
                        }
                    },
                    negative: { label:"Cancel"}
                }).open();
            },
            cells: [
                { className: "th", innerHTML: "&#150; dismiss after, sec" },
                { className: "option", innerHTML: "..." }
        ]});

        tableSummary.createdNode = tableSummary.add({ cells: [
                { className: "th", innerHTML: "Created" },
                { className: "option", innerHTML: "..." }
        ]});

        tableSummary.changedNode = tableSummary.add({ cells: [
                { className: "th", innerHTML: "Changed" },
                { className: "option highlight", innerHTML: "..." }
        ]});

        function filterActive(row){
           return !row.classList.contains("inactive");
        };
        tableSummary.usersNode = tableSummary.add({
            onclick: function(e){
                tableUsers.filter.remove(filterActive);
            },
            cells: [
                { className: "th", innerHTML: "Users total" },
                { className: "option highlight", innerHTML: 0 }
        ]});

        tableSummary.activeUsersNode = tableSummary.add({
            onclick: function(e){
                tableUsers.filter.add(filterActive);
            },
            cells: [
                { className: "th", innerHTML: "&#150; online" },
                { className: "option highlight", innerHTML: 0 }
        ]});

        u.create(HTML.H2, "Users", div);

        tableUsers = u.table({
            id: "admin:users",
            caption: {
                items: [
                    { label: "#", width: "5%" },
                    { label: "Name" },
                    { label: "Color", width: "5%" },
                    { label: "Created", className: "media-hidden" },
                    { label: "Updated" },
                    { label: "Platform", className: "media-hidden" },
                    { label: "Device", className: "media-hidden" }
                ]
            },
            placeholder: "Loading..."
        }, div);

        u.create("br", null, div);
        buttons = u.create("div", {className:"buttons"}, div);
        renderButtons(buttons);

    }

    function updateSummary() {
        if(!groupId) {
            WTU.switchTo("/admin/groups/");
            return;
        }

        var ref = database.ref();
//        tableSummary.placeholder.show();

        ref.child(groupId).child(DATABASE.SECTION_OPTIONS).once("value").then(function(snapshot) {
            if(!snapshot || !snapshot.val()) return;

            ref.child(groupId).child(DATABASE.SECTION_OPTIONS).off();
            ref.child(groupId).child(DATABASE.SECTION_OPTIONS).on("value", function(snapshot){
                if(!snapshot.val()) return;

//                tableSummary.requiresPasswordNode.lastChild.innerHTML = snapshot.val()[DATABASE.OPTION_REQUIRES_PASSWORD] ? "Yes" : "No";
//                tableSummary.passwordNode[snapshot.val()[DATABASE.OPTION_REQUIRES_PASSWORD] ? "show":"hide"]();

                tableSummary.welcomeMessageNode.lastChild.innerHTML = snapshot.val()[DATABASE.OPTION_WELCOME_MESSAGE] || "";

                tableSummary.persistentNode.lastChild.innerHTML = snapshot.val()[DATABASE.OPTION_PERSISTENT] ? "Yes" : "No";
                tableSummary.timeToLiveNode[snapshot.val()[DATABASE.OPTION_PERSISTENT] ? "hide":"show"]();

                tableSummary.timeToLiveNode.lastChild.innerHTML = snapshot.val()[DATABASE.OPTION_TIME_TO_LIVE_IF_EMPTY] || 15;

                tableSummary.dismissInactiveNode.lastChild.innerHTML = snapshot.val()[DATABASE.OPTION_DISMISS_INACTIVE] ? "Yes" : "No";
                tableSummary.delayToDismissNode[snapshot.val()[DATABASE.OPTION_DISMISS_INACTIVE] ? "show":"hide"]();

                tableSummary.delayToDismissNode.lastChild.innerHTML = snapshot.val()[DATABASE.OPTION_DELAY_TO_DISMISS] || 300;

                tableSummary.createdNode.lastChild.innerHTML = new Date(snapshot.val()[DATABASE.OPTION_DATE_CREATED]).toLocaleString();
//                tableSummary.changedNode.lastChild.innerHTML = new Date(snapshot.val()[DATABASE.OPTION_DATE_CHANGED]).toLocaleString();

            });
        }).catch(function(error){
            console.warn("Resign because of",error);
            WTU.resign(updateAll);
        });

    }

    function updateAll() {
        updateSummary();
        updateData();
    }

    function updateData(){

        var ref = database.ref();
        tableUsers.placeholder.show();
        u.clear(tableUsers.body);
        var reload = false;
        var initial = true;
        setTimeout(function(){initial = false;}, 3000);

        tableSummary.usersNode.lastChild.innerHTML = 0;
        tableSummary.activeUsersNode.lastChild.innerHTML = 0;

        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).off();
        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA_PRIVATE).off();

        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).on("child_added", function(snapshot) {
            if(!snapshot || !snapshot.val()){
                tableUsers.placeholder.show("No users");
                return;
            }
            reload = false;

            var row = tableUsers.add({
                className: "highlight" + (snapshot.val()[DATABASE.USER_ACTIVE] ? "" : " inactive"),
                onclick: function(){
                    WTU.switchTo("/admin/user/"+groupId+"/"+snapshot.key);
                    return false;
                 },
                cells: [
                    { innerHTML: snapshot.key, sort: parseInt(snapshot.key) },
                    { innerHTML: snapshot.val()[DATABASE.USER_NAME] },
                    { style: { backgroundColor: utils.getHexColor(snapshot.val()[DATABASE.USER_COLOR]), opacity: 0.5 } },
                    { className: "media-hidden", sort: snapshot.val()[DATABASE.USER_CREATED], innerHTML: snapshot.val()[DATABASE.USER_CREATED] ? new Date(snapshot.val()[DATABASE.USER_CREATED]).toLocaleString() : "&#150;" },
                    { sort: 0, innerHTML: "..." },
                    { className: "media-hidden", innerHTML: "..." },
                    { className: "media-hidden", innerHTML: "..." }
                ]
            });
            var userName = row.cells[1];
            var userChanged = row.cells[4];
            var userOs = row.cells[5];
            var userDevice = row.cells[6];

            var usersCount = parseInt(tableSummary.usersNode.lastChild.innerHTML);
            tableSummary.usersNode.lastChild.innerHTML = usersCount + 1;
            if(snapshot.val()[DATABASE.USER_ACTIVE]) {
                var usersCount = parseInt(tableSummary.activeUsersNode.lastChild.innerHTML);
                tableSummary.activeUsersNode.lastChild.innerHTML = ++usersCount;
            }

            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(snapshot.key).child(DATABASE.USER_CHANGED).on("value", function(snapshot){
                if(!snapshot.val()) return;
                userChanged.sort = snapshot.val();
                userChanged.innerHTML = new Date(snapshot.val()).toLocaleString();
                if(!initial) row.classList.add("changed");
                setTimeout(function(){row.classList.remove("changed")}, 5000);
                tableSummary.changedNode.lastChild.innerHTML = new Date(snapshot.val()).toLocaleString();
                tableUsers.update();
            });
            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(snapshot.key).child(DATABASE.USER_ACTIVE).on("value", function(snapshot){
                var active = !!snapshot.val();
                var wasInactive = row.classList.contains("inactive");
                row.classList[active ? "remove" : "add"]("inactive");
                var usersCount = parseInt(tableSummary.activeUsersNode.lastChild.innerHTML);
                if(active && wasInactive) {
                    tableSummary.activeUsersNode.lastChild.innerHTML = ++usersCount;
                } else if(!active && !wasInactive) {
                    tableSummary.activeUsersNode.lastChild.innerHTML = --usersCount;
                }
                tableUsers.update();
            });
            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(snapshot.key).child(DATABASE.USER_NAME).on("value", function(snapshot){
                if(snapshot.val()) {
                    userName.innerHTML = snapshot.val();
                } else {
                    userName.innerHTML = "undefined";
                }
                tableUsers.update();
            });
            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA_PRIVATE).child(snapshot.key).once("value").then(function(snapshot){
                if(!snapshot.val()) return;
                userOs.innerHTML = snapshot.val().os;
                userDevice.innerHTML = snapshot.val().model;
                tableUsers.update();
            }).catch(function(error){
                if(!reload) {
                    reload = true;
                    console.warn("Resign because of",error);
                    WTU.resign(updateData);
                } else {
//                    console.error("ERROR, ALREADY RESIGNING");
                }
             });
        }, function(error){
            console.warn("Resign because of",error);
            WTU.resign(updateAll);
        });

    }

    function renderButtons(div) {
        u.clear(div);
        u.create(HTML.BUTTON, { innerHTML:"Delete group", onclick: deleteGroupQuestion}, div);
    }

    function deleteGroupQuestion(e){
        u.clear(buttons);
        u.create({className:"question", innerHTML: "Are you sure you want to delete group "+groupId+"?"}, buttons);
        u.create(HTML.BUTTON,{ className:"question", innerHTML:"Yes", onclick: function() {
            u.post("/admin/rest/v1/group/delete", JSON.stringify({group_id:groupId}))
            .then(function(){
               WTU.switchTo("/admin/groups");
               u.toast.show("Group "+groupId+" was deleted.");
            }).catch(function(code,xhr){
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
                this.page = request[2] + "/" + request[3];
                groupId = request[3];
            } else {
                var parts = window.location.pathname.split("/");
                this.page = parts[2] + "/" + parts[3];
                groupId = parts[3];
            }
            div = document.getElementsByClassName("layout")[0];
            u.clear(div);

            renderInterface();
            updateSummary();
            updateData();
        },
        page: "group",
        title: title,
        move:true,
    }
}


