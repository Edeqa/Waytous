package ru.wtg.whereaminowserver.servers;

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

import ru.wtg.whereaminowserver.helpers.Common;

import static ru.wtg.whereaminowserver.helpers.Constants.SENSITIVE;

/**
 * Created 1/19/17.
 */
public class MyHttpXhrHandler implements HttpHandler {

    private volatile AbstractWainProcessor wainProcessor;

    public MyHttpXhrHandler(){
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        URI uri = exchange.getRequestURI();

        InputStreamReader isr =  new InputStreamReader(exchange.getRequestBody(),"utf-8");
        BufferedReader br = new BufferedReader(isr);
        String body = br.readLine();

        String host = null;
        try {
            host = exchange.getRequestHeaders().get("Host").get(0);
            host = host.split(":")[0];
        } catch(Exception e){
            e.printStackTrace();
        }


        if(body != null) {
            Common.log("Xhr","Request",host + uri.getPath(),exchange.getRemoteAddress().toString(), body);
            getWainProcessor().onMessage(new HttpConnection(exchange), body);
        } else {
            Common.log("Xhr","Internal",host + uri.getPath(),exchange.getRemoteAddress().toString());
            List<String> parts = Arrays.asList(uri.getPath().split("/"));
            JSONObject json = new JSONObject();
            switch(parts.get(2)) {
                case "getSounds":
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
                    break;
            }

            byte[] bytes = json.toString().getBytes();
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            try {
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public AbstractWainProcessor getWainProcessor() {
        return wainProcessor;
    }

    public void setWainProcessor(AbstractWainProcessor wainProcessor) {
        this.wainProcessor = wainProcessor;
    }

    class HttpConnection implements AbstractWainProcessor.Connection {

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

            byte[] bytes = string.getBytes();

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            try {
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void close() {
//            conn.close();
        }
    }


}
