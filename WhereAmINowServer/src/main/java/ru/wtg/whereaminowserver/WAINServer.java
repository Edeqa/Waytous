package ru.wtg.whereaminowserver;

import com.sun.net.httpserver.HttpServer;

import org.java_websocket.WebSocketImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import ru.wtg.whereaminowserver.helpers.MyHttpServer;
import ru.wtg.whereaminowserver.helpers.MyWssServer;


/**
 * Created by tujger on 10/2/16.
 */

public class WAINServer {

    private static MyWssServer wssProcessor;
    private static MyHttpServer httpServer;

    private final static int PORT_HTTP = 8080;
    private final static int PORT_WSS = 8081;

    public static void main(final String[] args ) throws InterruptedException , IOException {

        wssProcessor = new MyWssServer(PORT_WSS);
        new Thread() {
            public void run() {
                try {
                    WebSocketImpl.DEBUG = false;
                    wssProcessor.start();
                    System.out.println("WSS server started on port: " + PORT_WSS);

                    BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
                    while (true) {
//                        if(!wssProcessor.parse(sysin)) break;
                        String in = sysin.readLine();
                        System.out.println("READ:" + in);
//                        s.sendToAll(in);
                        if (in.equals("exit")) {
                            wssProcessor.stop();
                            break;
                        } else if (in.equals("restart")) {
                            wssProcessor.stop();
                            wssProcessor.start();
                            break;
                        }

                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        httpServer = new MyHttpServer();
        httpServer.setWssProcessor(wssProcessor);
        new Thread() {
            public void run() {
                try {
                    HttpServer server = HttpServer.create();
                    server.bind(new InetSocketAddress(PORT_HTTP), 0);
                    server.createContext("/", httpServer);
                    server.start();
                    System.out.println("HTTP server started on port: " + PORT_HTTP);


                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }.start();

    };

}