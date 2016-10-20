package ru.wtg.whereaminowserver.helpers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Created by tujger on 10/5/16.
 */
public class MyHttpServer implements HttpHandler {

    private HtmlGenerator html = new HtmlGenerator();
    private volatile MyWssServer wssProcessor;


    @Override
    public void handle(HttpExchange exchange) throws IOException {

        StringBuilder builder = new StringBuilder();

        URI uri = exchange.getRequestURI();
        Map<String, List<String>> query = splitQuery(uri.toString());

        if(query.containsKey("text")){
//            synchronized (MyHttpServer.class){
                wssProcessor.sendToAll(query.get("text").toString(), null);
//            }

        }

        if(processQuery(query)){
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            return;
//            OutputStream os = exchange.getResponseBody();
//            os.write(bytes);
//            os.close();
        };

        System.out.println("QUERY:"+query);

        Headers headers = exchange.getRequestHeaders();
        for (String header : headers.keySet()) {
//            System.out.println(header+":"+headers.getFirst(header));
        }

        header();
        tableTokens();

        HtmlGenerator.Tag div = html.getBody().add("div").with("style","display:flex; justify-content:space-between");
        tableIpToUser(div);
        tableIpToToken(div);
        tableChecks();
        builder.append(html.build());

        byte[] bytes = builder.toString().getBytes();
        exchange.sendResponseHeaders(200, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();

    }

    private void header() {

        html.addHead().add("title").with("Admin");
        html.getHead().add("style").with("table {width: 100%; border-style: solid;border-width: 1px; border-spacing:0px; font-family: sans-serif; }");
        html.getHead().add("style").with("td {vertical-align:top; }");

        html.addBody();
    }

    private void tableTokens() {

        html.getBody().add("h1").with("Tokens");
        HtmlGenerator.Tag table = html.getBody().add("table").with("border",1);
        HtmlGenerator.Tag thr = table.add("tr");
        HtmlGenerator.Tag td1 = thr.add("th").with("Token").with("rowspan",2);
        HtmlGenerator.Tag td2 = thr.add("th").with("Owner").with("rowspan",2);
        HtmlGenerator.Tag td3 = thr.add("th").with("Changed").with("rowspan",2);

        HtmlGenerator.Tag td4 = thr.add("th").with("Users").with("colspan",5);

        thr = table.add("tr");
        td4 = thr.add("th").with("DeviceId");
        thr.add("th").with("Address");
        thr.add("th").with("Joined");
        thr.add("th").with("Control");
        thr.add("th").with("X");
        HtmlGenerator.Tag tr;

        for(Map.Entry<String,MyToken> x: wssProcessor.tokens.entrySet()){
            tr = table.add("tr");

            td1 = tr.add("td").with(x.getKey());
            td2 = tr.add("td").with(x.getValue().getOwner());
            td3 = tr.add("td").with(new Date(x.getValue().getChanged()).toString());

            int indent = 0;
            for(Map.Entry<String,MyUser> y:x.getValue().users.entrySet()){
                if(indent>0) tr = table.add("tr");
                tr.add("td").with(y.getValue().getDeviceId());
                tr.add("td").with(y.getValue().getAddress());
                tr.add("td").with(new Date(y.getValue().getTimestamp()).toString());
                tr.add("td").with(y.getValue().getControl());
                tr.add("td").add("a").with("Del").with("href","/?action=del&token="+x.getKey()+"&id="+y.getValue().getDeviceId());

                indent ++;
            }
            td1.with("rowspan",indent);
            td2.with("rowspan",indent);
            td3.with("rowspan",indent);
        }
    }

    private void tableChecks() {

        html.getBody().add("h1").with("Checks");
        HtmlGenerator.Tag table = html.getBody().add("table").with("border",1);
        HtmlGenerator.Tag tr = table.add("tr");
        tr.add("th").with("Address");
        tr.add("th").with("Token");
        tr.add("th").with("Control");
        tr.add("th").with("Timestamp");

        for(Map.Entry<String,MyWssServer.CheckReq> x: wssProcessor.checkUsers.entrySet()){
            tr = table.add("tr");

            tr.add("td").with(x.getKey());
            tr.add("td").with(x.getValue().token.getId());
            tr.add("td").with(x.getValue().control);
            tr.add("td").with(new Date(x.getValue().timestamp).toString());

        }
    }

    private void tableIpToUser(HtmlGenerator.Tag out) {

        HtmlGenerator.Tag div = out.add("div").with("style","width:48%");
        div.add("h1").with("IP to User corresponds");
        HtmlGenerator.Tag table = div.add("table").with("border",1);
        HtmlGenerator.Tag tr = table.add("tr");
        tr.add("th").with("IP");
        tr.add("th").with("Device ID");

        for(Map.Entry<String,MyUser> x: wssProcessor.ipToUser.entrySet()){
            tr = table.add("tr");

            tr.add("td").with(x.getKey());
            tr.add("td").with(x.getValue().getDeviceId());
        }
    }

    private void tableIpToToken(HtmlGenerator.Tag out) {

        HtmlGenerator.Tag div = out.add("div").with("style","width:48%");
        div.add("h1").with("IP to Token corresponds");
        HtmlGenerator.Tag table = div.add("table").with("border",1);
        HtmlGenerator.Tag tr = table.add("tr");
        tr.add("th").with("IP");
        tr.add("th").with("Token ID");

        for(Map.Entry<String,MyToken> x: wssProcessor.ipToToken.entrySet()){
            tr = table.add("tr");

            tr.add("td").with(x.getKey());
            tr.add("td").with(x.getValue().getId());
        }
    }



    private boolean processQuery(Map<String, List<String>> query) {
        boolean processed = false;
        if(query.containsKey("action")){
            for(String x:query.get("action")){
                processAction(x,query);
    processed = true;
            }
        }
return processed;
    }

    private void processAction(String action, Map<String, List<String>> query) {

        if("del".equals(action)){

            String token=null,id=null;

            if(query.containsKey("token")) token = query.get("token").get(0);
            if(query.containsKey("id")) id = query.get("id").get(0);

            wssProcessor.removeUser(token,id);

        }

    }

    public static Map<String, List<String>> splitQuery(String url) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<>();
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

    public MyWssServer getWssProcessor() {
        return wssProcessor;
    }

    public void setWssProcessor(MyWssServer wssProcessor) {
        this.wssProcessor = wssProcessor;
    }
}
