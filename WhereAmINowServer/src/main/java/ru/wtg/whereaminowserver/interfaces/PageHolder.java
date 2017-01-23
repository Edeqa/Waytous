package ru.wtg.whereaminowserver.interfaces;

import java.util.List;
import java.util.Map;

import ru.wtg.whereaminowserver.helpers.HtmlGenerator;

/**
 * Created by eduardm on 023, 1/23/2017.
 */
public interface PageHolder {

    String getType();
    HtmlGenerator create(Map<String, List<String>> query);
}
