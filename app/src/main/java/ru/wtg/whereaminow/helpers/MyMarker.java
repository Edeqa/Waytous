package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import ru.wtg.whereaminow.R;

import static ru.wtg.whereaminowserver.helpers.Constants.CAMERA_DEFAULT_ZOOM;

/**
 * Created by tujger on 9/18/16.
 */
public class MyMarker {


    private static GoogleMap map;
    private Context context;
    private MyCamera myCamera;

    private Marker marker;
    private int color;
    private ArrayList<Location> locations;
    private Location location;

    private boolean draft;

    public MyMarker(){
        locations = new ArrayList<>();
        color = Color.BLUE;
    }

    public MyMarker(Context context){
        this();
        setContext(context);
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

    public void addLocation(Location location) {
        locations.add(location);
        setLocation(location);
        update();
    }

    private void createMarker(){
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getResources().getDrawable(R.drawable.navigation_marker,context.getTheme());
        } else {
            drawable = /*ContextCompat.getDrawable(context, R.drawable.navigation_marker);*/ context.getResources().getDrawable(R.drawable.navigation_marker);

        }
        if(isDraft()) {
            drawable.setColorFilter(new ColorMatrixColorFilter(Utils.getColorMatrix(Color.GRAY)));
        } else {
            drawable.setColorFilter(new ColorMatrixColorFilter(Utils.getColorMatrix(color)));
        }
        Canvas canvas = new Canvas();

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

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
        final float durationInMs = 1000;

        handler.post(new Runnable() {
            long elapsed;
            float t, v;

            @Override
            public void run() {
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                LatLng currentPosition = new LatLng(
                        startPosition.latitude*(1-t)+finalPosition.latitude*t,
                        startPosition.longitude*(1-t)+finalPosition.longitude*t);

                float rot = v * finalRotation + (1 - v) * startRotation;

                marker.setRotation(-rot > 180 ? rot/2 : rot);
                marker.setPosition(currentPosition);

                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void setContext(Context context) {
        this.context = context;
    }

    public static GoogleMap getMap() {
        return map;
    }

    public static void setMap(GoogleMap map) {
        MyMarker.map = map;
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

    public MyMarker setMyCamera(MyCamera myCamera) {
        this.myCamera = myCamera;
        return this;
    }

    public Marker getMarker(){
        return marker;
    }

}
