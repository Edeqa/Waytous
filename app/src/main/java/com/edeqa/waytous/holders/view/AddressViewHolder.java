package com.edeqa.waytous.holders.view;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.AddressResolver;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.interfaces.Runnable1;

import io.nlopez.smartlocation.SmartLocation;

import static com.edeqa.waytous.helpers.Events.SELECT_USER;
import static com.edeqa.waytous.helpers.Events.UNSELECT_USER;

/**
 * Created 11/18/16.
 */
@SuppressWarnings("WeakerAccess")
public class AddressViewHolder extends AbstractViewHolder<AddressViewHolder.AddressView> {

    private static final String TYPE = "address"; //NON-NLS
    private Runnable1<String> callback;

    public AddressViewHolder(final MainActivity context) {
        super(context);
        setCallback(new Runnable1<String>() {
            @Override
            public void call(String text) {
                if(context.getSupportActionBar() != null) {
                    context.getSupportActionBar().setSubtitle(text);
                }
            }
        });

    }

    @Override
    public String getType(){
        return TYPE;
    }

    @Override
    public AddressView create(MyUser myUser) {
        if (myUser == null) return null;
        return new AddressView(context, myUser);
    }

    public AddressViewHolder setCallback(Runnable1<String> callback) {
        this.callback = callback;
        return this;
    }

    private void setTitle(final String text){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                callback.call(text);
            }
        });
    }

    class AddressView extends AbstractView {
        private final SmartLocation.GeocodingControl geocoding;
        private long lastRequestTimestamp;
        private AddressResolver addressResolver;

        AddressView(MainActivity context, MyUser myUser) {
            super(context, myUser);
            geocoding = SmartLocation.with(context).geocoding();
            addressResolver = new AddressResolver(context);
            addressResolver.setUser(myUser);
            addressResolver.setCallback(new Runnable1<String>() {
                @Override
                public void call(String formattedAddress) {
                    setTitle(formattedAddress);
                }
            });
        }

        @Override
        public void remove() {
            super.remove();
            geocoding.stop();
        }

        @Override
        public boolean dependsOnLocation(){
            return true;
        }

        @Override
        public void onChangeLocation(Location location) {
            resolveAddress(location);
        }

        @Override
        public boolean onEvent(String event, Object object) {
            switch(event){
                case SELECT_USER:
                case UNSELECT_USER:
                    resolveAddress(myUser.getLocation());
                    break;
            }
            return true;
        }

        private void resolveAddress(final Location location) {
            if(State.getInstance().getUsers().getCountSelectedTotal() > 1) {
                callback.call(context.getString(R.string.d_users_selected, State.getInstance().getUsers().getCountSelectedTotal()));
                return;
            } else if(myUser.getProperties().isSelected() && location == null) {
                callback.call(null);
            } else if(!myUser.getProperties().isSelected() || location == null){
                return;
            }

            addressResolver.resolve();
            /*long currentTimestamp = new Date().getTime();
            if(currentTimestamp - lastRequestTimestamp < 5000) return;
            lastRequestTimestamp = currentTimestamp;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    *//*try {
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
                    }*//*

//                    callback.call("...");

                    try {
                        String req = context.getString(R.string.address_request_template, location.getLatitude(), location.getLongitude());
                        Utils.log(AddressView.this, "User:", myUser.getProperties().getNumber() + "|" + myUser.getProperties().getDisplayName(), "Request:", req); //NON-NLS
                        final String res = Utils.getUrl(req);
                        Utils.log(AddressView.this, "Response:", res); //NON-NLS
                        if(res.length() > 0) {
                            JSONObject address = new JSONObject(res);
                            setTitle(address.getString("display_name")); //NON-NLS
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setTitle(null);
                    }
                }
            }).start();*/
        }
    }

}
