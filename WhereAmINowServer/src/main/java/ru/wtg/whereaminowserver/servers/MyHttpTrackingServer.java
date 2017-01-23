package ru.wtg.whereaminowserver.servers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;

import ru.wtg.whereaminowserver.helpers.Common;
import ru.wtg.whereaminowserver.helpers.HtmlGenerator;

import static ru.wtg.whereaminowserver.helpers.Constants.HTTP_PORT;
import static ru.wtg.whereaminowserver.helpers.Constants.WEB_ROOT_DIRECTORY;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.A;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.BR;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.CONTENT;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.DIV;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HREF;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.HTTP_EQUIV;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.ID;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.META;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.STYLE;
import static ru.wtg.whereaminowserver.helpers.HtmlGenerator.TITLE;

/**
 * Created 1/19/17.
 */
public class MyHttpTrackingServer implements HttpHandler {

    private HtmlGenerator html = new HtmlGenerator();
    private volatile MyWssServer wssProcessor;


    @Override
    public void handle(HttpExchange exchange) throws IOException {

        System.out.println("Tracking server requested");

        URI uri = exchange.getRequestURI();
        System.out.println(uri.getPath());
        System.out.println(exchange.getRemoteAddress().getAddress().getHostAddress());
        System.out.println(InetAddress.getLocalHost().getHostAddress());

        String[] parts = uri.getPath().split("/");
        String tokenId = null;

        if(parts.length >= 3){
            tokenId = parts[2];
        }

        if(tokenId == null) {

        } else {

        }

        File root = new File(WEB_ROOT_DIRECTORY);
        File file = new File(root + uri.getPath()).getCanonicalFile();

        HtmlGenerator.Tag head = html.getHead();
        head.add(TITLE).with("Tracking");

        head.add(DIV).with(ID,"tracking-token").with(tokenId).with(STYLE,"display:none");
//        head.add(SCRIPT).with(SRC, "https://code.jquery.com/jquery-3.1.1.min.js");
//        head.add(SCRIPT).with(SRC, "/js/tracking.js");

        HtmlGenerator.Tag body = html.getBody();
        body.with("Here will be a web version soon...");
        body.add(BR);

        if(tokenId != null) {
            String mobileRedirect = "orw://" + InetAddress.getLocalHost().getHostAddress() + ":" + HTTP_PORT + "/track/" + tokenId;
            System.out.println("Mobile redirect generated: " + mobileRedirect);
            head.add(META).with(HTTP_EQUIV, "refresh").with(CONTENT, "0;URL='" + mobileRedirect + "'");
            body.add(A).with(HREF, mobileRedirect).with("Click here for start mobile client");
        }

        Common.addIncludes(html);

        byte[] bytes = html.build().getBytes();

        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();


    }


    public MyWssServer getWssProcessor() {
        return wssProcessor;
    }

    public void setWssProcessor(MyWssServer wssProcessor) {
        this.wssProcessor = wssProcessor;
    }



}
