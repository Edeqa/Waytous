package com.edeqa.waytous.helpers;

import android.content.Context;
import android.location.Location;

import com.edeqa.helpers.Misc;
import com.edeqa.helpers.interfaces.Consumer;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created 8/22/17.
 */

@SuppressWarnings("unused")
public class AddressResolver {

    private final Context context;
    private Location location;
    private MyUser user;
    private LatLng latLng;
    private LatLng current;
    private Consumer<String> callback;
    private long lastRequestTimestamp;

    public AddressResolver(Context context) {
        this.context = context;
        lastRequestTimestamp = 0L;
    }

    public void resolve(boolean force) {
        callback.accept("...");
        resolve();
    }

    public void resolve() {

        long currentTimestamp = new Date().getTime();
        if(currentTimestamp - lastRequestTimestamp < 5000) return;
        lastRequestTimestamp = currentTimestamp;

        if(getLatLng() != null) {
            current = getLatLng();
        } else if(getLocation() != null) {
            current = Utils.latLng(getLocation());
        } else if(user.getLocation() != null) {
            current = Utils.latLng(user.getLocation());
        } else {
            return;
        }

        setLatLng(null);
        setLocation(null);

        new Thread(new Runnable() {
            @Override
            public void run() {

                    /*try {
                        if(location == null) {
                            callback.accept(null);
                            return;
                        }
                        geocoding.reverse(location, new OnReverseGeocodingListener() {
                            @Override
                            public void onAddressResolved(Location location, List<Address> list) {
                                if (list != null && list.size() > 0) {
                                    ArrayList parts = new ArrayList();
                                    for (int i = 0; i < list.get(0).getMaxAddressLineIndex(); i++) {
                                        parts.add(list.get(0).getAddressLine(i));
                                    }
                                    String formatted = TextUtils.join(", ", parts);
                                    callback.accept(formatted); //NON-NLS
                                } else {
                                    callback.accept(null);
                                }
                            }

                        });
                    } catch(Exception e) {
                        Utils.err(AddressView.this, "User:", myUser.getProperties().getNumber(), "resolveAddress:", e.getMessage()); //NON-NLS
                        if(location == null) {
                            callback.accept(null);
                            return;
                        }
                    }*/

//                    callback.accept("...");

                try {
                    String req = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f&zoom=18&addressdetails=1", current.latitude, current.longitude); //NON-NLS
                    if(user != null) {
                        Utils.log(AddressResolver.this, "User:", user.getProperties().getNumber() + "|" + user.getProperties().getDisplayName(), "Request:", req); //NON-NLS
                    } else {
                        Utils.log(AddressResolver.this, "LatLng:", current, "Request:", req); //NON-NLS
                    }
                    final String res = Misc.getUrl(req);
                    Utils.log(AddressResolver.this, "Response:", res); //NON-NLS
                    if(res.length() > 0) {
                        JSONObject address = new JSONObject(res);
                        callback.accept(address.getString("display_name")); //NON-NLS
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.accept(null);
                }
            }
        }).start();
    }


    private MyUser getUser() {
        return user;
    }

    public AddressResolver setUser(MyUser user) {
        this.user = user;
        return this;
    }

    private LatLng getLatLng() {
        return latLng;
    }

    public AddressResolver setLatLng(LatLng latLng) {
        this.latLng = latLng;
        return this;
    }

    public AddressResolver setLocation(Location location) {
        this.location = location;
        return this;
    }

    private Location getLocation() {
        return location;
    }

    private Consumer getCallback() {
        return callback;
    }

    public AddressResolver setCallback(Consumer<String> callback) {
        this.callback = callback;
        return this;
    }
}
