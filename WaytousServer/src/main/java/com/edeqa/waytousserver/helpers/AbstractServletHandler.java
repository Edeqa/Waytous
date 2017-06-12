/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.edeqa.waytousserver.helpers;

import com.edeqa.waytousserver.servers.AbstractDataProcessor;
import com.edeqa.waytousserver.servers.DataProcessorFirebaseV1;
import com.edeqa.waytousserver.servers.MyWsServer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;

abstract public class AbstractServletHandler extends HttpServlet implements HttpHandler {

    private volatile Map<String,AbstractDataProcessor> dataProcessor;


    public AbstractServletHandler() {
        dataProcessor = new HashMap<>();
        setDataProcessor(new DataProcessorFirebaseV1());

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


    public AbstractDataProcessor getDataProcessor(String version) {
        if(dataProcessor.containsKey(version)) {
            return dataProcessor.get(version);
        } else {
            return dataProcessor.get("v1");
        }
    }

    public void setDataProcessor(AbstractDataProcessor dataProcessor) {
        this.dataProcessor.put(DataProcessorFirebaseV1.VERSION, dataProcessor);
    }


}
