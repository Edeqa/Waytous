package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.HttpDPConnection;
import com.edeqa.waytousserver.helpers.Utils;
import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;


/**
 * Created 1/19/17.
 */
public class MyHttpRestHandler implements HttpHandler {

    private volatile Map<String,AbstractDataProcessor> dataProcessor;

    public MyHttpRestHandler(){
        dataProcessor = new HashMap<>();
    }

    @SuppressWarnings("HardCodedStringLiteral")
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
        Common.log("Rest",host + uri.getPath(),exchange.getRemoteAddress().toString());

        List<String> parts = Arrays.asList(uri.getPath().split("/"));
        JSONObject json = new JSONObject();
        boolean printRes = false;

//        switch(exchange.getRequestMethod()) {
//            case HttpMethods.GET:
        switch(uri.getPath()) {
            case "/rest/v1/getVersion":
                printRes = getVersionV1(json);
                break;
            case "/rest/v1/getSounds":
                printRes = getSoundsV1(json);
                break;
            case "/rest/v1/getResources":
                printRes = getResourcesV1(json, exchange);
                break;
            case "/rest/v1/join":
                printRes = joinV1(json, exchange);
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

        if(printRes) Utils.sendResultJson.call(exchange, json);

    }

    public AbstractDataProcessor getDataProcessor(String version) {
        if(dataProcessor.containsKey(version)) {
            return dataProcessor.get(version);
        } else {
            return dataProcessor.get("v1");
        }
    }

    public void setDataProcessor(AbstractDataProcessor dataProcessor) {
        this.dataProcessor.put(DataProcessorFirebaseV1.VERSION, dataProcessor);
    }

    private boolean getVersionV1(JSONObject json) {
        json.put("version", Constants.SERVER_BUILD);
        return true;
    }

    private boolean noAction(JSONObject json) {
        json.put("status", "Action not defined");
        return true;
    }

    private boolean getSoundsV1(JSONObject json) {
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

    private boolean joinV1(JSONObject json, HttpExchange exchange) {
        try {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(),"utf-8");
            BufferedReader br = new BufferedReader(isr);
            String body = br.readLine();

            Common.log("Rest",exchange.getRemoteAddress().toString(), "joinV1:", body);
            getDataProcessor(exchange.getRequestURI().getPath().split("/")[3]).onMessage(new HttpDPConnection(exchange), body);
        } catch (Exception e) {
            e.printStackTrace();
            json.put("status", "Action failed");
            Utils.sendResultJson.call(exchange,json);
        }
        return false;
    }


    private boolean getResourcesV1(final JSONObject json, final HttpExchange exchange) {
        File dir = new File(SENSITIVE.getWebRootDirectory() + "/locales");

        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = exchange.getRequestBody();
            int b;
            while ((b = is.read()) != -1) {
                buf.append((char) b);
            }

            is.close();
            JSONObject options = new JSONObject(buf.toString());

            if(!dir.getCanonicalPath().equals(dir.getAbsolutePath())) {
                return true;
            }

            File[] files;
            if(options.has("type")) {
                final String prefix = options.getString("type");
                files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.startsWith(prefix);
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
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

    }

}
