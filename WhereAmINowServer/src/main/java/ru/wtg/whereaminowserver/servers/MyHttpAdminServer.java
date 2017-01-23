package ru.wtg.whereaminowserver.servers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
import ru.wtg.whereaminowserver.holders.AdminMainPageHolder;
import ru.wtg.whereaminowserver.holders.AdminSummaryPageHolder;

import static ru.wtg.whereaminowserver.helpers.Constants.SERVER_BUILD;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.CLASS;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.DIV;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HREF;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.LINK;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.REL;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SCRIPT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SRC;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.STYLESHEET;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TITLE;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TYPE;


/**
 * Created 10/5/16.
 */
public class MyHttpAdminServer implements HttpHandler {

    private final AdminSummaryPageHolder summaryHolder;
    private final AdminMainPageHolder mainHolder;
    private volatile MyWssServer wssProcessor;

    public MyHttpAdminServer(){
        summaryHolder = new AdminSummaryPageHolder(this);
        mainHolder = new AdminMainPageHolder(this);
    }

    public static Map<String, List<String>> splitQuery(String url) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
        String[] a = url.split("\\?");

        if(a.length>0) {
            final String[] pairs = a[a.length - 1].split("&");
            for (String pair : pairs) {
                final int idx = pair.indexOf("=");
                final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                if (!query_pairs.containsKey(key)) {
                    query_pairs.put(key, new LinkedList<String>());
                }
                final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
                query_pairs.get(key).add(value);
            }
        }
        return query_pairs;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {

            System.out.println("\nAdmin server processing");

            URI uri = exchange.getRequestURI();
            Map<String, List<String>> query = splitQuery(uri.toString());

            ArrayList<String> parts = new ArrayList<String>();
            parts.addAll(Arrays.asList(uri.getPath().split("/")));

            System.out.println("SPLIT:" + query);
            System.out.println("PARTS:" + parts);

            HtmlGenerator html;
            if (parts.size() > 2 && parts.get(1).equals("admin") && parts.get(2).equals("summary")) {
                html = summaryHolder.create(query);
            } else {
                html = mainHolder.create(query);
                new HtmlGenerator();
            }
            html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/3.6.6/firebase-app.js");
            html.getHead().add(SCRIPT).with(SRC, "https://www.gstatic.com/firebasejs/3.6.6/firebase-messaging.js");
            html.getHead().add(SCRIPT).with("var config = {\n" +
                    "messagingSenderId: \"365115596478\",\n" +
                    "};\n" +
                    "firebase.initializeApp(config);\n" +
                    "messaging = firebase.messaging();");





/*
        if(query.containsKey("text")){
            wssProcessor.sendToAll(query.get("text").toString(), null);
        }

        if(processQuery(query)){
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            return;
        }
*/

            byte[] bytes = html.build().getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public MyWssServer getWssProcessor() {
        return wssProcessor;
    }

    public void setWssProcessor(MyWssServer wssProcessor) {
        this.wssProcessor = wssProcessor;
    }
}
