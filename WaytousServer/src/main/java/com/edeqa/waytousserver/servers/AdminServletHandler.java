package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.AbstractServletHandler;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.edeqa.waytousserver.helpers.Utils;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.TaskCompletionSource;
import com.google.firebase.tasks.Tasks;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;
import static com.edeqa.waytousserver.helpers.Constants.SERVER_BUILD;
import static com.edeqa.waytousserver.helpers.Constants.USER_NAME;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SRC;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.TITLE;


/**
 * Created 10/5/16.
 */
public class AdminServletHandler extends AbstractServletHandler {

    private final LinkedHashMap<String, PageHolder> holders;
    private volatile AbstractDataProcessor dataProcessor;

    public AdminServletHandler(){

        holders = new LinkedHashMap<>();

        LinkedList<String> classes = new LinkedList<>();
        classes.add("AdminLogsHolder");
        classes.add("AdminMainHolder");
        classes.add("AdminRestHolder");
        classes.add("AdminSettingsHolder");

        for(String s:classes){
            try {
                //noinspection unchecked
                Class<PageHolder> _tempClass = (Class<PageHolder>) Class.forName("com.edeqa.waytousserver.holders.admin."+s);
                Constructor<PageHolder> ctor = _tempClass.getDeclaredConstructor(AdminServletHandler.class);
                PageHolder holder = ctor.newInstance(this);
                holders.put(holder.getType(), holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void perform(final RequestWrapper requestWrapper) throws IOException {
        try {
//            System.out.println("Admin server requested");

            /*if("/".equals(requestWrapper.getRequestURI().getPath())) {
                requestWrapper.sendRedirect("/admin/");
                return;
            }*/

            String[] parts = requestWrapper.getRequestURI().getPath().split("/");
            if(parts.length >2) {
                for(Map.Entry<String, PageHolder> x: holders.entrySet()) {

                    if(parts[2].equals(x.getValue().getType()) && x.getValue().perform(requestWrapper)) {
                        return;
                    }
                }
            }

            Task<String> task = FirebaseAuth.getInstance().createCustomToken("Viewer");
            try {
                Tasks.await(task);
                String customToken = task.getResult();

                    Map<String,Object> update = new HashMap<>();
                    update.put("active", false);
                    update.put("color", "black");
                    update.put("changed", new Date().getTime());
                    update.put(USER_NAME,"Viewer");

                    final JSONObject o = new JSONObject();
                    o.put("version", SERVER_BUILD);
                    o.put("HTTP_PORT", SENSITIVE.getHttpPort());
                    o.put("HTTPS_PORT", SENSITIVE.getHttpsPort());
                    o.put("WS_FB_PORT", SENSITIVE.getWsPortFirebase());
                    o.put("WSS_FB_PORT", SENSITIVE.getWssPortFirebase());
                    o.put("WS_PORT", SENSITIVE.getWsPortDedicated());
                    o.put("WSS_PORT", SENSITIVE.getWssPortDedicated());
                    o.put("firebase_config", SENSITIVE.getFirebaseConfig());
                    o.put("sign", customToken);

                    HtmlGenerator html = new HtmlGenerator();
                    html.getHead().add(TITLE).with("Admin");

                    html.getHead().add(SCRIPT).with("data", o);
                    html.getHead().add(SCRIPT).with(SRC, "/js/admin/Main.js");

                    Utils.sendResult.call(requestWrapper, 200, Constants.MIME.TEXT_HTML, html.build().getBytes());

                    // Send token back to client
                } catch (Exception e) {
                e.printStackTrace();
            }
/*
            FirebaseAuth.getInstance().createCustomToken("Viewer").addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(final String customToken) {
                    System.out.println("ADMIN1");
                    Map<String,Object> update = new HashMap<>();
                    update.put("active", false);
                    update.put("color", "black");
                    System.out.println("ADMIN11");
                    update.put("changed", new Date().getTime());
                    System.out.println("ADMIN12");
                    update.put(USER_NAME,"Viewer");
                    System.out.println("ADMIN2");

                    final JSONObject o = new JSONObject();
                    o.put("version", SERVER_BUILD);
                    o.put("HTTP_PORT", SENSITIVE.getHttpPort());
                    o.put("HTTPS_PORT", SENSITIVE.getHttpsPort());
                    o.put("WS_FB_PORT", SENSITIVE.getWsPortFirebase());
                    o.put("WSS_FB_PORT", SENSITIVE.getWssPortFirebase());
                    o.put("WS_PORT", SENSITIVE.getWsPortDedicated());
                    o.put("WSS_PORT", SENSITIVE.getWssPortDedicated());
                    o.put("firebase_config", SENSITIVE.getFirebaseConfig());
                    o.put("sign", customToken);

                    HtmlGenerator html = new HtmlGenerator();
                    html.getHead().add(TITLE).with("Admin");

                    html.getHead().add(SCRIPT).with("data", o);
                    html.getHead().add(SCRIPT).with(SRC, "/js/admin/Main.js");

                    Utils.sendResult.call(requestWrapper, 200, Constants.MIME.TEXT_HTML, html.build().getBytes());

                    // Send token back to client
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println("FAIL7:"+e.getMessage());
                }
            });
*/

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
