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
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.nlopez.smartlocation.SmartLocation;
import ru.wtg.whereaminow.helpers.ContinueDialog;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.holders.AddressViewHolder;
import ru.wtg.whereaminow.holders.ButtonViewHolder;
import ru.wtg.whereaminow.holders.CameraViewHolder;
import ru.wtg.whereaminow.holders.DrawerViewHolder;
import ru.wtg.whereaminow.holders.FabViewHolder;
import ru.wtg.whereaminow.holders.FacebookViewHolder;
import ru.wtg.whereaminow.holders.MapButtonsViewHolder;
import ru.wtg.whereaminow.holders.MarkerViewHolder;
import ru.wtg.whereaminow.holders.MenuViewHolder;
import ru.wtg.whereaminow.holders.MessagesViewHolder;
import ru.wtg.whereaminow.holders.SavedLocationsViewHolder;
import ru.wtg.whereaminow.holders.SensorsViewHolder;
import ru.wtg.whereaminow.holders.SnackbarViewHolder;
import ru.wtg.whereaminow.holders.TrackViewHolder;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

import static ru.wtg.whereaminow.State.ACTIVITY_CREATE;
import static ru.wtg.whereaminow.State.ACTIVITY_DESTROY;
import static ru.wtg.whereaminow.State.ACTIVITY_PAUSE;
import static ru.wtg.whereaminow.State.ACTIVITY_RESULT;
import static ru.wtg.whereaminow.State.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.PREPARE_OPTIONS_MENU;
import static ru.wtg.whereaminow.State.TRACKING_JOIN;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_LOCATION_SINGLE;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_DAY;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_NIGHT;
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
import static ru.wtg.whereaminowserver.helpers.Constants.RESPONSE_TOKEN;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_DISMISSED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_JOINED;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private Intent intentService;
    private State state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        state = State.getInstance();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        state.registerEntityHolder(new FabViewHolder(this).setView(findViewById(R.id.fab_layout)));
        state.registerEntityHolder(new FacebookViewHolder());
        state.registerEntityHolder(new DrawerViewHolder(this).setViewAndToolbar(findViewById(R.id.drawer_layout),toolbar).setCallback(onNavigationDrawerCallback));

        intentService = new Intent(MainActivity.this, WhereAmINowService.class);

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
        state.clearViewHolders();
    }

    @Override
    public void onBackPressed() {
        DrawerViewHolder holder = (DrawerViewHolder) state.getEntityHolder(DrawerViewHolder.TYPE);
        if (holder != null && holder.isDrawerOpen()) {
            holder.closeDrawer();
        } else {
            super.onBackPressed();
            state.fire(ACTIVITY_DESTROY);
        }
    }


    private boolean day = true;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        state.getMe().fire(CREATE_OPTIONS_MENU, menu);
        state.fire(CREATE_OPTIONS_MENU, menu);

        /*menu.findItem(R.id.switch_day_night).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                state.fire((day = !day) ? REQUEST_MODE_DAY : REQUEST_MODE_NIGHT);

                return true;
            }
        });*/

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        state.getMe().fire(PREPARE_OPTIONS_MENU, menu);
        state.fire(PREPARE_OPTIONS_MENU, menu);
        return true;
    }

    @Override
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

    }

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
                    System.out.println("nav_settings");
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
//            checkPermissions(REQUEST_PERMISSION_LOCATION,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION});
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
        System.out.println("onMapReadyPermitted");

        if(!SmartLocation.with(MainActivity.this).location().state().locationServicesEnabled()) return;

        new MapButtonsViewHolder(mapFragment,findViewById(R.id.sv_users));
        state.registerEntityHolder(new SavedLocationsViewHolder(this).setMap(map));
        state.registerEntityHolder(new MenuViewHolder(this));
        state.registerEntityHolder(new TrackViewHolder().setMap(map));
        state.registerEntityHolder(new ButtonViewHolder(this).setLayout((LinearLayout) findViewById(R.id.layout_users)));
        state.registerEntityHolder(new MarkerViewHolder(this).setMap(map));
        state.registerEntityHolder(new AddressViewHolder().setCallback(new SimpleCallback<String>() {
            @Override
            public void call(String text) {
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle(text);
                }
            }
        }));
        state.registerEntityHolder(CameraViewHolder.getInstance(this,0)
                .setMap(map).setScaleView((MapScaleView) findViewById(R.id.scale_view)));
        state.registerEntityHolder(new MessagesViewHolder(MainActivity.this));
        state.registerEntityHolder(new SnackbarViewHolder(getApplicationContext()).setLayout(findViewById(R.id.fab_layout)));
        state.registerEntityHolder(new SensorsViewHolder(this).setMap(map));

        state.getUsers().setMe();

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
//                    state.myTracking.locationUpdatedListener.onLocationUpdated(loc);
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

        Uri data = newIntent.getData();
        newIntent.setData(null);
        if(data != null){
            String tokenId = data.getEncodedPath().replaceFirst("/track/","");

            if (!state.tracking() || !tokenId.equals(state.getToken())) {
                intentService.putExtra("mode", "join");
                intentService.putExtra("token", tokenId);
                intentService.putExtra("host", data.getHost());
                startService(intentService);
                state.fire(TRACKING_JOIN);
            }
        } else if(!state.tracking()) {
            String trackingUri = state.getStringPreference(TRACKING_URI, null);
            String tokenId = state.getStringPreference(RESPONSE_TOKEN, null);
            if(trackingUri != null && tokenId != null){
                intentService.putExtra("mode", "join");
                intentService.putExtra("token", tokenId);

                Uri uri = Uri.parse(trackingUri);
                intentService.putExtra("host", uri.getHost());
                startService(intentService);
                state.fire(TRACKING_JOIN);
            }
        }
        System.out.println("TRACKINGI:"+state.getStringPreference(TRACKING_URI, null)+":"+state.getStringPreference(RESPONSE_TOKEN, null));

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
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
