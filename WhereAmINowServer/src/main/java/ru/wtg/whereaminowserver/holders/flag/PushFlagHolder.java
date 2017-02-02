package ru.wtg.whereaminowserver.holders.flag;

import org.json.JSONObject;

import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.interfaces.FlagHolder;
import ru.wtg.whereaminowserver.interfaces.RequestHolder;
import ru.wtg.whereaminowserver.servers.MyWssServer;

import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_PUSH;

/**
 * Created 1/19/17.
 */

public class PushFlagHolder implements FlagHolder {

    public static final String TYPE = REQUEST_PUSH;

    public PushFlagHolder(MyWssServer context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result) {

        System.out.println("PUSHFLAGHOLDER:" + request);

        if (request.has(REQUEST_PUSH) && request.getBoolean(REQUEST_PUSH)){
            result.put(REQUEST_PUSH, true);
        }
        return true;
    }


}
