package ru.wtg.whereaminowserver.helpers;

import java.util.Date;

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
}
