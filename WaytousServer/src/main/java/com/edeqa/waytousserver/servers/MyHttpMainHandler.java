package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.Utils;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import sun.misc.Regexp;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;
import static com.edeqa.waytousserver.helpers.Constants.SERVER_BUILD;


/**
 * Created 1/19/17.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class MyHttpMainHandler implements HttpHandler {

    private volatile AbstractDataProcessor dataProcessor;
    private Map<String, String> substitutions;

    @SuppressWarnings("HardCodedStringLiteral")
    public MyHttpMainHandler() {

        substitutions = new LinkedHashMap<>();
        substitutions.put("\\$\\{SERVER_BUILD\\}", ""+ SERVER_BUILD);
        substitutions.put("\\$\\{APP_NAME\\}", SENSITIVE.getAppName() + (SENSITIVE.isDebugMode() ? " &beta;" : ""));
        substitutions.put("\\$\\{SUPPORT_EMAIL\\}", SENSITIVE.getSupportEmail());
        substitutions.put("\\$\\{WEB_PAGE\\}", SENSITIVE.getAppLink());

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {
            String ifModifiedSince = null;

            URI uri = exchange.getRequestURI();


            File root = new File(SENSITIVE.getWebRootDirectory());
            File file = new File(root + uri.getPath()).getCanonicalFile();
            int resultCode = 200;

            Common.log("Main", uri.getPath(), "[" + (file.exists() ? file.length() + " byte(s)" : "not found") + "]");

            String etag = "W/1976" + ("" + file.lastModified()).hashCode();

            String path = uri.getPath().toLowerCase();
            if (!file.getCanonicalPath().startsWith(root.getCanonicalPath())) {
                // Suspected path traversal attack: reject with 403 error.
                resultCode = 403;
                file = new File(root + "/403.html");
//                Utils.sendResult.call(exchange, 403, Constants.MIME.TEXT_PLAIN, "403 Forbidden\n".getBytes());
            } else if (file.isDirectory()) {
                file = new File(file.getCanonicalPath() + "/index.html");
            } else if (etag.equals(ifModifiedSince)) {
                resultCode = 304;
                file = new File(root + "/304.html");
//                Utils.sendResult.call(exchange, 304, null, "304 Not Modified\n".getBytes());
            } else if (!uri.getPath().endsWith("/") && !file.exists()) {
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set(HttpHeaders.CONTENT_TYPE, Constants.MIME.TEXT_PLAIN);
                responseHeaders.set(HttpHeaders.DATE, new Date().toString());
                responseHeaders.set(HttpHeaders.LOCATION, uri.getPath() + "/");
                exchange.sendResponseHeaders(302, 0);
                exchange.close();
                return;
            } else if (!file.isFile() || path.startsWith("/WEB-INF") || path.startsWith("/META-INF") || path.startsWith("/.idea")) {
                // Object does not exist or is not a file: reject with 404 error.
                resultCode = 404;
                file = new File(root + "/404.html");
            }
             {
                // Object exists and it is a file: accept with response code 200.

                boolean gzip = false;
                for (Map.Entry<String, List<String>> entry : exchange.getRequestHeaders().entrySet()) {
                    if (HttpHeaders.ACCEPT_ENCODING.toLowerCase().equals(entry.getKey().toLowerCase())) {
                        for (String s : entry.getValue()) {
                            if(s.toLowerCase().contains("gzip")) {
                                gzip = true;
                                break;
                            }
                        }
                    } else if (HttpHeaders.IF_NONE_MATCH.toLowerCase().equals(entry.getKey().toLowerCase())) {

                    }
                }

                boolean text = false;
                String type = Constants.MIME.APPLICATION_OCTET_STREAM;
                JSONArray types = SENSITIVE.getTypes();
                types.put(new JSONObject("{\"type\":\"\",\"mime\":\"application/unknown\"}"));
                JSONObject json = null;
                for (int i = 0; i < types.length(); i++) {
                    json = types.getJSONObject(i);
                    if (json.has("name") && file.getName().toLowerCase().equals(json.getString("name"))) {
                        type = json.getString("mime");
                        break;
                    } else if (json.has("type") && file.getName().toLowerCase().endsWith("." + json.getString("type"))) {
                        type = json.getString("mime");
                        break;
                    }
                }

                assert json != null;
                if(type.startsWith("text") || (json.has("text") && json.getBoolean("text"))) text = true;
                if (json.has("gzip") && !json.getBoolean("gzip")) gzip = false;
                if(!SENSITIVE.isGzip()) gzip = false;

                 SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
                dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
                String lastModified = dateFormat.format(file.lastModified());

                exchange.getResponseHeaders().set(HttpHeaders.LAST_MODIFIED, lastModified);
                exchange.getResponseHeaders().set(HttpHeaders.CACHE_CONTROL, SENSITIVE.isDebugMode() ? "max-age=10" : "max-age=120");
                exchange.getResponseHeaders().set(HttpHeaders.ETAG, etag);
                exchange.getResponseHeaders().set(HttpHeaders.SERVER, "Waytous/" + SERVER_BUILD);
                exchange.getResponseHeaders().set(HttpHeaders.ACCEPT_RANGES, "bytes");

                if (gzip) {
                    exchange.getResponseHeaders().set(HttpHeaders.CONTENT_ENCODING, "gzip");
                } else {
//                    exchange.getResponseHeaders().set("Content-Length", String.valueOf(file.length()));
                }

                if (text) {
                    if(!type.toLowerCase().matches(";\\s*charset\\s*=")) {
                        type += "; charset=UTF-8";
                    }

                    byte[] bytes = Files.readAllBytes(file.toPath());
                    Charset charset = StandardCharsets.ISO_8859_1;
                    if(bytes[0] == -1 && bytes[1] == -2) charset = StandardCharsets.UTF_16;
                    else if(bytes[0] == -2 && bytes[1] == -1) charset = StandardCharsets.UTF_16;


                    String string = new String(bytes, charset);
                    for(Map.Entry<String,String> x: substitutions.entrySet()) {
                        string = string.replaceAll(x.getKey(), x.getValue());
                    }

                    exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, type);
                    if(!gzip) exchange.getResponseHeaders().set(HttpHeaders.CONTENT_LENGTH, String.valueOf(string.length()));
                    exchange.sendResponseHeaders(resultCode, 0);
                    OutputStream os;
                    if(gzip) {
                        os = new BufferedOutputStream(new GZIPOutputStream(exchange.getResponseBody()));
                    } else {
                        os = exchange.getResponseBody();
                    }

                    os.write(string.getBytes(charset));
                    os.close();
                } else {
                    exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, type);
                    if(!gzip) exchange.getResponseHeaders().set(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));
                    exchange.sendResponseHeaders(resultCode, 0);
                    OutputStream os;
                    if (gzip) {
                        os = new BufferedOutputStream(new GZIPOutputStream(exchange.getResponseBody()));
                    } else {
                        os = exchange.getResponseBody();
                    }

                    FileInputStream fs = new FileInputStream(file);
                    final byte[] buffer = new byte[0x10000];
                    int count;
                    while ((count = fs.read(buffer)) >= 0) {
                        os.write(buffer, 0, count);
                    }
                    fs.close();
                    os.close();
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public AbstractDataProcessor getDataProcessor() {
        return dataProcessor;
    }

    public void setDataProcessor(AbstractDataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }


}
