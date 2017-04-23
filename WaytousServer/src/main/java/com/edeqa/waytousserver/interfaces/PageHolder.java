package com.edeqa.waytousserver.interfaces;

import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.sun.net.httpserver.HttpExchange;

import java.net.URI;
import java.util.ArrayList;


/**
 * Created 1/23/2017.
 */
public interface PageHolder {

    String getType();

    boolean perform(HttpExchange parts);
}
