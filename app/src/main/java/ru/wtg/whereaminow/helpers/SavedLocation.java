package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
import java.util.Date;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.interfaces.SimpleCallback;

/**
 * Created 12/4/16.
 */

public class SavedLocation extends AbstractSavedItem {

    static final long serialVersionUID =-6395904747332820022L;

    public static final String LOCATION = "location";

    private double latitude;
    private double longitude;
    private String title;
    private String username;
    private long timestamp;
    private String address;
    private BitmapDataObject bitmap;

    public SavedLocation(Context context) {
        super(context, LOCATION);
    }

    public static void init(Context context) {
        init(context, SavedLocation.class, LOCATION);
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

    public BitmapDataObject getBitmap() {
        return bitmap;
    }

    public void setBitmap(BitmapDataObject bitmap) {
        this.bitmap = bitmap;
    }

    public void save(final Context context) {

        super.save(new SimpleCallback<AbstractSavedItem>() {
            @Override
            public void call(AbstractSavedItem listItem) {
                final SavedLocation item = (SavedLocation) listItem;
                if(item.getAddress() == null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String req = "http://nominatim.openstreetmap.org/reverse?format=json&lat=" + item.getLatitude() + "&lon=" + item.getLongitude() + "&zoom=18&addressdetails=1";
                                final String res = Utils.getUrl(req);
                                JSONObject o = new JSONObject(res);
                                item.setAddress(o.getString("display_name"));
                                item.save(context);
                            } catch (JSONException | IOException | NullPointerException e) {
                                //e.printStackTrace();
                            }
                        }
                    }).start();
                }
                if(item.getBitmap() == null) {
                    new Thread(new LoadRunnable(context, item, null)).start();
                }
            }
        } );
    }


    public static int getCount(Context context){
        return getCount(context, LOCATION);
    }

    public static SavedLocation getItemByPosition(Context context, int position) {
        return (SavedLocation) getItemByPosition(context, LOCATION, position);
    }

    public static SavedLocation getItemByNumber(Context context, int number) {
        return (SavedLocation) getItemByNumber(context, LOCATION, number);
    }

    public static void clear(Context context){
        clear(context, LOCATION);
    }



    static public class SavedLocationsAdapter extends AbstractSavedItemsAdapter {

        private SimpleCallback<SavedLocation> onLocationClickListener;

        public SavedLocationsAdapter(Context context, RecyclerView list) {
            super(context, LOCATION, list);
        }



        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_saved_location, parent, false);
            return new SavedLocation.SavedLocationsAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
            try {
                final SavedLocation savedLocation = SavedLocation.getItemByPosition(context, position);
                if (savedLocation == null) return;
                final ViewHolder holder = (ViewHolder) viewHolder;
                holder.tvUsername.setText(savedLocation.getUsername());
                holder.tvTimestamp.setText(new Date(savedLocation.getTimestamp()).toString());
                holder.tvAddress.setText(savedLocation.getAddress());
                holder.tvComment.setText(savedLocation.getTitle());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickListener.call(savedLocation);
                    }
                });

                holder.ibImage.setScaleType(ImageView.ScaleType.CENTER);
                holder.ibImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onLocationClickListener.call(savedLocation);
                    }
                });
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if(savedLocation.getBitmap() != null) {
                            holder.ibImage.setImageBitmap(savedLocation.getBitmap().getCurrentImage());
                            holder.ibImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        } else {
                            new Thread(new LoadRunnable(context, savedLocation, new SimpleCallback<Bitmap>() {
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

                            savedLocation.save(context);
                        }
                    }
                });

            } catch(Exception e){e.printStackTrace();}
        }


        public void setOnLocationClickListener(SimpleCallback<SavedLocation> onLocationClickListener) {
            this.onLocationClickListener = onLocationClickListener;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvTimestamp;
            private final TextView tvAddress;
            private final TextView tvUsername;
            private final TextView tvComment;
            private final ImageButton ibImage;

            private ViewHolder(View view) {
                super(view);
                tvUsername = (TextView) view.findViewById(R.id.tv_saved_location_username);
                tvTimestamp = (TextView) view.findViewById(R.id.tv_saved_location_timestamp);
                tvAddress = (TextView) view.findViewById(R.id.tv_saved_location_address);
                tvComment = (TextView) view.findViewById(R.id.tv_saved_location_comment);
                ibImage = (ImageButton) view.findViewById(R.id.ib_saved_location_image);
            }
        }

    }

    public String toString() {
        return "{ timestamp: " + new Date(timestamp).toString()
                + (username != null ? ", username: "+username : "")
                + (title != null ? ", title: "+title : "")
                + (latitude != 0 ? ", latitude: "+latitude : "")
                + (longitude != 0 ? ", longitude: "+longitude : "")
                + (address != null ? ", address: ["+address + "]" : "")
                + " }";
    }


    private static class LoadRunnable implements Runnable {
        private SavedLocation savedLocation;
        private Context context;
        private SimpleCallback<Bitmap> callback;
        LoadRunnable(Context context, SavedLocation savedLocation, SimpleCallback<Bitmap> callback){
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
    };

}