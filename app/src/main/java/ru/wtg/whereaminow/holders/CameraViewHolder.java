package ru.wtg.whereaminow.holders;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;

import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;

import static ru.wtg.whereaminow.State.CHANGE_NUMBER;
import static ru.wtg.whereaminow.State.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.SELECT_USER;
import static ru.wtg.whereaminow.State.UNSELECT_USER;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_LOCATION_SINGLE;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_DAY;
import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;

/**
 * Created 11/20/16.
 */
public class CameraViewHolder extends AbstractViewHolder<CameraViewHolder.CameraUpdateView> {
    public static final String TYPE = "camera";

    public static final String ADJUST_ZOOM = "adjust_zoom";
    public static final String UPDATE_CAMERA = "update_camera";

    public static final String CAMERA_NEXT_ORIENTATION = "change_next_orientation";

    private final static int CAMERA_ORIENTATION_NORTH = 0;
    private final static int CAMERA_ORIENTATION_DIRECTION = 1;
    private final static int CAMERA_ORIENTATION_PERSPECTIVE = 2;
    private final static int CAMERA_ORIENTATION_STAY = 3;
    private final static int CAMERA_ORIENTATION_USER = 4;
    private static final float CAMERA_DEFAULT_ZOOM = 15.f;
    private static final float CAMERA_DEFAULT_TILT = 0.f;
    private static final float CAMERA_DEFAULT_BEARING = 0.f;
    private final static int CAMERA_ORIENTATION_LAST = 2;

    private final static String LATITUDE = "latitude";
    private final static String LONGITUDE = "longitude";
    private final static String ALTITUDE = "altitude";
    private final static String TILT = "tilt";
    private final static String BEARING = "bearing";
    private final static String ZOOM = "zoom";
    private final static String ORIENTATION = "orientation";
    private final static String PREVIOUS_ORIENTATION = "previous_orientation";

    private static ArrayList<CameraViewHolder> instance = new ArrayList<>();

    private final AppCompatActivity context;
    private CameraUpdateView cameraUpdate;
    private MapScaleView scaleView;
    private GoogleMap map;
    private int cameraNumber;
    private int padding;
    private boolean moveFromHardware = false;
    private boolean canceled = false;

    private CameraViewHolder(AppCompatActivity context) {
        this.context = context;
        padding = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
    }

    public static CameraViewHolder getInstance(AppCompatActivity context, int number) {
        if (instance.size() > number) {
            return instance.get(number);
        } else {
            CameraViewHolder holder = new CameraViewHolder(context);
            holder.setCameraNumber(number);
            instance.add(number, holder);
            return holder;
        }
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

    private void setCameraNumber(int cameraNumber) {
        this.cameraNumber = cameraNumber;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case REQUEST_MODE_DAY:
//                scaleView.
                break;
            case UPDATE_CAMERA:
                update(false);
                break;
        }
        return true;
    }

    private void update(boolean move) {
        if(cameraUpdate == null) return;
        CameraUpdate camera;
        MyUser user;
        if(State.getInstance().getUsers().getCountSelected()>1){
            double lat1=0f,lat2=0f,lng1=0f,lng2=0f;
            //noinspection LoopStatementThatDoesntLoop
            for(Map.Entry<Integer,MyUser> entry: State.getInstance().getUsers().getUsers().entrySet()){
                user = entry.getValue();
                if(user.getProperties().isSelected()){
                    lat1 = user.getLocation().getLatitude();
                    lat2 = user.getLocation().getLatitude();
                    lng1 = user.getLocation().getLongitude();
                    lng2 = user.getLocation().getLongitude();
                    break;
                }
            }
            for(Map.Entry<Integer,MyUser> entry: State.getInstance().getUsers().getUsers().entrySet()){
                user = entry.getValue();
                if(user.getProperties().isSelected()){
                    lat1 = Math.min(lat1,user.getLocation().getLatitude());
                    lat2 = Math.max(lat2,user.getLocation().getLatitude());
                    lng1 = Math.min(lng1,user.getLocation().getLongitude());
                    lng2 = Math.max(lng2,user.getLocation().getLongitude());
                }
            }
            LatLng latLngLB = new LatLng(lat1, lng1);
            LatLng latLngRT = new LatLng(lat2, lng2);

            camera = CameraUpdateFactory.newLatLngBounds(new LatLngBounds(latLngLB, latLngRT), padding);
        } else {
            camera = CameraUpdateFactory.newCameraPosition(cameraUpdate.getCameraPosition().build());
        }
        moveFromHardware = true;

        try {
            if(move){
                map.moveCamera(camera);
            } else {
                map.animateCamera(camera, LOCATION_UPDATES_DELAY, null);
            }
        } catch(IllegalStateException e){
            e.printStackTrace();
        }
    }

    public void move(){
        update(true);
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

    public CameraViewHolder setScaleView(MapScaleView scaleView) {
        this.scaleView = scaleView;
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

        CameraUpdateView(MyUser myUser) {
            this.myUser = myUser;
            number = myUser.getProperties().getNumber();

            HashMap<String, Double> props = (HashMap<String, Double>) myUser.getProperties().loadFor(TYPE);

            double latitude;
            double longitude;
            if (props == null) {
                tilt = CAMERA_DEFAULT_TILT;
                bearing = CAMERA_DEFAULT_BEARING;
                zoom = CAMERA_DEFAULT_ZOOM;
                orientation = CAMERA_ORIENTATION_NORTH;
                previousOrientation = CAMERA_ORIENTATION_NORTH;
                latitude = myUser.getLocation().getLatitude();
                longitude = myUser.getLocation().getLongitude();
            } else {
                tilt = props.get(TILT).floatValue();
                bearing = props.get(BEARING).floatValue();
                zoom = props.get(ZOOM).floatValue();
                orientation = props.get(ORIENTATION).intValue();
                previousOrientation = props.get(PREVIOUS_ORIENTATION).intValue();
                latitude = props.get(LATITUDE);
                longitude = props.get(LONGITUDE);
            }

            padding = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);

            position = new CameraPosition.Builder().bearing(bearing).tilt(tilt).zoom(zoom);
            position.target(new LatLng(latitude, longitude));

            if(myUser.getProperties().isSelected()) {
                myUser.fire(SELECT_USER, cameraNumber);
            }
        }

        @Override
        public void remove() {
            HashMap<String, Double> props = new HashMap<>();
            props.put(TILT, tilt * 1.);
            props.put(BEARING, bearing * 1.);
            props.put(ZOOM, zoom * 1.);
            props.put(ORIENTATION, orientation*1.);
            props.put(PREVIOUS_ORIENTATION, previousOrientation*1.);
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
                    position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                    position.bearing(0);
                    position.tilt(0);
                    break;
                case CAMERA_ORIENTATION_DIRECTION:
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
//                        map.setPadding(0, 0, 0, 0);
                        break;
                    case CAMERA_ORIENTATION_PERSPECTIVE:
//                        map.setPadding(0, (int) (context.getResources().getDisplayMetrics().heightPixels / 3), 0, 0);
                        break;
                }
            }*/
            orientationChanged = false;
            CameraViewHolder.this.setCameraUpdate(this);
            CameraViewHolder.this.update(false);
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch (event) {
                case SELECT_USER:
                    if(object != null && (int) object == cameraNumber) {
                        orientation = previousOrientation;
                        orientationChanged = true;
                        onChangeLocation(myUser.getLocation());
                    }
                    break;
                case UNSELECT_USER:
                    if(State.getInstance().getUsers().getCountSelected()==1){
                        State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                            @Override
                            public void call(Integer number, MyUser user) {
                                if(user.getProperties().isSelected()) {
                                    user.fire(SELECT_USER, cameraNumber);
                                }
                            }
                        });
                    } else {
                        CameraViewHolder.this.update(false);
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
                    orientationChanged = true;
                    previousOrientation = orientation;
                    setOrientation(orientation);
                    onChangeLocation(myUser.getLocation());
                    break;
                case ADJUST_ZOOM:
                    zoom = CAMERA_DEFAULT_ZOOM;
                    onChangeLocation(myUser.getLocation());
                    break;
                case CREATE_CONTEXT_MENU:
                    ContextMenu menu = (ContextMenu) object;
                    if(!myUser.getProperties().isSelected()) {
                        menu.add("Pin").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(SELECT_USER, cameraNumber);
                                return false;
                            }
                        });
                    } else if(State.getInstance().getUsers().getCountSelected()!=1) {
                        menu.add("Unpin").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                myUser.fire(UNSELECT_USER);
                                return false;
                            }
                        });
                    }
                    break;
                case CHANGE_NUMBER:
                    int newNumber = (int) object;
                    if(newNumber == number) break;
                    number = newNumber;
                    break;
            }
            return true;
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

            if(cameraUpdate == null || State.getInstance().getUsers().getCountSelected() != 1) return;
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

    public GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
//            System.out.println("onMyLocationButtonClick");
            if(cameraUpdate == null) return false;
            State.getInstance().fire(REQUEST_LOCATION_SINGLE);
            cameraUpdate.myUser.fire(SELECT_USER,cameraNumber);
            return false;
        }
    };

}