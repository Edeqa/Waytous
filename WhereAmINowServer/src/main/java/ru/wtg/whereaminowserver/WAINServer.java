package ru.wtg.whereaminowserver;

import com.sun.net.httpserver.HttpServer;

import org.java_websocket.WebSocketImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import ru.wtg.whereaminowserver.helpers.MyHttpAdminServer;
import ru.wtg.whereaminowserver.helpers.MyHttpMainServer;
import ru.wtg.whereaminowserver.helpers.MyHttpTrackingServer;
import ru.wtg.whereaminowserver.helpers.MyWssServer;

import static ru.wtg.whereaminowserver.helpers.Constants.HTTP_PORT;
import static ru.wtg.whereaminowserver.helpers.Constants.WEB_ROOT_DIRECTORY;
import static ru.wtg.whereaminowserver.helpers.Constants.WSS_PORT;

/**
 * Created 10/2/16.
 */

public class WAINServer {

    private static MyWssServer wssProcessor;
    private static HttpServer server;

    public static void main(final String[] args ) throws InterruptedException , IOException {

        System.out.println("Server web root directory: "+new File(WEB_ROOT_DIRECTORY).getAbsolutePath());

        System.out.println("Server \t\t\t| Port \t| Path");
        System.out.println("----------------------------------------------");

        wssProcessor = new MyWssServer(WSS_PORT);
        new Thread() {
            public void run() {
                try {
                    WebSocketImpl.DEBUG = false;
                    wssProcessor.start();
                    System.out.println("WSS\t\t\t\t| " + WSS_PORT + "\t|");

                    /*BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
                    while (true) {
//                        if(!wssProcessor.parse(sysin)) break;
                        String in = sysin.readLine();
                        System.out.println("READ:" + in);
//                        s.sendToAll(in);
                        if (in.equals("exit")) {
                            wssProcessor.stop();
                            break;
                        }
                    }*/
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }.start();


        server = HttpServer.create();
        server.bind(new InetSocketAddress(HTTP_PORT), 0);

        MyHttpMainServer mainServer = new MyHttpMainServer();
        mainServer.setWssProcessor(wssProcessor);
        server.createContext("/", mainServer);
        System.out.println("Main HTTP\t\t| " + HTTP_PORT + "\t| /, /*");

        MyHttpTrackingServer trackingServer = new MyHttpTrackingServer();
        trackingServer.setWssProcessor(wssProcessor);
        server.createContext("/track", trackingServer);
        System.out.println("Tracking HTTP\t| " + HTTP_PORT + "\t| " + "/track");

        MyHttpAdminServer adminServer = new MyHttpAdminServer();
        adminServer.setWssProcessor(wssProcessor);
        server.createContext("/admin", adminServer);
        System.out.println("Admin HTTP\t\t| " + HTTP_PORT + "\t| " + "/admin");

        new Thread() {
            public void run() {
                try {
                    server.start();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

}