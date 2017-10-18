package com.edeqa.waytous.interfaces;

import com.edeqa.helpers.interfaces.Callable2;
import com.edeqa.helpers.interfaces.Runnable2;
import com.edeqa.helpers.interfaces.Runnable3;
import com.edeqa.waytous.helpers.SyncFB;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created 10/16/2017.
 */

public interface Sync {
    String CREATE_KEY = "$create_key$";

    String getUserNumber();

    Sync setUserNumber(String userNumber);

    Sync setUserNumber(int userNumber);

    void getValues();

    boolean ready();

    void getValue();

    void syncValue(Object value);

    void updateRemoteValue(Object value);

    void overrideRemoteValue(Object value);

    void removeRemoteValue();

    void removeLocalValue();

    void updateLocalValue(Object value);

    void overrideLocalValue(Object value);

    void syncValues(ArrayList<Map<String, Object>> values);

    void watch(String key, Callable2<Object, String, Object> onChangeValue);

    void watchChanges(Callable2<Object, String, Object> onChangeValue);

    Sync setDebug(boolean debug);

    Sync setOnGetValue(Callable2<Object,String,Object> onGetValue);

    Sync setOnAddRemoteValue(Runnable2<String, Object> onAddRemoteValue);

    Sync setOnUpdateRemoteValue(Runnable3<String, Object, Object> onUpdateRemoteValue);

    Sync setOnRemoveRemoteValue(Runnable2<String, Object> onRemoveRemoteValue);

    Sync setOnSaveRemoteValue(Runnable3<String, Object, Object> onSaveRemoteValue);

    Sync setOnAddLocalValue(Runnable2<String, Object> onAddLocalValue);

    Sync setOnUpdateLocalValue(Runnable3<String, Object, Object> onUpdateLocalValue);

    Sync setOnRemoveLocalValue(Runnable2<String, Object> onRemoveLocalValue);

    Sync setOnSaveLocalValue(Runnable3<String, Object, Object> onSaveLocalValue);

    Sync setOnFinish(Runnable2<Mode, String> onFinish);

    Sync setOnError(Runnable2<String, Throwable> onError);

    DatabaseReference getReference();

    Sync setReference(DatabaseReference reference);

    Mode getMode();

    Sync setMode(Mode mode);

    boolean isLog();

    Sync setLog(boolean log);

    String getKey();

    SyncFB setKey(String key);

    String getChild();

    Sync setChild(String child);

    String getUid();

    Sync setUid(String uid);

    Type getType();

    Sync setType(Type type);

    String getGroup();

    Sync setGroup(String group);

    public enum Type {
        ACCOUNT_PRIVATE("account-private"),
        USER_PUBLIC("user-public");

        private String id;
        Type(String id) {
            this.id = id;
        }
        public String toString() {
            return this.id;
        }
    }

    public enum Mode {
        ADD_REMOTE("ra"),
        UPDATE_REMOTE("ru"),
        OVERRIDE_REMOTE("ro"),
        REMOVE_REMOTE("rr"),
        ADD_LOCAL("la"),
        UPDATE_LOCAL("lu"),
        OVERRIDE_LOCAL("lo"),
        REMOVE_LOCAL("lr"),
        UPDATE_BOTH("bu"),
        SKIP("sk"),
        GET_REMOTE("rg");

        private String id;
        Mode(String id) {
            this.id = id;
        }
        public String toString() {
            return this.id;
        }
    }
}
