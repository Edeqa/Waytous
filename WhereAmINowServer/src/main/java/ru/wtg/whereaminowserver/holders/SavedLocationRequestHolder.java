package ru.wtg.whereaminowserver.holders;

import org.json.JSONObject;

import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.helpers.MyWssServer;
import ru.wtg.whereaminowserver.interfaces.RequestHolder;

import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_SAVED_LOCATION;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_ADDRESS;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DESCRIPTION;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LATITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LONGITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;

/**
 * Created 1/16/17.
 */

public class SavedLocationRequestHolder implements RequestHolder {

    public static final String TYPE = REQUEST_SAVED_LOCATION;

    public SavedLocationRequestHolder(MyWssServer context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result) {

        System.out.println("SAVEDLOCATION:");

        result.put(USER_LATITUDE, request.getDouble(USER_LATITUDE));
        result.put(USER_LONGITUDE, request.getDouble(USER_LONGITUDE));
        if(request.has(USER_ADDRESS)) {
            result.put(USER_ADDRESS, request.getString(USER_ADDRESS));
        }
        result.put(USER_NAME, request.getString(USER_NAME));
        if(request.has(USER_DESCRIPTION)) {
            result.put(USER_DESCRIPTION, request.getString(USER_DESCRIPTION));
        }

        return true;
    }


}
