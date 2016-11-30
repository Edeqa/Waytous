package ru.wtg.whereaminow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import ru.wtg.whereaminow.helpers.ContinueDialog;
import ru.wtg.whereaminow.helpers.FabMenu;
import ru.wtg.whereaminow.helpers.InviteSender;
import ru.wtg.whereaminow.helpers.LightSensorManager;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.Utils;
import ru.wtg.whereaminow.holders.AddressViewHolder;
import ru.wtg.whereaminow.holders.ButtonViewHolder;
import ru.wtg.whereaminow.holders.CameraViewHolder;
import ru.wtg.whereaminow.holders.FabViewHolder;
import ru.wtg.whereaminow.holders.MapButtonsViewHolder;
import ru.wtg.whereaminow.holders.MarkerViewHolder;
import ru.wtg.whereaminow.holders.MenuViewHolder;
import ru.wtg.whereaminow.holders.MessagesViewHolder;
import ru.wtg.whereaminow.holders.SnackbarViewHolder;
import ru.wtg.whereaminow.holders.TrackViewHolder;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.JOIN_TRACKING;
import static ru.wtg.whereaminow.State.NEW_MESSAGE;
import static ru.wtg.whereaminow.State.NEW_TRACKING;
import static ru.wtg.whereaminow.State.SEND_LINK;
import static ru.wtg.whereaminow.State.STOP_TRACKING;
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
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private DrawerLayout drawer;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private Intent intent;
    private State state;
    private CameraViewHolder camera;
    private FabMenu fab;
    private LightSensorManager lightSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        state = State.getInstance();
        state.setActivity(this);
        state.checkDeviceId();

        lightSensor = new LightSensorManager(this);
        lightSensor.setOnEnvironmentChangeListener(new SimpleCallback<String>() {
            @Override
            public void call(String environment) {
                switch(environment){
                    case "day":
                        if(map != null) map.setMapStyle(null);
                        setTheme(R.style.DayTheme);
                        break;
                    case "night":
                        if(map != null) map.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.mapstyle_night));
                        setTheme(R.style.NightTheme);
                        break;
                }
            }
        });

        fab = (FabMenu) findViewById(R.id.fab);
        fab.initialize();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        intent = new Intent(MainActivity.this, WhereAmINowService.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(BROADCAST);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!state.isGpsAccessRequested()) {
            state.setGpsAccessRequested(true);
            fab.hideMenu(true);
            checkPermissions(REQUEST_PERMISSION_LOCATION,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
        } else if(state.isGpsAccessAllowed()) {
            onRequestPermissionsResult(REQUEST_PERMISSION_LOCATION, new String[]{}, new int[]{});
        } else {
            fab.setGpsOff();
            fab.showMenu(true);
        }
        lightSensor.enable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        SmartLocation.with(MainActivity.this).location().stop();
        lightSensor.disable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
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
        state.clearViewHolders();
        state.setActivity(null);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        state.getMe().fire(CREATE_OPTIONS_MENU, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.user_menu, menu);

        int number = (int) v.getTag();
        MyUser user = state.getUsers().getUsers().get(number);
        menu.setHeaderTitle(user.getProperties().getName());

        user.fire(CREATE_CONTEXT_MENU, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_location:
//                System.out.println("TRY");
//                System.out.println("TRY1:"+(1/0));
                System.out.println("action_save_location");
                /*snackbar.setText("Location saved.").setDuration(Snackbar.LENGTH_LONG).setAction("Edit", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("edit location");
                    }
                }).show();*/
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_start_and_share:
                System.out.println("nav_start_and_share");
                break;
            case R.id.nav_saved_locations:
                System.out.println("nav_saved_locations");
                break;
            case R.id.nav_settings:
                System.out.println("nav_settings");
                break;
            case R.id.nav_traffic:
                map.setTrafficEnabled(!map.isTrafficEnabled());
                break;
            case R.id.nav_satellite:
                if (map.getMapType() != GoogleMap.MAP_TYPE_SATELLITE)
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                else
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.nav_terrain:
                if (map.getMapType() != GoogleMap.MAP_TYPE_TERRAIN)
                    map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                else
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if(state.isGpsAccessAllowed()) {
            checkPermissions(REQUEST_PERMISSION_LOCATION,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    // permissionsForCheck = android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION
    // requestCode = MainActivity.REQUEST_PERMISSION_LOCATION
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

        System.out.println("CHECK:"+permissions.size());
        if (permissions.size() > 0) {
            Thread.dumpStack();
            new ContinueDialog(this).setMessage("Request for permissions").setCallback(new SimpleCallback<Void>() {
                @Override
                public void call(Void arg) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            permissions.toArray(new String[permissions.size()]),
                            requestCode);
                    System.out.println("checkPermission:" + requestCode);
                }
            }).show();
            return false;
        } else {
            onRequestPermissionsResult(requestCode, permissionsForCheck, grants);
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        System.out.println("onRequestPermissionsResult=" + requestCode + ":");

        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                state.setGpsAccessRequested(true);
                int res = 0;
                for (int i : grantResults) {
                    res += i;
                }
                if (res == 0) {
                    fab.setPlus();
                    state.setGpsAccessAllowed(true);
                    onMapReadyPermitted();
//                    if(enableLocationManager()) {
//                    if(SmartLocation.with(MainActivity.this).location().state().locationServicesEnabled()) {
//                        state.getUsers().forAllUsers(new MyUsers.Callback() {
//                            @Override
//                            public void call(Integer number, MyUser myUser) {
//                                myUser.createViews();
//                            }
//                        });
//                        if(state.tracking()){
//                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//                        }
//                    }
                } else {
                    state.setGpsAccessAllowed(false);
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fab.hideMenu(true);
                            checkPermissions(REQUEST_PERMISSION_LOCATION,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
                        }
                    });
                    fab.setGpsOff();
                    fab.showMenu(true);
                    Toast.makeText(getApplicationContext(), "GPS access is not granted.", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
    }

    public void onMapReadyPermitted() {
        if(map == null) return;
        System.out.println("onMapReadyPermitted");
        if(!SmartLocation.with(MainActivity.this).location().state().locationServicesEnabled()) return;

        new MapButtonsViewHolder(mapFragment);
        state.registerEntityHolder(new ButtonViewHolder(this).setLayout((LinearLayout) findViewById(R.id.layout_users)));
        state.registerEntityHolder(new MarkerViewHolder(this).setMap(map));
        state.registerEntityHolder(new MenuViewHolder(this));
        state.registerEntityHolder(new TrackViewHolder().setMap(map));
        state.registerEntityHolder(new AddressViewHolder().setCallback(new SimpleCallback<String>() {
            @Override
            public void call(String text) {
                getSupportActionBar().setSubtitle(text);
            }
        }));
        state.registerEntityHolder(camera = CameraViewHolder.getInstance(this,0)
                .setMap(map).setScaleView((MapScaleView) findViewById(R.id.scaleView)));
        state.registerEntityHolder(new MessagesViewHolder(MainActivity.this));
        state.registerEntityHolder(new SnackbarViewHolder(getApplicationContext()).setLayout(findViewById(R.id.fab_layout)));
        state.registerEntityHolder(new FabViewHolder().setFab(fab).setOnFabClick(onFabClick));

        state.getUsers().setMe();

        SmartLocation.with(MainActivity.this).location().oneFix().start(locationUpdateListener);

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
                    locationUpdateListener.onLocationUpdated(loc);
                    state.myTracking.locationUpdatedListener.onLocationUpdated(loc);
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

        if(state.tracking() || state.getUsers().getCountActive() > 1) {
            camera.move();
        }
        if(!state.tracking()) {
            enableLocationManager();
        }

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        super.onNewIntent(intent);

        Uri data = newIntent.getData();
        if(data != null){
            String tokenId = data.getEncodedPath().replaceFirst("/track/","");

            if(state.tracking() && tokenId.equals(state.getToken())) {
            } else {
                intent.putExtra("mode", "join");
                intent.putExtra("token", tokenId);
                intent.putExtra("host", data.getHost());
                startService(intent);
                state.fire(JOIN_TRACKING);
            }
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String r = intent.getStringExtra(BROADCAST_MESSAGE);
            JSONObject o;
            try {
                o = new JSONObject(r);
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
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        if (o.has(RESPONSE_TOKEN)) {
                            onFabClick.call(SEND_LINK);
                        }
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
                        enableLocationManager();
                        break;
                    case RESPONSE_STATUS_UPDATED:
                        if(o.has(USER_DISMISSED)) {
                            int number = o.getInt(USER_DISMISSED);
                            state.getUsers().forUser(number,new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, final MyUser myUser) {
                                    myUser.fire(USER_DISMISSED);
                                    myUser.removeViews();
                                }
                            });
                        }
                        if(o.has(USER_JOINED)) {
                            int number = o.getInt(USER_JOINED);
                            state.getUsers().forUser(number,new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
                                    myUser.createViews();
                                    myUser.fire(USER_JOINED);
                                }
                            });
                        }
                        break;
                    case RESPONSE_STATUS_STOPPED:
                        enableLocationManager();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void enableLocationManager() {
        LocationParams.Builder builder = new LocationParams.Builder()
                .setAccuracy(LocationAccuracy.HIGH).setDistance(1).setInterval(1000);
        SmartLocation.with(this).location().continuous().config(builder.build()).start(locationUpdateListener);

/*
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            return false;
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATES_DELAY, 1, locationListener);
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATES_DELAY, 1, locationListener);
            return true;
        }
*/
    }

    private OnLocationUpdatedListener locationUpdateListener = new OnLocationUpdatedListener() {
        @Override
        public void onLocationUpdated(final Location location) {
            if(!state.tracking()) {
                state.getMe().addLocation(Utils.normalizeLocation(state.getGpsFilter(), location));
            }
        }
    };

    private SimpleCallback<String> onFabClick = new SimpleCallback<String>() {
        @Override
        public void call(String id) {
            switch (id) {
                case NEW_TRACKING:
                    intent.putExtra("mode", "start");
                    startService(intent);
                    break;
                case SEND_LINK:
                    new InviteSender(MainActivity.this).send("https://" + state.myTracking.getHost() + ":8080/track/" + state.getToken());
                    break;
                case NEW_MESSAGE:
                    state.fire(NEW_MESSAGE);
                    break;
                case STOP_TRACKING:
                    state.getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser myUser) {
                            myUser.removeViews();
                        }
                    });
                    intent.putExtra("mode", "stop");
                    startService(intent);
                    break;
            }
        }
    };

/*    GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            System.out.println("ONMAPCLICK");
        }
    };*/


}
