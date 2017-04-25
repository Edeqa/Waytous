/**
 * Created 1/19/17.
 */
function Groups() {

    var title = "Groups";

    var alertArea;
    var trhead;
    var table;
    var user;
    var firebaseToken;
    var div;
    var groupNodes = {};
    var ref;

    var renderInterface = function() {

        div = document.getElementsByClassName("layout")[0];
        u.clear(div);
//        u.create("div", {className:"summary"}, div);
//        u.create("h2", "Groups", div);
        ref = database.ref();

        table = u.table({
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

    }


    function updateData(){

        var initial = true;
        setTimeout(function(){initial = false;}, 3000);
        var resign = true;

        table.placeholder.show();

        ref.child(DATABASE.SECTION_GROUPS).off();
        ref.child(DATABASE.SECTION_GROUPS).on("child_added", function(data) {
            resign = false;
            ref.child(data.key).child(DATABASE.SECTION_OPTIONS).once("value").then(function(snapshot) {
                if(!snapshot || !snapshot.val()) return;

                var row = table.add({
                    className: "highlight",
                    onclick: function(){
                        WTU.switchTo("/admin/group/"+data.key);
                        return false;
                     },
                    cells: [
                        { innerHTML: data.key },
                        { className: "media-hidden", innerHTML:snapshot.val()["requires-password"] ? "Yes" : "No" },
                        { className: "media-hidden", innerHTML:snapshot.val().persistent ? "Yes" : "No" },
                        { className: "media-hidden", innerHTML:snapshot.val().persistent ? "&#150;" : snapshot.val()["time-to-live-if-empty"] },
                        { className: "media-hidden", innerHTML:snapshot.val()["dismiss-inactive"] ? snapshot.val()["delay-to-dismiss"] : "&#150;" },
                        { innerHTML:"..." },
                        { className: "media-hidden", sort: snapshot.val()["date-created"], innerHTML:snapshot.val()["date-created"] ? new Date(snapshot.val()["date-created"]).toLocaleString() : "&#150;" },
                        { sort: 0, innerHTML:"..." }
                    ]
                });
                var usersNode = row.cells[5]
                var changedNode = row.cells[7]

                ref.child(data.key).child(DATABASE.SECTION_USERS_DATA).on("value", function(snapshot){
                    if(!snapshot.val()) return;
                    usersNode.innerHTML = snapshot.val().length;
                    var changed = 0;
                    for(var i in snapshot.val()) {
                        var c = parseInt(snapshot.val()[i].changed);
                        if(c > changed) changed = c;
                    }
                    changedNode.sort = changed;
                    changedNode.innerHTML = new Date(changed).toLocaleString();
                    if(!initial) row.classList.add("changed");
                    setTimeout(function(){row.classList.remove("changed")}, 5000);
                    table.update();
                });
            }).catch(function(error){
                console.error(error);
                table.placeholder.show();
            });
        }, function(e) {
//            if(resign) {
//                console.error(e);
//                return;
//            }
            console.warn("Resign because of",e.message);
            resign = true;
            WTU.resign(updateData);
        });
    }


    return {
        start: function() {
            renderInterface();
            updateData();
        },
        page: "groups",
        icon: "group",
        title: title,
        menu: title,
        move:true,
    }
}


