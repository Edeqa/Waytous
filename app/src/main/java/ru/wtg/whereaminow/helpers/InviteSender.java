package ru.wtg.whereaminow.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Created 10/12/16.
 */

public class InviteSender {

    private final Context context;

    public InviteSender(Context context) {
        this.context = context;
    }

    public void send(String link) {

        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        share.putExtra(Intent.EXTRA_SUBJECT, "Link to the tracking");
        share.putExtra(Intent.EXTRA_TEXT, "Follow me at " + link );

        context.startActivity(Intent.createChooser(share, "Invite a friend"));

    }

}
