package com.edeqa.waytousserver.holders.admin;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.MyHttpAdminHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
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

        switch (uri.getPath()) {
            case "/admin/logs/log":
                printLog(exchange);
                return true;
            default:
                break;
        }

        return false;
    }

    private void printLog(HttpExchange exchange) {
        try {
            File file = new File(SENSITIVE.getLogFile());

            Common.log("Logs",file.getCanonicalPath());

            boolean gzip = true;
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.getResponseHeaders().set("Server", "WAIN/"+ Constants.SERVER_BUILD);
            exchange.getResponseHeaders().set("Accept-Ranges", "bytes");

            if(gzip){
                exchange.getResponseHeaders().set("Content-Encoding", "gzip");
            } else {
                exchange.getResponseHeaders().set("Content-Length", String.valueOf(file.length()));
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
                os.write(buffer,0,count);
            }
            fs.close();
            os.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
