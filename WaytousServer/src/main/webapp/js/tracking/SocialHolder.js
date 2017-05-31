/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 3/10/17.
 */

function SocialHolder(main) {

    var type = "social";

    var shareDialogFacebook;
    var shareDialogWhatsapp;
    var drawerItemShareFacebook;
    var drawerItemShareWhatsapp;
    var shareBlockedDialog;
    var ic_facebook;

    var facebook_svg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"0 0 24 24",
        version:"1.1",
        className: "menu-item"
    };
    var facebook_path = {
        xmlns:"http://www.w3.org/2000/svg",
        strokeWidth:"2",
        fill:"transparent",
        d: "M17,2V2H17V6H15C14.31,6 14,6.81 14,7.5V10H14L17,10V14H14V22H10V14H7V10H10V6A4,4 0 0,1 14,2H17Z"
    };
    var whatsapp_svg = {
        xmlns:"http://www.w3.org/2000/svg",
        viewbox:"0 0 90 90",
        version:"1.1",
        className: "menu-item",

    };
    var whatsapp_path = {
        xmlns:"http://www.w3.org/2000/svg",
        fill: "darkslategray",
        d: "M90,43.841c0,24.213-19.779,43.841-44.182,43.841c-7.747,0-15.025-1.98-21.357-5.455L0,90l7.975-23.522 c-4.023-6.606-6.34-14.354-6.34-22.637C1.635,19.628,21.416,0,45.818,0C70.223,0,90,19.628,90,43.841z M45.818,6.982 c-20.484,0-37.146,16.535-37.146,36.859c0,8.065,2.629,15.534,7.076,21.61L11.107,79.14l14.275-4.537 c5.865,3.851,12.891,6.097,20.437,6.097c20.481,0,37.146-16.533,37.146-36.857S66.301,6.982,45.818,6.982z M68.129,53.938 c-0.273-0.447-0.994-0.717-2.076-1.254c-1.084-0.537-6.41-3.138-7.4-3.495c-0.993-0.358-1.717-0.538-2.438,0.537 c-0.721,1.076-2.797,3.495-3.43,4.212c-0.632,0.719-1.263,0.809-2.347,0.271c-1.082-0.537-4.571-1.673-8.708-5.333 c-3.219-2.848-5.393-6.364-6.025-7.441c-0.631-1.075-0.066-1.656,0.475-2.191c0.488-0.482,1.084-1.255,1.625-1.882 c0.543-0.628,0.723-1.075,1.082-1.793c0.363-0.717,0.182-1.344-0.09-1.883c-0.27-0.537-2.438-5.825-3.34-7.977 c-0.902-2.15-1.803-1.792-2.436-1.792c-0.631,0-1.354-0.09-2.076-0.09c-0.722,0-1.896,0.269-2.889,1.344 c-0.992,1.076-3.789,3.676-3.789,8.963c0,5.288,3.879,10.397,4.422,11.113c0.541,0.716,7.49,11.92,18.5,16.223 C58.2,65.771,58.2,64.336,60.186,64.156c1.984-0.179,6.406-2.599,7.312-5.107C68.398,56.537,68.398,54.386,68.129,53.938z"
    };

    function start() {

        window.fbAsyncInit = function() {
            FB.init({
              appId      : '1226806074062997',
              xfbml      : false,
              version    : 'v2.9'
            });
//            FB.AppEvents.logPageView();
            FB.ui({
              method: 'share_open_graph',
              action_type: 'og.likes',
              action_properties: JSON.stringify({
                object:'https://developers.facebook.com/docs/',
              })
            }, function(response){
              // Debug response (optional)
              console.log(response);
            });
          };

          (function(d, s, id){
             var js, fjs = d.getElementsByTagName(s)[0];
             if (d.getElementById(id)) {return;}
             js = d.createElement(s); js.id = id;
             js.src = "//connect.facebook.net/en_US/sdk.js";
             fjs.parentNode.insertBefore(js, fjs);
           }(document, 'script', 'facebook-jssdk'));
    }

    function onEvent(EVENT,object){
        switch (EVENT){
            case EVENTS.CREATE_DRAWER:
                ic_facebook = ic_facebook || u.create(HTML.PATH, facebook_path, u.create(HTML.SVG, facebook_svg)).parentNode;
                drawerItemShareFacebook = object.add(DRAWER.SECTION_SHARE,"facebook","Share to Facebook",ic_facebook,function(){
                    if(shareDialogFacebook) shareDialogFacebook.close();
                    shareDialogFacebook = shareDialogFacebook || u.dialog({
                        items: [
                            {type:HTML.DIV, innerHTML: u.lang.social_share_to_facebook_dialog },
                            {type:HTML.INPUT, className: "dialog-item-input-link", value: main.tracking.getTrackingUri(), readOnly:true }
                        ],
                        positive: {
                            label: u.lang.share,
                            onclick: function() {
                                FB.ui({
                                    method: "share",
                                    display: "popup",
                                    href: main.tracking.getTrackingUri(),
                                    caption: "Follow me at ${WEB_PAGE}",
                                }, function(response){
                                    console.log("A",response);
                                });
                            }
                        },
                        neutral: {
                            label: u.lang.copy,
                            dismiss: false,
                            onclick: function(items) {
                                if(u.copyToClipboard(items[1])) {
                                    main.toast.show(u.lang.link_was_copied_into_clipboard, 3000);
                                }
                                shareDialogFacebook.close();
                            }
                        },
                        negative: {
                            label: u.lang.cancel
                        },
                        timeout: 20000
                    }, main.right);
                    shareDialogFacebook.open();
                });
                drawerItemShareFacebook.hide();
                var ic_whatsapp = u.create(HTML.PATH, whatsapp_path, u.create(HTML.SVG, whatsapp_svg)).parentNode;
                drawerItemShareWhatsapp = object.add(DRAWER.SECTION_SHARE,"whatsapp","Share to Whatsapp",ic_whatsapp,function(){
                    if(shareDialogWhatsapp) shareDialogWhatsapp.close();
                    shareDialogWhatsapp = shareDialogWhatsapp || u.dialog({
                        items: [
                            {type:HTML.DIV, innerHTML: u.lang.social_share_to_whatsapp_dialog },
                            {type:HTML.INPUT, className: "dialog-item-input-link", value: main.tracking.getTrackingUri(), readOnly:true }
                        ],
                        positive: {
                            label: u.lang.share,
                            onclick: function() {
                                var popup = window.open("whatsapp://send?text=Way%20to%20us&body="+main.tracking.getTrackingUri(),"_blank");
                                utils.popupBlockerChecker.check(popup, function() {
                                    shareBlockedDialog = shareBlockedDialog || u.dialog({
                                        items: [
                                            {type:HTML.DIV, innerHTML: u.lang.popup_blocked_dialog_1 },
                                            {type:HTML.DIV, enclosed:true, innerHTML: u.lang.popup_blocked_dialog_2 },
                                            {type:HTML.DIV, innerHTML: u.lang.popup_blocked_dialog_3 },
                                            {type:HTML.DIV, innerHTML: main.tracking.getTrackingUri()}
                                        ],
                                        positive: {
                                            label: u.lang.close
                                        }
                                    }, main.right);
                                    shareBlockedDialog.open();
                                });

                                FB.ui({
                                    method: "share",
                                    display: "popup",
                                    href: main.tracking.getTrackingUri(),
                                    caption: "Follow me at ${WEB_PAGE}",
                                }, function(response){
                                    console.log("A",response);
                                });
                            }
                        },
                        neutral: {
                            label: u.lang.copy,
                            dismiss: false,
                            onclick: function(items) {
                                if(u.copyToClipboard(items[1])) {
                                    main.toast.show(u.lang.link_was_copied_into_clipboard, 3000);
                                }
                                shareDialogWhatsapp.close();
                            }
                        },
                        negative: {
                            label: u.lang.cancel
                        },
                        timeout: 20000
                    }, main.right);
                    shareDialogWhatsapp.open();
                });
                drawerItemShareWhatsapp.classList.add("desktop-hidden");
                drawerItemShareWhatsapp.hide();
                break;
            case EVENTS.TRACKING_CONNECTING:
            case EVENTS.TRACKING_RECONNECTING:
            case EVENTS.TRACKING_ACTIVE:
                drawerItemShareFacebook.show();
                drawerItemShareWhatsapp.show();
                break;
            case EVENTS.TRACKING_DISABLED:
                drawerItemShareFacebook.hide();
                drawerItemShareWhatsapp.hide();
                break;
            default:
                break;
        }
        return true;
    }

    function createView(myUser){
        var view = {};
        view.user = myUser;

        view.show = u.load("track:show:" + myUser.number);

        if(view.show) {
            show.call(myUser);
        }
        drawerPopulate();
        return view;
    }


    function onChangeLocation(location) {
        show.call(this);
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
    }
}