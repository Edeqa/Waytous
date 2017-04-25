/**
 * Created 1/19/17.
 */
function Settings() {

    var title = "Settings";
    var dialog;

    var start = function() {
        div = document.getElementsByClassName("layout")[0];

        dialog = dialog || u.dialog({
            title: "Settings",
            className: "settings-dialog",
            items: [
                { type: HTML.DIV, label: "To be implemented soon..." },
            ],
            positive: {
                label: "OK",
            },
        }, div);
        dialog.open();

   }

    return {
        start: start,
        page: "settings",
        icon: "settings",
        title: title,
        menu: title,
    }
}
