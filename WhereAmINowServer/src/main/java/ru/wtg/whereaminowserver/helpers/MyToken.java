package ru.wtg.whereaminowserver.helpers;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tujger on 10/8/16.
 */

public class MyToken {
    public Map<String,MyUser> users = new HashMap<>();
    private Long timestamp;
    private Long changed;
    private String id;
    private String owner;

    public MyToken(){

        String token = Utils.getUnique();

        System.out.println("NEW TOKEN CREATED:"+token);

        this.id = token;
        timestamp = new Date().getTime();
    }
    public String getId(){
        return id;
    }

    public void addUser(MyUser user){

        user.setToken(id);
        users.put(user.getDeviceId(),user);
        if(owner == null) setOwner(user.getDeviceId());

        setChanged();

        return;
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
        res += "Token id:"+id+", owner:"+owner+", timestamp:"+new Date(timestamp);

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

    public void setChanged(){
        changed = new Date().getTime();
    }

}
