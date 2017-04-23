package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;
import static com.edeqa.waytousserver.helpers.Constants.SERVER_BUILD;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.ONLOAD;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SRC;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.TITLE;


/**
 * Created 1/19/17.
 */
public class MyHttpTrackingHandler implements HttpHandler {

    private HtmlGenerator html = new HtmlGenerator();
    private volatile AbstractDataProcessor dataProcessor;

    public MyHttpTrackingHandler(){
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        URI uri = exchange.getRequestURI();

        String host = null;
        try {
            host = exchange.getRequestHeaders().get(HttpHeaders.HOST).get(0);
            host = host.split(":")[0];
        } catch(Exception e){
            e.printStackTrace();
        }

        Common.log("Tracking",exchange.getRemoteAddress(),host + uri.getPath() );

        ArrayList<String> parts = new ArrayList<>();
        parts.addAll(Arrays.asList(uri.getPath().split("/")));

        File root = new File(SENSITIVE.getWebRootDirectory());
        File file = new File(root + uri.getPath()).getCanonicalFile();

        JSONObject o = new JSONObject();
        o.put("request", parts);
        o.put("version", SERVER_BUILD);
        o.put("HTTP_PORT", SENSITIVE.getHttpPort());
        o.put("HTTPS_PORT", SENSITIVE.getHttpsPort());
        o.put("WS_FB_PORT", SENSITIVE.getWsPortFirebase());
        o.put("WSS_FB_PORT", SENSITIVE.getWssPortFirebase());
        o.put("WS_PORT", SENSITIVE.getWsPortDedicated());
        o.put("WSS_PORT", SENSITIVE.getWssPortDedicated());
        o.put("firebase_config", SENSITIVE.getFirebaseConfig());


        html.clear();
        html.getHead().add(TITLE).with("Waytogo");
        html.getHead().add(SCRIPT).with("data", o);
        html.getHead().add(SCRIPT).with(SRC, "/js/tracking/Main.js").with("async","true").with(ONLOAD, "(window.WTU = new Main()).start();");

        byte[] bytes = html.build().getBytes();

        exchange.getResponseHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, "text/html");
        exchange.sendResponseHeaders(200, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();

    }

    public AbstractDataProcessor getDataProcessor() {
        return dataProcessor;
    }

    public void setDataProcessor(AbstractDataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

}
