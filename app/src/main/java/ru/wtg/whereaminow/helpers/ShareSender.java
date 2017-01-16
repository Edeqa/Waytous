package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.content.Intent;

/**
 * Created 10/12/16.
 */

public class ShareSender {

    private final Context context;
    private final Intent share;

    public ShareSender(Context context) {
        this.context = context;

        share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    }

    public void sendLink(String link) {

        share.putExtra(Intent.EXTRA_SUBJECT, "Link to the tracking_active");
        share.putExtra(Intent.EXTRA_TEXT, "Follow me at " + link );

        Intent chooser = Intent.createChooser(share, "Invite a friend");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(chooser);

    }

    public void send(String title, String subject, String body) {

        share.putExtra(Intent.EXTRA_SUBJECT, subject);
        share.putExtra(Intent.EXTRA_TEXT, body);

        Intent chooser = Intent.createChooser(share, title);
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(chooser);

    }

}
