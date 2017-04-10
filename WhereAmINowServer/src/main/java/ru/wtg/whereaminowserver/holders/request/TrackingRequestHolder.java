package ru.wtg.whereaminowserver.holders.request;

import org.json.JSONObject;

import java.util.Iterator;

import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.interfaces.RequestHolder;
import ru.wtg.whereaminowserver.servers.AbstractWainProcessor;

import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_TRACKING;

/**
 * Created 1/16/17.
 */

public class TrackingRequestHolder implements RequestHolder {

    public static final String TYPE = REQUEST_TRACKING;

    public TrackingRequestHolder(AbstractWainProcessor context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result) {

        user.addPosition(request);
        JSONObject o = user.getPosition().toJSON();
        Iterator iter = o.keys();
        while(iter.hasNext()) {
            String key = (String) iter.next();
            result.put(key, o.get(key));
        }

        return true;
    }

    @Override
    public boolean isSaveable() {
        return true;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }


}
