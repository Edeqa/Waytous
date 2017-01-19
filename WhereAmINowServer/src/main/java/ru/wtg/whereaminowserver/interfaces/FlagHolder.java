package ru.wtg.whereaminowserver.interfaces;

import org.json.JSONObject;

import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;

/**
 * Created 1/19/17.
 */
public interface FlagHolder {
    String getType();

    boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result);
}
