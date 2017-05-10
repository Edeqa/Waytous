package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.Constants;
import com.google.api.client.http.HttpMethods;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;


/**
 * Created 1/19/17.
 */
public class MyHttpXhrHandler implements HttpHandler {

    private volatile AbstractDataProcessor dataProcessor;

    public MyHttpXhrHandler(){
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        URI uri = exchange.getRequestURI();
        String host = null;
        try {
            host = exchange.getRequestHeaders().get(HttpHeaders.HOST).get(0);
            host = host.split(":")[0];
        } catch(Exception e){
            e.printStackTrace();
        }
        Common.log("Xhr",host + uri.getPath(),exchange.getRemoteAddress().toString());

        List<String> parts = Arrays.asList(uri.getPath().split("/"));
        JSONObject json = new JSONObject();
        boolean printRes = false;

//        switch(exchange.getRequestMethod()) {
//            case HttpMethods.GET:
                switch(parts.get(2)) {
                    case "getApiVersion":
                        printRes = getApiVersion(json);
                        break;
                    case "getSounds":
                        printRes = getSounds(json);
                        break;
                    case "getResources":
                        printRes = getResources(json, parts.size() > 2 ? parts.get(3) : null);
                        break;
                    case "join":
                        printRes = join(json, exchange);
                        break;
                    default:
                        printRes = noAction(json);
                        break;
                }
//                break;
//            case HttpMethods.PUT:
//                break;
//            case HttpMethods.POST:
//                break;
//        }

        if(printRes) printResult(json.toString().getBytes(),exchange);

    }

    private void printResult(byte[] bytes, HttpExchange exchange) {
        exchange.getResponseHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");
        try {
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public AbstractDataProcessor getDataProcessor() {
        return dataProcessor;
    }

    public void setDataProcessor(AbstractDataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    class HttpConnection implements AbstractDataProcessor.Connection {

        private final HttpExchange exchange;

        public HttpConnection(HttpExchange exchange) {
            this.exchange = exchange;
        }

        @Override
        public boolean isOpen() {
            return true;//conn.isOpen();
        }

        @Override
        public InetSocketAddress getRemoteSocketAddress() {
            return exchange.getRemoteAddress();
        }

        @Override
        public void send(String string) {
            printResult(string.getBytes(), exchange);
        }

        @Override
        public void close() {
//            conn.close();
        }
    }

    private boolean getApiVersion(JSONObject json) {
        json.put("apiVersion", Constants.SERVER_BUILD);
        return true;
    }

    private boolean noAction(JSONObject json) {
        json.put("status", "Action not defined");
        return true;
    }

    private boolean getSounds(JSONObject json) {
        File dir = new File(SENSITIVE.getWebRootDirectory() + "/sounds");
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp3");
            }
        });
        ArrayList<String> list = new ArrayList<>();
        list.add("none.mp3");
        if(files != null) {
            for(File file: files) {
                if(!list.contains(file.getName())) list.add(file.getName());
            }
        }
        json.put("files", list);
        return true;
    }

    private boolean join(JSONObject json, HttpExchange exchange) {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(),"utf-8");
            BufferedReader br = new BufferedReader(isr);
            String body = br.readLine();

            Common.log("Xhr-join",exchange.getRemoteAddress().toString(), body);
            getDataProcessor().onMessage(new HttpConnection(exchange), body);
        } catch (Exception e) {
            e.printStackTrace();
            json.put("status", "Action failed");
            printResult(json.toString().getBytes(), exchange);
        }
        return false;
    }


    private boolean getResources(final JSONObject json, final String resource) {
        File dir = new File(SENSITIVE.getWebRootDirectory() + "/locales");

        try {
            if(!dir.getCanonicalPath().equals(dir.getAbsolutePath())) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }

        File[] files;
        if(resource != null) {
            files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith(resource);
                }
            });
        } else {
            files = new File[]{};
        }
        ArrayList<String> list = new ArrayList<>();
        if(files != null) {
            for(File file: files) {
                if(!list.contains(file.getName())) list.add(file.getName());
            }
        }
        json.put("files", list);
        return true;
    }

}
