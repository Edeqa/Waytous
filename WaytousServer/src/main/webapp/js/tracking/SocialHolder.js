/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 3/10/17.
 */

function SocialHolder(main) {

    var type = "social";

    var shareDialog;
    var drawerItemShare;
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
                drawerItemShare = object.add(DRAWER.SECTION_COMMUNICATION,"facebook","Share to Facebook",ic_facebook,function(){
                    if(shareDialog) shareDialog.close();
                    shareDialog = shareDialog || u.dialog({
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
                                shareDialog.close();
                            }
                        },
                        negative: {
                            label: u.lang.cancel
                        },
                        timeout: 20000
                    }, main.right);
                    shareDialog.open();
                });
                drawerItemShare.hide();
                // var drawerItem = object.add(DRAWER.SECTION_LAST,type+"_1");
                //
                // drawerItem.classList.add("menu-item-social");
                //
                //
                //
                // u.create(HTML.DIV, { className: "menu-item-twitter", innerHTML: "twitter" }, drawerItem);

                break;
            case EVENTS.TRACKING_CONNECTING:
            case EVENTS.TRACKING_RECONNECTING:
            case EVENTS.TRACKING_ACTIVE:
                drawerItemShare.show();
                break;
            case EVENTS.TRACKING_DISABLED:
                drawerItemShare.hide();
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
        // console.log("SAMPLECREATEVIEW",user);
    }


    function onChangeLocation(location) {
        show.call(this);
        // console.log("SAMPLEONCHANGELOCATION",this,location);
    }

    return {
        type:type,
        start:start,
        onEvent:onEvent,
    }
}