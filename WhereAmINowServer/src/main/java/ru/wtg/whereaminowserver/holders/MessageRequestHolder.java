package ru.wtg.whereaminowserver.holders;

import org.json.JSONObject;

import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.helpers.MyWssServer;
import ru.wtg.whereaminowserver.interfaces.RequestHolder;

import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_MESSAGE;

/**
 * Created 1/16/17.
 */

public class MessageRequestHolder implements RequestHolder {

    public static final String TYPE = REQUEST_MESSAGE;

    public MessageRequestHolder(MyWssServer context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result) {

        result.put(USER_MESSAGE, request.getString(USER_MESSAGE));

        return true;
    }


}
