package com.edeqa.waytousserver;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.SensitiveData;
import com.edeqa.waytousserver.servers.DataProcessorFirebaseV1;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;

/**
 * Created 6/12/2017.
 */

public class WaytousServlet implements ServletContextListener {


    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        //noinspection HardCodedStringLiteral
        System.out.println("ServletContextListener destroyed");
    }

    //Run this before web application is started
    @Override
    public void contextInitialized(ServletContextEvent event) {
        //noinspection HardCodedStringLiteral
        System.out.println("ServletContextListener started");

//        String sensitiveData = event.getServletContext().getInitParameter("sensitiveData");
//        SENSITIVE = new SensitiveData(new String[]{sensitiveData});

//        Common.getInstance().setDataProcessor(new DataProcessorFirebaseV1());
    }
}
