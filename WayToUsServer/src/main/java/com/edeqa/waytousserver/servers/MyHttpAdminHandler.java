package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.holders.admin.AdminMainPageHolder;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.edeqa.waytousserver.helpers.Constants.USER_NAME;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.CLASS;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.ID;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SCRIPT;


/**
 * Created 10/5/16.
 */
public class MyHttpAdminHandler implements HttpHandler {

    private final LinkedHashMap<String, PageHolder> holders;
    private DatabaseReference ref;
    private volatile AbstractDataProcessor dataProcessor;
    private HtmlGenerator html;

    public MyHttpAdminHandler(){

        holders = new LinkedHashMap<>();

        LinkedList<String> classes = new LinkedList<>();
        classes.add("AdminHomePageHolder");
        classes.add("AdminCreatePageHolder");
        classes.add("AdminGroupsPageHolder");
        classes.add("AdminGroupPageHolder");
        classes.add("AdminSummaryPageHolder");
        classes.add("AdminHelpPageHolder");
        classes.add("AdminSettingsPageHolder");
        classes.add("AdminUserPageHolder");
        classes.add("AdminMainPageHolder");

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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference();
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        try {
//            System.out.println("Admin server requested");

            URI uri = exchange.getRequestURI();

            ArrayList<String> parts = new ArrayList<>();
            parts.addAll(Arrays.asList(uri.getPath().split("/")));

            html = new HtmlGenerator();
            AdminMainPageHolder main = (AdminMainPageHolder) holders.get(AdminMainPageHolder.HOLDER_TYPE);
            main.addRequest(parts);
            if (parts.size() > 3 && parts.get(1).equals("admin") && holders.containsKey(parts.get(2)) && parts.get(parts.size()-1).equals("set")) {
                html = holders.get(parts.get(2)).create(html,parts);
            } else if (parts.size() > 2 && parts.get(1).equals("admin") && holders.containsKey(parts.get(2))) {
                main.addPart(parts.get(2));
                html = holders.get(AdminMainPageHolder.HOLDER_TYPE).create(html,parts);
            } else {
                main.addPart("home");
                html = holders.get(AdminMainPageHolder.HOLDER_TYPE).create(html,parts);
            }

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

                    JSONObject o = new JSONObject();
                    o.put("token", customToken);

                    html.getHead().add(SCRIPT).with(ID, "sign").with("sign", o);

                    byte[] bytes = html.build().getBytes();
                    try {
                        exchange.getResponseHeaders().set("Content-Type", "text/html");
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
