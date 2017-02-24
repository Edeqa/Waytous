package ru.wtg.whereaminowserver.holders.admin;

import java.util.ArrayList;

import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
import ru.wtg.whereaminowserver.interfaces.PageHolder;
import ru.wtg.whereaminowserver.servers.MyHttpAdminHandler;

import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SCRIPT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SRC;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TITLE;

/**
 * Created 1/23/2017.
 */

@SuppressWarnings("unused")
public class AdminCreatePageHolder implements PageHolder {

    private final static String HOLDER_TYPE = "create";

    private final MyHttpAdminHandler server;
    private HtmlGenerator html;

    public AdminCreatePageHolder(MyHttpAdminHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    @Override
    public HtmlGenerator create(HtmlGenerator html,ArrayList<String> query) {
        this.html = html;
        html.clear();
        html.getHead().add(TITLE).with("Create");
        html.getHead().add(SCRIPT).with(SRC, "/js/admin/Create.js");

        return html;
    }

}
