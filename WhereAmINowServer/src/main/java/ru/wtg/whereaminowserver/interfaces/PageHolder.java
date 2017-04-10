package ru.wtg.whereaminowserver.interfaces;

import java.util.ArrayList;

import ru.wtg.whereaminowserver.helpers.HtmlGenerator;

/**
 * Created 1/23/2017.
 */
public interface PageHolder {

    String getType();
    HtmlGenerator create(HtmlGenerator html,ArrayList<String> query);
}
