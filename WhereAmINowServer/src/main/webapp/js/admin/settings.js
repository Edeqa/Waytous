/**
 * Created 1/19/17.
 */
function Settings() {

    var u = new Utils();

    var start = function() {
        if(window.name == "content") {
            window.parent.history.pushState({}, null, "/admin/settings");
        }

        u.clear(document.body);

        u.create("h1", "Settings", document.body);

        u.create("p", "To be implemented soon...", document.body);
   }

    return {
        start: start,
        page: "settings",
        icon: "settings",
        title: "Settings",
        menu: true,
    }
}
document.addEventListener("DOMContentLoaded", (new Settings()).start);