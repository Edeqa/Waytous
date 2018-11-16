package com.edeqa.waytous.holders.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.edeqa.helpers.interfaces.Consumer;
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
import com.edeqa.waytous.holders.property.PropertiesHolder;

import java.util.ArrayList;

import static com.edeqa.waytous.Constants.OPTIONS;
import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.CHANGE_NAME;
import static com.edeqa.waytous.helpers.Events.CREATE_OPTIONS_MENU;
import static com.edeqa.waytous.holders.view.SettingsViewHolder.CREATE_SETTINGS;


/**
 * Created 11/18/16.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class MenuViewHolder extends AbstractViewHolder {

    public static final String PREFERENCES_ABOUT = "about"; //NON-NLS

    private boolean day = true;

    public MenuViewHolder(MainActivity context) {
        super(context);
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case CREATE_OPTIONS_MENU:
                Menu optionsMenu = (Menu) object;
                if(OPTIONS.isDebugMode()) {
                    optionsMenu.add("Switch day/night mode").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            State.getInstance().fire((day = !day) ? SensorsViewHolder.REQUEST_MODE_DAY : SensorsViewHolder.REQUEST_MODE_NIGHT);
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
                        final EditText etMyName = layoutDialogSetMyName.findViewById(R.id.et_my_name);
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
                                        State.getInstance().getMe().fire(CHANGE_NAME, etMyName.getText().toString());
                                    }
                                });

                        builder.setNegativeButton(context.getString(android.R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Utils.log(MenuViewHolder.this, "onEvent:", "Cancel"); //NON-NLS
                                    }
                                });

                        builder.create().show();
                        return false;
                    }
                });
                break;
            case CREATE_SETTINGS:
                SettingItem.Page item = (SettingItem.Page) object;
                item.add(new SettingItem.Group(SettingsViewHolder.PREFERENCES_GENERAL).setTitle(R.string.general).setPriority(100));
                item.add(new SettingItem.Text(PropertiesHolder.PREFERENCE_MY_NAME).setTitle(R.string.menu_set_my_name).setGroupId(SettingsViewHolder.PREFERENCES_GENERAL).setCallback(new Consumer<String>() {
                    @Override
                    public void accept(String arg) {
                        State.getInstance().getMe().fire(CHANGE_NAME,arg);
                    }
                }));

                SettingItem.Page about = new SettingItem.Page(PREFERENCES_ABOUT).setTitle(R.string.about).setPriority(0)
                        .add(new SettingItem.Group(SettingsViewHolder.PREFERENCES_GENERAL).setTitle(R.string.general).setPriority(100)
                                .add(new SettingItem.Label("waytous")
                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://play.google.com/store/apps/details?id=com.edeqa.waytous")))
                                        .setTitle(R.string.app_name).setMessage(context.getString(R.string.version_s_d, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)))
                                .add(new SettingItem.Label("waytous_web")
                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.waytous.net")))
                                        .setTitle(context.getString(R.string.waytous_web_service)).setMessage("http://www.waytous.net"))
                                .add(new SettingItem.Label("copyright")
                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.edeqa.com")))
                                        .setTitle("Copyright © 2017-18 Edeqa").setMessage("http://www.edeqa.com"))
                                .add(new SettingItem.Label("report")
                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://waytous.myjetbrains.com/youtrack/newIssue?project=WTU")))
                                        .setTitle(context.getString(R.string.report_an_issue)).setMessage(context.getString(R.string.click_here_to_report_about_error_odd_behaviour_or_some_great_idea))))
                        .add(new SettingItem.Page("legal_information").setTitle(context.getString(R.string.legal_information))
                                .add(new SettingItem.Label("legal_text").setTitle(R.string.legal_information_body)))
                        .add(new SettingItem.Page("third_party").setTitle(context.getString(R.string.third_party_components)).setPriority(0)
//                                .add(new SettingItem.Label("amis")
//                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/TangoAgency/material-intro-screen")))
//                                        .setTitle("Android Material Intro Screen"))
                                .add(new SettingItem.Label("msv")
                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/pengrad/MapScaleView")))
                                        .setTitle("Map Scale View"))
                                .add(new SettingItem.Label("jws")
                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/TakahikoKawasaki/nv-websocket-client")))
                                        .setTitle("nv-websocket-client"))
                                .add(new SettingItem.Label("sll")
                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/mrmans0n/smart-location-lib")))
                                        .setTitle("Smart Location Library")));
//                                .add(new SettingItem.Label("scv")
//                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/amlcurran/ShowcaseView")))
//                                        .setTitle("ShowcaseView"))
//                                .add(new SettingItem.Label("ttv")
//                                        .setIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://github.com/KeepSafe/TapTargetView")))
//                                        .setTitle("TapTargetView")));

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
        //noinspection HardCodedStringLiteral
        rules.add(new IntroRule().setEvent(ACTIVITY_RESUME).setId("menu_intro_option").setLinkTo(IntroRule.LINK_TO_OPTIONS_MENU).setTitle("Main menu").setDescription("Here you can access general actions to this group. And something else..."));

        return rules;
    }

}
