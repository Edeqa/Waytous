package ru.wtg.whereaminowserver.holders.request;

import org.json.JSONObject;

import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.servers.MyWssServer;
import ru.wtg.whereaminowserver.interfaces.RequestHolder;

import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_ADMIN;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_LEAVE;

/**
 * Created 1/16/17.
 */

public class LeaveRequestHolder implements RequestHolder {

    public static final String TYPE = REQUEST_ADMIN;

    public LeaveRequestHolder(MyWssServer context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result) {


        return true;
    }

    @Override
    public boolean isSaveable() {
        return false;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }


}
