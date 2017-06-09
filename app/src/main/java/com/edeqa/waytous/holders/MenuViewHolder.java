package com.edeqa.waytous.holders;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;

import java.util.ArrayList;

import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.CHANGE_NAME;
import static com.edeqa.waytous.helpers.Events.CREATE_OPTIONS_MENU;
import static com.edeqa.waytous.holders.SensorsViewHolder.REQUEST_MODE_DAY;
import static com.edeqa.waytous.holders.SensorsViewHolder.REQUEST_MODE_NIGHT;
import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;


/**
 * Created 11/18/16.
 */
@SuppressWarnings("unused")
public class MenuViewHolder extends AbstractViewHolder {
    private static final String TYPE = "menu";

    private boolean day = true;

    public MenuViewHolder(MainActivity context) {
        super(context);
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
                if(SENSITIVE.isDebugMode()) {
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
                            builder.setNeutralButton(context.getString(R.string.remove), new DialogInterface.OnClickListener() {
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
//        rules.put(new IntroRule().setEvent(PREPARE_OPTIONS_MENU).setId("menu_show_context").setLinkTo(IntroRule.LINK_TO_OPTIONS_MENU).setViewId(R.string.menu_set_my_name).setTitle("Here you can").setDescription("Change your name in group."));
        rules.add(new IntroRule().setEvent(ACTIVITY_RESUME).setId("menu_intro_option").setLinkTo(IntroRule.LINK_TO_OPTIONS_MENU).setTitle("Main menu").setDescription("Here you can access general actions to this group. And something else..."));

        return rules;
    }

}
