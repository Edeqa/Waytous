/**
 * Created 4/20/17.
 */
function Logs() {

    var title = "Logs";

    var renderInterface = function() {

        div = document.getElementsByClassName("content")[0];
        u.clear(div);
//        u.create("div", {className:"summary"}, div);
//        u.create("h2", "Groups", div);

        table = u.table({
            id: "logs",
            className: "table-logs",
            placeholder: "Loading..."
        }, div);

    }


    function updateData(){

        u.getRemote("/admin/logs/log").then(function(xhr){
            console.log(xhr);
        });

    }


    return {
        start: function() {
            renderInterface();
            updateData();
        },
        page: "logs",
        icon: "receipt",
        title: title,
        menu: title,
        move:true,
    }
}


