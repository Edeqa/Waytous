package com.edeqa.waytousserver.helpers;

import com.edeqa.waytousserver.servers.AbstractDataProcessor;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.edeqa.waytousserver.helpers.Constants.REQUEST_PUSH;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_WELCOME_MESSAGE;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_INITIAL;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_TOKEN;
import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;
import static com.edeqa.waytousserver.helpers.Constants.USER_COLOR;
import static com.edeqa.waytousserver.helpers.Constants.USER_NAME;
import static com.edeqa.waytousserver.helpers.Constants.USER_NUMBER;


/**
 * Created 10/8/16.
 */

public class MyToken {
    public Map<String,MyUser> users = new HashMap<>();
    private Long created;
    private Long changed;
    private String id;
    private String owner;
    private String welcomeMessage;
    private int count;

    private ArrayList<Color> colors = new ArrayList<>(Arrays.asList(Color.GREEN, Color.RED, Color.MAGENTA, Color.PINK, Color.ORANGE,
            Color.CYAN, Color.YELLOW
    ));


    public MyToken(){

        String token = Utils.getUnique();

        System.out.println("NEW TOKEN CREATED:"+token);

        this.id = token;
        created = new Date().getTime();
    }

    public String getId(){
        return id;
    }

    public void addUser(MyUser user){

//        user.setToken(id);
        users.put(user.getDeviceId(),user);
        user.setNumber(count++);
        if(owner == null) setOwner(user.getDeviceId());

        if(user.getColor() == 0){
            user.setColor(Utils.selectColor(user.getNumber()));
        }

        setChanged();
    }



    public boolean removeUser(String hash){
        boolean res;
        if(users.containsKey(hash)){
            users.remove(hash);
            res = true;
        } else {
            res = false;
        }
        return res;
    }


    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String toString(){
        String res = "";
        res += "Token id:"+id+", owner:"+owner+", created:"+new Date(created);

        for (Map.Entry<String,MyUser> x: users.entrySet()) {
            res += ", \n\tuser: ["+x.getValue().toString()+"]";
        }

        return res;
    }

    public Long getChanged() {
        return changed;
    }

    public void setChanged(Long changed) {
        this.changed = changed;
    }

    public Long getCreated() {
        return changed;
    }

    public void setChanged(){
        changed = new Date().getTime();
    }

    public boolean isEmpty() {
        for(Map.Entry<String,MyUser> x:users.entrySet()){
            if(x.getValue().getAddress() != null){
                return false;
            }
        }
        return true;
    }

    public void sendToAllFrom(JSONObject o, MyUser fromUser) {
        ArrayList<MyUser> dest = fetchDestinations(o, fromUser);
        System.out.println("SEND:to all:"+o);
        sendToUsers(o,dest);
    }

    public ArrayList<MyUser> fetchDestinations(JSONObject o, MyUser fromUser){
        ArrayList<MyUser> dest = new ArrayList<>();
        o.put(USER_NUMBER,fromUser.getNumber());
        for(Map.Entry<String,MyUser> x:users.entrySet()){
            if(x.getValue() != fromUser){
                dest.add(x.getValue());
            }
        }

        return dest;
    }

    public void sendToFrom(JSONObject o, int toUserNumber, MyUser fromUser) {
        ArrayList<MyUser> dest = ooo(o,toUserNumber,fromUser);
        System.out.println("SEND:to user:"+toUserNumber+":"+o);
        sendToUsers(o,dest);
    }

    private ArrayList<MyUser> ooo(JSONObject o, int toUserNumber, MyUser fromUser) {
        ArrayList<MyUser> dest = new ArrayList<>();
        for(Map.Entry<String,MyUser> x:users.entrySet()){
            if(x.getValue().getNumber() == toUserNumber){
                dest.add(x.getValue());
                break;
            }
        }
        o.put(USER_NUMBER,fromUser.getNumber());
        return dest;
    }

    public void sendToFrom(JSONObject o, MyUser toUser, MyUser fromUser) {
        ArrayList<MyUser> dest = new ArrayList<>();
        dest.add(toUser);
        o.put(USER_NUMBER,fromUser.getNumber());
        System.out.println("SEND:to user:"+toUser.getName()+":"+o);
        sendToUsers(o,dest);

    }

    public void sendToUsers(JSONObject o, ArrayList<MyUser> users) {

        if(o.has(REQUEST_PUSH)) {
            o.remove(REQUEST_PUSH);
            JSONObject push = new JSONObject();
            try {
                o.put(RESPONSE_TOKEN, id);
                push.put("data", o);
//            push.put("to")
            } catch (JSONException e){
                e.printStackTrace();
            }

            for(MyUser x:users){
                sendToFirebase(x, push, "");
            }
        } else {

            for (MyUser x : users) {
                AbstractDataProcessor.Connection conn = x.connection;//.getConnection();
                if (conn != null && conn.isOpen()) {
                    conn.send(o.toString());
                }
            }
        }
    }

    private void sendToFirebase(MyUser to, JSONObject json, String category) {
        try {
//            URL url = new URL("http://fcm.googleapis.com/fcm/send");

            JSONObject o = new JSONObject();

            JSONObject dataSection = new JSONObject(json.toString());

            if("ios".equals(to.getOs())) {
                JSONObject notificationSection = new JSONObject();
                notificationSection.put("title", json.getString("title"));

                String body = "";
                if(json.has("body")) {
                    try{
                        LinkedHashMap<String,String> map = (LinkedHashMap<String, String>) json.get("body");
                        if(map != null && map.size()>0) {
                            for(Map.Entry<String,String> entry: map.entrySet()){
                                String value = entry.getValue().replace("[\\r\\n]+", "\\n");
                                body += entry.getKey() +": "+ value;
                            }
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                        body = json.get("body").toString();
                    }
                }
                notificationSection.put("body", body + " ");
                notificationSection.put("badge", 1);
                notificationSection.put("click_action", category);
                dataSection.remove("title");
                dataSection.remove("body");
                o.put("notification", notificationSection);
                o.put("content_available",true);
            }

            o.put("to", to.getDeviceId());
            o.put("data", dataSection);

            URL url = new URL("https://android.googleapis.com/gcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=" + SENSITIVE.getFirebaseServerKey());
            conn.setRequestMethod("POST");

            conn.setDoOutput(true);

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(o.toString().getBytes());
            outputStream.flush();

            /*DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(o.toString());
            wr.flush();*/



//            InputStream inputStream = conn.getInputStream();
//            String resp = IOUtils.toString(inputStream);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder resp = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                resp.append(inputLine);
            }
            in.close();

            System.out.println("\nSending push FCM to device");
            System.out.println("--- device name: "+to.getName()+", platform: "+to.getOs());
            System.out.println("--- token: "+to.getDeviceId().substring(0,30)+"...");
            System.out.println("--- body: "+o.toString(3));
            System.out.println("--- response: "+resp.toString());

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendInitialTo(JSONObject initial, MyUser user) {
        ArrayList<JSONObject> initialUsers = new ArrayList<>();
        for(Map.Entry<String,MyUser> x:users.entrySet()){
            if(x.getValue() == user) continue;
            MyUser.MyPosition p = x.getValue().getPosition();

            try {
                System.out.println("INITIAL:"+x.getValue().getNumber()+":"+p);

                if (/*p.timestamp > 0 && */x.getValue().connection.getRemoteSocketAddress() != null) {
                    JSONObject o = new JSONObject();
                    if(p != null) o = p.toJSON();

                    o.put(RESPONSE_NUMBER, x.getValue().getNumber());
                    o.put(USER_COLOR, x.getValue().getColor());
                    if (x.getValue().getName() != null && x.getValue().getName().length() > 0) {
                        o.put(USER_NAME, x.getValue().getName());
                    }
                    initialUsers.add(o);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        if(initialUsers.size()>0){
            initial.put(RESPONSE_INITIAL,initialUsers);
        }
        if(getWelcomeMessage() != null && getWelcomeMessage().length() > 0) {
            initial.put(REQUEST_WELCOME_MESSAGE, getWelcomeMessage());
        }

        user.send(initial.toString());
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }
}
