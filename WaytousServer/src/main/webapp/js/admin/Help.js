/**
 * Created 1/19/17.
 */
function Help() {

    var title = "Help";

    var start = function() {
        div = document.getElementsByClassName("content")[0];
        u.clear(div);

        u.create("p", "To be implemented soon...", div);

    }

    return {
        start: start,
        page: "help",
        icon: "help_outline",
        title: title,
        menu: title,
        move:true,
    }
}