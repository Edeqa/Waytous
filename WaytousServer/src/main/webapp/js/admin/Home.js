/**
 * Created 1/19/17.
 */
function Home() {

    var title = "Home";

    var start = function() {
        div = document.getElementsByClassName("content")[0];
        u.clear(div);

        u.create("p", "To be implemented soon...", div);

    }

    return {
        start: start,
        page: "home",
        icon: "home",
        title: title,
        menu: title,
        move:true,
    }
}