package ru.wtg.whereaminow.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.io.PrintWriter;
import java.io.StringWriter;

import static ru.wtg.whereaminowserver.helpers.Constants.SHARE_ERROR;

/**
 * Created by tujger on 11/22/16.
 */

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Context context;

    public GlobalExceptionHandler(Context context) {
        this.context = context;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String exceptionAsString = sw.toString();

        System.err.println(exceptionAsString);

        if(SHARE_ERROR) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Uncaught error");
            intent.putExtra(Intent.EXTRA_TEXT, exceptionAsString);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

            context.startActivity(intent);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        },500);
    }
}