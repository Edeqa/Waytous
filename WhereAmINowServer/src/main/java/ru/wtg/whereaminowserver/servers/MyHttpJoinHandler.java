package ru.wtg.whereaminowserver.servers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

import ru.wtg.whereaminowserver.helpers.Common;

/**
 * Created 1/19/17.
 */
public class MyHttpJoinHandler implements HttpHandler {

    private volatile AbstractWainProcessor wainProcessor;

    public MyHttpJoinHandler(){
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

        Common.log("Join",host + uri.getPath(),exchange.getRemoteAddress().toString(), body);

        getWainProcessor().onMessage(new HttpConnection(exchange), body);

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
