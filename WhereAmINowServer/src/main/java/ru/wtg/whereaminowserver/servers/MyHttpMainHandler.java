package ru.wtg.whereaminowserver.servers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import ru.wtg.whereaminowserver.helpers.Common;
import ru.wtg.whereaminowserver.helpers.HtmlGenerator;

import static ru.wtg.whereaminowserver.helpers.Constants.SENSITIVE;
import static ru.wtg.whereaminowserver.helpers.Constants.SERVER_BUILD;

/**
 * Created 1/19/17.
 */
public class MyHttpMainHandler implements HttpHandler {

    private HtmlGenerator html = new HtmlGenerator();
    private volatile AbstractWainProcessor wainProcessor;

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        boolean gzip = false;
        String ifModifiedSince = null;

        URI uri = exchange.getRequestURI();

        for (Map.Entry<String, List<String>> entry : exchange.getRequestHeaders().entrySet()) {
            if ("accept-encoding".equals(entry.getKey().toLowerCase())) {
                for (String s : entry.getValue()) {
                    for (String ss : s.split(", ")) {
                        if ("gzip".equals(ss.toLowerCase())) {
                            gzip = true;
                            break;
                        }
                    }
                }
            } else if ("if-none-match".equals(entry.getKey().toLowerCase())) {

            }
        }

        File root = new File(SENSITIVE.getWebRootDirectory());
        File file = new File(root + uri.getPath()).getCanonicalFile();

        Common.log("Main",uri.getPath(),"(" + (file.exists() ? file.length() +" byte(s)" : "not found") + ")" );

        String etag = "W/1976" + ("" + file.lastModified()).hashCode();

        String path = uri.getPath().toLowerCase();
        if (!file.isFile()) {
            file = new File(file.getCanonicalPath() + "/index.html");
        }
        if(etag.equals(ifModifiedSince)) {
            String response = "304 Not Modified\n";
            exchange.sendResponseHeaders(304, 0);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else if (!file.getCanonicalPath().startsWith(root.getCanonicalPath())) {
            // Suspected path traversal attack: reject with 403 error.
            String response = "403 Forbidden\n";
            exchange.sendResponseHeaders(403, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else if (!file.isFile() || path.startsWith("/WEB-INF") || path.startsWith("/META-INF") || path.startsWith("/.idea")) {
            // Object does not exist or is not a file: reject with 404 error.
            String response = "404 Not Found\n";
            exchange.sendResponseHeaders(404, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            // Object exists and is a file: accept with response code 200.

            if(path.startsWith("/css/")) {
                exchange.getResponseHeaders().set("Content-Type", "text/css");
            } else if(path.startsWith("/js/")) {
                exchange.getResponseHeaders().set("Content-Type", "text/javascript");
            } else if(path.endsWith(".xml")) {
                exchange.getResponseHeaders().set("Content-Type", "application/xml");
            } else if(path.endsWith(".json")) {
                exchange.getResponseHeaders().set("Content-Type", "application/json");
            } else if(path.endsWith("manifest.json")) {
                exchange.getResponseHeaders().set("Content-Type", "application/x-web-app-manifest+json");
            } else if(path.endsWith(".gif") || path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".svg") || path.endsWith(".ico")) {
                String type = "image/";
                String[] parts = file.getName().split("\\.");
                if(parts.length>1){
                    type += parts[parts.length-1].toLowerCase();
                    if("svg".equals(parts[parts.length-1].toLowerCase())) type += "+xml";
                } else type += "*";
                gzip = false;
                exchange.getResponseHeaders().set("Content-Type", type);
            } else {
                String type = "text/html";
                String[] parts = file.getName().split("\\.");
                if(parts.length>1){
                    if("js".equals(parts[parts.length-1].toLowerCase())) {
                        type = "text/javascript";
                    }
                }
                exchange.getResponseHeaders().set("Content-Type", type);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
            String lastModified = dateFormat.format(file.lastModified());

            exchange.getResponseHeaders().set("Last-Modified", lastModified);
            exchange.getResponseHeaders().set("Cache-Control", SENSITIVE.isDebugMode() ? "max-age=10" : "max-age=120");
            exchange.getResponseHeaders().set("ETag", etag);
            exchange.getResponseHeaders().set("Server", "WAIN/"+SERVER_BUILD);
            exchange.getResponseHeaders().set("Accept-Ranges", "bytes");

//            gzip = false;
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
        }

    }

    public AbstractWainProcessor getWainProcessor() {
        return wainProcessor;
    }

    public void setWainProcessor(AbstractWainProcessor wainProcessor) {
        this.wainProcessor = wainProcessor;
    }
}
