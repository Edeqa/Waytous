package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.CheckReq;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.Utils;
import com.edeqa.waytousserver.interfaces.RequestHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;
import com.google.firebase.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import static com.edeqa.waytousserver.helpers.Constants.DATABASE_OPTION_DATE_CHANGED;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_OPTION_DATE_CREATED;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_OPTION_DELAY_TO_DISMISS;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_OPTION_DISMISS_INACTIVE;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_OPTION_PERSISTENT;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_OPTION_REQUIRES_PASSWORD;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_OPTION_TIME_TO_LIVE_IF_EMPTY;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_SECTION_GROUPS;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_SECTION_OPTIONS;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_SECTION_PUBLIC;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_SECTION_USERS_DATA;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_SECTION_USERS_DATA_PRIVATE;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_SECTION_USERS_KEYS;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_SECTION_USERS_ORDER;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_USER_ACTIVE;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_USER_CHANGED;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_USER_COLOR;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_USER_CREATED;
import static com.edeqa.waytousserver.helpers.Constants.DATABASE_USER_NAME;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_CHECK_USER;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_DEVICE_ID;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_HASH;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_JOIN_GROUP;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_KEY;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_MANUFACTURER;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_MODEL;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_NEW_GROUP;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_OS;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytousserver.helpers.Constants.REQUEST_TOKEN;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_CONTROL;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_MESSAGE;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_NUMBER;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_SIGN;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_STATUS;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_STATUS_CHECK;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_STATUS_ERROR;
import static com.edeqa.waytousserver.helpers.Constants.RESPONSE_TOKEN;
import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;
import static com.edeqa.waytousserver.helpers.Constants.USER_DISMISSED;
import static com.edeqa.waytousserver.helpers.Constants.USER_NAME;


/**
 * Created 10/5/16.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class DataProcessorFirebaseV1 extends AbstractDataProcessor {

    public static String VERSION = "v1";
    private DatabaseReference ref;

    public DataProcessorFirebaseV1() {
        super();

        File f = new File(SENSITIVE.getFirebasePrivateKeyFile());
        try {
            Common.log("DPF1","Data Processor Firebase "+VERSION+", config file: "+f.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ref = database.getReference();

    }

    @Override
    public LinkedList<String> getRequestHoldersList() {
        LinkedList<String> classes = new LinkedList<>();
        classes.add("TrackingRequestHolder");
        classes.add("MessageRequestHolder");
        classes.add("ChangeNameRequestHolder");
        classes.add("WelcomeMessageRequestHolder");
        classes.add("LeaveRequestHolder");
        classes.add("SavedLocationRequestHolder");
        return classes;
    }

    @Override
    public LinkedList<String> getFlagsHoldersList() {
        return null;
    }

    /*   @Override
        public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
            Common.log("Main","HANDSHAKE:"+conn+":"+draft+":"+request);

            return super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
        }
    */

    @Override
    public void onMessage(final Connection conn, String message) {
        boolean disconnect = false;
        try {
            final String ip = conn.getRemoteSocketAddress().toString();
            final JSONObject request, response = new JSONObject();

            try {
                request = new JSONObject(message);
            } catch(JSONException e) {
                Common.err("DPF1","onMessage:request"+e.getMessage());
                return;
            }
            if (!request.has(REQUEST_TIMESTAMP)) return;
//            long timestamp = request.getLong(REQUEST_TIMESTAMP);
        /*if(new Date().getTime() - timestamp > LIFETIME_REQUEST_TIMEOUT*1000) {
            Common.log("Main","WSS:ignore request because of timeout");
//            conn.close(CloseFrame.GOING_AWAY, "Request timeout");
            return;
        }*/

            if (!request.has(REQUEST)) return;

            String req = request.getString(REQUEST);
            if (REQUEST_NEW_GROUP.equals(req)) {
                if (request.has(REQUEST_DEVICE_ID)) {
                    final MyGroup group = new MyGroup();

                    final ValueEventListener[] groupRegistrationListener = new ValueEventListener[1];
                    groupRegistrationListener[0] = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.getValue() == null) {
                                final MyUser user = new MyUser(conn, request.getString(REQUEST_DEVICE_ID));

                                Map<String, Object> childUpdates = new HashMap<>();
                                childUpdates.put(DATABASE_SECTION_OPTIONS + "/" + DATABASE_OPTION_TIME_TO_LIVE_IF_EMPTY, 15);
                                childUpdates.put(DATABASE_SECTION_OPTIONS + "/" + DATABASE_OPTION_REQUIRES_PASSWORD, false);
                                childUpdates.put(DATABASE_SECTION_OPTIONS + "/" + DATABASE_OPTION_PERSISTENT, false);
                                childUpdates.put(DATABASE_SECTION_OPTIONS + "/" + DATABASE_OPTION_DISMISS_INACTIVE, false);
                                childUpdates.put(DATABASE_SECTION_OPTIONS + "/" + DATABASE_OPTION_DELAY_TO_DISMISS, 300);
                                childUpdates.put(DATABASE_SECTION_OPTIONS + "/" + DATABASE_OPTION_DATE_CREATED, ServerValue.TIMESTAMP);
                                childUpdates.put(DATABASE_SECTION_OPTIONS + "/" + DATABASE_OPTION_DATE_CHANGED, ServerValue.TIMESTAMP);
                                ref.child(group.getId()).updateChildren(childUpdates);

                                ref.child(DATABASE_SECTION_GROUPS).child(group.getId()).setValue(user.getUid());
                                DatabaseReference nodeNumber = ref.child(group.getId()).child(DATABASE_SECTION_USERS_ORDER).push();
                                nodeNumber.setValue(user.getUid());

                                registerUser(group.getId(), user, request);

                                Common.log("DPF1", "onMessage:newGroup:" + conn.getRemoteSocketAddress(), "id:" + group.getId());
                            } else {
                                Common.log("DPF1", "onMessage:newGroup:" + conn.getRemoteSocketAddress(), "alreadyExists:" + group.getId());
                                group.fetchNewId();
                                ref.child(DATABASE_SECTION_GROUPS).child(group.getId()).addListenerForSingleValueEvent(groupRegistrationListener[0]);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    };

                    ref.child(DATABASE_SECTION_GROUPS).child(group.getId()).addListenerForSingleValueEvent(groupRegistrationListener[0]);
                } else {
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Cannot create group (code 15).");
                    conn.send(response.toString());
                    conn.close();
                    Common.err("DPF1","onMessage:newGroup:",response);
                }
            } else if (REQUEST_JOIN_GROUP.equals(req)) {
                if (request.has(REQUEST_TOKEN)) {

                    final String groupId = request.getString(REQUEST_TOKEN);
                    final DatabaseReference refGroup = ref.child(groupId);

                    final ValueEventListener userDataListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null) { //already was here
                                System.out.println("READED:"+dataSnapshot.getValue());

                            } else { // join as new member
                                System.out.println("REFUSED:"+dataSnapshot.getValue());

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("SINGLEERR3:"+databaseError.getMessage());

                        }
                    };

                    final ValueEventListener[] requestDataPrivateListener = new ValueEventListener[1];
                    requestDataPrivateListener[0] = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final MyUser user = new MyUser(conn, request.getString(REQUEST_DEVICE_ID));

                            int count = 1;
                            boolean found = false;
                            Object value = dataSnapshot.getValue();
                            if(value == null) {
                                dataSnapshot.getRef().push().setValue(user.getUid());
                                refGroup.child(DATABASE_SECTION_USERS_ORDER).addListenerForSingleValueEvent(requestDataPrivateListener[0]);
                                return;
                            }

                            TreeMap<String, String> map = new TreeMap<>();
                            map.putAll((HashMap<String, String>) dataSnapshot.getValue());

                            for(Map.Entry<String,String> x: map.entrySet()) {
                                if(user.getUid().equals(x.getValue())){
                                    found = true;
                                    break;
                                }
                                ++count;
                            }
                            if(found) {
                                final MyGroup group = new MyGroup();
                                user.number = (int) count;
                                registerUser(groupId, user, request);
                            } else {
                                ref.child(DATABASE_SECTION_GROUPS).child(groupId).setValue(user.getUid());
                                DatabaseReference nodeNumber = ref.child(groupId).child(DATABASE_SECTION_USERS_ORDER).push();
                                nodeNumber.setValue(user.getUid());
                                refGroup.child(DATABASE_SECTION_USERS_ORDER).addListenerForSingleValueEvent(requestDataPrivateListener[0]);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };

                    final ValueEventListener numberForKeyListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null) { //join as existing member, go to check
                                CheckReq check = new CheckReq();
                                check.setControl(Utils.getUnique());
                                check.setGroupId(groupId);
                                check.setUid(dataSnapshot.getKey());
                                check.setNumber((long) dataSnapshot.getValue());
                                check.setUser(conn, request);

                                Common.log("DPF1","onMessage:checkRequest:"+conn.getRemoteSocketAddress(),"{ number:"+dataSnapshot.getValue(), "key:"+dataSnapshot.getKey(), "control:"+check.getControl()+" }");
//                                if (request.has(USER_NAME))
//                                    check.setName(request.getString(USER_NAME));
//
                                response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                                response.put(RESPONSE_CONTROL, check.getControl());
                                ipToCheck.put(ip, check);
                                try {
                                    conn.send(response.toString());
                                } catch(Exception e){
                                    e.printStackTrace();
                                }

//                                long number = (long) dataSnapshot.getValue();
//                                refGroup.child("/users/data-private/" + number+"/control").setValue(check.control);

                            } else { // join as new member

                                refGroup.child(DATABASE_SECTION_USERS_ORDER).addListenerForSingleValueEvent(requestDataPrivateListener[0]);
//                                refGroup.child(DATABASE_SECTION_USERS_DATA_PRIVATE).addListenerForSingleValueEvent(requestDataPrivateListener);

//                                System.out.println("ASNEW:"+dataSnapshot.getValue());
//                                long number = (long) dataSnapshot.getValue();
//                                refGroup.child(DATABASE_SECTION_USERS_DATA_PRIVATE + "/" + number).addListenerForSingleValueEvent(userDataListener);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("SINGLEERR2:"+databaseError.getMessage());

                        }
                    };

                    ValueEventListener groupOptionsListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null) {
                                String deviceId = request.getString(REQUEST_DEVICE_ID);
                                final String uid = Utils.getEncryptedHash(deviceId);
                                refGroup.child(DATABASE_SECTION_USERS_KEYS).child(uid).addListenerForSingleValueEvent(numberForKeyListener);
                            } else {
                                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                                response.put(RESPONSE_MESSAGE, "This group is expired.");
                                conn.send(response.toString());
                                conn.close();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("FAIL5");
                        }
                    };

                    if (request.has(REQUEST_DEVICE_ID)) {
                        refGroup.child(DATABASE_SECTION_OPTIONS).addListenerForSingleValueEvent(groupOptionsListener);
                    } else {
                        CheckReq check = new CheckReq();
                        check.setControl(Utils.getUnique());
                        check.setGroupId(groupId);

                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                        response.put(RESPONSE_CONTROL, check.getControl());
                        ipToCheck.put(ip, check);
                        conn.send(response.toString());
                    }
                    return;
                } else {
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Wrong request (group not defined).");
                    disconnect = true;
                }
                conn.send(response.toString());
                System.out.println("JOIN:response:" + response);
            } else if (REQUEST_CHECK_USER.equals(req)) {
                if (request.has(REQUEST_HASH)) {
                    final String hash = request.getString((REQUEST_HASH));
                    Common.log("DPF1","onMessage:checkResponse:"+conn.getRemoteSocketAddress(),"hash:"+hash);
                    if (ipToCheck.containsKey(ip)) {
                        final CheckReq check = ipToCheck.get(ip);
                        ipToCheck.remove(ip);

                        Common.log("DPF1","onMessage:checkFound:"+conn.getRemoteSocketAddress(),"{ name:"+check.getName(), "group:"+check.getGroupId(), "control:"+check.getControl() +" }");

                        final DatabaseReference refGroup = ref.child(check.getGroupId());

                        final ValueEventListener userCheckListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null) { //join as existing member
                                    try{
                                        String calculatedHash = Utils.getEncryptedHash(check.getControl() + ":" + ((HashMap) dataSnapshot.getValue()).get("device_id"));

                                        if(calculatedHash.equals(hash)) {
                                            Common.log("DPF1", "onMessage:joinAsExisting:"+conn.getRemoteSocketAddress(),"group:"+check.getGroupId(),"user:{ number:"+dataSnapshot.getKey(), "properties:"+dataSnapshot.getValue()," }");

                                            FirebaseAuth.getInstance().createCustomToken(check.getUid()).addOnSuccessListener(new OnSuccessListener<String>() {
                                                @Override
                                                public void onSuccess(final String customToken) {

                                                    Map<String,Object> update = new HashMap<>();
                                                    update.put(DATABASE_USER_ACTIVE, true);
                                                    update.put(DATABASE_USER_COLOR,Utils.selectColor((int) check.getNumber()));
                                                    update.put(DATABASE_USER_CHANGED, new Date().getTime());
                                                    if (check.getName() != null && check.getName().length() > 0) {
                                                        update.put(USER_NAME,check.getName());
                                                    }

                                                    refGroup.child(DATABASE_SECTION_USERS_DATA).child(""+check.getNumber()).updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                                                        response.put(RESPONSE_NUMBER, check.getNumber());
                                                        response.put(RESPONSE_SIGN, customToken);
                                                        conn.send(response.toString());
                                                        conn.close();
                                                        Common.log("DPF1", "onMessage:joined:"+conn.getRemoteSocketAddress(),"signToken: [provided]"/*+customToken*/);
                                                        }
                                                    });

                                                    // Send token back to client
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    System.out.println("FAIL7:"+e.getMessage());
                                                }
                                            });

                                        } else {
                                            Common.log("DPF1", "onMessage:joinNotAuthenticated:"+conn.getRemoteSocketAddress(),"group:"+check.getGroupId(),"{ number:"+dataSnapshot.getKey(), "properties:"+dataSnapshot.getValue(),"}");
                                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                                            response.put(RESPONSE_MESSAGE, "Cannot join to group (user not authenticated).");
                                            conn.send(response.toString());
                                        }

                                    } catch(Exception e) {
                                        Common.log("DPF1", "onMessage:joinHashFailed:"+conn.getRemoteSocketAddress(),"group:"+check.getGroupId(),"{ number:"+dataSnapshot.getKey(), "properties:"+dataSnapshot.getValue(),"}");
                                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                                        response.put(RESPONSE_MESSAGE, "Cannot join to group (user not authenticated).");
                                        conn.send(response.toString());
                                        e.printStackTrace();
                                    }

                                } else { // join as new member

                                    check.getUser().setNumber((int) check.getNumber());
                                    registerUser(check.getGroupId(), check.getUser(), request);
                                    Common.log("DPF1", "onMessage:joinAsNew:"+check.getUser().connection.getRemoteSocketAddress());
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                System.out.println("SINGLEERR2:"+databaseError.getMessage());

                            }
                        };

                        ValueEventListener groupOptionsListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null) {
                                    refGroup.child(DATABASE_SECTION_USERS_DATA_PRIVATE).child(""+check.getNumber()).addListenerForSingleValueEvent(userCheckListener);
                                } else {
                                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                                    response.put(RESPONSE_MESSAGE, "This group is expired.");
                                    conn.send(response.toString());
                                    conn.close();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                System.out.println("FAIL6");
                            }
                        };

                        refGroup.child(DATABASE_SECTION_OPTIONS).addListenerForSingleValueEvent(groupOptionsListener);


                        return;
                    } else {
                        Common.log("DPF1", "onMessage:joinNotAuthorized:"+conn.getRemoteSocketAddress());
                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                        response.put(RESPONSE_MESSAGE, "Cannot join to group (user not authorized).");
                        disconnect = true;
                    }
                } else {
                    Common.log("DPF1", "onMessage:joinNotDefined:"+conn.getRemoteSocketAddress());
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Cannot join to group (hash not defined).");
                    disconnect = true;
                }

                System.out.println("CHECK:response:" + response);
                conn.send(response.toString());
            }
        } catch (Exception e) {
            Common.log("DPF1", "onMessage:error:"+e.getMessage(),"req:"+message);
//            e.printStackTrace();
            conn.send("{\"status\":\"Request failed\"}");
        }
        if(disconnect) {
            conn.close();
        }
    }

    private void registerUser(final String groupId, final MyUser user, final JSONObject request) {
        final JSONObject response = new JSONObject();

        user.setColor(Utils.selectColor(user.getNumber()));
        if(request.has(REQUEST_MANUFACTURER)) user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
        if(request.has(REQUEST_MODEL)) user.setModel(request.getString(REQUEST_MODEL));
        if(request.has(REQUEST_OS)) user.setOs(request.getString(REQUEST_OS));
        if(request.has(USER_NAME)) user.setName(request.getString(USER_NAME));

        final String uid = Utils.getEncryptedHash(user.getDeviceId());

        final Map<String, Object> childUpdates = new HashMap<>();

        Map<String,Object> o = new HashMap<>();
        o.put(DATABASE_USER_COLOR, user.getColor());
        o.put(DATABASE_USER_NAME, user.getName());
        o.put(DATABASE_USER_ACTIVE, true);
        o.put(DATABASE_USER_CREATED, user.getCreated());
        o.put(DATABASE_USER_CHANGED, ServerValue.TIMESTAMP);
        childUpdates.put(DATABASE_SECTION_USERS_DATA + "/" + user.getNumber(),o);

        o = new HashMap<>();

//                    o.put(RESPONSE_CONTROL,user.getControl());
        o.put(REQUEST_MODEL,user.getModel());
        o.put(REQUEST_DEVICE_ID,user.getDeviceId());
        o.put(REQUEST_OS,user.getOs());
        o.put(REQUEST_KEY,uid);
        childUpdates.put(DATABASE_SECTION_USERS_DATA_PRIVATE + "/" + user.getNumber(),o);

        for(Map.Entry<String,RequestHolder> entry: requestHolders.entrySet()) {
            if(entry.getValue().isSaveable()) {
                childUpdates.put(DATABASE_SECTION_PUBLIC +"/" + entry.getKey() + "/"+user.getNumber(), "{}");
            }
        }

        childUpdates.put(DATABASE_SECTION_USERS_KEYS + "/"+uid,user.getNumber());

        Task<Void> a = ref.child(groupId).updateChildren(childUpdates);
        a.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                System.out.println("SUCCESS REGISTERING USER, IDs:"+groupId+":"+uid);
                FirebaseAuth.getInstance().createCustomToken(uid).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String customToken) {
                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                        if(!REQUEST_JOIN_GROUP.equals(request.getString(REQUEST)) && !REQUEST_CHECK_USER.equals(request.getString(REQUEST))) {
                            response.put(RESPONSE_TOKEN, groupId);
                        }
                        response.put(RESPONSE_NUMBER, user.getNumber());
                        response.put(RESPONSE_SIGN, customToken);
                        user.connection.send(response.toString());
                        user.connection.close();
                        // Send token back to client
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("FAIL4:"+e.getMessage());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("FAIL3");
            }
        });

    }

//    @Override
//    public void onWebsocketPong(WebSocket conn, Framedata f) {
//        super.onWebsocketPong(conn, f);
//        System.out.println("PONG:"+conn.getRemoteSocketAddress()+":"+f);
//    }

//    @Override
//    public void onWebsocketPing(WebSocket conn, Framedata f) {
//        super.onWebsocketPing(conn, f);
//
//        try {
//            String ip = conn.getRemoteSocketAddress().toString();
//            if (ipToUser.containsKey(ip)) {
//                ipToUser.get(ip).setChanged();
//            }
////            System.out.println("PING:" + conn.getRemoteSocketAddress() + ":" + f);
//        } catch ( Exception e) {
//            e.printStackTrace();
//        }
//    }

    /*public void sendToAll(String text, WebSocket insteadConnection) {
        Collection<WebSocket> con = connections();
//        synchronized (con) {
        for (WebSocket c : con) {
            if (insteadConnection != null && c == insteadConnection) continue;
            System.out.println("WSS:to:" + c.getRemoteSocketAddress() + ":" + text);
            c.send(text);
        }
//        }
    }*/

    public void removeUser(String groupId,String id){
        if(groupId != null && id != null && groups.containsKey(groupId)){
            MyGroup t = groups.get(groupId);
            MyUser user = t.users.get(id);
            if(user != null){
                JSONObject response = new JSONObject();
                response.put(RESPONSE_STATUS, USER_DISMISSED);
                response.put(RESPONSE_MESSAGE,"You have been dismissed.");
                user.send(response);
                user.disconnect();
                t.removeUser(id);
            }
        }
    }

    public void validateGroups() {
        if(SENSITIVE.isDebugMode()) return;
        Common.log("WPF","Groups validation scheduled is performing, checking online users");
        ChildEventListener groupsList = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String group = dataSnapshot.getKey();
                final String leader = dataSnapshot.getValue().toString();

                ValueEventListener groupValidation = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map value = (Map) dataSnapshot.getValue();

                        Common.log("WPF","Group:", group + ", leader id:", leader, dataSnapshot.getValue());

                        if(value == null) {
                            Common.log("WPF","--- corrupted group detected, removing"); //TODO
                            ref.child(DATABASE_SECTION_GROUPS).child(group).removeValue();
                            ref.child(group).removeValue();
                            return;
                        }

                        final boolean requiresPassword;
                        final boolean dismissInactive;
                        final boolean persistent;
                        final long delayToDismiss;
                        final long timeToLiveIfEmpty;


                        Object object = value.get(DATABASE_OPTION_REQUIRES_PASSWORD);
                        if(object != null) requiresPassword = (boolean) object;
                        else requiresPassword = false;

                        object = value.get(DATABASE_OPTION_DISMISS_INACTIVE);
                        if(object != null) dismissInactive = (boolean) object;
                        else dismissInactive = false;

                        object = value.get(DATABASE_OPTION_PERSISTENT);
                        if(object != null) persistent = (boolean) object;
                        else persistent = false;

                        object = value.get(DATABASE_OPTION_DELAY_TO_DISMISS);
                        if(object != null) delayToDismiss = Long.parseLong("0"+object.toString());
                        else delayToDismiss = 0;

                        object = value.get(DATABASE_OPTION_TIME_TO_LIVE_IF_EMPTY);
                        if(object != null) timeToLiveIfEmpty = Long.parseLong("0"+object.toString());
                        else timeToLiveIfEmpty = 0;

                        ValueEventListener usersValidation = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Common.log("WPF","Users validation for", group);

                                ArrayList<Map> users = null;
//                                try {
                                users = (ArrayList<Map>) dataSnapshot.getValue();
//                                } catch(Exception e) {
//                                    e.printStackTrace();
//                                }
                                if(users == null) {
                                    Common.log("WPF","--- corrupted group detected, removing ----- 2"); //TODO
                                    Common.log(ref.child(DATABASE_SECTION_GROUPS).child(group), ref.child(group));
                                    return;
                                }
                                long groupChanged = 0;

                                for(int i = 0; i < users.size(); i++) {
                                    Map user = users.get(i);
                                    if(user == null) continue;

                                    String name = (String) user.get(DATABASE_USER_NAME);
                                    Long changed = (Long) user.get(DATABASE_USER_CHANGED);
                                    if(changed != null && changed > groupChanged) groupChanged = changed;
                                    boolean active = false;
                                    Object object = user.get(DATABASE_USER_ACTIVE);
                                    if(object != null) {
                                        active = (Boolean) object;
                                    }

                                    if(!active) continue;

                                    if(dismissInactive) {

                                        Long current = new Date().getTime();
                                        if (changed == null) {
                                            Common.log("WPF", "--- user:", i, "name:", name, "is NULL");
//                                            dataSnapshot.getRef().child(""+i).child(DATABASE_USER_ACTIVE).setValue(false);
                                        } else if (current - delayToDismiss * 1000 > changed) {
                                            Common.log("WPF", "--- user:", i, "name:", name, "is EXPIRED for", ((current - delayToDismiss * 1000 - changed) / 1000), "seconds");
//                                            dataSnapshot.getRef().child(""+i).child(DATABASE_USER_ACTIVE).setValue(false);
                                        } else {
                                            dataSnapshot.getRef().getParent().getParent().child(DATABASE_SECTION_OPTIONS).child(DATABASE_OPTION_DATE_CHANGED).setValue(changed);
                                            Common.log("WPF", "--- user:", i, "name:", name, "is OK");
                                        }
                                    }
                                }

                                if(!persistent && timeToLiveIfEmpty > 0 && new Date().getTime() - groupChanged > timeToLiveIfEmpty * 60 * 1000 ) {
//TODO
                                    Common.log("WPF","--- removing expired group "+group+" for:", (new Date().getTime() - groupChanged - timeToLiveIfEmpty * 60 * 1000)/1000/60, "minutes");
                                    Common.log(ref.child(DATABASE_SECTION_GROUPS).child(group), ref.child(group));

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };

                        ref.child(group).child(DATABASE_SECTION_USERS_DATA).removeEventListener(usersValidation);
                        ref.child(group).child(DATABASE_SECTION_USERS_DATA).addListenerForSingleValueEvent(usersValidation);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };


                ref.child(group).child(DATABASE_SECTION_OPTIONS).removeEventListener(groupValidation);
                ref.child(group).child(DATABASE_SECTION_OPTIONS).addListenerForSingleValueEvent(groupValidation);

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
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ref.child(DATABASE_SECTION_GROUPS).removeEventListener(groupsList);
        ref.child(DATABASE_SECTION_GROUPS).addChildEventListener(groupsList);

/*        while(true) {
            try {
                Thread.sleep(LIFETIME_INACTIVE_USER * 1000);

                MyUser user;
                long currentDate = new Date().getTime();
                for (Map.Entry<String, MyUser> entry : ipToUser.entrySet()) {

                    user = entry.getValue();
                    if (user != null) {

                        System.out.println("INACTIVITY: " + user.getName() + ":" + (currentDate - user.getChanged()));

                        if (currentDate - user.getChanged() > LIFETIME_INACTIVE_USER * 1000) {
                            // dismiss user
                            JSONObject o = new JSONObject();
                            if(ipToToken.containsKey(entry.getKey())) {
                                MyGroup token = ipToToken.get(entry.getKey());
                                o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                                o.put(USER_DISMISSED, user.getNumber());
                                token.sendToAllFrom(o, user);
                            }

                            if(ipToUser.containsKey(entry.getKey())) ipToUser.remove(entry.getKey());
                            if(ipToToken.containsKey(entry.getKey())) ipToToken.remove(entry.getKey());
                            if(ipToCheck.containsKey(entry.getKey())) ipToCheck.remove(entry.getKey());
                            user.connection.close();
                        }

                    } else {
                        if (ipToToken.containsKey(entry.getKey())) ipToToken.remove(entry.getKey());
                        if (ipToCheck.containsKey(entry.getKey())) ipToCheck.remove(entry.getKey());
                        ipToUser.remove(entry.getKey());
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    @Override
    public void validateUsers() {

    }

}
