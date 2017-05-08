/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 1/19/17.
 */
function Create() {

    var title = "Create group";
    var dialog;

    var inputId,inputRequiresPassword,inputPassword,inputWelcomeMessage,inputPersistent,inputTtl,inputDismissInactive,inputDelay;

    var start = function() {

        div = document.getElementsByClassName("layout")[0];
        dialog = dialog || u.dialog({
            title: "Create group",
            className: "create-dialog",
            items: [
                {
                    type: HTML.INPUT,
                    label: u.create(HTML.DIV, "ID ").place(HTML.DIV, {className: "create-dialog-button-generate", innerHTML:"Generate ID", onclick(){
                        dialog.items[0].value = Math.random().toString(32).toUpperCase().replace(/0\./,"");
                    }}),
                    oninput: validate_id
                },
                { type: HTML.CHECKBOX, label: "Requires password", onchange: function() {
                       dialog.items[2].parentNode[this.checked ? "show" : "hide"]();
                       dialog.items[2].focus();
                } },
                { type: HTML.PASSWORD, itemClassName: "hidden", label: "Password" },
                { type: HTML.INPUT, label: "Welcome message" },
                { type: HTML.CHECKBOX, label: "Persistent group", onchange: function() {
                      dialog.items[5].parentNode[this.checked ? "hide" : "show"]();
                      dialog.items[5].focus();
                } },
                { type: HTML.INPUT, label: "Time to live, min", oninput: validate_ttl },
                { type: HTML.CHECKBOX, label: "Dismiss inactive users", onchange: function() {
                    dialog.items[7].parentNode[this.checked ? "show" : "hide"]();
                    dialog.items[7].focus();
                } },
                { type: HTML.INPUT, itemClassName: "hidden", label: "Delay to dismiss, sec", title:"Minimal value 300", onchange: validate_delay, oninput: validate_delay },
            ],
            positive: {
                label: "OK",
                onclick: validate_submit
            },
            negative: {
                label: "Cancel"
            },
            help: function() {
                console.log("HELP");
            }
        });
        dialog.open();
        inputId = dialog.items[0];
        inputRequiresPassword = dialog.items[1];
        inputPassword = dialog.items[2];
        inputWelcomeMessage = dialog.items[3];
        inputPersistent = dialog.items[4];
        inputTtl = dialog.items[5];
        inputDismissInactive = dialog.items[6];
        inputDelay = dialog.items[7];

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

        if(!inputId.value) return;

        var ref = database.ref();

        ref.child(DATABASE.SECTION_GROUPS).child(inputId.value).set(0);

        ref.child(inputId.value).child(DATABASE.SECTION_USERS_DATA).child(0).child(DATABASE.USER_ACTIVE).set(false);
        ref.child(inputId.value).child(DATABASE.SECTION_USERS_KEYS).child(0).set(0);
//        ref.child(inputId.value).child(DATABASE.SECTION_USERS_ORDER).push().set(0);
        ref.child(inputId.value).child(DATABASE.SECTION_USERS_DATA_PRIVATE).child(0).child("key").set(0);

        ref.child(inputId.value).child(DATABASE.SECTION_OPTIONS).set({
            "date-created": firebase.database.ServerValue.TIMESTAMP,
            "requires-password": inputRequiresPassword.checked,
            "password": inputPassword.value ? inputPassword.value : null,
            "welcome-message": inputWelcomeMessage.value,
            "persistent": inputPersistent.checked,
            "time-to-live-if-empty": inputTtl.value,
            "dismiss-inactive": inputDismissInactive.value,
            "delay-to-dismiss": inputDelay.value
        }).then(function(){
            WTU.switchTo("/admin/groups");
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
        icon: "group_add",
        title: title,
        menu: title,
    }
}
