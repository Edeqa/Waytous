package ru.wtg.whereaminowserver.holders.admin;

import org.json.JSONObject;

import java.util.ArrayList;

import ru.wtg.whereaminowserver.helpers.Common;
import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
import ru.wtg.whereaminowserver.interfaces.PageHolder;
import ru.wtg.whereaminowserver.servers.MyHttpAdminServer;

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
 * Created 1/20/2017.
 */

public class AdminMainPageHolder implements PageHolder {

    public static final String HOLDER_TYPE = "main";

    @SuppressWarnings("unused")
    private final MyHttpAdminServer server;
    private HtmlGenerator html;
    private String part;

    public AdminMainPageHolder(MyHttpAdminServer server) {
        html = new HtmlGenerator();
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    public HtmlGenerator create(ArrayList<String> query) {
        html.clear();

        Common.addIncludes(html);
        html.getHead().add(TITLE).with("Admin");
        html.getHead().add(LINK).with(REL, STYLESHEET).with(TYPE,"text/css").with(HREF, "/css/admin.css");

        JSONObject o = new JSONObject();
        o.put("page", part);

        html.getHead().add(SCRIPT).with("data", o);
        html.getHead().add(SCRIPT).with(SRC, "/js/admin/Main.js");

        html.getBody().with(CLASS,"body");
        html.getBody().add(DIV).with(CLASS, "version").with("Build: "+SERVER_BUILD);


        return html;
    }

    public void addPart(String s) {
        part = s;
    }
}
