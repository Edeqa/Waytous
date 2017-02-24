package ru.wtg.whereaminowserver.servers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONObject;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.wtg.whereaminowserver.helpers.Common;
import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
import ru.wtg.whereaminowserver.holders.admin.AdminMainPageHolder;
import ru.wtg.whereaminowserver.interfaces.PageHolder;

import static java.awt.SystemColor.window;
import static ru.wtg.whereaminowserver.helpers.Constants.HTTPS_PORT;
import static ru.wtg.whereaminowserver.helpers.Constants.HTTP_PORT;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.CLASS;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.CONTENT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HREF;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.ID;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.LINK;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.META;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.NAME;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.REL;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.SCRIPT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.STYLESHEET;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TYPE;

/**
 * Created 10/5/16.
 */
public class MyHttpRedirectHandler implements HttpHandler {

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        try {
            URI uri = exchange.getRequestURI();
//            Headers headers = exchange.getRequestHeaders();
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
                String mobileRedirect = "orw://" + host + ":" + HTTP_PORT + "/track/" + tokenId;
                String webRedirect = "https://" + host + ":" + HTTPS_PORT + "/track/" + tokenId;

                HtmlGenerator html = new HtmlGenerator();

                html.getHead().add(SCRIPT).with(
                    "\nvar mobile = \"" + mobileRedirect + "\";" +
                    "\nvar web = \"" + webRedirect + "\";" +
                    "\nfunction noClient(){window.location.href = web;}" +
                    "\nwindow.location.href = mobile;\n" +
                    "\nsetTimeout(noClient,500);\n");

                Common.log("Redirect ->", mobileRedirect, "||", webRedirect);

                byte[] bytes = html.build().getBytes();
                try {
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                redirect(exchange, host, uri.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void redirect(HttpExchange exchange, String host, String path) throws IOException {
            String newUri = "https://" + host + ":" + HTTPS_PORT + path;
//            OutputStream os = exchange.getResponseBody();
//            os.write(response.getBytes());
//            os.close();

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
