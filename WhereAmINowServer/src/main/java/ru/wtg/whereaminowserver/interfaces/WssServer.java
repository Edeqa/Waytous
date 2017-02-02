package ru.wtg.whereaminowserver.interfaces;

import java.util.concurrent.ConcurrentHashMap;

import ru.wtg.whereaminowserver.helpers.CheckReq;
import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;

/**
 * Created 2/2/17.
 */
public interface WssServer {
    ConcurrentHashMap<String, MyToken> getTokens();

    ConcurrentHashMap<String, MyToken> getIpToToken();

    ConcurrentHashMap<String, MyUser> getIpToUser();

    ConcurrentHashMap<String, CheckReq> getIpToCheck();
}
