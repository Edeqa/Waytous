package ru.wtg.whereaminowserver.servers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ru.wtg.whereaminowserver.helpers.Common;

/**
 * Created 1/19/17.
 */
public class MyHttpJoinHandler implements HttpHandler {

    private volatile AbstractWainProcessor wainProcessor;
    private DatabaseReference ref;

    public MyHttpJoinHandler(){

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference();
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {

        URI uri = exchange.getRequestURI();

        Headers headers = exchange.getRequestHeaders();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
//            System.out.println("HEADER:"+entry.getKey()+":"+entry.getValue());
        }
        InputStreamReader isr =  new InputStreamReader(exchange.getRequestBody(),"utf-8");
        BufferedReader br = new BufferedReader(isr);
        String body = br.readLine();
//        System.out.println("BODY:"+body);

        String host = null;
        try {
            host = exchange.getRequestHeaders().get("Host").get(0);
            host = host.split(":")[0];
//            System.out.println(host);
        } catch(Exception e){
            e.printStackTrace();
//            host = InetAddress.getLocalHost().getHostAddress();
        }

        Common.log("Join",host + uri.getPath(), body);


//        System.out.println(InetAddress.getLocalHost().getHostAddress());

        ArrayList<String> parts = new ArrayList<String>();
        parts.addAll(Arrays.asList(uri.getPath().split("/")));

        String tokenId = null;

//        HttpsExchange httpsExchange = (HttpsExchange) exchange;

        if(parts.size() >= 4){
            tokenId = parts.get(3);
        }

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


            if (parts.size() > 2 && (parts.get(1).equals("track") || parts.get(1).equals("group"))) {
            }

        }

//        Common.addIncludes(html);

//        html.getHead().add(SCRIPT).with(SRC, "/js/Utils.js");



        JSONObject json = new JSONObject(body);

        getWainProcessor().onMessage(new HttpConnection(exchange), body);

//        byte[] bytes = json.toString().getBytes();
//
//        System.out.println(json.toString(4));
//
//        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
//        exchange.getResponseHeaders().set("Content-Type", "application/json");
//        exchange.sendResponseHeaders(200, bytes.length);
//
//        OutputStream os = exchange.getResponseBody();
//        os.write(bytes);
//        os.close();

    }


    public AbstractWainProcessor getWainProcessor() {
        return wainProcessor;
    }

    public void setWainProcessor(AbstractWainProcessor wainProcessor) {
        this.wainProcessor = wainProcessor;
    }

    class HttpConnection implements AbstractWainProcessor.Connection {

        private final HttpExchange exchange;

        public HttpConnection(HttpExchange exchange) {
            this.exchange = exchange;
        }

        @Override
        public boolean isOpen() {
            return true;//conn.isOpen();
        }

        @Override
        public InetSocketAddress getRemoteSocketAddress() {
            return exchange.getRemoteAddress();
        }

        @Override
        public void send(String string) {

//            System.out.println("AbstractWainProcessor.Connection:"+string);

            byte[] bytes = string.getBytes();

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            try {
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }



//            conn.send(string);
        }

        @Override
        public void close() {
//            conn.close();
        }
    }


}
