/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 9/11/17.
 */
function Statistics() {

    var title = "Statistics";

    var alertArea;
    var trhead;
    var tableSummary;
    var tableGroups;
    var user;
    var firebaseToken;
    var div;
    var groupNodes = {};
    var ref;
    var chartNode;
    var groupsChart;
    var groupsStat;
    var usersChart;
    var usersStat;

    var initInterface = function() {
        u.require("https://www.google.com/jsapi")
            .then(function () {
                google.load("visualization", "1", { "callback": renderInterface, "packages": ["corechart", "line"] });
            })
            .catch(function(){
                console.log("FAIL");
            })
    };

    function renderInterface() {

        div = document.getElementsByClassName("layout")[0];
        u.clear(div);
//        u.create("div", {className:"summary"}, div);
//        u.create("h2", "Groups", div);
        ref = database.ref();

        u.create(HTML.H2, "Summary", div);

        tableSummary = u.table({
            className: "option"
        }, div);
        tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Groups" },
                { className:"option", innerHTML: 0 }
            ]
        });
        tableSummary.groupsCreatedPersistentItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; created persistent" },
                { className:"option", innerHTML: 0 }
            ]
        });
        tableSummary.groupsCreatedTemporaryItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; created temporary" },
                { className:"option", innerHTML: 0 },
            ]
        });
        tableSummary.groupsDeletedItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; deleted" },
                { className:"option", innerHTML: 0 },
            ]
        });
        tableSummary.groupsRejectedItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; rejected" },
                { className:"option", innerHTML: 0 },
            ]
        });
        tableSummary.add({
            cells: [
                { className:"th", innerHTML: "Users" },
                { className:"option", innerHTML: 0 }
            ]
        });
        tableSummary.isersJoinedItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; joined" },
                { className:"option", innerHTML: 0 },
            ]
        });
        tableSummary.isersReconnectedItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; reconnected" },
                { className:"option", innerHTML: 0 },
            ]
        });
        tableSummary.isersRejectedItem = tableSummary.add({
            cells: [
                { className:"th", innerHTML: "&#150; rejected" },
                { className:"option", innerHTML: 0 },
            ]
        });

        groupsChartNode = u.create(HTML.DIV, null, div);
        usersChartNode = u.create(HTML.DIV, null, div);

       // Create the data table.
        groupsStat = new google.visualization.DataTable();
        groupsStat.addColumn('string', 'Date');
        groupsStat.addColumn('number', 'Persistent groups created');
        groupsStat.addColumn('number', 'Temporary groups created');
        groupsStat.addColumn('number', 'Deleted');
        groupsStat.addColumn('number', 'Rejected');
        groupsStat.addRow(["a",0,0,0,0]);
//        groupsStat.addRows([
//            ['2004',  1,1,1,1],
//            ['2005',  2,3,4,2],
//            ['2006',  1,4,12,2],
//            ['2007',  5,4,6,23]
//        ]);

        // Set chart options
        var groupsChartOptions = {
                  title: "Groups",
//                  legend: { position: "bottom" },
                };

       // Create the data table.
        usersStat = new google.visualization.DataTable();
        usersStat.addColumn('string', 'Date');
        usersStat.addColumn('number', 'Joined');
        usersStat.addColumn('number', 'Reconnected');
        usersStat.addColumn('number', 'Rejected');
        usersStat.addRow(["a",0,0,0]);
//        usersStat.addRows([
//            ['2004',  1,1,1],
//            ['2005',  2,3,4],
//            ['2006',  1,4,1],
//            ['2007',  5,4,6]
//        ]);

        // Set chart options
        var usersChartOptions = {
                  title: "Users",
//                  legend: { position: "bottom" },
                };

        // Instantiate and draw our chart, passing in some options.
        groupsChart = new google.charts.Line(groupsChartNode);
        google.visualization.events.addOneTimeListener(groupsChart, "ready", function(){
            usersChart = new google.charts.Line(usersChartNode);
            google.visualization.events.addOneTimeListener(usersChart, "ready", function(){
                updateData();
            });
            usersChart.draw(usersStat, google.charts.Line.convertOptions(usersChartOptions));

        });
        groupsChart.draw(groupsStat, google.charts.Line.convertOptions(groupsChartOptions));




        u.create(HTML.H2, "Groups", div);

        tableGroups = u.table({
            id: "groups",
            caption: {
                items: [
                    { label: "ID" },
                    { label: "Requires password", className: "media-hidden" },
                    { label: "Persistent", className: "media-hidden" },
                    { label: "Time to live, min", className: "media-hidden" },
                    { label: "Dismiss inactive, sec", className: "media-hidden" },
                    { label: "Users" },
                    { label: "Created", className: "media-hidden" },
                    { label: "Updated" }
                ]
            },
            placeholder: "No data, try to refresh page."
        }, div);

        u.create("br", null, div);
        buttons = u.create("div", {className:"buttons"}, div);
        renderButtons(buttons);
    }


    function updateData(){

        var initial = true;
        setTimeout(function(){initial = false;}, 3000);
        var resign = true;

        tableGroups.placeholder.show();
        u.clear(tableGroups.body);

        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_BY_DATE).off();
        ref.child(DATABASE.SECTION_STAT).child(DATABASE.STAT_BY_DATE).on("child_added", function(data) {
            resign = false;

            var json = data.val();

            var groupsData = ["",0,0,0,0];
            var usersData = ["",0,0,0];

            groupsData[0] = data.key;
            if(json[DATABASE.STAT_GROUPS_CREATED_PERSISTENT]) {
                groupsData[1] = json[DATABASE.STAT_GROUPS_CREATED_PERSISTENT];
            }
            if(json[DATABASE.STAT_GROUPS_CREATED_TEMPORARY]) {
                groupsData[2] = json[DATABASE.STAT_GROUPS_CREATED_TEMPORARY];
            }
            if(json[DATABASE.STAT_GROUPS_DELETED]) {
                groupsData[3] = json[DATABASE.STAT_GROUPS_DELETED];
            }
            if(json[DATABASE.STAT_GROUPS_REJECTED]) {
                groupsData[4] = json[DATABASE.STAT_GROUPS_REJECTED];
            }

            usersData[0] = data.key;
            if(json[DATABASE.STAT_USERS_JOINED]) {
                usersData[1] = json[DATABASE.STAT_USERS_JOINED];
            }
            if(json[DATABASE.STAT_USERS_RECONNECTED]) {
                usersData[2] = json[DATABASE.STAT_USERS_RECONNECTED];
            }
            if(json[DATABASE.STAT_USERS_REJECTED]) {
                usersData[3] = json[DATABASE.STAT_USERS_REJECTED];
            }

            groupsStat.addRow(groupsData);
            groupsChart.draw(groupsStat);

            usersStat.addRow(usersData)
            usersChart.draw(usersStat);
            console.log(data.val())

        }, function(e) {
            console.warn("Resign because of",e.message);
            resign = true;
            WTU.resign(updateData);
        });
        ref.child(DATABASE.SECTION_GROUPS).on("child_removed", function(data) {
            for(var i in tableGroups.rows) {
                if(tableGroups.rows[i].id == data.key) {
                    tableGroups.body.removeChild(tableGroups.rows[i]);
                    tableGroups.rows.splice(i,1);
                }
            }
            u.toast.show("Group "+data.key+" was removed.");
             updateTableSummary()
       }, function(error){
            console.error("REMOVED",error);

        })
    }

    function renderButtons(div) {
        u.clear(div);
        u.create(HTML.BUTTON, { innerHTML:"Clean groups", onclick: cleanGroupsQuestion}, div);
    }

    function cleanGroupsQuestion(e){
        u.clear(buttons);
        u.create({className:"question", innerHTML: "This will check expired users and groups immediately using each group options. Continue?"}, buttons);
        u.create(HTML.BUTTON,{ className:"question", innerHTML:"Yes", onclick: function() {
           renderButtons(buttons);
           u.toast.show("Groups clean is performing.");
           u.get("/admin/rest/v1/groups/clean")
            .then(function(xhr){
//               WTU.switchTo("/admin/groups");
            }).catch(function(code,xhr){
//               console.warn("Resign because of",code,xhr);
//               WTU.resign(updateData);
               var res = JSON.parse(xhr.responseText) || {};
               u.toast.show(res.message || xhr.statusText);
             });
        }}, buttons);
        u.create(HTML.BUTTON,{ innerHTML:"No", onclick: function(){
            renderButtons(buttons);
        }}, buttons);
    }

    function updateTableSummary() {

        tableSummary.groupsItem.lastChild.innerHTML = tableGroups.rows.length;

        var usersTotal = 0, usersOnline = 0, groupPersistent = 0;
        for(var i in tableGroups.rows) {
            if(tableGroups.rows[i].cells[2].innerHTML == "Yes") groupPersistent ++;

            var users = tableGroups.rows[i].cells[5].innerHTML;
            users = users.split("/");
            if(users.length > 1) {
                usersOnline += +users[0];
                usersTotal += +users[1];
            }
        }
        tableSummary.groupsPersistentItem.lastChild.innerHTML = groupPersistent;
        tableSummary.usersTotalItem.lastChild.innerHTML = usersTotal;
        tableSummary.usersOnlineItem.lastChild.innerHTML = usersOnline;

    }

    return {
        start: function() {
            initInterface();
//            updateData();
        },
        page: "statistics",
        icon: "trending_up",
        title: title,
        menu: title,
        move: true,
    }
}


