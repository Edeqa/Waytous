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
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.A;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.CLASS;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.COLSPAN;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.DIV;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.H1;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HREF;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.ID;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.LINK;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.REL;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.ROWSPAN;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SMALL;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.STYLESHEET;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TABLE;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TARGET;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TD;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TH;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TITLE;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TR;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TYPE;


/**
 * Created 10/5/16.
 */
public class MyHttpAdminServer implements HttpHandler {

    private HtmlGenerator html = new HtmlGenerator();
    private volatile MyWssServer wssProcessor;


    @Override
    public void handle(HttpExchange exchange) throws IOException {

        System.out.println("\nAdmin server processing");

        StringBuilder builder = new StringBuilder();

        URI uri = exchange.getRequestURI();
        Map<String, List<String>> query = splitQuery(uri.toString());

        if(query.containsKey("text")){
            wssProcessor.sendToAll(query.get("text").toString(), null);
        }

        if(processQuery(query)){
            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        header();
        tableTokens();

        HtmlGenerator.Tag div = html.getBody().add(DIV).with(CLASS,"two_tables");
        tableIpToUser(div);
        try {
            tableIpToToken(div);
        } catch(Exception e){e.printStackTrace();}
        try {
            tableChecks();
        } catch(Exception e){e.printStackTrace();}

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

        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();

    }

    private void header() {

        html.addHead().add(TITLE).with("Admin");
        html.getHead().add(LINK).with(REL, STYLESHEET).with(TYPE,"text/css").with(HREF, "/css/admin.css");

//        html.getHead().add(META).with(HTTP_EQUIV,"refresh").with(CONTENT,2);

        html.addBody().with(CLASS,"body");

        html.getBody().add(DIV).with(CLASS, "version").with("Build: "+SERVER_BUILD);

    }

    private void tableTokens() {

        html.getBody().add(H1).with("Tokens");
        HtmlGenerator.Tag table = html.getBody().add(TABLE).with(ID, "tokens");
        HtmlGenerator.Tag thr = table.add(TR);
        HtmlGenerator.Tag td1 = thr.add(TH).with("Token").with(ROWSPAN,2);
        HtmlGenerator.Tag td2 = thr.add(TH).with("Owner").with(ROWSPAN,2);
        HtmlGenerator.Tag td3 = thr.add(TH).with("Created").with(ROWSPAN,2);
        HtmlGenerator.Tag td4 = thr.add(TH).with("Changed").with(ROWSPAN,2);

        thr.add(TH).with("Users").with(COLSPAN,8);

        thr = table.add(TR);
        thr.add(TH).with("#");
        thr.add(TH).with("Device");
        thr.add(TH).with("Address");
        thr.add(TH).with("Created");
        thr.add(TH).with("Changed");
        thr.add(TH).with("Control");
        thr.add(TH).with("Pos");
        thr.add(TH).with("X");
        HtmlGenerator.Tag tr;

        for(Map.Entry<String,MyToken> x: wssProcessor.tokens.entrySet()){
            tr = table.add(TR);

            td1 = tr.add(TD).with(x.getKey());
            td2 = tr.add(TD).with(x.getValue().getOwner().substring(0, 30) + "...");
            td3 = tr.add(TD).with(new Date(x.getValue().getCreated()).toString());
            td4 = tr.add(TD).with(new Date(x.getValue().getChanged()).toString());

            int indent = 0;
            for(Map.Entry<String,MyUser> y:x.getValue().users.entrySet()){
                if(indent>0) tr = table.add("tr");
                tr.add(TD).with(y.getValue().getNumber());
                tr.add(TD).add("a").with(y.getValue().getModel()).with(HREF,"/?list=user&token="+x.getKey()+"&id="+y.getValue().getDeviceId());
                tr.add(TD).with(y.getValue().getAddress());
                tr.add(TD).with(new Date(y.getValue().getCreated()).toString());
                tr.add(TD).with(new Date(y.getValue().getChanged()).toString());
                tr.add(TD).with(y.getValue().getControl());
                tr.add(TD);//.with(y.getValue().getPositions().size());
                tr.add(TD).add(A).with("Del").with(HREF,"/?action=del&token="+x.getKey()+"&id="+y.getValue().getDeviceId());

                indent ++;
            }
            td1.with(ROWSPAN,indent);
            td2.with(ROWSPAN,indent);
            td3.with(ROWSPAN,indent);
            td4.with(ROWSPAN,indent);
        }
    }

    private void tableChecks() {

        html.getBody().add(H1).with("Checks");
        HtmlGenerator.Tag table = html.getBody().add(TABLE);
        HtmlGenerator.Tag tr = table.add(TR);
        tr.add(TH).with("Address");
        tr.add(TH).with("Token");
        tr.add(TH).with("Control");
        tr.add(TH).with("Timestamp");

        for(Map.Entry<String,MyWssServer.CheckReq> x: wssProcessor.ipToCheck.entrySet()){
            tr = table.add(TR);

            tr.add(TD).with(x.getKey());
            tr.add(TD).with(x.getValue().token.getId());
            tr.add(TD).with(x.getValue().control);
            tr.add(TD).with(new Date(x.getValue().timestamp).toString());

        }
    }

    private void tableUser(String tokenId, String userId) {

        MyToken token = wssProcessor.tokens.get(tokenId);
        MyUser user = token.users.get(userId);

        html.getBody().add(H1).with("User").add(SMALL).add(SMALL).add(A).with("[Del]").with(HREF,"/?action=del&token="+tokenId+"&id="+userId);
        html.getBody().add(DIV).with("Token: "+tokenId);
        html.getBody().add(DIV).with("Address: "+user.getConnection().getRemoteSocketAddress());
        html.getBody().add(DIV).with("Number in token: "+user.getNumber());
        html.getBody().add(DIV).with("DeviceID: "+userId);
        html.getBody().add(DIV).with("Color: "+user.getColor());
        if(user.hasName()) html.getBody().add(DIV).with("Name: "+user.getName());
        html.getBody().add(DIV).with("Model: "+user.getModel());
        html.getBody().add(DIV).with("Created: "+new Date(user.getCreated()).toString());
        html.getBody().add(DIV).with("Changed: "+new Date(user.getChanged()).toString());

        HtmlGenerator.Tag table = html.getBody().add(TABLE);
        HtmlGenerator.Tag tr = table.add(TR);
        tr.add(TH).with("#");
        tr.add(TH).with("Time");
        tr.add(TH).with("Latitude");
        tr.add(TH).with("Longitude");
        tr.add(TH).with("Altitude");
        tr.add(TH).with("Accuracy");
        tr.add(TH).with("Bearing");
        tr.add(TH).with("Speed");

//        int count = 1;
//        for(MyUser.MyPosition x: user.getPositions()){
        MyUser.MyPosition x = user.getPosition();
        tr = table.add("tr");

        tr.add(TD).add(A).with(1/*count*/).with(TARGET,"_blank").with(HREF,"http://maps.google.com/?q="+x.latitude+"+"+x.longitude+"&z=13");
        tr.add(TD).with(new Date(x.timestamp).toString());
        tr.add(TD).with(x.latitude);
        tr.add(TD).with(x.longitude);
        tr.add(TD).with(x.altitude);
        tr.add(TD).with(x.accuracy);
        tr.add(TD).with(x.bearing);
        tr.add(TD).with(x.speed);

//            count++;
//        }
    }

    private void tableIpToUser(HtmlGenerator.Tag out) {

        HtmlGenerator.Tag div = out.add(DIV).with(CLASS,"table_ip_to_user");
        div.add(H1).with("IP to User corresponds");
        HtmlGenerator.Tag table = div.add(TABLE);
        HtmlGenerator.Tag tr = table.add(TR);
        tr.add(TH).with("IP");
        tr.add(TH).with("Device ID");

        for(Map.Entry<String,MyUser> x: wssProcessor.ipToUser.entrySet()){
            tr = table.add(TR);

            tr.add(TD).with(x.getKey());
            tr.add(TD).with(x.getValue().getDeviceId().substring(0,30)+"...");
        }
    }

    private void tableIpToToken(HtmlGenerator.Tag out) {

        HtmlGenerator.Tag div = out.add(DIV).with(CLASS, "table_ip_to_token");
        div.add(H1).with("IP to Token corresponds");
        HtmlGenerator.Tag table = div.add(TABLE);
        HtmlGenerator.Tag tr = table.add("tr");
        tr.add(TH).with("IP");
        tr.add(TH).with("Token ID");

        for(Map.Entry<String,MyToken> x: wssProcessor.ipToToken.entrySet()){
            tr = table.add("tr");

            tr.add(TD).with(x.getKey());
            tr.add(TD).with(x.getValue().getId());
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
