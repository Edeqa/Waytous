package ru.wtg.whereaminow.holders;

import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ru.wtg.whereaminow.MainActivity;
import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.abstracts.AbstractView;
import ru.wtg.whereaminow.abstracts.AbstractViewHolder;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;
import ru.wtg.whereaminow.helpers.Utils;

import static ru.wtg.whereaminow.State.EVENTS.CHANGE_NUMBER;
import static ru.wtg.whereaminow.State.EVENTS.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.EVENTS.MAP_MY_LOCATION_BUTTON_CLICKED;
import static ru.wtg.whereaminow.State.EVENTS.PREPARE_FAB;
import static ru.wtg.whereaminow.State.EVENTS.SELECT_USER;
import static ru.wtg.whereaminow.State.EVENTS.UNSELECT_USER;
import static ru.wtg.whereaminow.holders.GpsHolder.REQUEST_LOCATION_SINGLE;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_DAY;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_NIGHT;
import static ru.wtg.whereaminow.interfaces.Tracking.TRACKING_URI;
import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;

/**
 * Created 11/20/16.
 */
@SuppressWarnings("deprecation")
public class CameraViewHolder extends AbstractViewHolder<CameraViewHolder.CameraUpdateView> {

    public static final String TYPE = "camera";

    public static final String UPDATE_CAMERA = "update_camera";
    public static final String CAMERA_UPDATED = "camera_updated";
    public static final String CAMERA_ZOOM_IN = "camera_zoom_in";
    public static final String CAMERA_ZOOM_OUT = "camera_zoom_out";
    public static final String CAMERA_ZOOM = "camera_zoom";

    static final String CAMERA_NEXT_ORIENTATION = "change_next_orientation";

    private final static int CAMERA_ORIENTATION_NORTH = 0;
    private final static int CAMERA_ORIENTATION_DIRECTION = 1;
    private final static int CAMERA_ORIENTATION_PERSPECTIVE = 2;
    private final static int CAMERA_ORIENTATION_STAY = 3;
    private final static int CAMERA_ORIENTATION_USER = 4;
    private static final float CAMERA_DEFAULT_ZOOM = 15.f;
    private static final float CAMERA_DEFAULT_TILT = 0.f;
    private static final float CAMERA_DEFAULT_BEARING = 0.f;
    private final static int CAMERA_ORIENTATION_LAST = 2;
    private final static boolean CAMERA_ORIENTATION_PERSPECTIVE_NORTH = true;

    private final static String LATITUDE = "latitude";
    private final static String LONGITUDE = "longitude";
    private final static String ALTITUDE = "altitude";
    private final static String TILT = "tilt";
    private final static String BEARING = "bearing";
    private final static String ZOOM = "zoom";
    private final static String ORIENTATION = "orientation";
    private final static String PREVIOUS_ORIENTATION = "previous_orientation";
    private final static String PERSPECTIVE_NORTH = "perspective_north";

    private final AppCompatActivity context;
    private CameraUpdateView cameraUpdate;
    private MapScaleView scaleView;
    private GoogleMap map;
    private int padding;
    private boolean moveFromHardware = false;
    private boolean canceled = false;
    private boolean initialStart = true;

    public CameraViewHolder(MainActivity context) {
        this.context = context;
        padding = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);

        setMap(context.getMap());
        setScaleView((MapScaleView) context.findViewById(R.id.scale_view));
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public CameraUpdateView create(MyUser myUser) {
        if (myUser == null || myUser.getLocation() == null) return null;
        return new CameraUpdateView(myUser);
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case REQUEST_MODE_DAY:
                scaleView.setColor(Color.BLACK);
                break;
            case REQUEST_MODE_NIGHT:
                scaleView.setColor(Color.WHITE);
                break;
            case UPDATE_CAMERA:
                if(State.getInstance().getUsers().getCountAllSelected()==0){
                    State.getInstance().getMe().fire(SELECT_USER);
                }
                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                    @Override
                    public void call(Integer number, MyUser myUser) {
                        if(myUser.getProperties().isActive() && myUser.getProperties().isSelected()) {
                            setCameraUpdate((CameraUpdateView) myUser.getEntity(CameraViewHolder.TYPE));
                            update();
                        }
                    }
                });
                break;
            case PREPARE_FAB:
                final FabViewHolder fab = (FabViewHolder) object;
                if(State.getInstance().getUsers().getCountActive() > 1 && State.getInstance().getUsers().getCountSelected() < State.getInstance().getUsers().getCountActive()) {
                    fab.add(R.string.fit_to_screen, R.drawable.ic_fullscreen_black_24dp).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fab.close(true);
                            State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                                @Override
                                public void call(Integer number, MyUser myUser) {
                                    if(myUser.getProperties().isActive()) {
                                        myUser.fire(SELECT_USER);
                                    }
                                }
                            });
                        }
                    });
                }
                break;
            case MAP_MY_LOCATION_BUTTON_CLICKED:
                if(cameraUpdate == null) return false;
                State.getInstance().fire(REQUEST_LOCATION_SINGLE);
                if(cameraUpdate.orientation == CAMERA_ORIENTATION_PERSPECTIVE) {
                    cameraUpdate.perspectiveNorth = !cameraUpdate.perspectiveNorth;
                }
                cameraUpdate.myUser.fire(SELECT_USER);
                break;
        }
        return true;
    }

    private void update() {
        if(cameraUpdate == null) return;
        CameraUpdate camera;
        MyUser user;
//                System.out.println("PERSPECTIVE:"+((CameraUpdateView) State.getInstance().getMe().getEntity(CameraViewHolder.TYPE)).perspectiveNorth);
        if(State.getInstance().getUsers().getCountAllSelected()>1){
            double lat1=0f,lat2=0f,lng1=0f,lng2=0f;
            //noinspection LoopStatementThatDoesntLoop
            for(Map.Entry<Integer,MyUser> entry: State.getInstance().getUsers().getUsers().entrySet()){
                user = entry.getValue();
                if(user.getProperties().isSelected() && user.getLocation() != null){
                    lat1 = user.getLocation().getLatitude();
                    lat2 = user.getLocation().getLatitude();
                    lng1 = user.getLocation().getLongitude();
                    lng2 = user.getLocation().getLongitude();
                    break;
                }
            }
            for(Map.Entry<Integer,MyUser> entry: State.getInstance().getUsers().getUsers().entrySet()){
                user = entry.getValue();
                if(user.getProperties().isSelected() && user.getLocation() != null){
                    lat1 = Math.min(lat1, user.getLocation().getLatitude());
                    lat2 = Math.max(lat2, user.getLocation().getLatitude());
                    lng1 = Math.min(lng1, user.getLocation().getLongitude());
                    lng2 = Math.max(lng2, user.getLocation().getLongitude());
                }
            }
            LatLng latLngLB = new LatLng(lat1, lng1);
            LatLng latLngRT = new LatLng(lat2, lng2);

            camera = CameraUpdateFactory.newLatLngBounds(Utils.reduce(new LatLngBounds(latLngLB, latLngRT), 1.1), padding);
        } else {
            camera = CameraUpdateFactory.newCameraPosition(cameraUpdate.getCameraPosition().build());
        }
        moveFromHardware = true;

        try {
            if(initialStart) {
                if(!State.getInstance().tracking_disabled() || State.getInstance().getStringPreference(TRACKING_URI, null) != null) {
                    map.moveCamera(camera);
                } else {
                    map.animateCamera(camera, LOCATION_UPDATES_DELAY, null);
                }
            } else {
                map.animateCamera(camera, LOCATION_UPDATES_DELAY, null);
            }
        } catch(IllegalStateException e) {
            e.printStackTrace();
        }
        initialStart = false;

    }

    public void move(){
        update();
    }

    public CameraViewHolder setMap(GoogleMap map) {
        this.map = map;

        map.setOnCameraMoveStartedListener(onCameraMoveStartedListener);
        map.setOnCameraMoveListener(onCameraMoveListener);
        map.setOnCameraIdleListener(onCameraIdleListener);
        map.setOnCameraMoveCanceledListener(onCameraMoveCanceledListener);

        return this;
    }

    private void setCameraUpdate(CameraUpdateView cameraUpdate) {
        this.cameraUpdate = cameraUpdate;
    }

    private CameraViewHolder setScaleView(MapScaleView scaleView) {
        this.scaleView = scaleView;
        if(Locale.US.equals(Locale.getDefault())) {
            scaleView.setIsMiles(true);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    class CameraUpdateView extends AbstractView implements Serializable{

        static final long serialVersionUID =-6395904747332820026L;

        private transient MyUser myUser;
        private transient CameraPosition.Builder position;
        private transient Location location;

        private float zoom;
        private float bearing;
        private float tilt;
        private int number;
        private int orientation;
        private int previousOrientation;
        private boolean orientationChanged;
        private boolean perspectiveNorth;

        CameraUpdateView(MyUser myUser) {
            this.myUser = myUser;
            number = myUser.getProperties().getNumber();

            HashMap<String, Double> props = (HashMap<String, Double>) myUser.getProperties().loadFor(TYPE);

            double latitude;
            double longitude;
                try {
                    tilt = props.get(TILT).floatValue();
                    bearing = props.get(BEARING).floatValue();
                    zoom = props.get(ZOOM).floatValue();
                    orientation = props.get(ORIENTATION).intValue();
                    previousOrientation = props.get(PREVIOUS_ORIENTATION).intValue();
                    perspectiveNorth = props.get(PERSPECTIVE_NORTH).intValue() == 1;
                    latitude = props.get(LATITUDE);
                    longitude = props.get(LONGITUDE);
                }catch (Exception e) {
                    tilt = CAMERA_DEFAULT_TILT;
                    bearing = CAMERA_DEFAULT_BEARING;
                    zoom = CAMERA_DEFAULT_ZOOM;
                    orientation = CAMERA_ORIENTATION_NORTH;
                    previousOrientation = CAMERA_ORIENTATION_NORTH;
                    perspectiveNorth = true;
                    latitude = myUser.getLocation().getLatitude();
                    longitude = myUser.getLocation().getLongitude();
            }

            padding = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);

            position = new CameraPosition.Builder().bearing(bearing).tilt(tilt).zoom(zoom);
            position.target(new LatLng(latitude, longitude));

            if(myUser.getProperties().isSelected()) {
                myUser.fire(SELECT_USER);
            }
        }

        @Override
        public void remove() {
            if(myUser.getLocation() == null) return;
            HashMap<String, Double> props = new HashMap<>();
            props.put(TILT, tilt * 1.);
            props.put(BEARING, bearing * 1.);
            props.put(ZOOM, zoom * 1.);
            props.put(ORIENTATION, orientation*1.);
            props.put(PREVIOUS_ORIENTATION, previousOrientation*1.);
            props.put(PERSPECTIVE_NORTH, perspectiveNorth ? 1. : 0.);
            props.put(LATITUDE, myUser.getLocation().getLatitude());
            props.put(LONGITUDE, myUser.getLocation().getLongitude());

            myUser.getProperties().saveFor(TYPE, props);
        }

        @Override
        public boolean dependsOnLocation() {
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {
            if(!myUser.getProperties().isSelected()){
                return;
            }
            this.location = location;
            switch (orientation){
                case CAMERA_ORIENTATION_NORTH:
//                    if(orientationChanged) {
//                    }
                    position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                    position.bearing(0);
                    position.tilt(0);
                    break;
                case CAMERA_ORIENTATION_DIRECTION:
//                    if(orientationChanged) {
//                    }
                    position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                    position.bearing(location.getBearing());
                    position.tilt(0);
                    break;
                case CAMERA_ORIENTATION_PERSPECTIVE:
                    if(orientationChanged) {
                        position.tilt(60);
                    }
                    position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                    position.bearing(location.getBearing());

                    /*DisplayMetrics metrics = new DisplayMetrics();
                    context.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                    Projection projection = map.getProjection();

                    Point cameraCenter = projection.toScreenLocation(Utils.latLng(location));

                    float tiltFactor = (90 - map.getCameraPosition().tilt) / 90;

                    System.out.println("METRICS:"+metrics);
                    System.out.println("VISIBLE:"+projection.getVisibleRegion());
                    System.out.println("POINT:"+cameraCenter);

                    cameraCenter.x -= metrics.widthPixels / 2;// - cameraCenter.x;
                    cameraCenter.y -= metrics.heightPixels *.2;// / 2 * tiltFactor;

                    System.out.println("POINT2:"+cameraCenter);

                    LatLng fixLatLng = projection.fromScreenLocation(cameraCenter);
                    position.target(fixLatLng);*/

                    break;
                case CAMERA_ORIENTATION_STAY:
                    position.target(map.getCameraPosition().target);
                    break;
            }
            position.zoom(zoom);
            /*if(orientationChanged) {
                switch (orientation) {
                    case CAMERA_ORIENTATION_NORTH:
                    case CAMERA_ORIENTATION_DIRECTION:
                        map.setPadding(0, 0, 0, 0);
                        break;
                    case CAMERA_ORIENTATION_PERSPECTIVE:
                        map.setPadding(0, (int) (context.getResources().getDisplayMetrics().heightPixels / 2), 0, 0);
                        break;
                }
            }*/
            orientationChanged = false;
            CameraViewHolder.this.setCameraUpdate(this);
            CameraViewHolder.this.update();
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch (event) {
                case SELECT_USER:
                    orientation = previousOrientation;
                    orientationChanged = true;
                    onChangeLocation(myUser.getLocation());
                    break;
                case UNSELECT_USER:
                    if(State.getInstance().getUsers().getCountAllSelected()==0){
                        State.getInstance().getMe().fire(SELECT_USER);
                    }

                    if(State.getInstance().getUsers().getCountSelected()==1){
                        State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser user) {
                                if(user.getProperties().isSelected()) {
                                    user.fire(SELECT_USER);
                                }
                            }
                        });
                    } else {
                        CameraViewHolder.this.update();
                    }
                    break;
                case CAMERA_NEXT_ORIENTATION:
                    if(orientation > CAMERA_ORIENTATION_LAST) {
                        orientation = previousOrientation;
                    } else if(orientation == CAMERA_ORIENTATION_LAST){
                        orientation = CAMERA_ORIENTATION_NORTH;
                    } else {
                        orientation++;
                    }
                    if(orientation == CAMERA_ORIENTATION_DIRECTION && myUser.getLocation().getBearing() == 0) {
                        orientation++;
                    }
                    orientationChanged = true;
                    previousOrientation = orientation;
                    setOrientation(orientation);
                    onChangeLocation(myUser.getLocation());
                    break;
//                case ADJUST_ZOOM:
//                    zoom = CAMERA_DEFAULT_ZOOM;
//                    onChangeLocation(myUser.getLocation());
//                    break;
                case CREATE_CONTEXT_MENU:
                    Menu menu = (Menu) object;
                    menu.findItem(R.id.action_pin).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(SELECT_USER);
                            return false;
                        }
                    }).setIcon(R.drawable.ic_select_all_black_24dp).setVisible(!myUser.getProperties().isSelected());
                    if(State.getInstance().getUsers().getCountSelected()!=1 && myUser.getProperties().isSelected()) {
                        menu.add(0, R.string.unpin, Menu.NONE, R.string.unpin).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(UNSELECT_USER, 0);
                                return false;
                            }
                        }).setIcon(R.drawable.ic_unselect_black_24dp);
                    }
                    if(myUser.isUser()) {
                        menu.add(0, R.string.change_orientation, Menu.NONE, R.string.change_orientation).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(CAMERA_NEXT_ORIENTATION);
                                return false;
                            }
                        }).setIcon(R.drawable.ic_compass_black_24dp);
                    }
                    break;
                case CHANGE_NUMBER:
                    int newNumber = (int) object;
                    if(newNumber == number) break;
                    number = newNumber;
                    break;
                case CAMERA_ZOOM:
                    float z;// = CAMERA_DEFAULT_ZOOM;
                    if(object != null) {
                        z = (float) object;
                    } else {
                        float[] zooms = new float[]{CAMERA_DEFAULT_ZOOM, CAMERA_DEFAULT_ZOOM + 2, CAMERA_DEFAULT_ZOOM + 4, CAMERA_DEFAULT_ZOOM - 2, CAMERA_DEFAULT_ZOOM - 4, CAMERA_DEFAULT_ZOOM};
                        int index = -1;
                        for(int i=0; i<zooms.length; i++){
                            if(zooms[i] == zoom) {
                                index = i;
                                break;
                            }
                        }
                        if(index >= 0) {
                            z = zooms[index+1];
                        } else {
                            z = CAMERA_DEFAULT_ZOOM;
                        }
                    }
                    zoom = z;
                    onChangeLocation(myUser.getLocation());
//                    State.getInstance().fire(UPDATE_CAMERA);
                    break;
                case CAMERA_ZOOM_IN:
                    z = 1;
                    if(object != null) {
                        z = (float) object;
                    }
                    zoom = zoom + z;
                    onChangeLocation(myUser.getLocation());
//                    State.getInstance().fire(UPDATE_CAMERA);
                    break;
                case CAMERA_ZOOM_OUT:
                    z = -1;
                    if(object != null) {
                        z = (float) object;
                    }
                    zoom = zoom + z;
                    onChangeLocation(myUser.getLocation());
//                    State.getInstance().fire(UPDATE_CAMERA);
                    break;
            }
            return true;
        }

        public int getOrientation() {
            return orientation;
        }

        public void setOrientation(int orientation) {
            if(this.orientation <= CAMERA_ORIENTATION_LAST){
                previousOrientation = this.orientation;
            }
            this.orientation = orientation;
            orientationChanged = true;

//            myUser.getProperties().saveFor(TYPE, this);
        }

        CameraPosition.Builder getCameraPosition(){
            return position;
        }

        public Location getLocation() {
            return location;
        }

    }

    private GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener = new GoogleMap.OnCameraMoveStartedListener() {
        @Override
        public void onCameraMoveStarted(int i) {
            if(scaleView != null) {
                scaleView.update(map.getProjection(), map.getCameraPosition());
            }
//            if(cameraUpdate.zoom != map.getCameraPosition().zoom)
//                moveFromHardware = true;

//            System.out.println("onCameraMoveStarted");
        }
    };

    private GoogleMap.OnCameraIdleListener onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
        @Override
        public void onCameraIdle() {
            if(scaleView != null) {
                scaleView.update(map.getProjection(), map.getCameraPosition());
            }

            if(cameraUpdate == null || State.getInstance().getUsers().getCountSelected() != 1){
                State.getInstance().fire(CAMERA_UPDATED);
                return;
            }
            if(canceled && cameraUpdate.zoom != map.getCameraPosition().zoom){
                cameraUpdate.setOrientation(cameraUpdate.previousOrientation);
                moveFromHardware = true;
            } else if(cameraUpdate.zoom != map.getCameraPosition().zoom){
                moveFromHardware = true;
            }

            cameraUpdate.zoom = map.getCameraPosition().zoom;
            cameraUpdate.bearing = map.getCameraPosition().bearing;
            cameraUpdate.tilt = map.getCameraPosition().tilt;

//            System.out.println("onCameraIdle,orientation:"+orientation+":"+moveFromHardware);
            if(!moveFromHardware){
                cameraUpdate.setOrientation(CAMERA_ORIENTATION_STAY);
            }

            moveFromHardware = false;
            canceled = false;
            State.getInstance().fire(CAMERA_UPDATED, cameraUpdate.myUser);
//            System.out.println("onCameraIdle");
        }
    };

    private GoogleMap.OnCameraMoveCanceledListener onCameraMoveCanceledListener = new GoogleMap.OnCameraMoveCanceledListener() {
        @Override
        public void onCameraMoveCanceled() {
            if(scaleView != null) {
                scaleView.update(map.getProjection(), map.getCameraPosition());
            }

//            System.out.println("onCameraMoveCanceled");
            if(cameraUpdate == null || State.getInstance().getUsers().getCountSelected() != 1) return;
            if(cameraUpdate.zoom == map.getCameraPosition().zoom){
                cameraUpdate.setOrientation(CAMERA_ORIENTATION_STAY);
                moveFromHardware = false;
                canceled = true;
            }
        }
    };

    private GoogleMap.OnCameraMoveListener onCameraMoveListener = new GoogleMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {
//            System.out.println("onCameraMove");
            if(scaleView != null) {
                scaleView.update(map.getProjection(), map.getCameraPosition());
            }
        }
    };

}