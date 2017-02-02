package ru.wtg.whereaminowserver.servers;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.internal.NonNull;
import com.google.firebase.tasks.OnFailureListener;
import com.google.firebase.tasks.OnSuccessListener;
import com.google.firebase.tasks.Task;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.wtg.whereaminowserver.helpers.CheckReq;
import ru.wtg.whereaminowserver.helpers.MyToken;
import ru.wtg.whereaminowserver.helpers.MyUser;
import ru.wtg.whereaminowserver.helpers.SensitiveData;
import ru.wtg.whereaminowserver.helpers.Utils;
import ru.wtg.whereaminowserver.interfaces.RequestHolder;

import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_GROUPS;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_OPTIONS;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_OPTIONS_DELAY_TO_DISMISS;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_OPTIONS_DISMISS_INACTIVE;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_OPTIONS_PERSISTENT;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_OPTIONS_REQUIRES_PASSWORD;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_OPTIONS_TIME_TO_LIVE_IF_EMPTY;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_PUBLIC;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_USERS_DATA;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_USERS_DATA_PRIVATE;
import static ru.wtg.whereaminowserver.helpers.Constants.DATABASE_SECTION_USERS_KEYS;
import static ru.wtg.whereaminowserver.helpers.Constants.INACTIVE_USER_DISMISS_DELAY;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_CHECK_USER;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_DEVICE_ID;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_HASH;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_JOIN_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MANUFACTURER;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_MODEL;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_NEW_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_OS;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_TIMESTAMP;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_CONTROL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_SIGN;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_CHECK;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ERROR;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_COLOR;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NAME;

/**
 * Created 10/5/16.
 */

public class MyWssServerFB extends MyWssServer {

    private DatabaseReference ref;

    private Runnable dismissInactiveUsers = new Runnable() {
        @Override
        public void run() {

            while(true) {
                try {
                    Thread.sleep(INACTIVE_USER_DISMISS_DELAY * 1000);

                    MyUser user;
                    long currentDate = new Date().getTime();
                    for (Map.Entry<String, MyUser> entry : ipToUser.entrySet()) {

                        user = entry.getValue();
                        if (user != null) {

                            System.out.println("INACTIVITY: " + user.getName() + ":" + (currentDate - user.getChanged()));

                            if (currentDate - user.getChanged() > INACTIVE_USER_DISMISS_DELAY * 1000) {
                                // dismiss user
                                JSONObject o = new JSONObject();
                                if(ipToToken.containsKey(entry.getKey())) {
                                    MyToken token = ipToToken.get(entry.getKey());
                                    o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                                    o.put(USER_DISMISSED, user.getNumber());
                                    token.sendToAllFrom(o, user);
                                }

                                if(ipToUser.containsKey(entry.getKey())) ipToUser.remove(entry.getKey());
                                if(ipToToken.containsKey(entry.getKey())) ipToToken.remove(entry.getKey());
                                if(ipToCheck.containsKey(entry.getKey())) ipToCheck.remove(entry.getKey());
                                user.webSocket.close();
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
            }
        }
    };

    public MyWssServerFB(int port) throws UnknownHostException {
        this(new InetSocketAddress(port));
        tokens = new ConcurrentHashMap<String, MyToken>();
        ipToToken = new ConcurrentHashMap<String, MyToken>();
        ipToUser = new ConcurrentHashMap<String, MyUser>();
        ipToCheck = new ConcurrentHashMap<String, CheckReq>();

        File f = new File(new SensitiveData().getFBPrivateKeyFile());
        try {
            System.out.println(f.getCanonicalPath().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FirebaseApp.initializeApp(new FirebaseOptions.Builder()
                    .setServiceAccount(new FileInputStream(new SensitiveData().getFBPrivateKeyFile()))
                    .setDatabaseUrl(new SensitiveData().getFBDatabaseUrl())
                    .build());

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            ref = database.getReference();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        requestHolders = new LinkedHashMap<>();

        LinkedList<String> classes = new LinkedList<String>();
        classes.add("TrackingRequestHolder");
        classes.add("MessageRequestHolder");
        classes.add("ChangeNameRequestHolder");
        classes.add("WelcomeMessageRequestHolder");
        classes.add("LeaveRequestHolder");
        classes.add("SavedLocationRequestHolder");

        for(String s:classes){
            try {
                Class<RequestHolder> _tempClass = (Class<RequestHolder>) Class.forName("ru.wtg.whereaminowserver.holders.request."+s);
                Constructor<RequestHolder> ctor = _tempClass.getDeclaredConstructor(MyWssServer.class);
                registerRequestHolder(ctor.newInstance(this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public MyWssServerFB(InetSocketAddress address) {
        super(address);

    }

    /*   @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        System.out.println("HANDSHAKE:"+conn+":"+draft+":"+request);

        return super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
    }
*/
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
//        this.sendToAll( "new connection: " + handshake.getResourceDescriptor() );
        System.out.println("WSS:on open:" + conn.getRemoteSocketAddress() + " connected");

        try {
//            conn.send("{\"" + RESPONSE_STATUS + "\":\""+RESPONSE_STATUS_CONNECTED+"\",\"version\":" + SERVER_BUILD + "}");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("WSS:on close:" + conn.getRemoteSocketAddress() + " disconnected:by client:"+remote+":"+code+":"+reason);
//        this.sendToAll( conn + " has left the room!" );
        String ip = conn.getRemoteSocketAddress().toString();
        if(ipToCheck.containsKey(ip)) ipToCheck.remove(ip);

    }

    @Override
    public void onMessage(final WebSocket conn, String message) {
        boolean disconnect = false;
        try {
            System.out.println("WSS:on message:" + conn.getRemoteSocketAddress() + ": " + message);

            final String ip = conn.getRemoteSocketAddress().toString();
            final JSONObject request, response = new JSONObject();

            try {
                request = new JSONObject(message);
            } catch(JSONException e) {
                System.err.println("WSS:error in request message: "+e.getMessage());
                return;
            }
            if (request == null || !request.has(REQUEST_TIMESTAMP)) return;
//            long timestamp = request.getLong(REQUEST_TIMESTAMP);
        /*if(new Date().getTime() - timestamp > LIFETIME_REQUEST_TIMEOUT*1000) {
            System.out.println("WSS:ignore request because of timeout");
//            conn.close(CloseFrame.GOING_AWAY, "Request timeout");
            return;
        }*/

            if (!request.has(REQUEST)) return;

            String req = request.getString(REQUEST);
            if (REQUEST_NEW_TOKEN.equals(req)) {
                if (request.has(REQUEST_DEVICE_ID)) {
                    final MyToken token = new MyToken();

                    final MyUser user = new MyUser(conn, request.getString(REQUEST_DEVICE_ID));

                    ref.child(DATABASE_SECTION_GROUPS).child(token.getId()).setValue(user.getUid());
                    //TODO implement token controller here

                    registerUser(token.getId(), user, request);

                    System.out.println("NEW:token created and accepted:" + token);

                } else {
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Your device id is not defined");
                    conn.send(response.toString());
                    conn.close();
                    System.out.println("NEW:RESPONSE_STATUS_ERROR:" + response);
                }

            } else if (REQUEST_JOIN_TOKEN.equals(req)) {
                if (request.has(REQUEST_TOKEN)) {
                    final String tokenId = request.getString(REQUEST_TOKEN);

                    final DatabaseReference refToken = ref.child(tokenId);

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

                    final ValueEventListener requestDataPrivateListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            System.out.println(dataSnapshot.getValue().getClass() +":"+ dataSnapshot.getChildrenCount());
                            long number = dataSnapshot.getChildrenCount();

                            final MyToken token = new MyToken();


                            final MyUser user = new MyUser(conn, request.getString(REQUEST_DEVICE_ID));
                            user.number = (int) number;

                            registerUser(tokenId, user, request);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };

                    final ValueEventListener numberForKeyListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null) { //join as existing member, go to check
                                System.out.println("RECONNECT:"+dataSnapshot.getKey()+":"+dataSnapshot.getValue()+", check control");
                                CheckReq check = new CheckReq();
                                check.setControl(Utils.getUnique());
                                check.setTokenId(tokenId);
                                check.setUid(dataSnapshot.getKey());
                                check.setNumber((long) dataSnapshot.getValue());
                                if (request.has(USER_NAME))
                                    check.setName(request.getString(USER_NAME));

                                response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                                response.put(RESPONSE_CONTROL, check.getControl());
                                ipToCheck.put(ip, check);
                                conn.send(response.toString());

//                                long number = (long) dataSnapshot.getValue();
//                                refToken.child("/users/data-private/" + number+"/control").setValue(check.control);

                            } else { // join as new member

                                refToken.child(DATABASE_SECTION_USERS_DATA_PRIVATE).addListenerForSingleValueEvent(requestDataPrivateListener);

//                                System.out.println("ASNEW:"+dataSnapshot.getValue());
//                                long number = (long) dataSnapshot.getValue();
//                                refToken.child(DATABASE_SECTION_USERS_DATA_PRIVATE + "/" + number).addListenerForSingleValueEvent(userDataListener);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("SINGLEERR2:"+databaseError.getMessage());

                        }
                    };

                    ValueEventListener tokenOptionsListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue() != null) {
                                String deviceId = request.getString(REQUEST_DEVICE_ID);
                                final String uid = Utils.getEncryptedHash(deviceId);
                                refToken.child(DATABASE_SECTION_USERS_KEYS + "/" + uid).addListenerForSingleValueEvent(numberForKeyListener);
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
                        refToken.child(DATABASE_SECTION_OPTIONS).addListenerForSingleValueEvent(tokenOptionsListener);
                    } else {
                        CheckReq check = new CheckReq();
                        check.setControl(Utils.getUnique());
                        check.setTokenId(tokenId);

                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                        response.put(RESPONSE_CONTROL, check.getControl());
                        ipToCheck.put(ip, check);
                        conn.send(response.toString());
                    }

if(true) return;
                    if (tokens.containsKey(tokenId)) {
                        MyToken token = tokens.get(tokenId);

                        if (request.has(REQUEST_DEVICE_ID)) {
                            String deviceId1 = request.getString(REQUEST_DEVICE_ID);
                            MyUser user = null;
                            for (Map.Entry<String, MyUser> x : token.users.entrySet()) {
                                if (deviceId1.equals(x.getValue().getDeviceId())) {
                                    user = x.getValue();
                                    break;
                                }
                            }
                            if (user != null) {
                                user.setChanged();
                                CheckReq check = new CheckReq();
                                check.setControl(Utils.getUnique());
                                check.setToken(token);
                                if (request.has(USER_NAME))
                                    check.setName(request.getString(USER_NAME));

                                response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                                response.put(RESPONSE_CONTROL, check.getControl());
                                ipToCheck.put(ip, check);
                            } else {

                                user = new MyUser(conn, request.getString(REQUEST_DEVICE_ID));
                                user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
                                user.setModel(request.getString(REQUEST_MODEL));
                                user.setOs(request.getString(REQUEST_OS));
                                if (request.has(USER_NAME) && request.getString(USER_NAME) != null && request.getString(USER_NAME).length() > 0) {
                                    user.setName(request.getString(USER_NAME));
                                }
                                token.addUser(user);

                                response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                                response.put(RESPONSE_NUMBER, user.getNumber());

                                ipToToken.put(ip, token);
                                ipToUser.put(ip, user);

                                token.sendInitialTo(response, user);

                                JSONObject o = new JSONObject();
                                o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                                o.put(USER_COLOR, user.getColor());
                                o.put(USER_JOINED, user.getNumber());
                                if (user.getName() != null && user.getName().length() > 0) {
                                    o.put(USER_NAME, user.getName());
                                }
                                token.sendToAllFrom(o, user);
                                return;
                            }

                        } else {
                            CheckReq check = new CheckReq();
                            check.setControl(Utils.getUnique());
                            check.setToken(token);

                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                            response.put(RESPONSE_CONTROL, check.getControl());
                            ipToCheck.put(ip, check);
                        }
                    } else {
                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                        response.put(RESPONSE_MESSAGE, "This group is expired.");
                        disconnect = true;
                    }
                } else {
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Wrong request (token not defined).");
                    disconnect = true;
                }
                Utils.pause(2);//FIXME remove pause
                conn.send(response.toString());
                System.out.println("JOIN:response:" + response);
            } else if (REQUEST_CHECK_USER.equals(req)) {
                if (request.has(REQUEST_HASH)) {
                    String hash = request.getString((REQUEST_HASH));
                    System.out.println(("CHECK:requested device: [hash=" + hash + "]"));

                    if (ipToCheck.containsKey(ip)) {
                        final CheckReq check = ipToCheck.get(ip);

                        System.out.println("CHECK:found token: [name=" + check.getName() + ", token=" + check.getTokenId() + ", control=" + check.getControl() + "]");

                        final DatabaseReference refToken = ref.child(check.getTokenId());

                        final ValueEventListener userDataListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null) { //join as existing member
                                    System.out.println("FOUNDUSER:"+dataSnapshot.getKey()+":"+dataSnapshot.getValue()+", check control");
/*
                                    check.uid = dataSnapshot.getKey();
                                    if (request.has(USER_NAME))
                                        check.name = request.getString(USER_NAME);

                                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_CHECK);
                                    response.put(RESPONSE_CONTROL, check.control);
                                    ipToCheck.put(ip, check);
                                    conn.send(response.toString());
*/
                                    System.out.println("SUCCESS REGISTERING USER, IDs:"+check.getTokenId()+":"+check.getNumber()+":"+check.getUid());
                                    FirebaseAuth.getInstance().createCustomToken(check.getUid()).addOnSuccessListener(new OnSuccessListener<String>() {
                                        @Override
                                        public void onSuccess(final String customToken) {
                                            System.out.println("GOT:"+customToken);

                                            Map<String,Object> update = new HashMap<>();
                                            update.put("active", true);
                                            update.put("color",Utils.selectColor((int) check.getNumber()));
                                            update.put("changed", new Date().getTime());
                                            if (check.getName() != null && check.getName().length() > 0) {
                                                update.put(USER_NAME,check.getName());
                                            }

                                            refToken.child(DATABASE_SECTION_USERS_DATA + "/"+check.getNumber()).updateChildren(update).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                                                    response.put(RESPONSE_NUMBER, check.getNumber());
                                                    response.put(RESPONSE_SIGN, customToken);
                                                    conn.send(response.toString());
                                                    conn.close();
                                                    System.out.println("SUCCESSJOIN:"+response);
//                                            conn.close();
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

//                                    long number = (long) dataSnapshot.getValue();
//                                    refToken.child("/users/data-private/" + number+"/control").setValue(check.control);

                                } else { // join as new member
                                    System.out.println("ASNEW:"+dataSnapshot.getValue());
                                    long number = (long) dataSnapshot.getValue();
//                                    refToken.child("/users/data-private/" + number).addListenerForSingleValueEvent(userDataListener);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                System.out.println("SINGLEERR2:"+databaseError.getMessage());

                            }
                        };

                        ValueEventListener tokenOptionsListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue() != null) {
                                    refToken.child(DATABASE_SECTION_USERS_DATA_PRIVATE + "/" + check.getNumber()).addListenerForSingleValueEvent(userDataListener);
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

                        refToken.child(DATABASE_SECTION_OPTIONS).addListenerForSingleValueEvent(tokenOptionsListener);

if(true)return;
                        MyUser user = null;
                        for (Map.Entry<String, MyUser> x : check.getToken().users.entrySet()) {
                            System.out.println("CHECK:looking for device: [control=" + check.getControl() + ", deviceId=" + x.getValue().getDeviceId().substring(0, 20) + "..., calculatedHash=" + x.getValue().calculateHash(check.getControl()) + "]");
                            if (hash.equals(x.getValue().calculateHash(check.getControl()))) {
                                user = x.getValue();
                                break;
                            }
                        }
                        if (user != null) {
                            System.out.println("CHECK:user accepted:" + user);
                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                            response.put(RESPONSE_NUMBER, user.getNumber());
                            user.setConnection(conn);
                            user.setChanged();
                            if (check.getName() != null && check.getName().length() > 0) {
                                user.setName(check.getName());
                            }

                            ipToToken.put(ip, check.getToken());
                            ipToUser.put(ip, user);
                            ipToCheck.remove(ip);

                            check.getToken().sendInitialTo(response, user);

                            JSONObject o = new JSONObject();//user.getPosition().toJSON();
                            o.put(RESPONSE_STATUS, RESPONSE_STATUS_UPDATED);
                            o.put(USER_COLOR, user.getColor());
                            o.put(USER_JOINED, user.getNumber());
                            if (check.getName() != null && check.getName().length() > 0) {
                                o.put(USER_NAME, check.getName());
                            }
                            check.getToken().sendToAllFrom(o, user);
                            return;
//                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
//                            response.put(RESPONSE_MESSAGE, "User not granted.");
                        } else {
                            System.out.println("CHECK:user not accepted for hash:" + hash);
                            if (ipToToken.containsKey(ip)) ipToToken.remove(ip);
                            if (ipToUser.containsKey(ip)) ipToUser.remove(ip);

                            response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                            response.put(RESPONSE_MESSAGE, "Cannot join to group (user not accepted).");
                            disconnect = true;
                        }

                        ipToCheck.remove(ip);
                    } else {
                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                        response.put(RESPONSE_MESSAGE, "Cannot join to group (user not authorized).");
                        disconnect = true;
                    }
                } else {
                    response.put(RESPONSE_STATUS, RESPONSE_STATUS_ERROR);
                    response.put(RESPONSE_MESSAGE, "Cannot join to group (hash not defined).");
                    disconnect = true;
                }

                System.out.println("CHECK:response:" + response);
                Utils.pause(2);//FIXME remove pause
                conn.send(response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(disconnect) {
            conn.close();
        }
    }

    private void registerUser(final String tokenId, final MyUser user, final JSONObject request) {
        final JSONObject response = new JSONObject();

        user.setColor(Utils.selectColor(user.getNumber()));
        user.setManufacturer(request.getString(REQUEST_MANUFACTURER));
        user.setModel(request.getString(REQUEST_MODEL));
        user.setOs(request.getString(REQUEST_OS));
        if (request.has(USER_NAME)) user.setName(request.getString(USER_NAME));

        final String uid = Utils.getEncryptedHash(user.getDeviceId());

        final Map<String, Object> childUpdates = new HashMap<>();

        Map<String,Object> o = new HashMap();
        o.put(USER_COLOR, user.getColor());
        o.put(USER_NAME, user.getName());
        o.put("active", true);
        o.put("created", user.getCreated());
        o.put("created", user.getCreated());
        childUpdates.put(DATABASE_SECTION_USERS_DATA + "/" + user.getNumber(),o);

        o = new HashMap();

//                    o.put(RESPONSE_CONTROL,user.getControl());
        o.put(REQUEST_MODEL,user.getModel());
        o.put(REQUEST_DEVICE_ID,user.getDeviceId());
        o.put(REQUEST_OS,user.getOs());
        o.put("key",uid);
        childUpdates.put(DATABASE_SECTION_USERS_DATA_PRIVATE + "/" + user.getNumber(),o);

        for(Map.Entry<String,RequestHolder> entry: requestHolders.entrySet()) {
            if(entry.getValue().isSaveable()) {
                childUpdates.put(DATABASE_SECTION_PUBLIC +"/" + entry.getKey() + "/"+user.getNumber(), "{}");
            }
        }

        childUpdates.put(DATABASE_SECTION_USERS_KEYS + "/"+uid,user.getNumber());
        childUpdates.put(DATABASE_SECTION_OPTIONS_TIME_TO_LIVE_IF_EMPTY,15);
        childUpdates.put(DATABASE_SECTION_OPTIONS_REQUIRES_PASSWORD,false);
        childUpdates.put(DATABASE_SECTION_OPTIONS_PERSISTENT,false);
        childUpdates.put(DATABASE_SECTION_OPTIONS_DISMISS_INACTIVE,false);
        childUpdates.put(DATABASE_SECTION_OPTIONS_DELAY_TO_DISMISS,300);

        Task<Void> a = ref.child(tokenId).updateChildren(childUpdates);
        a.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                System.out.println("SUCCESS REGISTERING USER, IDs:"+tokenId+":"+uid);
                FirebaseAuth.getInstance().createCustomToken(uid).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String customToken) {
                        System.out.println("GOT:"+customToken);
                        response.put(RESPONSE_STATUS, RESPONSE_STATUS_ACCEPTED);
                        if(!REQUEST_JOIN_TOKEN.equals(request.getString(REQUEST)) && !REQUEST_CHECK_USER.equals(request.getString(REQUEST))) {
                            response.put(RESPONSE_TOKEN, tokenId);
                        }
                        response.put(RESPONSE_NUMBER, user.getNumber());
                        response.put(RESPONSE_SIGN, customToken);
                        user.webSocket.send(response.toString());
                        user.webSocket.close();
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

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null && conn.getRemoteSocketAddress() != null) {
            System.out.println("WSS:on error:" + conn.getRemoteSocketAddress() + ": " + ex.getMessage());

            String ip = conn.getRemoteSocketAddress().toString();
            if(ipToToken.containsKey(ip)) ipToToken.remove(ip);
            if(ipToUser.containsKey(ip)) ipToUser.remove(ip);
            if(ipToCheck.containsKey(ip)) ipToCheck.remove(ip);
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onWebsocketPing(WebSocket conn, Framedata f) {
        super.onWebsocketPing(conn, f);

        try {
            String ip = conn.getRemoteSocketAddress().toString();
            if (ipToUser.containsKey(ip)) {
                ipToUser.get(ip).setChanged();
            }
//            System.out.println("PING:" + conn.getRemoteSocketAddress() + ":" + f);
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends <var>text</var> to all currently connected WebSocket clients.
     *
     * @param text The String to send across the network.
     */
    public void sendToAll(String text, WebSocket insteadConnection) {
        Collection<WebSocket> con = connections();
//        synchronized (con) {
        for (WebSocket c : con) {
            if (insteadConnection != null && c == insteadConnection) continue;
            System.out.println("WSS:to:" + c.getRemoteSocketAddress() + ":" + text);
            c.send(text);
        }
//        }
    }

    public boolean parse(BufferedReader sysin) throws IOException, InterruptedException {
        String in = sysin.readLine();
        System.out.println("READ:" + in);
//                        s.sendToAll(in);
        if (in.equals("exit")) {
            stop();
            return false;
        } else if (in.equals("restart")) {
            stop();
            start();
            return false;
        }
        return true;
    }

    public void removeUser(String tokenId,String id){
        if(tokenId != null && id != null && tokens.containsKey(tokenId)){
            MyToken t = tokens.get(tokenId);
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
}
