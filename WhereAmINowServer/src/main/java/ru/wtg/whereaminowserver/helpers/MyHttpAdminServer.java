package ru.wtg.whereaminowserver.helpers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


import org.json.JSONException;

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

import static ru.wtg.whereaminowserver.helpers.Constants.SERVER_BUILD;


/**
 * Created by tujger on 10/5/16.
 */
public class MyHttpAdminServer implements HttpHandler {

    private HtmlGenerator html = new HtmlGenerator();
    private volatile MyWssServer wssProcessor;


    @Override
    public void handle(HttpExchange exchange) throws IOException {

        StringBuilder builder = new StringBuilder();

        URI uri = exchange.getRequestURI();
        Map<String, List<String>> query = splitQuery(uri.toString());

        if(query.containsKey("text")){
//            synchronized (MyHttpAdminServer.class){
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
        }

//        System.out.println("QUERY:"+query);

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

        if(query.containsKey("list")) {
            try {
                String list = query.get("list").get(0);

                if("user".equals(list)){
                    tableUser(query.get("token").get(0),query.get("id").get(0));
                }

            } catch(NullPointerException e) {
//                e.printStackTrace();
            } catch(JSONException e) {
//                e.printStackTrace();
            }
        }


        builder.append(html.build());

        byte[] bytes = builder.toString().getBytes();
        exchange.sendResponseHeaders(200, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();

    }

    private void header() {

        html.addHead().add("title").with("Admin");
        html.getHead().add("style").with("table {width: 100%; border-style: solid;border-width: 1px; border-spacing:0px; font-family: sans-serif; font-size:13px }");
        html.getHead().add("style").with("td {vertical-align:top; }");
        html.getHead().add("meta").with("http-equiv","refresh").with("content",2);

        html.addBody();

        html.getBody().add("div").with("Build: "+SERVER_BUILD).with("style","float: right; font-family: sans-serif; font-size: 10px;");

    }

    private void tableTokens() {

        html.getBody().add("h1").with("Tokens");
        HtmlGenerator.Tag table = html.getBody().add("table").with("border",1);
        HtmlGenerator.Tag thr = table.add("tr");
        HtmlGenerator.Tag td1 = thr.add("th").with("Token").with("rowspan",2);
        HtmlGenerator.Tag td2 = thr.add("th").with("Owner").with("rowspan",2);
        HtmlGenerator.Tag td3 = thr.add("th").with("Created").with("rowspan",2);
        HtmlGenerator.Tag td4 = thr.add("th").with("Changed").with("rowspan",2);

        thr.add("th").with("Users").with("colspan",8);

        thr = table.add("tr");
        thr.add("th").with("#");
        thr.add("th").with("DeviceId");
        thr.add("th").with("Address");
        thr.add("th").with("Created");
        thr.add("th").with("Changed");
        thr.add("th").with("Control");
        thr.add("th").with("Pos");
        thr.add("th").with("X");
        HtmlGenerator.Tag tr;

        for(Map.Entry<String,MyToken> x: wssProcessor.tokens.entrySet()){
            tr = table.add("tr");

            td1 = tr.add("td").with(x.getKey());
            td2 = tr.add("td").with(x.getValue().getOwner());
            td3 = tr.add("td").with(new Date(x.getValue().getCreated()).toString());
            td4 = tr.add("td").with(new Date(x.getValue().getChanged()).toString());

            int indent = 0;
            for(Map.Entry<String,MyUser> y:x.getValue().users.entrySet()){
                if(indent>0) tr = table.add("tr");
                tr.add("td").with(y.getValue().getNumber());
                tr.add("td").add("a").with(y.getValue().getDeviceId()).with("href","/?list=user&token="+x.getKey()+"&id="+y.getValue().getDeviceId());
                tr.add("td").with(y.getValue().getAddress());
                tr.add("td").with(new Date(y.getValue().getCreated()).toString());
                tr.add("td").with(new Date(y.getValue().getChanged()).toString());
                tr.add("td").with(y.getValue().getControl());
                tr.add("td").with(y.getValue().getPositions().size());
                tr.add("td").add("a").with("Del").with("href","/?action=del&token="+x.getKey()+"&id="+y.getValue().getDeviceId());

                indent ++;
            }
            td1.with("rowspan",indent);
            td2.with("rowspan",indent);
            td3.with("rowspan",indent);
            td4.with("rowspan",indent);
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

        for(Map.Entry<String,MyWssServer.CheckReq> x: wssProcessor.ipToCheck.entrySet()){
            tr = table.add("tr");

            tr.add("td").with(x.getKey());
            tr.add("td").with(x.getValue().token.getId());
            tr.add("td").with(x.getValue().control);
            tr.add("td").with(new Date(x.getValue().timestamp).toString());

        }
    }

    private void tableUser(String tokenId, String userId) {

        MyToken token = wssProcessor.tokens.get(tokenId);
        MyUser user = token.users.get(userId);

        html.getBody().add("h1").with("User").add("small").add("small").add("a").with("[Del]").with("href","/?action=del&token="+tokenId+"&id="+userId);
        html.getBody().add("div").with("Token: "+tokenId);
        html.getBody().add("div").with("Address: "+user.getConnection().getRemoteSocketAddress());
        html.getBody().add("div").with("Number in token: "+user.getNumber());
        html.getBody().add("div").with("DeviceID: "+userId);
        html.getBody().add("div").with("Color: "+user.getColor());
        if(user.hasName()) html.getBody().add("div").with("Name: "+user.getName());
        html.getBody().add("div").with("Model: "+user.getModel());
        html.getBody().add("div").with("Created: "+new Date(user.getCreated()).toString());
        html.getBody().add("div").with("Changed: "+new Date(user.getChanged()).toString());

        HtmlGenerator.Tag table = html.getBody().add("table").with("border",1);
        HtmlGenerator.Tag tr = table.add("tr");
        tr.add("th").with("#");
        tr.add("th").with("Time");
        tr.add("th").with("Latitude");
        tr.add("th").with("Longitude");
        tr.add("th").with("Altitude");
        tr.add("th").with("Accuracy");
        tr.add("th").with("Bearing");
        tr.add("th").with("Speed");

        int count = 1;
        for(MyUser.MyPosition x: user.getPositions()){
            tr = table.add("tr");

            tr.add("td").add("a").with(count).with("target","_blank").with("href","http://maps.google.com/?q="+x.latitude+"+"+x.longitude+"&z=13");
            tr.add("td").with(new Date(x.timestamp).toString());
            tr.add("td").with(x.latitude);
            tr.add("td").with(x.longitude);
            tr.add("td").with(x.altitude);
            tr.add("td").with(x.accuracy);
            tr.add("td").with(x.bearing);
            tr.add("td").with(x.speed);

            count++;
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

    public MyWssServer getWssProcessor() {
        return wssProcessor;
    }

    public void setWssProcessor(MyWssServer wssProcessor) {
        this.wssProcessor = wssProcessor;
    }
}
