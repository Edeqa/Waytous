package ru.wtg.whereaminowserver;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import org.java_websocket.WebSocketImpl;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import ru.wtg.whereaminowserver.helpers.Common;
import ru.wtg.whereaminowserver.helpers.Constants;
import ru.wtg.whereaminowserver.helpers.SensitiveData;
import ru.wtg.whereaminowserver.servers.MyHttpAdminHandler;
import ru.wtg.whereaminowserver.servers.MyHttpJoinHandler;
import ru.wtg.whereaminowserver.servers.MyHttpMainHandler;
import ru.wtg.whereaminowserver.servers.MyHttpRedirectHandler;
import ru.wtg.whereaminowserver.servers.MyHttpTrackingHandler;
import ru.wtg.whereaminowserver.servers.MyWsServer;
import ru.wtg.whereaminowserver.servers.WainProcessorFirebase;

import static ru.wtg.whereaminowserver.helpers.Constants.SENSITIVE;

/**
 * Created 10/2/16.
 */

public class WAINServer {

    private static MyWsServer wsServer;
    private static MyWsServer wssServer;

    public static void main(final String[] args ) throws InterruptedException , IOException {

        Constants.SENSITIVE = new SensitiveData(args);


        try {
            FirebaseApp.initializeApp(new FirebaseOptions.Builder()
                    .setServiceAccount(new FileInputStream(SENSITIVE.getFirebasePrivateKeyFile()))
                    .setDatabaseUrl(SENSITIVE.getFirebaseDatabaseUrl())
                    .build());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        WainProcessorFirebase wainProcessorFirebase = new WainProcessorFirebase();
        wsServer = new MyWsServer(SENSITIVE.getWsPortFirebase(), wainProcessorFirebase);
        wssServer = new MyWsServer(SENSITIVE.getWssPortFirebase(), wainProcessorFirebase);

        Common.log("Main","Server web root directory: "+new File(SENSITIVE.getWebRootDirectory()).getCanonicalPath());

        System.out.println("Server \t\t\t\t| Port \t| Path");
        System.out.println("----------------------------------------------");

        try {
            String STORETYPE = "JKS";
            String STOREPASSWORD = SENSITIVE.getSSLCertificatePassword();
            String KEYPASSWORD = SENSITIVE.getSSLCertificatePassword();

            KeyStore ks = KeyStore.getInstance(STORETYPE);
            File kf = new File(SENSITIVE.getKeystoreFilename());
            ks.load(new FileInputStream(kf), STOREPASSWORD.toCharArray());

        //    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        //    kmf.init(ks, KEYPASSWORD.toCharArray());
        //    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        //    tmf.init(ks);



            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, KEYPASSWORD.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance("TLS");
        //    sslContext.init(null, null, null);
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            ////////////////*///////////
         /*   CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
            InputStream caInput = new BufferedInputStream(new FileInputStream("verisign.cert"));
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }

                String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);// my question shows how to get 'ca'
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // Initialise the TMF as you normally would, for example:
            tmf.init(keyStore);

            TrustManager[] trustManagers = tmf.getTrustManagers();
            final X509TrustManager origTrustmanager = (X509TrustManager)trustManagers[0];

            TrustManager[] wrappedTrustManagers = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return origTrustmanager.getAcceptedIssuers();
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            try {
                                origTrustmanager.checkClientTrusted(certs, authType);
                            } catch (CertificateException e) {
                                e.printStackTrace();
                            }
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            try {
                                origTrustmanager.checkServerTrusted(certs, authType);
                            } catch (CertificateExpiredException e) {
                                e.printStackTrace();
                            } catch (CertificateException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, wrappedTrustManagers, null);
        */

            DefaultSSLWebSocketServerFactory socket = new DefaultSSLWebSocketServerFactory(sslContext);
        //    socket.setEnabledCipherSuites(sc.getServerSocketFactory().getSupportedCipherSuites());
            wssServer.setWebSocketFactory(socket);


            new Thread() {
                public void run() {
                    try {
                        WebSocketImpl.DEBUG = false;
                        System.out.println("WS FB\t\t\t\t| " + SENSITIVE.getWsPortFirebase() + "\t|");
                        wsServer.start();
                        System.out.println("WSS FB\t\t\t\t| " + SENSITIVE.getWssPortFirebase() + "\t|");
                        wssServer.start();

                            /*BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
                            while (true) {
        //                        if(!wssServer.parse(sysin)) break;
                                String in = sysin.readLine();
                                System.out.println("READ:" + in);
        //                        s.sendToAll(in);
                                if (in.equals("exit")) {
                                    wssServer.stop();
                                    break;
                                }
                            }*/
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }catch(Exception e){
            e.printStackTrace();
        }

        /*wssServer = new MyWsServer(WSS_PORT);
        new Thread() {
            public void run() {
                try {
                    WebSocketImpl.DEBUG = false;
                    wssServer.start();
                    System.out.println("WSS\t\t\t\t| " + WSS_PORT + "\t|");

                    *//*BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
                    while (true) {
//                        if(!wssServer.parse(sysin)) break;
                        String in = sysin.readLine();
                        System.out.println("READ:" + in);
//                        s.sendToAll(in);
                        if (in.equals("exit")) {
                            wssServer.stop();
                            break;
                        }
                    }*//*
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }.start();*/

        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(SENSITIVE.getHttpPort()), 0);

        MyHttpRedirectHandler redirectServer = new MyHttpRedirectHandler();
        System.out.println("Redirect HTTP\t\t| " + SENSITIVE.getHttpPort() + "\t| " + "/");
        server.createContext("/", redirectServer);

//        server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
//        server.start();


//        server = HttpServer.create();
//        server.bind(new InetSocketAddress(HTTP_PORT), 0);

        MyHttpMainHandler mainServer = new MyHttpMainHandler();
        mainServer.setWainProcessor(wainProcessorFirebase);
//        server.createContext("/", mainServer);
//        System.out.println("Main HTTP\t\t| " + HTTP_PORT + "\t| /, /*");

        MyHttpJoinHandler joinServer = new MyHttpJoinHandler();
        joinServer.setWainProcessor(wainProcessorFirebase);
//        server.createContext("/", mainServer);
//        System.out.println("Main HTTP\t\t| " + HTTP_PORT + "\t| /, /*");

        MyHttpTrackingHandler trackingServer = new MyHttpTrackingHandler();
        trackingServer.setWainProcessor(wainProcessorFirebase);
//        server.createContext("/track", trackingServer);
//        System.out.println("Tracking HTTP\t| " + HTTP_PORT + "\t| " + "/track");

//        server.createContext("/group", trackingServer);
//        System.out.println("Tracking HTTP\t| " + HTTP_PORT + "\t| " + "/group");

        MyHttpAdminHandler adminServer = new MyHttpAdminHandler();
        adminServer.setWainProcessor(wainProcessorFirebase);
//        server.createContext("/admin", adminServer).setAuthenticator(new Authenticator("get"));
//        System.out.println("Admin HTTP\t\t| " + HTTP_PORT + "\t| " + "/admin");

        server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
        server.start();


        try {
            HttpsServer sslServer = HttpsServer.create(new InetSocketAddress(SENSITIVE.getHttpsPort()), 0);

            SSLContext sslContext = SSLContext.getInstance("TLS");

            // initialise the keystore
            char[] password = SENSITIVE.getSSLCertificatePassword().toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            FileInputStream fis = new FileInputStream(SENSITIVE.getKeystoreFilename());
            ks.load(fis, password);

            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // setup the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            sslServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext c = SSLContext.getDefault();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // get the default parameters
                        SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                        params.setSSLParameters(defaultSSLParameters);

                    } catch (Exception ex) {
                        Common.log("Main","Failed to create HTTPS port");
                    }
                }
            });

            sslServer.createContext("/", mainServer);
            System.out.println("Main HTTPS\t\t\t| " + SENSITIVE.getHttpsPort() + "\t| /, /*");

            sslServer.createContext("/track/", trackingServer);
            System.out.println("Tracking HTTPS\t\t| " + SENSITIVE.getHttpsPort() + "\t| " + "/track");

            sslServer.createContext("/group/", trackingServer);
            System.out.println("Tracking HTTPS\t\t| " + SENSITIVE.getHttpsPort() + "\t| " + "/group");

            sslServer.createContext("/join/", joinServer);
            System.out.println("Join HTTPS\t\t\t| " + SENSITIVE.getHttpsPort() + "\t| " + "/join");

            sslServer.createContext("/admin/", adminServer).setAuthenticator(new Authenticator("get"));
            System.out.println("Admin HTTPS\t\t\t| " + SENSITIVE.getHttpsPort() + "\t| " + "/admin");

            sslServer.setExecutor(Executors.newCachedThreadPool()); // creates a default executor


//            sslServer.setHttpsConfigurator(new HttpsConfigurator(SSLContext.getDefault()));
            sslServer.start();
        } catch(Exception e){
            e.printStackTrace();
        }

/*        new Thread() {
            public void run() {
                try {
                    server.start();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }.start();*/


    }

    static class Authenticator extends BasicAuthenticator {
        public Authenticator(String s) {
            super(s);
        }

        @Override
        public boolean checkCredentials(String user, String pwd) {
            return user.equals(SENSITIVE.getLogin()) && pwd.equals(SENSITIVE.getPassword());
        }
    }

}