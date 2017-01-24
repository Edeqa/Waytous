/**
 * Created 1/19/17.
 */
function Help() {

    var u = new Utils();

    var start = function() {
        if(window.name == "content") {
            window.parent.history.pushState({}, null, "/admin/help");
        }

        u.clear(document.body);

        u.create("h1", "Help", document.body);

        u.create("p", "To be implemented soon...", document.body);

    }

    return {
        start: start,
        page: "help",
        icon: "help_outline",
        title: "Help",
        menu: true,
    }
}
document.addEventListener("DOMContentLoaded", (new Help()).start);