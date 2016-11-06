package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;

import ru.wtg.whereaminow.R;

import static ru.wtg.whereaminowserver.helpers.Constants.USER_ACCURACY;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_ALTITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_BEARING;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LATITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_LONGITUDE;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_PROVIDER;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_SPEED;
import static ru.wtg.whereaminowserver.helpers.Constants.USER_TIMESTAMP;

/**
 * Created by tujger on 10/8/16.
 */

public class Utils {


    public static String getEncryptedHash(String str) {
        return getEncryptedHash(str, 5);
    }

    public static final int DIGEST_METHOD_MD2 = 2;
    public static final int DIGEST_METHOD_MD5 = 5;
    public static final int DIGEST_METHOD_SHA1 = 1;
    public static final int DIGEST_METHOD_SHA256 = 256;
    public static final int DIGEST_METHOD_SHA512 = 512;

    public static String getEncryptedHash(String str, int type) {
        String sType;
        switch (type) {
            case 1:
                sType = "SHA-1";
                break;
            case 2:
                sType = "MD2";
                break;
            case 5:
                sType = "MD5";
                break;
            case 256:
                sType = "SHA-256";
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

    public static float[] getColorMatrix(int color) {

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int a = Color.alpha(color);
        System.out.println("COLOR:"+r+":"+g+":"+b+":"+a);

        if(true)
        return new float[] {
                1, 0, 0, 0, r,
                0, 1, 0, 0, g,
                0, 0, 1, 0, b,
                0, 0, 0, 1, 0
        };

        switch (color) {
            case Color.RED:
                return new float[]{
                        1, 0, 0, 0, 255, //red
                        0, 1, 0, 0, 0, //green
                        0, 0, 1, 0, 0, //blue
                        0, 0, 0, 1, 0 //alpha
                };
            case Color.GREEN:
                return new float[]{
                        1, 0, 0, 0, 0, //red
                        0, 1, 0, 0, 255, //green
                        0, 0, 1, 0, 0, //blue
                        0, 0, 0, 1, 0 //alpha
                };
            case Color.BLUE:
                return new float[]{
                        1, 0, 0, 0, 0, //red
                        0, 1, 0, 0, 0, //green
                        0, 0, 1, 0, 255, //blue
                        0, 0, 0, 1, 0 //alpha
                };
            case Color.MAGENTA:
                return new float[]{
                        1, 0, 0, 0, 0, //red
                        0, 1, 0, 0, 0, //green
                        0, 0, 1, 0, 255, //blue
                        0, 0, 0, 1, 0 //alpha
                };
            case Color.GRAY:
                return new float[]{
                        1, 0, 0, 0, 0, //red
                        0, 1, 0, 0, 0, //green
                        0, 0, 1, 0, 0, //blue
                        0, 0, 0, .5f, 0 //alpha
                };
            default:
                return new float[]{
                        1, 0, 0, 0, 0, //red
                        0, 1, 0, 0, 0, //green
                        0, 0, 1, 0, 0, //blue
                        0, 0, 0, 1, 0 //alpha
                };

        }
    }

    public static Location jsonToLocation(JSONObject json) throws JSONException {
        Location loc = null;
        loc = new Location(json.getString(USER_PROVIDER));
        loc.setLatitude(json.getDouble(USER_LATITUDE));
        loc.setLongitude(json.getDouble(USER_LONGITUDE));
        loc.setAltitude(json.getDouble(USER_ALTITUDE));
        loc.setAccuracy((float) json.getDouble(USER_ACCURACY));
        loc.setBearing((float) json.getDouble(USER_BEARING));
        loc.setSpeed((float) json.getDouble(USER_SPEED));
        loc.setTime(json.getLong(USER_TIMESTAMP));
        return loc;
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

}
