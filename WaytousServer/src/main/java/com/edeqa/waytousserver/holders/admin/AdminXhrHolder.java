
package com.edeqa.waytousserver.holders.admin;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.MyHttpAdminHandler;
import com.google.api.client.http.HttpMethods;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.HttpExchange;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.zip.GZIPOutputStream;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;


/**
 * Created 4/20/2017.
 */

@SuppressWarnings("unused")
public class AdminXhrHolder implements PageHolder {

    private static final String HOLDER_TYPE = "xhr";

    private final MyHttpAdminHandler server;
    private HtmlGenerator html;

    public AdminXhrHolder(MyHttpAdminHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    @Override
    public boolean perform(HttpExchange exchange) {

        URI uri = exchange.getRequestURI();

        Common.log("AXH", exchange.getRemoteAddress(), uri.getPath());

        switch(exchange.getRequestMethod()) {
            case HttpMethods.GET:
                switch (uri.getPath()) {
                    case "/admin/logs/logaaaaaaaa":
                        break;
                    default:
                        break;
                }
                break;
            case HttpMethods.PUT:
                break;
            case HttpMethods.POST:
                switch (uri.getPath()) {
                    case "/admin/xhr/group/create":
                        createGroup(exchange);
                        return true;
                    default:
                        break;
                }
                break;
        }

        return false;
    }

    private void createGroup(HttpExchange exchange) {
        try {
            File file = new File(SENSITIVE.getLogFile());

            StringBuilder buf = new StringBuilder();
            InputStream is = exchange.getRequestBody();
            int b;
            while((b = is.read()) != -1) {
                buf.append((char) b);
            }

            is.close();
            String options = buf.toString();

            Common.log("AXH", "createGroup:", options);

            JSONObject json = new JSONObject();
            json.put("firstkey","firstvalue");
            json.put("secondkey","secondvalue");
            json.put("body",options);




            byte[] bytes = json.toString().getBytes();

            exchange.getResponseHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }


    /*private interface SimpleCallback {
        call(HttpExchange exchange, )
    }*/



    private Runnable sendResult = new Runnable() {
        @Override
        public void run() {

        }
    };

}
