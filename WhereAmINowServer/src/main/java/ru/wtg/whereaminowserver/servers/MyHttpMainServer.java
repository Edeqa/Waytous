package ru.wtg.whereaminowserver.servers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import ru.wtg.whereaminowserver.helpers.HtmlGenerator;

import static ru.wtg.whereaminowserver.helpers.Constants.WEB_ROOT_DIRECTORY;


/**
 * Created 1/19/17.
 */
public class MyHttpMainServer implements HttpHandler {

    private HtmlGenerator html = new HtmlGenerator();
    private volatile MyWssServer wssProcessor;

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        URI uri = exchange.getRequestURI();
//        System.out.println("\nMain server processing for " + uri);

        File root = new File(WEB_ROOT_DIRECTORY);
        File file = new File(root + uri.getPath()).getCanonicalFile();

        if(!file.isFile()) {
            file = new File(file.getAbsolutePath() + "/index.html");
        }

        if (!file.getPath().startsWith(root.getAbsolutePath())) {
            // Suspected path traversal attack: reject with 403 error.
            String response = "403 (Forbidden)\n";
            exchange.sendResponseHeaders(403, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else if (!file.isFile()) {

            // Object does not exist or is not a file: reject with 404 error.
            String response = "404 (Not Found)\n";
            exchange.sendResponseHeaders(404, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            // Object exists and is a file: accept with response code 200.

            if(uri.getPath().startsWith("/css/")) {
                exchange.getResponseHeaders().set("Content-Type", "text/css");
            } else if(uri.getPath().startsWith("/js/")) {
                exchange.getResponseHeaders().set("Content-Type", "text/javascript");
            } else {
                exchange.getResponseHeaders().set("Content-Type", "text/html");
            }
            exchange.sendResponseHeaders(200, 0);

            OutputStream os = exchange.getResponseBody();
            FileInputStream fs = new FileInputStream(file);
            final byte[] buffer = new byte[0x10000];
            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer,0,count);
            }
            fs.close();
            os.close();
        }
    }


    public MyWssServer getWssProcessor() {
        return wssProcessor;
    }

    public void setWssProcessor(MyWssServer wssProcessor) {
        this.wssProcessor = wssProcessor;
    }
}
