package com.edeqa.waytousserver.interfaces;

import com.sun.net.httpserver.HttpExchange;

/**
 * Created 1/23/2017.
 */

public interface PageHolder {

    String getType();

    boolean perform(HttpExchange parts);
}
