package ru.wtg.whereaminow;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.wtg.whereaminow.helpers.AddressViewHolder;
import ru.wtg.whereaminow.helpers.ButtonViewHolder;
import ru.wtg.whereaminow.helpers.CameraViewHolder;
import ru.wtg.whereaminow.helpers.FabMenu;
import ru.wtg.whereaminow.helpers.InviteSender;
import ru.wtg.whereaminow.helpers.MarkerViewHolder;
import ru.wtg.whereaminow.helpers.GlobalExceptionHandler;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.SimpleCallback;
import ru.wtg.whereaminow.helpers.State;

import static ru.wtg.whereaminow.helpers.MyUser.ASSIGN_TO_CAMERA;
import static ru.wtg.whereaminow.helpers.MyUser.CAMERA_NEXT_ORIENTATION;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_HIDE_TRACK;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_PIN;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_SHOW_TRACK;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_UNPIN;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.DEBUGGING;
import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;
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
import static ru.wtg.whereaminowserver.helpers.Constants.USER_NUMBER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private DrawerLayout drawer;
    private LocationManager locationManager;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private Intent intent;
    private FabMenu fabButtons;
    private Snackbar snackbar;
    private State state;
    private Button contextButton;

    private boolean serviceBound;
    private ButtonViewHolder buttons;
    private CameraViewHolder camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new GlobalExceptionHandler(MainActivity.this);

        setContentView(R.layout.activity_main);
        System.out.println("onCreate:Activity");

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        state = State.getInstance();
        state.setApplication(getApplicationContext());
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
        if (!serviceBound) bindService(intent, serviceConnection, BIND_AUTO_CREATE);

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
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        menu.findItem(R.id.action_show_tracks).setVisible(false);
        menu.findItem(R.id.action_hide_tracks).setVisible(false);
        state.getUsers().forAllUsers(new MyUsers.Callback() {
            @Override
            public void call(Integer number, MyUser myUser) {
                if(myUser.isActive()) {
                    /*if (myUser.isShowTrack()) {
                        menu.findItem(R.id.action_hide_tracks).setVisible(true);
                    } else {
                        menu.findItem(R.id.action_show_tracks).setVisible(true);
                    }*/
                }
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);

        contextButton = (Button) v;
        int number = (int) contextButton.getTag();
        MyUser user = state.getUsers().getUsers().get(number);
        menu.setHeaderTitle(user.getName());

        user.fire(MENU_ITEM_PIN, menu.findItem(R.id.action_pin));
        user.fire(MENU_ITEM_UNPIN, menu.findItem(R.id.action_unpin));
        user.fire(MENU_ITEM_SHOW_TRACK, menu.findItem(R.id.action_show_track));
        user.fire(MENU_ITEM_HIDE_TRACK, menu.findItem(R.id.action_hide_track));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_navigate:
                MyUser user = state.getUsers().getUsers().get(contextButton.getTag());
                System.out.println("NAVIGATE:"+user.getName());
                Uri uri = Uri.parse("google.navigation:q=" + String.valueOf(user.getLocation().getLatitude())
                        + "," + String.valueOf(user.getLocation().getLongitude()));
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                try {
                    startActivity(intent);
                } catch(ActivityNotFoundException ex) {
                    try {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(unrestrictedIntent);
                    } catch(ActivityNotFoundException innerEx) {
                        Toast.makeText(getApplicationContext(), "Please install a navigation application.", Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
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
            case R.id.action_set_my_name:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Set my name");

                View layoutDialogSetMyName = getLayoutInflater().inflate(R.layout.dialog_set_my_name, null);

                builder.setView(layoutDialogSetMyName);
                final EditText etMyName = (EditText) layoutDialogSetMyName.findViewById(R.id.etMyName);
                String name = state.getStringPreference("my_name","");
                if(name != null && name.length()>0){
                    etMyName.setText(name);
                    builder.setNeutralButton("Remove", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            state.getUsers().setNameFor(state.getUsers().getMyNumber(),"");
                            state.setPreference("my_name",null);
                        }
                    });
                }
                builder.setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                state.getUsers().setNameFor(state.getUsers().getMyNumber(),etMyName.getText().toString());
                                state.setPreference("my_name",etMyName.getText().toString());
                            }
                        });

                builder.setNegativeButton(getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                System.out.println("CANCEL");
                            }
                        });

                builder.create().show();
                break;
            case R.id.action_show_tracks:
                state.getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
//                        myUser.setShowTrack(true);
//                        myUser.update();
                    }
                });
                break;
            case R.id.action_hide_tracks:
                state.getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
//                        myUser.setShowTrack(false);
//                        myUser.update();
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
                    if(enableLocationManager()) {
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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, final IBinder binder) {
            serviceBound = true;
            System.out.println("onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            System.out.println("onServiceDisconnected");
        }
    };

    public void onMapReadyPermitted() {
        System.out.println("onMapReadyPermitted");
        if(!enableLocationManager()) return;

        state.registerViewHolder(buttons = new ButtonViewHolder(this).setLayout((LinearLayout) findViewById(R.id.layout_users)));
        state.registerViewHolder(new MarkerViewHolder(getApplicationContext()).setMap(map));
        state.registerViewHolder(new AddressViewHolder().setCallback(new SimpleCallback<String>() {
            @Override
            public void call(String text) {
                getSupportActionBar().setSubtitle(text);
            }
        }));
        camera = CameraViewHolder.getInstance(getApplicationContext(),0).setMap(map).setScaleView((MapScaleView) findViewById(R.id.scaleView));
        state.registerViewHolder(camera);

        adjustButtonsPositions();

        Location lastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
        state.getUsers().setMe();
        if (lastLocation != null) {
            state.getMe().addLocation(lastLocation);//.showDraft(lastLocation);
        }

        map.setOnMarkerClickListener(onMarkerClickListener);
        map.setOnCameraMoveStartedListener(camera.onCameraMoveStartedListener);
        map.setOnCameraMoveListener(camera.onCameraMoveListener);
        map.setOnCameraIdleListener(camera.onCameraIdleListener);
        map.setOnCameraMoveCanceledListener(camera.onCameraMoveCanceledListener);
//        map.setOnMapClickListener(onMapClickListener);

        map.setBuildingsEnabled(true);
        map.setIndoorEnabled(true);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(!DEBUGGING) return;
                try {
                    Location location = state.getMe().getLocation();
                    Location loc = new Location("gps");
                    loc.setLatitude(latLng.latitude);
                    loc.setLongitude(latLng.longitude);
                    loc.setAltitude(location.getAltitude());
                    loc.setAccuracy(location.getAccuracy());
                    loc.setBearing(location.getBearing());
                    loc.setSpeed(location.getSpeed());
                    loc.setTime(location.getTime());
                    locationListener.onLocationChanged(loc);
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
        state.getMe().fire(MyUser.ASSIGN_TO_CAMERA, 0);

        if(state.tracking()) buttons.show();

        Intent intent2 = getIntent();
        Uri data = intent2.getData();
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
        System.out.println("INTENT:"+data+":"+intent.getType());

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String r = intent.getStringExtra(BROADCAST_MESSAGE);
            System.out.println("onReceive:" + r);
            JSONObject o;
            try {
                o = new JSONObject(r);
                if (!o.has(RESPONSE_STATUS)) return;

                switch (o.getString(RESPONSE_STATUS)) {
                    case RESPONSE_STATUS_DISCONNECTED:
//                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        state.getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser myUser) {
                                myUser.removeViews();
                            }
                        });

                        if(!state.disconnected()) {
                            snackbar.setText("You have been disconnected.").setAction("Reconnect", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    onFabClick(100);
                                }
                            }).show();
                        }

                        break;
                    case RESPONSE_STATUS_ACCEPTED:
                        snackbar.dismiss();

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
                        buttons.hide();

                        String message = o.getString(RESPONSE_MESSAGE);
                        if(message == null) message = "Failed join to tracking.";
                        snackbar.setText(message).setAction("New tracking", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                onFabClick(-1);//R.id.fab_start_tracking_and_send_link);
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
                                    if(camera.getAssignedCount()==0){
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
                            /*int number = o.getInt(USER_NUMBER);
                            state.getUsers().forUser(number,new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
//                                    myUser.onChangeLocation();
                                }
                            });*/
                        }
                        break;
                    case RESPONSE_STATUS_STOPPED:
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        buttons.hide();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private boolean enableLocationManager() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            return false;
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATES_DELAY, 1, locationListener);
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATES_DELAY, 1, locationListener);
            return true;
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            if(!state.tracking()) {
                System.out.println("Activity:onLocationChanged");
                state.getUsers().forMe(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        myUser.addLocation(location);
                    }
                });
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("onProviderDisabled:" + provider);
            fabButtons.hideMenuButton(true);
        }

        public void onProviderEnabled(String provider) {
            System.out.println("onProviderEnabled:" + provider);
            fabButtons.showMenuButton(true);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("onStatusChanged:" + provider);
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
