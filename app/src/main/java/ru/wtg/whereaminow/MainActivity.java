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
import ru.wtg.whereaminow.helpers.MyMarker;
import ru.wtg.whereaminow.helpers.State;
import ru.wtg.whereaminow.helpers.UserButtons;

import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST_ACTION_DISCONNECTED;
import static ru.wtg.whereaminowserver.helpers.Constants.BROADCAST_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.DEBUGGING;
import static ru.wtg.whereaminowserver.helpers.Constants.REQUEST_PERMISSION_LOCATION;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_MESSAGE;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ACCEPTED;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_STATUS_ERROR;
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_ACTIVE;
import static ru.wtg.whereaminowserver.helpers.Constants.TRACKING_DISABLED;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private DrawerLayout drawer;
    private LocationManager locationManager;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Intent intent;
    private FabMenu buttons;
    private Snackbar snackbar;
    private State state;
    private UserButtons userButtons;

    private ArrayList<MyMarker> markers = new ArrayList<>();
    private ArrayList<MyCamera> cameras = new ArrayList<>();

    private boolean serviceBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        state = State.getInstance();
        state.setMainContext(this);
        state.checkDeviceId();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        buttons = (FabMenu) findViewById(R.id.fab);

        userButtons = new UserButtons();
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
        if(enableLocationManager()) {
            if (state.tracking()) userButtons.show();
        }
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
                mMap.setTrafficEnabled(!mMap.isTrafficEnabled());
                break;
            case R.id.nav_satellite:
                if (mMap.getMapType() != GoogleMap.MAP_TYPE_SATELLITE)
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                else
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.nav_terrain:
                if (mMap.getMapType() != GoogleMap.MAP_TYPE_TERRAIN)
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                else
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
                    buttons.setPlus();
                    onMapReadyPermitted();
                } else {
                    buttons.initAndSetOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            buttons.hideMenu(true);
                            checkPermissions(REQUEST_PERMISSION_LOCATION,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
                        }
                    });
                    buttons.setGpsOff();
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

        MyMarker.setMap(mMap);
        MyCamera.setMap(mMap);

        adjustButtonsPositions();

        cameras.add(new MyCamera(getApplicationContext()));
        markers.add(new MyMarker(getApplicationContext()).setMyCamera(cameras.get(0)));

        mMap.setOnMarkerClickListener(onMarkerClickListener);
        mMap.setOnCameraMoveStartedListener(cameras.get(0).onCameraMoveStartedListener);
        mMap.setOnCameraMoveListener(cameras.get(0).onCameraMoveListener);
        mMap.setOnCameraIdleListener(cameras.get(0).onCameraIdleListener);
        mMap.setOnCameraMoveCanceledListener(cameras.get(0).onCameraMoveCanceledListener);
//        mMap.setOnMapClickListener(onMapClickListener);

        Location lastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
        if (lastLocation != null) {
            markers.get(0).showDraft(lastLocation);
            cameras.get(0).setLocation(lastLocation).update();
            locationListener.onLocationChanged(lastLocation);
        }

        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if(!DEBUGGING) return;
                try {
                    Location location = markers.get(0).getLocation();
                    location.setLatitude(latLng.latitude);
                    location.setLongitude(latLng.longitude);
                    locationListener.onLocationChanged(location);
                    state.myTracking.locationListener.onLocationChanged(location);
                }catch(Exception e){
                    System.out.println("Error setOnMapClickListener: "+e.getMessage());
                }
            }
        });
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);


        Intent intent2 = getIntent();
        Uri data = intent2.getData();
        if(data != null){
            intent.putExtra("mode", "join");
            String query = data.getEncodedPath().replaceFirst("/track/","");
            intent.putExtra("token", query);
            startService(intent);
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
                    case BROADCAST_ACTION_DISCONNECTED:
                        View v = new View(context);
                        v.setId(R.id.fab_stop_tracking);
                        onFabClickListener.onClick(v);
                        state.setStatus(TRACKING_DISABLED);
                        snackbar.dismiss();
                        userButtons.hide();
                        break;
                    case RESPONSE_STATUS_ACCEPTED:
                        state.setToken(o.getString(RESPONSE_TOKEN));
                        state.setStatus(TRACKING_ACTIVE);
                        snackbar.dismiss();
                        userButtons.show();
                        new InviteSender(MainActivity.this).send(state.getToken());

                        break;
                    case RESPONSE_STATUS_ERROR:
                        String message = o.getString(RESPONSE_MESSAGE);
                        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                        state.setStatus(TRACKING_DISABLED);
                        snackbar.dismiss();
                        userButtons.hide();
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1, locationListener);
            return true;
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            System.out.println("onLocationChanged");

            if (!buttons.isInitialized()) {

                buttons.initAndSetOnClickListener(onFabClickListener);
                initSnackbar();

            }

            if (markers.size() > 0) {
                markers.get(0).addLocation(location);
            }
            if (cameras.size() > 0) {
                cameras.get(0).setLocation(location).update();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("onProviderDisabled:" + provider);
            buttons.hideMenuButton(true);
        }

        public void onProviderEnabled(String provider) {
            System.out.println("onProviderEnabled:" + provider);
            buttons.showMenuButton(true);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("onStatusChanged:" + provider);
        }
    };

    private View.OnClickListener onFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case -1:
                    if (state.rejected()) {
                        checkPermissions(REQUEST_PERMISSION_LOCATION,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION});
                    } else if (state.waiting()) {
                        System.out.println("fab.onClick:start");

                        intent.putExtra("mode", "start");
                        startService(intent);

                        snackbar.setText("Starting tracking...").setAction("Cancel", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                System.out.println("snackbar.onClick");
                                intent.putExtra("mode", "stop");
                                startService(intent);
                                buttons.close(true);
                            }
                        }).show();
                    } else if (state.tracking()) {
                        System.out.println("fab.onClick:toggle");
                        if (!buttons.isOpened()) {
                            buttons.removeAllMenuButtons();
                            buttons.addMenuButton(buttons.sendLink);
//                            buttons.addMenuButton(buttons.navigate);
                            buttons.addMenuButton(buttons.cancelTracking);
                        }
                        buttons.toggle(true);
                    }
                    break;
                case R.id.fab_send_link:
                    System.out.println("fab_send_link");
                    buttons.close(true);
                    new InviteSender(MainActivity.this).send(state.getToken());
                    break;
                case R.id.fab_stop_tracking:
                    System.out.println("fab_stop_tracking");
                    buttons.close(true);
                    snackbar.dismiss();
                    intent.putExtra("mode", "stop");
                    startService(intent);
                    break;
                case R.id.fab_cancel_tracking:
                    System.out.println("fab_cancel_tracking");
                    buttons.close(true);
                    snackbar.dismiss();
                    intent.putExtra("mode", "cancel");
                    startService(intent);
                    break;
                case R.id.fab_switch_to_friend:
                    System.out.println("fab_switch_to_friend");
                    buttons.close(true);
                    break;
                case R.id.fab_switch_to_me:
                    System.out.println("fab_switch_to_me");
                    buttons.toggle(true);
                    break;
                case R.id.fab_navigate:
                    System.out.println("fab_navigate");
                    buttons.close(true);
                    break;
                case R.id.fab_show_us:
                    System.out.println("fab_show_us");
                    buttons.close(true);
                    break;
                case R.id.fab_messages:
                    System.out.println("fab_messages");
                    buttons.close(true);
                    break;
                case R.id.fab_split_screen:
                    System.out.println("fab_split_screen");
                    buttons.close(true);
                    break;
            }
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
        public boolean onMarkerClick(Marker marker) {
            MyMarker m = null;
            for (MyMarker x : markers) {
                if (marker.getId().equals(x.getMarker().getId())) {
                    m = x;
                    break;
                }
            }
            if (m != null) {
                m.getMyCamera().nextOrientation();
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
