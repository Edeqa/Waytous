/**
 * Created 1/19/17.
 */
function Home() {

    var u = new Utils();

    var start = function() {
        if(window.name == "content") {
            window.parent.history.pushState({}, null, "/admin/home");
        }

        u.clear(document.body);

        u.create("h1", "Home", document.body);
        u.create("p", "To be implemented soon...", document.body);

    }

    return {
        start: start,
        icon: "home",
        title: "Home",
    }
}
document.addEventListener("DOMContentLoaded", (new Home()).start);