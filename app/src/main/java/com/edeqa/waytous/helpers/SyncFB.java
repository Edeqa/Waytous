package com.edeqa.waytous.helpers;

import android.support.annotation.NonNull;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Callable2;
import com.edeqa.helpers.interfaces.Runnable2;
import com.edeqa.helpers.interfaces.Runnable3;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.State;
import com.edeqa.waytous.interfaces.Sync;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.edeqa.waytous.interfaces.Sync.Mode.ADD_LOCAL;
import static com.edeqa.waytous.interfaces.Sync.Mode.ADD_REMOTE;
import static com.edeqa.waytous.interfaces.Sync.Mode.GET_REMOTE;
import static com.edeqa.waytous.interfaces.Sync.Mode.OVERRIDE_LOCAL;
import static com.edeqa.waytous.interfaces.Sync.Mode.OVERRIDE_REMOTE;
import static com.edeqa.waytous.interfaces.Sync.Mode.REMOVE_LOCAL;
import static com.edeqa.waytous.interfaces.Sync.Mode.REMOVE_REMOTE;
import static com.edeqa.waytous.interfaces.Sync.Mode.SKIP;
import static com.edeqa.waytous.interfaces.Sync.Mode.UPDATE_BOTH;
import static com.edeqa.waytous.interfaces.Sync.Mode.UPDATE_LOCAL;
import static com.edeqa.waytous.interfaces.Sync.Mode.UPDATE_REMOTE;

/**
 * Created 10/10/2017.
 */

@SuppressWarnings("unchecked")
public class SyncFB implements Sync {

    private boolean debug;
    private Callable2<Object, String, Object> onGetValue = new Callable2<Object, String, Object>() {
        @Override
        public Object call(String key, Object value) {
            if(isDebug()) System.out.println("Got value: " + key + ", [value]:" + value); //NON-NLS
            return null;
        }
    };
    private Runnable2<String, Object> onAddRemoteValue = new Runnable2<String, Object>() {
        @Override
        public void call(String key, Object value) {
            if(isDebug()) System.out.println("Added remote: " + key + ", [value]:" + value); //NON-NLS
        }
    };
    private Runnable3<String, Object, Object> onUpdateRemoteValue = new Runnable3<String, Object, Object>() {
        @Override
        public void call(String key, Object newValue, Object oldValue) {
            if(isDebug()) System.out.println("Updated remote: " + key + ", [new]:" + newValue + ", [old]:" + oldValue); //NON-NLS
        }
    };
    private Runnable2<String, Object> onRemoveRemoteValue = new Runnable2<String, Object>() {
        @Override
        public void call(String key, Object value) {
            if(isDebug()) System.out.println("Removed remote: " + key + ", [value]:" + value); //NON-NLS
        }
    };
    private Runnable3<String, Object, Object> onSaveRemoteValue = new Runnable3<String, Object, Object>() {
        @Override
        public void call(String key, Object newValue, Object oldValue) {
            if(isDebug()) System.out.println("Saved remote: " + key + ", [new]:" + newValue + ", [old]:" + oldValue); //NON-NLS
        }
    };
    private Runnable2<String, Object> onAddLocalValue = new Runnable2<String, Object>() {
        @Override
        public void call(String key, Object value) {
            if(isDebug()) System.out.println("Added local: " + key + ", [value]:" + value); //NON-NLS
        }
    };
    private Runnable3<String, Object, Object> onUpdateLocalValue = new Runnable3<String, Object, Object>() {
        @Override
        public void call(String key, Object newValue, Object oldValue) {
            if(isDebug()) System.out.println("Updated local: " + key + ", [new]:" + newValue + ", [old]:" + oldValue); //NON-NLS
        }
    };
    private Runnable2<String, Object> onRemoveLocalValue = new Runnable2<String, Object>() {
        @Override
        public void call(String key, Object value) {
            if(isDebug()) System.out.println("Removed local: " + key + ", [value]:" + value); //NON-NLS
        }
    };
    private Runnable3<String, Object, Object> onSaveLocalValue = new Runnable3<String, Object, Object>() {
        @Override
        public void call(String key, Object newValue, Object oldValue) {
            if(isDebug()) System.out.println("Saved local: " + key + ", [new]:" + newValue + ", [old]:" + oldValue); //NON-NLS
        }
    };
    private Runnable2<Mode, String> onFinish = new Runnable2<Mode, String>() {
        @Override
        public void call(Mode mode, String key) {
            System.out.println("Finish: " + key + ", [mode]:" + mode); //NON-NLS
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

    @Override
    public String getUserNumber() {
        return userNumber;
    }

    @Override
    public SyncFB setUserNumber(String userNumber) {
        this.userNumber = userNumber;
        return this;
    }

    @Override
    public SyncFB setUserNumber(int userNumber) {
        this.userNumber = String.valueOf(userNumber);
        return this;
    }

    @Override
    public void getValues() {
        this._ref = getRef(getChild());
        if(this._ref == null) return;

        this._ref.child(getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                if(data.getValue() == null) {
                    getOnFinish().call(SKIP, getKey());
                } else if(data.getValue() instanceof Map){
                    for (DataSnapshot child : data.getChildren()) {
                        getOnGetValue().call(child.getKey(), child.getValue());
                    }
                    getOnFinish().call(GET_REMOTE, getKey());
                } else {
                    getOnError().call(getKey(), new Exception("Not an object or array, use 'getValue' instead."));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                getOnError().call(getKey(), databaseError.toException());
            }
        });
    }


    @Override
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
        if(ref != null && child != null) {
            ref = ref.child(child);
        }
        return ref;
    }

    @Override
    public void getValue() {
        this._ref = getRef(getChild());
        if(this._ref == null) return;
        _getValue(getKey(), getOnGetValue(), getOnFinish(), getOnError());
    }

    private void _getValue(final String key, final Callable2<Object, String, Object> onGetValue, final Runnable2<Mode, String> onFinish, final Runnable2<String, Throwable> onError) {
        if(key == null) {
            onError.call(getKey(), new Exception("Key not defined."));
            if(onFinish != null) onFinish.call(getMode(), getKey());
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

    @Override
    public void syncValue(Object value) {
        _ref = getRef(getChild());
        if(_ref == null) return;
        _syncValue(UPDATE_BOTH, value, getOnGetValue(), getOnAddRemoteValue(), getOnUpdateRemoteValue(), getOnRemoveRemoteValue(), onSaveRemoteValueWithTimestamp, getOnAddLocalValue(), getOnUpdateLocalValue(), getOnRemoveLocalValue(), getOnSaveLocalValue(), new Runnable2<Mode, String>() {
            @Override
            public void call(Mode mode, String key) {
                getOnFinish().call(mode, key);
                State.getInstance().setPreference("synced_" + getType(), new Date().getTime()); //NON-NLS
            }
        }, getOnError());
    }

    @Override
    public void updateRemoteValue(Object value) {
        _ref = getRef(getChild());
        if(_ref == null) return;
        _syncValue(UPDATE_REMOTE, value, getOnGetValue(), getOnAddRemoteValue(), getOnUpdateRemoteValue(), getOnRemoveRemoteValue(), onSaveRemoteValueWithTimestamp, getOnAddLocalValue(), getOnUpdateLocalValue(), getOnRemoveLocalValue(), getOnSaveLocalValue(), getOnFinish(), getOnError());
    }

    @Override
    public void overrideRemoteValue(Object value) {
        _ref = getRef(getChild());
        if(_ref == null) return;
        _syncValue(OVERRIDE_REMOTE, value, getOnGetValue(), getOnAddRemoteValue(), getOnUpdateRemoteValue(), getOnRemoveRemoteValue(), onSaveRemoteValueWithTimestamp, getOnAddLocalValue(), getOnUpdateLocalValue(), getOnRemoveLocalValue(), getOnSaveLocalValue(), getOnFinish(), getOnError());
    }

    @Override
    public void removeRemoteValue() {
        _ref = getRef(getChild());
        if(_ref == null) return;
        _syncValue(REMOVE_REMOTE, null, getOnGetValue(), getOnAddRemoteValue(), getOnUpdateRemoteValue(), getOnRemoveRemoteValue(), onSaveRemoteValueWithTimestamp, getOnAddLocalValue(), getOnUpdateLocalValue(), getOnRemoveLocalValue(), getOnSaveLocalValue(), getOnFinish(), getOnError());
    }

    @Override
    public void removeLocalValue() {
        _ref = getRef(getChild());
        if(_ref == null) return;
        _syncValue(REMOVE_LOCAL, null, getOnGetValue(), getOnAddRemoteValue(), getOnUpdateRemoteValue(), getOnRemoveRemoteValue(), getOnSaveRemoteValue(), getOnAddLocalValue(), getOnUpdateLocalValue(), getOnRemoveLocalValue(), getOnSaveLocalValue(), getOnFinish(), getOnError());
    }

    private void _syncValue(final Mode mode, final Object newValue, final Callable2<Object, String, Object> onGetValue, final Runnable2<String,Object> onAddRemoteValue, final Runnable3<String,Object,Object> onUpdateRemoteValue, final Runnable2<String,Object> onRemoveRemoteValue, final Runnable3<String, Object, Object> onSaveRemoteValue, final Runnable2<String,Object> onAddLocalValue, final Runnable3<String,Object,Object> onUpdateLocalValue, final Runnable2<String,Object> onRemoveLocalValue, final Runnable3<String,Object,Object> onSaveLocalValue, final Runnable2<Mode, String> onFinish, final Runnable2<String, Throwable> onError) {

        setMode(mode);

        _getValue(getKey(), new Callable2<Object, String, Object>() {
            @Override
            public Object call(final String key, final Object remote) {

                Map<String, Object> updates = new HashMap<>();

                Object local = newValue;
                if(mode != REMOVE_REMOTE) {
                    if (local == null) {
                        local = onGetValue.call(key, remote);
                    }
                    if (local == null) {
                        onError.call(key, new Exception("Local value not defined, define it or use 'ongetvalue'."));
                        return null;
                    }
                    if (!Misc.isEmpty(remote) && !remote.getClass().equals(local.getClass()) && local != ServerValue.TIMESTAMP) {
                        onError.call(key, new Exception("Remote value [" + remote.getClass().getSimpleName() + "] is not equivalent to local value [" + local.getClass().getSimpleName() + "], use 'syncValues' for sync objects."));
                        return null;
                    }
                    if (local.equals(remote)) {
                        onFinish.call(SKIP, key);
                        return null;
                    }
                }
                switch (mode) {
                    case UPDATE_LOCAL:
                        boolean process = false;
                        //noinspection StatementWithEmptyBody
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
                                onFinish.call(UPDATE_LOCAL, key);
                            } else {
                                onAddLocalValue.call(key, remote);
                                onSaveLocalValue.call(key, remote, null);
                                onFinish.call(ADD_LOCAL, key);
                            }
                        } else {
                            onFinish.call(SKIP, key);
                        }
                        break;
                    case OVERRIDE_LOCAL:
                        /*if(!(local instanceof String)) {
                            onError.call(key, new Exception("Mode OVERRIDE_LOCAL allowed only for strings."));
                            return null;
                        }*/
                        if(!Misc.isEmpty(local)) {
                            onUpdateLocalValue.call(key, remote, local);
                            onSaveLocalValue.call(key, remote, local);
                            onFinish.call(OVERRIDE_LOCAL, key);
                        } else {
                            onAddLocalValue.call(key, remote);
                            onSaveLocalValue.call(key, remote, null);
                            onFinish.call(ADD_LOCAL, key);
                        }
                        break;
                    case REMOVE_LOCAL:
                        if(!Misc.isEmpty(local)) {
                            onRemoveLocalValue.call(getKey(), local);
                            onFinish.call(REMOVE_LOCAL, getKey());
                        } else {
                            onFinish.call(SKIP, getKey());
                        }
                        break;
                    case UPDATE_REMOTE:
                        process = false;
                        if(Misc.isEmpty(remote) && !Misc.isEmpty(local)) {
                            if(local instanceof Map) {
                                //noinspection unchecked
                                ((Map) local).put(Firebase.SYNCED, ServerValue.TIMESTAMP);
                            }
                            process = true;
                        } else //noinspection StatementWithEmptyBody
                            if(!Misc.isEmpty(remote) && Misc.isEmpty(local)) {
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
                                                registerHistory(UPDATE_REMOTE, _ref.getKey() + "/" + key, updated);
                                                onUpdateRemoteValue.call(key, updated, remote);
                                                onSaveRemoteValue.call(key, updated, remote);
                                                onFinish.call(UPDATE_REMOTE, key);
                                            } else {
                                                registerHistory(ADD_REMOTE, _ref.getKey() + "/" + key, updated);
                                                onAddRemoteValue.call(key, updated);
                                                onSaveRemoteValue.call(key, updated, null);
                                                onFinish.call(ADD_REMOTE, key);
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
                        } else {
                            onFinish.call(SKIP, key);
                        }
                        break;
                    case OVERRIDE_REMOTE:
                        if(!Misc.isEmpty(local)) {
                            if(local instanceof Map && local != ServerValue.TIMESTAMP) {
                                //noinspection unchecked
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
                                                registerHistory(OVERRIDE_REMOTE, _ref.getKey() + "/" + key, updated);
                                                onUpdateRemoteValue.call(key, updated, remote);
                                                onSaveRemoteValue.call(key, updated, remote);
                                                onFinish.call(OVERRIDE_REMOTE, key);
                                            } else {
                                                registerHistory(ADD_REMOTE, _ref.getKey() + "/" + key, updated);
                                                onAddRemoteValue.call(key, updated);
                                                onSaveRemoteValue.call(key, updated, null);
                                                onFinish.call(ADD_REMOTE, key);
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
                        } else {
                            onFinish.call(SKIP, key);
                        }
                        break;
                    case REMOVE_REMOTE:
                        if(!Misc.isEmpty(remote)) {
                            _ref.child(getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    registerHistory(REMOVE_REMOTE, _ref.getKey() + "/" + key, remote);
                                    onRemoveRemoteValue.call(getKey(), remote);
                                    onFinish.call(REMOVE_REMOTE, getKey());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    onError.call(key, e);
                                }
                            });
                        } else {
                            onFinish.call(SKIP, getKey());
                        }
                        break;
                    case UPDATE_BOTH:
                        boolean processLocal = false;
                        boolean processRemote = false;

                        if(Misc.isEmpty(remote) && !Misc.isEmpty(local)) {
                            processRemote = true;
                        } else if(!Misc.isEmpty(remote) && Misc.isEmpty(local)) {
                            processLocal = true;
                        } else {
                            Long localTimestamp = 0L;
                            if(local instanceof Map) {
                                if(((Map) local).containsKey(Firebase.SYNCED)) {
                                    localTimestamp = (Long) ((Map) local).get(Firebase.SYNCED);
                                } else if(!Misc.isEmpty(local)) {
                                    localTimestamp = State.getInstance().getLongPreference("synced_"+getType(), 0); //NON-NLS
                                }
                            } else {
                                localTimestamp = State.getInstance().getLongPreference("synced_"+getType(), 0); //NON-NLS
                            }
                            Long remoteTimestamp;
                            if(remote instanceof Map) {
                                if(((Map) remote).containsKey(Firebase.SYNCED)) {
                                    remoteTimestamp = (Long) ((Map) remote).get(Firebase.SYNCED);
                                } else {
                                    remoteTimestamp = new Date().getTime();
//                                    localTimestamp = State.getInstance().getLongPreference("synced_"+getType(), 0);
                                }
                            } else {
                                remoteTimestamp = new Date().getTime();
                            }
                            if(localTimestamp == 0L) {
                                processRemote = true;
                            } else if(localTimestamp > remoteTimestamp) {
                                processRemote = true;
                            } else if(localTimestamp < remoteTimestamp) {
                                processLocal = true;
                            }
                        }
                        if(processLocal) {
                            if(!Misc.isEmpty(local)) {
                                onUpdateLocalValue.call(getKey(), remote, local);
                                onSaveLocalValue.call(getKey(), remote, local);
                                onFinish.call(UPDATE_LOCAL, getKey());
                            } else {
                                onAddLocalValue.call(getKey(), remote);
                                onSaveLocalValue.call(getKey(), remote, null);
                                onFinish.call(ADD_LOCAL, getKey());
                            }
                        } else if(processRemote) {
                            if(local instanceof Map && local != ServerValue.TIMESTAMP) {
                                ((Map) local).put(Firebase.SYNCED, ServerValue.TIMESTAMP);
                                if(((Map) local).containsKey(Firebase.KEYS)) ((Map) local).remove(Firebase.KEYS);
                            }
                            updates = new HashMap<>();
                            updates.put(key, local);
                            _ref.updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    _getValue(getKey(), new Callable2<Object, String, Object>() {
                                        @Override
                                        public Object call(String key, Object updated) {
                                            if(!Misc.isEmpty(remote)) {
                                                registerHistory(UPDATE_REMOTE, _ref.getKey() + "/" + key, updated);
                                                onUpdateRemoteValue.call(key, updated, remote);
                                                onSaveRemoteValue.call(key, updated, remote);
                                                onFinish.call(UPDATE_REMOTE, key);
                                            } else {
                                                registerHistory(ADD_REMOTE, _ref.getKey() + "/" + key, updated);
                                                onAddRemoteValue.call(key, updated);
                                                onSaveRemoteValue.call(key, updated, null);
                                                onFinish.call(ADD_REMOTE, key);
                                            }
                                            return updated;
                                        }
                                    }, null, onError);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    onError.call(key, e);
                                }
                            });
                        } else {
                            onFinish.call(SKIP, getKey());
                        }
                        break;
                    default:
                        onError.call(key, new Exception("Mode not defined"));
                        break;
                }
                return null;
            }
        }, null, onError);

    }

    @Override
    public void updateLocalValue(Object value) {
        _ref = getRef(getChild());
        if(_ref == null) return;
        _syncValue(UPDATE_LOCAL, value, getOnGetValue(), getOnAddRemoteValue(), getOnUpdateRemoteValue(), getOnRemoveRemoteValue(), getOnSaveRemoteValue(), getOnAddLocalValue(), getOnUpdateLocalValue(), getOnRemoveLocalValue(), getOnSaveLocalValue(), getOnFinish(), getOnError());
    }

    @Override
    public void overrideLocalValue(Object value) {
        _ref = getRef(getChild());
        if(_ref == null) return;
        _syncValue(OVERRIDE_LOCAL, value, getOnGetValue(), getOnAddRemoteValue(), getOnUpdateRemoteValue(), getOnRemoveRemoteValue(), getOnSaveRemoteValue(), getOnAddLocalValue(), getOnUpdateLocalValue(), getOnRemoveLocalValue(), getOnSaveLocalValue(), getOnFinish(), getOnError());
    }


    @Override
    public void syncValues(final ArrayList<Map<String, Object>> values) {
        _ref = getRef(getChild());
        if(_ref == null) return;

        final Map<String, Object> updates = new HashMap<>();
        for(Object x: values) {
            if(x == null) {
                continue;
            }
            String key = null;
            if(x instanceof Map) {
                if (((Map) x).containsKey(Firebase.KEYS)) {
                    key = (String) ((Map) x).get(Firebase.KEYS);
                }
                if(key == null) {
                    key = getReference().push().getKey();
                }
                updates.put(key, x);
            } else {
                getOnError().call(getKey(), new Exception("Some of local values is not an object, use 'syncValue' for each one."));
                getOnFinish().call(getMode(), getKey());
                return;
            }
        }

        _ref.child(getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {

                Object value = data.getValue();
                final int[] counters = {0, updates.size(), 0, 0};
                if(value != null) {
                    if(!(value instanceof Map)) {
                        getOnError().call(getKey(), new Exception("Remote value is not an object, use 'syncValue'."));
                        getOnFinish().call(getMode(), getKey());
                        return;
                    } else {
                        for (DataSnapshot x : data.getChildren()) {
                            if(!updates.containsKey(x.getKey())){
                                updates.put(x.getKey(), new HashMap<String,Object>());
                                counters[1]++;
                            }
                        }
                    }
                }

                if(updates.size() > 0) {
                    Runnable2<Mode, String> _onFinish = new Runnable2<Mode, String>() {
                        @Override
                        public void call(Mode mode, String key) {
                            switch (mode) {
                                case UPDATE_REMOTE:
                                case OVERRIDE_REMOTE:
                                case ADD_REMOTE:
                                case REMOVE_REMOTE:
                                    counters[2]++;
                                    break;
                                case UPDATE_LOCAL:
                                case OVERRIDE_LOCAL:
                                case ADD_LOCAL:
                                case REMOVE_LOCAL:
                                    counters[3]++;
                                    break;
                                default:
                            }
                            counters[0]++;
                            if(counters[0] == counters[1]) {
                                getOnFinish().call(mode, key);
                                State.getInstance().setPreference("synced_" + getType(), new Date().getTime()); //NON-NLS
                                updateTimestamp();
                            }
                        }
                    };

                    for(Map.Entry<String,Object> x : updates.entrySet()) {
                        SyncFB sync = new SyncFB().setType(getType()).setChild(getKey()).setKey(x.getKey());
                        sync._ref = getRef(getKey());

                        Runnable3<String, Object, Object> onSaveLocal = new Runnable3<String, Object, Object>() {
                            @Override
                            public void call(String key, Object newValue, Object oldValue) {
                                Map<String,Object> newMap = (Map) newValue;
//                                if(oldValue != null && oldValue instanceof Map && ((Map) oldValue).containsKey(Firebase.SYNCED) && ((Map) oldValue).get(Firebase.SYNCED) == newMap.get(Firebase.SYNCED)) {
//                                    return;
//                                }
                                newMap.put(Firebase.KEYS, key);

                                if(updates.containsKey(key) && updates.get(key) != null && updates.get(key) instanceof Map && !Misc.isEmpty(updates.get(key))) {
                                    ((Map) updates.get(key)).clear();
                                    for(Map.Entry<String,Object> y:newMap.entrySet()) {
                                        ((Map) updates.get(key)).put(y.getKey(), y.getValue());
                                    }
                                } else {
                                    values.add(newMap);
                                }
                                getOnSaveLocalValue().call(key, newMap, oldValue);
                            }
                        };
                        Runnable3<String, Object, Object> onSaveRemote = new Runnable3<String, Object, Object>() {
                            @Override
                            public void call(String key, Object newValue, Object oldValue) {
                                Map<String,Object> newMap = (Map) newValue;
                                newMap.put(Firebase.KEYS, key);
                                if(updates.containsKey(key) && updates.get(key) != null && updates.get(key) instanceof Map && !Misc.isEmpty(updates.get(key))) {
                                    ((Map) updates.get(key)).put(Firebase.KEYS, key);
                                    ((Map) updates.get(key)).put(Firebase.SYNCED, newMap.get(Firebase.SYNCED));
                                } else {
                                    values.add(newMap);
                                }
                                getOnSaveRemoteValue().call(key, newMap, oldValue);
                            }
                        };
                        sync._syncValue(UPDATE_BOTH, x.getValue(), getOnGetValue(), getOnAddRemoteValue(), getOnUpdateRemoteValue(), getOnRemoveRemoteValue(), onSaveRemote, getOnAddLocalValue(), getOnUpdateLocalValue(), getOnRemoveLocalValue(), onSaveLocal, _onFinish, getOnError());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                getOnError().call(getKey(), error.toException());
                getOnFinish().call(getMode(), getKey());
            }
        });
    }

    private void registerHistory(Mode mode, String key, Object value) {
        if(!isLog()) return;
        Map<String,Object> item = new HashMap<>();

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
                item.put(Firebase.VALUE, "Array(" + ((ArrayList) value).size() + ")"); //NON-NLS
            } else {
                item.put(Firebase.VALUE, "[" + value.getClass().getSimpleName() + "]");
            }
        }
        //noinspection ConstantConditions
        getRef(null).child(Firebase.HISTORY).push().setValue(item);
    }

    private Runnable3<String, Object, Object> onSaveRemoteValueWithTimestamp = new Runnable3<String, Object, Object>() {
        @Override
        public void call(String key, Object newValue, Object oldValue) {
            getOnSaveRemoteValue().call(key, newValue, oldValue);
            updateTimestamp();
        }
    };

    private void updateTimestamp() {
        DatabaseReference ref;
        ref = null;
        switch(type) {
            case ACCOUNT_PRIVATE:
                ref = getReference().child(Firebase.SECTION_USERS).child(getUid()).child(Firebase.PRIVATE);
                break;
            case USER_PUBLIC:
                ref = getReference().child(getGroup()).child(Firebase.USERS).child(Firebase.PUBLIC).child(getUserNumber());
                break;
        }
        Map<String,Object> update = new HashMap<>();
        update.put(Firebase.SYNCED, ServerValue.TIMESTAMP);

        if(ref == null) return;
        ref.updateChildren(update);

    }

    @Override
    public void watch(String key, Callable2<Object,String,Object> onChangeValue) {
        // TODO
    }

    @Override
    public void watchChanges(Callable2<Object,String,Object> onChangeValue) {
        // TODO
    }

    private boolean isDebug() {
        return debug;
    }

    @Override
    public SyncFB setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    private Callable2<Object, String, Object> getOnGetValue() {
        return onGetValue;
    }

    @Override
    public SyncFB setOnGetValue(Callable2<Object,String,Object> onGetValue) {
        this.onGetValue = onGetValue;
        return this;
    }

    private Runnable2<String,Object> getOnAddRemoteValue() {
        return onAddRemoteValue;
    }

    @Override
    public SyncFB setOnAddRemoteValue(Runnable2<String, Object> onAddRemoteValue) {
        this.onAddRemoteValue = onAddRemoteValue;
        return this;
    }

    private Runnable3<String,Object,Object> getOnUpdateRemoteValue() {
        return onUpdateRemoteValue;
    }

    @Override
    public SyncFB setOnUpdateRemoteValue(Runnable3<String, Object, Object> onUpdateRemoteValue) {
        this.onUpdateRemoteValue = onUpdateRemoteValue;
        return this;
    }

    private Runnable2<String,Object> getOnRemoveRemoteValue() {
        return onRemoveRemoteValue;
    }

    @Override
    public SyncFB setOnRemoveRemoteValue(Runnable2<String, Object> onRemoveRemoteValue) {
        this.onRemoveRemoteValue = onRemoveRemoteValue;
        return this;
    }

    private Runnable3<String,Object,Object> getOnSaveRemoteValue() {
        return onSaveRemoteValue;
    }

    @Override
    public SyncFB setOnSaveRemoteValue(Runnable3<String, Object, Object> onSaveRemoteValue) {
        this.onSaveRemoteValue = onSaveRemoteValue;
        return this;
    }

    private Runnable2<String,Object> getOnAddLocalValue() {
        return onAddLocalValue;
    }

    @Override
    public SyncFB setOnAddLocalValue(Runnable2<String, Object> onAddLocalValue) {
        this.onAddLocalValue = onAddLocalValue;
        return this;
    }

    private Runnable3<String,Object,Object> getOnUpdateLocalValue() {
        return onUpdateLocalValue;
    }

    @Override
    public SyncFB setOnUpdateLocalValue(Runnable3<String, Object, Object> onUpdateLocalValue) {
        this.onUpdateLocalValue = onUpdateLocalValue;
        return this;
    }

    private Runnable2<String,Object> getOnRemoveLocalValue() {
        return onRemoveLocalValue;
    }

    @Override
    public SyncFB setOnRemoveLocalValue(Runnable2<String, Object> onRemoveLocalValue) {
        this.onRemoveLocalValue = onRemoveLocalValue;
        return this;
    }

    private Runnable3<String,Object,Object> getOnSaveLocalValue() {
        return onSaveLocalValue;
    }

    @Override
    public SyncFB setOnSaveLocalValue(Runnable3<String, Object, Object> onSaveLocalValue) {
        this.onSaveLocalValue = onSaveLocalValue;
        return this;
    }

    private Runnable2<Mode, String> getOnFinish() {
        return onFinish;
    }

    @Override
    public SyncFB setOnFinish(Runnable2<Mode, String> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    private Runnable2<String, Throwable> getOnError() {
        return onError;
    }

    @Override
    public SyncFB setOnError(Runnable2<String, Throwable> onError) {
        this.onError = onError;
        return this;
    }

    @Override
    public DatabaseReference getReference() {
        return reference;
    }

    @Override
    public SyncFB setReference(DatabaseReference reference) {
        this.reference = reference;
        return this;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public SyncFB setMode(Mode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public boolean isLog() {
        return log;
    }

    @Override
    public SyncFB setLog(boolean log) {
        this.log = log;
        return this;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public SyncFB setKey(String key) {
        this.key = key;
        return this;
    }

    @Override
    public String getChild() {
        return child;
    }

    @Override
    public SyncFB setChild(String child) {
        this.child = child;
        return this;
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public SyncFB setUid(String uid) {
        this.uid = uid;
        return this;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public SyncFB setType(Type type) {
        this.type = type;
        return this;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public SyncFB setGroup(String group) {
        this.group = group;
        return this;
    }
}
