package ru.wtg.whereaminow.holders;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;

import static ru.wtg.whereaminow.helpers.MyUser.CHANGE_NAME;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_CHANGE_NAME;
import static ru.wtg.whereaminow.helpers.MyUser.MENU_ITEM_NAVIGATE;

/**
 * Created 11/18/16.
 */
public class MenuViewHolder extends AbstractViewHolder<MenuViewHolder.MenuView> {
    private static final String TYPE = "contextMenu";
    private final Activity context;

    public MenuViewHolder(Activity context) {
        this.context = context;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public MenuView create(MyUser myUser) {
        if (myUser == null) return null;
        return this.new MenuView(myUser);
    }

    public class MenuView extends AbstractView {
        private final MyUser myUser;

        public MenuView(MyUser myUser){
            this.myUser = myUser;
        }

        @Override
        public void onEvent(int event, Object object) {
            if(object == null) return;
            switch (event){
                case MENU_ITEM_NAVIGATE:
                    MenuItem item = (MenuItem) object;
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Uri uri = Uri.parse("google.navigation:q="
                                    + String.valueOf(myUser.getLocation().getLatitude())
                                    + "," + String.valueOf(myUser.getLocation().getLongitude()));
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            try {
                                context.startActivity(intent);
                            } catch(ActivityNotFoundException ex) {
                                try {
                                    Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, uri);
                                    unrestrictedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    context.startActivity(unrestrictedIntent);
                                } catch(ActivityNotFoundException innerEx) {
                                    Toast.makeText(context.getApplicationContext(), "Please install a navigation application.", Toast.LENGTH_LONG).show();
                                }
                            }
                            return false;
                        }
                    });
                    break;
                case MENU_ITEM_CHANGE_NAME:
                    item = (MenuItem) object;
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Set my name");

                            View layoutDialogSetMyName = context.getLayoutInflater().inflate(R.layout.dialog_set_my_name, null);

                            builder.setView(layoutDialogSetMyName);
                            final EditText etMyName = (EditText) layoutDialogSetMyName.findViewById(R.id.etMyName);
                            String name = State.getInstance().getStringPreference("my_name","");

                            if(name != null && name.length()>0){
                                etMyName.setText(name);
                                builder.setNeutralButton("Remove", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        myUser.fire(CHANGE_NAME, null);
                                    }
                                });
                            }
                            builder.setPositiveButton(context.getString(android.R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            myUser.fire(CHANGE_NAME,etMyName.getText().toString());
                                        }
                                    });

                            builder.setNegativeButton(context.getString(android.R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            System.out.println("CANCEL");
                                        }
                                    });

                            builder.create().show();
                            return false;
                        }
                    });
                    break;
            }
        }

        @Override
        public boolean dependsOnLocation() {
            return false;
        }

    }
}
