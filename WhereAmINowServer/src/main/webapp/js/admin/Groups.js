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

    var u = new Utils();

    var renderInterface = function() {

        div = document.getElementsByClassName("content")[0];

//        u.create("div", {className:"summary"}, div);
//        u.create("h2", "Groups", div);

        table = u.table({
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
            }
        }, div);

    }


    function updateData(){

        var ref = database.ref();
        u.clear(table.body);
        ref.child(DATABASE.SECTION_GROUPS).on("child_added", function(data) {
            ref.child(data.key).child(DATABASE.SECTION_OPTIONS).once("value").then(function(snapshot) {
                if(!snapshot || !snapshot.val()) return;

                var row = table.add({
                    className: "highlight",
                    onclick: function(){
                        WAIN.switchTo("/admin/group/"+data.key);
                        return false;
                     },
                    cells: [
                        { innerHTML: data.key },
                        { className: "media-hidden", innerHTML:snapshot.val()["requires-password"] ? "Yes" : "No" },
                        { className: "media-hidden", innerHTML:snapshot.val().persistent ? "Yes" : "No" },
                        { className: "media-hidden", innerHTML:snapshot.val().persistent ? "&#150;" : snapshot.val()["time-to-live-if-empty"] },
                        { className: "media-hidden", innerHTML:snapshot.val()["dismiss-inactive"] ? snapshot.val()["delay-to-dismiss"] : "&#150;" },
                        { innerHTML:"..." },
                        { className: "media-hidden", innerHTML:snapshot.val()["date-created"] ? new Date(snapshot.val()["date-created"]).toLocaleString() : "&#150;" },
                        { innerHTML:"..." }
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
                    changedNode.innerHTML = new Date(changed).toLocaleString();
                    row.classList.add("changed");
                    setTimeout(function(){row.classList.remove("changed")}, 2000);
                });
            }).catch(function(error){
                table.placeholder.show("Error loading data, try to refresh page.");
            });

        });
    }


    return {
        start: function() {
            renderInterface();
            updateData();
        },
        page: "groups",
        icon: "list",
        title: title,
        menu: title,
    }
}
document.addEventListener("DOMContentLoaded", (new Groups()).start);


