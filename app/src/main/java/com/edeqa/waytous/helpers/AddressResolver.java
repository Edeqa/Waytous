package com.edeqa.waytous.helpers;

import android.content.Context;
import android.location.Location;

import com.edeqa.waytous.R;
import com.edeqa.waytous.interfaces.Runnable1;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created 8/22/17.
 */

public class AddressResolver {

    private final Context context;
    private Location location;
    private MyUser user;
    private LatLng latLng;
    private LatLng current;
    private Runnable1<String> callback;
    private long lastRequestTimestamp;

    public AddressResolver(Context context) {
        this.context = context;
        lastRequestTimestamp = 0L;
    }

    public void resolve(boolean force) {
        callback.call("...");
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
        } else {
            current = Utils.latLng(user.getLocation());
        }

        setLatLng(null);
        setLocation(null);

        new Thread(new Runnable() {
            @Override
            public void run() {

                    /*try {
                        if(location == null) {
                            callback.call(null);
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
                                    callback.call(formatted); //NON-NLS
                                } else {
                                    callback.call(null);
                                }
                            }

                        });
                    } catch(Exception e) {
                        Utils.err(AddressView.this, "User:", myUser.getProperties().getNumber(), "resolveAddress:", e.getMessage()); //NON-NLS
                        if(location == null) {
                            callback.call(null);
                            return;
                        }
                    }*/

//                    callback.call("...");

                try {
                    String req = context.getString(R.string.address_request_template, current.latitude, current.longitude);
                    if(user != null) {
                        Utils.log(AddressResolver.this, "User:", user.getProperties().getNumber() + "|" + user.getProperties().getDisplayName(), "Request:", req); //NON-NLS
                    } else {
                        Utils.log(AddressResolver.this, "LatLng:", current, "Request:", req); //NON-NLS
                    }
                    final String res = Utils.getUrl(req);
                    Utils.log(AddressResolver.this, "Response:", res); //NON-NLS
                    if(res.length() > 0) {
                        JSONObject address = new JSONObject(res);
                        callback.call(address.getString("display_name")); //NON-NLS
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.call(null);
                }
            }
        }).start();
    }


    public MyUser getUser() {
        return user;
    }

    public AddressResolver setUser(MyUser user) {
        this.user = user;
        return this;
    }

    public LatLng getLatLng() {
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

    public Location getLocation() {
        return location;
    }

    public Runnable1 getCallback() {
        return callback;
    }

    public AddressResolver setCallback(Runnable1<String> callback) {
        this.callback = callback;
        return this;
    }
}
