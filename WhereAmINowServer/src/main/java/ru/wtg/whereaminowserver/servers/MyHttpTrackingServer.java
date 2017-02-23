package ru.wtg.whereaminowserver.servers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
import ru.wtg.whereaminowserver.holders.tracking.TrackingMainPageHolder;
import ru.wtg.whereaminowserver.interfaces.PageHolder;

import static ru.wtg.whereaminowserver.helpers.Constants.WEB_ROOT_DIRECTORY;

/**
 * Created 1/19/17.
 */
public class MyHttpTrackingServer implements HttpHandler {

    private HtmlGenerator html = new HtmlGenerator();
    private volatile AbstractWainProcessor wainProcessor;
    private final LinkedHashMap<String, PageHolder> holders;
    private DatabaseReference ref;

    public MyHttpTrackingServer(){

        holders = new LinkedHashMap<String, PageHolder>();

        LinkedList<String> classes = new LinkedList<String>();
        classes.add("TrackingMainPageHolder");

        for(String s:classes){
            try {
                //noinspection unchecked
                Class<PageHolder> _tempClass = (Class<PageHolder>) Class.forName("ru.wtg.whereaminowserver.holders.tracking."+s);
                Constructor<PageHolder> ctor = _tempClass.getDeclaredConstructor(MyHttpTrackingServer.class);
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
    public void handle(HttpExchange exchange) throws IOException {

        System.out.println("Tracking server requested");

        URI uri = exchange.getRequestURI();

        Headers headers = exchange.getRequestHeaders();
        String host;
        try {
            host = exchange.getRequestHeaders().get("Host").get(0);
            host = host.split(":")[0];
            System.out.println(host);
        } catch(Exception e){
            e.printStackTrace();
//            host = InetAddress.getLocalHost().getHostAddress();
        }

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


            TrackingMainPageHolder main = (TrackingMainPageHolder) holders.get(TrackingMainPageHolder.HOLDER_TYPE);
            main.addRequest(parts);


            if (parts.size() > 2 && (parts.get(1).equals("track") || parts.get(1).equals("group"))) {
                html = holders.get(TrackingMainPageHolder.HOLDER_TYPE).create(html,parts);
            } else {
                // interface for create tracking
                html = holders.get(TrackingMainPageHolder.HOLDER_TYPE).create(html,parts);
            }

        }

//        Common.addIncludes(html);

//        html.getHead().add(SCRIPT).with(SRC, "/js/Utils.js");


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
