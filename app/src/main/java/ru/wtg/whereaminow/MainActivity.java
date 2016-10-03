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
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import ru.wtg.whereaminow.helpers.FabMenu;
import ru.wtg.whereaminow.helpers.MyCamera;
import ru.wtg.whereaminow.helpers.MyMarker;
import ru.wtg.whereaminow.helpers.MyWebSocketClient;
import ru.wtg.whereaminow.helpers.State;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    public final static String BROADCAST_ACTION = "ru.wtg.whereaminow.whereaminowservice";
    public final static String SERVER_URL = "wss://192.168.56.1:8080";
    public final static int REQUEST_PERMISSION_LOCATION = 1;

    private DrawerLayout drawer;
    private LocationManager locationManager = null;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Intent intent;
    private FabMenu buttons;
    private Snackbar snackbar;
    private URI serverUri;
    private State state;

    private ArrayList<MyMarker> markers = new ArrayList<>();
    private ArrayList<MyCamera> cameras = new ArrayList<>();

    private boolean serviceBound;
    private boolean locationManagerDefined = false;
    private WebSocketClient webSocketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            serverUri = new URI(SERVER_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        webSocketClient = new MyWebSocketClient(serverUri);
        state = State.getInstance();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        buttons = (FabMenu) findViewById(R.id.fab);

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(receiver, intentFilter);
        webSocketClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableLocationManager();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationManagerDefined = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        webSocketClient.close();
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
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
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
        System.out.println("onRequestPermissionsResult=" + requestCode+":");
        for(int i:grantResults){
            System.out.println("RES="+i);
        }

        switch (requestCode) {
            case REQUEST_PERMISSION_LOCATION:
                int res = 0;
                for(int i:grantResults){
                    res += i;
                }
                System.out.println("PERM="+res);
                if(res==0){
                    onMapReadyPermitted();
                } else {
                    return;
                }
                break;
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, final IBinder binder) {
            serviceBound = true;
            System.out.println("onServiceConnected");

            if(!checkPermissions(MainActivity.REQUEST_PERMISSION_LOCATION,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION})) return;

      /*      if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            }*/
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

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            System.err.println("NOT locationManager.isProviderEnabled");
            return;
        }
        enableLocationManager();

        cameras.add(new MyCamera(getApplicationContext()));
        markers.add(new MyMarker(getApplicationContext()).setMyCamera(cameras.get(0)));

        mMap.setOnMarkerClickListener(onMarkerClickListener);
        mMap.setOnCameraMoveStartedListener(cameras.get(0).onCameraMoveStartedListener);
        mMap.setOnCameraMoveListener(cameras.get(0).onCameraMoveListener);
        mMap.setOnCameraIdleListener(cameras.get(0).onCameraIdleListener);
        mMap.setOnCameraMoveCanceledListener(cameras.get(0).onCameraMoveCanceledListener);
//        mMap.setOnMapClickListener(onMapClickListener);

        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (lastLocation == null)
            lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastLocation == null)
            lastLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (lastLocation != null) {
            markers.get(0).showDraft(lastLocation);
            cameras.get(0).setLocation(lastLocation).update();
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
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


        connectWebSocket();


    }

    //MARK - temp
    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("wss://192.168.56.1:8080");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new MyWebSocketClient(uri);
//        webSocketClient.connect();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("onReceive");
        }
    };

    private void enableLocationManager() {
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
//            onMapReadyPermitted();
            if (!locationManagerDefined && locationManager != null && locationListener != null) {
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    System.err.println("NOT locationManager.isProviderEnabled");
                    return;
                }

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
                locationManagerDefined = true;
            }
        }



    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            System.out.println("onLocationChanged");

            if (!buttons.isInitialized()) {
                adjustButtonsPositions();
//        buttons.setOnMenuButtonLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                System.out.println("fab.onLongClick");
//                return false;
//            }
//        });

                buttons.initAndSetOnClickListener(onFabClickListener);
                initSnackbar();

            }

            if (markers.size() > 0) {
                markers.get(0).addLocation(location);
            }
            if (cameras.size() > 0) {
                cameras.get(0).setLocation(location).update();


//                new ServletPostAsyncTask().execute(new Pair<Context, String>(getApplicationContext(), cameras.get(0).toString()));
            }

            /*if(webSocketClient.getReadyState() == WebSocket.READYSTATE.OPEN) {
                webSocketClient.send(""+location);
            } else if(webSocketClient.getReadyState() == WebSocket.READYSTATE.CLOSED) {
                webSocketClient = new MyWebSocketClient(serverUri);
                System.out.println("WEBSOCKET:CLOSED_RECONNECT");
                webSocketClient.connect();
            }*/

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
                    if(state.waiting()){
                        System.out.println("fab.onClick:start");

                        intent.putExtra("mode","start");
                        startService(intent);

                        snackbar.setText("Starting tracking...").setAction("Cancel", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                System.out.println("snackbar.onClick");
                                intent.putExtra("mode","stop");
                                startService(intent);
                                buttons.close(true);
                            }
                        }).show();
                    } else {
                        System.out.println("fab.onClick:toggle");
                        if (!buttons.isOpened()) {
                            buttons.removeAllMenuButtons();
                            buttons.addMenuButton(buttons.startTrackingAndSendLink);
                            buttons.addMenuButton(buttons.navigate);
                            buttons.addMenuButton(buttons.cancelTracking);
                        }
                        buttons.toggle(true);
                    }
                    break;
                case R.id.fab_send_link:
                    System.out.println("fab_send_link");
                    buttons.close(true);
                    break;
                case R.id.fab_stop_tracking:
                    System.out.println("fab_stop_tracking");
                    buttons.close(true);
                    snackbar.dismiss();
                    intent.putExtra("mode","stop");
                    startService(intent);
                    break;
                case R.id.fab_cancel_tracking:
                    System.out.println("fab_cancel_tracking");
                    buttons.close(true);
                    snackbar.dismiss();
                    intent.putExtra("mode","stop");
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
    private boolean checkPermissions(int requestCode, String[] permissionsForCheck){
        ArrayList<String> permissions = new ArrayList<>();
        int[] grants = new int[permissionsForCheck.length];
        for(int i=0;i<permissionsForCheck.length;i++){
            if(ActivityCompat.checkSelfPermission(MainActivity.this, permissionsForCheck[i]) != PackageManager.PERMISSION_GRANTED){
                permissions.add(permissionsForCheck[i]);
                grants[i] = PackageManager.PERMISSION_DENIED;
            } else {
                grants[i] = PackageManager.PERMISSION_GRANTED;
            }
        }
        for(int i=0;i<grants.length;i++){
        }
        if(permissions.size()>0){
            ActivityCompat.requestPermissions(MainActivity.this,
                    permissions.toArray(new String[permissions.size()]),
                    requestCode);
            System.out.println("checkPermission:"+requestCode+":"+permissionsForCheck+":"+permissions);
            return false;
        } else {
            onRequestPermissionsResult(requestCode,permissionsForCheck,grants);
            return true;
        }
    }


}
