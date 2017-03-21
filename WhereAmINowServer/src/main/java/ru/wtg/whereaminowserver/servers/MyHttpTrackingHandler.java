package ru.wtg.whereaminowserver.servers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import ru.wtg.whereaminowserver.helpers.Common;
import ru.wtg.whereaminowserver.helpers.HtmlGenerator;

import static ru.wtg.whereaminowserver.helpers.Constants.HTTPS_PORT;
import static ru.wtg.whereaminowserver.helpers.Constants.HTTP_PORT;
import static ru.wtg.whereaminowserver.helpers.Constants.HTTP_SERVER_HOST;
import static ru.wtg.whereaminowserver.helpers.Constants.SERVER_BUILD;
import static ru.wtg.whereaminowserver.helpers.Constants.WEB_ROOT_DIRECTORY;
import static ru.wtg.whereaminowserver.helpers.Constants.WSS_FB_PORT;
import static ru.wtg.whereaminowserver.helpers.Constants.WSS_PORT;
import static ru.wtg.whereaminowserver.helpers.Constants.WSS_SERVER_HOST;
import static ru.wtg.whereaminowserver.helpers.Constants.WS_FB_PORT;
import static ru.wtg.whereaminowserver.helpers.Constants.WS_PORT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.CONTENT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HREF;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.LINK;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.META;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.NAME;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.ONLOAD;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.REL;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SCRIPT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SIZES;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SRC;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TITLE;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TYPE;

/**
 * Created 1/19/17.
 */
public class MyHttpTrackingHandler implements HttpHandler {

    private HtmlGenerator html = new HtmlGenerator();
    private volatile AbstractWainProcessor wainProcessor;
    private DatabaseReference ref;

    public MyHttpTrackingHandler(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        URI uri = exchange.getRequestURI();

        String host = null;
        try {
            host = exchange.getRequestHeaders().get("Host").get(0);
            host = host.split(":")[0];
        } catch(Exception e){
            e.printStackTrace();
//            host = InetAddress.getLocalHost().getHostAddress();
        }

        Common.log("Tracking",host + uri.getPath() );

//        System.out.println(InetAddress.getLocalHost().getHostAddress());

        ArrayList<String> parts = new ArrayList<String>();
        parts.addAll(Arrays.asList(uri.getPath().split("/")));

        String tokenId = null;

//        HttpsExchange httpsExchange = (HttpsExchange) exchange;

        if(parts.size() >= 3){
            tokenId = parts.get(2);
        }
        System.out.println("TOKEN");
        if(tokenId == null) {

        } else {

        }
        System.out.println("A");
        File root = new File(WEB_ROOT_DIRECTORY);
        File file = new File(root + uri.getPath()).getCanonicalFile();

        System.out.println("B");
        html.clear();
        HtmlGenerator.Tag head = html.getHead();
//        head.add(TITLE).with("Tracking");

//        head.add(DIV).with(ID,"tracking-token").with(tokenId).with(STYLE,"display:none");
//        head.add(SCRIPT).with(SRC, "https://code.jquery.com/jquery-3.1.1.min.js");
//        head.add(SCRIPT).with(SRC, "/js/tracking.js");

//        HtmlGenerator.Tag body = html.getBody();
//        body.with("Here will be a web version soon...");
//        body.add(BR);

        html.getHead().add(TITLE).with("Waytogo");

        JSONObject o = new JSONObject();
//                o.put("page", part);
        o.put("request", parts);
        o.put("version", SERVER_BUILD);
        o.put("WSS_SERVER_HOST", WSS_SERVER_HOST);
        o.put("HTTP_SERVER_HOST", HTTP_SERVER_HOST);
        o.put("HTTP_PORT", HTTP_PORT);
        o.put("HTTPS_PORT", HTTPS_PORT);
        o.put("WS_FB_PORT", WS_FB_PORT);
        o.put("WSS_FB_PORT", WSS_FB_PORT);
        o.put("WS_PORT", WS_PORT);
        o.put("WSS_PORT", WSS_PORT);


        html.getHead().add(SCRIPT).with("data", o);

        html.getHead().add(SCRIPT).with(SRC, "/js/tracking/Main.js").with(ONLOAD, "(window.WAIN = new Main()).start();");

        if(tokenId != null) {

//            String mobileRedirect = "orw://" + InetAddress.getLocalHost().getHostAddress() + ":" + HTTP_PORT + "/track/" + tokenId;

/*            if (parts.size() > 2 && parts.get(1).equals("track")) {
                System.out.println("Mobile redirect generated: " + mobileRedirect);
                JSONObject o = new JSONObject();
                o.put("url", mobileRedirect);
                head.add(SCRIPT).with("redirect",o);
//                head.add(META).with(HTTP_EQUIV, "refresh").with(CONTENT, "0;URL='" + mobileRedirect + "'");
            }*/
//            body.add(A).with(HREF, mobileRedirect).with("Click here for start mobile client");


//            if (parts.size() > 2 && (parts.get(1).equals("track") || parts.get(1).equals("group"))) {


//            html.getHead().add(LINK).with(REL,"apple-touch-icon").with(HREF, "/images/apple-touch-icon.png");
//            html.getHead().add(LINK).with(REL,"apple-touch-icon").with(SIZES, "57x57").with(HREF, "/images/apple-touch-icon-57x57.png");
//            html.getHead().add(LINK).with(REL,"apple-touch-icon").with(SIZES, "60x60").with(HREF, "/images/apple-touch-icon-60x60.png");
//            html.getHead().add(LINK).with(REL,"apple-touch-icon").with(SIZES, "72x72").with(HREF, "/images/apple-touch-icon-72x72.png");
//            html.getHead().add(LINK).with(REL,"apple-touch-icon").with(SIZES, "76x76").with(HREF, "/images/apple-touch-icon-76x76.png");
//            html.getHead().add(LINK).with(REL,"apple-touch-icon").with(SIZES, "114x114").with(HREF, "/images/apple-touch-icon-114x114.png");
//            html.getHead().add(LINK).with(REL,"apple-touch-icon").with(SIZES, "120x120").with(HREF, "/images/apple-touch-icon-120x120.png");
//            html.getHead().add(LINK).with(REL,"apple-touch-icon").with(SIZES, "152x152").with(HREF, "/images/apple-touch-icon-152x152.png");
//            html.getHead().add(LINK).with(REL,"apple-touch-icon").with(SIZES, "180x180").with(HREF, "/images/apple-touch-icon.png");
//            html.getHead().add(LINK).with(REL,"apple-touch-startup-image").with(HREF, "/images/apple-touch-icon.png");
//            html.getHead().add(LINK).with(REL,"icon").with(TYPE,"image/png").with(SIZES, "32x32").with(HREF, "/images/favicon-32x32.png");
//            html.getHead().add(LINK).with(REL,"icon").with(TYPE,"image/png").with(SIZES, "16x16").with(HREF, "/images/favicon-16x16.png");
//            html.getHead().add(LINK).with(REL,"manifest").with(HREF, "/images/manifest.json");
//            html.getHead().add(LINK).with(REL,"mask-icon").with(HREF, "/images/safari-pinned-tab.svg").with("color","#007574");
//            html.getHead().add(LINK).with(REL,"shortcut icon").with(HREF, "/images/favicon.ico");
//            html.getHead().add(META).with(NAME,"apple-mobile-web-app-capable").with(CONTENT, "yes");
//            html.getHead().add(META).with(NAME,"application-name").with(CONTENT, "Waytogo");
//            html.getHead().add(META).with(NAME,"msapplication-config").with(CONTENT, "/images/browserconfig.xml");
//            html.getHead().add(META).with(NAME,"theme-color").with(CONTENT, "#ffffff");


//            } else {
            // interface for create tracking
//            }

        } else {

        }

        byte[] bytes = html.build().getBytes();

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();

    }



    public AbstractWainProcessor getWainProcessor() {
        return wainProcessor;
    }

    public void setWainProcessor(AbstractWainProcessor wainProcessor) {
        this.wainProcessor = wainProcessor;
    }



}
