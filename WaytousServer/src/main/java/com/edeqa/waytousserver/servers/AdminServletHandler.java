package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.edeqa.waytousserver.helpers.Utils;
import com.edeqa.waytousserver.interfaces.PageHolder;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletException;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;
import static com.edeqa.waytousserver.helpers.Constants.SERVER_BUILD;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SRC;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.TITLE;


/**
 * Created 10/5/16.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class AdminServletHandler extends AbstractServletHandler {

    private final LinkedHashMap<String, PageHolder> holders;

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

    /**
     * Initialize DataProcessorFirebaseV1 for installation type "google-appengine".
     */
    @Override
    public void init() throws ServletException {
        super.init();
        initDataProcessor();
    }

    @Override
    public void perform(final RequestWrapper requestWrapper) throws IOException {

        try {
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

            try {
                String customToken = Common.getInstance().getDataProcessor("v1").createCustomToken("Viewer");

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

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
