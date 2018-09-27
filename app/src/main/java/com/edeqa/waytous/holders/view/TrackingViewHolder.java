package com.edeqa.waytous.holders.view;

import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SettingItem;

import java.util.ArrayList;
import java.util.List;

import static com.edeqa.waytous.Constants.USER_DISMISSED;
import static com.edeqa.waytous.Constants.USER_JOINED;
import static com.edeqa.waytous.helpers.Events.BACK_PRESSED;
import static com.edeqa.waytous.helpers.Events.SELECT_SINGLE_USER;
import static com.edeqa.waytous.helpers.Events.TRACKING_JOIN;
import static com.edeqa.waytous.helpers.Events.TRACKING_NEW;
import static com.edeqa.waytous.holders.property.TrackingHolder.PREFERENCE_TERMS_OF_SERVICE_CONFIRMED;
import static com.edeqa.waytous.holders.view.MenuViewHolder.PREFERENCES_ABOUT;
import static com.edeqa.waytous.holders.view.SettingsViewHolder.CREATE_SETTINGS;

/**
 * Created 8/3/17.
 */

@SuppressWarnings("WeakerAccess")
public class TrackingViewHolder extends AbstractViewHolder<TrackingViewHolder.TrackingView> {

    public static final String TRACKING_TERMS_OF_SERVICE = "tracking_terms_of_service"; //NON-NLS
    public final static String PREFERENCE_BACKGROUND_ALERT_SHOWN = "background_alert_shown";  //NON-NLS

    public TrackingViewHolder(MainActivity context) {
        super(context);
    }

    @Override
    public List<String> events() {
        ArrayList<String> list = new ArrayList<>();
        list.add(TRACKING_TERMS_OF_SERVICE);
        return list;
    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

    @Override
    public TrackingView create(MyUser myUser) {
        if (myUser == null) return null;
        return new TrackingView(myUser);
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case BACK_PRESSED:
                if(State.getInstance().getBooleanPreference(PREFERENCE_BACKGROUND_ALERT_SHOWN, false) || State.getInstance().tracking_disabled()) {
                    context.onBackPressedSuper();
                } else {
                    final AlertDialog dialog = new AlertDialog.Builder(context).create();
                    dialog.setTitle(context.getString(R.string.alert));
                    dialog.setMessage(context.getString(R.string.you_want_to_minimize_waytous));

                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            State.getInstance().setPreference(PREFERENCE_BACKGROUND_ALERT_SHOWN, true);
                            context.onBackPressedSuper();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
                    dialog.show();
                }
            case USER_JOINED:
                if(object != null && object instanceof MyUser) {
                    MyUser user = (MyUser) object;
                    user.createViews();
                }
                break;
            case USER_DISMISSED:
                if(object != null && object instanceof MyUser) {
                    MyUser user = (MyUser) object;
                    user.removeViews();
                    if(State.getInstance().getUsers().getCountSelectedTotal() == 0) {
                        State.getInstance().getMe().fire(SELECT_SINGLE_USER);
                    }
                }
                break;
            case TRACKING_TERMS_OF_SERVICE:
                String link = (String) object;
                showTermsOfService(link);
                break;
            case CREATE_SETTINGS:
                SettingItem.Page item = (SettingItem.Page) object;
                //noinspection HardCodedStringLiteral
                SettingItem.Page about = new SettingItem.Page(PREFERENCES_ABOUT).setTitle(R.string.about).setPriority(0)
                        .add(new SettingItem.Page("pp").setTitle(R.string.privacy_policy).setPriority(0)
                                .add(new SettingItem.Label("pp").setMessageHtml(R.string.privacy_policy_body)))
                        .add(new SettingItem.Page("tos").setTitle(R.string.terms_of_service).setPriority(0)
                                .add(new SettingItem.Label("offer").setTitle(R.string.you_must_read_and_confirm_your_agreement_with_terms_of_service))
                                .add(new SettingItem.Label("tos").setMessageHtml(R.string.terms_of_service_body))
                                .add(new SettingItem.Checkbox(PREFERENCE_TERMS_OF_SERVICE_CONFIRMED).setTitle(R.string.i_have_read_and_agree_with_terms_of_service)));
                item.add(about);
                break;
        }
        return true;
    }

    private void showTermsOfService(final String link) {

        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        final View content = context.getLayoutInflater().inflate(R.layout.dialog_terms_of_service, null);

        TextView tvMessageCreate = content.findViewById(R.id.tv_message_create);
        TextView tvMessageJoin = content.findViewById(R.id.tv_message_join);
        final CheckBox cbConfirm = content.findViewById(R.id.cb_terms_of_service_confirm);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        cbConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(((CheckBox)v).isChecked());
                int buttonTitle = R.string.close;
                if(((CheckBox)v).isChecked() && link == null) {
                    buttonTitle = R.string.create_group;
                } else if(((CheckBox)v).isChecked() && link != null) {
                    buttonTitle = R.string.join_group;
                }
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(buttonTitle);
            }
        });

        if (link == null) {
            dialog.setTitle(context.getString(R.string.create_group));
            tvMessageCreate.setVisibility(View.VISIBLE);
        } else {
            dialog.setTitle(context.getString(R.string.join_group));
            tvMessageJoin.setVisibility(View.VISIBLE);
        }

        dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(cbConfirm.isChecked() && (link == null || link.length() == 0)) {
                    State.getInstance().setPreference(PREFERENCE_TERMS_OF_SERVICE_CONFIRMED, true);
                    State.getInstance().fire(TRACKING_NEW);
                } else if(cbConfirm.isChecked() && link != null) {
                    State.getInstance().setPreference(PREFERENCE_TERMS_OF_SERVICE_CONFIRMED, true);
                    State.getInstance().fire(TRACKING_JOIN, link);
                }
            }
        });
        dialog.setView(content);

        dialog.show();

    }

    class TrackingView extends AbstractView {

        TrackingView(MyUser myUser){
            super(myUser);
        }

        @Override
        public boolean dependsOnLocation() {
            return false;
        }

        @Override
        public void remove() {
        }

        @Override
        public boolean onEvent(String event, Object object) {
            return true;
        }

    }
}
