package ru.wtg.whereaminow.helpers;

import android.app.Activity;
import android.content.Intent;

import static ru.wtg.whereaminowserver.helpers.Constants.HTTP_SERVER_URL;

/**
 * Created by tujger on 10/12/16.
 */

public class InviteSender {

    private final Activity context;

    public InviteSender(Activity context) {
        this.context = context;
    }

    public void send(String token) {

        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        share.putExtra(Intent.EXTRA_SUBJECT, "Link to the tracking");
        share.putExtra(Intent.EXTRA_TEXT, "Follow me at " + HTTP_SERVER_URL + "/track/"+ token);

        context.startActivity(Intent.createChooser(share, "Send link to a friend"));

    }

}
