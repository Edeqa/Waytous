/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

abstract public class AbstractServletHandler extends HttpServlet implements HttpHandler {

    AbstractServletHandler() {
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestWrapper requestWrapper = new RequestWrapper();

        requestWrapper.setHttpServletRequest(req);
        requestWrapper.setHttpServletResponse(resp);

        internalPerform(requestWrapper);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setHttpServletRequest(req);
        requestWrapper.setHttpServletResponse(resp);

        internalPerform(requestWrapper);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setHttpExchange(exchange);
        internalPerform(requestWrapper);

    }

    abstract public void perform(RequestWrapper requestWrapper) throws IOException;

    private void internalPerform(RequestWrapper requestWrapper) throws IOException {
        perform(requestWrapper);
    }



}
