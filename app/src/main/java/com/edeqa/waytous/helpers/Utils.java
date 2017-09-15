package com.edeqa.waytous.helpers;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

import static com.edeqa.waytous.Constants.REQUEST_TIMESTAMP;
import static com.edeqa.waytous.Constants.SENSITIVE;
import static com.edeqa.waytous.Constants.USER_ACCURACY;
import static com.edeqa.waytous.Constants.USER_ALTITUDE;
import static com.edeqa.waytous.Constants.USER_BEARING;
import static com.edeqa.waytous.Constants.USER_LATITUDE;
import static com.edeqa.waytous.Constants.USER_LONGITUDE;
import static com.edeqa.waytous.Constants.USER_PROVIDER;
import static com.edeqa.waytous.Constants.USER_SPEED;


/**
 * Created 10/8/16.
 */

public class Utils {


    public static final int DIGEST_METHOD_MD2 = 2;
    public static final int DIGEST_METHOD_MD5 = 5;
    public static final int DIGEST_METHOD_SHA1 = 1;
    public static final int DIGEST_METHOD_SHA256 = 256;
    public static final int DIGEST_METHOD_SHA512 = 512;
    public static final int MATCH_SCREEN = -100;

    public static String getEncryptedHash(String str) {
        return getEncryptedHash(str, 5);
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    public static String getEncryptedHash(String str, int type) {
        String sType;
        switch (type) {
            case 1:
                sType = "SHA-1";
                break;
            case 5:
                sType = "MD5";
                break;
            case 224:
                sType = "SHA-224";
                break;
            case 256:
                sType = "SHA-256";
                break;
            case 384:
                sType = "SHA-384";
                break;
            case 512:
                sType = "SHA-512";
                break;
            default:
                sType = "SHA-512";
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(sType);
            messageDigest.update(str.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes) {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static float[] getColorMatrix(int color) {

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int a = Color.alpha(color);

        return new float[] {
                1, 0, 0, 0, r,
                0, 1, 0, 0, g,
                0, 0, 1, 0, b,
                0, 0, 0, 1, 0
        };
    }

    public static Location jsonToLocation(JSONObject json) throws JSONException {
        Location loc = new Location(json.getString(USER_PROVIDER));
        loc.setLatitude(json.getDouble(USER_LATITUDE));
        loc.setLongitude(json.getDouble(USER_LONGITUDE));
        loc.setAltitude(json.has(USER_ALTITUDE) ? json.getDouble(USER_ALTITUDE) : 0);
        loc.setAccuracy(json.has(USER_ACCURACY) ? (float) json.getDouble(USER_ACCURACY) : 0);
        loc.setBearing(json.has(USER_BEARING) ? (float) json.getDouble(USER_BEARING): 0);
        loc.setSpeed(json.has(USER_SPEED) ? (float) json.getDouble(USER_SPEED): 0);
        loc.setTime(json.getLong(REQUEST_TIMESTAMP));
        return loc;
    }

    public static JSONObject locationToJson(Location location) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(USER_PROVIDER,location.getProvider());
        json.put(USER_LATITUDE,location.getLatitude());
        json.put(USER_LONGITUDE,location.getLongitude());
        json.put(USER_ALTITUDE,location.getAltitude());
        json.put(USER_ACCURACY,location.getAccuracy());
        json.put(USER_BEARING,location.getBearing());
        json.put(USER_SPEED,location.getSpeed());
        json.put(REQUEST_TIMESTAMP,location.getTime());
        return json;
    }

    public static Drawable renderDrawable(Context context, int resource, int color){
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getResources().getDrawable(resource,context.getTheme());
        } else {
            drawable = /*ContextCompat.getDrawable(context, R.drawable.navigation_marker);*/ context.getResources().getDrawable(resource);
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        return renderDrawable(context,resource,color,width,height);
    }

    public static Drawable renderDrawable(Context context, int resource, int color, int width, int height){
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getResources().getDrawable(resource,context.getTheme());
        } else {
            drawable = /*ContextCompat.getDrawable(context, R.drawable.navigation_marker);*/ context.getResources().getDrawable(resource);

        }
        drawable.setColorFilter(new ColorMatrixColorFilter(Utils.getColorMatrix(color)));
        Canvas canvas = new Canvas();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return drawable;
    }

    public static Bitmap renderBitmap(Context context, int resource, int color){
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getResources().getDrawable(resource,context.getTheme());
        } else {
            drawable = /*ContextCompat.getDrawable(context, R.drawable.navigation_marker);*/ context.getResources().getDrawable(resource);
        }
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        return renderBitmap(context,resource,color,width,height);
    }

    public static Bitmap renderBitmap(Context context, int resource, int color, int width, int height){
        Drawable drawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getResources().getDrawable(resource,context.getTheme());
        } else {
            drawable = /*ContextCompat.getDrawable(context, R.drawable.navigation_marker);*/ context.getResources().getDrawable(resource);

        }
        drawable.setColorFilter(new ColorMatrixColorFilter(Utils.getColorMatrix(color)));
        Canvas canvas = new Canvas();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return bitmap;
    }

    public static String getUrl(String url) throws IOException {
        return Utils.getUrl(url, "UTF-8");
    }

    public static String getUrl(String url, String urlCharset) throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        HttpURLConnection feedUrl;
        feedUrl = (HttpURLConnection) new URL(url).openConnection();
        feedUrl.setConnectTimeout(5000);
        feedUrl.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; ru; rv:1.8.1.12) Gecko/20080201 Firefox");
        feedUrl.setRequestProperty("Accept", "*/*");

        int status = feedUrl.getResponseCode();
        if(status >= 200 && status < 300) {
            in = feedUrl.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, urlCharset));
            while ((line = reader.readLine()) != null) {
                sb.append(new String(line.getBytes("UTF-8"))).append("\n");
            }
            in.close();
        } else {
            log("Utils","getUrl:status:"+status);
        }
        return sb.toString();
    }

    public static String getUnique() {
        return new BigInteger(48, new SecureRandom()).toString(32).toUpperCase();
    }

    public static Location normalizeLocation(GeoTrackFilter filter, Location location) {
        filter.update_velocity2d(location.getLatitude(),location.getLongitude(),location.getTime());
        double[] latlng = filter.get_lat_long();
        location.setLatitude(latlng[0]);
        location.setLongitude(latlng[1]);
        if(SENSITIVE.isDebugMode()) location.setBearing((float) filter.get_bearing());
        location.setSpeed((float) filter.get_speed(location.getAltitude()));
        return location;

    }

    /** Read the object from Base64 string. */
    public static Object deserializeFromString( String s ) {
        Object o = null;
        try {
            byte [] data = Base64.decode( s, android.util.Base64.DEFAULT);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            o  = ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
        }
        return o;
    }

    /** Write the object to a Base64 string. */
    public static String serializeToString( Serializable o ) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream( baos );
            oos.writeObject( o );
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    public static LatLng findPoint(List<LatLng> points, double fraction) {

        double length = 0;
        for(int i=1; i<points.size();i++) {
            length += SphericalUtil.computeDistanceBetween(points.get(i-1),points.get(i));
        }

        length = length * fraction;

        for(int i=1; i<points.size();i++) {
            double current = SphericalUtil.computeDistanceBetween(points.get(i-1),points.get(i));
            if(length - current < 0) {
                return SphericalUtil.interpolate(points.get(i-1),points.get(i), length / current);
            } else {
                length -= current;
            }
        }
        return SphericalUtil.interpolate(points.get(0),points.get(points.size()-1), fraction);
    }

    public static LatLng latLng(Location location){
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static String formatLengthToLocale(double meters) {
        if(Locale.US.equals(Locale.getDefault())) {
            meters = meters * 3.2808399;
            if(meters < 530) {
                return String.format("%4.0f %s", meters, "ft");
            } else {
                meters = meters / 5280;
                return String.format("%4.1f %s", meters, "mi");
            }
        } else {
            String unit = "m";
            if (meters < 1) {
                meters *= 1000;
                unit = "mm";
            } else if (meters > 1000) {
                meters /= 1000;
                unit = "km";
            }
            return String.format("%4.1f %s", meters, unit);
        }
    }

    public static LatLngBounds reduce(LatLngBounds bounds, double fraction) {

        LatLng newNortheast = SphericalUtil.interpolate(bounds.northeast, bounds.southwest, (1+fraction)/2);
        LatLng newSouthwest = SphericalUtil.interpolate(bounds.southwest, bounds.northeast, (1+fraction)/2);

        return new LatLngBounds(newNortheast,newSouthwest);
    }

    public static void updateMarkerPosition(final GoogleMap map, final Marker marker, final List<LatLng> points) {
        if(marker == null || points == null || points.size() < 2) return;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    LatLng mePosition = points.get(0);
                    LatLng userPosition = points.get(points.size() - 1);
                        LatLng markerPosition = Utils.findPoint(points, .5);
                        LatLngBounds bounds = Utils.reduce(map.getProjection().getVisibleRegion().latLngBounds, .8);
                        if (!bounds.contains(markerPosition) && (bounds.contains(mePosition) || bounds.contains(userPosition))) {
                            if (!bounds.contains(markerPosition)) {
                                double fract = 0.5;
                                while (!bounds.contains(markerPosition)) {
                                    fract = fract + (bounds.contains(mePosition) ? -1 : +1) * .01;
                                    if (fract < 0 || fract > 1) break;
                                    markerPosition = Utils.findPoint(points, fract);
                                }
                            }
                        }
                        marker.setPosition(markerPosition);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void resizeDialog(Activity activity, Dialog dialog, int width, int height) {
        if(width == MATCH_SCREEN || height == MATCH_SCREEN) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            if(width == MATCH_SCREEN) {
                width = displaymetrics.widthPixels;
            }
            if(height == MATCH_SCREEN) {
                height = displaymetrics.heightPixels;
            }
        }
        dialog.getWindow().setLayout(width, height);
    }

    public static int adaptedSize(Context context,int size) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, context.getResources().getDisplayMetrics());
    }

    public static void log(Object... text) {
        String str = "";
        String tag = "Utils";
        int count = 0;
        for (Object aText : text) {
            if(aText instanceof Serializable) {
                str += aText.toString() + " ";
            } else if((count++) == 0) {
//                str += aText.getClass().getSimpleName() + ": ";
                tag = aText.getClass().getSimpleName();
            } else if(aText == null) {
                str += "null ";
            } else {
                str += aText.toString() + " ";
            }
        }
        Log.i(tag, str);
    }

    public static void err(Object... text) {
        String str = "";
        String tag = "Utils";
        Throwable e = null;
        int count = 0;
        for (Object aText : text) {
            if (aText instanceof Throwable) {
                str += aText + " ";
                e = (Throwable) aText;
            } else if(aText instanceof Serializable) {
                str += aText.toString() + " ";
            } else if((count++) == 0) {
                tag = aText.getClass().getSimpleName();
//                str += aText.getClass().getSimpleName() + ": ";
            } else if(aText == null) {
                str += "null ";
            } else {
                str += aText.toString() + " ";
            }
        }
        Log.e(tag, str);
        if(e != null) e.printStackTrace();
    }

    public static String toDateString(long millis) {
        StringBuilder res = new StringBuilder();
        int delta = Math.round(Math.round(millis/1000));

        int secInDay = 24 * 60 * 60;
        int secInHour = 24 * 60;
        int secInMinute = 60;

        int d = Math.round(Math.round(delta / secInDay));
        delta -= d * secInDay;

        int h = Math.round(Math.round(delta / secInHour));
        delta -= h * secInHour;

        int m = Math.round(Math.round(delta / secInMinute));
        int s = delta - m * secInMinute;

        if(d > 0) {
            res.append(d + "d");
        }
        if(h > 0) {
            if(res.length() > 0) res.append(" ");
            res.append(h + "h");
        }
        if(m > 0) {
            if(res.length() > 0) res.append(" ");
            res.append(m + "m");
        }
        if(res.length() > 0) res.append(" ");
        res.append((s > 0 ? s : "0") + "s");
        return res.toString();
    }


    public static String getWrappedHttpPort(){
        return SENSITIVE.getHttpPortMasked() == 80 ? "" : ":" + SENSITIVE.getHttpPortMasked();
    }

    public static String getWrappedHttpsPort(){
        return SENSITIVE.getHttpsPortMasked() == 443 ? "" : ":" + SENSITIVE.getHttpsPortMasked();
    }

}
