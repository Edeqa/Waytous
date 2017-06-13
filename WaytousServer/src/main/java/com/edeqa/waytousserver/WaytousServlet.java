package com.edeqa.waytousserver;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.SensitiveData;
import com.edeqa.waytousserver.servers.DataProcessorFirebaseV1;
import com.edeqa.waytousserver.servers.MyWsServer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseCredentials;

import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;

/**
 * Created 6/12/2017.
 */

public class WaytousServlet implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        System.out.println("ServletContextListener destroyed");
    }

    //Run this before web application is started
    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.out.println("ServletContextListener started");

        String sensitiveData = event.getServletContext().getInitParameter("sensitiveData");
        SENSITIVE = new SensitiveData(new String[]{sensitiveData});

        try {
            FirebaseApp.initializeApp(new FirebaseOptions.Builder()
                    .setCredential(FirebaseCredentials.fromCertificate(new FileInputStream(SENSITIVE.getFirebasePrivateKeyFile())))
                    .setDatabaseUrl(SENSITIVE.getFirebaseDatabaseUrl())
                    .build());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Common.getInstance().setDataProcessor(new DataProcessorFirebaseV1());
    }
}
