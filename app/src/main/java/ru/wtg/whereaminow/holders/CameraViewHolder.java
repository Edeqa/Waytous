package ru.wtg.whereaminow.holders;

import android.content.Context;
import android.location.Location;
import android.view.MenuItem;

import com.github.pengrad.mapscaleview.MapScaleView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.MyUsers;

import static ru.wtg.whereaminow.helpers.MyUser.ADJUST_ZOOM;
import static ru.wtg.whereaminow.helpers.MyUser.ASSIGN_TO_CAMERA;
import static ru.wtg.whereaminow.helpers.MyUser.CAMERA_NEXT_ORIENTATION;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_PIN_ALL;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_PIN;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_UNPIN;
import static ru.wtg.whereaminow.helpers.MyUser.REFUSE_FROM_CAMERA;
import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;

/**
 * Created 11/20/16.
 */
public class CameraViewHolder extends AbstractViewHolder<CameraViewHolder.CameraUpdateView> {
    private static final String TYPE = "camera";

    private final static int CAMERA_ORIENTATION_NORTH = 0;
    private final static int CAMERA_ORIENTATION_DIRECTION = 1;
    private final static int CAMERA_ORIENTATION_PERSPECTIVE = 2;
    private final static int CAMERA_ORIENTATION_STAY = 3;
    private final static int CAMERA_ORIENTATION_USER = 4;
    private static final float CAMERA_DEFAULT_ZOOM = 15.f;
    private static final float CAMERA_DEFAULT_TILT = 0.f;
    private static final float CAMERA_DEFAULT_BEARING = 0.f;
    private final static int CAMERA_ORIENTATION_LAST = 2;


    private HashMap<Integer,CameraUpdateView> cameraUpdates;
    private CameraUpdateView cameraUpdate;
    private final Context context;
    private static ArrayList<CameraViewHolder> instance = new ArrayList<>();
    private int cameraNumber;
    private GoogleMap map;
    private int padding;
    private boolean moveFromHardware = false;
    private boolean canceled = false;
    private MapScaleView scaleView;

    private CameraViewHolder(Context context) {
        this.context = context;
        cameraUpdates = new HashMap<>();
        padding = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
    }

    public static CameraViewHolder getInstance(Context context, int number) {
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

    public int getCountAssigned(){
        return cameraUpdates.size();
    }

    private void update(boolean move) {
        if(cameraUpdate == null) return;
        CameraUpdate camera;
        if(cameraUpdates.size()>1){
            double lat1=0f,lat2=0f,lng1=0f,lng2=0f;
            //noinspection LoopStatementThatDoesntLoop
            for(Map.Entry<Integer,CameraUpdateView> entry: cameraUpdates.entrySet()){
                lat1 = entry.getValue().getLocation().getLatitude();
                lat2 = entry.getValue().getLocation().getLatitude();
                lng1 = entry.getValue().getLocation().getLongitude();
                lng2 = entry.getValue().getLocation().getLongitude();
                break;
            }
            for(Map.Entry<Integer,CameraUpdateView> entry: cameraUpdates.entrySet()){
                lat1 = Math.min(lat1,entry.getValue().getLocation().getLatitude());
                lat2 = Math.max(lat2,entry.getValue().getLocation().getLatitude());
                lng1 = Math.min(lng1,entry.getValue().getLocation().getLongitude());
                lng2 = Math.max(lng2,entry.getValue().getLocation().getLongitude());
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


    class CameraUpdateView extends AbstractView {
        private MyUser myUser;
        private CameraPosition.Builder position;
        private Location location;

        private int number;

        private float zoom;
        private float bearing;
        private float tilt;
        private int orientation = CAMERA_ORIENTATION_NORTH;
        private int previousOrientation = CAMERA_ORIENTATION_NORTH;
        private boolean orientationChanged;

        CameraUpdateView(MyUser myUser) {
            this.myUser = myUser;
            number = myUser.getProperties().getNumber();

            tilt = CAMERA_DEFAULT_TILT;
            bearing = CAMERA_DEFAULT_BEARING;
            zoom = CAMERA_DEFAULT_ZOOM;

            padding = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);

            position = new CameraPosition.Builder().bearing(bearing).tilt(tilt).zoom(zoom);
            position.target(new LatLng(myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude()));

            if(myUser.getProperties().isSelected()) {
                myUser.fire(ASSIGN_TO_CAMERA, cameraNumber);
            }
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
                    position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                    position.bearing(location.getBearing());
                    if(orientationChanged) {
                        position.tilt(60);
                    }
                    break;
                case CAMERA_ORIENTATION_STAY:
                    position.target(map.getCameraPosition().target);
                    break;
            }
            position.zoom(zoom);
            orientationChanged = false;
            CameraViewHolder.this.setCameraUpdate(this);
            CameraViewHolder.this.update(false);
        }

        @Override
        public void onEvent(int event, Object object) {
            switch (event) {
                case ASSIGN_TO_CAMERA:
                    if(object != null && (int) object == cameraNumber) {
                        cameraUpdates.put(number,this);
                        orientation = previousOrientation;
                        orientationChanged = true;

                        onChangeLocation(myUser.getLocation());
                    }
                    break;
                case REFUSE_FROM_CAMERA:
                    if(cameraUpdates.containsKey(number))
                        cameraUpdates.remove(number);

                    CameraViewHolder.this.update(false);

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
                case MENU_ITEM_PIN:
                    MenuItem item = (MenuItem) object;
                    item.setVisible(!myUser.getProperties().isSelected());
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(MyUser.ASSIGN_TO_CAMERA, 0);
                            return false;
                        }
                    });
                    break;
                case MENU_ITEM_UNPIN:
                    item = (MenuItem) object;
                    if(State.getInstance().getUsers().getCountSelected()==1){
                        item.setVisible(false);
                    } else {
                        item.setVisible(myUser.getProperties().isSelected());
                    }
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(MyUser.REFUSE_FROM_CAMERA);
                            return false;
                        }
                    });
                    break;
                case MENU_ITEM_PIN_ALL:
                    item = (MenuItem) object;
                    if(State.getInstance().getUsers().getCountActive() < 2 || State.getInstance().getUsers().getCountActive() == getCountAssigned()) {
                        item.setVisible(false);
                    } else {
                        item.setVisible(true);
                        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                State.getInstance().getUsers().forAllUsers(new MyUsers.Callback() {
                                    @Override
                                    public void call(Integer number, MyUser myUser) {
                                        myUser.fire(ASSIGN_TO_CAMERA, 0);
                                    }
                                });
                                return false;
                            }
                        });
                    }
                    break;
            }
        }

        public void setOrientation(int orientation) {
            if(this.orientation <= CAMERA_ORIENTATION_LAST){
                previousOrientation = this.orientation;
            }
            this.orientation = orientation;
            orientationChanged = true;
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

            if(cameraUpdate == null || cameraUpdates.size() != 1) return;
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
            if(cameraUpdate == null || cameraUpdates.size() != 1) return;
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
            cameraUpdate.myUser.fire(ASSIGN_TO_CAMERA,cameraNumber);
            return false;
        }
    };
}