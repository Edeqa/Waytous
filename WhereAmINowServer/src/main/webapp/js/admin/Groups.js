/**
 * Created 1/19/17.
 */
function Groups() {

    var title = "Groups";

    var alertArea;
    var trhead;
    var tbody;
    var user;
    var firebaseToken;
    var div;
    var groupNodes = {};

    var u = new Utils();

    var renderInterface = function() {

//        div.appendChild(renderAlertArea());

        div.appendChild(renderInterfaceHeader());

    }

    var renderInterfaceHeader = function () {

        var div = u.create("div", {className:"summary"});
//        u.create("h2", "Groups", div);

        var table = u.create("div", {id:"groups", className:"summary table"}, div);

        var thead = u.create("div", { className:"thead"}, table);
        trhead = u.create("div", {className:"tr"}, thead);

        u.create("div", {className:"th", innerHTML:"ID"}, trhead);
        u.create("div", {className:"th hideable", innerHTML:"Requires password"}, trhead);
        u.create("div", {className:"th hideable", innerHTML:"Persistent"}, trhead);
        u.create("div", {className:"th hideable", innerHTML:"Time to live, min"}, trhead);
        u.create("div", {className:"th hideable", innerHTML:"Dismiss inactive, sec"}, trhead);
        u.create("div", {className:"th", innerHTML:"Users"}, trhead);
        u.create("div", {className:"th hideable", innerHTML:"Created"}, trhead);
        u.create("div", {className:"th", innerHTML:"Updated"}, trhead);

        tbody = u.create("div", {className:"tbody"}, table);
        u.create("div", {
            className:"td",
            colspan: trhead.childElementCount,
            align: "center",
            innerHTML: "Loading..."
        }, u.create("tr", {}, tbody));

        return div;

    }


    function updateData(){

        var ref = database.ref();
        u.clear(tbody);
        ref.child("_groups").on("child_added", function(data) {
            ref.child(data.key).child("o").once("value").then(function(snapshot) {
                if(!snapshot || !snapshot.val()) return;
                var tr = u.create("div", { className: "tr clickable changeable", onclick: function(){
                   WAIN.switchTo("/admin/group/"+data.key);
                   return false;
                }}, tbody);

                u.create("div", {className: "td", innerHTML:data.key}, tr);
                u.create("div", {className: "td hideable", innerHTML:snapshot.val()["requires-password"] ? "Yes" : "No"}, tr);
                u.create("div", {className: "td hideable", innerHTML:snapshot.val().persistent ? "Yes" : "No"}, tr);
                u.create("div", {className: "td hideable", innerHTML:snapshot.val().persistent ? "&#150;" : snapshot.val()["time-to-live-if-empty"]}, tr);
                u.create("div", {className: "td hideable", innerHTML:snapshot.val()["dismiss-inactive"] ? snapshot.val()["delay-to-dismiss"] : "&#150;"}, tr);
                var usersNode = u.create("div", {className: "td", innerHTML:"..."}, tr);
                u.create("div", {className: "td hideable", innerHTML:snapshot.val()["date-created"] ? new Date(snapshot.val()["date-created"]).toLocaleString() : "&#150;"}, tr);
                var changedNode = u.create("div", {className: "td", innerHTML:"..."}, tr);

                ref.child(data.key).child("u/b").on("value", function(snapshot){
                    if(!snapshot.val()) return;
                    usersNode.innerHTML = snapshot.val().length;
                    var changed = 0;
                    for(var i in snapshot.val()) {
                        var c = parseInt(snapshot.val()[i].changed);
                        if(c > changed) changed = c;
                    }
                    changedNode.innerHTML = new Date(changed).toLocaleString();
                    tr.classList.add("changed");
                    setTimeout(function(){tr.classList.remove("changed")}, 2000);
                });
            }).catch(function(error){
                u.clear(tbody);
                u.create("div", {className: "td", colspan: trhead.childElementCount, innerHTML: "Error loading data, try to refresh page."}, u.create("tr", {}, tbody));
            });

        });
    }


    return {
        start: function() {
            div = u.createPage(this);

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


