package com.edeqa.waytous.helpers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.Firebase;
import com.edeqa.waytous.R;
import com.edeqa.waytous.abstracts.AbstractSavedItem;
import com.edeqa.waytous.abstracts.AbstractSavedItemsAdapter;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static com.edeqa.waytous.Constants.USER_LATITUDE;
import static com.edeqa.waytous.Constants.USER_LONGITUDE;
import static com.edeqa.waytous.Constants.USER_PROVIDER;
import static com.edeqa.waytous.Firebase.KEYS;
import static com.edeqa.waytous.Firebase.SYNCED;


/**
 * Created 12/4/16.
 */

@SuppressWarnings("WeakerAccess")
public class SavedLocation extends AbstractSavedItem {

    public static final String LOCATION = "location";

    public static final String DESCRIPTION = "d";
    public static final String ADDRESS = "a";
    public static final String USERNAME = "n";
    public static final String NUMBER = "number";
    static final long serialVersionUID =-6395904747332820022L;

    private String key;
    private double latitude;
    private double longitude;
    private String title;
    private String username;
    private String provider;
    private long timestamp;
    private long synced;
    private String address;
    private BitmapDataObject bitmap;

    public SavedLocation(Context context) {
        super(context, LOCATION);
        timestamp = new Date().getTime();
        provider = "saved"; //NON-NLS
    }

    public static void init(Context context) {
        init(context, SavedLocation.class, LOCATION);
    }

    public static DBHelper getDb(){
        return getDb(LOCATION);
    }

    public static SavedLocation getItemByPosition(int position) {
        return (SavedLocation) getItemByPosition(LOCATION, position);
    }

    public static SavedLocation getItemByNumber(long number) {
        return (SavedLocation) getItemByNumber(LOCATION, number);
    }

    public static SavedLocation getItemByCursor(Cursor cursor) {
        return (SavedLocation) getItemByCursor(LOCATION, cursor);
    }

    public static SavedLocation getItemByFieldValue(String field, String value) {
        return (SavedLocation) getItemByFieldValue(LOCATION, field, value);
    }

    public static void clear(){
        clear(LOCATION);
    }

    public static int getCount(){
        return getCount(LOCATION);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getShortAddress() {
        String[] parts = address.split(", ");
        int max = 3;
        if(parts.length<max) max = parts.length;
        return TextUtils.join(", ", Arrays.copyOfRange(parts, 0, max));
    }

    public BitmapDataObject getBitmap() {
        return bitmap;
    }

    public void setBitmap(BitmapDataObject bitmap) {
        this.bitmap = bitmap;
    }

    public void save(final Context context) {
        //noinspection unchecked
        super.save(new Runnable1<SavedLocation>() {
            @Override
            public void call(final SavedLocation listItem) {
                if(listItem.getAddress() == null && (listItem.getLatitude() != 0.0 || listItem.getLongitude() != 0.0)) {
                    new AddressResolver(context)
                            .setLatLng(new LatLng(listItem.getLatitude(), listItem.getLongitude()))
                            .setCallback(new Runnable1<String>() {
                                @Override
                                public void call(String formattedAddress) {
                                    listItem.setAddress(formattedAddress);
                                    listItem.setTitle(formattedAddress);
                                    listItem.save(context);
                                }
                            })
                            .resolve();

                }
                if(listItem.getBitmap() == null && (listItem.getLatitude() != 0.0 || listItem.getLongitude() != 0.0)) {
                    new Thread(new LoadBitmap(context, listItem, null)).start();
                }
            }
        } );
    }

    public void delete(Context context) {
        setAddress(null);
        setUsername(null);
        setBitmap(null);
        setTitle(null);
        setLongitude(0);
        setLatitude(0);
        setDeleted(true);
        save(context);
    }

    public static SavedLocation newLocation(Context context, Map map) {
        SavedLocation location = new SavedLocation(context);

        if(map.containsKey(Firebase.KEYS)) try { location.setKey((String) map.get(Firebase.KEYS)); } catch (Exception e) {}
        if(map.containsKey(USER_LATITUDE)) try { location.setLatitude((Double) map.get(USER_LATITUDE)); } catch (Exception e) {}
        if(map.containsKey(USER_LONGITUDE)) try { location.setLongitude((Double) map.get(USER_LONGITUDE)); } catch (Exception e) {}
        if(map.containsKey(Firebase.SYNCED)) try { location.setSynced((Long) map.get(Firebase.SYNCED)); } catch (Exception e) {}
        if(map.containsKey(USER_PROVIDER)) try { location.setProvider((String) map.get(USER_PROVIDER)); } catch (Exception e) {}
        if(map.containsKey(DESCRIPTION)) try { location.setTitle((String) map.get(DESCRIPTION)); } catch (Exception e) {}
        if(map.containsKey(ADDRESS)) try { location.setAddress((String) map.get(ADDRESS)); } catch (Exception e) {}
        if(map.containsKey(USERNAME)) try { location.setUsername((String) map.get(USERNAME)); } catch (Exception e) {}
        if(map.containsKey(NUMBER)) try { location.setNumber((Long) map.get(NUMBER)); } catch (Exception e) {}
        if(map.containsKey(DELETED)) try { location.setDeleted((Boolean) map.get(DELETED)); } catch (Exception e) {}

        return location;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    public String toString() {
        return "{ timestamp: " + new Date(timestamp).toString()
                + ", number: "+getNumber()
                + (key != null ? ", key: "+key : "")
                + (synced != 0 ? ", synced: "+new Date(synced).toString() : "")
                + (username != null ? ", username: "+username : "")
                + (title != null ? ", title: "+title : "")
                + (latitude != 0 ? ", latitude: "+latitude : "")
                + (longitude != 0 ? ", longitude: "+longitude : "")
                + (address != null ? ", address: ["+address + "]" : "")
                + (bitmap != null ? ", bitmap: ["+bitmap.getCurrentImage().getByteCount() + "]" : "")
                + " }";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public long getSynced() {
        return synced;
    }

    public void setSynced(long synced) {
        this.synced = synced;
    }

    static public class SavedLocationsAdapter extends AbstractSavedItemsAdapter {

        private Runnable1<SavedLocation> onLocationClickListener;

        public SavedLocationsAdapter(Context context, RecyclerView list) {
            super(context, list);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_saved_location, parent, false);
            return new SavedLocation.SavedLocationsAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
            try {
                final ViewHolder holder = (ViewHolder) viewHolder;

                final SavedLocation item = SavedLocation.getItemByCursor(cursor);
//
//                final SavedLocation savedLocation = SavedLocation.getItemByPosition(context, position);
//                if (savedLocation == null) return;
//                final ViewHolder holder = (ViewHolder) viewHolder;
                holder.tvUsername.setText(item.getUsername());
                holder.tvTimestamp.setText(new Date(item.getTimestamp()).toString());

                if (item.getAddress() != null && item.getAddress().length() > 0) {
                    holder.tvAddressShort.setText(item.getShortAddress());
                    holder.tvAddress.setText(item.getAddress());
                    holder.tvAddressShort.setVisibility(View.VISIBLE);
                    holder.tvAddress.setVisibility(View.GONE);
                } else {
                    holder.tvAddressShort.setVisibility(View.GONE);
                    holder.tvAddress.setVisibility(View.GONE);
                }

                if (item.getTitle() != null && item.getTitle().length() > 0) {
                    holder.tvComment.setText(item.getTitle());
                    holder.tvComment.setVisibility(View.VISIBLE);
                    holder.tvComment.setMaxLines(3);
                } else {
                    holder.tvComment.setVisibility(View.GONE);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //noinspection unchecked
                        onItemClickListener.call(item);
                    }
                });

                holder.ibImage.setScaleType(ImageView.ScaleType.CENTER);
                holder.ibImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onLocationClickListener.call(item);
                    }
                });
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (item.getBitmap() != null) {
                            holder.ibImage.setImageBitmap(item.getBitmap().getCurrentImage());
                            holder.ibImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        } else {
                            new Thread(new LoadBitmap(context, item, new Runnable1<Bitmap>() {
                                @Override
                                public void call(final Bitmap bmp) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (bmp != null) {
                                                holder.ibImage.setImageBitmap(bmp);
                                                holder.ibImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                            }
                                        }
                                    });
                                }
                            })).start();

                            item.save(context);
                        }
                    }
                });

                holder.ibExpand.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.ibExpand.setVisibility(View.INVISIBLE);
                        holder.ibCollapse.setVisibility(View.VISIBLE);
                        holder.tvAddressShort.setVisibility(View.GONE);
                        holder.tvAddress.setVisibility(View.VISIBLE);
                        holder.tvComment.setMaxLines(1000);
                    }
                });
                holder.ibCollapse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.ibExpand.setVisibility(View.VISIBLE);
                        holder.ibCollapse.setVisibility(View.INVISIBLE);
                        holder.tvAddressShort.setVisibility(View.VISIBLE);
                        holder.tvAddress.setVisibility(View.GONE);
                        holder.tvComment.setMaxLines(3);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public void setOnLocationClickListener(Runnable1<SavedLocation> onLocationClickListener) {
            this.onLocationClickListener = onLocationClickListener;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new SavedItemCursorLoader(context, LOCATION);
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvTimestamp;
            private final TextView tvAddress;
            private final TextView tvAddressShort;
            private final TextView tvUsername;
            private final TextView tvComment;
            private final ImageButton ibImage;
            private final ImageButton ibExpand;
            private final ImageButton ibCollapse;
            View.OnTouchListener onTouchListener = new View.OnTouchListener() {
                float prevY = -1;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (prevY != -1) {
                            if (event.getY() > prevY) {
                                ibExpand.performClick();
                            } else if (prevY > event.getY()) {
                                ibCollapse.performClick();
                            }
                        }
                        prevY = event.getY();
                    } else {
                        prevY = -1;
                    }
                    return false;
                }
            };

            private ViewHolder(View view) {
                super(view);
                tvUsername = (TextView) view.findViewById(R.id.tv_saved_location_username);
                tvTimestamp = (TextView) view.findViewById(R.id.tv_saved_location_timestamp);
                tvAddressShort = (TextView) view.findViewById(R.id.tv_saved_location_address_short);
                tvAddress = (TextView) view.findViewById(R.id.tv_saved_location_address);
                tvComment = (TextView) view.findViewById(R.id.tv_saved_location_comment);
                ibImage = (ImageButton) view.findViewById(R.id.ib_saved_location_image);
                ibExpand = (ImageButton) view.findViewById(R.id.ib_expand_address);
                ibCollapse = (ImageButton) view.findViewById(R.id.ib_collapse_address);

                tvUsername.setOnTouchListener(onTouchListener);
                view.findViewById(R.id.layout_address).setOnTouchListener(onTouchListener);
            }
        }

    }

    private static class LoadBitmap implements Runnable {
        private SavedLocation savedLocation;
        private Context context;
        private Runnable1<Bitmap> callback;

        LoadBitmap(Context context, SavedLocation savedLocation, Runnable1<Bitmap> callback){
            this.context = context;
            this.savedLocation = savedLocation;
            this.callback = callback;
        }
        @Override
        public void run() {
            String URL = "http://maps.google.com/maps/api/staticmap?center=" +savedLocation.getLatitude() + "," + savedLocation.getLongitude() + "&zoom=15&size=200x200&sensor=false" +
                    "&markers=color:darkgreen|"+savedLocation.getLatitude()+","+savedLocation.getLongitude();

            try {
                URL url = new URL(URL);
                Utils.log(LoadBitmap.this, "run:", "url="+url);
                InputStream in = url.openStream();

                final Bitmap bmp = BitmapFactory.decodeStream(in);
                in.close();

                savedLocation.setBitmap(new BitmapDataObject(bmp));
                savedLocation.save(context);

                if(callback != null) {
                    callback.call(bmp);
                }
            } catch (IllegalStateException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}