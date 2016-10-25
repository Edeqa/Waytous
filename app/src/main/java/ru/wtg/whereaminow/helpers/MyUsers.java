package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ru.wtg.whereaminowserver.helpers.MyUser;

import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;

/**
 * Created by tujger on 10/23/16.
 */

public class MyUsers {
    private Context context;
    private HashMap<Integer,MyMarker> users;
    private int myNumber = 0;

    public MyUsers(Context context){
        this.context = context;

        users = new HashMap<>();
    }

    public void setContext(Context context){
        this.context = context;
    }

    public MyMarker setMe() {
        if(users.containsKey(myNumber)) return users.get(myNumber);
        MyMarker marker = new MyMarker(context);
        users.put(myNumber,marker);
        return marker;
    }

    public MyMarker getMe(){
        return users.get(myNumber);
    }

    public void forAllUsers(Callback callback) {
        forMe(callback);
        forAllUsersExceptMe(callback);
    }

    public void forUser(int number,Callback callback) {
        if(users.containsKey(number)){
            callback.call(number,users.get(number));
        }
    }

    public void forMe(Callback callback) {
        callback.call(myNumber,users.get(myNumber));
    }

    public void forAllUsersExceptMe(Callback callback) {
        if(myNumber != 0 && users.containsKey(0)){
            callback.call(0,users.get(0));
        }
        for(Map.Entry<Integer,MyMarker> x: users.entrySet()){
            if(x.getKey() == myNumber || x.getKey() == 0) continue;
            callback.call(x.getKey(),x.getValue());
        }
    }

    public void setMyNumber(int newNumber){
        if(newNumber == myNumber) return;
        users.put(newNumber,users.get(myNumber));
        users.remove(myNumber);
        myNumber = newNumber;
    }

    public void removeAllUsersExceptMe() {
        setMyNumber(0);
        for(Map.Entry<Integer,MyMarker> x: users.entrySet()){
            if(x.getKey() == myNumber) continue;
            users.remove(x.getKey());
        }
    }

    public MyMarker addUser(JSONObject o) throws JSONException {
        if (!users.containsKey(o.getInt(RESPONSE_NUMBER))) {

            MyMarker marker = new MyMarker(context);
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
        public void call(Integer number,MyMarker marker);
    }

    public HashMap<Integer,MyMarker> getUsers(){
        return users;
    }

}
