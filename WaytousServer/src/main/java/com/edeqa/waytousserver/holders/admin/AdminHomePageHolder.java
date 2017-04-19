package com.edeqa.waytousserver.holders.admin;

import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.MyHttpAdminHandler;

import java.util.ArrayList;

import static com.edeqa.waytousserver.helpers.HtmlGenerator.SCRIPT;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.SRC;
import static com.edeqa.waytousserver.helpers.HtmlGenerator.TITLE;


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

    public HtmlGenerator create(HtmlGenerator html, ArrayList<String> query) {
        this.html = html;
        html.clear();

        html.getHead().add(TITLE).with("Home");
        html.getHead().add(SCRIPT).with(SRC, "/js/admin/Home.js");

        return html;
    }

}
