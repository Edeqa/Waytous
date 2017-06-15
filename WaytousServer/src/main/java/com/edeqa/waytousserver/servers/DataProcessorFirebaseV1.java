package com.edeqa.waytousserver.servers;

import com.edeqa.waytousserver.helpers.CheckReq;
import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.MyUser;
import com.edeqa.waytousserver.helpers.TaskSingleValueEventFor;
import com.edeqa.waytousserver.helpers.Utils;
import com.edeqa.waytousserver.interfaces.Callable1;
import com.edeqa.waytousserver.interfaces.DataProcessorConnection;
import com.edeqa.waytousserver.interfaces.RequestHolder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;

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
import static com.edeqa.waytousserver.helpers.Constants.USER_NAME;


/**
 * Created 10/5/16.
 */

@SuppressWarnings("HardCodedStringLiteral")
public class DataProcessorFirebaseV1 extends AbstractDataProcessor {

    public static String VERSION = "v1";
    private static String LOG = "DPF1";
    private DatabaseReference ref;

    public DataProcessorFirebaseV1() throws ServletException, IOException {
        super();

        try {
            Common.log(LOG,"Data Processor Firebase "+VERSION+", config file: "+new File(SENSITIVE.getFirebasePrivateKeyFile()).getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        FirebaseOptions options = createFirebaseOptions();

        try {
            FirebaseApp.getInstance();
        } catch (Exception e){
//            Common.log("doesn't exist...");
//            e.printStackTrace();
        }

        try {
//            if(FirebaseApp.getApps().size() < 1) {
            FirebaseApp.initializeApp(options);
//            }
        } catch(Exception e){
//            Common.log("already exists...");
//            e.printStackTrace();
        }
        try {
            ref = FirebaseDatabase.getInstance().getReference();
        }catch (Exception e) {
            e.printStackTrace();
        }
//        throw new ServletException("SENSITIVE:"+FirebaseDatabase.getInstance());

    }

    /**
     * This method creates the options for Firebase connecting. Depending on current installation type
     * it defines the properly request and performs it. Installation type can be defined in gradle.build.
     *
     * Current installation type is recognizing by presense of method:
     * - "setCredential" in stand-alone server mode,
     * - "setServiceAccount" in Google AppEngine mode.
     *
     * Stand-alone server mode extends com.sun.net.httpserver.HttpServer.
     */
    private FirebaseOptions createFirebaseOptions() throws FileNotFoundException {

        FirebaseOptions.Builder builder = new FirebaseOptions.Builder();

        Class<? extends FirebaseOptions.Builder> builderClass = builder.getClass();
        Method[] methods = builderClass.getDeclaredMethods();
        Method method = null;
        for(Method m:methods) {
            if("setCredential".equals(m.getName())) {
                method = m;
                setServerMode(true);
                break;
            } else if("setServiceAccount".equals(m.getName())) {
                method = m;
//                break;
            }

        }
        if(isServerMode()) {
            try {
                Class tempClass = Class.forName("com.google.firebase.auth.FirebaseCredentials");
                Method fromCertificate = tempClass.getDeclaredMethod("fromCertificate", InputStream.class);

                assert method != null;
                builder = (FirebaseOptions.Builder) method.invoke(builder, fromCertificate.invoke(null, new FileInputStream(SENSITIVE.getFirebasePrivateKeyFile())));
//                builder = (FirebaseOptions.Builder) method.invoke(builder, FirebaseCredentials.fromCertificate(new FileInputStream(SENSITIVE.getFirebasePrivateKeyFile())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                assert method != null;
                builder = (FirebaseOptions.Builder) method.invoke(builder, new FileInputStream(SENSITIVE.getFirebasePrivateKeyFile()));
            } catch (IllegalAccessException | InvocationTargetException | IOException e) {
                e.printStackTrace();
            }
        }

//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredential(com.google.firebase.auth.FirebaseCredentials.fromCertificate(new FileInputStream(SENSITIVE.getFirebasePrivateKeyFile())))
//                .setDatabaseUrl(SENSITIVE.getFirebaseDatabaseUrl())
//                .build();

//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setServiceAccount(new FileInputStream(SENSITIVE.getFirebasePrivateKeyFile()))
//                .setDatabaseUrl(SENSITIVE.getFirebaseDatabaseUrl())
//                .build();
        return builder.setDatabaseUrl(SENSITIVE.getFirebaseDatabaseUrl()).build();
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
    public void onMessage(final DataProcessorConnection conn, String message) {

        boolean disconnect = false;
        try {
            final String ip = conn.getRemoteSocketAddress().toString();
            final JSONObject request, response = new JSONObject();

            try {
                request = new JSONObject(message);
            } catch(JSONException e) {
                Common.err(LOG,"onMessage:request"+e.getMessage());
                return;
            }
            if (!request.has(REQUEST) || !request.has(REQUEST_TIMESTAMP)) return;

            String req = request.getString(REQUEST);
            if (REQUEST_NEW_GROUP.equals(req)) {
                if (request.has(REQUEST_DEVICE_ID)) {
                    final MyGroup group = new MyGroup();
                    final MyUser user = new MyUser(conn, request.getString(REQUEST_DEVICE_ID));
                    //noinspection unchecked
                    final Callable1<JSONObject>[] onresult = new Callable1[2];
                    onresult[0] = new Callable1<JSONObject>() {
                        @Override
                        public void call(JSONObject json) {
                            ref.child(Constants.DATABASE.SECTION_GROUPS).child(group.getId()).setValue(user.getUid());
                            DatabaseReference nodeNumber = ref.child(group.getId()).child(Constants.DATABASE.SECTION_USERS_ORDER).push();
                            nodeNumber.setValue(user.getUid());

                            registerUser(group.getId(), user, request);
                        }
                    };
                    onresult[1] = new Callable1<JSONObject>() {
                        @Override
                        public void call(JSONObject json) {
                            group.fetchNewId();
                            createGroup(group, onresult[0], onresult[1]);
                        }
                    };
                    createGroup(group, onresult[0], onresult[1]);
                } else {
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Cannot create group (code 15).");
                    conn.send(response.toString());
                    conn.close();
                    Common.err(LOG,"onMessage:newGroup:",response);
                }
            } else if (REQUEST_JOIN_GROUP.equals(req)) {
                if (request.has(REQUEST_TOKEN)) {

                    final String groupId = request.getString(REQUEST_TOKEN);
                    final DatabaseReference refGroup = ref.child(groupId);

                    final TaskSingleValueEventFor[] requestDataPrivateTask = new TaskSingleValueEventFor[1];
                    requestDataPrivateTask[0] = new TaskSingleValueEventFor().addOnCompleteListener(new Callable1<DataSnapshot>() {
                        @Override
                        public void call(DataSnapshot dataSnapshot) {
                            final MyUser user = new MyUser(conn, request.getString(REQUEST_DEVICE_ID));

                            int count = 1;
                            boolean found = false;
                            Object value = dataSnapshot.getValue();
                            if(value == null) {
                                dataSnapshot.getRef().push().setValue(user.getUid());
                                requestDataPrivateTask[0].setRef(refGroup.child(Constants.DATABASE.SECTION_USERS_ORDER)).start();
                                return;
                            }

                            TreeMap<String, String> map = new TreeMap<>();
                            //noinspection unchecked
                            map.putAll((HashMap<String, String>) dataSnapshot.getValue());

                            for(Map.Entry<String,String> x: map.entrySet()) {
                                if(user.getUid().equals(x.getValue())){
                                    found = true;
                                    break;
                                }
                                ++count;
                            }
                            if(found) {
//                                final MyGroup group = new MyGroup();
                                user.number = count;
                                registerUser(groupId, user, request);
                            } else {
                                ref.child(Constants.DATABASE.SECTION_GROUPS).child(groupId).setValue(user.getUid());
                                DatabaseReference nodeNumber = ref.child(groupId).child(Constants.DATABASE.SECTION_USERS_ORDER).push();
                                nodeNumber.setValue(user.getUid());
                                requestDataPrivateTask[0].setRef(refGroup.child(Constants.DATABASE.SECTION_USERS_ORDER)).start();
                            }
                        }
                    });

                    final TaskSingleValueEventFor numberForKeyTask = new TaskSingleValueEventFor()
                            .addOnCompleteListener(new Callable1<DataSnapshot>() {
                                @Override
                                public void call(DataSnapshot dataSnapshot) {
                                    System.out.println("DG:" + dataSnapshot.getKey() + ":" + dataSnapshot.getValue());
                                    if(dataSnapshot.getValue() != null) { //join as existing member, go to check
                                        CheckReq check = new CheckReq();
                                        check.setControl(Utils.getUnique());
                                        check.setGroupId(groupId);
                                        check.setUid(dataSnapshot.getKey());
                                        check.setNumber((long) dataSnapshot.getValue());
                                        check.setUser(conn, request);

                                        Common.log(LOG,"onMessage:checkRequest:"+conn.getRemoteSocketAddress(),"{ number:"+dataSnapshot.getValue(), "key:"+dataSnapshot.getKey(), "control:"+check.getControl()+" }");

                                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                                        response.put(RESPONSE_CONTROL, check.getControl());
                                        ipToCheck.put(ip, check);
                                        try {
                                            conn.send(response.toString());
                                        } catch(Exception e){
                                            e.printStackTrace();
                                        }
                                    } else { // join as new member
                                        requestDataPrivateTask[0].setRef(refGroup.child(Constants.DATABASE.SECTION_USERS_ORDER)).start();
                                    }
                                }
                            });

                    TaskSingleValueEventFor groupOptionsTask = new TaskSingleValueEventFor()
                            .addOnCompleteListener(new Callable1<DataSnapshot>() {
                                @Override
                                public void call(DataSnapshot dataSnapshot) {
                                    System.out.println("DF:" + dataSnapshot.getKey() + ":" + dataSnapshot.getValue());
                                    if (dataSnapshot.getValue() != null) {
                                        String deviceId = request.getString(REQUEST_DEVICE_ID);
                                        final String uid = Utils.getEncryptedHash(deviceId);

                                        numberForKeyTask.setRef(refGroup.child(Constants.DATABASE.SECTION_USERS_KEYS).child(uid)).start();
                                    } else {
                                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                                        response.put(RESPONSE_MESSAGE, "This group is expired.");
                                        conn.send(response.toString());
                                        conn.close();
                                    }
                                }
                            });

                    if (request.has(REQUEST_DEVICE_ID)) {
                        groupOptionsTask.setRef(refGroup.child(Constants.DATABASE.SECTION_OPTIONS)).start();
                    } else {
                        System.out.println("B");
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
                    Common.log(LOG,"onMessage:checkResponse:"+conn.getRemoteSocketAddress(),"hash:"+hash);
                    if (ipToCheck.containsKey(ip)) {
                        final CheckReq check = ipToCheck.get(ip);
                        ipToCheck.remove(ip);

                        Common.log(LOG,"onMessage:checkFound:"+conn.getRemoteSocketAddress(),"{ name:"+check.getName(), "group:"+check.getGroupId(), "control:"+check.getControl() +" }");

                        final DatabaseReference refGroup = ref.child(check.getGroupId());

                        final TaskSingleValueEventFor userCheckTask = new TaskSingleValueEventFor().addOnCompleteListener(new Callable1<DataSnapshot>() {
                            @Override
                            public void call(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null) { //join as existing member
                                    try{
                                        String calculatedHash = Utils.getEncryptedHash(check.getControl() + ":" + ((HashMap) dataSnapshot.getValue()).get("device_id"));

                                        if(calculatedHash.equals(hash)) {
                                            Common.log(LOG, "onMessage:joinAsExisting:"+conn.getRemoteSocketAddress(),"group:"+check.getGroupId(),"user:{ number:"+dataSnapshot.getKey(), "properties:"+dataSnapshot.getValue()," }");

                                            try {
                                                String customToken = createCustomToken(check.getUid());
                                                System.out.println("TOKENGET:"+customToken);

                                                Map<String,Object> update = new HashMap<>();
                                                update.put(Constants.DATABASE.USER_ACTIVE, true);
                                                update.put(Constants.DATABASE.USER_COLOR,Utils.selectColor((int) check.getNumber()));
                                                update.put(Constants.DATABASE.USER_CHANGED, new Date().getTime());
                                                if (check.getName() != null && check.getName().length() > 0) {
                                                    update.put(USER_NAME,check.getName());
                                                }

                                                Task<Void> updateUserTask = refGroup.child(Constants.DATABASE.SECTION_USERS_DATA).child(""+check.getNumber()).updateChildren(update);
                                                try {
                                                    Tasks.await(updateUserTask);
                                                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                                                    response.put(RESPONSE_NUMBER, check.getNumber());
                                                    response.put(RESPONSE_SIGN, customToken);
                                                    conn.send(response.toString());
                                                    conn.close();
                                                    Common.log(LOG, "onMessage:joined:"+conn.getRemoteSocketAddress(),"signToken: [provided]"/*+customToken*/);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            Common.log(LOG, "onMessage:joinNotAuthenticated:"+conn.getRemoteSocketAddress(),"group:"+check.getGroupId(),"{ number:"+dataSnapshot.getKey(), "properties:"+dataSnapshot.getValue(),"}");
                                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                                            response.put(RESPONSE_MESSAGE, "Cannot join to group (user not authenticated).");
                                            conn.send(response.toString());
                                        }

                                    } catch(Exception e) {
                                        Common.log(LOG, "onMessage:joinHashFailed:"+conn.getRemoteSocketAddress(),"group:"+check.getGroupId(),"{ number:"+dataSnapshot.getKey(), "properties:"+dataSnapshot.getValue(),"}");
                                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                                        response.put(RESPONSE_MESSAGE, "Cannot join to group (user not authenticated).");
                                        conn.send(response.toString());
                                        e.printStackTrace();
                                    }

                                } else { // join as new member

                                    check.getUser().setNumber((int) check.getNumber());
                                    registerUser(check.getGroupId(), check.getUser(), request);
                                    Common.log(LOG, "onMessage:joinAsNew:"+check.getUser().connection.getRemoteSocketAddress());
                                }
                            }
                        });

                        TaskSingleValueEventFor groupOptionsTask = new TaskSingleValueEventFor()
                                .addOnCompleteListener(new Callable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue() != null) {
                                            userCheckTask.setRef(refGroup.child(Constants.DATABASE.SECTION_USERS_DATA_PRIVATE).child(""+check.getNumber())).start();
                                        } else {
                                            System.out.println("D");
                                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                                            response.put(RESPONSE_MESSAGE, "This group is expired.");
                                            conn.send(response.toString());
                                            conn.close();
                                        }
                                    }
                                });

                        groupOptionsTask.setRef(refGroup.child(Constants.DATABASE.SECTION_OPTIONS)).start();
                        return;
                    } else {
                        Common.log(LOG, "onMessage:joinNotAuthorized:"+conn.getRemoteSocketAddress());
                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                        response.put(RESPONSE_MESSAGE, "Cannot join to group (user not authorized).");
                        disconnect = true;
                    }
                } else {
                    Common.log(LOG, "onMessage:joinNotDefined:"+conn.getRemoteSocketAddress());
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Cannot join to group (hash not defined).");
                    disconnect = true;
                }

                System.out.println("CHECK:response:" + response);
                conn.send(response.toString());
            }
        } catch (Exception e) {
            Common.log(LOG, "onMessage:error:"+e.getMessage(),"req:"+message);
//            e.printStackTrace();
            conn.send("{\"status\":\"Request failed\"}");
        }
        if(disconnect) {
            conn.close();
        }
    }

    @Override
    public void createGroup(final MyGroup group, final Callable1<JSONObject> onsuccess, final Callable1<JSONObject> onerror) {

        final JSONObject json = new JSONObject();

        Common.log(LOG,"New group ID:",group.getId());

        new TaskSingleValueEventFor(ref.child(Constants.DATABASE.SECTION_GROUPS).child(group.getId()))
                .addOnCompleteListener(new Callable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue() == null) {
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put(Constants.DATABASE.SECTION_OPTIONS + "/"
                                    + Constants.DATABASE.OPTION_WELCOME_MESSAGE, group.getWelcomeMessage());
                            childUpdates.put(Constants.DATABASE.SECTION_OPTIONS + "/"
                                    + Constants.DATABASE.OPTION_REQUIRES_PASSWORD, group.isRequirePassword());
                            childUpdates.put(Constants.DATABASE.SECTION_OPTIONS + "/"
                                    + Constants.DATABASE.OPTION_TIME_TO_LIVE_IF_EMPTY, group.getTimeToLiveIfEmpty());
                            childUpdates.put(Constants.DATABASE.SECTION_OPTIONS + "/"
                                    + Constants.DATABASE.OPTION_PERSISTENT, group.isPersistent());
                            childUpdates.put(Constants.DATABASE.SECTION_OPTIONS + "/"
                                    + Constants.DATABASE.OPTION_DISMISS_INACTIVE, group.isDismissInactive());
                            childUpdates.put(Constants.DATABASE.SECTION_OPTIONS + "/"
                                    + Constants.DATABASE.OPTION_DELAY_TO_DISMISS, group.getDelayToDismiss());
                            childUpdates.put(Constants.DATABASE.SECTION_OPTIONS + "/"
                                    + Constants.DATABASE.OPTION_DATE_CREATED, ServerValue.TIMESTAMP);
                            childUpdates.put(Constants.DATABASE.SECTION_OPTIONS + "/"
                                    + Constants.DATABASE.OPTION_DATE_CHANGED, ServerValue.TIMESTAMP);
                            ref.child(group.getId()).updateChildren(childUpdates);
                            ref.child(Constants.DATABASE.SECTION_GROUPS).child(group.getId()).setValue(0);

                            json.put(Constants.REST.STATUS, Constants.REST.SUCCESS);
                            json.put(Constants.REST.GROUP_ID, group.getId());

                            Common.log(LOG, "onMessage:createGroup:created:" + group.getId());
                            onsuccess.call(json);

                        } else {
                            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
                            json.put(Constants.REST.GROUP_ID, group.getId());
                            json.put(Constants.REST.MESSAGE, "Group " + group.getId() + " already exists.");
                            Common.log(LOG, "onMessage:createGroup:alreadyExists:" + group.getId());
                            if(onerror != null) onerror.call(json);
                        }
                    }
                }).start();

    }

    @Override
    public void deleteGroup(final String groupId, final Callable1<JSONObject> onsuccess, final Callable1<JSONObject> onerror) {
        final JSONObject json = new JSONObject();

        json.put(Constants.REST.GROUP_ID, groupId);

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                json.put(Constants.REST.STATUS, Constants.REST.ERROR);
                json.put(Constants.REST.MESSAGE, e.getMessage());
                Common.log(LOG, "deleteGroup:" + groupId, "error:"+e.getMessage());
                onerror.call(json);
            }
        };

        Task<Void> deleteGroupTask = ref.child(Constants.DATABASE.SECTION_GROUPS).child(groupId).removeValue();
        try {
            Tasks.await(deleteGroupTask);

            Task<Void> deleteGroupIdTask = ref.child(groupId).removeValue();
            Tasks.await(deleteGroupIdTask);
            json.put(Constants.REST.STATUS, Constants.REST.SUCCESS);
            Common.log(LOG, "deleteGroup:" + groupId);
            onsuccess.call(json);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            onFailureListener.onFailure(e);
        }
    }

    @Override
    public void switchPropertyInGroup(final String groupId, final String property, final Callable1<JSONObject> onsuccess, final Callable1<JSONObject> onerror) {

        final JSONObject res = new JSONObject();
        res.put(Constants.REST.PROPERTY, property);

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.put(Constants.REST.STATUS, Constants.REST.ERROR);
                res.put(Constants.REST.MESSAGE, e.getMessage());
                Common.log(LOG, "switchPropertyInGroup:", property, e.getMessage());
                onerror.call(res);
            }
        };

        new TaskSingleValueEventFor(ref.child(groupId).child(Constants.DATABASE.SECTION_OPTIONS).child(property))
                .addOnCompleteListener(new Callable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        Boolean value = (Boolean) dataSnapshot.getValue();
                        if(value != null) {
                            res.put(Constants.REST.OLD_VALUE, value);
                            value = !value;
                            ref.child(groupId).child(Constants.DATABASE.SECTION_OPTIONS).child(property).setValue(value).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    res.put(Constants.REST.STATUS, Constants.REST.SUCCESS);
                                    onsuccess.call(res);
                                }
                            }).addOnFailureListener(onFailureListener);
                        } else {
                            onFailureListener.onFailure(new Exception("Null value."));
                        }
                    }
                }).start();

    }

    @Override
    public void modifyPropertyInGroup(final String groupId, final String property, final Serializable value, final Callable1<JSONObject> onsuccess, final Callable1<JSONObject> onerror) {

        final JSONObject res = new JSONObject();
        res.put(Constants.REST.PROPERTY, property);
        res.put(Constants.REST.VALUE, value);

        new TaskSingleValueEventFor(ref.child(groupId).child(Constants.DATABASE.SECTION_OPTIONS).child(property))
                .addOnCompleteListener(new Callable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        Serializable oldValue = (Serializable) dataSnapshot.getValue();
                        if(oldValue != null && value != null) {
                            res.put(Constants.REST.OLD_VALUE, oldValue);
                            ref.child(groupId).child(Constants.DATABASE.SECTION_OPTIONS).child(property).setValue(value);
                            res.put(Constants.REST.STATUS, Constants.REST.SUCCESS);
                            onsuccess.call(res);
                        } else {
                            Common.log(LOG, "modifyPropertyInGroup:nullValue:", property);
                            res.put(Constants.REST.STATUS, Constants.REST.ERROR);
                            onerror.call(res);
                        }
                    }
                }).start();

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
        o.put(Constants.DATABASE.USER_COLOR, user.getColor());
        o.put(Constants.DATABASE.USER_NAME, user.getName());
        o.put(Constants.DATABASE.USER_ACTIVE, true);
        o.put(Constants.DATABASE.USER_CREATED, user.getCreated());
        o.put(Constants.DATABASE.USER_CHANGED, ServerValue.TIMESTAMP);
        childUpdates.put(Constants.DATABASE.SECTION_USERS_DATA + "/" + user.getNumber(),o);

        o = new HashMap<>();

//                    o.put(RESPONSE_CONTROL,user.getControl());
        o.put(REQUEST_MODEL,user.getModel());
        o.put(REQUEST_DEVICE_ID,user.getDeviceId());
        o.put(REQUEST_OS,user.getOs());
        o.put(REQUEST_KEY,uid);
        childUpdates.put(Constants.DATABASE.SECTION_USERS_DATA_PRIVATE + "/" + user.getNumber(),o);

        for(Map.Entry<String,RequestHolder> entry: requestHolders.entrySet()) {
            if(entry.getValue().isSaveable()) {
                childUpdates.put(Constants.DATABASE.SECTION_PUBLIC +"/" + entry.getKey() + "/"+user.getNumber(), "{}");
            }
        }

        childUpdates.put(Constants.DATABASE.SECTION_USERS_KEYS + "/"+uid,user.getNumber());

        final Task<Void> updateUserTask = ref.child(groupId).updateChildren(childUpdates);
        try {
            Tasks.await(updateUserTask);

            Common.log(LOG, "onMessage:registerUser:"+user.getNumber(),"uid:"+uid,"group:"+groupId);

            String customToken = createCustomToken(uid);
            System.out.println("TOKENGET2:"+customToken);

            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
            if (!REQUEST_JOIN_GROUP.equals(request.getString(REQUEST)) && !REQUEST_CHECK_USER.equals(request.getString(REQUEST))) {
                response.put(RESPONSE_TOKEN, groupId);
            }
            response.put(RESPONSE_NUMBER, user.getNumber());
            response.put(RESPONSE_SIGN, customToken);
            user.connection.send(response.toString());
            user.connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
//                ipToUser.get(ip).updateChanged();
//            }
////            System.out.println("PING:" + conn.getRemoteSocketAddress() + ":" + f);
//        } catch ( Exception e) {
//            e.printStackTrace();
//        }
//    }


    @Override
    public void removeUser(final String groupId, final Long userNumber, final Callable1<JSONObject> onsuccess, final Callable1<JSONObject> onerror) {

        final JSONObject json = new JSONObject();
//        final String user = String.valueOf(userNumber);

        json.put(Constants.REST.GROUP_ID, groupId);
        json.put(Constants.REST.USER_NUMBER, userNumber);

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                json.put(Constants.REST.STATUS, Constants.REST.ERROR);
                json.put(Constants.REST.MESSAGE, e.getMessage());
                Common.log(LOG, "removeUser:" + userNumber, "group:"+groupId, "error:"+e.getMessage());
                onerror.call(json);
            }
        };
        onFailureListener.onFailure(new Exception("Not implemented yet."));

        /*ref.child(groupId).child(DATABASE.SECTION_USERS_DATA_PRIVATE).child(user).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ref.child(groupId).child(DATABASE.SECTION_USERS_DATA).child(user).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        new TaskSingleValueEventFor(ref.child(groupId).child(DATABASE.SECTION_USERS_KEYS))
                                .addOnCompleteListener(new Callable1<DataSnapshot>() {
                                    @Override
                                    public void call(DataSnapshot dataSnapshot) {
                                        HashMap<String,Serializable> val = (HashMap<String, Serializable>) dataSnapshot.getValue();
                                        for(Map.Entry<String,Serializable> x:val.entrySet()) {
                                            System.out.println(userNumber +":"+x.getKey() + ":" + x.getValue() + ":"+x.getValue().getClass()+":"+(x.getValue() == userNumber));
                                            if(x.getValue() == userNumber) {
                                                ref.child(groupId).child(DATABASE.SECTION_USERS_KEYS).child(x.getKey()).removeValue().addOnCompleteListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        json.put(Constants.REST.STATUS, Constants.REST.SUCCESS);
                                                        Common.log(LOG, "removeUser:" + userNumber, "group:"+groupId);
                                                        onsuccess.call(json);
                                                    }
                                                });
                                                return;
                                            }
                                        }
                                        onFailureListener.onFailure(new Exception("User not found."));
                                    }
                                }).start();
                    }
                }).addOnFailureListener(onFailureListener);
            }
        }).addOnFailureListener(onFailureListener);*/
    }

    @Override
    public void switchPropertyForUser(final String groupId, final Long userNumber, final String property, final Boolean value, final Callable1<JSONObject> onsuccess, final Callable1<JSONObject> onerror) {

        final JSONObject res = new JSONObject();
        res.put(Constants.REST.PROPERTY, property);

        final OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                res.put(Constants.REST.STATUS, Constants.REST.ERROR);
                res.put(Constants.REST.MESSAGE, e.getMessage());
                Common.log(LOG, "switchPropertyForUser:", property, e.getMessage());
                onerror.call(res);
            }
        };

        new TaskSingleValueEventFor(ref.child(groupId).child(Constants.DATABASE.SECTION_USERS_DATA).child(String.valueOf(userNumber)).child(property))
                .addOnCompleteListener(new Callable1<DataSnapshot>() {
                    @Override
                    public void call(DataSnapshot dataSnapshot) {
                        Boolean oldValue = (Boolean) dataSnapshot.getValue();
                        if(oldValue != null) {
                            res.put(Constants.REST.OLD_VALUE, oldValue);
                            Boolean newValue = !oldValue;
                            if(value != null) newValue = value;
                            ref.child(groupId).child(Constants.DATABASE.SECTION_USERS_DATA).child(String.valueOf(userNumber)).child(property).setValue(newValue).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    res.put(Constants.REST.STATUS, Constants.REST.SUCCESS);
                                    onsuccess.call(res);
                                }
                            }).addOnFailureListener(onFailureListener);
                        } else {
                            onFailureListener.onFailure(new Exception("Invalid property."));
                        }
                    }
                }).start();
    }

    public void validateGroups() {

        Common.log(LOG, "Groups validation is performing, checking online users");
        try {
            String res = Utils.getUrl("https://waytous-beta.firebaseio.com/.json?shallow=true&print=pretty&auth="+SENSITIVE.getFirebaseWebApiKey(),"UTF-8");

            JSONObject groups = new JSONObject(res);

            Iterator<String> iter = groups.keys();
            while(iter.hasNext()) {
                final String group = iter.next();
                if(Constants.DATABASE.SECTION_GROUPS.equals(group) || "overview".equals(group)) continue;

                new TaskSingleValueEventFor(ref.child(group).child(Constants.DATABASE.SECTION_OPTIONS))
                        .addOnCompleteListener(new Callable1<DataSnapshot>() {
                            @Override
                            public void call(DataSnapshot dataSnapshot) {
                                Map value = (Map) dataSnapshot.getValue();

                                Common.log(LOG, "Group found:", group/* + ", leader id:", leader, dataSnapshot.getValue()*/);

                                if (value == null) {
                                    Common.log(LOG, "--- corrupted group detected, removing ----- 1"); //TODO
                                    ref.child(Constants.DATABASE.SECTION_GROUPS).child(group).removeValue();
                                    ref.child(group).removeValue();
                                    return;
                                }

                                final boolean requiresPassword;
                                final boolean dismissInactive;
                                final boolean persistent;
                                final long delayToDismiss;
                                final long timeToLiveIfEmpty;


                                Object object = value.get(Constants.DATABASE.OPTION_REQUIRES_PASSWORD);
                                requiresPassword = object != null && (boolean) object;

                                object = value.get(Constants.DATABASE.OPTION_DISMISS_INACTIVE);
                                dismissInactive = object != null && (boolean) object;

                                object = value.get(Constants.DATABASE.OPTION_PERSISTENT);
                                persistent = object != null && (boolean) object;

                                object = value.get(Constants.DATABASE.OPTION_DELAY_TO_DISMISS);
                                if (object != null)
                                    delayToDismiss = Long.parseLong("0" + object.toString());
                                else delayToDismiss = 0;

                                object = value.get(Constants.DATABASE.OPTION_TIME_TO_LIVE_IF_EMPTY);
                                if (object != null)
                                    timeToLiveIfEmpty = Long.parseLong("0" + object.toString());
                                else timeToLiveIfEmpty = 0;

                                new TaskSingleValueEventFor(ref.child(group).child(Constants.DATABASE.SECTION_USERS_DATA))
                                        .addOnCompleteListener(new Callable1<DataSnapshot>() {
                                            @Override
                                            public void call(DataSnapshot dataSnapshot) {
                                                Common.log(LOG, "Users validation for group:", group);

                                                ArrayList<Map<String, Serializable>> users = null;
                                                try {
                                                    users = (ArrayList<Map<String, Serializable>>) dataSnapshot.getValue();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                if (users == null) {
                                                    Common.log(LOG, "--- corrupted group detected, removing: ----- 2"); //TODO
                                                    ref.child(Constants.DATABASE.SECTION_GROUPS).child(group).removeValue();
                                                    ref.child(group).removeValue();
                                                    return;
                                                }
                                                long groupChanged = 0;

                                                for (int i = 0; i < users.size(); i++) {
                                                    Map<String, Serializable> user = users.get(i);
                                                    if (user == null) continue;

                                                    String name = (String) user.get(Constants.DATABASE.USER_NAME);
                                                    Long changed = (Long) user.get(Constants.DATABASE.USER_CHANGED);
                                                    if (changed != null && changed > groupChanged)
                                                        groupChanged = changed;
                                                    boolean active = false;
                                                    Object object = user.get(Constants.DATABASE.USER_ACTIVE);
                                                    if (object != null) {
                                                        active = (Boolean) object;
                                                    }

                                                    if (!active) continue;

                                                    if (dismissInactive) {
                                                        Long current = new Date().getTime();
                                                        if (changed == null) {
                                                            Common.log(LOG, "--- user:", i, "name:", name, "is NULL");
                                                            dataSnapshot.getRef().child("" + i).child(Constants.DATABASE.USER_ACTIVE).setValue(false);
                                                        } else if (current - delayToDismiss * 1000 > changed) {
                                                            Common.log(LOG, "--- user:", i, "name:", name, "is EXPIRED for", ((current - delayToDismiss * 1000 - changed) / 1000), "seconds");
                                                            dataSnapshot.getRef().child("" + i).child(Constants.DATABASE.USER_ACTIVE).setValue(false);
                                                        } else {
                                                            dataSnapshot.getRef().getParent().getParent().child(Constants.DATABASE.SECTION_OPTIONS).child(Constants.DATABASE.OPTION_DATE_CHANGED).setValue(changed);
                                                            Common.log(LOG, "--- user:", i, "name:", name, "is OK");
                                                        }
                                                    }
                                                }

                                                if (!persistent && timeToLiveIfEmpty > 0 && new Date().getTime() - groupChanged > timeToLiveIfEmpty * 60 * 1000) {
                                                    Common.log(LOG, "--- removing group " + group + " expired for", (new Date().getTime() - groupChanged - timeToLiveIfEmpty * 60 * 1000) / 1000 / 60, "minutes");
                                                    ref.child(Constants.DATABASE.SECTION_GROUPS).child(group).removeValue();
                                                    ref.child(group).removeValue();
                                                }
                                            }
                                        }).start();
                            }
                        }).start();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void validateUsers() {

    }

    public void setRef(DatabaseReference ref) {
        this.ref = ref;
    }


    /**
     * This method requests and returns customToken from Firebase. Depending on current installation type
     * it defines the properly request and performs it. Installation type can be defined in gradle.build.
     */
    @Override
    public String createCustomToken(String uid) {
        String customToken = null;
        if(Common.getInstance().getDataProcessor("v1").isServerMode()) {
            try {
                Class tempClass = Class.forName("com.google.firebase.auth.FirebaseAuth");
                Method method = tempClass.getDeclaredMethod("createCustomToken", String.class);
                Task<String> taskCreateToken = (Task<String>) method.invoke(FirebaseAuth.getInstance(), uid);
                Tasks.await(taskCreateToken);
                customToken = taskCreateToken.getResult();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            customToken = String.valueOf(FirebaseAuth.getInstance().createCustomToken("Viewer"));
        }
        return customToken;
    }

}
