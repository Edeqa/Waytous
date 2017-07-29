package com.edeqa.waytous.holders;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.edeqa.waytous.BuildConfig;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.IntroRule;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SettingItem;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.Runnable1;

import java.util.ArrayList;

import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.CHANGE_NAME;
import static com.edeqa.waytous.helpers.Events.CREATE_OPTIONS_MENU;
import static com.edeqa.waytous.holders.SensorsViewHolder.REQUEST_MODE_DAY;
import static com.edeqa.waytous.holders.SensorsViewHolder.REQUEST_MODE_NIGHT;
import static com.edeqa.waytous.holders.SettingsViewHolder.PREFERENCES_GENERAL;
import static com.edeqa.waytous.holders.SettingsViewHolder.PREPARE_SETTINGS;
import static com.edeqa.waytousserver.helpers.Constants.SENSITIVE;


/**
 * Created 11/18/16.
 */
@SuppressWarnings("unused")
public class MenuViewHolder extends AbstractViewHolder {
    private static final String TYPE = "menu";

    public static final String PREFERENCES_ABOUT = "about";


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
                        String name = State.getInstance().getStringPreference(PropertiesHolder.PREFERENCE_MY_NAME,"");

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
                                        Utils.log(MenuViewHolder.this, "onEvent:", "Cancel");
                                    }
                                });

                        builder.create().show();
                        return false;
                    }
                });
                break;
            case PREPARE_SETTINGS:
                SettingItem.Page item = (SettingItem.Page) object;
                item.add(new SettingItem.Group(PREFERENCES_GENERAL).setTitle(R.string.general).setPriority(100));
                item.add(new SettingItem.Text(PropertiesHolder.PREFERENCE_MY_NAME).setTitle(R.string.menu_set_my_name).setGroupId(PREFERENCES_GENERAL).setCallback(new Runnable1<String>() {
                    @Override
                    public void call(String arg) {
                        State.getInstance().getMe().fire(CHANGE_NAME,arg);
                    }
                }));

                SettingItem.Page about = new SettingItem.Page(PREFERENCES_ABOUT).setTitle(R.string.about).setPriority(0)
                        .add(new SettingItem.Group(PREFERENCES_GENERAL).setTitle(R.string.general).setPriority(100))
                        .add(new SettingItem.Label("waytous")
                                .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=com.edeqa.waytous")))
                                .setTitle(R.string.app_name).setMessage("Version " + BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE))
                        .add(new SettingItem.Label("waytous_web")
                                .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.waytous.net")))
                                .setTitle("Waytous web service").setMessage("http://www.waytous.net"))
                        .add(new SettingItem.Label("copyright")
                                .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.edeqa.com")))
                                .setTitle("Copyright (c) 2017 Edeqa").setMessage("http://www.edeqa.com"))
                        .add(new SettingItem.Page("legal_information").setTitle("Legal information")
                                .add(new SettingItem.Label("legal_text").setTitle(R.string.legal_information_body)))
                        .add(new SettingItem.Page("third_party").setTitle("Third party components").setPriority(0)
                                .add(new SettingItem.Label("sll")
                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/mrmans0n/smart-location-lib")))
                                        .setTitle("Smart Location Library"))
                                .add(new SettingItem.Label("jws")
                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/TooTallNate/Java-WebSocket")))
                                        .setTitle("Java WebSockets"))
                                .add(new SettingItem.Label("msv")
                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/pengrad/MapScaleView")))
                                        .setTitle("Map Scale View"))
                                .add(new SettingItem.Label("ttv")
                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/KeepSafe/TapTargetView")))
                                        .setTitle("TapTargetView"))
                                .add(new SettingItem.Label("amis")
                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/TangoAgency/material-intro-screen")))
                                        .setTitle("Android Material Intro Screen")));

                item.add(about);

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
