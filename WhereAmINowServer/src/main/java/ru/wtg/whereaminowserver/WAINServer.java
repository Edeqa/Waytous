package ru.wtg.whereaminowserver;

import com.sun.net.httpserver.HttpServer;

import org.java_websocket.WebSocketImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import ru.wtg.whereaminowserver.helpers.MyHttpAdminServer;
import ru.wtg.whereaminowserver.helpers.MyWssServer;

import static ru.wtg.whereaminowserver.helpers.Constants.HTTP_PORT;
import static ru.wtg.whereaminowserver.helpers.Constants.WSS_PORT;


/**
 * Created 10/2/16.
 */

public class WAINServer {

    private static MyWssServer wssProcessor;
    private static MyHttpAdminServer httpServer;

    public static void main(final String[] args ) throws InterruptedException , IOException {

        wssProcessor = new MyWssServer(WSS_PORT);
        new Thread() {
            public void run() {
                try {
                    WebSocketImpl.DEBUG = false;
                    wssProcessor.start();
                    System.out.println("WSS server started on port: " + WSS_PORT);

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
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        httpServer = new MyHttpAdminServer();
        httpServer.setWssProcessor(wssProcessor);
        new Thread() {
            public void run() {
                try {
                    HttpServer server = HttpServer.create();
                    server.bind(new InetSocketAddress(HTTP_PORT), 0);
                    server.createContext("/", httpServer);
                    server.start();
                    System.out.println("HTTP server started on port: " + HTTP_PORT);


                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }.start();

    }

}