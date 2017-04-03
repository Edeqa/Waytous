package ru.wtg.whereaminowserver.helpers;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static ru.wtg.whereaminowserver.helpers.Constants.SENSITIVE;

/**
 * Created 1/23/2017.
 */

public class Common {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z", Locale.getDefault());
    volatile private static PrintWriter out;

    public Common() throws FileNotFoundException {
    }


    public static JSONObject fetchGeneralInfo() {
        JSONObject o = new JSONObject();

        try {
            String wss = "ws://" + InetAddress.getLocalHost().getHostAddress() + ":" + SENSITIVE.getWssPortDedicated();
            o.put("uri", wss);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return o;
    }

    public static void log(Object... text) {
        String str = "";
        for(int i = 0; i < text.length; i++){
            str += text[i] + " ";
        }
        System.out.println(Common.dateFormat.format(new Date()) + "/" + str);
        /*try {
            if(out == null) {
                File log = new File("WhereAmINowServer/WAIN.log");
                System.out.println("Log file: "+log.getAbsolutePath());
//            out = new PrintWriter(new BufferedWriter(new FileWriter("WhereAmINowServer/WAIN.log", true)));
                out = new PrintWriter(log);
            }

            out.println(Common.dateFormat.format(new Date()) + "/" + str);
            out.flush();
//            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

}
