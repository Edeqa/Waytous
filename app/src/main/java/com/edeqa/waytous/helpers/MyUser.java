package com.edeqa.waytous.helpers;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.edeqa.eventbus.EventBus;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractProperty;
import com.edeqa.waytous.abstracts.AbstractPropertyHolder;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.holders.PropertiesHolder;
import com.edeqa.waytous.interfaces.Entity;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created 9/18/16.
 */
public class MyUser {

    private static final int SIMPLIFY_AFTER_EACH = 100;

    private static final String TYPE = "MyUser";

    private final EventBus<AbstractView> viewBus;
    private final EventBus<AbstractProperty> propertyBus;

    private Map<String,AbstractProperty> properties;
    private Map<String,AbstractView> views;
    private ArrayList<Location> locations;
    private Location location;
    private AtomicBoolean continueFiring = new AtomicBoolean();
    private long counter;
    private boolean user;

    public MyUser() throws TooManyListenersException {
        locations = new ArrayList<>();
        properties = new LinkedHashMap<>();
        views = new LinkedHashMap<>();

        propertyBus = new EventBus<>("properties_" + String.valueOf(this.hashCode()));
        viewBus = new EventBus<>("views_" + String.valueOf(this.hashCode()));
        viewBus.setRunner(State.getInstance().getAndroidRunner());
//        propertyBus.setRunner(State.getInstance().getAndroidRunner());

        counter = 0;
        createProperties();
        user = false;

    }

    public MyUser addLocation(Location location) {
        if(location == null) return this;
        locations.add(location);
        if(getLocation() == null) {
            setLocation(location);
            createViews();
        } else {
            setLocation(location);
        }
        if((++counter) % SIMPLIFY_AFTER_EACH == 0 && counter > 1) { // simplifying locations
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<LatLng> positions = new ArrayList<>();

                        long previous = locations.size() - SIMPLIFY_AFTER_EACH - 1;
                        if(previous < 0) previous = 0;

                        for(long i = previous; i<locations.size()-1;i++) {
                            try {
                                positions.add(Utils.latLng(locations.get((int) i)));
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                        positions = PolyUtil.simplify(positions, 10);

                        Iterator<Location> iterLocs = locations.iterator();
                        Iterator<LatLng> iterPozs = positions.iterator();

                        int sizeBefore = locations.size();
                        LatLng pos = iterPozs.next();
                        int i = 0;
                        while(i++ < previous) {
                            iterLocs.next();
                        }
                        while (iterLocs.hasNext()) {
                            Location loc = iterLocs.next();
                            if (loc != null && loc.getLatitude() == pos.latitude && loc.getLongitude() == pos.longitude) {
                                if (iterPozs.hasNext()) {
                                    pos = iterPozs.next();
                                } else {
                                    break;
                                }
                            } else {
                                iterLocs.remove();
                            }
                        }
                        Log.i(TYPE,"Simplified locations {user:"+getProperties().getDisplayName()+", counter:"+counter+", size before:"+sizeBefore+", size after:"+locations.size()+"}");
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                    onChangeLocation();
                }
            }).start();
        } else {
            System.out.println("ADDLOC:"+location + ":"+MyUser.this);
            onChangeLocation();
        }
        return this;
    }

    public Location getLocation(){
        return location;
    }

    private void setLocation(Location location) {
        this.location = location;
    }

    public ArrayList<Location> getLocations(){
        return locations;
    }

    private void createProperties(){
        Iterator<String> iter = State.getInstance().getUserPropertyHolders().keySet().iterator();

        while(iter.hasNext()) {
            String type = iter.next();
            if(properties.containsKey(type)) continue;

            AbstractPropertyHolder holder = State.getInstance().getUserPropertyHolders().get(type);
            if(!(holder instanceof AbstractViewHolder)) {
                AbstractProperty property = holder.create(this);
                if (property != null) {
                    properties.put(holder.getType(), property);
                    propertyBus.register(property);
                }
            }
        }
    }

    public void createViews(){
        if(getProperties().isActive()) {
//            removeViews();
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    Log.v(TYPE,"createViews:"+getProperties().getNumber()+":"+getProperties().getDisplayName());

                    Iterator<Map.Entry<String, AbstractViewHolder>> iterator = State.getInstance().getUserViewHolders().entrySet().iterator();
                    while(iterator.hasNext()) {
                        Map.Entry<String, AbstractViewHolder> entry = iterator.next();
                        String type = entry.getKey();

                        if(views.containsKey(type) && views.get(type) != null){
                            Log.v(TYPE, "remove:" + type + ":"+getProperties().getNumber()+":"+views.get(type));
                            views.get(type).remove();
                        }
                        AbstractPropertyHolder holder = entry.getValue();
                        if(holder == null) continue;
                        AbstractView view = ((AbstractViewHolder) holder).create(MyUser.this);
                        if (view != null) {
                            if(views.containsKey(type)) {
                                Log.v(TYPE, "update:" + type + ":" + getProperties().getNumber() + ":" + view);
                                viewBus.update(view);
                            } else {
                                Log.v(TYPE, "create:" + type + ":" + getProperties().getNumber() + ":" + view);
                                viewBus.register(view);
                            }
                            views.put(type, view);
                        }
                    }
                }
            });
        }

    }

    public void fire(final String EVENT){
        fire(EVENT, null);
    }

    public void fire(final String EVENT, final Object object){
        Log.i(TYPE,"--->>> "+EVENT+":"+getProperties().getNumber()+"|"+getProperties().getDisplayName()+":"+object);
        propertyBus.post(EVENT, object);
        viewBus.post(EVENT, object);


/*        continueFiring.set(true);
        for(Map.Entry<String,Entity> entry: properties.entrySet()){
            if(entry.getValue() instanceof AbstractProperty){
                try {
                    if(!continueFiring.get()) break;
                    continueFiring.set(entry.getValue().onEvent(EVENT, object));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                for(Map.Entry<String,Entity> entry: properties.entrySet()){
                    if(entry.getValue() instanceof AbstractView){
                        try {
                            if(!continueFiring.get()) break;
                            continueFiring.set(entry.getValue().onEvent(EVENT, object));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });*/
    }

    public void onChangeLocation(){
        for(Map.Entry<String,AbstractProperty> entry: properties.entrySet()){
            if(entry.getValue().dependsOnLocation() && getLocation() != null){
                try {
                    entry.getValue().onChangeLocation(getLocation());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                for (Map.Entry<String, AbstractView> entry : views.entrySet()) {
                    if (getProperties().isActive() && entry.getValue() == null) {
                        AbstractViewHolder holder = State.getInstance().getUserViewHolders().get(entry.getKey());
//                        if(holder != null) {
                            entry.setValue(holder.create(MyUser.this));
//                        }
                    }
                    if (entry.getValue().dependsOnLocation() && getLocation() != null) {
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
                Log.v(TYPE,"removeViews:" + getProperties().getNumber() + ":" + getProperties().getDisplayName());
                for (Map.Entry<String, AbstractViewHolder> entry : State.getInstance().getUserViewHolders().entrySet()) {
                    String type = entry.getKey();
                    if (views.containsKey(type) && views.get(type) != null) {
                        Log.v(TYPE, "remove:" + type + ":" + getProperties().getNumber() + ":" + views.get(type));
                        views.get(type).remove();
                        viewBus.unregister(views.get(type));
                        views.remove(type);
                    }
                }
            }
        });
    }

    public PropertiesHolder.Properties getProperties(){
        return (PropertiesHolder.Properties) properties.get(PropertiesHolder.TYPE);
    }

    public Entity getProperty(String TYPE) {
        if(properties.containsKey(TYPE)){
            return properties.get(TYPE);
        }
        return null;
    }

    public Entity getView(String TYPE) {
        if(views.containsKey(TYPE)){
            return views.get(TYPE);
        }
        return null;
    }

    public boolean isUser(){
        return user;
        /*return location != null
                && (location.getProvider().equals(LocationManager.GPS_PROVIDER)
                || location.getProvider().equals(LocationManager.NETWORK_PROVIDER)
                || location.getProvider().equals(LocationManager.PASSIVE_PROVIDER)
                || location.getProvider().equals("LocationStore")
                || location.getProvider().equals("fused")
                || location.getProvider().equals("touch"));*/
    }

    public void setUser(boolean user) {
        this.user = user;
    }

    public boolean isShown() {
        return (views != null && views.keySet().size() > 0);
    }

    @Override
    public String toString() {
        return "MyUser {" +
                "properties=" + properties +
                ", \nviews=" + views +
                ", \nlocation=" + location +
                ", user=" + user +
                "} " + (getProperties() != null ? getProperties().toString() : "");
    }
}
