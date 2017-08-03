package com.edeqa.waytous.holders.view;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
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

import static com.edeqa.waytous.helpers.Events.TRACKING_JOIN;
import static com.edeqa.waytous.helpers.Events.TRACKING_NEW;
import static com.edeqa.waytous.holders.property.TrackingHolder.PREFERENCE_TERMS_OF_SERVICE_CONFIRMED;
import static com.edeqa.waytous.holders.view.MenuViewHolder.PREFERENCES_ABOUT;
import static com.edeqa.waytous.holders.view.SettingsViewHolder.CREATE_SETTINGS;
import static com.edeqa.waytousserver.helpers.Constants.USER_DISMISSED;
import static com.edeqa.waytousserver.helpers.Constants.USER_JOINED;

/**
 * Created 8/3/17.
 */

@SuppressWarnings("WeakerAccess")
public class TrackingViewHolder extends AbstractViewHolder<TrackingViewHolder.TrackingView> {

    public static final String TRACKING_TERMS_OF_SERVICE = "tracking_terms_of_service"; //NON-NLS

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
            case USER_JOINED:
            case USER_DISMISSED:
                break;
            case TRACKING_TERMS_OF_SERVICE:
                String link = (String) object;
                showTermsOfService(link);
                break;
            case CREATE_SETTINGS:
                SettingItem.Page item = (SettingItem.Page) object;
                SettingItem.Page about = new SettingItem.Page(PREFERENCES_ABOUT).setTitle(R.string.about).setPriority(0)
                        .add(new SettingItem.Label("offer").setTitle(R.string.you_must_read_and_confirm_your_agreement_with_terms_of_service))
                        .add(new SettingItem.Label("tos").setMessage(R.string.terms_of_service))
                        .add(new SettingItem.Checkbox(PREFERENCE_TERMS_OF_SERVICE_CONFIRMED).setTitle(R.string.i_have_read_and_agree_with_terms_of_service));
                item.add(about);
                break;
        }
        return true;
    }

    private void showTermsOfService(final String link) {

        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        final View content = context.getLayoutInflater().inflate(R.layout.dialog_terms_of_service, null);

        TextView tvMessageCreate = (TextView) content.findViewById(R.id.tv_message_create);
        TextView tvMessageJoin = (TextView) content.findViewById(R.id.tv_message_join);
        final CheckBox cbConfirm = (CheckBox) content.findViewById(R.id.cb_terms_of_service_confirm);
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
            switch(event) {
            }
            return true;
        }

    }
}
