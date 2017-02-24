package ru.wtg.whereaminowserver.holders.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
import ru.wtg.whereaminowserver.interfaces.PageHolder;
import ru.wtg.whereaminowserver.servers.MyHttpAdminHandler;

import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SCRIPT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SRC;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TITLE;

/**
 * Created 1/20/2017.
 */

@SuppressWarnings("unused")
public class AdminGroupPageHolder implements PageHolder {

    private static final String HOLDER_TYPE = "group";

    private final MyHttpAdminHandler server;
    private HtmlGenerator html;

    public AdminGroupPageHolder(MyHttpAdminHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    public HtmlGenerator create(HtmlGenerator html,ArrayList<String> query) {
        this.html = html;
        html.clear();

        header();

        return html;
    }

    private void header() {

        html.getHead().add(TITLE).with("Group");
        html.getHead().add(SCRIPT).with(SRC, "/js/admin/Group.js");

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

            server.getWainProcessor().removeUser(token,id);

        }

    }

}
