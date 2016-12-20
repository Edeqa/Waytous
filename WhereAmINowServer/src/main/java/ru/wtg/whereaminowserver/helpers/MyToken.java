package ru.wtg.whereaminowserver.helpers;

import org.java_websocket.WebSocket;
import org.json.JSONObject;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_INITIAL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_WELCOME_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NUMBER;

/**
 * Created 10/8/16.
 */

public class MyToken {
    public Map<String,MyUser> users = new HashMap<String,MyUser>();
    private Long created;
    private Long changed;
    private String id;
    private String owner;
    private String welcomeMessage;
    private int count;

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
            user.setColor(selectColor(user.getNumber()));
        }

        setChanged();

        return;
    }

    private ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(Color.GREEN,Color.RED,Color.MAGENTA,Color.PINK,Color.ORANGE,
            Color.CYAN, Color.YELLOW
    ));

    private int selectColor(int number) {
        Random randomGenerator = new Random();
        int red = randomGenerator.nextInt(256);
        int green = randomGenerator.nextInt(256);
        int blue = randomGenerator.nextInt(256);

        return new Color(red,green,blue).getRGB();

//        int color = colors.get(number).getRGB();
//        return color;
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

    public Long getCreated() {
        return changed;
    }

    public void setChanged(Long changed) {
        this.changed = changed;
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
        ArrayList<MyUser> dest = new ArrayList<MyUser>();
        o.put(USER_NUMBER,fromUser.getNumber());
        for(Map.Entry<String,MyUser> x:users.entrySet()){
            if(x.getValue() != fromUser){
                dest.add(x.getValue());
            }
        }
        System.out.println("SENDTOALL:"+o);
        sendToUsers(o,dest);

    }

    public void sendToFrom(JSONObject o, int toUserNumber, MyUser fromUser) {
        ArrayList<MyUser> dest = new ArrayList<MyUser>();
        for(Map.Entry<String,MyUser> x:users.entrySet()){
            if(x.getValue().getNumber() == toUserNumber){
                dest.add(x.getValue());
                break;
            }
        }
        o.put(USER_NUMBER,fromUser.getNumber());
        System.out.println("SENDTOUSER:"+toUserNumber+":"+o);
        sendToUsers(o,dest);
    }

    public void sendToFrom(JSONObject o, MyUser toUser, MyUser fromUser) {
        ArrayList<MyUser> dest = new ArrayList<MyUser>();
        dest.add(toUser);
        o.put(USER_NUMBER,fromUser.getNumber());
        System.out.println("SENDTOUSER:"+toUser.getName()+":"+o);
        sendToUsers(o,dest);

    }

    public void sendToUsers(JSONObject o, ArrayList<MyUser> users) {
        for(MyUser x:users){
            WebSocket conn = x.getConnection();
            if(conn != null && conn.isOpen()){
                conn.send(o.toString());
            }
        }
    }

    public void sendInitialTo(JSONObject initial, MyUser user) {
        ArrayList<JSONObject> initialUsers = new ArrayList<JSONObject>();
        for(Map.Entry<String,MyUser> x:users.entrySet()){
            if(x.getValue() == user) continue;
            MyUser.MyPosition p = x.getValue().getPosition();
            if(p.timestamp > 0 && x.getValue().getConnection().getRemoteSocketAddress() != null){
                JSONObject o = p.toJSON();

                o.put(RESPONSE_NUMBER,x.getValue().getNumber());
                o.put(USER_COLOR,x.getValue().getColor());
                if(x.getValue().getName() != null && x.getValue().getName().length()>0){
                    o.put(USER_NAME,x.getValue().getName());
                }
                initialUsers.add(o);
            }
        }
        if(initialUsers.size()>0){
            initial.put(RESPONSE_INITIAL,initialUsers);
        }
        if(getWelcomeMessage() != null && getWelcomeMessage().length() > 0) {
            initial.put(RESPONSE_WELCOME_MESSAGE, getWelcomeMessage());
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
