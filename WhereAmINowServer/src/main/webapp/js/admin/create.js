/**
 * Created 1/19/17.
 */
function Create() {

    var title = "Create group";

    var inputId,inputRequiresPassword,inputPassword,inputWelcomeMessage,inputPersistent,inputTtl,inputDismissInactive,inputDelay;

    var u = new Utils();

    var start = function() {

        div = u.createPage(this);

        var form = u.create("form", null, div);

        var table = u.create("div", {className: "table option" }, form);

        var tbody = u.create("div", {className:"tbody"}, table);

        var tr = u.create("div", {className:"tr"}, tbody);
        u.create("div", {className:"th option", innerHTML:"ID"}, tr);
        inputId = u.create("input", { oninput: validate_id }, u.create("div", {className:"td option"}, tr));

        var tr = u.create("div", {className:"tr"}, tbody);
        u.create("div", {className:"th option", innerHTML:"Requires password"}, tr);
        inputRequiresPassword = u.create("input", { type:"checkbox", onchange: function() {
            trPassword.style.display = this.checked ? "" : "none";
            inputPassword.focus();
        } }, u.create("div", {className:"td option"}, tr));

        var trPassword = u.create("div", {className:"tr", style:{display:"none"}}, tbody);
        u.create("div", {className:"th option", innerHTML:"Password"}, trPassword);
        inputPassword = u.create("input", { type:"password" }, u.create("div", {className:"td option"}, trPassword));

        var tr = u.create("div", {className:"tr"}, tbody);
        u.create("div", {className:"th option", innerHTML:"Welcome message"}, tr);
        inputWelcomeMessage = u.create("input", {}, u.create("div", {className:"td option"}, tr));

        var tr = u.create("div", {className:"tr"}, tbody);
        u.create("div", {className:"th option", innerHTML:"Persistent group"}, tr);
        inputPersistent = u.create("input", { type:"checkbox", onchange: function() {
            trTtl.style.display = this.checked ? "none" : ""
        } }, u.create("div", {className:"td option"}, tr));

        var trTtl = u.create("div", {className:"tr"}, tbody);
        u.create("div", {className:"th option", innerHTML:"Time to live, min"}, trTtl);
        inputTtl = u.create("input", { oninput: validate_ttl }, u.create("div", {className:"td option"}, trTtl));

        var tr = u.create("div", {className:"tr"}, tbody);
        u.create("div", {className:"th option", innerHTML:"Dismiss inactive users"}, tr);
        inputDismissInactive = u.create("input", { type:"checkbox", onchange: function() {
            trDelay.style.display = this.checked ? "" : "none";
            inputDelay.focus();
        } }, u.create("div", {className:"td option"}, tr));

        var trDelay = u.create("div", {className:"tr", style: {display:"none"}}, tbody);
        u.create("div", {className:"th option", innerHTML:"Delay to dismiss, sec"}, trDelay);
        inputDelay = u.create("input", { onchange: validate_delay , title:"Minimum 300"}, u.create("div", {className:"td option"}, trDelay));

        var div = u.create("div", {className:"buttons"}, div);
        u.create("button", { type: "button", innerHTML:"OK", onclick: validate_submit}, div);
        u.create("button", { type: "reset", innerHTML:"Clear"}, div);

        inputId.focus();

    }

    var validate_id = function(e) {
        this.value = this.value.toUpperCase().replace(/[^\w]/g, "");
    }

    var validate_ttl = function(e) {
        this.value = this.value.replace(/[^\d]/g, "");
    }

    var validate_delay = function(e) {
        this.value = this.value.replace(/[^\d]/g, "");
        if(this.value < 300) this.value = 300;
    }

    var validate_submit = function(e) {

        validate_id.call(inputId);
        validate_ttl.call(inputTtl);
        validate_delay.call(inputDelay);

        var ref = database.ref();

        ref.child("_groups").child(inputId.value).set(0);

        ref.child(inputId.value).child("u/b/0/active").set(false);
        ref.child(inputId.value).child("u/k/0").set(0);
        ref.child(inputId.value).child("u/p/0/key").set(0);

        ref.child(inputId.value).child("o").set({
            "date-created": firebase.database.ServerValue.TIMESTAMP,
            "requires-password": inputRequiresPassword.checked,
            "password": inputPassword.value ? inputPassword.value : null,
            "welcome-message": inputWelcomeMessage.value,
            "persistent": inputPersistent.checked,
            "time-to-live-if-empty": inputTtl.value,
            "dismiss-inactive": inputDismissInactive.value,
            "delay-to-dismiss": inputDelay.value
        }).then(function(){
            WAIN.switchTo("/admin/groups");
        });

//        window.location.href = "/admin/groups";

//        if(window.name == "content") {
//            window.parent.history.pushState({}, null, "/admin/create");
//        }

        return false;
    }


    return {
        start: start,
        page: "create",
        icon: "add",
        title: title,
        menu: title,
    }
}
document.addEventListener("DOMContentLoaded", (new Create()).start);