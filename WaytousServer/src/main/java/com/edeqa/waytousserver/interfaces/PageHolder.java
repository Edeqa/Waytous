package com.edeqa.waytousserver.interfaces;

import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;


/**
 * Created 1/23/2017.
 */
public interface PageHolder {

    String getType();
    HtmlGenerator create(HtmlGenerator html, ArrayList<String> query, HttpExchange exchange);
}
