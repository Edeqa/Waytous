/**
 * Created 1/19/17.
 */
function Create() {

    var inputId,inputRequiresPassword,inputPassword,inputWelcomeMessage,inputPersistent,inputTtl;

    var u = new Utils();

    var start = function() {

        if(window.name == "content") {
            window.parent.history.pushState({}, null, "/admin/create");
        }

        u.clear(document.body);

        u.create("h1", "Create", document.body);

        u.create("h2", "Create group", document.body);

        var div = u.create("div", null, document.body);

        var form = u.create("form", null, div);

        var table = u.create("table", { className: "create" }, form);

        var tbody = u.create("thead", null, table);

        var tr = u.create("tr", null, tbody);
        u.create("th", "ID", tr);
        inputId = u.create("input", { oninput: validate_id }, u.create("td", null, tr));

        var tr = u.create("tr", null, tbody);
        u.create("th", "Requires password", tr);
        u.create("input", { type:"checkbox", onchange: function() {
            trPassword.style.display = this.checked ? "" : "none";
            inputPassword.focus();
        } }, u.create("td", null, tr));

        var trPassword = u.create("tr", {style:{display:"none"}}, tbody);
        u.create("th", "Password", trPassword);
        inputPassword = u.create("input", { type:"password" }, u.create("td", null, trPassword));

        var tr = u.create("tr", null, tbody);
        u.create("th", "Welcome message", tr);
        inputWelcomeMessage = u.create("input", {}, u.create("td", null, tr));

        var tr = u.create("tr", null, tbody);
        u.create("th", "Persistence group", tr);
        inputPersistent = u.create("input", { type:"checkbox", onchange: function() {
            trTtl.style.display = this.checked ? "none" : ""
        } }, u.create("td", null, tr));

        var trTtl = u.create("tr", null, tbody);
        u.create("th", "Time to live, min", trTtl);
        inputTtl = u.create("input", { oninput: validate_ttl }, u.create("td", null, trTtl));

        var div = u.create("div", null, document.body);
        u.create("button", { type: "button", innerHTML:"OK", onclick: validate_submit}, div);
        u.create("button", { type: "reset", innerHTML:"Clear"}, div);

        inputId.focus();

    }

    var validate_id = function(e) {
        this.value = this.value.toUpperCase().replace(/[^\w]/g, "");
        console.log(this.value)
    }

    var validate_ttl = function(e) {
        this.value = this.value.replace(/[^\d]/g, "");
        console.log(this.value)
    }

    var validate_submit = function(e) {
        console.log(inputId.value);

        window.location.href = "/admin/summary" + (window.name == "content" ? "/set" : "");

//        if(window.name == "content") {
//            window.parent.history.pushState({}, null, "/admin/create");
//        }

        return false;
    }


    return {
        start: start,
        page: "create",
        icon: "add",
        title: "Create group",
        menu: true,
    }
}
document.addEventListener("DOMContentLoaded", (new Create()).start);