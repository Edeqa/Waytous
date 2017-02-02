package ru.wtg.whereaminowserver.holders.admin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import ru.wtg.whereaminowserver.helpers.Common;
import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.interfaces.PageHolder;
import ru.wtg.whereaminowserver.servers.MyHttpAdminServer;

import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HREF;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.LINK;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.REL;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SCRIPT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SRC;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.STYLESHEET;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TITLE;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TYPE;

/**
 * Created 1/23/2017.
 */

@SuppressWarnings("unused")
public class AdminUserPageHolder implements PageHolder {

    private final static String HOLDER_TYPE = "user";

    private final MyHttpAdminServer server;
    private HtmlGenerator html;

    public AdminUserPageHolder(MyHttpAdminServer server) {
        html = new HtmlGenerator();
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    @Override
    public HtmlGenerator create(ArrayList<String> query) {
        html.clear();

        html.getHead().add(TITLE).with("Group");
        html.getHead().add(LINK).with(REL, STYLESHEET).with(TYPE,"text/css").with(HREF, "/css/admin.css");

        Common.addIncludes(html);

        JSONObject o = new JSONObject();
        o.put("general",Common.fetchGeneralInfo());

        if(query.size() > 4) {
            String token = query.get(3);
            String user = query.get(4);

            o.put("user", fetchUserData(token, user));
        }
        html.getHead().add(SCRIPT).with("data", o);

        html.getHead().add(SCRIPT).with(SRC, "/js/admin/User.js");

        return html;
    }

    private JSONObject fetchUserData(String tokenId, String userId) {
        JSONObject o = new JSONObject();

        MyUser user = null;
        MyToken token = server.getWssProcessor().getTokens().get(tokenId);
        for(Map.Entry<String,MyUser> entry:token.users.entrySet()) {
            if(userId.equals(""+entry.getValue().getNumber())){
                user = entry.getValue();
            }
        }
            System.out.println(":::"+user);
        if(user != null) {

            o.put("token", tokenId);
            o.put("ip", user.webSocket.getRemoteSocketAddress());
            o.put("number", user.getNumber());
            o.put("deviceId", user.getDeviceId());
            o.put("color", user.getColor());
            o.put("name", user.getName());
            o.put("model", user.getModel());
            o.put("created", new Date(user.getCreated()).toString());
            o.put("changed", new Date(user.getChanged()).toString());

            JSONArray a = new JSONArray();
//        for(MyUser.MyPosition x: user.getPositions()){
            MyUser.MyPosition x = user.getPosition();
            JSONArray pa = new JSONArray();
            pa.put(1);
            pa.put(new Date(x.timestamp).toString());
            pa.put(x.latitude);
            pa.put(x.longitude);
            pa.put(x.altitude);
            pa.put(x.accuracy);
            pa.put(x.bearing);
            pa.put(x.speed);
            a.put(pa);
//            count++;
//        }
            o.put("positions", a);
        }
        return o;
    }

}
