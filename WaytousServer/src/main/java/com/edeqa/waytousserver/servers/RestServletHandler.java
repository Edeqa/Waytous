package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.HttpDPConnection;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.edeqa.waytousserver.helpers.Utils;
import com.google.common.net.HttpHeaders;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;

import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;


/**
 * Created 1/19/17.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class RestServletHandler extends AbstractServletHandler {

    private static final String LOG = "RSH";

    @Override
    public void init() throws ServletException {
        super.init();
        initDataProcessor();
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public void perform(RequestWrapper requestWrapper) throws IOException {


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
            case "/rest/v1/getContent":
                printRes = getContentV1(json, requestWrapper);
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
            br.close();

            Common.log("Rest",requestWrapper.getRemoteAddress(), "joinV1:", body);
            Common.getInstance().getDataProcessor(requestWrapper.getRequestURI().getPath().split("/")[3]).onMessage(new HttpDPConnection(requestWrapper), body);
        } catch (Exception e) {
            e.printStackTrace();
            json.put("status", "Action failed");
            json.put("message", e.getMessage());
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

    private boolean getContentV1(final JSONObject json, final RequestWrapper requestWrapper) {
        File dir = new File(SENSITIVE.getWebRootDirectory());

        /*requestWrapper.processBody(new Runnable1<StringBuilder>() {
            @Override
            public void call(StringBuilder buf) {
                try {
                    JSONObject options = new JSONObject(buf.toString());
                    Common.log(LOG, "Content requested: " + options);

                    ArrayList<File> files = new ArrayList<>();

                    if (options.has("type")) {
                        if (options.has("locale") && options.has("resource")) {
                            files.add(new File(SENSITIVE.getWebRootDirectory() + "/" + options.getString("type") + "/" + options.getString("locale") + "/" + options.getString("resource")));
                        }
                        if (options.has("resource")) {
                            files.add(new File(SENSITIVE.getWebRootDirectory() + "/" + options.getString("type") + "/" + options.getString("resource")));
                        }
                    } else {
                        if (options.has("locale") && options.has("resource")) {
                            files.add(new File(SENSITIVE.getWebRootDirectory() + "/content/" + options.getString("locale") + "/" + options.getString("resource")));
                        }
                        if (options.has("resource")) {
                            files.add(new File(SENSITIVE.getWebRootDirectory() + "/content/" + options.getString("resource")));
                        }
                    }

                    boolean exists = false;
                    File file = null;
                    for (File f : files) {
                        if (f.getCanonicalPath().equals(f.getAbsolutePath()) && f.exists()) {
                            file = f;
                            exists = true;
                            break;
                        }
                    }

                    if (exists) {
                        Common.log(LOG, "File found: " + file.toString());
                        boolean gzip = true;
                        requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, Constants.MIME.TEXT_PLAIN);
                        requestWrapper.setHeader(HttpHeaders.SERVER, "Waytous/" + Constants.SERVER_BUILD);
                        requestWrapper.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

                        if (gzip) {
                            requestWrapper.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                        } else {
                            requestWrapper.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));
                        }

                        requestWrapper.sendResponseHeaders(200, 0);

                        OutputStream os;
                        if (gzip) {
                            os = new BufferedOutputStream(new GZIPOutputStream(requestWrapper.getResponseBody()));
                        } else {
                            os = requestWrapper.getResponseBody();
                        }

                        FileInputStream fs = new FileInputStream(file);
                        final byte[] buffer = new byte[0x10000];

                        int count = 0;
                        while ((count = fs.read(buffer)) >= 0) {
                            os.write(buffer, 0, count);
                        }
                        fs.close();
                        os.close();
                    } else {
                        Common.log(LOG, "Content not found.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Runnable1<Exception>() {
            @Override
            public void call(Exception arg) {

            }
        });
        if(true) return false;
*/
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = requestWrapper.getRequestBody();
            int b;
            while ((b = is.read()) != -1) {
                buf.append((char) b);
            }
            is.close();

            JSONObject options = new JSONObject(buf.toString());
            Common.log(LOG,"Content requested: " + options);

            ArrayList<File> files = new ArrayList<>();

            if(options.has("type")) {
                if (options.has("locale") && options.has("resource")) {
                    files.add(new File(SENSITIVE.getWebRootDirectory() + "/" + options.getString("type") + "/" + options.getString("locale") + "/" + options.getString("resource")));
                }
                if (options.has("resource")) {
                    files.add(new File(SENSITIVE.getWebRootDirectory() + "/" + options.getString("type") + "/" + options.getString("resource")));
                }
            } else {
                if (options.has("locale") && options.has("resource")) {
                    files.add(new File(SENSITIVE.getWebRootDirectory() + "/content/" + options.getString("locale") + "/" + options.getString("resource")));
                }
                if (options.has("resource")) {
                    files.add(new File(SENSITIVE.getWebRootDirectory() + "/content/" + options.getString("resource")));
                }
            }

            boolean exists = false;
            File file = null;
            for (File f : files) {
                if (f.getCanonicalPath().equals(f.getAbsolutePath()) && f.exists()) {
                    file = f;
                    exists = true;
                    break;
                }
            }

            if(exists) {
                Common.log(LOG,"File found: " + file.toString());
                boolean gzip = true;
                requestWrapper.setHeader(HttpHeaders.CONTENT_TYPE, Constants.MIME.TEXT_PLAIN);
                requestWrapper.setHeader(HttpHeaders.SERVER, "Waytous/"+ Constants.SERVER_BUILD);
                requestWrapper.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");

                if(gzip){
                    requestWrapper.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                } else {
                    requestWrapper.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.length()));
                }

                requestWrapper.sendResponseHeaders(200, 0);

                OutputStream os;
                if(gzip) {
                    os = new BufferedOutputStream(new GZIPOutputStream(requestWrapper.getResponseBody()));
                } else {
                    os = requestWrapper.getResponseBody();
                }

                FileInputStream fs = new FileInputStream(file);
                final byte[] buffer = new byte[0x10000];

                int count = 0;
                while ((count = fs.read(buffer)) >= 0) {
                    os.write(buffer, 0, count);
                }
                fs.close();
                os.close();
                return false;
            } else {
                Common.log(LOG,"Content not found.");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

    }

}
