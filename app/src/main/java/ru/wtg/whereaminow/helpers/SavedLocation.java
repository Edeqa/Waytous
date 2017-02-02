package ru.wtg.whereaminow.helpers;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

/**
 * Created 12/4/16.
 */

@SuppressWarnings("WeakerAccess")
public class SavedLocation extends AbstractSavedItem {

    public static final String LOCATION = "location";
    static final long serialVersionUID =-6395904747332820022L;

    private String key;
    private double latitude;
    private double longitude;
    private String title;
    private String username;
    private long timestamp;
    private String address;
    private BitmapDataObject bitmap;

    public SavedLocation(Context context) {
        super(context, LOCATION);
        timestamp = new Date().getTime();
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
        super.save(new SimpleCallback<SavedLocation>() {
            @Override
            public void call(SavedLocation listItem) {
                if(listItem.getAddress() == null) {
                    new Thread(new LoadAddress(context, listItem, null)).start();
                }
                if(listItem.getBitmap() == null) {
                    new Thread(new LoadBitmap(context, listItem, null)).start();
                }
            }
        } );
    }

    public String toString() {
        return "{ timestamp: " + new Date(timestamp).toString()
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

    static public class SavedLocationsAdapter extends AbstractSavedItemsAdapter {

        private SimpleCallback<SavedLocation> onLocationClickListener;

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
                            new Thread(new LoadBitmap(context, item, new SimpleCallback<Bitmap>() {
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


        public void setOnLocationClickListener(SimpleCallback<SavedLocation> onLocationClickListener) {
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
        private SimpleCallback<Bitmap> callback;

        LoadBitmap(Context context, SavedLocation savedLocation, SimpleCallback<Bitmap> callback){
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
                System.out.println(url);
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

    private static class LoadAddress implements Runnable {
        private SavedLocation savedLocation;
        private Context context;
        private SimpleCallback<String> callback;

        LoadAddress(Context context, SavedLocation savedLocation, SimpleCallback<String> callback){
            this.context = context;
            this.savedLocation = savedLocation;
            this.callback = callback;
        }
        @Override
        public void run() {
            try {
                String req = "http://nominatim.openstreetmap.org/reverse?format=json&lat=" + savedLocation.getLatitude() + "&lon=" + savedLocation.getLongitude() + "&zoom=18&addressdetails=1";
                final String res = Utils.getUrl(req);
                JSONObject o = new JSONObject(res);

                savedLocation.setAddress(o.getString("display_name"));
                savedLocation.save(context);

                if(callback != null) {
                    callback.call(o.getString("display_name"));
                }
            } catch (JSONException | IOException | NullPointerException e) {
                //e.printStackTrace();
            }
        }
    }
}