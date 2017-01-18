package ru.wtg.whereaminowserver.holders;

import org.json.JSONObject;

import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.helpers.MyWssServer;
import ru.wtg.whereaminowserver.interfaces.RequestHolder;

import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_LEAVE;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_MESSAGE;

/**
 * Created 1/16/17.
 */

public class LeaveRequestHolder implements RequestHolder {

    public static final String TYPE = REQUEST_LEAVE;

    public LeaveRequestHolder(MyWssServer context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result) {

        System.out.println("USER LEAVE:"+user.getName());
        // TODO user leave token

        return true;
    }


}
