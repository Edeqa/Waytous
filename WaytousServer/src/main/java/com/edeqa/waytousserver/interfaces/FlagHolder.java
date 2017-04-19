package com.edeqa.waytousserver.interfaces;

import com.edeqa.waytousserver.helpers.MyToken;
import com.edeqa.waytousserver.helpers.MyUser;

import org.json.JSONObject;


/**
 * Created 1/16/17.
 */
public interface FlagHolder {
    String getType();

    boolean perform(MyToken token, MyUser user, JSONObject request, JSONObject result);

}
