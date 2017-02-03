package ru.wtg.whereaminowserver.helpers;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static ru.wtg.whereaminowserver.helpers.Constants.WSS_PORT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HREF;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.LINK;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.REL;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SCRIPT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SRC;

/**
 * Created 1/23/2017.
 */

public class Common {

    public static void addIncludes(HtmlGenerator html) {


        html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/3.6.6/firebase-app.js");
        html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/3.6.6/firebase-messaging.js");
        html.getHead().add(SCRIPT).with("var config = { messagingSenderId: \"365115596478\",}; firebase.initializeApp(config); var messaging = firebase.messaging();\n");
        html.getHead().add(SCRIPT).with(SRC, "https://code.jquery.com/jquery-3.1.1.min.js");
        html.getHead().add(SCRIPT).with(SRC, "/js/Utils.js");
        html.getHead().add(LINK).with(HREF, "https://fonts.googleapis.com/icon?family=Material+Icons").with(REL, "stylesheet");
    }

    public static JSONObject fetchGeneralInfo() {
        JSONObject o = new JSONObject();

        try {
            String wss = "ws://" + InetAddress.getLocalHost().getHostAddress() + ":" + WSS_PORT;
            o.put("uri", wss);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return o;
    }

}