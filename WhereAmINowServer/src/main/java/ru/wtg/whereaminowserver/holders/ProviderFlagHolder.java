package ru.wtg.whereaminowserver.holders;

import org.json.JSONObject;

import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.interfaces.RequestHolder;
import ru.wtg.whereaminowserver.servers.MyWssServer;

/**
 * Created 1/19/17.
 */

public class ProviderFlagHolder implements RequestHolder {

    public static final String TYPE = "provider";

    public ProviderFlagHolder(MyWssServer context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result) {

        System.out.println("PROVIDERFLAGHOLDER:" + request);
        return true;
    }


}
