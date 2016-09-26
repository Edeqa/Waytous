package ru.wtg.whereaminow;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

import ru.wtg.whereaminow.helpers.FabMenu;
import ru.wtg.whereaminow.helpers.MyCamera;
import ru.wtg.whereaminow.helpers.MyMarker;
import ru.wtg.whereaminow.helpers.RelativeLayoutMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    public final static String BROADCAST_ACTION = "ru.wtg.whereaminow.whereaminowservice";
    public final static int REQUEST_PERMISSION_LOCATION = 1;

    private LocationManager locationManager = null;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Intent intent;
    private FabMenu buttons;
    private ArrayList<MyMarker> markers = new ArrayList<>();
    private ArrayList<MyCamera> cameras = new ArrayList<>();

    private boolean serviceBound;
    private boolean locationManagerDefined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.initAndSetOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        buttons = (FabMenu) findViewById(R.id.fab);

        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableLocationManager();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationManagerDefined = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serviceBound){
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch(item.getItemId()) {
            case R.id.nav_start_and_share:
                break;
            case R.id.nav_settings:
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        intent = new Intent(MainActivity.this, WhereAmINowService.class);
        if (!serviceBound) bindService(intent, serviceConnection, BIND_AUTO_CREATE);

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        System.out.println("onRequestPermissionsResult=" + requestCode);
        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                onMapReadyPermitted();
                break;
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, final IBinder binder) {
            serviceBound = true;
            System.out.println("onServiceConnected");

            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        MainActivity.REQUEST_PERMISSION_LOCATION);
                System.out.println("checkSelfPermission");
            } else {
                onMapReadyPermitted();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
            System.out.println("onServiceDisconnected");
        }
    };

    public void onMapReadyPermitted() {
        System.out.println("onMapReadyPermitted");
        MyMarker.setMap(mMap);
        MyCamera.setMap(mMap);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            System.err.println("NOT locationManager.isProviderEnabled");
            return;
        }
        enableLocationManager();

        cameras.add(new MyCamera(getApplicationContext()));
        markers.add(new MyMarker(getApplicationContext()));
        markers.get(0).setMyCamera(cameras.get(0));

        mMap.setOnMarkerClickListener(onMarkerClickListener);
        mMap.setOnCameraMoveStartedListener(cameras.get(0).onCameraMoveStartedListener);
        mMap.setOnCameraMoveListener(cameras.get(0).onCameraMoveListener);
        mMap.setOnCameraIdleListener(cameras.get(0).onCameraIdleListener);
        mMap.setOnCameraMoveCanceledListener(cameras.get(0).onCameraMoveCanceledListener);
        mMap.setOnMapClickListener(onMapClickListener);

/*
        ((RelativeLayoutMap)findViewById(R.id.layout_map)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==0){
                    cameras.get(0).setOrientation(MyCamera.ORIENTATION_STAY);
                    System.out.println("motionEvent:"+motionEvent.getAction()+":"+motionEvent.getActionMasked());
                }
                return false;
            }
        });
*/

//        mMap.setMyLocationEnabled(true);
//        mMap.setOnMyLocationButtonClickListener(cameras.get(0).onMyLocationButtonClickListener);

        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if(lastLocation == null) lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(lastLocation == null) lastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if(lastLocation != null) {
            markers.get(0).showDraft(lastLocation);
            cameras.get(0).setLocation(lastLocation).update();
        }

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(lastLocation != null){
                locationListener.onLocationChanged(lastLocation);
            }
        }

        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                System.out.println("MAP CLICK");
            }
        });
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        System.out.println("onReceive");
        }
    };

    private void enableLocationManager() {
        if(!locationManagerDefined && locationManager != null && locationListener != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
            locationManagerDefined = true;
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            System.out.println("onLocationChanged");

            if(!buttons.isInitialized()) {
                adjustButtonsPositions();
                initFab();
            }

            if(markers.size()>0) {
                markers.get(0).addLocation(location);
            }
            if(cameras.size()>0) {
                cameras.get(0).setLocation(location).update();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("onProviderDisabled:"+provider);
            buttons.hideMenuButton(true);
        }

        public void onProviderEnabled(String provider) {
            System.out.println("onProviderEnabled:"+provider);
            buttons.showMenuButton(true);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("onStatusChanged:"+provider);
        }
    };

    private void initFab(){

        buttons.setOnMenuButtonLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                System.out.println("fab.onLongClick");
                return false;
            }
        });

        buttons.initAndSetOnClickListener(onFabClickListener);

    }

    private View.OnClickListener onFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case -1:
                    System.out.println("fab.onClick");

                    if(!buttons.isOpened()) {
                        buttons.removeAllMenuButtons();
                        buttons.addMenuButton(buttons.startTrackingAndSendLink);
                        buttons.addMenuButton(buttons.navigate);
                        buttons.addMenuButton(buttons.cancelTracking);
                    }
                    buttons.toggle(true);

                    break;
                case R.id.fab_start_tracking_and_send_link:
                    buttons.toggle(false);
                    System.out.println("fab_start_tracking_and_send_link");
                    Snackbar.make(findViewById(R.id.fab_layout), "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                    break;
                case R.id.fab_send_link:
                    buttons.toggle(false);
                    System.out.println("fab_send_link");
                    break;
                case R.id.fab_stop_tracking:
                    buttons.toggle(false);
                    System.out.println("fab_stop_tracking");
                    break;
                case R.id.fab_cancel_tracking:
                    buttons.toggle(false);
                    System.out.println("fab_cancel_tracking");
                    break;
                case R.id.fab_switch_to_friend:
                    buttons.toggle(false);
                    System.out.println("fab_switch_to_friend");
                    break;
                case R.id.fab_switch_to_me:
                    buttons.toggle(false);
                    System.out.println("fab_switch_to_me");
                    break;
                case R.id.fab_navigate:
                    buttons.toggle(false);
                    System.out.println("fab_navigate");
                    break;
                case R.id.fab_show_us:
                    buttons.toggle(false);
                    System.out.println("fab_show_us");
                    break;
                case R.id.fab_messages:
                    buttons.toggle(false);
                    System.out.println("fab_messages");
                    break;
                case R.id.fab_split_screen:
                    buttons.toggle(false);
                    System.out.println("fab_split_screen");
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
                if(cameras.size()>0){
                    cameras.get(0).setOrientation(cameras.get(0).getPreviousOrientation()).update();

                    System.out.println("onMyLocationButtonClick:"+cameras.get(0).getPreviousOrientation());
                }
            }
        });

        View zoomButtons =  v3.getChildAt(2);
        int positionWidth = zoomButtons.getLayoutParams().width;
        int positionHeight = zoomButtons.getLayoutParams().height;

        RelativeLayout.LayoutParams zoomParams = new RelativeLayout.LayoutParams(positionWidth,positionHeight);
        int margin = positionWidth/5;
        zoomParams.setMargins(margin, 0, 0, margin);
        zoomParams.addRule(RelativeLayout.BELOW, myLocationButton.getId());
        zoomParams.addRule(RelativeLayout.ALIGN_LEFT, myLocationButton.getId());
        zoomButtons.setLayoutParams(zoomParams);
    }

    public GoogleMap.OnMarkerClickListener onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick (Marker marker) {
            MyMarker m = null;
            for(MyMarker x:markers){
                if(marker.getId().equals(x.getMarker().getId())){
                    m = x;
                    break;
                }
            }
            if(m != null){
                m.getMyCamera().nextOrientation();
            }
            return true;
        }
    };


    GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            System.out.println("ONMAPCLICK");
        }
    };


}
