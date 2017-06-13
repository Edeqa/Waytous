package com.edeqa.waytousserver.helpers;

import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.edeqa.waytousserver.servers.DataProcessorFirebaseV1;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;


/**
 * Created 1/23/2017.
 */

public class Common {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z", Locale.getDefault());
    volatile private static PrintWriter out;

    private volatile Map<String,AbstractDataProcessor> dataProcessor;

    private static final Common ourInstance = new Common();

    public static Common getInstance() {
        return ourInstance;
    }

    private Common() {
        dataProcessor = new HashMap<>();
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
        for (Object aText : text) {
            str += aText + " ";
        }
        System.out.println(Common.dateFormat.format(new Date()) + "/" + str);
    }

    public static void err(Object... text) {
        String str = "";
        for (Object aText : text) {
            str += aText + " ";
        }
        System.err.println(Common.dateFormat.format(new Date()) + "/" + str);
        /*try {
            if(out == null) {
                File log = new File("WaytousServer/WTU.log");
                System.out.println("Log file: "+log.getAbsolutePath());
//            out = new PrintWriter(new BufferedWriter(new FileWriter("WaytousServer/WTU.log", true)));
                out = new PrintWriter(log);
            }

            out.println(Common.dateFormat.format(new Date()) + "/" + str);
            out.flush();
//            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    public static String getWrappedHttpPort(){
        return SENSITIVE.getHttpPort() == 80 ? "" : ":" + SENSITIVE.getHttpPort();
    }

    public static String getWrappedHttpsPort(){
        return SENSITIVE.getHttpsPort() == 443 ? "" : ":" + SENSITIVE.getHttpsPort();
    }


    public AbstractDataProcessor getDataProcessor(String version) {
        if(dataProcessor.containsKey(version)) {
            return dataProcessor.get(version);
        } else {
            return dataProcessor.get("v1");
        }
    }

    public void setDataProcessor(AbstractDataProcessor dataProcessor) {
        this.dataProcessor.put(DataProcessorFirebaseV1.VERSION, dataProcessor);
    }

}
