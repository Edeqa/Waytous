/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.edeqa.waytousserver.helpers.SensitiveData;
import com.google.firebase.database.FirebaseDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

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

    AbstractServletHandler() {
    }

    @Override
    public void init() throws ServletException {
        super.init();

        if(SENSITIVE == null) {
            String sensitiveData = getServletContext().getInitParameter("sensitiveData");
            SENSITIVE = new SensitiveData(new String[]{sensitiveData});
        }
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

        if(Common.getInstance().getDataProcessor(DataProcessorFirebaseV1.VERSION) == null) {
            try {
                System.out.println("A");
                DataProcessorFirebaseV1 a = new DataProcessorFirebaseV1();
                System.out.println("B");
                try {
                    a.setRef(FirebaseDatabase.getInstance().getReference());
                }catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("C");
                Common.getInstance().setDataProcessor(a);
                System.out.println("D");
            } catch (ServletException | FileNotFoundException e) {
                e.printStackTrace();
//                requestWrapper.sendResponseHeaders(500,0);
//                requestWrapper.getResponseBody().write(e.getMessage().getBytes());
            }
        }

        RequestWrapper requestWrapper = new RequestWrapper();
        requestWrapper.setHttpExchange(exchange);
        internalPerform(requestWrapper);

    }

    abstract public void perform(RequestWrapper requestWrapper) throws IOException;

    private void internalPerform(RequestWrapper requestWrapper) throws IOException {
        perform(requestWrapper);
    }


 }
