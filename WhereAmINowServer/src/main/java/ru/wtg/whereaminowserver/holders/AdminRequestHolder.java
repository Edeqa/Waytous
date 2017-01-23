package ru.wtg.whereaminowserver.holders;

import org.json.JSONObject;

import java.util.HashMap;

import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.interfaces.RequestHolder;
import ru.wtg.whereaminowserver.servers.MyWssServer;

import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_LEAVE;

/**
 * Created 1/16/17.
 */

public class AdminRequestHolder implements RequestHolder {

    public static final String TYPE = REQUEST_LEAVE;

    private HashMap<String,MyUser> admins;


    public AdminRequestHolder(MyWssServer context) {
        admins = new HashMap<String, MyUser>();
    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result) {

        admins.put(user.getConnection().getRemoteSocketAddress().toString(), user);

        System.out.println("ADMIN:"+user);


        return false;
    }


}
