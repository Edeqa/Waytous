/**
 * Created 2/8/17.
 */
EVENTS.UPDATE_ACTIONBAR_SUBTITLE = "update_actionbar_subtitle";

function DrawerHolder(main) {

    var drawer;
    var title;
    var subtitle;
    var alphaDialog;
    var backButtonAction;
    var actionbar;


    var target = window; // this can be any scrollable element
    var last_y = 0;

    var start = function() {

        var dialogAbout = utils.dialogAbout(main.right);
        drawer = new u.drawer({
            title: main.appName,
            subtitle: u.lang.be_always_on_the_same_way,
            logo: {
                src:"/images/logo.svg",
                onclick: function(){
                    alphaDialog.open();
                }
            },
            onwidthtoggle: function() {
                main.fire(EVENTS.CAMERA_UPDATE);
            },
            onprimaryclick: function(){
                main.me.fire(EVENTS.SELECT_SINGLE_USER);
            },
            footer: {
                className: "drawer-footer-label",
                content: u.create(HTML.DIV).place(HTML.A, {innerHTML: "${APP_NAME} &copy;2017 Edeqa", href: "", onclick: function(e){
                    dialogAbout.open();
                    e.preventDefault();
                    e.stopPropagation;
                    return false;
                }}).place(HTML.SPAN, "\nBuild " + data.version)
            }
        }, main.layout);

        actionbar = u.actionBar({
            title: main.appName,
            onbuttonclick: function(){
                 try {
                     drawer.open();
                 } catch(e) {
                     console.error(e);
                 }
             }
        }, main.right);

        setTimeout(function(){
            main.fire(EVENTS.CREATE_DRAWER, drawer);
        },0);

        window.history.pushState(null, document.title, location.href);
        backButtonAction = function (event) {
           window.history.pushState(null, document.title, location.href);
           drawer.toggle();
        }


////// FIXME - remove when no alpha
        alphaDialog = alphaDialog || u.dialog({
            className: "alert-dialog",
            items: [
                { type: HTML.DIV, innerHTML: u.lang.alpha_1 },
                { type: HTML.DIV, innerHTML: u.lang.alpha_2 },
                { type: HTML.DIV, innerHTML: u.lang.alpha_3 },
                { type: HTML.DIV, innerHTML: u.lang.alpha_4 },
                { type: HTML.DIV, innerHTML: u.lang.alpha_5 },
                { type: HTML.DIV, innerHTML: u.lang.alpha_6 },
                { type: HTML.DIV, innerHTML: u.lang.alpha_7 },
            ],
            positive: {
                label: u.lang.ok,
                onclick: function(){
                    alphaDialog.close();
                }
            },
        }, main.right);
        main.alpha.addEventListener("click", function(){
            alphaDialog.open();
        });

    };

    var onEvent = function(EVENT,object){
        switch (EVENT){
            case EVENTS.UPDATE_ADDRESS:
                if(main.users.getCountSelected() == 1 && this.properties.selected) {
                    actionbar.subtitle.innerHTML = object;
                    actionbar.subtitle.show();
                } else {
                    actionbar.subtitle.hide();
                }
                break;
            case EVENTS.TRACKING_ACTIVE:
                actionbar.titleNode.innerHTML = main.appName;
                drawer.headerTitle.innerHTML = main.appName;
                break;
            case EVENTS.TRACKING_DISABLED:
                actionbar.titleNode.innerHTML = main.appName;
                drawer.headerTitle.innerHTML = main.appName;
                window.removeEventListener("popstate", backButtonAction);
                break;
            case EVENTS.TRACKING_CONNECTING:
            case EVENTS.TRACKING_RECONNECTING:
                u.lang.updateNode(actionbar.titleNode, u.lang.connecting);
                u.lang.updateNode(drawer.headerTitle, u.lang.connecting);
                window.addEventListener("popstate", backButtonAction);
                break;
            case EVENTS.CHANGE_NAME:
            case USER.JOINED:
                if(main.me.properties && main.me.properties.getDisplayName) {
                    drawer.headerPrimary.innerHTML = main.me.properties.getDisplayName();
                }
                break;
            case EVENTS.SELECT_USER:
            case EVENTS.SELECT_SINGLE_USER:
                onChangeLocation.call(this, this.location)
                break;
        }
        return true;
    };

    function createView(user) {
        return {};
    }

    function onChangeLocation(location) {
        if(this && this.properties && this.properties.selected && main.users.getCountSelected() == 1) {
            actionbar.subtitle.show();
            this.fire(EVENTS.UPDATE_ACTIONBAR_SUBTITLE, actionbar.subtitle);
        }
    }

    function options(){
        return {
            id: "general",
            title: u.lang.general,
            categories: [
                {
                    id: "general:main",
                    title: u.lang.main,
                    items: [
                        {
                            id:"drawer:collapsed",
                            itemClassName: "media-hidden",
                            type: HTML.CHECKBOX,
                            label: u.lang.collapsed_drawer,
                            checked: u.load("drawer:collapsed"),
                            onaccept: function(e, event) {
                                drawer.toggleWidth(this.checked);
                            },
                        }
                    ]
                }
            ]
        }
    }

    return {
        type:"drawer",
        start:start,
        onEvent:onEvent,
        createView:createView,
        onChangeLocation:onChangeLocation,
        options:options,
    }
}

