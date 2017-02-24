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

import static ru.wtg.whereaminowserver.helpers.Constants.SERVER_BUILD;
import static ru.wtg.whereaminowserver.helpers.Constants.WEB_ROOT_DIRECTORY;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SCRIPT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SRC;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TITLE;

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

        if(tokenId == null) {

        } else {

        }
        File root = new File(WEB_ROOT_DIRECTORY);
        File file = new File(root + uri.getPath()).getCanonicalFile();

        html.clear();
        HtmlGenerator.Tag head = html.getHead();
//        head.add(TITLE).with("Tracking");

//        head.add(DIV).with(ID,"tracking-token").with(tokenId).with(STYLE,"display:none");
//        head.add(SCRIPT).with(SRC, "https://code.jquery.com/jquery-3.1.1.min.js");
//        head.add(SCRIPT).with(SRC, "/js/tracking.js");

//        HtmlGenerator.Tag body = html.getBody();
//        body.with("Here will be a web version soon...");
//        body.add(BR);

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

                html.getHead().add(TITLE).with("Waytogo");

                JSONObject o = new JSONObject();
//                o.put("page", part);
                o.put("request", parts);
                o.put("version", SERVER_BUILD);

                html.getHead().add(SCRIPT).with("data", o);
                html.getHead().add(SCRIPT).with(SRC, "/js/tracking/Main.js");

//            } else {
                // interface for create tracking
//            }

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
