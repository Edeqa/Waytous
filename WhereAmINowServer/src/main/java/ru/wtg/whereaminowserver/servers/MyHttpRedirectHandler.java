package ru.wtg.whereaminowserver.servers;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import ru.wtg.whereaminowserver.helpers.Common;

import static ru.wtg.whereaminowserver.helpers.Constants.SENSITIVE;

/**
 * Created 10/5/16.
 */
public class MyHttpRedirectHandler implements HttpHandler {

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        try {
            URI uri = exchange.getRequestURI();
            String host;
            try {
                host = exchange.getRequestHeaders().get("Host").get(0);
                host = host.split(":")[0];
            } catch(Exception e){
                e.printStackTrace();
                host = InetAddress.getLocalHost().getHostAddress();
            }

            Common.log("Redirect",host + uri.getPath());

            ArrayList<String> parts = new ArrayList<String>();
            parts.addAll(Arrays.asList(uri.getPath().split("/")));

            String tokenId = null;

            if(parts.size() >= 3){
                tokenId = parts.get(2);
            }

            if(uri.getPath().startsWith("/track/") && tokenId != null) {
                String mobileRedirect = "wain://" + host + "/track/" + tokenId;
                String webRedirect = "https://" + host + ":" + SENSITIVE.getHttpsPort() + "/group/" + tokenId;
                String mainLink = "https://" + host + ":" + SENSITIVE.getHttpsPort() + "/track/" + tokenId;

                String redirectLink = "http://"+ SENSITIVE.getFirebaseDynamicLinkHost()+"/?"
                        + "link=" + mainLink
                        + "&apn=ru.wtg.whereaminow"
                        + "&al=" + mobileRedirect
                        + "&afl=" + webRedirect
                        + "&ifl=" + webRedirect
                        + "&st=Waytogo"
                        + "&sd=Waytogo+description"
                        + "&si=https://raw.githubusercontent.com/tujger/WhereAmINow/master/WhereAmINowServer/src/main/webapp/images/logo.png";

                Common.log("Redirect ->", redirectLink);

                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "text/plain");
                responseHeaders.set("Date", new Date().toString());
                responseHeaders.set("Location", redirectLink);
                exchange.sendResponseHeaders(302, 0);
                exchange.close();

            } else {
                redirect(exchange, host, uri.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void redirect(HttpExchange exchange, String host, String path) throws IOException {
        String newUri = "https://" + host + ":" + SENSITIVE.getHttpsPort() + path;

        Common.log("Redirect ->", newUri);

        String requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("GET")) {
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/plain");
            responseHeaders.set("Date", new Date().toString());
            responseHeaders.set("Location", newUri);
            exchange.sendResponseHeaders(302, 0);
        }
    }

}
