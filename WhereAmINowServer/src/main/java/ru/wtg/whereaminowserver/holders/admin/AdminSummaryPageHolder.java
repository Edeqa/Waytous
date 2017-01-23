package ru.wtg.whereaminowserver.holders.admin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.wtg.whereaminowserver.helpers.Common;
import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.interfaces.PageHolder;
import ru.wtg.whereaminowserver.servers.MyHttpAdminServer;
import ru.wtg.whereaminowserver.servers.MyWssServer;

import static ru.wtg.whereaminowserver.helpers.Constants.SERVER_BUILD;
import static ru.wtg.whereaminowserver.helpers.Constants.WSS_PORT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.A;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.CLASS;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.DIV;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.FORM;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.H1;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HREF;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.INPUT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.LINK;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.NAME;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.REL;
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

public class AdminSummaryPageHolder implements PageHolder {

    public static final String HOLDER_TYPE = "summary";

    private final MyHttpAdminServer server;
    private HtmlGenerator html;

    public AdminSummaryPageHolder(MyHttpAdminServer server) {
        html = new HtmlGenerator();
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    public HtmlGenerator create(Map<String, List<String>> query) {
        html.clear();

        header();

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

        return html;
    }

    private void header() {

        html.getHead().add(TITLE).with("Admin");
        html.getHead().add(LINK).with(REL, STYLESHEET).with(TYPE,"text/css").with(HREF, "/css/admin.css");

        Common.addIncludes(html);

        JSONObject o = new JSONObject();
        o.put("general",fetchGeneralInfo());
        o.put("tokens",fetchTokensData());
        o.put("ipToUser",fetchIpToUserData());
        o.put("ipToToken",fetchIpToTokenData());
        o.put("ipToCheck",fetchIpToCheckData());

        html.getHead().add(SCRIPT).with("data", o);
        html.getHead().add(SCRIPT).with(SRC, "/js/admin/summary.js");


        html.getBody().with(CLASS,"body");
        html.getBody().add(DIV).with(CLASS, "version").with("Build: "+SERVER_BUILD);

    }

    private JSONObject fetchGeneralInfo() {
        JSONObject o = new JSONObject();

        try {
            String wss = "ws://" + InetAddress.getLocalHost().getHostAddress() + ":" + WSS_PORT;
            o.put("uri", wss);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return o;
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
                uo.put("deviceId",y.getValue().getDeviceId().substring(0, 30) + "...");
                uo.put("address",y.getValue().getAddress());
                uo.put("created",new Date(y.getValue().getCreated()).toString());
                uo.put("changed",new Date(y.getValue().getChanged()).toString());
                uo.put("control",y.getValue().getControl());

            }
        }
        return a;
    }

    private JSONArray fetchIpToUserData() {
        JSONArray a = new JSONArray();

        for(Map.Entry<String,MyUser> x: server.getWssProcessor().ipToUser.entrySet()){
            JSONArray ua = new JSONArray();
            a.put(ua);
            ua.put(x.getKey());
            ua.put(x.getValue().getDeviceId().substring(0,30)+"...");
        }
        return a;
    }

    private JSONArray fetchIpToTokenData() {
        JSONArray a = new JSONArray();

        for(Map.Entry<String,MyToken> x: server.getWssProcessor().ipToToken.entrySet()){
            JSONArray ta = new JSONArray();
            a.put(ta);
            ta.put(x.getKey());
            ta.put(x.getValue().getId());
        }
        return a;
    }

    private JSONArray fetchIpToCheckData() {
        JSONArray a = new JSONArray();

        for(Map.Entry<String,MyWssServer.CheckReq> x: server.getWssProcessor().ipToCheck.entrySet()){
            JSONArray ca = new JSONArray();
            a.put(ca);
            ca.put(x.getKey());
            ca.put(x.getValue().getToken().getId());
            ca.put(x.getValue().getControl());
            ca.put(new Date(x.getValue().getTimestamp()).toString());
        }
        return a;
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
