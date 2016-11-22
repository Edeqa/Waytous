package ru.wtg.whereaminow.helpers;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static ru.wtg.whereaminow.helpers.MyUser.ASSIGN_TO_CAMERA;
import static ru.wtg.whereaminow.helpers.MyUser.CAMERA_NEXT_ORIENTATION;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_PIN;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_UNPIN;
import static ru.wtg.whereaminow.helpers.MyUser.REFUSE_FROM_CAMERA;
import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;

/**
 * Created by tujger on 11/20/16.
 */
public class CameraViewHolder implements ViewHolder<CameraViewHolder.CameraUpdateView> {

    final static int CAMERA_ORIENTATION_NORTH = 0;
    private final static int CAMERA_ORIENTATION_DIRECTION = 1;
    private final static int CAMERA_ORIENTATION_PERSPECTIVE = 2;
    private final static int CAMERA_ORIENTATION_STAY = 3;
    public final static int CAMERA_ORIENTATION_USER = 4;
    static final float CAMERA_DEFAULT_ZOOM = 15.f;
    static final float CAMERA_DEFAULT_TILT = 0.f;
    static final float CAMERA_DEFAULT_BEARING = 0.f;
    private final static int CAMERA_ORIENTATION_LAST = 2;

    private static final String TYPE = "camera";

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
    public CameraUpdateView createView(MyUser myUser) {
        if (myUser == null || myUser.getLocation() == null) return null;

        CameraUpdateView view = new CameraUpdateView(myUser);
        return view;
    }

    public void setCameraNumber(int cameraNumber) {
        this.cameraNumber = cameraNumber;
    }

    public int getAssignedCount(){
        return cameraUpdates.size();
    }

    public void update() {
        if(cameraUpdate == null) return;
        if(cameraUpdates.size()>1){
            double lat1=0f,lat2=0f,lng1=0f,lng2=0f;
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

//            System.out.println("BOUNDS:"+lng1+":"+lng2+":"+lat1+":"+lat2);
            final LatLngBounds bounds = new LatLngBounds(latLngLB, latLngRT);

            CameraUpdate camera = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            map.animateCamera(camera, LOCATION_UPDATES_DELAY, null);
        } else {
            CameraPosition.Builder cameraPosition = cameraUpdate.getCameraPosition();
//            System.out.println("CAMERAPOSITION:" + cameraUpdate + ":" + cameraPosition);
            CameraUpdate camera = CameraUpdateFactory.newCameraPosition(cameraPosition.build());
            moveFromHardware = true;
            map.animateCamera(camera, LOCATION_UPDATES_DELAY, null);
        }

    }

    public CameraViewHolder setMap(GoogleMap map) {
        this.map = map;
        return this;
    }

    private void setCameraUpdate(CameraUpdateView cameraUpdate) {
        this.cameraUpdate = cameraUpdate;
    }

    public CameraViewHolder setScaleView(MapScaleView scaleView) {
        this.scaleView = scaleView;
        return this;
    }


    public class CameraUpdateView implements AbstractView {
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

        public CameraUpdateView(MyUser myUser) {
            this.myUser = myUser;
            number = myUser.getNumber();

            tilt = CAMERA_DEFAULT_TILT;
            bearing = CAMERA_DEFAULT_BEARING;
            zoom = CAMERA_DEFAULT_ZOOM;

            padding = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);

            position = new CameraPosition.Builder().bearing(bearing).tilt(tilt).zoom(zoom);
            position.target(new LatLng(myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude()));

            if(myUser.isSelected()) {
                myUser.fire(ASSIGN_TO_CAMERA, cameraNumber);
            }
        }

        @Override
        public void remove() {
        }

        @Override
        public boolean dependsOnLocation() {
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {
            if(!myUser.isSelected()){
                return;
            }
            this.location = location;
            switch (orientation){
                case CAMERA_ORIENTATION_NORTH:
                    position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                    position.bearing(0);
                    position.tilt(0);
                    if(orientationChanged) {
                    }
                    break;
                case CAMERA_ORIENTATION_DIRECTION:
                    position.target(new LatLng(location.getLatitude(), location.getLongitude()));
                    position.bearing(location.getBearing());
                    position.tilt(0);
                    if(orientationChanged) {
                    }
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
            CameraViewHolder.this.update();
        }

        @Override
        public void setNumber(int number) {
            this.number = number;
        }

        @Override
        public int getNumber() {
            return number;
        }

        @Override
        public void onEvent(int event, Object object) {
            switch (event) {
                case ASSIGN_TO_CAMERA:
                    if(object != null && (int) object == cameraNumber) {
                        myUser.setSelected(true);
                        cameraUpdates.put(number,this);
                        orientation = previousOrientation;
                        orientationChanged = true;

                        onChangeLocation(myUser.getLocation());
                    }
                    break;
                case REFUSE_FROM_CAMERA:
                    if(cameraUpdates.containsKey(number))
                        cameraUpdates.remove(number);

                    myUser.setSelected(false);
                    CameraViewHolder.this.update();
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
                case MENU_ITEM_PIN:
                    MenuItem item = (MenuItem) object;
                    item.setVisible(!myUser.isSelected());
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(MyUser.ASSIGN_TO_CAMERA, cameraNumber);
                            return false;
                        }
                    });
                    break;
                case MENU_ITEM_UNPIN:
                    item = (MenuItem) object;
                    if(cameraUpdates.size()==1){
                        item.setVisible(false);
                    } else {
                        item.setVisible(myUser.isSelected());
                    }
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            myUser.fire(MyUser.REFUSE_FROM_CAMERA);
                            return false;
                        }
                    });
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

        public CameraPosition.Builder getCameraPosition(){
            return position;
        }

        public Location getLocation() {
            return location;
        }

    }


    public GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener = new GoogleMap.OnCameraMoveStartedListener() {
        @Override
        public void onCameraMoveStarted(int i) {
//            if(cameraUpdate.zoom != map.getCameraPosition().zoom)
//                moveFromHardware = true;

            if(scaleView != null) {
                scaleView.update(map.getProjection(), map.getCameraPosition());
            }
//            System.out.println("onCameraMoveStarted");
        }
    };

    public GoogleMap.OnCameraIdleListener onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
        @Override
        public void onCameraIdle() {
            if(cameraUpdate == null) return;
            if(canceled && cameraUpdate.zoom != map.getCameraPosition().zoom){
                cameraUpdate.setOrientation(cameraUpdate.previousOrientation);
                moveFromHardware = false;
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

            if(scaleView != null) {
                scaleView.update(map.getProjection(), map.getCameraPosition());
            }

            moveFromHardware = false;
            canceled = false;
//            System.out.println("onCameraIdle");
        }
    };

    public GoogleMap.OnCameraMoveCanceledListener onCameraMoveCanceledListener = new GoogleMap.OnCameraMoveCanceledListener() {
        @Override
        public void onCameraMoveCanceled() {
//            System.out.println("onCameraMoveCanceled");
            if(cameraUpdate == null) return;
            if(cameraUpdate.zoom == map.getCameraPosition().zoom){
                cameraUpdate.setOrientation(CAMERA_ORIENTATION_STAY);
                moveFromHardware = false;
                canceled = true;
            }
            if(scaleView != null) {
                scaleView.update(map.getProjection(), map.getCameraPosition());
            }
        }
    };

    public GoogleMap.OnCameraMoveListener onCameraMoveListener = new GoogleMap.OnCameraMoveListener() {
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