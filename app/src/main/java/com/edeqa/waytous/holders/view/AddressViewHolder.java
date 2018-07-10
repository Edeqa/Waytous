package com.edeqa.waytous.holders.view;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.edeqa.helpers.interfaces.BiConsumer;
import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.AddressResolver;
import com.edeqa.waytous.helpers.MyUser;

import io.nlopez.smartlocation.SmartLocation;

import static com.edeqa.waytous.helpers.Events.SELECT_SINGLE_USER;
import static com.edeqa.waytous.helpers.Events.SELECT_USER;
import static com.edeqa.waytous.helpers.Events.UNSELECT_USER;

/**
 * Created 11/18/16.
 */
@SuppressWarnings("unused")
public class AddressViewHolder extends AbstractViewHolder<AddressViewHolder.AddressView> {

    private static final String TYPE = "address"; //NON-NLS
    private Consumer<String> callback;

    public AddressViewHolder(final MainActivity context) {
        super(context);
        setCallback(new Consumer<String>() {
            @Override
            public void accept(String text) {
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

    public AddressViewHolder setCallback(Consumer<String> callback) {
        this.callback = callback;
        return this;
    }

    private void setTitle(final String text){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                callback.accept(text);
            }
        });
    }

    class AddressView extends AbstractView {
        private final SmartLocation.GeocodingControl geocoding;
        private long lastRequestTimestamp;
        private AddressResolver addressResolver;
        private String lastKnownAddress;

        AddressView(MainActivity context, final MyUser myUser) {
            super(context, myUser);

            Object object = myUser.getProperties().loadFor(TYPE);
            if(object != null) {
                lastKnownAddress = (String) object;
            }
            geocoding = SmartLocation.with(context).geocoding();
            addressResolver = new AddressResolver(context);
            addressResolver.setUser(myUser);
            addressResolver.setCallback(new Consumer<String>() {
                @Override
                public void accept(String formattedAddress) {
                    lastKnownAddress = formattedAddress;
                    if(State.getInstance().getUsers().getCountSelectedTotal() == 1 && myUser.getProperties().isSelected()) {
                        setTitle(formattedAddress);
                    }
                }
            });
        }

        @Override
        public void remove() {
            super.remove();
            geocoding.stop();
            myUser.getProperties().saveFor(TYPE, lastKnownAddress);
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
                    if(State.getInstance().getUsers().getCountSelectedTotal() > 1) {
                        callback.accept(context.getString(R.string.d_selected, State.getInstance().getUsers().getCountSelectedTotal()));
                    } else {
                        State.getInstance().getUsers().forSelectedUsers(new BiConsumer<Integer, MyUser>() {
                            @Override
                            public void accept(Integer number, MyUser myUser) {
                                ((AddressView) myUser.getView(TYPE)).resolveAddress(myUser.getLocation());
                            }
                        });
                    }
                    break;
                case SELECT_SINGLE_USER:
                    resolveAddress(myUser.getLocation());
                    break;
            }
            return true;
        }

        @SuppressWarnings("WeakerAccess")
        public void resolveAddress(final Location location) {
            if(State.getInstance().getUsers().getCountSelectedTotal() > 1) {
                callback.accept(context.getString(R.string.d_selected, State.getInstance().getUsers().getCountSelectedTotal()));
                return;
            } else if(myUser.getProperties().isSelected() && location == null) {
                callback.accept(null);
            }

            if(lastKnownAddress != null) setTitle(lastKnownAddress);
            addressResolver.resolve();
            /*long currentTimestamp = new Date().getTime();
            if(currentTimestamp - lastRequestTimestamp < 5000) return;
            lastRequestTimestamp = currentTimestamp;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    *//*try {
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
                    }*//*

//                    callback.accept("...");

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
