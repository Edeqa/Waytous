package com.edeqa.waytous.interfaces;

import com.edeqa.helpers.interfaces.BiConsumer;
import com.edeqa.helpers.interfaces.BiFunction;
import com.edeqa.helpers.interfaces.TriConsumer;
import com.edeqa.waytous.helpers.SyncFB;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created 10/16/2017.
 */

@SuppressWarnings({"HardCodedStringLiteral", "unused"})
public interface Sync {
    @SuppressWarnings("unused")
    String CREATE_KEY = "$create_key$";

    String getUserNumber();

    Sync setUserNumber(String userNumber);

    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
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

    void watch(String key, BiFunction<String, Object, Object> onChangeValue);

    void watchChanges(BiFunction<String, Object, Object> onChangeValue);

    @SuppressWarnings("SameParameterValue")
    Sync setDebug(boolean debug);

    Sync setOnGetValue(BiFunction<String, Object, Object> onGetValue);

    Sync setOnAddRemoteValue(BiConsumer<String, Object> onAddRemoteValue);

    Sync setOnUpdateRemoteValue(TriConsumer<String, Object, Object> onUpdateRemoteValue);

    Sync setOnRemoveRemoteValue(BiConsumer<String, Object> onRemoveRemoteValue);

    Sync setOnSaveRemoteValue(TriConsumer<String, Object, Object> onSaveRemoteValue);

    Sync setOnAddLocalValue(BiConsumer<String, Object> onAddLocalValue);

    Sync setOnUpdateLocalValue(TriConsumer<String, Object, Object> onUpdateLocalValue);

    Sync setOnRemoveLocalValue(BiConsumer<String, Object> onRemoveLocalValue);

    Sync setOnSaveLocalValue(TriConsumer<String, Object, Object> onSaveLocalValue);

    Sync setOnFinish(BiConsumer<Mode, String> onFinish);

    Sync setOnError(BiConsumer<String, Throwable> onError);

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

    enum Type {
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

    enum Mode {
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
