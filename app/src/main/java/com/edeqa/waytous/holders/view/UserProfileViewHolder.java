package com.edeqa.waytous.holders.view;

import android.content.DialogInterface;
import android.view.MenuItem;
import android.view.View;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.Account;
import com.edeqa.waytous.helpers.CustomDialog;
import com.edeqa.waytous.helpers.CustomListDialog;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SettingItem;

import static com.edeqa.waytous.helpers.Events.CREATE_DRAWER;
import static com.edeqa.waytous.helpers.Events.MAP_READY;
import static com.edeqa.waytous.helpers.Events.SYNC_PROFILE;

/**
 * Created 10/21/17.
 */

@SuppressWarnings("WeakerAccess")
public class UserProfileViewHolder extends AbstractViewHolder {

    public static final String SHOW_USER_PROFILE = "show_user_profile"; //NON-NLS

    private CustomDialog dialog;

    public UserProfileViewHolder(MainActivity context) {
        super(context);
        SettingItem.setSharedPreferences(State.getInstance().getSharedPreferences());
        SettingItem.setContext(context);

    }

    @Override
    public boolean dependsOnUser() {
        return false;
    }

    @Override
    public boolean dependsOnEvent() {
        return true;
    }

    @Override
    public boolean onEvent(String event, Object object) {
        switch(event){
            case CREATE_DRAWER:
                DrawerViewHolder.ItemsHolder adder = (DrawerViewHolder.ItemsHolder) object;
                adder.add(R.id.drawer_section_miscellaneous, R.string.user_profile, R.string.user_profile, R.drawable.ic_person_black_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().fire(SHOW_USER_PROFILE);
                        return false;
                    }
                });
                break;
            case SHOW_USER_PROFILE:
                showAccountDialog();
                break;
            case MAP_READY:
                doGlobalSync();
                break;

        }
        return true;
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    public void showAccountDialog() {

        dialog = new CustomDialog(context);
        dialog.setLayout(R.layout.dialog_user_profile);
        dialog.getLayout().findViewById(R.id.layoutSignButtons).setVisibility(View.VISIBLE);

        dialog.setMenu(R.menu.dialog_saved_locations_menu);
//        dialog.setFlat(true);

        dialog.setTitle("User profile");
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.close), null);
        dialog.setOnCancelListener(null);
        dialog.show();

    }

    public Account fetchAccount() {
        Account account = null;

//        var data = firebase.auth().currentUser;
//        if(data) {
//            user = {};
//            data.providerData.forEach(function(item){
//                user = u.cloneAsObject(item);
//                return false;
//            });
//            user.uid = data.uid;
//        }
        return account;
    }

    public void doGlobalSync() {
        if(fetchAccount() != null) {
            State.getInstance().fire(SYNC_PROFILE);
        }
    }
}
