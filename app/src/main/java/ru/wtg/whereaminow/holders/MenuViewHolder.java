package ru.wtg.whereaminow.holders;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.MyUser;
import ru.wtg.whereaminow.helpers.NavigationStarter;

import static ru.wtg.whereaminow.State.CHANGE_NAME;
import static ru.wtg.whereaminow.State.CREATE_CONTEXT_MENU;
import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_DAY;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_NIGHT;
import static ru.wtg.whereaminowserver.helpers.Constants.DEBUGGING;

/**
 * Created 11/18/16.
 */
public class MenuViewHolder extends AbstractViewHolder<MenuViewHolder.MenuView> {
    private static final String TYPE = "menu";

    private final Activity context;
    private boolean day = true;

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

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case CREATE_OPTIONS_MENU:
                Menu optionsMenu = (Menu) object;
                if(DEBUGGING) {
                    optionsMenu.add("Switch day/night mode").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            State.getInstance().fire((day = !day) ? REQUEST_MODE_DAY : REQUEST_MODE_NIGHT);
                            return true;
                        }
                    });
                }
                optionsMenu.add("Set my name").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Set my name");

                        View layoutDialogSetMyName = context.getLayoutInflater().inflate(R.layout.dialog_set_my_name, null);

                        builder.setView(layoutDialogSetMyName);
                        final EditText etMyName = (EditText) layoutDialogSetMyName.findViewById(R.id.et_my_name);
                        String name = State.getInstance().getStringPreference("my_name","");

                        if(name != null && name.length()>0){
                            etMyName.setText(name);
                            builder.setNeutralButton("Remove", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    State.getInstance().getMe().fire(CHANGE_NAME, null);
                                }
                            });
                        }
                        builder.setPositiveButton(context.getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        State.getInstance().getMe().fire(CHANGE_NAME,etMyName.getText().toString());
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
        return true;
    }

    public class MenuView extends AbstractView {
        private final MyUser myUser;

        public MenuView(MyUser myUser){
            this.myUser = myUser;
        }

        @Override
        public boolean onEvent(String event, Object object) {
            if(object == null) return true;
            switch (event){
                case CREATE_CONTEXT_MENU:
                    ContextMenu contextMenu = (ContextMenu) object;
                    MenuItem item = contextMenu.findItem(R.id.action_navigate);
                    item.setVisible(myUser != State.getInstance().getMe());
                    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                new NavigationStarter(context, myUser.getLocation().getLatitude(), myUser.getLocation().getLongitude()).start();
                                return false;
                            }
                        });
                    break;
            }
            return true;
        }

        @Override
        public boolean dependsOnLocation() {
            return false;
        }

    }
}
