/**
 * Created 1/20/17.
 */

function startAdmin() {
//    var data = $("#admin-data")[0];
    console.log("DATA",data);
    console.log("MAIN");

    $("body").append("<div id='content'></div>");

    $("body").append("<a href='summary'>Summary</div>");



}
function logout(to_url) {
    var out = window.location.href.replace(/:\/\//, '://log:out@');

    if(!to_url){
        to_url = window.location.protocol + "//" + window.location.host + window.location.pathname
    }

    jQuery.get(out).error(function() {
        window.location = to_url;
    });
}

document.addEventListener("DOMContentLoaded", startAdmin);