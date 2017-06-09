package com.edeqa.waytous.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.edeqa.waytous.R;
import com.edeqa.waytous.interfaces.Callable1;


/**
 * Created 11/29/16.
 */
public class ContinueDialog {
    private final Context context;
    private String message;
    private Callable1 callback;

    public ContinueDialog(Context context) {
        this.context = context;
    }

    public ContinueDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public ContinueDialog setCallback(Callable1 callback){
        this.callback = callback;
        return this;
    }

    public void show(){

        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle(context.getString(R.string.alert));
        dialog.setIcon(R.drawable.ic_warning_black_24dp);
        dialog.setMessage(message);

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.continue_string), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //noinspection unchecked
                callback.call(null);
            }
        });

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();

    }
}
