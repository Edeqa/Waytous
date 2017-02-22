package ru.wtg.whereaminowserver.holders.tracking;

import org.json.JSONObject;

import java.util.ArrayList;

import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
import ru.wtg.whereaminowserver.interfaces.PageHolder;
import ru.wtg.whereaminowserver.servers.MyHttpAdminServer;
import ru.wtg.whereaminowserver.servers.MyHttpTrackingServer;

import static ru.wtg.whereaminowserver.helpers.Constants.SERVER_BUILD;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.CONTENT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HREF;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.LINK;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.MANIFEST;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.META;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.NAME;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.REL;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SCRIPT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SRC;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TITLE;

/**
 * Created 1/20/2017.
 */

public class TrackingMainPageHolder implements PageHolder {

    public static final String HOLDER_TYPE = "main";

    @SuppressWarnings("unused")
    private final MyHttpTrackingServer server;
    private HtmlGenerator html;
    private String part;
    private ArrayList<String> request;

    public TrackingMainPageHolder(MyHttpTrackingServer server) {
//        html = new HtmlGenerator();
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    public HtmlGenerator create(HtmlGenerator html,ArrayList<String> query) {
//        html.clear();

        html.getHead().add(TITLE).with("Tracking");

        JSONObject o = new JSONObject();
        o.put("page", part);
        o.put("request", request);
        o.put("version", SERVER_BUILD);

        html.getHead().add(META).with(NAME, "viewport").with(CONTENT, "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0");
        html.getHead().add(SCRIPT).with("data", o);
        html.getHead().add(SCRIPT).with(SRC, "/js/tracking/Main.js");

        return html;
    }

    public void addPart(String s) {
        part = s;
    }
    public void addRequest(ArrayList<String> s) {
        request = s;
    }
}
