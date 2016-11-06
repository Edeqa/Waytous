package ru.wtg.whereaminow;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ru.wtg.whereaminow.helpers.FabMenu;
import ru.wtg.whereaminow.helpers.InviteSender;
import ru.wtg.whereaminow.helpers.MyCamera;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.State;
import ru.wtg.whereaminow.helpers.UserButtons;
import ru.wtg.whereaminow.helpers.Utils;

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
    private UserButtons userButtons;

    private ArrayList<MyCamera> cameras = new ArrayList<>();

    private boolean serviceBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        userButtons = new UserButtons(getApplicationContext());
        userButtons.setLayout((LinearLayout) findViewById(R.id.layout_users));
        userButtons.hide();

        intent = new Intent(MainActivity.this, WhereAmINowService.class);
        if (!serviceBound) bindService(intent, serviceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(BROADCAST);
        System.out.println("REGREC");
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPermissions(REQUEST_PERMISSION_LOCATION_ONRESUME,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("UNREGREC");

        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            serviceBound = false;
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_location:
                System.out.println("action_save_location");
                snackbar.setText("Location saved.").setDuration(Snackbar.LENGTH_LONG).setAction("Edit", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("edit location");
                    }
                }).show();
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

        checkPermissions(REQUEST_PERMISSION_LOCATION,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        System.out.println("onRequestPermissionsResult=" + requestCode + ":");
        for (int i : grantResults) {
            System.out.println("RES=" + i);
        }

        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
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
                if(enableLocationManager()) {
                    if (state.tracking()) userButtons.show();
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


    public GoogleMap getMap(){
        return map;
    }

    public void onMapReadyPermitted() {
        System.out.println("onMapReadyPermitted");
        if(!enableLocationManager()) return;

        MyCamera.setMap(map);
        MyUser.setContext(getApplicationContext());
        MyUser.setMap(map);

        adjustButtonsPositions();

        Location lastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
        cameras.add(new MyCamera(getApplicationContext()));

        state.getUsers().setMe();
        if (lastLocation != null) {
            state.getUsers().getMe().showDraft(lastLocation);
            state.getUsers().getMe().setMyCamera(cameras.get(0));

            cameras.get(0).setLocation(lastLocation).update().force();
            locationListener.onLocationChanged(lastLocation);
        }
        userButtons.setOnClickCallback(new UserButtons.Callback() {
            @Override
            public void call(MyUser marker) {
                marker.setMyCamera(cameras.get(0));
            }
        });

        state.getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
            @Override
            public void call(Integer number, MyUser marker) {
                marker.update();
            }
        });
        userButtons.synchronizeWith(state.getUsers());

        map.setOnMarkerClickListener(onMarkerClickListener);
        map.setOnCameraMoveStartedListener(cameras.get(0).onCameraMoveStartedListener);
        map.setOnCameraMoveListener(cameras.get(0).onCameraMoveListener);
        map.setOnCameraIdleListener(cameras.get(0).onCameraIdleListener);
        map.setOnCameraMoveCanceledListener(cameras.get(0).onCameraMoveCanceledListener);
//        map.setOnMapClickListener(onMapClickListener);


        map.setBuildingsEnabled(true);
        map.setIndoorEnabled(true);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(!DEBUGGING) return;
                try {
                    Location location = state.getUsers().getMe().getLocation();
                    location.setLatitude(latLng.latitude);
                    location.setLongitude(latLng.longitude);
                    locationListener.onLocationChanged(location);
                    state.myTracking.locationListener.onLocationChanged(location);
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

        Intent intent2 = getIntent();
        Uri data = intent2.getData();
        if(data != null){
            String tokenId = data.getEncodedPath().replaceFirst("/track/","");

            if(state.tracking() && tokenId.equals(state.getToken())) {

            } else {
                intent.putExtra("mode", "join");
                intent.putExtra("token", tokenId);
                startService(intent);
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

//                        state.getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
//                            @Override
//                            public void call(Integer number, final MyUser marker) {
//                                marker.hide();
//                            }
//                        });


                        if(!state.disconnected()) {
//                            onFabClick(R.id.fab_stop_tracking);
//                            userButtons.hide();

                            snackbar.setText("You have been disconnected.").setAction("Reconnect", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    onFabClick(100);
                                }
                            }).show();
                        }
                        userButtons.hide();
                        state.getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser marker) {
                                marker.hide();
                            }
                        });

                        break;
                    case RESPONSE_STATUS_ACCEPTED:
                        snackbar.dismiss();

//                        userButtons.setMyId(state.getUsers().getMe().getId());

                        if (o.has(RESPONSE_TOKEN)) {
                            new InviteSender(MainActivity.this).send(state.getToken());
                        }
                        if (o.has(RESPONSE_NUMBER)) {
                            userButtons.setMyNumber(o.getInt(RESPONSE_NUMBER));
//                            userButtons.add( )setMyId(state.getNumber());
                        }
                        if (o.has(RESPONSE_INITIAL)) {
                            state.getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser marker) {
                                    marker.update();
//                                    userButtons.add(number,marker);
                                }
                            });
                        }

                        userButtons.synchronizeWith(state.getUsers());
                        userButtons.show();
                        break;
                    case RESPONSE_STATUS_ERROR:
                        userButtons.hide();

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
                            userButtons.synchronizeWith(state.getUsers()).show();
                        }
                        if(o.has(USER_JOINED)) {
                            int number = o.getInt(USER_JOINED);
                            state.getUsers().forUser(number,new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser marker) {
                                    marker.update();
//                                    userButtons.add(number,marker);
                                }
                            });
                            userButtons.synchronizeWith(state.getUsers()).show();
                        }
                        if(o.has(USER_PROVIDER)){
                            final Location location = Utils.jsonToLocation(o);
                            int number = o.getInt(USER_NUMBER);
                            state.getUsers().forUser(number,new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser marker) {
                                    marker.addLocation(location).update();
                                }
                            });
                        }

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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATES_DELAY, 1, locationListener);
            return true;
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            System.out.println("onLocationChanged");

            state.getUsers().forMe(new MyUsers.Callback() {
                @Override
                public void call(Integer number, MyUser marker) {
                    if(marker != null) {
                        marker.addLocation(location).update();
                    }
                }
            });
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
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION});
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
                    new InviteSender(MainActivity.this).send(state.getToken());
                    break;
                case R.id.fab_stop_tracking:
                    System.out.println("fab_stop_tracking");
                    fabButtons.close(true);
                    snackbar.dismiss();
                    state.getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser marker) {
                            marker.hide();
                            userButtons.remove(number);
                        }
                    });
                    intent.putExtra("mode", "stop");
                    startService(intent);
                    break;
                case R.id.fab_cancel_tracking:
                    System.out.println("fab_cancel_tracking");
                    fabButtons.close(true);
                    snackbar.dismiss();
                    state.getUsers().forAllUsersExceptMe(new MyUsers.Callback() {
                        @Override
                        public void call(Integer number, MyUser marker) {
                            marker.hide();
                            userButtons.remove(number);
                        }
                    });
                    intent.putExtra("mode", "cancel");
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
    };

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
                if (cameras.size() > 0) {
                    cameras.get(0).onMyLocationButtonClickListener.onMyLocationButtonClick();
                }
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
            final MyUser[] m = new MyUser[1];
            state.getUsers().forAllUsers(new MyUsers.Callback() {
                @Override
                public void call(Integer number, MyUser user) {
//                    System.out.println("LOOKING MARKER:"+number+":"+marker.getId()+":"+user.getMarker().getId());
                    if (marker.getId().equals(user.getMarker().getId())) {
                        m[0] = user;
                    }
                }
            });
            if (m[0] != null && m[0].getMyCamera() != null) {
                m[0].getMyCamera().nextOrientation();
            }
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
        for (int i = 0; i < grants.length; i++) {
        }
        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    permissions.toArray(new String[permissions.size()]),
                    requestCode);
            System.out.println("checkPermission:" + requestCode + ":" + permissionsForCheck + ":" + permissions);
            return false;
        } else {
            onRequestPermissionsResult(requestCode, permissionsForCheck, grants);
            return true;
        }
    }


}
