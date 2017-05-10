package com.edeqa.waytousserver.holders.admin;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.MyHttpAdminHandler;
import com.google.api.client.http.HttpMethods;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;


/**
 * Created 4/20/2017.
 */

@SuppressWarnings("unused")
public class AdminLogsHolder implements PageHolder {

    private static final String HOLDER_TYPE = "logs";

    private final MyHttpAdminHandler server;
    private HtmlGenerator html;

    public AdminLogsHolder(MyHttpAdminHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    @Override
    public boolean perform(HttpExchange exchange) {

        URI uri = exchange.getRequestURI();

        switch(exchange.getRequestMethod()) {
            case HttpMethods.GET:
                switch (uri.getPath()) {
                    case "/admin/logs/log":
                        printLog(exchange);
                        return true;
                    default:
                        break;
                }
                break;
            case HttpMethods.PUT:
                switch (uri.getPath()) {
                    case "/admin/logs/clear":
                        clearLog(exchange);
                        return true;
                    default:
                        break;
                }
                break;
            case HttpMethods.POST:
                break;
        }


        return false;
    }

    private void clearLog(HttpExchange exchange) {
        try {
            File file = new File(SENSITIVE.getLogFile());
            Common.log("Logs", "Clear:", file.getCanonicalPath());

            PrintWriter writer = new PrintWriter(file);
            writer.close();

            byte[] bytes = "".getBytes();

            exchange.getResponseHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, "text/plain");
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void printLog(HttpExchange exchange) {
        try {
            File file = new File(SENSITIVE.getLogFile());

            Common.log("Logs","Update:",file.getCanonicalPath());

            if(!file.exists()) {
                Common.log("Logs","File not found.");
                exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, "text/plain");
                exchange.getResponseHeaders().set(HttpHeaders.SERVER, "WAIN/"+ Constants.SERVER_BUILD);
                exchange.getResponseHeaders().set(HttpHeaders.ACCEPT_RANGES, "bytes");

                exchange.sendResponseHeaders(500, 0);

                byte[] bytes = (file.toString() + " not found. Fix the key 'log_file' in your options file.").getBytes();

                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
                return;
            }


            boolean gzip = true;
            exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, "text/plain");
            exchange.getResponseHeaders().set(HttpHeaders.SERVER, "WAIN/"+ Constants.SERVER_BUILD);
            exchange.getResponseHeaders().set(HttpHeaders.ACCEPT_RANGES, "bytes");

            if(gzip){
                exchange.getResponseHeaders().set(HttpHeaders.CONTENT_ENCODING, "gzip");
            } else {
                exchange.getResponseHeaders().set(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));
            }

            exchange.sendResponseHeaders(200, 0);

            OutputStream os;
            if(gzip) {
                os = new BufferedOutputStream(new GZIPOutputStream(exchange.getResponseBody()));
            } else {
                os = exchange.getResponseBody();
            }

            FileInputStream fs = new FileInputStream(file);
            final byte[] buffer = new byte[0x10000];

            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
            }
            fs.close();
            os.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
