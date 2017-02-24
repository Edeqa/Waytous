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
public class AdminHomePageHolder implements PageHolder {

    private static final String HOLDER_TYPE = "home";

    private final MyHttpAdminHandler server;
    private HtmlGenerator html;

    public AdminHomePageHolder(MyHttpAdminHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    public HtmlGenerator create(HtmlGenerator html,ArrayList<String> query) {
        this.html = html;
        html.clear();

        html.getHead().add(TITLE).with("Home");
        html.getHead().add(SCRIPT).with(SRC, "/js/admin/Home.js");

        return html;
    }

}
