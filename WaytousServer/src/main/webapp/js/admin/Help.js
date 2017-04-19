/**
 * Created 1/19/17.
 */
function Help() {

    var title = "Help";

    var u = new Utils();

    var start = function() {
        div = document.getElementsByClassName("content")[0];

        u.create("p", "To be implemented soon...", div);

    }

    return {
        start: start,
        page: "help",
        icon: "help_outline",
        title: title,
        menu: title,
    }
}
document.addEventListener("DOMContentLoaded", (new Help()).start);
