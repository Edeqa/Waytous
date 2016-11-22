package ru.wtg.whereaminow.helpers;

import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tujger on 9/18/16.
 */
public class MyUser {
    public static final int ASSIGN_TO_CAMERA = 1;
    public static final int REFUSE_FROM_CAMERA = 2;
    public static final int CHANGE_NAME = 3;
    public static final int CAMERA_NEXT_ORIENTATION = 4;
    public static final int MENU_ITEM_NAVIGATE = 6;
    public static final int MENU_ITEM_PIN = 7;
    public static final int MENU_ITEM_UNPIN = 8;
    public static final int MENU_ITEM_SHOW_TRACK = 9;
    public static final int MENU_ITEM_HIDE_TRACK = 10;


    private static GoogleMap map;

    private LinkedHashMap<String,AbstractView> views;

//    private MyCamera myCamera;
    private GoogleMap currentMap;
//    private Marker marker;
    private Polyline route;
    private ArrayList<Location> locations;
    private Location location;

    private String name;
    private int color;
    private int number;
    private boolean draft;
    private boolean active;
    private boolean selected;

    public MyUser(){
        locations = new ArrayList<>();
        views = new LinkedHashMap<>();
        color = Color.BLUE;
    }

    public static void setMap(GoogleMap map) {
        MyUser.map = map;
    }


    public void showDraft(Location location){
//        System.out.println("showDraft:"+location);
        if(location == null) return;

        setLocation(location);
        setDraft(true);
//        createMarker();
        /*CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(location.getLatitude(), location.getLongitude())).radius(location.getAccuracy())
                .fillColor(Color.CYAN).strokeColor(Color.BLUE).strokeWidth(2f);
        circle = map.addCircle(circleOptions);*/

//        update();
    }

    public MyUser addLocation(Location location) {
        locations.add(location);
        setLocation(location);
        return this;
    }

    /*public void update(){

        if(showTrack){
            if(route != null) route.remove();
            route = map.addPolyline(new PolylineOptions().width(10).color(color).geodesic(true).zIndex(100f));
            route.setPoints(getTrail());
        } else if(route != null) {
            route.remove();
            route = null;
        }

    }*/

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
        onChangeLocation();
    }

    public Location getLocation(){
        return location;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void createViews(){
        if(isActive()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    for(Map.Entry<String, ViewHolder> entry: State.getInstance().getViewHolders().entrySet()){
                        if(views.containsKey(entry.getKey()) && views.get(entry.getKey()) != null){
                            views.get(entry.getKey()).remove();
                        }
                        views.put(entry.getKey(), entry.getValue().createView(MyUser.this));
                    }
                }
            });
        }
    }

    public void fire(final int EVENT, final Object object){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                for(Map.Entry<String,AbstractView> entry: views.entrySet()){
                    if(entry.getValue() != null){
                        entry.getValue().onEvent(EVENT, object);
                    }
                }
            }
        });
    }

    public void fire(final int EVENT){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                for (Map.Entry<String, AbstractView> entry : views.entrySet()) {
                    if (entry.getValue() != null) {
                        entry.getValue().onEvent(EVENT, null);
                    }
                }
            }
        });
    }

    public void onChangeLocation(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                for (Map.Entry<String, AbstractView> entry : views.entrySet()) {
                    if (isActive() && entry.getValue() == null) {
                        ViewHolder holder = State.getInstance().getViewHolders().get(entry.getKey());
                        entry.setValue(holder.createView(MyUser.this));
                    }
                    if (entry.getValue() != null && entry.getValue().dependsOnLocation() && getLocation() != null) {
                        entry.getValue().onChangeLocation(getLocation());
                    }

                }
            }
        });
    }

    public void removeViews(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                Iterator<Map.Entry<String,AbstractView>> iter = views.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry<String,AbstractView> entry = iter.next();
                    if(entry.getValue() != null) entry.getValue().remove();
                    iter.remove();
                }
            }
        });
    }

    public void assignToCamera(int numberOfCamera){
        fire(ASSIGN_TO_CAMERA,numberOfCamera);
    }

    public List<LatLng> getTrail(){
        List<LatLng> points = new ArrayList<>();
        for(Location location: locations){
//            System.out.println(location.getLatitude()+":"+location.getLongitude());
            points.add(new LatLng(location.getLatitude(),location.getLongitude()));
        }
        return points;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
