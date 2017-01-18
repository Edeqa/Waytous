package ru.wtg.whereaminow.interfaces;

import org.json.JSONObject;

/**
 * Created 1/15/17.
 */
public interface TrackingCallback {
    void onCreating();

    void onJoining(String tokenId);

    void onReconnecting();

    void onAccept(JSONObject o);

    //        void onExpire();
    void onReject(String reason);

    void onStop();

    void onClose();

    //        void onOpen();
    void onMessage(JSONObject message);
//        void onError();
}
