package ru.wtg.whereaminow.holders;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import ru.wtg.whereaminow.R;
import ru.wtg.whereaminow.State;
import ru.wtg.whereaminow.helpers.IntroRule;
import ru.wtg.whereaminow.helpers.MyUser;

import static ru.wtg.whereaminow.State.ACTIVITY_RESUME;
import static ru.wtg.whereaminow.State.CHANGE_NAME;
import static ru.wtg.whereaminow.State.CREATE_OPTIONS_MENU;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_DAY;
import static ru.wtg.whereaminow.holders.SensorsViewHolder.REQUEST_MODE_NIGHT;
import static ru.wtg.whereaminowserver.helpers.Constants.DEBUGGING;

/**
 * Created 11/18/16.
 */
public class MenuViewHolder extends AbstractViewHolder {
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
    public AbstractView create(MyUser myUser) {
        return null;
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
                optionsMenu.add(R.string.menu_set_my_name).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.menu_set_my_name);

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

    @Override
    public boolean dependsOnUser() {
        return false;
    }

    @Override
    public ArrayList<IntroRule> getIntro() {
        ArrayList<IntroRule> rules = new ArrayList<>();
//        rules.add(new IntroRule().setEvent(PREPARE_OPTIONS_MENU).setId("menu_show_context").setLinkTo(IntroRule.LINK_TO_OPTIONS_MENU).setViewId(R.string.menu_set_my_name).setTitle("Here you can").setDescription("Change your name in group."));
        rules.add(new IntroRule().setEvent(ACTIVITY_RESUME).setId("menu_intro_option").setLinkTo(IntroRule.LINK_TO_OPTIONS_MENU).setTitle("Main menu").setDescription("Here you can access general actions to this group. And something else..."));

        return rules;
    }

}
