package com.edeqa.waytous.helpers;

import android.annotation.SuppressLint;
import android.location.Location;

import com.edeqa.helpers.interfaces.Runnable2;
import com.edeqa.waytous.State;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.ConcurrentHashMap;

import static com.edeqa.waytous.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytous.Constants.USER_COLOR;
import static com.edeqa.waytous.Constants.USER_DESCRIPTION;
import static com.edeqa.waytous.Constants.USER_NAME;
import static com.edeqa.waytous.Constants.USER_PROVIDER;
import static com.edeqa.waytous.helpers.Events.CHANGE_COLOR;
import static com.edeqa.waytous.helpers.Events.CHANGE_NAME;
import static com.edeqa.waytous.helpers.Events.CHANGE_NUMBER;

/**
 * Created 10/23/16.
 */

public class MyUsers {
    private Map<Integer,MyUser> users;
    private int myNumber = 0;

    @SuppressLint("UseSparseArrays")
    public MyUsers(){
        users = new ConcurrentHashMap<>();
    }

    public MyUser setMe() {
        if(users.containsKey(myNumber)){
            users.remove(myNumber);
        }
        State.getInstance().getMe().fire(CHANGE_NUMBER, myNumber);
        users.put(myNumber,State.getInstance().getMe());

        return State.getInstance().getMe();
    }

    public void forAllUsers(Runnable2<Integer, MyUser> callback) {
        forMe(callback);
        forAllUsersExceptMe(callback);
    }

    public void forSelectedUsers(Runnable2<Integer, MyUser> callback) {
        for (Map.Entry<Integer, MyUser> entry : users.entrySet()) {
            MyUser user = entry.getValue();
            if (user == null || user.getProperties() == null || !user.getProperties().isSelected()) continue;
            forUser(entry.getKey(), callback);
        }
    }

    public void forUser(int number,Runnable2<Integer, MyUser> callback) {
        if(users.containsKey(number) && users.get(number) != null){
            callback.call(number,users.get(number));
        }
    }

    public void forMe(Runnable2<Integer, MyUser> callback) {
        forUser(myNumber,callback);
    }

    public void forAllUsersExceptMe(Runnable2<Integer, MyUser> callback) {
        try {
            if (myNumber != 0) {
                forUser(0, callback);
            }
            for (Iterator<Map.Entry<Integer, MyUser>> iterator = users.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<Integer, MyUser> entry = iterator.next();
                if (entry.getKey() == myNumber || entry.getKey() == 0) continue;
                forUser(entry.getKey(), callback);
            }
        } catch(Exception e){
            e.printStackTrace();
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
        MyUser myUser = null;
        if (!users.containsKey(o.getInt(RESPONSE_NUMBER))) {
            try {
                myUser = new MyUser();
                myUser.getProperties().setNumber(o.getInt(RESPONSE_NUMBER));
                if(o.has(USER_COLOR)) myUser.getProperties().setColor(o.getInt(USER_COLOR));
                if(o.has(USER_DESCRIPTION)) myUser.getProperties().setDescription(o.getString(USER_DESCRIPTION));
                if(o.has(USER_NAME)) {
                    myUser.getProperties().setName(o.getString(USER_NAME));
                    myUser.fire(CHANGE_NAME,o.getString(USER_NAME));
                }
//                if(o.has(USER_COLOR)) myUser.fire(CHANGE_COLOR,o.getInt(USER_COLOR));
                if(o.has(USER_PROVIDER)) {
                    Location location = Utils.jsonToLocation(o);
                    myUser.addLocation(location);
                }
                users.put(o.getInt(RESPONSE_NUMBER), myUser);
//                myUser.fire(CHANGE_NUMBER,o.getInt(RESPONSE_NUMBER));
            } catch (TooManyListenersException e) {
                e.printStackTrace();
            }
        } else {
            myUser = users.get(o.getInt(RESPONSE_NUMBER));
            if(o.has(USER_COLOR)) myUser.fire(CHANGE_COLOR,o.getInt(USER_COLOR));
        }
        return myUser;
    }

    public Map<Integer,MyUser> getUsers(){
        return users;
    }

    public MyUser findUserByName(String name) {
        for (Iterator<Map.Entry<Integer, MyUser>> iterator = users.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Integer, MyUser> entry = iterator.next();
            if (name.equals(entry.getValue().getProperties().getDisplayName())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public int getCountActive(){
        int count = 0;
        for (Iterator<Map.Entry<Integer, MyUser>> iterator = users.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Integer, MyUser> x = iterator.next();
            if (x.getValue().getProperties().isActive() && (x.getValue().isUser() || x.getValue().getProperties().getNumber() == myNumber)) {
                count++;
            }
        }
        return count;
    }

    public int getCountActiveTotal(){
        int count = 0;
        for (Iterator<Map.Entry<Integer, MyUser>> iterator = users.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Integer, MyUser> x = iterator.next();
            if (x.getValue().getProperties().isActive()) {
                count++;
            }
        }
        return count;
    }

    public int getCountSelected(){
        int count = 0;
        for (Iterator<Map.Entry<Integer, MyUser>> iterator = users.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Integer, MyUser> x = iterator.next();
            if (x.getValue().getProperties().isSelected() && (x.getValue().isUser() || x.getValue().getProperties().getNumber() == myNumber)) {
                count++;
            }
        }
        return count;
    }

    public int getCountSelectedTotal(){
        int count = 0;
        for (Iterator<Map.Entry<Integer, MyUser>> iterator = users.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Integer, MyUser> x = iterator.next();
            if (x.getValue().getProperties().isSelected()) {
                count++;
            }
        }
        return count;
    }

    public int getCountShown(){
        int count = 0;
        for (Iterator<Map.Entry<Integer, MyUser>> iterator = users.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<Integer, MyUser> x = iterator.next();
            if (x.getValue().isShown() && x.getValue().isUser()) {
                count++;
            }
        }
        return count;
    }


}
