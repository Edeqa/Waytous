package com.edeqa.waytousserver.helpers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created 1/13/17.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class SensitiveData {

    private JSONObject json;

    public SensitiveData(String[] args) {

        File file = new File("options.json");

        /*if(args.length != 0 && ("true".equals(args[0]) || "false".equals(args[0]))) { // call from android version
            switch(args[0]) {
                case "true": // android debug mode
                    break;
                case "false": // android release mode
                    break;
            }

        } else*/ if(args.length != 0 && args[0] != null && args[0].length() != 0) {
            if("-g".equals(args[0].toLowerCase())) {
                generateSampleOptions();
                return;
            }
            file = new File(args[0]);
        }

        readWithFile(file);
    }

    public SensitiveData(File file) {

        readWithFile(file);
    }

    public SensitiveData(Reader reader) {

        try {
            readWithFileReader(reader);
        } catch (IOException e) {
            System.err.println("Reader tried to read options file from corrupted or damaged source.\n");
            e.printStackTrace();
        }
    }

    private void readWithFile(File file) {

        if(!file.exists()) {
            System.err.println("Options file "+file.getAbsolutePath()+" not found.\nRun with key -g for generate the sample.\n");
            return;
        }

        try {
            Common.log("SD","Read config from options file:",file.getAbsoluteFile());
            FileReader reader = new FileReader(file);
            readWithFileReader(reader);
        } catch (Exception e) {
            System.err.println("Options file "+file.getAbsolutePath()+" is corrupted or damaged.\nRun with key -g for generate the sample.\n");
            e.printStackTrace();
        }
    }

    private void readWithFileReader(Reader reader) throws IOException {

        int c;
        StringBuilder string = new StringBuilder();
        while((c=reader.read())!=-1){
            string.append((char)c);
        }

        Matcher m = Pattern.compile("\\/\\* [\\s\\S]*?\\*\\/").matcher(string);
        string.replace(0, string.length(), m.replaceAll(""));
        json = new JSONObject(string.toString());

    }

    private void generateSampleOptions() {
        File file = new File("options.json");
        FileWriter writer = null;

        try {
            writer = new FileWriter(file);

            JSONObject jsonSample = new JSONObject();

            Field map = jsonSample.getClass().getDeclaredField("map");
            map.setAccessible(true);//because the field is private final...
            map.set(jsonSample, new LinkedHashMap<>());
            map.setAccessible(false);//return flag

            jsonSample.put("firebase_authorization_key","SAMPL");
            jsonSample.put("firebase_dynamic_link_host","SAMPL");
            jsonSample.put("firebase_private_key_file","SAMPL");

            JSONObject jsonFirebaseConfig = new JSONObject();
            jsonFirebaseConfig.put("apiKey","SAMPL");
            jsonFirebaseConfig.put("authDomain","SAMPL");
            jsonFirebaseConfig.put("databaseURL","SAMPL");
            jsonFirebaseConfig.put("storageBucket","SAMPL");
            jsonFirebaseConfig.put("messagingSenderId","SAMPL");
            jsonSample.put("firebase_config",jsonFirebaseConfig);

            jsonSample.put("admin_login","admin");
            jsonSample.put("admin_password","password");
            jsonSample.put("server_host","localhost");
            jsonSample.put("http_port",8080);
            jsonSample.put("http_secured_port",8000);
            jsonSample.put("websocket_port_dedicated",8082);
            jsonSample.put("websocket_secured_port_dedicated",8002);
            jsonSample.put("websocket_port_firebase",8081);
            jsonSample.put("websocket_secured_port_firebase",8001);
            jsonSample.put("ssl_certificate_password","SAMPL");
            jsonSample.put("keystore_filename","SAMPL");
            jsonSample.put("debug_mode",false);
            jsonSample.put("log_file","waytous.log");

            JSONArray jsonMimeTypes = new JSONArray();
            JSONObject jsonMimeType = new JSONObject();
            jsonMimeType.put("type","js");
            jsonMimeType.put("mime","text/javascript");
            jsonMimeTypes.put(jsonMimeType);

            jsonMimeType = new JSONObject();
            jsonMimeType.put("type","css");
            jsonMimeType.put("mime","text/css");
            jsonMimeTypes.put(jsonMimeType);

            jsonMimeType = new JSONObject();
            jsonMimeType.put("type","xml");
            jsonMimeType.put("mime","application/xml");
            jsonMimeType.put("text",true);
            jsonMimeTypes.put(jsonMimeType);

            jsonMimeType = new JSONObject();
            jsonMimeType.put("name","manifest.json");
            jsonMimeType.put("mime","application/x-web-app-manifest+json");
            jsonMimeType.put("text",true);
            jsonMimeTypes.put(jsonMimeType);

            jsonMimeType = new JSONObject();
            jsonMimeType.put("type","json");
            jsonMimeType.put("mime","application/json");
            jsonMimeType.put("text",true);
            jsonMimeTypes.put(jsonMimeType);

            jsonMimeType = new JSONObject();
            jsonMimeType.put("type","gif");
            jsonMimeType.put("mime","image/gif");
            jsonMimeTypes.put(jsonMimeType);

            jsonMimeType = new JSONObject();
            jsonMimeType.put("type","jpg");
            jsonMimeType.put("mime","image/jpg");
            jsonMimeType.put("gzip",false);
            jsonMimeTypes.put(jsonMimeType);

            jsonMimeType = new JSONObject();
            jsonMimeType.put("type","png");
            jsonMimeType.put("mime","image/png");
            jsonMimeTypes.put(jsonMimeType);

            jsonMimeType = new JSONObject();
            jsonMimeType.put("type","ico");
            jsonMimeType.put("mime","image/ico");
            jsonMimeTypes.put(jsonMimeType);

            jsonMimeType = new JSONObject();
            jsonMimeType.put("type","svg");
            jsonMimeType.put("mime","image/svg+xml");
            jsonMimeType.put("text",true);
            jsonMimeTypes.put(jsonMimeType);

            jsonMimeType = new JSONObject();
            jsonMimeType.put("type","mp3");
            jsonMimeType.put("mime","audio/mp3");
            jsonMimeType.put("gzip",false);
            jsonMimeTypes.put(jsonMimeType);

            jsonMimeType = new JSONObject();
            jsonMimeType.put("type","ogg");
            jsonMimeType.put("mime","audio/ogg");
            jsonMimeType.put("gzip",false);
            jsonMimeTypes.put(jsonMimeType);

            jsonMimeType = new JSONObject();
            jsonMimeType.put("type","m4r");
            jsonMimeType.put("mime","audio/aac");
            jsonMimeType.put("gzip",false);
            jsonMimeTypes.put(jsonMimeType);

            jsonSample.put("types",jsonMimeTypes);

            writer.write(jsonSample.toString(4));
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Sample options file was generated into "+file.getAbsolutePath()+".\nModify it using your custom options and run again.\n");

    }

    public String getFirebaseServerKey() {
        return json.getString("firebase_authorization_key");
    }

    public String getFirebaseDynamicLinkHost() {
        return json.getString("firebase_dynamic_link_host");
    }

    public String getFirebasePrivateKeyFile() {
        return json.getString("firebase_private_key_file");
    }

    public String getFirebaseDatabaseUrl() {
        return json.getJSONObject("firebase_config").getString("databaseURL");
    }

    public String getSSLCertificatePassword() {
        return json.getString("ssl_certificate_password");
    }

    public JSONObject getFirebaseConfig() {
        return json.getJSONObject("firebase_config");
    }

    public String getLogin() {
        return json.getString("admin_login");
    }

    public String getPassword() {
        return json.getString("admin_password");
    }

    public String getServerHost() {
        return json.getString("server_host");
    }

    public int getWsPortDedicated(){
        return json.getInt("websocket_port_dedicated");
    }

    public int getWssPortDedicated(){
        return json.getInt("websocket_secured_port_dedicated");
    }

    public int getWsPortFirebase(){
        return json.getInt("websocket_port_firebase");
    }

    public int getWssPortFirebase(){
        return json.getInt("websocket_secured_port_firebase");
    }

    public int getHttpPort(){
        return json.getInt("http_port");
    }

    public int getHttpsPort(){
        return json.getInt("http_secured_port");
    }

    public String getWebRootDirectory() {
        return json.getString("web_root_directory");
    }

    public String getKeystoreFilename() {
        return json.getString("keystore_filename");
    }

    public boolean isDebugMode() {
        return json.getBoolean("debug_mode");
    }

    public String getLogFile() {
        if(json.has("log_file")) {
            return json.getString("log_file");
        } else {
            return "waytous.log";
        }
    }

    public JSONArray getTypes() {
        return json.getJSONArray("types");
    }

}
