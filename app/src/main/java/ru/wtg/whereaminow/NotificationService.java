package ru.wtg.whereaminow;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import ru.wtg.whereaminow.interfaces.EntityHolder;

import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_PUSH;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;

/**
 * Created 1/8/17.
 */

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        if(State.getInstance().tracking_disabled()) return;

        if(data != null && data.containsKey("data")) {
            try {
                final JSONObject json = new JSONObject(data.get("data"));
                if(json.has(RESPONSE_TOKEN)) {
                    String token = json.getString(RESPONSE_TOKEN);
                    if (!token.equals(State.getInstance().getTracking().getToken())) {
                        return;
                    }
                    json.remove(RESPONSE_TOKEN);

                    String responseStatus = json.getString(RESPONSE_STATUS);
                    json.put(REQUEST_PUSH, true);
                    EntityHolder holder = State.getInstance().getEntityHolder(responseStatus);
                    if(holder != null) {
                        holder.perform(json);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}