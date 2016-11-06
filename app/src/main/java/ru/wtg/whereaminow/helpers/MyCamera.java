package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static ru.wtg.whereaminowserver.helpers.Constants.CAMERA_DEFAULT_BEARING;
import static ru.wtg.whereaminowserver.helpers.Constants.CAMERA_DEFAULT_TILT;
import static ru.wtg.whereaminowserver.helpers.Constants.CAMERA_DEFAULT_ZOOM;
import static ru.wtg.whereaminowserver.helpers.Constants.CAMERA_ORIENTATION_DIRECTION;
import static ru.wtg.whereaminowserver.helpers.Constants.CAMERA_ORIENTATION_LAST;
import static ru.wtg.whereaminowserver.helpers.Constants.CAMERA_ORIENTATION_NORTH;
import static ru.wtg.whereaminowserver.helpers.Constants.CAMERA_ORIENTATION_PERSPECTIVE;
import static ru.wtg.whereaminowserver.helpers.Constants.CAMERA_ORIENTATION_STAY;
import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;

/**
 * Created by tujger on 9/20/16.
 */

public class MyCamera {

    private static GoogleMap map;
    private Context context;
    private Location location;
    private CameraUpdate camera;
    private CameraPosition.Builder position;
    private MyUser user;

    private float bearing;
    private float tilt;
    private float zoom;
    private int orientation = CAMERA_ORIENTATION_NORTH;
    private int previousOrientation = CAMERA_ORIENTATION_NORTH;
    private boolean orientationChanged = false;
    private boolean locationChanged = false;
    private boolean zoomChanged = false;
    private boolean tiltChanged = false;
    private boolean bearingChanged = false;
    private boolean moveFromHardware = false;
    private boolean canceled = false;

    public MyCamera(Context context){
        setContext(context);

        zoom = CAMERA_DEFAULT_ZOOM;
        tilt = CAMERA_DEFAULT_TILT;
        bearing = CAMERA_DEFAULT_BEARING;

        position = new CameraPosition.Builder().bearing(bearing).tilt(tilt).zoom(zoom);

    }

    public MyCamera update(){
        if(orientationChanged){
            switch (orientation){
                case CAMERA_ORIENTATION_NORTH:
                    setBearing(0);
                    setTilt(0);
                    break;
                case CAMERA_ORIENTATION_DIRECTION:
                    if(location != null) {
                        setBearing(location.getBearing());
                    }
                    setTilt(0);
                    break;
                case CAMERA_ORIENTATION_PERSPECTIVE:
                    if(location != null)
                        setBearing(location.getBearing());
                    setTilt(60);
                    break;
                case CAMERA_ORIENTATION_STAY:
                    position.target(map.getCameraPosition().target);
                    break;
            }
            orientationChanged = false;
        }
        if(locationChanged && location != null && orientation != CAMERA_ORIENTATION_STAY) {
            position.target(new LatLng(location.getLatitude(), location.getLongitude()));
//            camera = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), getZoom());
            if(orientation == CAMERA_ORIENTATION_DIRECTION || orientation == CAMERA_ORIENTATION_PERSPECTIVE){
                setBearing(location.getBearing());
            }
            locationChanged = false;
        }

        if(tiltChanged){
            position.tilt(tilt);
            tiltChanged = false;
        }

        if(zoomChanged){
            position.zoom(zoom);
            zoomChanged = false;
        }

        if(bearingChanged){
            position.bearing(bearing);
            bearingChanged = false;
        }
        System.out.println(toString());
        try{
            camera = CameraUpdateFactory.newCameraPosition(position.build());
        }catch(Exception e){
            System.out.println("Error CameraUpdateFactory: "+e.getMessage());
        }
        return this;
    }

    public void animate(){
        if(camera != null) {
            map.animateCamera(camera, LOCATION_UPDATES_DELAY, cancelableCallback);
        }
    }
    public void force(){
        if(camera != null) {
            map.moveCamera(camera);
        }
    }

    private GoogleMap.CancelableCallback cancelableCallback = new GoogleMap.CancelableCallback() {
        @Override
        public void onFinish() {
//            System.out.println("GoogleMap.CancelableCallback.onFinish");
        }

        @Override
        public void onCancel() {
//            System.out.println("GoogleMap.CancelableCallback.onCancel");
        }
    };

    public GoogleMap.OnCameraMoveStartedListener onCameraMoveStartedListener = new GoogleMap.OnCameraMoveStartedListener() {
        @Override
        public void onCameraMoveStarted(int i) {
            if(zoom != map.getCameraPosition().zoom)
                moveFromHardware = true;

//            System.out.println("onCameraMoveStarted");
        }
    };

    public GoogleMap.OnCameraIdleListener onCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
        @Override
        public void onCameraIdle() {
            if(canceled && zoom != map.getCameraPosition().zoom){
                setOrientation(getPreviousOrientation());
                moveFromHardware = true;
            } else if(zoom != map.getCameraPosition().zoom){
                moveFromHardware = true;
            }

            setZoom(map.getCameraPosition().zoom);
            setBearing(map.getCameraPosition().bearing);
            setTilt(map.getCameraPosition().tilt);

//            System.out.println("onCameraIdle,orientation:"+orientation+":"+moveFromHardware);
            if(!moveFromHardware){
                setOrientation(CAMERA_ORIENTATION_STAY);
            }

            moveFromHardware = false;
            canceled = false;
        }
    };

    public GoogleMap.OnCameraMoveCanceledListener onCameraMoveCanceledListener = new GoogleMap.OnCameraMoveCanceledListener() {
        @Override
        public void onCameraMoveCanceled() {
//            System.out.println("onCameraMoveCanceled:"+zoom+":"+map.getCameraPosition().zoom);
            if(zoom == map.getCameraPosition().zoom){
                setOrientation(CAMERA_ORIENTATION_STAY);
                moveFromHardware = false;
                canceled = true;
            }
        }
    };

    public GoogleMap.OnCameraMoveListener onCameraMoveListener = new GoogleMap.OnCameraMoveListener() {
        @Override
        public void onCameraMove() {
//            System.out.println("onCameraMove");
        }
    };

    public GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener = new GoogleMap.OnMyLocationButtonClickListener() {
        @Override
        public boolean onMyLocationButtonClick() {
//            System.out.println("onMyLocationButtonClick:"+getPreviousOrientation());
            moveFromHardware = true;
            if(orientation > CAMERA_ORIENTATION_LAST) {
                setOrientation(getPreviousOrientation());
            }
//            setZoom(CAMERA_DEFAULT_ZOOM);
            update().animate();
            return false;
        }
    };

    public static GoogleMap getMap() {
        return map;
    }

    public static void setMap(GoogleMap map) {
        MyCamera.map = map;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Location getLocation() {
        return location;
    }

    public MyCamera setLocation(Location location) {
        this.location = location;
        moveFromHardware = true;
        locationChanged = true;
        return this;
    }

    public float getZoom() {
        return zoom;
    }

    public MyCamera setZoom(float zoom) {
        this.zoom = zoom;
        zoomChanged = true;
        return this;
    }

    public void setCancelableCallback(GoogleMap.CancelableCallback cancelableCallback) {
        this.cancelableCallback = cancelableCallback;
    }

    public int getOrientation() {
        return orientation;
    }

    public int nextOrientation(){

        if(orientation > CAMERA_ORIENTATION_LAST) {
            orientation = previousOrientation;
        } else if(orientation == CAMERA_ORIENTATION_LAST){
            orientation = CAMERA_ORIENTATION_NORTH;
        } else {
            orientation++;
        }

        previousOrientation = orientation;
        moveFromHardware = true;
        setOrientation(orientation);
        update().animate();
        return orientation;
    }

    public MyCamera setOrientation(int orientation) {
        if(this.orientation <= CAMERA_ORIENTATION_LAST){
            previousOrientation = this.orientation;
        }
        this.orientation = orientation;
        orientationChanged = true;
        return this;
    }

    public float getBearing() {
        return bearing;
    }

    public MyCamera setBearing(float bearing) {
        this.bearing = bearing;
        bearingChanged = true;
        return this;
    }

    public float getTilt() {
        return tilt;
    }

    public MyCamera setTilt(float tilt) {
        this.tilt = tilt;
        tiltChanged = true;
        return this;
    }

    public String toString() {
        ArrayList<String> s = new ArrayList<>();
        if(location != null) {
            s.add("lat: "+location.getLatitude());
            s.add("lng: "+location.getLongitude());
        }
        s.add("bearing: "+bearing);
        s.add("tilt: "+tilt);
        s.add("zoom: "+zoom);
        s.add("orientation: "+orientation);

        return s.toString();
    }

    public int getPreviousOrientation() {
        return previousOrientation;
    }

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }
}
