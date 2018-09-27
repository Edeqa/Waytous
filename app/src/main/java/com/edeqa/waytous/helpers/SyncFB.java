package com.edeqa.waytous.helpers;

import androidx.annotation.NonNull;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.BiConsumer;
import com.edeqa.helpers.interfaces.BiFunction;
import com.edeqa.helpers.interfaces.TriConsumer;
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
    private BiFunction<String, Object, Object> onGetValue = new BiFunction<String, Object, Object>() {
        @Override
        public Object apply(String key, Object value) {
            if(isDebug()) System.out.println("Got value: " + key + ", [value]:" + value); //NON-NLS
            return null;
        }
    };
    private BiConsumer<String, Object> onAddRemoteValue = new BiConsumer<String, Object>() {
        @Override
        public void accept(String key, Object value) {
            if(isDebug()) System.out.println("Added remote: " + key + ", [value]:" + value); //NON-NLS
        }
    };
    private TriConsumer<String, Object, Object> onUpdateRemoteValue = new TriConsumer<String, Object, Object>() {
        @Override
        public void accept(String key, Object newValue, Object oldValue) {
            if(isDebug()) System.out.println("Updated remote: " + key + ", [new]:" + newValue + ", [old]:" + oldValue); //NON-NLS
        }
    };
    private BiConsumer<String, Object> onRemoveRemoteValue = new BiConsumer<String, Object>() {
        @Override
        public void accept(String key, Object value) {
            if(isDebug()) System.out.println("Removed remote: " + key + ", [value]:" + value); //NON-NLS
        }
    };
    private TriConsumer<String, Object, Object> onSaveRemoteValue = new TriConsumer<String, Object, Object>() {
        @Override
        public void accept(String key, Object newValue, Object oldValue) {
            if(isDebug()) System.out.println("Saved remote: " + key + ", [new]:" + newValue + ", [old]:" + oldValue); //NON-NLS
        }
    };
    private BiConsumer<String, Object> onAddLocalValue = new BiConsumer<String, Object>() {
        @Override
        public void accept(String key, Object value) {
            if(isDebug()) System.out.println("Added local: " + key + ", [value]:" + value); //NON-NLS
        }
    };
    private TriConsumer<String, Object, Object> onUpdateLocalValue = new TriConsumer<String, Object, Object>() {
        @Override
        public void accept(String key, Object newValue, Object oldValue) {
            if(isDebug()) System.out.println("Updated local: " + key + ", [new]:" + newValue + ", [old]:" + oldValue); //NON-NLS
        }
    };
    private BiConsumer<String, Object> onRemoveLocalValue = new BiConsumer<String, Object>() {
        @Override
        public void accept(String key, Object value) {
            if(isDebug()) System.out.println("Removed local: " + key + ", [value]:" + value); //NON-NLS
        }
    };
    private TriConsumer<String, Object, Object> onSaveLocalValue = new TriConsumer<String, Object, Object>() {
        @Override
        public void accept(String key, Object newValue, Object oldValue) {
            if(isDebug()) System.out.println("Saved local: " + key + ", [new]:" + newValue + ", [old]:" + oldValue); //NON-NLS
        }
    };
    private BiConsumer<Mode, String> onFinish = new BiConsumer<Mode, String>() {
        @Override
        public void accept(Mode mode, String key) {
            System.out.println("Finish: " + key + ", [mode]:" + mode); //NON-NLS
        }
    };
    private BiConsumer<String, Throwable> onError = new BiConsumer<String, Throwable>() {
        @SuppressWarnings("HardCodedStringLiteral")
        @Override
        public void accept(String key, Throwable error) {
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
                    getOnFinish().accept(SKIP, getKey());
                } else if(data.getValue() instanceof Map){
                    for (DataSnapshot child : data.getChildren()) {
                        getOnGetValue().apply(child.getKey(), child.getValue());
                    }
                    getOnFinish().accept(GET_REMOTE, getKey());
                } else {
                    getOnError().accept(getKey(), new Exception("Not an object or array, use 'getValue' instead."));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                getOnError().accept(getKey(), databaseError.toException());
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
            onError.accept(getKey(), new Exception("Reference not defined."));
            return null;
        }
        switch(type) {
            case ACCOUNT_PRIVATE:
                if(getUid() == null) {
                    onError.accept(getKey(), new Exception("UID not defined."));
                } else {
                    ref = getReference().child(Firebase.SECTION_USERS).child(getUid()).child(Firebase.PRIVATE);
                }
                break;
            case USER_PUBLIC:
                if(getGroup() == null) {
                    onError.accept(getKey(), new Exception("Group not defined."));
                } else if(getUserNumber() == null) {
                    onError.accept(getKey(), new Exception("UserNumber not defined."));
                } else {
                    ref = getReference().child(getGroup()).child(Firebase.USERS).child(Firebase.PUBLIC).child(getUserNumber());
                }
                break;

        }
        if(ref == null) {
            onError.accept(getKey(), new Exception("Firebase database reference not defined."));
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

    private void _getValue(final String key, final BiFunction<String, Object, Object> onGetValue, final BiConsumer<Mode, String> onFinish, final BiConsumer<String, Throwable> onError) {
        if(key == null) {
            onError.accept(getKey(), new Exception("Key not defined."));
            if(onFinish != null) onFinish.accept(getMode(), getKey());
            return;
        }
        this._ref.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot data) {
                onGetValue.apply(data.getKey(), data.getValue());
                if(onFinish != null) onFinish.accept(getMode(), data.getKey());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                onError.accept(key, error.toException());
                if(onFinish != null) onFinish.accept(getMode(), key);
            }
        });
    }

    @Override
    public void syncValue(Object value) {
        _ref = getRef(getChild());
        if(_ref == null) return;
        _syncValue(UPDATE_BOTH, value, getOnGetValue(), getOnAddRemoteValue(), getOnUpdateRemoteValue(), getOnRemoveRemoteValue(), onSaveRemoteValueWithTimestamp, getOnAddLocalValue(), getOnUpdateLocalValue(), getOnRemoveLocalValue(), getOnSaveLocalValue(), new BiConsumer<Mode, String>() {
            @Override
            public void accept(Mode mode, String key) {
                getOnFinish().accept(mode, key);
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

    private void _syncValue(final Mode mode, final Object newValue, final BiFunction<String, Object, Object> onGetValue, final BiConsumer<String,Object> onAddRemoteValue, final TriConsumer<String,Object,Object> onUpdateRemoteValue, final BiConsumer<String,Object> onRemoveRemoteValue, final TriConsumer<String, Object, Object> onSaveRemoteValue, final BiConsumer<String,Object> onAddLocalValue, final TriConsumer<String,Object,Object> onUpdateLocalValue, final BiConsumer<String,Object> onRemoveLocalValue, final TriConsumer<String,Object,Object> onSaveLocalValue, final BiConsumer<Mode, String> onFinish, final BiConsumer<String, Throwable> onError) {

        setMode(mode);

        _getValue(getKey(), new BiFunction<String, Object, Object>() {
            @Override
            public Object apply(final String key, final Object remote) {

                Map<String, Object> updates = new HashMap<>();

                Object local = newValue;
                if(mode != REMOVE_REMOTE) {
                    if (local == null) {
                        local = onGetValue.apply(key, remote);
                    }
                    if (local == null) {
                        onError.accept(key, new Exception("Local value not defined, define it or use 'ongetvalue'."));
                        return null;
                    }
                    if (!Misc.isEmpty(remote) && !remote.getClass().equals(local.getClass()) && local != ServerValue.TIMESTAMP) {
                        onError.accept(key, new Exception("Remote value [" + remote.getClass().getSimpleName() + "] is not equivalent to local value [" + local.getClass().getSimpleName() + "], use 'syncValues' for sync objects."));
                        return null;
                    }
                    if (local.equals(remote)) {
                        onFinish.accept(SKIP, key);
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
                                onUpdateLocalValue.accept(key, remote, local);
                                onSaveLocalValue.accept(key, remote, local);
                                onFinish.accept(UPDATE_LOCAL, key);
                            } else {
                                onAddLocalValue.accept(key, remote);
                                onSaveLocalValue.accept(key, remote, null);
                                onFinish.accept(ADD_LOCAL, key);
                            }
                        } else {
                            onFinish.accept(SKIP, key);
                        }
                        break;
                    case OVERRIDE_LOCAL:
                        /*if(!(local instanceof String)) {
                            onError.accept(key, new Exception("Mode OVERRIDE_LOCAL allowed only for strings."));
                            return null;
                        }*/
                        if(!Misc.isEmpty(local)) {
                            onUpdateLocalValue.accept(key, remote, local);
                            onSaveLocalValue.accept(key, remote, local);
                            onFinish.accept(OVERRIDE_LOCAL, key);
                        } else {
                            onAddLocalValue.accept(key, remote);
                            onSaveLocalValue.accept(key, remote, null);
                            onFinish.accept(ADD_LOCAL, key);
                        }
                        break;
                    case REMOVE_LOCAL:
                        if(!Misc.isEmpty(local)) {
                            onRemoveLocalValue.accept(getKey(), local);
                            onFinish.accept(REMOVE_LOCAL, getKey());
                        } else {
                            onFinish.accept(SKIP, getKey());
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
                                    _getValue(getKey(), new BiFunction<String, Object, Object>() {
                                        @Override
                                        public Object apply(String key, Object updated) {
                                            if(!Misc.isEmpty(remote)) {
                                                registerHistory(UPDATE_REMOTE, _ref.getKey() + "/" + key, updated);
                                                onUpdateRemoteValue.accept(key, updated, remote);
                                                onSaveRemoteValue.accept(key, updated, remote);
                                                onFinish.accept(UPDATE_REMOTE, key);
                                            } else {
                                                registerHistory(ADD_REMOTE, _ref.getKey() + "/" + key, updated);
                                                onAddRemoteValue.accept(key, updated);
                                                onSaveRemoteValue.accept(key, updated, null);
                                                onFinish.accept(ADD_REMOTE, key);
                                            }
                                            return null;
                                        }
                                    }, null, onError);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    onError.accept(key, e);
                                }
                            });
                        } else {
                            onFinish.accept(SKIP, key);
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
                                    _getValue(getKey(), new BiFunction<String, Object, Object>() {
                                        @Override
                                        public Object apply(String key, Object updated) {
                                            if(!Misc.isEmpty(remote)) {
                                                registerHistory(OVERRIDE_REMOTE, _ref.getKey() + "/" + key, updated);
                                                onUpdateRemoteValue.accept(key, updated, remote);
                                                onSaveRemoteValue.accept(key, updated, remote);
                                                onFinish.accept(OVERRIDE_REMOTE, key);
                                            } else {
                                                registerHistory(ADD_REMOTE, _ref.getKey() + "/" + key, updated);
                                                onAddRemoteValue.accept(key, updated);
                                                onSaveRemoteValue.accept(key, updated, null);
                                                onFinish.accept(ADD_REMOTE, key);
                                            }
                                            return null;
                                        }
                                    }, null, onError);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    onError.accept(key, e);
                                }
                            });
                        } else {
                            onFinish.accept(SKIP, key);
                        }
                        break;
                    case REMOVE_REMOTE:
                        if(!Misc.isEmpty(remote)) {
                            _ref.child(getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    registerHistory(REMOVE_REMOTE, _ref.getKey() + "/" + key, remote);
                                    onRemoveRemoteValue.accept(getKey(), remote);
                                    onFinish.accept(REMOVE_REMOTE, getKey());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    onError.accept(key, e);
                                }
                            });
                        } else {
                            onFinish.accept(SKIP, getKey());
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
                                onUpdateLocalValue.accept(getKey(), remote, local);
                                onSaveLocalValue.accept(getKey(), remote, local);
                                onFinish.accept(UPDATE_LOCAL, getKey());
                            } else {
                                onAddLocalValue.accept(getKey(), remote);
                                onSaveLocalValue.accept(getKey(), remote, null);
                                onFinish.accept(ADD_LOCAL, getKey());
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
                                    _getValue(getKey(), new BiFunction<String, Object, Object>() {
                                        @Override
                                        public Object apply(String key, Object updated) {
                                            if(!Misc.isEmpty(remote)) {
                                                registerHistory(UPDATE_REMOTE, _ref.getKey() + "/" + key, updated);
                                                onUpdateRemoteValue.accept(key, updated, remote);
                                                onSaveRemoteValue.accept(key, updated, remote);
                                                onFinish.accept(UPDATE_REMOTE, key);
                                            } else {
                                                registerHistory(ADD_REMOTE, _ref.getKey() + "/" + key, updated);
                                                onAddRemoteValue.accept(key, updated);
                                                onSaveRemoteValue.accept(key, updated, null);
                                                onFinish.accept(ADD_REMOTE, key);
                                            }
                                            return updated;
                                        }
                                    }, null, onError);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    onError.accept(key, e);
                                }
                            });
                        } else {
                            onFinish.accept(SKIP, getKey());
                        }
                        break;
                    default:
                        onError.accept(key, new Exception("Mode not defined"));
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
                getOnError().accept(getKey(), new Exception("Some of local values is not an object, use 'syncValue' for each one."));
                getOnFinish().accept(getMode(), getKey());
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
                        getOnError().accept(getKey(), new Exception("Remote value is not an object, use 'syncValue'."));
                        getOnFinish().accept(getMode(), getKey());
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
                    BiConsumer<Mode, String> _onFinish = new BiConsumer<Mode, String>() {
                        @Override
                        public void accept(Mode mode, String key) {
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
                                getOnFinish().accept(mode, key);
                                State.getInstance().setPreference("synced_" + getType(), new Date().getTime()); //NON-NLS
                                updateTimestamp();
                            }
                        }
                    };

                    for(Map.Entry<String,Object> x : updates.entrySet()) {
                        SyncFB sync = new SyncFB().setType(getType()).setChild(getKey()).setKey(x.getKey());
                        sync._ref = getRef(getKey());

                        TriConsumer<String, Object, Object> onSaveLocal = new TriConsumer<String, Object, Object>() {
                            @Override
                            public void accept(String key, Object newValue, Object oldValue) {
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
                                getOnSaveLocalValue().accept(key, newMap, oldValue);
                            }
                        };
                        TriConsumer<String, Object, Object> onSaveRemote = new TriConsumer<String, Object, Object>() {
                            @Override
                            public void accept(String key, Object newValue, Object oldValue) {
                                Map<String,Object> newMap = (Map) newValue;
                                newMap.put(Firebase.KEYS, key);
                                if(updates.containsKey(key) && updates.get(key) != null && updates.get(key) instanceof Map && !Misc.isEmpty(updates.get(key))) {
                                    ((Map) updates.get(key)).put(Firebase.KEYS, key);
                                    ((Map) updates.get(key)).put(Firebase.SYNCED, newMap.get(Firebase.SYNCED));
                                } else {
                                    values.add(newMap);
                                }
                                getOnSaveRemoteValue().accept(key, newMap, oldValue);
                            }
                        };
                        sync._syncValue(UPDATE_BOTH, x.getValue(), getOnGetValue(), getOnAddRemoteValue(), getOnUpdateRemoteValue(), getOnRemoveRemoteValue(), onSaveRemote, getOnAddLocalValue(), getOnUpdateLocalValue(), getOnRemoveLocalValue(), onSaveLocal, _onFinish, getOnError());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                getOnError().accept(getKey(), error.toException());
                getOnFinish().accept(getMode(), getKey());
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

    private TriConsumer<String, Object, Object> onSaveRemoteValueWithTimestamp = new TriConsumer<String, Object, Object>() {
        @Override
        public void accept(String key, Object newValue, Object oldValue) {
            getOnSaveRemoteValue().accept(key, newValue, oldValue);
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
    public void watch(String key, BiFunction<String, Object, Object> onChangeValue) {
        // TODO
    }

    @Override
    public void watchChanges(BiFunction<String, Object, Object> onChangeValue) {
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

    private BiFunction<String, Object, Object> getOnGetValue() {
        return onGetValue;
    }

    @Override
    public SyncFB setOnGetValue(BiFunction<String, Object, Object> onGetValue) {
        this.onGetValue = onGetValue;
        return this;
    }

    private BiConsumer<String,Object> getOnAddRemoteValue() {
        return onAddRemoteValue;
    }

    @Override
    public SyncFB setOnAddRemoteValue(BiConsumer<String, Object> onAddRemoteValue) {
        this.onAddRemoteValue = onAddRemoteValue;
        return this;
    }

    private TriConsumer<String,Object,Object> getOnUpdateRemoteValue() {
        return onUpdateRemoteValue;
    }

    @Override
    public SyncFB setOnUpdateRemoteValue(TriConsumer<String, Object, Object> onUpdateRemoteValue) {
        this.onUpdateRemoteValue = onUpdateRemoteValue;
        return this;
    }

    private BiConsumer<String,Object> getOnRemoveRemoteValue() {
        return onRemoveRemoteValue;
    }

    @Override
    public SyncFB setOnRemoveRemoteValue(BiConsumer<String, Object> onRemoveRemoteValue) {
        this.onRemoveRemoteValue = onRemoveRemoteValue;
        return this;
    }

    private TriConsumer<String,Object,Object> getOnSaveRemoteValue() {
        return onSaveRemoteValue;
    }

    @Override
    public SyncFB setOnSaveRemoteValue(TriConsumer<String, Object, Object> onSaveRemoteValue) {
        this.onSaveRemoteValue = onSaveRemoteValue;
        return this;
    }

    private BiConsumer<String,Object> getOnAddLocalValue() {
        return onAddLocalValue;
    }

    @Override
    public SyncFB setOnAddLocalValue(BiConsumer<String, Object> onAddLocalValue) {
        this.onAddLocalValue = onAddLocalValue;
        return this;
    }

    private TriConsumer<String,Object,Object> getOnUpdateLocalValue() {
        return onUpdateLocalValue;
    }

    @Override
    public SyncFB setOnUpdateLocalValue(TriConsumer<String, Object, Object> onUpdateLocalValue) {
        this.onUpdateLocalValue = onUpdateLocalValue;
        return this;
    }

    private BiConsumer<String,Object> getOnRemoveLocalValue() {
        return onRemoveLocalValue;
    }

    @Override
    public SyncFB setOnRemoveLocalValue(BiConsumer<String, Object> onRemoveLocalValue) {
        this.onRemoveLocalValue = onRemoveLocalValue;
        return this;
    }

    private TriConsumer<String,Object,Object> getOnSaveLocalValue() {
        return onSaveLocalValue;
    }

    @Override
    public SyncFB setOnSaveLocalValue(TriConsumer<String, Object, Object> onSaveLocalValue) {
        this.onSaveLocalValue = onSaveLocalValue;
        return this;
    }

    private BiConsumer<Mode, String> getOnFinish() {
        return onFinish;
    }

    @Override
    public SyncFB setOnFinish(BiConsumer<Mode, String> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    private BiConsumer<String, Throwable> getOnError() {
        return onError;
    }

    @Override
    public SyncFB setOnError(BiConsumer<String, Throwable> onError) {
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
