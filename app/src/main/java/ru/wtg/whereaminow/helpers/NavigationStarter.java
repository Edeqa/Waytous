package ru.wtg.whereaminow.helpers;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import ru.wtg.whereaminow.R;

/**
 * Created 12/11/16.
 */

public class NavigationStarter {

    private final double latitude;
    private final double longitude;
    private final Context context;

    public NavigationStarter(Context context, double latitude, double longitude){
        this.context = context;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void start(){
        Uri uri = Uri.parse("google.navigation:q="
                + String.valueOf(latitude)
                + "," + String.valueOf(longitude));
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            try {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, uri);
                unrestrictedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(unrestrictedIntent);
            } catch (ActivityNotFoundException innerEx) {
                Toast.makeText(context.getApplicationContext(), R.string.please_install_a_navigation_application, Toast.LENGTH_LONG).show();
            }
        }
    }
}
