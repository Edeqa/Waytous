package ru.wtg.whereaminowserver.helpers;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault());

    public static void addIncludes(HtmlGenerator html) {

        html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/3.6.8/firebase-app.js");
        html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/3.6.8/firebase-auth.js");
        html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/3.6.8/firebase-database.js");
//        html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/3.6.8/firebase-messaging.js");

        html.getHead().add(SCRIPT).with("var config = {\n" +
                "    apiKey: \"AIzaSyCRH9g5rmQdvShE4mI2czumO17u_hwUF8Q\",\n" +
                "    authDomain: \"where-am-i-now-1373.firebaseapp.com\",\n" +
                "    databaseURL: \"https://where-am-i-now-1373.firebaseio.com\",\n" +
                "    storageBucket: \"where-am-i-now-1373.appspot.com\",\n" +
                "    messagingSenderId: \"365115596478\"\n" +
                "  };\n" +
                "  firebase.initializeApp(config);" +
                "  var database = firebase.database();\n");
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

    public static void log(String... text) {
        String str = "";
        for(int i = 0; i < text.length; i++){
            str += text[i] + " ";
        }
        System.out.println(Common.dateFormat.format(new Date()) + "/" + str);
    }

}
