package ru.wtg.whereaminow.helpers;

import android.annotation.SuppressLint;
import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ru.wtg.whereaminow.State;

import static ru.wtg.whereaminow.State.EVENTS.CHANGE_COLOR;
import static ru.wtg.whereaminow.State.EVENTS.CHANGE_NAME;
import static ru.wtg.whereaminow.State.EVENTS.CHANGE_NUMBER;
import static ru.wtg.whereaminow.State.EVENTS.MAKE_ACTIVE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;

/**
 * Created 10/23/16.
 */

public class MyUsers {
    private HashMap<Integer,MyUser> users;
    private int myNumber = 0;

    @SuppressLint("UseSparseArrays")
    public MyUsers(){
        users = new HashMap<>();
    }

    public MyUser setMe() {
        if(users.containsKey(myNumber)){
            users.remove(myNumber);
        }
        State.getInstance().getMe().fire(CHANGE_NUMBER, myNumber);
        users.put(myNumber,State.getInstance().getMe());

        return State.getInstance().getMe();
    }

    public void forAllUsers(Callback callback) {
        forMe(callback);
        forAllUsersExceptMe(callback);
    }

    public void forSelectedUsers(Callback callback) {
        for (Map.Entry<Integer, MyUser> entry : users.entrySet()) {
            MyUser user = entry.getValue();
            if (user == null || user.getProperties() == null || !user.getProperties().isSelected()) continue;
            forUser(entry.getKey(), callback);
        }
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
        for (Map.Entry<Integer, MyUser> entry : users.entrySet()) {
            if (entry.getKey() == myNumber || entry.getKey() == 0) continue;
            forUser(entry.getKey(), callback);
        }
    }

    public int getMyNumber(){
        return myNumber;
    }

    public void setMyNumber(int newNumber){
        if(newNumber == myNumber) return;
        users.remove(myNumber);
        myNumber = newNumber;
        users.put(myNumber, State.getInstance().getMe());
        users.get(myNumber).fire(CHANGE_NUMBER,myNumber);
    }

    public void removeAllUsersExceptMe() {
        Iterator<Map.Entry<Integer, MyUser>> iter = users.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<Integer, MyUser> entry = iter.next();
            if(entry.getKey() == myNumber) continue;
            entry.getValue().removeViews();
            iter.remove();
        }
        setMyNumber(0);
    }

    public MyUser addUser(JSONObject o) throws JSONException {
        MyUser myUser;
        if (!users.containsKey(o.getInt(RESPONSE_NUMBER))) {
            myUser = new MyUser();
            if(o.has(USER_COLOR)) myUser.fire(CHANGE_COLOR,o.getInt(USER_COLOR));
            if(o.has(USER_NAME)) myUser.fire(CHANGE_NAME,o.getString(USER_NAME));
            if(o.has(USER_PROVIDER)) {
                Location location = Utils.jsonToLocation(o);
                myUser.addLocation(location);
            }
            users.put(o.getInt(RESPONSE_NUMBER), myUser);
            myUser.fire(CHANGE_NUMBER,o.getInt(RESPONSE_NUMBER));
        } else {
            myUser = users.get(o.getInt(RESPONSE_NUMBER));
            if(o.has(USER_COLOR)) myUser.fire(CHANGE_COLOR,o.getInt(USER_COLOR));
        }
        myUser.fire(MAKE_ACTIVE);
        return myUser;
    }

    public synchronized HashMap<Integer,MyUser> getUsers(){
        return users;
    }

    public MyUser findUserByName(String name) {
        for(Map.Entry<Integer, MyUser> entry:users.entrySet()) {
            if(name.equals(entry.getValue().getProperties().getDisplayName())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public int getCountActive(){
        int count = 0;
        for(Map.Entry<Integer,MyUser> x: users.entrySet()){
            if(x.getValue().getProperties().isActive() && (x.getValue().isUser() || x.getValue().getProperties().getNumber() == myNumber)){
                count ++;
            }
        }
        return count;
    }

    public int getCountSelected(){
        int count = 0;
        for(Map.Entry<Integer,MyUser> x: users.entrySet()){
            if(x.getValue().getProperties().isSelected() && (x.getValue().isUser() || x.getValue().getProperties().getNumber() == myNumber)){
                count ++;
            }
        }
        return count;
    }

    public int getCountAllSelected(){
        int count = 0;
        for(Map.Entry<Integer,MyUser> x: users.entrySet()){
            if(x.getValue().getProperties().isSelected()){
                count ++;
            }
        }
        return count;
    }

    public interface Callback {
        void call(Integer number,MyUser myUser);
    }

}
