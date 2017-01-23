package ru.wtg.whereaminowserver.holders;

import org.json.JSONObject;

import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.interfaces.RequestHolder;
import ru.wtg.whereaminowserver.servers.MyWssServer;

import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_DELIVERY_CONFIRMATION;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;

/**
 * Created 1/19/17.
 */

public class DeliveryFlagHolder implements RequestHolder {

    public static final String TYPE = REQUEST_DELIVERY_CONFIRMATION;

    public DeliveryFlagHolder(MyWssServer context) {

    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result) {

        String id = request.getString(REQUEST_DELIVERY_CONFIRMATION);
        JSONObject o = new JSONObject();
        try {
            o.put(RESPONSE_STATUS, request.get(REQUEST));
            o.put(REQUEST_DELIVERY_CONFIRMATION, id);
            user.send(o);
        } catch(Exception e){
            e.printStackTrace();
        }

        return true;
    }


}
