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

        var divHeader = u.create(HTML.DIV, {className: "logs-header"}, div);

        u.create(HTML.BUTTON, {innerHTML:"Refresh", onclick: function(){
            updateData();
        }}, divHeader);
        table = u.table({
            id: "logs",
            caption: {
                className: "table-logs-caption",
                items: [
                    { className: "table-logs-caption-cell", label: "Logs" },
                ]
            },
            className: "table-logs",
            bodyClassName: "table-logs-body",
            placeholder: "Loading..."
        }, div);

    }


    function updateData(){

        table.placeholder.show("Loading...");
        u.get("/admin/logs/log").then(function(xhr){
            table.head.cells[0].lastChild.innerHTML = "Logs (updated "+(new Date().toLocaleString())+")";
            var rows = xhr.response.split("\n");
            for(var i in rows) {
                table.add({
                  className: "table-logs-row",
                  cells: [
                      { className: "table-logs-row-cell", innerHTML: rows[i] },
                  ]
              });
            }
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


