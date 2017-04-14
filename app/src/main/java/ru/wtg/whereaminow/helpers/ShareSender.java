package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.content.Intent;

import ru.wtg.whereaminow.R;

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

        share.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.link_to_the_group));
        share.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.follow_me_at_s, link));

        Intent chooser = Intent.createChooser(share, context.getString(R.string.invite_a_friend));
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
