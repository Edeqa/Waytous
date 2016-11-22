package ru.wtg.whereaminow.helpers;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;
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
        MyUser me = State.getInstance().getMe();
        if(me == null){
            me = new MyUser();
            me.setSelected(true);
            State.getInstance().setMe(me);
        }
        me.setNumber(myNumber);
        users.put(myNumber,me);
        me.setActive(true);

        String name = State.getInstance().getStringPreference("my_name",null);
        if(name != null){
            me.setName(name);
        }

        return me;
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
        users.put(newNumber,users.get(myNumber));
        users.remove(myNumber);
        users.get(newNumber).setNumber(newNumber);
        myNumber = newNumber;
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

    public void removeUser(int number){
        users.get(number).removeViews();
        users.remove(number);
    }

    public MyUser addUser(JSONObject o) throws JSONException {
        if (!users.containsKey(o.getInt(RESPONSE_NUMBER))) {
            MyUser myUser = new MyUser();
            if(o.has(USER_COLOR)) myUser.setColor(o.getInt(USER_COLOR));
            if(o.has(USER_NAME)) myUser.setName(o.getString(USER_NAME));
            if(o.has(USER_PROVIDER)) {
                Location location = Utils.jsonToLocation(o);
                myUser.addLocation(location);
            }
            users.put(o.getInt(RESPONSE_NUMBER), myUser);
            myUser.setNumber(o.getInt(RESPONSE_NUMBER));
            return myUser;
        } else {
            if(o.has(USER_COLOR)) users.get(o.getInt(RESPONSE_NUMBER)).setColor(o.getInt(USER_COLOR));
        }
        users.get(o.getInt(RESPONSE_NUMBER)).setActive(true);
        return users.get(o.getInt(RESPONSE_NUMBER));
    }

    public interface Callback {
        void call(Integer number,MyUser myUser);
    }

    public synchronized HashMap<Integer,MyUser> getUsers(){
        return users;
    }


    public void setNameFor(int number, final String name){
        if(!users.containsKey(number)) return;
        String newName = name;
//        String name = users.get(number).getName();
        if(newName != null && newName.length()>0) {

        } else if (number == myNumber) {
            newName = "Me";
        } else if (number == 0) {
            newName = "Leader";
        } else {
            newName = "Friend " + number;
        }
        final String oldName = users.get(number).getName();
        if(!newName.equals(oldName)) {
            users.get(number).setName(newName);
            forUser(number, new Callback() {
                @Override
                public void call(Integer number, MyUser myUser) {
                    myUser.fire(MyUser.CHANGE_NAME,null);
//                    myUser.removeViews();
//                    myUser.createViews();
                }
            });
        }
        if(number == myNumber){
            if(State.getInstance().myTracking != null) State.getInstance().myTracking.sendMessage(USER_NAME,newName);
        }

    }


}
