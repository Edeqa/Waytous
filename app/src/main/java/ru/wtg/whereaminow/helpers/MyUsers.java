package ru.wtg.whereaminow.helpers;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;

/**
 * Created by tujger on 10/23/16.
 */

public class MyUsers {
    private HashMap<Integer,MyUser> users;
    private int myNumber = 0;

    public MyUsers(){
        users = new HashMap<>();
    }

    public MyUser setMe() {
        if(users.containsKey(myNumber)){
            users.remove(myNumber);
        }
        MyUser marker = new MyUser();
        users.put(myNumber,marker);
        return marker;
    }

    public MyUser getMe(){
        return users.get(myNumber);
    }

    public void forAllUsers(Callback callback) {
        forMe(callback);
        forAllUsersExceptMe(callback);
    }

    public void forUser(int number,Callback callback) {
        if(users.containsKey(number) && users.get(number) != null){
            callback.call(number,users.get(number));
        }
    }

    public void forMe(Callback callback) {
        forUser(myNumber,callback);
    }

    public void forAllUsersExceptMe(Callback callback) {
        if(myNumber != 0){
            forUser(0,callback);
        }
        for(Map.Entry<Integer,MyUser> x: users.entrySet()){
            if(x.getKey() == myNumber || x.getKey() == 0) continue;
            forUser(x.getKey(),callback);
        }
    }

    public int getMyNumber(){
        return myNumber;
    }

    public void setMyNumber(int newNumber){
        if(newNumber == myNumber) return;
        users.put(newNumber,users.get(myNumber));
        users.remove(myNumber);
        myNumber = newNumber;
    }

    public void removeAllUsersExceptMe() {
        setMyNumber(0);
        Iterator<Map.Entry<Integer, MyUser>> iter = users.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<Integer, MyUser> entry = iter.next();
            if(entry.getKey() == myNumber) continue;
            entry.getValue().hide();
            iter.remove();
        }
    }

    public MyUser addUser(JSONObject o) throws JSONException {
        if (!users.containsKey(o.getInt(RESPONSE_NUMBER))) {

            MyUser marker = new MyUser();
            if(o.has(USER_COLOR)) marker.setColor(o.getInt(USER_COLOR));
            if(o.has(USER_PROVIDER)) {
                Location location = Utils.jsonToLocation(o);
                marker.addLocation(location);
            }
            users.put(o.getInt(RESPONSE_NUMBER), marker);
            return marker;
        } else {
            if(o.has(USER_COLOR)) users.get(o.getInt(RESPONSE_NUMBER)).setColor(o.getInt(USER_COLOR));
        }
        return users.get(o.getInt(RESPONSE_NUMBER));
    }

    public interface Callback {
        void call(Integer number,MyUser marker);
    }

    public synchronized HashMap<Integer,MyUser> getUsers(){
        return users;
    }

}
