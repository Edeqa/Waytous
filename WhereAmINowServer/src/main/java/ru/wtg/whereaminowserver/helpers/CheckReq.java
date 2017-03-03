package ru.wtg.whereaminowserver.helpers;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ru.wtg.whereaminowserver.servers.AbstractWainProcessor;

import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_USERS_DATA;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_DEVICE_ID;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MANUFACTURER;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MODEL;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_OS;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;

/**
 * Created by tujger on 2/2/17.
 */
public class CheckReq {

    private long timestamp;
    private MyToken token;
    private String tokenId;
    private String control;
    private String name;
    private String uid;
    private long number;
    private MyUser user;

    public CheckReq() {
        timestamp = new Date().getTime();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MyToken getToken() {
        return token;
    }

    public void setToken(MyToken token) {
        this.token = token;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    public void setUser(AbstractWainProcessor.Connection connection, JSONObject request) {
        user = new MyUser(connection, request.getString(REQUEST_DEVICE_ID));

        user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
        user.setModel(request.getString(REQUEST_MODEL));
        user.setOs(request.getString(REQUEST_OS));
        if (request.has(USER_NAME)) user.setName(request.getString(USER_NAME));

    }
}
