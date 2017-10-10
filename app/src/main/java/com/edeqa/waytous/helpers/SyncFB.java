package com.edeqa.waytous.helpers;

import android.support.annotation.NonNull;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Callable2;
import com.edeqa.helpers.interfaces.Callable3;
import com.edeqa.helpers.interfaces.Runnable2;
import com.edeqa.helpers.interfaces.Runnable3;
import com.edeqa.helpers.interfaces.Runnable4;
import com.edeqa.waytous.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.edeqa.waytous.helpers.SyncFB.Mode.ADD_REMOTE;
import static com.edeqa.waytous.helpers.SyncFB.Mode.OVERRIDE_REMOTE;
import static com.edeqa.waytous.helpers.SyncFB.Mode.UPDATE_REMOTE;

/**
 * Created 10/10/2017.
 */

public class SyncFB {

    public final static String CREATE_KEY = "$create_key$";

    private boolean debug;
    private Callable2<Object, String, Object> onGetValue = new Callable2<Object, String, Object>() {
        @Override
        public Object call(String key, Object value) {
            if(isDebug()) System.out.println("Got value: " + key + ", [value]:" + value);
            return null;
        }
    };
    private Runnable2<String, Object> onAddRemoteValue = new Runnable2<String, Object>() {
        @Override
        public void call(String key, Object value) {
            if(isDebug()) System.out.println("Added remote: " + key + ", [value]:" + value);
        }
    };
    private Runnable3<String, Object, Object> onUpdateRemoteValue = new Runnable3<String, Object, Object>() {
        @Override
        public void call(String key, Object newValue, Object oldValue) {
            if(isDebug()) System.out.println("Updated remote: " + key + ", [new]:" + newValue + ", [old]:" + oldValue);
        }
    };
    private Runnable2<String, Object> onRemoveRemoteValue = new Runnable2<String, Object>() {
        @Override
        public void call(String key, Object value) {
            if(isDebug()) System.out.println("Removed remote: " + key + ", [value]:" + value);
        }
    };
    private Runnable3<String, Object, Object> onSaveRemoteValue = new Runnable3<String, Object, Object>() {
        @Override
        public void call(String key, Object newValue, Object oldValue) {
            if(isDebug()) System.out.println("Saved remote: " + key + ", [new]:" + newValue + ", [old]:" + oldValue);
        }
    };
    private Runnable2<String, Object> onAddLocalValue = new Runnable2<String, Object>() {
        @Override
        public void call(String key, Object value) {
            if(isDebug()) System.out.println("Added local: " + key + ", [value]:" + value);
        }
    };
    private Runnable3<String, Object, Object> onUpdateLocalValue = new Runnable3<String, Object, Object>() {
        @Override
        public void call(String key, Object newValue, Object oldValue) {
            if(isDebug()) System.out.println("Updated local: " + key + ", [new]:" + newValue + ", [old]:" + oldValue);
        }
    };
    private Runnable2<String, Object> onRemoveLocalValue = new Runnable2<String, Object>() {
        @Override
        public void call(String key, Object value) {
            if(isDebug()) System.out.println("Removed local: " + key + ", [value]:" + value);
        }
    };
    private Runnable3<String, Object, Object> onSaveLocalValue = new Runnable3<String, Object, Object>() {
        @Override
        public void call(String key, Object newValue, Object oldValue) {
            if(isDebug()) System.out.println("Saved local: " + key + ", [new]:" + newValue + ", [old]:" + oldValue);
        }
    };
    private Runnable2<Mode, String> onFinish = new Runnable2<Mode, String>() {
        @Override
        public void call(Mode key, String value) {
            System.out.println("Finish: " + key + ", [mode]:" + value);
        }
    };
    private Runnable2<String, Throwable> onError = new Runnable2<String, Throwable>() {
        @SuppressWarnings("HardCodedStringLiteral")
        @Override
        public void call(String key, Throwable error) {
            System.err.println("Error: " + key);
            error.printStackTrace();
        }
    };
    private DatabaseReference reference;
    private DatabaseReference _ref;
    private Mode mode;
    private boolean log;
    private String key;
    private String child;
    private String uid;
    private String userNumber;
    private Type type;
    private String group;

    public String getUserNumber() {
        return userNumber;
    }

    public SyncFB setUserNumber(String userNumber) {
        this.userNumber = userNumber;
        return this;
    }

    public SyncFB setUserNumber(int userNumber) {
        this.userNumber = String.valueOf(userNumber);
        return this;
    }

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
        ADD_REMOTE("ar"),
        UPDATE_REMOTE("ur"),
        OVERRIDE_REMOTE("or"),
        REMOVE_REMOTE("rr"),
        ADD_LOCAL("al"),
        UPDATE_LOCAL("ul"),
        OVERRIDE_LOCAL("ol"),
        REMOVE_LOCAL("rl"),
        UPDATE_BOTH("ub"),
        SKIP("sk");

        private String id;
        Mode(String id) {
            this.id = id;
        }
        public String toString() {
            return this.id;
        }
    }


    ChildEventListener lastKeyListener;
    ChildEventListener valuesListener;
    public void getValues() {

        this._ref = getRef(getChild());
        if(this._ref == null) return;

        lastKeyListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot data, String s) {

                SyncFB.this._ref.child(getKey()).removeEventListener(lastKeyListener);
                final String lastKey = data.getKey();

                System.err.println("LASTKEY:"+lastKey);

                if(lastKey == null || lastKey.length() == 0) {
                    getOnFinish().call(getMode(), "[no keys]");
                    return;
                }

                valuesListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot data, String s) {
                        System.out.println("KEY:"+data.getKey());
                        getOnGetValue().call(data.getKey(), data.getValue());
                        if(lastKey.equals(data.getKey())) {
                            SyncFB.this._ref.child(getKey()).removeEventListener(valuesListener);
                            getOnFinish().call(getMode(), data.getKey());
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        getOnError().call(getKey(), error.toException());
                        SyncFB.this._ref.child(getKey()).removeEventListener(valuesListener);
                        getOnFinish().call(getMode(), getKey());
                    }
                };
                SyncFB.this._ref.child(getKey()).orderByKey().limitToLast(100).addChildEventListener(valuesListener);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError error) {
                getOnError().call(getKey(), error.toException());
                SyncFB.this._ref.child(getKey()).removeEventListener(lastKeyListener);
            }
        };
        this._ref.child(getKey()).orderByKey().limitToLast(1).addChildEventListener(lastKeyListener);
    }


    public boolean ready() {
        this._ref = getRef(getChild());
        if(this._ref == null) return false;
//        if(!firebase || !firebase.auth() || !firebase.auth().currentUser || !firebase.auth().currentUser.uid) return false;
        return true;
    }

    private DatabaseReference getRef(String child) {
        DatabaseReference ref = null;
        if(getReference() == null) {
            onError.call(getKey(), new Exception("Reference not defined."));
            return null;
        }
        switch(type) {
            case ACCOUNT_PRIVATE:
                if(getUid() == null) {
                    onError.call(getKey(), new Exception("UID not defined."));
                } else {
                    ref = getReference().child(Firebase.SECTION_USERS).child(getUid()).child(Firebase.PRIVATE);
                }
                break;
            case USER_PUBLIC:
                if(getGroup() == null) {
                    onError.call(getKey(), new Exception("Group not defined."));
                } else if(getUserNumber() == null) {
                    onError.call(getKey(), new Exception("UserNumber not defined."));
                } else {
                    ref = getReference().child(getGroup()).child(Firebase.USERS).child(Firebase.PUBLIC).child(getUserNumber());
                }
                break;

        }
        if(ref == null) {
            onError.call(getKey(), new Exception("Firebase database reference not defined."));
        }
        if(child != null) {
            ref = ref.child(child);
        }
        return ref;
    }

    public void getValue() {
        this._ref = getRef(getChild());
        if(this._ref == null) return;
        _getValue(getKey(), getOnGetValue(), getOnFinish(), getOnError());
    }

    private void _getValue(final String key, final Callable2<Object, String, Object> onGetValue, final Runnable2<Mode, String> onFinish, final Runnable2<String, Throwable> onError) {
        if(key == null) {
            onError.call(key, new Exception("Key not defined."));
            if(onFinish != null) onFinish.call(getMode(), key);
            return;
        }
        this._ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                onGetValue.call(data.getKey(), data.getValue());
                if(onFinish != null) onFinish.call(getMode(), data.getKey());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                onError.call(key, error.toException());
                if(onFinish != null) onFinish.call(getMode(), key);
            }
        });
    }

    public void syncValue(Object value) {
        // TODO
    }

    public void updateRemoteValue(Object value) {
        // TODO
    }

    public void overrideRemoteValue(Object value) {
        _ref = getRef(getChild());
        if(_ref == null) return;
        _syncValue(OVERRIDE_REMOTE, value, getOnGetValue(), getOnAddRemoteValue(), getOnUpdateRemoteValue(), getOnRemoveRemoteValue(), onSaveRemoteValueWithTimestamp, getOnAddLocalValue(), getOnUpdateLocalValue(), getOnRemoveLocalValue(), getOnSaveLocalValue(), getOnFinish(), getOnError());
    }

    private void _syncValue(final Mode mode, final Object newValue, final Callable2<Object, String, Object> onGetValue, final Runnable2<String,Object> onAddRemoteValue, final Runnable3<String,Object,Object> onUpdateRemoteValue, Runnable2<String,Object> onRemoveRemoteValue, final Runnable3<String, Object, Object> onSaveRemoteValueWithTimestamp, final Runnable2<String,Object> onAddLocalValue, final Runnable3<String,Object,Object> onUpdateLocalValue, Runnable2<String,Object> onRemoveLocalValue, final Runnable3<String,Object,Object> onSaveLocalValue, final Runnable2<Mode, String> onFinish, final Runnable2<String, Throwable> onError) {


        _getValue(getKey(), new Callable2<Object, String, Object>() {
            @Override
            public Object call(final String key, final Object remote) {

                Map<String, Object> updates = new HashMap<>();

                Object local = newValue;
                if(Misc.isEmpty(local)) {
                    local = onGetValue.call(key, remote);
                }
                if(local == null) {
                    onError.call(key,new Exception("Local value not defined, define it or use 'ongetvalue'."));
                    return null;
                }
                if(!Misc.isEmpty(remote) && !remote.getClass().equals(local.getClass()) && local != ServerValue.TIMESTAMP) {
                    onError.call(key,new Exception("Remote value [" + (remote != null ? remote.getClass().getSimpleName() : null) +"] is not equivalent to local value [" + (local != null ? local.getClass().getSimpleName() : null) + "], use 'syncValues' for sync objects."));
                    return null;
                }
                if(local.equals(remote)) {
                    return null;
                }

                switch (mode) {
                    case UPDATE_LOCAL:
                        boolean process = false;
                        if(remote == null) {
                        } else if(Misc.isEmpty(local)) {
                            process = true;
                        } else {
                            if(local instanceof Map && ((Map) local).containsKey(Firebase.SYNCED)
                                    && (ServerValue.TIMESTAMP.equals(((Map) local).get(Firebase.SYNCED)) || Long.valueOf(((Map) local).get(Firebase.SYNCED).toString()) < Long.valueOf(((Map) remote).get(Firebase.SYNCED).toString()))) {
                                process = true;
                            }
                        }
                        if(process) {
                            if(Misc.isEmpty(local)) {
                                onUpdateLocalValue.call(key, remote, local);
                                onSaveLocalValue.call(key, remote, local);
                                onFinish.call(Mode.UPDATE_LOCAL, key);
                            } else {
                                onAddLocalValue.call(key, remote);
                                onSaveLocalValue.call(key, remote, null);
                                onFinish.call(Mode.ADD_LOCAL, key);
                            }
                        }
                        break;
                    case OVERRIDE_LOCAL:
                        if(!(local instanceof String)) {
                            onError.call(key, new Exception("Mode OVERRIDE_REMOTE allowed only for strings."));
                            return null;
                        }
                        if(!Misc.isEmpty(local)) {
                            onUpdateLocalValue.call(key, remote, local);
                            onSaveLocalValue.call(key, remote, local);
                            onFinish.call(Mode.OVERRIDE_LOCAL, key);
                        } else {
                            onAddLocalValue.call(key, remote);
                            onSaveLocalValue.call(key, remote, null);
                            onFinish.call(Mode.ADD_LOCAL, key);
                        }
                        break;
                    case UPDATE_REMOTE:
                        process = false;
                        if(Misc.isEmpty(remote) && !Misc.isEmpty(local)) {
                            if(local instanceof Map) {
                                ((Map) local).put(Firebase.SYNCED, ServerValue.TIMESTAMP);
                            }
                            process = true;
                        } else if(!Misc.isEmpty(remote) && Misc.isEmpty(local)) {

                        } else {
                            if(local instanceof Map && ((Map) local).containsKey(Firebase.SYNCED)
                                    && (ServerValue.TIMESTAMP.equals(((Map) local).get(Firebase.SYNCED)) || Long.valueOf(((Map) local).get(Firebase.SYNCED).toString()) > Long.valueOf(((Map) remote).get(Firebase.SYNCED).toString()))) {
                                process = true;
                            }

                        }
                        if(process) {
                            updates.put(key, local);
                            _ref.updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    _getValue(getKey(), new Callable2<Object, String, Object>() {
                                        @Override
                                        public Object call(String key, Object updated) {
                                            if(!Misc.isEmpty(remote)) {
                                                onUpdateRemoteValue.call(key, updated, remote);
                                                onSaveRemoteValue.call(key, updated, remote);
                                                onFinish.call(UPDATE_REMOTE, key);
                                                registerHistory(UPDATE_REMOTE, _ref.getKey() + "/" + key, updated);
                                            } else {
                                                onAddRemoteValue.call(key, updated);
                                                onSaveRemoteValue.call(key, updated, null);
                                                onFinish.call(ADD_REMOTE, key);
                                                registerHistory(ADD_REMOTE, _ref.getKey() + "/" + key, updated);
                                            }
                                            return null;
                                        }
                                    }, null, onError);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    onError.call(key, e);
                                }
                            });
                        }
                        break;
                    case OVERRIDE_REMOTE:
                        if((!Misc.isEmpty(local) && local != ServerValue.TIMESTAMP && !(local instanceof String || local instanceof Boolean || local instanceof Number
                            )) || (!Misc.isEmpty(remote) && !(remote instanceof String || remote instanceof Boolean || remote instanceof Number))) {
                            onError.call(key, new Exception("Mode OVERRIDE_REMOTE allowed only for primitives (string, number, boolean)."));
                            return null;
                        }
                        if(!Misc.isEmpty(local) && local instanceof Map) {
                            if(local != ServerValue.TIMESTAMP) {
                                ((Map) local).put(Firebase.SYNCED, ServerValue.TIMESTAMP);
                            }
                            updates.put(key, local);
                            _ref.updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    _getValue(getKey(), new Callable2<Object, String, Object>() {
                                        @Override
                                        public Object call(String key, Object updated) {
                                            if(!Misc.isEmpty(remote)) {
                                                onUpdateRemoteValue.call(key, updated, remote);
                                                onSaveRemoteValue.call(key, updated, remote);
                                                onFinish.call(OVERRIDE_REMOTE, key);
                                                registerHistory(OVERRIDE_REMOTE, _ref.getKey() + "/" + key, updated);
                                            } else {
                                                onAddRemoteValue.call(key, updated);
                                                onSaveRemoteValue.call(key, updated, null);
                                                onFinish.call(ADD_REMOTE, key);
                                                registerHistory(ADD_REMOTE, _ref.getKey() + "/" + key, updated);
                                            }
                                            return null;
                                        }
                                    }, null, onError);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    onError.call(key, e);
                                }
                            });
                        }
                        break;
                    case UPDATE_BOTH:
                        // TODO
                        break;
                    default:
                        // TODO
                        break;

                }

                return null;
            }
        }, null, getOnError());

    }

    public void updateLocalValue(Object value) {
        // TODO
    }

    public void overrideLocalValue(Object value) {
        // TODO
    }


    public void syncValues(ArrayList values) {
        // TODO
    }

    private void onFinish(Mode mode, String key, Object newValue, Object oldValue) {
        // TODO
    }

    private void registerHistory(Mode mode, String key, Object value) {
        if(!isLog()) return;
        Map item = new HashMap<String,Object>();

        item.put(Firebase.KEYS, key);
        item.put(Firebase.TIMESTAMP, ServerValue.TIMESTAMP);
        item.put(Firebase.MODE, mode.toString());
        if(value != null) {
            if(value instanceof Boolean) {
                item.put(Firebase.VALUE, value);
            } else if(value instanceof Number) {
                item.put(Firebase.VALUE, value);
            } else if(value instanceof String) {
                if(((String) value).length() < 50) {
                    item.put(Firebase.VALUE, value);
                } else {
                    item.put(Firebase.VALUE, ((String) value).substring(0,40) + "...");
                }
            } else if(value instanceof ArrayList) {
                item.put(Firebase.VALUE, "Array(" + ((ArrayList) value).size() + ")");
            } else {
                item.put(Firebase.VALUE, "[" + value.getClass().getSimpleName() + "]");
            }
        }
        getRef(null).child(Firebase.HISTORY).push().setValue(item);
    }

    private Runnable3<String, Object, Object> onSaveRemoteValueWithTimestamp = new Runnable3<String, Object, Object>() {
        @Override
        public void call(String key, Object newValue, Object oldValue) {
            getOnSaveRemoteValue().call(key, newValue, oldValue);
            updateTimestamp();
        }
    };

    private DatabaseReference ref;
    private void updateTimestamp() {
        ref = null;
        switch(type) {
            case ACCOUNT_PRIVATE:
                ref = getReference().child(Firebase.SECTION_USERS).child(getUid()).child(Firebase.PRIVATE);
                break;
            case USER_PUBLIC:
                ref = getReference().child(getGroup()).child(Firebase.USERS).child(Firebase.PUBLIC).child(getUserNumber());
                break;
        }
        Map update = new HashMap<String,Object>();
        update.put(Firebase.SYNCED, ServerValue.TIMESTAMP);

        if(ref == null) return;
        ref.updateChildren(update);

    }

    public void watch(String key, Callable2 onChangeValue) {
        // TODO
    }

    public void watchChanges(Callable2 onChangeValue) {
        // TODO
    }

    private boolean isDebug() {
        return debug;
    }

    public SyncFB setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    private Callable2<Object, String, Object> getOnGetValue() {
        return onGetValue;
    }

    public SyncFB setOnGetValue(Callable2 onGetValue) {
        this.onGetValue = onGetValue;
        return this;
    }

    private Runnable2<String,Object> getOnAddRemoteValue() {
        return onAddRemoteValue;
    }

    public SyncFB setOnAddRemoteValue(Runnable2<String,Object> onAddRemoteValue) {
        this.onAddRemoteValue = onAddRemoteValue;
        return this;
    }

    private Runnable3<String,Object,Object> getOnUpdateRemoteValue() {
        return onUpdateRemoteValue;
    }

    public SyncFB setOnUpdateRemoteValue(Runnable3<String,Object,Object> onUpdateRemoteValue) {
        this.onUpdateRemoteValue = onUpdateRemoteValue;
        return this;
    }

    private Runnable2<String,Object> getOnRemoveRemoteValue() {
        return onRemoveRemoteValue;
    }

    public SyncFB setOnRemoveRemoteValue(Runnable2<String,Object> onRemoveRemoteValue) {
        this.onRemoveRemoteValue = onRemoveRemoteValue;
        return this;
    }

    private Runnable3<String,Object,Object> getOnSaveRemoteValue() {
        return onSaveRemoteValue;
    }

    public SyncFB setOnSaveRemoteValue(Runnable3<String,Object,Object> onSaveRemoteValue) {
        this.onSaveRemoteValue = onSaveRemoteValue;
        return this;
    }

    private Runnable2<String,Object> getOnAddLocalValue() {
        return onAddLocalValue;
    }

    public SyncFB setOnAddLocalValue(Runnable2<String,Object> onAddLocalValue) {
        this.onAddLocalValue = onAddLocalValue;
        return this;
    }

    private Runnable3<String,Object,Object> getOnUpdateLocalValue() {
        return onUpdateLocalValue;
    }

    public SyncFB setOnUpdateLocalValue(Runnable3<String,Object,Object> onUpdateLocalValue) {
        this.onUpdateLocalValue = onUpdateLocalValue;
        return this;
    }

    private Runnable2<String,Object> getOnRemoveLocalValue() {
        return onRemoveLocalValue;
    }

    public SyncFB setOnRemoveLocalValue(Runnable2<String,Object> onRemoveLocalValue) {
        this.onRemoveLocalValue = onRemoveLocalValue;
        return this;
    }

    private Runnable3<String,Object,Object> getOnSaveLocalValue() {
        return onSaveLocalValue;
    }

    public SyncFB setOnSaveLocalValue(Runnable3<String,Object,Object> onSaveLocalValue) {
        this.onSaveLocalValue = onSaveLocalValue;
        return this;
    }

    private Runnable2<Mode, String> getOnFinish() {
        return onFinish;
    }

    public SyncFB setOnFinish(Runnable2<Mode, String> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    private Runnable2<String, Throwable> getOnError() {
        return onError;
    }

    public SyncFB setOnError(Runnable2<String, Throwable> onError) {
        this.onError = onError;
        return this;
    }

    public DatabaseReference getReference() {
        return reference;
    }

    public SyncFB setReference(DatabaseReference reference) {
        this.reference = reference;
        return this;
    }

    public Mode getMode() {
        return mode;
    }

    public SyncFB setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    public boolean isLog() {
        return log;
    }

    public SyncFB setLog(boolean log) {
        this.log = log;
        return this;
    }

    public String getKey() {
        return key;
    }

    public SyncFB setKey(String key) {
        this.key = key;
        return this;
    }

    public String getChild() {
        return child;
    }

    public SyncFB setChild(String child) {
        this.child = child;
        return this;
    }

    public String getUid() {
        return uid;
    }

    public SyncFB setUid(String uid) {
        this.uid = uid;
        return this;
    }

    public Type getType() {
        return type;
    }

    public SyncFB setType(Type type) {
        this.type = type;
        return this;
    }

    public String getGroup() {
        return group;
    }

    public SyncFB setGroup(String group) {
        this.group = group;
        return this;
    }
}
