/**
 * Created 1/19/17.
 */
function Settings() {

    var title = "Settings";

    var start = function() {
        div = document.getElementsByClassName("content")[0];

        u.create("p", "To be implemented soon...", div);
   }

    return {
        start: start,
        page: "settings",
        icon: "settings",
        title: title,
        menu: title,
    }
}
