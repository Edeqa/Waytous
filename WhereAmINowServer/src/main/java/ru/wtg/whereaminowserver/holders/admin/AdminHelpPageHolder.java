package ru.wtg.whereaminowserver.holders.admin;

import java.util.ArrayList;

import ru.wtg.whereaminowserver.helpers.Common;
import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
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
public class AdminHelpPageHolder implements PageHolder {

    private static final String HOLDER_TYPE = "help";

    private final MyHttpAdminServer server;
    private HtmlGenerator html;

    public AdminHelpPageHolder(MyHttpAdminServer server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }


    public HtmlGenerator create(HtmlGenerator html,ArrayList<String> query) {
        this.html=html;
        html.clear();

        html.getHead().add(TITLE).with("Help");
        html.getHead().add(SCRIPT).with(SRC, "/js/admin/Help.js");

        return html;
    }

}
