package ru.wtg.whereaminow;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;

import io.nlopez.smartlocation.SmartLocation;
import ru.wtg.whereaminow.helpers.ContinueDialog;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.holders.AbstractViewHolder;
import ru.wtg.whereaminow.holders.AddressViewHolder;
import ru.wtg.whereaminow.holders.ButtonViewHolder;
import ru.wtg.whereaminow.holders.CameraViewHolder;
import ru.wtg.whereaminow.holders.DistanceViewHolder;
import ru.wtg.whereaminow.holders.DrawerViewHolder;
import ru.wtg.whereaminow.holders.FabViewHolder;
import ru.wtg.whereaminow.holders.FacebookViewHolder;
import ru.wtg.whereaminow.holders.MapButtonsViewHolder;
import ru.wtg.whereaminow.holders.MarkerViewHolder;
import ru.wtg.whereaminow.holders.MenuViewHolder;
import ru.wtg.whereaminow.holders.MessagesViewHolder;
import ru.wtg.whereaminow.holders.NavigationViewHolder;
import ru.wtg.whereaminow.holders.SavedLocationsViewHolder;
import ru.wtg.whereaminow.holders.SensorsViewHolder;
import ru.wtg.whereaminow.holders.SnackbarViewHolder;
import ru.wtg.whereaminow.holders.StreetsViewHolder;
import ru.wtg.whereaminow.holders.TrackViewHolder;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.ACTIVITY_CREATE;
import static ru.wtg.whereaminow.State.ACTIVITY_DESTROY;
import static ru.wtg.whereaminow.State.ACTIVITY_PAUSE;
import static ru.wtg.whereaminow.State.ACTIVITY_RESULT;
import static ru.wtg.whereaminow.State.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.PREPARE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.TRACKING_JOIN;
import static ru.wtg.whereaminow.State.TRACKING_STOP;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_LOCATION_SINGLE;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_NORMAL;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_SATELLITE;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_TERRAIN;
import static ru.wtg.whereaminow.service_helpers.MyTracking.TRACKING_URI;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.DEBUGGING;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_PERMISSION_LOCATION;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_INITIAL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_DISCONNECTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ERROR;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_STOPPED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_UPDATED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private State state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);


        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        state = State.getInstance();

        if(DEBUGGING){
            getSharedPreferences("intro", MODE_PRIVATE).edit().clear().commit();
//            state.setPreference("intro",false);
        }

        System.out.println("APP_ID:"+state.getStringPreference("device_id", null));

        System.out.println("TOKENFIRE:"+FirebaseInstanceId.getInstance().getToken());

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        state.registerEntityHolder(new FabViewHolder(this).setView(findViewById(R.id.fab_layout)));
        state.registerEntityHolder(new DrawerViewHolder(this).setViewAndToolbar(findViewById(R.id.drawer_layout),toolbar).setCallback(onNavigationDrawerCallback));
        state.registerEntityHolder(new SnackbarViewHolder(getApplicationContext()).setLayout(findViewById(R.id.fab_layout)));


        state.fire(ACTIVITY_CREATE, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(BROADCAST);
        registerReceiver(receiver, intentFilter);

        if(!state.getBooleanPreference("intro",false)){
            state.setPreference("intro",true);
            startActivityForResult(new Intent(MainActivity.this, IntroActivity.class), 1);
            return;
        }

        if(!state.isGpsAccessRequested()) {
            state.setGpsAccessRequested(true);
            checkPermissions(REQUEST_PERMISSION_LOCATION,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
        } else if(state.isGpsAccessAllowed()) {
            onRequestPermissionsResult(REQUEST_PERMISSION_LOCATION, new String[]{}, new int[]{});
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        state.fire(ACTIVITY_PAUSE);
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        state.getUsers().forAllUsers(new MyUsers.Callback() {
            @Override
            public void call(Integer number, MyUser myUser) {
                myUser.removeViews();
            }
        });
        state.fire(ACTIVITY_DESTROY);
        state.clearViewHolders();
    }

    @Override
    public void onBackPressed() {
        DrawerViewHolder holder = (DrawerViewHolder) state.getEntityHolder(DrawerViewHolder.TYPE);
        if (holder != null && holder.isDrawerOpen()) {
            holder.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        state.fire(CREATE_OPTIONS_MENU, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        state.fire(PREPARE_OPTIONS_MENU, menu);
        return true;
    }

    /*@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.user_menu, menu);

        Object tag = v.getTag();
        if(tag != null) {
            int number = (int) tag;
            MyUser user = state.getUsers().getUsers().get(number);
            menu.setHeaderTitle(user.getProperties().getDisplayName());
            user.fire(CREATE_CONTEXT_MENU, menu);
        } else {
            state.fire(CREATE_CONTEXT_MENU, menu);
        }

    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle m = new Bundle();
        m.putInt("requestCode",requestCode);
        m.putInt("resultCode",resultCode);
        m.putParcelable("data",data);
        state.fire(ACTIVITY_RESULT, m);
    }

    SimpleCallback onNavigationDrawerCallback = new SimpleCallback<Integer>() {
        @Override
        public void call(Integer id) {
            switch(id) {
                case R.id.nav_settings:
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                    break;
                case R.id.nav_traffic:
                    map.setTrafficEnabled(!map.isTrafficEnabled());
                    break;
                case R.id.nav_satellite:
                    if (map.getMapType() != GoogleMap.MAP_TYPE_SATELLITE) {
                        state.fire(REQUEST_MODE_SATELLITE);
                    } else {
                        state.fire(REQUEST_MODE_NORMAL);
                    }
                    break;
                case R.id.nav_terrain:
                    if (map.getMapType() != GoogleMap.MAP_TYPE_TERRAIN)
                        state.fire(REQUEST_MODE_TERRAIN);
                    else
                        state.fire(REQUEST_MODE_NORMAL);
                    break;
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if(state.isGpsAccessAllowed()) {
            onMapReadyPermitted();
        }
    }

    public boolean checkPermissions(final int requestCode, String[] permissionsForCheck) {
        final ArrayList<String> permissions = new ArrayList<>();
        int[] grants = new int[permissionsForCheck.length];
        for (int i = 0; i < permissionsForCheck.length; i++) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, permissionsForCheck[i]) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(permissionsForCheck[i]);
                grants[i] = PackageManager.PERMISSION_DENIED;
            } else {
                grants[i] = PackageManager.PERMISSION_GRANTED;
            }
        }

        if (permissions.size() > 0) {
            Thread.dumpStack();
            new ContinueDialog(this).setMessage("Application must continuously get the locations. It seems using the GPS-sensor. Please grant application to access your location.").setCallback(new SimpleCallback<Void>() {
                @Override
                public void call(Void arg) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            permissions.toArray(new String[permissions.size()]),
                            requestCode);
                }
            }).show();
            return false;
        } else {
            onRequestPermissionsResult(requestCode, permissionsForCheck, grants);
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                state.setGpsAccessRequested(true);
                int res = 0;
                for (int i : grantResults) {
                    res += i;
                }
                if (res == 0) {
                    state.setGpsAccessAllowed(true);
                    onMapReadyPermitted();
                } else {
                    state.setGpsAccessAllowed(false);
                    Toast.makeText(getApplicationContext(), "GPS access is not granted.", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
    }

    public void onMapReadyPermitted() {
        if(map == null) return;
//        System.out.println("onMapReadyPermitted");

        if(!SmartLocation.with(MainActivity.this).location().state().locationServicesEnabled()){
            new ContinueDialog(this).setMessage("Application needs the enabled location services. Please enable it on the next screen.").setCallback(new SimpleCallback() {
                @Override
                public void call(Object arg) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            }).show();
            return;
        }

        new MapButtonsViewHolder(mapFragment);

        LinkedList<String> classes = new LinkedList<>();
        classes.add("FacebookViewHolder");
        classes.add("ButtonViewHolder");
        classes.add("MenuViewHolder");
        classes.add("MarkerViewHolder");
        classes.add("AddressViewHolder");
        classes.add("CameraViewHolder");
        classes.add("SavedLocationsViewHolder");
        classes.add("TrackViewHolder");
        classes.add("NavigationViewHolder");
        classes.add("DistanceViewHolder");
        classes.add("MessagesViewHolder");
        classes.add("SensorsViewHolder");
        classes.add("StreetsViewHolder");

        // IntroViewHolder must be registered last
//        classes.add("IntroViewHolder");

        for(String s:classes){
            try {
                Class<AbstractViewHolder> _tempClass = (Class<AbstractViewHolder>) Class.forName("ru.wtg.whereaminow.holders."+s);
                System.out.println("A:"+_tempClass);
                Constructor<AbstractViewHolder> ctor = _tempClass.getDeclaredConstructor(MainActivity.class);
                System.out.println("B:"+ctor);
                state.registerEntityHolder(ctor.newInstance(this));
                System.out.println("C");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        state.getUsers().setMe();
        state.getMe().addLocation(SmartLocation.with(MainActivity.this).location().getLastLocation());

        map.setBuildingsEnabled(true);
        map.setIndoorEnabled(true);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(!DEBUGGING) return;
                try {
                    Location location = state.getMe().getLocation();
                    Location loc = new Location("touch");
                    loc.setLatitude(latLng.latitude);
                    loc.setLongitude(latLng.longitude);
                    loc.setAltitude(location.getAltitude());
                    loc.setAccuracy(location.getAccuracy());
                    loc.setBearing(location.getBearing());
                    loc.setSpeed(location.getSpeed());
                    loc.setTime(location.getTime());
                    state.getMe().addLocation(loc);
                }catch(Exception e){
                    System.out.println("Error setOnMapClickListener: "+e.getMessage());
                }
            }
        });
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setIndoorLevelPickerEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        state.getUsers().forAllUsers(new MyUsers.Callback() {
            @Override
            public void call(Integer number, MyUser myUser) {
                myUser.createViews();
            }
        });
        ((CameraViewHolder) state.getEntityHolder(CameraViewHolder.TYPE)).move();

        state.fire(REQUEST_LOCATION_SINGLE);
        state.fire(ACTIVITY_RESUME, this);

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);

        String action = newIntent.getStringExtra("action");
        if(action != null) {
            switch(action) {
                case "fire":
                    String fire = newIntent.getStringExtra("fire");
                    Integer number = newIntent.getIntExtra("number", 0);
                    state.fire(fire, number);
            }
            return;
        }

        Uri data = newIntent.getData();
        newIntent.setData(null);
        if(data != null){
            String tokenId = data.getEncodedPath().replaceFirst("/track/", "");
            if(!tokenId.equals(State.getInstance().getToken())) {
                if(State.getInstance().getTracking() != null) state.fire(TRACKING_STOP);
                state.fire(TRACKING_JOIN, data);
            }
        } else if(!state.tracking_active()) {
            String trackingUri = state.getStringPreference(TRACKING_URI, null);
            if(trackingUri != null){
                state.fire(TRACKING_JOIN, Uri.parse(trackingUri));
            }
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String r = intent.getStringExtra(BROADCAST_MESSAGE);
                if(r != null && r.length() > 0) {
                    JSONObject o = new JSONObject(r);
                    if (!o.has(RESPONSE_STATUS)) return;

                    switch (o.getString(RESPONSE_STATUS)) {
                        case RESPONSE_STATUS_DISCONNECTED:
                            state.getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
                                    myUser.removeViews();
                                }
                            });
                            break;
                        case RESPONSE_STATUS_ACCEPTED:

                            SmartLocation.with(MainActivity.this).location().stop();
                            if (o.has(RESPONSE_NUMBER)) {
                                state.getUsers().forMe(new MyUsers.Callback() {
                                    @Override
                                    public void call(Integer number, MyUser myUser) {
                                        myUser.createViews();
                                    }
                                });
                            }
                            if (o.has(RESPONSE_INITIAL)) {
                                state.getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                                    @Override
                                    public void call(Integer number, MyUser myUser) {
                                        myUser.createViews();
                                    }
                                });
                            }
                            break;
                        case RESPONSE_STATUS_ERROR:
                            break;
                        case RESPONSE_STATUS_UPDATED:
                            if (o.has(USER_DISMISSED)) {
                                int number = o.getInt(USER_DISMISSED);
                                state.getUsers().forUser(number, new MyUsers.Callback() {
                                    @Override
                                    public void call(Integer number, final MyUser myUser) {
                                        myUser.fire(USER_DISMISSED);
                                        myUser.removeViews();
                                    }
                                });
                            }
                            if (o.has(USER_JOINED)) {
                                int number = o.getInt(USER_JOINED);
                                state.getUsers().forUser(number, new MyUsers.Callback() {
                                    @Override
                                    public void call(Integer number, MyUser myUser) {
                                        myUser.createViews();
                                        myUser.fire(USER_JOINED);
                                    }
                                });
                            }
                            break;
                        case RESPONSE_STATUS_STOPPED:
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public GoogleMap getMap() {
        return map;
    }
}
