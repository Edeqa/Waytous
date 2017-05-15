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

//        div.appendChild(renderAlertArea());

        u.create(HTML.H2, "Summary", div);

        tableSummary = u.table({
            className: "option",
            placeholder: "Loading..."
        }, div);

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
        tableSummary.placeholder.show();

        ref.child(groupId).child(DATABASE.SECTION_OPTIONS).off();
        ref.child(groupId).child(DATABASE.SECTION_OPTIONS).once("value").then(function(snapshot) {
            if(!snapshot || !snapshot.val()) return;

            var td = u.create()
                .place(HTML.A, { href:"/track/"+groupId, innerHTML:groupId, target:"_blank"})
                .place(HTML.SPAN, " ")
                .place(HTML.A, { href:"/group/"+groupId, innerHTML:"(Force open in browser)", target:"_blank"});

             tableSummary.add({ cells: [
                    { className: "th", innerHTML: "ID" },
                    { className: "option", content: td }
            ]});
/*
            var requiresPasswordNode = tableSummary.add({
                onclick: function() {
                    ref.child(groupId).child(DATABASE.SECTION_OPTIONS_REQUIRES_PASSWORD).once("value").then(function(snapshot){
                        var newValue = !snapshot.val();
                        ref.child(groupId).child(DATABASE.SECTION_OPTIONS_REQUIRES_PASSWORD).set(newValue).then(function(){
                            requiresPasswordNode.childNodes[1].innerHTML = newValue ? "Yes" : "No";
                            passwordNode[newValue ? "show" : "hide"]();
                        }).catch(function(error){
                            WTU.resign(updateSummary);
                            console.error(error)
                        });
                    }).catch(function(error){
                          WTU.resign(updateSummary);
                      });
                },
                cells: [
                    { className: "th", innerHTML: "Requires password" },
                    { className: "option", innerHTML: snapshot.val()["requires-password"] ? "Yes" : "No" }
            ]});


            var passwordNode = tableSummary.add({
                className: snapshot.val()["requires-password"] ? "" : "hidden",
                cells: [
                    { className: "th", innerHTML: "&#150; password" },
                    { className: "option", innerHTML: u.create(HTML.DIV, {innerHTML: "Not set"}).place(HTML.BUTTON, {className:"group-button-set-password", innerHTML:"Set password", onclick: function(e){
                          e.stopPropagation();
                          passwordNode.childNodes[1].firstChild.firstChild.nodeValue = "Not set "+Math.random()
                      }}) }
            ]});
*/
            var welcomeMessageNode = tableSummary.add({
                onclick: function() {
                    ref.child(groupId).child(DATABASE.SECTION_OPTIONS_WELCOME_MESSAGE).once("value").then(function(snapshot){
                        u.dialog({
                            title: "Welcome message",
                            items: [
                                { type:HTML.INPUT, className:"welcome-input", value:snapshot.val() }
                            ],
                            positive: {
                                label: "OK",
                                onclick: function(items) {
                                    var newValue = items[0].value;
                                    ref.child(groupId).child(DATABASE.SECTION_OPTIONS_WELCOME_MESSAGE).set(newValue).then(function(){
                                        welcomeMessageNode.childNodes[1].innerHTML = newValue;
                                    }).catch(function(error){
                                        WTU.resign(updateSummary);
                                        console.error(error)
                                    });
                                }
                            },
                            negative: { label:"Cancel"}
                        }).open();
                    }).catch(function(error){
                          WTU.resign(updateSummary);
                      });
                },
                cells: [
                    { className: "th", innerHTML: "Welcome message" },
                    { className: "option", innerHTML: snapshot.val()["welcome-message"] }
            ]});

            var persistentNode = tableSummary.add({
                onclick: function() {
                    ref.child(groupId).child(DATABASE.SECTION_OPTIONS_PERSISTENT).once("value").then(function(snapshot){
                        var newValue = !snapshot.val();
                        ref.child(groupId).child(DATABASE.SECTION_OPTIONS_PERSISTENT).set(newValue).then(function(){
                            persistentNode.childNodes[1].innerHTML = newValue ? "Yes" : "No";
                            timeToLiveNode[newValue ? "hide" : "show"]();
                        }).catch(function(error){
                            WTU.resign(updateSummary);
                            console.error(error)
                        });
                    }).catch(function(error){
                          WTU.resign(updateSummary);
                      });
                },
                cells: [
                    { className: "th", innerHTML: "Persistent group" },
                    { className: "option", innerHTML: snapshot.val().persistent ? "Yes" : "No" }
            ]});

            var timeToLiveNode = tableSummary.add({
                className: snapshot.val().persistent ? "hidden" : "",
                onclick: function() {
                    ref.child(groupId).child(DATABASE.SECTION_OPTIONS_TIME_TO_LIVE_IF_EMPTY).once("value").then(function(snapshot){
                        u.dialog({
                            title: "Time to live",
                            items: [
                                { type:HTML.DIV, innerHTML:"Set time to live (in minutes) if the group is empty (i.e. all users are offline)." },
                                { type:HTML.NUMBER, label:"Time to live, min", value:snapshot.val() }
                            ],
                            positive: {
                                label: "OK",
                                dismiss: false,
                                onclick: function(items) {
                                    var newValue = parseInt(items[1].value);
                                    if(newValue == NaN) {
                                        items[1].value = "";
                                    } else if (newValue) {
                                        this.close();
                                        ref.child(groupId).child(DATABASE.SECTION_OPTIONS_TIME_TO_LIVE_IF_EMPTY).set(newValue).then(function(){
                                            timeToLiveNode.childNodes[1].innerHTML = newValue;
                                        }).catch(function(error){
                                            WTU.resign(updateSummary);
                                            console.error(error)
                                        });
                                    }
                                }
                            },
                            negative: { label:"Cancel"}
                        }).open();
                    }).catch(function(error){
                          WTU.resign(updateSummary);
                      });
                },
                cells: [
                    { className: "th", innerHTML: "&#150; time to live, min" },
                    { className: "option", innerHTML: snapshot.val()["time-to-live-if-empty"] }
                ]
            });

            var dismissInactiveNode = tableSummary.add({
                onclick: function() {
                    ref.child(groupId).child(DATABASE.SECTION_OPTIONS_DISMISS_INACTIVE).once("value").then(function(snapshot){
                        var newValue = !snapshot.val();
                        ref.child(groupId).child(DATABASE.SECTION_OPTIONS_DISMISS_INACTIVE).set(newValue).then(function(){
                            dismissInactiveNode.childNodes[1].innerHTML = newValue ? "Yes" : "No";
                            delayToDismissNode[newValue ? "show" : "hide"]();
                        }).catch(function(error){
                            WTU.resign(updateSummary);
                            console.error(error)
                        });
                    }).catch(function(error){
                          WTU.resign(updateSummary);
                      });
                },
                cells: [
                    { className: "th", innerHTML: "Dismiss inactive" },
                    { className: "option", innerHTML: snapshot.val()["dismiss-inactive"] ? "Yes" :"No" }
            ]});

            var delayToDismissNode = tableSummary.add({
                className: snapshot.val()["dismiss-inactive"] ? "" : "hidden",
                onclick: function() {
                    ref.child(groupId).child(DATABASE.SECTION_OPTIONS_DELAY_TO_DISMISS).once("value").then(function(snapshot){
                        u.dialog({
                            title: "Delay to dismiss",
                            items: [
                                { type:HTML.DIV, innerHTML:"Switch user offline if he is not active at least (in seconds)." },
                                { type:HTML.NUMBER, label:"Delay to dismiss, sec", value:snapshot.val() }
                            ],
                            positive: {
                                label: "OK",
                                dismiss: false,
                                onclick: function(items) {
                                    var newValue = parseInt(items[1].value);
                                    if(newValue == NaN) {
                                        items[1].value = "";
                                    } else if (newValue) {
                                        this.close();
                                        ref.child(groupId).child(DATABASE.SECTION_OPTIONS_DELAY_TO_DISMISS).set(newValue).then(function(){
                                            delayToDismissNode.childNodes[1].innerHTML = newValue;
                                        }).catch(function(error){
                                            WTU.resign(updateSummary);
                                            console.error(error)
                                        });
                                    }
                                }
                            },
                            negative: { label:"Cancel"}
                        }).open();
                    }).catch(function(error){
                          WTU.resign(updateSummary);
                      });
                },
                cells: [
                    { className: "th", innerHTML: "&#150; dismiss after, sec" },
                    { className: "option", innerHTML: snapshot.val()["dismiss-inactive"] ? snapshot.val()["delay-to-dismiss"] : "&#150;" }
            ]});

            tableSummary.add({ cells: [
                    { className: "th", innerHTML: "Created" },
                    { className: "option", innerHTML: snapshot.val()["date-created"] ? new Date(snapshot.val()["date-created"]).toLocaleString() : "&#150;" }
            ]});

            var changedNode = tableSummary.add({ cells: [
                    { className: "th", innerHTML: "Changed" },
                    { className: "option highlight", innerHTML: "..." }
            ]});

            function filterActive(row){
               return !row.classList.contains("inactive");
            };
            var usersNode = tableSummary.add({
                onclick: function(e){
                    tableUsers.filter.remove(filterActive);
                },
                cells: [
                    { className: "th", innerHTML: "Users total" },
                    { className: "option highlight", innerHTML: "..." }
            ]});

            var activeUsersNode = tableSummary.add({
                onclick: function(e){
                    tableUsers.filter.add(filterActive);
                },
                cells: [
                    { className: "th", innerHTML: "&#150; online" },
                    { className: "option highlight", innerHTML: "..." }
            ]});

            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).on("value", function(snapshot){
                if(!snapshot.val()) return;
                usersNode.lastChild.innerHTML = snapshot.val().length;
                var changed = 0;var active = 0;
                for(var i in snapshot.val()) {
                    var c = parseInt(snapshot.val()[i].changed);
                    if(c > changed) changed = c;
                    if(snapshot.val()[i].active) active ++;
                }
                changedNode.lastChild.innerHTML = new Date(changed).toLocaleString();
                activeUsersNode.lastChild.innerHTML = active;
            });
        }).catch(function(error){
            WTU.resign(updateSummary);
        });

    }

    function updateData(){

        var ref = database.ref();
        tableUsers.placeholder.show();
        var reload = false;
        var initial = true;
        setTimeout(function(){initial = false;}, 3000);

        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).off();
        ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).on("child_added", function(snapshot) {
            if(!snapshot || !snapshot.val()) return;
            reload = false;

            var row = tableUsers.add({
                className: "highlight" + (snapshot.val().active ? "" : " inactive"),
                onclick: function(){
                    WTU.switchTo("/admin/user/"+groupId+"/"+snapshot.key);
                    return false;
                 },
                cells: [
                    { innerHTML: snapshot.key },
                    { innerHTML: snapshot.val().name },
                    { style: { backgroundColor: utils.getHexColor(snapshot.val().color), opacity: 0.5 } },
                    { className: "media-hidden", sort: snapshot.val().created, innerHTML: snapshot.val().created ? new Date(snapshot.val().created).toLocaleString() : "&#150;" },
                    { sort: 0, innerHTML: "..." },
                    { className: "media-hidden", innerHTML: "..." },
                    { className: "media-hidden", innerHTML: "..." }
                ]
            });
            var userName = row.cells[1];
            var userChanged = row.cells[4];
            var userOs = row.cells[5];
            var userDevice = row.cells[6];

            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(snapshot.key).child("changed").on("value", function(snapshot){
                if(!snapshot.val()) return;
                userChanged.sort = snapshot.val();
                userChanged.innerHTML = new Date(snapshot.val()).toLocaleString();
                if(!initial) row.classList.add("changed");
                setTimeout(function(){row.classList.remove("changed")}, 5000);
                tableUsers.update();
            });
            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(snapshot.key).child("active").on("value", function(snapshot){
                if(snapshot.val()) {
                    row.classList.remove("inactive");
                } else {
                    row.classList.add("inactive");
                }
                tableUsers.update();
            });
            ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(snapshot.key).child("name").on("value", function(snapshot){
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
                    console.error("PERMISSION ERROR, RESIGNING",error);
                    WTU.resign(updateData);
                } else {
//                    console.error("ERROR, ALREADY RESIGNING");
                }
             });
        });

    }

    function renderButtons(div) {
        u.clear(div);
        u.create(HTML.BUTTON, { innerHTML:"Delete group", onclick: deleteGroupQuestion}, div);
    }

    function deleteGroupQuestion(e){
        u.clear(buttons);

        u.create({className:"question", innerHTML: "Are you sure you want to delete group "+groupId+"?"}, buttons);
        u.create(HTML.BUTTON,{ className:"question", innerHTML:"Yes", onclick: deleteGroup}, buttons);
        u.create(HTML.BUTTON,{ innerHTML:"No", onclick: function(){
            renderButtons(buttons);
        }}, buttons);
    }

    function deleteGroup() {
        database.ref(DATABASE.SECTION_GROUPS).child(groupId).remove();
        database.ref().child(groupId).remove();
        WTU.switchTo("/admin/groups");
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


