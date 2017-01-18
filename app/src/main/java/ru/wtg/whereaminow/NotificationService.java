package ru.wtg.whereaminow;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.interfaces.EntityHolder;

import static ru.wtg.whereaminow.State.EVENTS.PUSH_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_PUSH;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_PRIVATE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;

/**
 * Created 1/8/17.
 */

public class NotificationService extends FirebaseMessagingService {

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

//        State.getInstance().fire(PUSH_MESSAGE, new PushMessage(remoteMessage));

//        System.out.println("PUSH:senttime:"+remoteMessage.getSentTime());
//        System.out.println("PUSH:collapsekey:"+remoteMessage.getCollapseKey());
//        System.out.println("PUSH:from:"+remoteMessage.getFrom());
//        System.out.println("PUSH:messageid:"+remoteMessage.getMessageId());
//        System.out.println("PUSH:messagetype:"+remoteMessage.getMessageType());
//        System.out.println("PUSH:to:"+remoteMessage.getTo());
//        System.out.println("PUSH:ttl:"+remoteMessage.getTtl());
//        System.out.println("PUSH:data:"+remoteMessage.getData());

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
//                    State.getInstance().getTracking().postMessage(json);

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

            /*if(data.containsKey(RESPONSE_PRIVATE)) {
                int number = Integer.parseInt(data.get(RESPONSE_PRIVATE));
                State.getInstance().getUsers().forUser(number, new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.fire(PUSH_MESSAGE, json);
                    }
                });
            } else {
                State.getInstance().fire(PUSH_MESSAGE, json);
            }*/

    }

}