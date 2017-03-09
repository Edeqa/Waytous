/**
 * Created 1/19/17.
 */
function Settings() {

    var title = "Settings";

    var u = new Utils();

    var start = function() {
        div = u.createPage(this);

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
document.addEventListener("DOMContentLoaded", (new Settings()).start);
