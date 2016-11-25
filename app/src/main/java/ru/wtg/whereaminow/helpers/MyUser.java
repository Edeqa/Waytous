package ru.wtg.whereaminow.helpers;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.holders.AbstractView;
import ru.wtg.whereaminow.holders.AbstractViewHolder;
import ru.wtg.whereaminow.holders.PropertiesHolder;
import ru.wtg.whereaminow.interfaces.Entity;
import ru.wtg.whereaminow.interfaces.EntityHolder;

/**
 * Created 9/18/16.
 */
public class MyUser {
    public static final int ASSIGN_TO_CAMERA = 1;
    public static final int REFUSE_FROM_CAMERA = 2;
    public static final int CAMERA_NEXT_ORIENTATION = 3;
    public static final int CHANGE_NAME = 4;
    public static final int CHANGE_NUMBER = 5;
    public static final int CHANGE_COLOR = 6;
    public static final int MENU_ITEM_NAVIGATE = 8;
    public static final int MENU_ITEM_PIN_ALL = 9;
    public static final int MENU_ITEM_PIN = 10;
    public static final int MENU_ITEM_UNPIN = 11;
    public static final int MENU_ITEM_SHOW_TRACK = 12;
    public static final int MENU_ITEM_HIDE_TRACK = 13;
    public static final int MENU_ITEM_SHOW_ALL_TRACKS = 14;
    public static final int MENU_ITEM_HIDE_ALL_TRACKS = 15;
    public static final int MENU_ITEM_CHANGE_NAME = 16;
    public static final int ADJUST_ZOOM = 17;
    public static final int MAKE_ACTIVE = 18;
    public static final int MAKE_INACTIVE = 19;
    public static final int SHOW_TRACK = 20;
    public static final int HIDE_TRACK = 21;

    private LinkedHashMap<String,Entity> entities;
    private ArrayList<Location> locations;
    private Location location;

    public MyUser(){
        locations = new ArrayList<>();
        entities = new LinkedHashMap<>();
        createProperties();
    }

        /*CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(location.getLatitude(), location.getLongitude())).radius(location.getAccuracy())
                .fillColor(Color.CYAN).strokeColor(Color.BLUE).strokeWidth(2f);
        circle = map.addCircle(circleOptions);*/

    public MyUser addLocation(Location location) {
        locations.add(location);
        setLocation(location);
        return this;
    }

    private void setLocation(Location location) {
        this.location = location;
        onChangeLocation();
    }

    public Location getLocation(){
        return location;
    }

    public ArrayList<Location> getLocations(){
        return locations;
    }

    private void createProperties(){
        for(Map.Entry<String, EntityHolder> entry: State.getInstance().getEntityHolders().entrySet()){
            if(entry.getValue() instanceof AbstractViewHolder) continue;
            if(entities.containsKey(entry.getKey())) continue;
            Entity property = entry.getValue().create(this);
            if(property != null){
                entities.put(entry.getKey(),property);
            }
        }
    }

    public void createViews(){
        if(getProperties().isActive()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    for(Map.Entry<String, EntityHolder> entry: State.getInstance().getEntityHolders().entrySet()){
                        if(!(entry.getValue() instanceof AbstractViewHolder)) continue;
                        if(entities.containsKey(entry.getKey()) && entities.get(entry.getKey()) != null){
                            entities.get(entry.getKey()).remove();
                        }
                        entities.put(entry.getKey(), entry.getValue().create(MyUser.this));
                    }
                }
            });
        }
    }

    public void fire(final int EVENT){
        fire(EVENT, null);
    }

    public void fire(final int EVENT, final Object object){
        for(Map.Entry<String,Entity> entry: entities.entrySet()){
            if(entry.getValue() instanceof AbstractView) continue;
            if(entry.getValue() != null){
                entry.getValue().onEvent(EVENT, object);
            }
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                for(Map.Entry<String,Entity> entry: entities.entrySet()){
                    if(entry.getValue() instanceof AbstractView){
                        entry.getValue().onEvent(EVENT, object);
                    }
                }
            }
        });
    }

    private void onChangeLocation(){
        for(Map.Entry<String,Entity> entry: entities.entrySet()){
            if(!(entry.getValue() instanceof AbstractView)
                    && entry.getValue() != null
                    && entry.getValue().dependsOnLocation()
                    && getLocation() != null){
                entry.getValue().onChangeLocation(getLocation());
            }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                for (Map.Entry<String, Entity> entry : entities.entrySet()) {
                    if (getProperties().isActive() && entry.getValue() == null) {
                        EntityHolder holder = State.getInstance().getEntityHolders().get(entry.getKey());
                        entry.setValue(holder.create(MyUser.this));
                    }
                    if (entry.getValue() instanceof AbstractView
                            && entry.getValue().dependsOnLocation()
                            && getLocation() != null) {
                        entry.getValue().onChangeLocation(getLocation());
                    }
                }
            }
        });
    }

    public void removeViews(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                Iterator<Map.Entry<String,Entity>> iter = entities.entrySet().iterator();
                while(iter.hasNext()){
                    Map.Entry<String,Entity> entry = iter.next();
                    if(entry.getValue() instanceof AbstractView){
                        entry.getValue().remove();
                        iter.remove();
                    }
                }
            }
        });
    }

    public PropertiesHolder.Properties getProperties(){
        return (PropertiesHolder.Properties) entities.get(PropertiesHolder.TYPE);
    }

}
