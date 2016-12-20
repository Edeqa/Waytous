package ru.wtg.whereaminow.helpers;

import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.holders.AbstractProperty;
import ru.wtg.whereaminow.holders.AbstractView;
import ru.wtg.whereaminow.holders.AbstractViewHolder;
import ru.wtg.whereaminow.holders.PropertiesHolder;
import ru.wtg.whereaminow.interfaces.Entity;
import ru.wtg.whereaminow.interfaces.EntityHolder;

/**
 * Created 9/18/16.
 */
public class MyUser {

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
        onChangeLocation();
        return this;
    }

    private void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation(){
        return location;
    }

    public ArrayList<Location> getLocations(){
        return locations;
    }

    private void createProperties(){
        for(Map.Entry<String, EntityHolder> entry: State.getInstance().getUserEntityHolders().entrySet()){
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
                    for(Map.Entry<String, AbstractViewHolder> entry: State.getInstance().getUserViewHolders().entrySet()){
                        if(entities.containsKey(entry.getKey()) && entities.get(entry.getKey()) != null){
                            entities.get(entry.getKey()).remove();
                        }
                        entities.put(entry.getKey(), entry.getValue().create(MyUser.this));
                    }
                }
            });
        }
    }

    public void fire(final String EVENT){
        fire(EVENT, null);
    }

    public void fire(final String EVENT, final Object object){
        for(Map.Entry<String,Entity> entry: entities.entrySet()){
            if(entry.getValue() instanceof AbstractProperty){
                try {
                    if(!entry.getValue().onEvent(EVENT, object)) break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                for(Map.Entry<String,Entity> entry: entities.entrySet()){
                    if(entry.getValue() instanceof AbstractView){
                        try {
                            if(!entry.getValue().onEvent(EVENT, object)) break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void onChangeLocation(){
        for(Map.Entry<String,Entity> entry: entities.entrySet()){
            if(entry.getValue() instanceof AbstractProperty
                    && entry.getValue().dependsOnLocation()
                    && getLocation() != null){
                try {
                    entry.getValue().onChangeLocation(getLocation());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                for (Map.Entry<String, Entity> entry : entities.entrySet()) {
                    if (getProperties().isActive() && entry.getValue() == null) {
                        AbstractViewHolder holder = State.getInstance().getUserViewHolders().get(entry.getKey());
                        if(holder != null) {
                            entry.setValue(holder.create(MyUser.this));
                        }
                    }
                    if (entry.getValue() instanceof AbstractView
                            && entry.getValue().dependsOnLocation()
                            && getLocation() != null) {
                        try {
                            entry.getValue().onChangeLocation(getLocation());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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

    public Entity getEntity(String TYPE) {
        if(entities.containsKey(TYPE)){
            return entities.get(TYPE);
        }
        return null;
    }

    public boolean isUser(){
        return location != null
            && (location.getProvider().equals(LocationManager.GPS_PROVIDER)
                || location.getProvider().equals(LocationManager.NETWORK_PROVIDER)
                || location.getProvider().equals(LocationManager.PASSIVE_PROVIDER)
                || location.getProvider().equals("fused")
                || location.getProvider().equals("touch"));
    }

}
