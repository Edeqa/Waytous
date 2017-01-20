package ru.wtg.whereaminowserver.holders;

import org.json.JSONObject;

import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.servers.MyWssServer;
import ru.wtg.whereaminowserver.interfaces.RequestHolder;

import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_WELCOME_MESSAGE;

/**
 * Created 1/16/17.
 */

public class WelcomeMessageRequestHolder implements RequestHolder {

    public static final String TYPE = REQUEST_WELCOME_MESSAGE;

    public WelcomeMessageRequestHolder(MyWssServer context) {

    }


    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result) {

        String text = request.getString(REQUEST_WELCOME_MESSAGE);
        if (user.getNumber() == 0 && text != null) {
            token.setWelcomeMessage(text);
        }
        System.out.println("UPDATE:welcome message:" + request.getString(REQUEST_WELCOME_MESSAGE));

        return false;
    }


}
