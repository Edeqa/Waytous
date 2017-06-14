package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.HttpDPConnection;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.edeqa.waytousserver.helpers.Utils;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

import javax.servlet.ServletException;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;


/**
 * Created 1/19/17.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class RestServletHandler extends AbstractServletHandler {

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public void perform(RequestWrapper requestWrapper)  {

        System.out.println("REST");
        URI uri = requestWrapper.getRequestURI();
        String host = null;
        try {
            host = requestWrapper.getRequestHeader(HttpHeaders.HOST).get(0);
            host = host.split(":")[0];
        } catch(Exception e){
            e.printStackTrace();
        }
        Common.log("Rest",host + uri.getPath(),requestWrapper.getRemoteAddress());

//        List<String> parts = Arrays.asList(uri.getPath().split("/"));
        JSONObject json = new JSONObject();
        boolean printRes;

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
                printRes = getResourcesV1(json, requestWrapper);
                break;
            case "/rest/v1/join":
                printRes = joinV1(json, requestWrapper);
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

        if(printRes) Utils.sendResultJson.call(requestWrapper, json);

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

    private boolean joinV1(JSONObject json, RequestWrapper requestWrapper) {
        try {
            InputStreamReader isr = new InputStreamReader(requestWrapper.getRequestBody(),"utf-8");
            BufferedReader br = new BufferedReader(isr);
            String body = br.readLine();

            Common.log("Rest",requestWrapper.getRemoteAddress(), "joinV1:", body);
            Common.getInstance().getDataProcessor(requestWrapper.getRequestURI().getPath().split("/")[3]).onMessage(new HttpDPConnection(requestWrapper), body);
        } catch (Exception e) {
            e.printStackTrace();
            json.put("status", "Action failed");
            Utils.sendResultJson.call(requestWrapper,json);
        }
        return false;
    }


    private boolean getResourcesV1(final JSONObject json, final RequestWrapper requestWrapper) {
        File dir = new File(SENSITIVE.getWebRootDirectory() + "/locales");

        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = requestWrapper.getRequestBody();
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
