package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONObject;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;
import static com.edeqa.waytousserver.helpers.Constants.SERVER_BUILD;
import static com.edeqa.waytousserver.helpers.Constants.USER_COLOR;
import static com.edeqa.waytousserver.helpers.Constants.USER_NAME;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.CLASS;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SRC;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.TITLE;


/**
 * Created 10/5/16.
 */
public class MyHttpAdminHandler implements HttpHandler {

    private final LinkedHashMap<String, PageHolder> holders;
    private volatile AbstractDataProcessor dataProcessor;
    private HtmlGenerator html;

    public MyHttpAdminHandler(){

        holders = new LinkedHashMap<>();

        LinkedList<String> classes = new LinkedList<>();
        classes.add("AdminLogsHolder");
        classes.add("AdminMainHolder");
        classes.add("AdminSettingsHolder");

        for(String s:classes){
            try {
                //noinspection unchecked
                Class<PageHolder> _tempClass = (Class<PageHolder>) Class.forName("com.edeqa.waytousserver.holders.admin."+s);
                Constructor<PageHolder> ctor = _tempClass.getDeclaredConstructor(MyHttpAdminHandler.class);
                PageHolder holder = ctor.newInstance(this);
                holders.put(holder.getType(), holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        try {
//            System.out.println("Admin server requested");


            for(Map.Entry<String, PageHolder> x: holders.entrySet()) {
                if(x.getValue().perform(exchange)) {
                    return;
                }
            }

            html = new HtmlGenerator();
            html.getHead().add(TITLE).with("Admin");

            final JSONObject o = new JSONObject();
            o.put("version", SERVER_BUILD);
            o.put("HTTP_PORT", SENSITIVE.getHttpPort());
            o.put("HTTPS_PORT", SENSITIVE.getHttpsPort());
            o.put("WS_FB_PORT", SENSITIVE.getWsPortFirebase());
            o.put("WSS_FB_PORT", SENSITIVE.getWssPortFirebase());
            o.put("WS_PORT", SENSITIVE.getWsPortDedicated());
            o.put("WSS_PORT", SENSITIVE.getWssPortDedicated());
            o.put("firebase_config", SENSITIVE.getFirebaseConfig());


            html.getBody().with(CLASS,"body");

            FirebaseAuth.getInstance().createCustomToken("Administrator").addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(final String customToken) {
                    System.out.println("GOT:"+customToken);

                    Map<String,Object> update = new HashMap<>();
                    update.put("active", false);
                    update.put("color", Color.BLACK);
                    update.put("changed", new Date().getTime());
                    update.put(USER_NAME,"Administrator");

                    o.put("sign", customToken);
                    html.getHead().add(SCRIPT).with("data", o);
                    html.getHead().add(SCRIPT).with(SRC, "/js/admin/Main.js");

                    byte[] bytes = html.build().getBytes();
                    try {
                        exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, "text/html");
                        exchange.sendResponseHeaders(200, bytes.length);
                        OutputStream os = exchange.getResponseBody();
                        os.write(bytes);
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Send token back to client
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println("FAIL7:"+e.getMessage());
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public AbstractDataProcessor getDataProcessor() {
        return dataProcessor;
    }

    public void setDataProcessor(AbstractDataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }
}
