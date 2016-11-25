package ru.wtg.whereaminow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import ru.wtg.whereaminow.helpers.FabMenu;
import ru.wtg.whereaminow.helpers.InviteSender;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.holders.AddressViewHolder;
import ru.wtg.whereaminow.holders.ButtonViewHolder;
import ru.wtg.whereaminow.holders.CameraViewHolder;
import ru.wtg.whereaminow.holders.MarkerViewHolder;
import ru.wtg.whereaminow.holders.MenuViewHolder;
import ru.wtg.whereaminow.holders.TrackViewHolder;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.helpers.MyUser.ASSIGN_TO_CAMERA;
import static ru.wtg.whereaminow.helpers.MyUser.CAMERA_NEXT_ORIENTATION;
import static ru.wtg.whereaminow.helpers.MyUser.HIDE_TRACK;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_CHANGE_NAME;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_HIDE_ALL_TRACKS;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_HIDE_TRACK;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_NAVIGATE;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_PIN;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_PIN_ALL;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_SHOW_ALL_TRACKS;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_SHOW_TRACK;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_UNPIN;
import static ru.wtg.whereaminow.helpers.MyUser.SHOW_TRACK;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.DEBUGGING;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_PERMISSION_LOCATION;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_PERMISSION_LOCATION_ONRESUME;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_INITIAL;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_MESSAGE;
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
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private DrawerLayout drawer;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private Intent intent;
    private FabMenu fabButtons;
    private Snackbar snackbar;
    private State state;

//    private boolean serviceBound;
    private ButtonViewHolder buttons;
    private CameraViewHolder camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        System.out.println("onCreate:Activity");

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        state = State.getInstance();
        state.setActivity(this);
        state.checkDeviceId();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fabButtons = (FabMenu) findViewById(R.id.fab);

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
            checkPermissions(REQUEST_PERMISSION_LOCATION_ONRESUME,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
        } else {
            onRequestPermissionsResult(REQUEST_PERMISSION_LOCATION_ONRESUME, new String[]{}, new int[]{});
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        SmartLocation.with(MainActivity.this).location().stop();
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
        state.getMe().fire(MENU_ITEM_CHANGE_NAME, menu.findItem(R.id.action_set_my_name));
        state.getMe().fire(MENU_ITEM_PIN_ALL, menu.findItem(R.id.action_fit_to_screen));

        menu.findItem(R.id.action_show_tracks).setVisible(false);
        menu.findItem(R.id.action_hide_tracks).setVisible(false);
        state.getUsers().forAllUsers(new MyUsers.Callback() {
            @Override
            public void call(Integer number, MyUser myUser) {
                if(myUser.getProperties().isActive()) {
                    myUser.fire(MENU_ITEM_SHOW_ALL_TRACKS, menu.findItem(R.id.action_show_tracks));
                    myUser.fire(MENU_ITEM_HIDE_ALL_TRACKS, menu.findItem(R.id.action_hide_tracks));
                }
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.user_menu, menu);

        int number = (int) v.getTag();
        MyUser user = state.getUsers().getUsers().get(number);
        menu.setHeaderTitle(user.getProperties().getName());

        user.fire(MENU_ITEM_NAVIGATE, menu.findItem(R.id.action_navigate));
        user.fire(MENU_ITEM_PIN, menu.findItem(R.id.action_pin));
        user.fire(MENU_ITEM_UNPIN, menu.findItem(R.id.action_unpin));
        user.fire(MENU_ITEM_SHOW_TRACK, menu.findItem(R.id.action_show_track));
        user.fire(MENU_ITEM_HIDE_TRACK, menu.findItem(R.id.action_hide_track));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_location:
//                System.out.println("TRY");
//                System.out.println("TRY1:"+(1/0));
                System.out.println("action_save_location");
                snackbar.setText("Location saved.").setDuration(Snackbar.LENGTH_LONG).setAction("Edit", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("edit location");
                    }
                }).show();
                return true;
            case R.id.action_show_tracks:
                state.getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.fire(SHOW_TRACK);
                    }
                });
                break;
            case R.id.action_hide_tracks:
                state.getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.fire(HIDE_TRACK);
                    }
                });
                break;

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
        checkPermissions(REQUEST_PERMISSION_LOCATION,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    // permissionsForCheck = android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION
    // requestCode = MainActivity.REQUEST_PERMISSION_LOCATION
    private boolean checkPermissions(int requestCode, String[] permissionsForCheck) {
        ArrayList<String> permissions = new ArrayList<>();
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
            ActivityCompat.requestPermissions(MainActivity.this,
                    permissions.toArray(new String[permissions.size()]),
                    requestCode);
            System.out.println("checkPermission:" + requestCode);
            Thread.dumpStack();
            return false;
        } else {
            onRequestPermissionsResult(requestCode, permissionsForCheck, grants);
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        System.out.println("onRequestPermissionsResult=" + requestCode + ":");
        for (int i : grantResults) {
            System.out.println("RES=" + i);
        }

        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                state.setGpsAccessRequested(true);
                int res = 0;
                for (int i : grantResults) {
                    res += i;
                }
                System.out.println("PERM=" + res);
                if (res == 0) {
                    fabButtons.setPlus();
                    onMapReadyPermitted();
                } else {
                    fabButtons.initAndSetOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fabButtons.hideMenu(true);
                            checkPermissions(REQUEST_PERMISSION_LOCATION,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
                        }
                    });
                    fabButtons.setGpsOff();
                    Toast.makeText(getApplicationContext(), "GPS access is not granted.", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case REQUEST_PERMISSION_LOCATION_ONRESUME:
                state.setGpsAccessRequested(true);
                res = 0;
                for (int i : grantResults) {
                    res += i;
                }
                System.out.println("PERM=" + res);
                if (res == 0) {
                    fabButtons.setPlus();
//                    if(enableLocationManager()) {
                    if(SmartLocation.with(MainActivity.this).location().state().locationServicesEnabled()) {
                        state.getUsers().forAllUsers(new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.createViews();
                            }
                        });
                        state.getUsers().forMe(new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.fire(MyUser.ASSIGN_TO_CAMERA, 0);
                            }
                        });
                        if(state.tracking()){
                            if(buttons != null) buttons.show();
                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }
                    }
                } else {
                    fabButtons.initAndSetOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fabButtons.hideMenu(true);
                            checkPermissions(REQUEST_PERMISSION_LOCATION,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
                        }
                    });
                    fabButtons.setGpsOff();
                    Toast.makeText(getApplicationContext(), "GPS access is not granted.", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
    }

    public void onMapReadyPermitted() {
        System.out.println("onMapReadyPermitted");
//        if(!enableLocationManager()) return;
        if(!SmartLocation.with(MainActivity.this).location().state().locationServicesEnabled()) return;

        state.registerEntityHolder(buttons = new ButtonViewHolder(this).setLayout((LinearLayout) findViewById(R.id.layout_users)));
        state.registerEntityHolder(new MarkerViewHolder(this).setMap(map));
        state.registerEntityHolder(new MenuViewHolder(this));
        state.registerEntityHolder(new TrackViewHolder().setMap(map));
        state.registerEntityHolder(new AddressViewHolder().setCallback(new SimpleCallback<String>() {
            @Override
            public void call(String text) {
                getSupportActionBar().setSubtitle(text);
            }
        }));
        state.registerEntityHolder(camera = CameraViewHolder.getInstance(getApplicationContext(),0)
                .setMap(map).setScaleView((MapScaleView) findViewById(R.id.scaleView)));

        adjustButtonsPositions();

        state.getUsers().setMe();
        SmartLocation.with(MainActivity.this).location().oneFix().start(locationUpdateListener);

        map.setOnMarkerClickListener(onMarkerClickListener);
//        map.setOnMapClickListener(onMapClickListener);

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
//                    locationListener.onLocationChanged(loc);
                    locationUpdateListener.onLocationUpdated(loc);
                    state.myTracking.locationListener.onLocationChanged(loc);
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

        if (!fabButtons.isInitialized()) {
            fabButtons.initAndSetOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onFabClick(view.getId());
                }
            });
            initSnackbar();
        }

        state.getUsers().forAllUsers(new MyUsers.Callback() {
            @Override
            public void call(Integer number, MyUser myUser) {
                myUser.createViews();
            }
        });

        if(state.tracking() || state.getUsers().getCountActive() > 1) {
            camera.move();
        }
        if(state.tracking()) {
            buttons.show();
        } else {
            enableLocationManager();
        }

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Implement a listener to receive updates
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float mLightQuantity = event.values[0];
                System.out.println("SENSOR:"+mLightQuantity);
                Toast.makeText(MainActivity.this,"Light: "+mLightQuantity,Toast.LENGTH_SHORT).show();
                if(state.tracking()) {

                    MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.mapstyle_night);
                    map.setMapStyle(style);

                    setTheme(R.style.NightTheme);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                System.out.println("ACCURACY:"+i+":"+sensor.getName());
            }
        };

        // Register the listener with the light sensor -- choosing
        // one of the SensorManager.SENSOR_DELAY_* constants.
        sensorManager.registerListener(
                listener, lightSensor, SensorManager.SENSOR_DELAY_UI);

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
                snackbar.setText("Joining tracking...").setAction("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        intent.putExtra("mode", "stop");
                        startService(intent);
                        fabButtons.close(true);
                    }
                }).show();
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
                        if(!state.disconnected()) {
                            snackbar.setText("Disconnected. Trying to reconnect").setAction("Cancel", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    onFabClick(R.id.fab_stop_tracking);
                                }
                            }).show();
                        }
                        break;
                    case RESPONSE_STATUS_ACCEPTED:
                        snackbar.dismiss();
                        SmartLocation.with(MainActivity.this).location().stop();
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        if (o.has(RESPONSE_TOKEN)) {
                            new InviteSender(MainActivity.this).send("https://"+state.myTracking.getHost()+":8080/track/"+state.getToken());
                        }
                        if (o.has(RESPONSE_NUMBER)) {
                            state.getUsers().forMe(new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
                                    myUser.createViews();
                                    myUser.fire(ASSIGN_TO_CAMERA,0);
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

                        buttons.show();
                        break;
                    case RESPONSE_STATUS_ERROR:
                        enableLocationManager();
                        buttons.hide();

                        String message = o.getString(RESPONSE_MESSAGE);
                        if(message == null) message = "Failed join to tracking.";
                        snackbar.setText(message).setAction("New tracking", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onFabClick(-1);
                            }
                        }).show();
                        break;
                    case RESPONSE_STATUS_UPDATED:
                        if(o.has(USER_DISMISSED)) {
                            int number = o.getInt(USER_DISMISSED);
                            state.getUsers().forUser(number,new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, final MyUser myUser) {
                                    myUser.removeViews();
                                    if(camera.getCountAssigned()==0){
                                        state.getMe().fire(ASSIGN_TO_CAMERA,0);
                                    }
                                }
                            });
                        }
                        if(o.has(USER_JOINED)) {
                            int number = o.getInt(USER_JOINED);
                            state.getUsers().forUser(number,new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
                                    myUser.createViews();
                                }
                            });
                        }
                        if(o.has(USER_PROVIDER)){
                        }
                        break;
                    case RESPONSE_STATUS_STOPPED:
                        enableLocationManager();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        buttons.hide();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void enableLocationManager() {
        LocationParams.Builder builder = new LocationParams.Builder()
                .setAccuracy(LocationAccuracy.HIGH).setDistance(1).setInterval(1 * 1000);

        SmartLocation.with(this).location().continuous().config(builder.build()).start(locationUpdateListener);
        if(map != null) map.setMapStyle(null);
        setTheme(R.style.DayTheme);
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
                state.getMe().addLocation(location);
            }
        }
    };

    private void onFabClick(int id) {
        switch (id) {
            case -1:
                if (state.rejected()) {
                    checkPermissions(REQUEST_PERMISSION_LOCATION,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
                } else if (state.disconnected()) {
                    System.out.println("fab.onClick:start");

                    intent.putExtra("mode", "start");
                    startService(intent);

                    snackbar.setText("Starting tracking...").setAction("Cancel", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            System.out.println("snackbar.onClick");
                            intent.putExtra("mode", "stop");
                            startService(intent);
                            fabButtons.close(true);
                            buttons.hide();
                        }
                    }).show();
                } else if (state.connecting()) {
                    if (!fabButtons.isOpened()) {
                        fabButtons.removeAllMenuButtons();
//                            fabButtons.addMenuButton(fabButtons.navigate);
                        fabButtons.addMenuButton(fabButtons.cancelTracking);
                    }
                    fabButtons.toggle(true);
                } else if (state.tracking()) {
                    System.out.println("fab.onClick:toggle");
                    if (!fabButtons.isOpened()) {
                        fabButtons.removeAllMenuButtons();
                        fabButtons.addMenuButton(fabButtons.sendLink);
//                            fabButtons.addMenuButton(fabButtons.navigate);
                        fabButtons.addMenuButton(fabButtons.cancelTracking);
                    }
                    fabButtons.toggle(true);
                }
                break;
//                case R.id.fab_start_tracking_and_send_link:
//                    System.out.println("fab_start_tracking_and_send_link");
//
//                    System.out.println("snackbar.onClick");
//                    intent.putExtra("mode", "start");
//                    startService(intent);
//                    fabButtons.close(true);
//
//                    break;
            case R.id.fab_send_link:
                System.out.println("fab_send_link");
                fabButtons.close(true);
                new InviteSender(MainActivity.this).send("https://"+state.myTracking.getHost()+":8080/track/"+state.getToken());
                break;
            case R.id.fab_stop_tracking:
            case R.id.fab_cancel_tracking:
                System.out.println("fab_stop_tracking");
                fabButtons.close(true);
                snackbar.dismiss();
                state.getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.removeViews();
                    }
                });
                buttons.hide();
                intent.putExtra("mode", "stop");
                startService(intent);
                break;
            case R.id.fab_switch_to_friend:
                System.out.println("fab_switch_to_friend");
                fabButtons.close(true);
                break;
            case R.id.fab_switch_to_me:
                System.out.println("fab_switch_to_me");
                fabButtons.toggle(true);
                break;
            case R.id.fab_navigate:
                System.out.println("fab_navigate");
                fabButtons.close(true);
                break;
            case R.id.fab_show_us:
                System.out.println("fab_show_us");
                fabButtons.close(true);
                break;
            case R.id.fab_messages:
                System.out.println("fab_messages");
                fabButtons.close(true);
                break;
            case R.id.fab_split_screen:
                System.out.println("fab_split_screen");
                fabButtons.close(true);
                break;
            case 100:
                System.out.println("fab_reconnect");
                intent.putExtra("mode", "join");
                intent.putExtra("token", state.getToken());
                startService(intent);
                break;
        }
    }

    private void adjustButtonsPositions() {
        ViewGroup v1 = (ViewGroup) mapFragment.getView();
        ViewGroup v2 = (ViewGroup) v1.getChildAt(0);
        ViewGroup v3 = (ViewGroup) v2.getChildAt(2);
        View myLocationButton = v3.getChildAt(0);

        myLocationButton.setVisibility(View.VISIBLE);
        myLocationButton.setEnabled(true);
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.onMyLocationButtonClickListener.onMyLocationButtonClick();
            }
        });

        View zoomButtons = v3.getChildAt(2);
        int positionWidth = zoomButtons.getLayoutParams().width;
        int positionHeight = zoomButtons.getLayoutParams().height;

        RelativeLayout.LayoutParams zoomParams = new RelativeLayout.LayoutParams(positionWidth, positionHeight);
        int margin = positionWidth / 5;
        zoomParams.setMargins(margin, 0, 0, margin);
        zoomParams.addRule(RelativeLayout.BELOW, myLocationButton.getId());
        zoomParams.addRule(RelativeLayout.ALIGN_LEFT, myLocationButton.getId());
        zoomButtons.setLayoutParams(zoomParams);
    }

    private GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            int number = (int) marker.getTag();
            state.getUsers().forUser(number, new MyUsers.Callback() {
                @Override
                public void call(Integer number, MyUser myUser) {
                    myUser.fire(CAMERA_NEXT_ORIENTATION);
                }
            });
            return true;
        }
    };

/*    GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            System.out.println("ONMAPCLICK");
        }
    };*/

    private void initSnackbar() {
        View fabLayout = findViewById(R.id.fab_layout);
        snackbar = Snackbar.make(fabLayout, "Starting...", Snackbar.LENGTH_INDEFINITE);

        snackbar.getView().setAlpha(.8f);
        snackbar.setAction("Action", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("SNACKBAR ACTION");
            }
        });
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                initSnackbar();
            }
        });
        /*snackbar.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
                System.out.println("SNACKBAR CLICK");
            }
        });*/
    }

}
