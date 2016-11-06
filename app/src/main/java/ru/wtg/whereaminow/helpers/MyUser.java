package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import ru.wtg.whereaminow.R;

import static ru.wtg.whereaminowserver.helpers.Constants.CAMERA_DEFAULT_ZOOM;
import static ru.wtg.whereaminowserver.helpers.Constants.LOCATION_UPDATES_DELAY;

/**
 * Created by tujger on 9/18/16.
 */
public class MyUser {

    private static GoogleMap map;
    private static Context context;

    private MyCamera myCamera;
    private GoogleMap currentMap;
    private Marker marker;
    private ArrayList<Location> locations;
    private Location location;

    private int color;
    private boolean draft;

    public MyUser(){
        locations = new ArrayList<>();
        color = Color.BLUE;
    }

    public static void setMap(GoogleMap map) {
        MyUser.map = map;
    }

    public static void setContext(Context context) {
        MyUser.context = context;
    }

    public void showDraft(Location location){
//        System.out.println("showDraft:"+location);
        if(location == null) return;

        setLocation(location);
        setDraft(true);
        createMarker();
        /*CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(location.getLatitude(), location.getLongitude())).radius(location.getAccuracy())
                .fillColor(Color.CYAN).strokeColor(Color.BLUE).strokeWidth(2f);
        circle = map.addCircle(circleOptions);*/

        update();
    }

    public MyUser addLocation(Location location) {
        locations.add(location);
        setLocation(location);
        return this;
//        update();
    }

    public void createMarker(){
        Bitmap bitmap;
        int size = context.getResources().getDimensionPixelOffset(android.R.dimen.app_icon_size);
        if(isDraft()) {
            bitmap = Utils.renderBitmap(context,R.drawable.navigation_marker,Color.GRAY,size,size);
        } else {
            bitmap = Utils.renderBitmap(context,R.drawable.navigation_marker,color,size,size);
        }

        currentMap = map;
        marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .rotation(location.getBearing())
                .anchor(0.5f, 0.5f)
                .flat(true)
//                .title("Melbourne")
//                .snippet("Population: 4,137,400")
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
//        marker.showInfoWindow();

    }

    public void update(){
        if(locations.size()==0) return;

        if(marker != null && map != currentMap){
            marker.remove();
            marker = null;
        }
        if(marker != null && isDraft()){
            marker.remove();
            setDraft(false);
            createMarker();
        } else if(marker == null){
            if(myCamera != null){
                myCamera.setZoom(CAMERA_DEFAULT_ZOOM);
            }
            createMarker();
        }

        final LatLng startPosition = marker.getPosition();
        final LatLng finalPosition = new LatLng(location.getLatitude(), location.getLongitude());

        final float startRotation = marker.getRotation();
        final float finalRotation = location.getBearing();

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = LOCATION_UPDATES_DELAY;
        handler.post(new Runnable() {
            long elapsed;
            float t, v;

            @Override
            public void run() {
//                if(marker == null) return;
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        startPosition.latitude*(1-t)+finalPosition.latitude*t,
                        startPosition.longitude*(1-t)+finalPosition.longitude*t);

                float rot = v * finalRotation + (1 - v) * startRotation;

                if(marker != null) {
                    marker.setRotation(-rot > 180 ? rot / 2 : rot);
                    marker.setPosition(currentPosition);
                }

                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });

        if(myCamera != null){
            myCamera.setLocation(location).update().animate();
        }

    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    private boolean isDraft() {
        return draft;
    }

    private void setDraft(boolean draft) {
        this.draft = draft;
    }

    private void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation(){
        return location;
    }

    public MyCamera getMyCamera() {
        return myCamera;
    }

    public MyUser setMyCamera(MyCamera myCamera) {
        if(this.myCamera == myCamera) {
            return this;
        } else if(this.myCamera != null) {
            this.myCamera.setUser(null);
        }
        if(myCamera != null){
            if(myCamera.getUser() != null){
                myCamera.getUser().setMyCamera(null);
            }
            myCamera.setUser(this);
        }
        this.myCamera = myCamera;
        if(location != null && myCamera != null) {
            myCamera.setLocation(location).update().animate();
        }
        return this;
    }

    public Marker getMarker(){
        return marker;
    }

    public void hide(){
        if(marker != null) {
            try {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    public void run() {
                        marker.remove();
                        marker = null;
                    }
                });
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public String getId(){
        if(marker != null){
            return marker.getId();
        }
        return null;
    }

}
