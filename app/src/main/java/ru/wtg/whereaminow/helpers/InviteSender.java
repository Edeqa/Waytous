package ru.wtg.whereaminow.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created 10/12/16.
 */

public class InviteSender {

    private final Context context;

    public InviteSender(Context context) {
        this.context = context;
    }

    public void send(String link) {

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.putExtra(Intent.EXTRA_SUBJECT, "Link to the tracking_active");
        share.putExtra(Intent.EXTRA_TEXT, "Follow me at " + link );

        Intent chooser = Intent.createChooser(share, "Invite a friend");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(chooser);

    }

}
