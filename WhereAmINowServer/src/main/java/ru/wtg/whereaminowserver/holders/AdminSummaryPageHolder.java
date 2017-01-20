package ru.wtg.whereaminowserver.holders;

import com.sun.net.httpserver.HttpExchange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.servers.MyHttpAdminServer;
import ru.wtg.whereaminowserver.servers.MyWssServer;

import static ru.wtg.whereaminowserver.helpers.Constants.SERVER_BUILD;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.A;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.CLASS;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.COLSPAN;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.CONTENT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.DIV;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.FORM;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.H1;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HREF;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HTTP_EQUIV;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.ID;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.INPUT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.LINK;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.META;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.NAME;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.REL;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.ROWSPAN;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SCRIPT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SMALL;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SRC;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.STYLESHEET;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TABLE;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TARGET;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TD;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TH;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TITLE;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TR;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TYPE;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.VALUE;

/**
 * Created 1/20/2017.
 */

public class AdminSummaryPageHolder {

    private final MyHttpAdminServer server;
    private HtmlGenerator html;

    public AdminSummaryPageHolder(MyHttpAdminServer server) {
        html = new HtmlGenerator();
        this.server = server;
    }

    public HtmlGenerator create(Map<String, List<String>> query) {
        html.clear();

        header();

//        html.addBody().with(CLASS,"body");

        html.getBody().add(DIV).add(FORM).with(NAME, "publish")
                .add(INPUT).with(TYPE,"text").with(NAME, "message")
                .add(INPUT).with(TYPE, "submit").with(VALUE, "Send");

//        html.getBody().add(DIV).with(ID, "subscript");



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

//        html = new HtmlGenerator();
//        html.addHead().add(TITLE).with("Admin");

        return html;
    }

    private void header() {

        html.addHead().add(TITLE).with("Admin");
        html.getHead().add(LINK).with(REL, STYLESHEET).with(TYPE,"text/css").with(HREF, "/css/admin.css");
//        html.getHead().add(META).with(HTTP_EQUIV,"refresh").with(CONTENT,2);

        html.getHead().add(SCRIPT).with(SRC, "https://code.jquery.com/jquery-3.1.1.min.js");
        html.getHead().add(SCRIPT).with(SRC, "/js/utils.js");

        JSONObject o = new JSONObject();
        o.put("tokens",fetchTokensData());


        html.getHead().add(SCRIPT).with("data", o);
        html.getHead().add(SCRIPT).with(SRC, "/js/summary.js");


        html.addBody().with(CLASS,"body");
        html.getBody().add(DIV).with(CLASS, "version").with("Build: "+SERVER_BUILD);

    }

    private JSONArray fetchTokensData() {
        JSONArray a = new JSONArray();

        for (Map.Entry<String, MyToken> x : server.getWssProcessor().tokens.entrySet()) {
            JSONObject o = new JSONObject();
            a.put(o);

            o.put("id",x.getKey());
            o.put("owner",x.getValue().getOwner().substring(0, 30) + "...");
            o.put("created",new Date(x.getValue().getCreated()).toString());
            o.put("changed",new Date(x.getValue().getChanged()).toString());

            JSONArray ua = new JSONArray();
            o.put("users", ua);

            for (Map.Entry<String, MyUser> y : x.getValue().users.entrySet()) {
                JSONObject uo = new JSONObject();
                ua.put(uo);

                uo.put("number",y.getValue().getNumber());
                uo.put("model",y.getValue().getModel());
                uo.put("deviceId",y.getValue().getDeviceId());
                uo.put("address",y.getValue().getAddress());
                uo.put("created",new Date(y.getValue().getCreated()).toString());
                uo.put("changed",new Date(y.getValue().getChanged()).toString());
                uo.put("control",y.getValue().getControl());

            }
        }
        return a;
    }

    private void tableTokens() {
        html.getBody().add(H1).with("Tokens");
        HtmlGenerator.Tag table = html.getBody().add(TABLE).with(ID, "tokens");
        HtmlGenerator.Tag thr = table.add(TR);
        HtmlGenerator.Tag td1 = thr.add(TH).with("Token").with(ROWSPAN,2);
        HtmlGenerator.Tag td2 = thr.add(TH).with("Owner").with(ROWSPAN,2);
        HtmlGenerator.Tag td3 = thr.add(TH).with("Created").with(ROWSPAN,2);
        HtmlGenerator.Tag td4 = thr.add(TH).with("Changed").with(ROWSPAN,2);

        thr.add(TH).with("Users").with(COLSPAN, 8);

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

            for (Map.Entry<String, MyToken> x : server.getWssProcessor().tokens.entrySet()) {
                tr = table.add(TR);

                td1 = tr.add(TD).with(x.getKey());
                td2 = tr.add(TD).with(x.getValue().getOwner().substring(0, 30) + "...");
                td3 = tr.add(TD).with(new Date(x.getValue().getCreated()).toString());
                td4 = tr.add(TD).with(new Date(x.getValue().getChanged()).toString());

                int indent = 0;
                for (Map.Entry<String, MyUser> y : x.getValue().users.entrySet()) {
                    if (indent > 0) tr = table.add("tr");
                    tr.add(TD).with(y.getValue().getNumber());
                    tr.add(TD).add("a").with(y.getValue().getModel()).with(HREF, "/?list=user&token=" + x.getKey() + "&id=" + y.getValue().getDeviceId());
                    tr.add(TD).with(y.getValue().getAddress());
                    tr.add(TD).with(new Date(y.getValue().getCreated()).toString());
                    tr.add(TD).with(new Date(y.getValue().getChanged()).toString());
                    tr.add(TD).with(y.getValue().getControl());
                    tr.add(TD);//.with(y.getValue().getPositions().size());
                    tr.add(TD).add(A).with("Del").with(HREF, "/?action=del&token=" + x.getKey() + "&id=" + y.getValue().getDeviceId());

                    indent++;
                }
                td1.with(ROWSPAN, indent);
                td2.with(ROWSPAN, indent);
                td3.with(ROWSPAN, indent);
                td4.with(ROWSPAN, indent);
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

        for(Map.Entry<String,MyWssServer.CheckReq> x: server.getWssProcessor().ipToCheck.entrySet()){
            tr = table.add(TR);

            tr.add(TD).with(x.getKey());
            tr.add(TD).with(x.getValue().getToken().getId());
            tr.add(TD).with(x.getValue().getControl());
            tr.add(TD).with(new Date(x.getValue().getTimestamp()).toString());

        }
    }

    private void tableUser(String tokenId, String userId) {

        MyToken token = server.getWssProcessor().tokens.get(tokenId);
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

        for(Map.Entry<String,MyUser> x: server.getWssProcessor().ipToUser.entrySet()){
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

        for(Map.Entry<String,MyToken> x: server.getWssProcessor().ipToToken.entrySet()){
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

            server.getWssProcessor().removeUser(token,id);

        }

    }

}
