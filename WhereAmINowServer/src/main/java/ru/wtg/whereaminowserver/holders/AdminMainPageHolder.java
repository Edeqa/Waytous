package ru.wtg.whereaminowserver.holders;

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
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.DIV;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.FORM;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.H1;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HREF;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.ID;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.INPUT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.LINK;
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

public class AdminMainPageHolder {

    private final MyHttpAdminServer server;
    private HtmlGenerator html;

    public AdminMainPageHolder(MyHttpAdminServer server) {
        html = new HtmlGenerator();
        this.server = server;
    }

    public HtmlGenerator create(Map<String, List<String>> query) {
        html.clear();

        html = new HtmlGenerator();
        html.addHead().add(TITLE).with("Admin");
        html.getHead().add(LINK).with(REL, STYLESHEET).with(TYPE,"text/css").with(HREF, "/css/admin.css");
        html.getHead().add(SCRIPT).with(SRC, "https://code.jquery.com/jquery-3.1.1.min.js");
        html.getHead().add(SCRIPT).with(SRC, "/js/utils.js");

        JSONObject o = new JSONObject();




        html.getHead().add(SCRIPT).with("data", o);
        html.getHead().add(SCRIPT).with(SRC, "/js/admin.js");

        html.addBody().with(CLASS,"body");
        html.getBody().add(DIV).with(CLASS, "version").with("Build: "+SERVER_BUILD);



        return html;
    }

}
