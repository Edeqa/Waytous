package com.edeqa.waytous.holders;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.Utils;

import static com.edeqa.waytous.helpers.Events.CREATE_DRAWER;
import static com.edeqa.waytous.helpers.Events.PREPARE_DRAWER;


/**
 * Created 11/24/16.
 */

public class SettingsHolder extends AbstractViewHolder {
    private static final String TYPE = "settings";

    public static final String SHOW_SETTINGS = "settings_show";
    private AlertDialog dialog;
    private RecyclerView list;
    private Toolbar toolbar;

    public SettingsHolder(MainActivity context) {
        super(context);
    }

    @Override
    public String getType() {
        return TYPE;
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
                NavigationView drawerView = (NavigationView) object;
                MenuItem menuItem = drawerView.getMenu().findItem(R.id.drawer_section_last);
                menuItem.setVisible(true);

                MenuItem item = menuItem.getSubMenu().findItem(R.string.settings);
                if(item == null) {
                    item = menuItem.getSubMenu().add(Menu.NONE, R.string.settings, Menu.NONE, context.getString(R.string.settings));
                }
                item.setIcon(R.drawable.ic_settings_black_24dp)
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                State.getInstance().fire(SHOW_SETTINGS);
                                return false;
                            }
                        });
                break;
            case PREPARE_DRAWER:
                drawerView = (NavigationView) object;
                menuItem = drawerView.getMenu().findItem(R.id.drawer_section_last);
                menuItem.setVisible(true);
                break;
            case SHOW_SETTINGS:
                showSettings();
                break;

        }
        return true;
    }

    @Override
    public AbstractView create(MyUser myUser) {
        return null;
    }

    public void showSettings() {

        dialog = new AlertDialog.Builder(context).create();

        final View content = context.getLayoutInflater().inflate(R.layout.dialog_items, null);

//        final LinearLayout layoutFooter = setupFooter(content);

        context.getLayoutInflater().inflate(R.layout.dialog_items, null);

        list = (RecyclerView) content.findViewById(R.id.list_items);

//        adapter = new UserMessage.UserMessagesAdapter(context, list);
//        adapter.setEmptyView(content.findViewById(R.id.tv_placeholder));

        dialog.setCustomTitle(setupToolbar());

        dialog.setView(content);

        if(dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }
        dialog.show();

        Utils.resizeDialog(context, dialog, Utils.MATCH_SCREEN, LinearLayout.LayoutParams.WRAP_CONTENT);

    }

    private AppBarLayout setupToolbar() {

        AppBarLayout layoutToolbar = (AppBarLayout) context.getLayoutInflater().inflate(R.layout.view_action_bar, null);
        toolbar = (Toolbar) layoutToolbar.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE,mMode);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                dialog = null;
            }
        });
        toolbar.setTitle(R.string.settings);

        return layoutToolbar;
    }

    class SettingsAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_preference, parent, false);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    onItemClickListener.call(view);
                }
            });
            return new SettingViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        class SettingViewHolder extends RecyclerView.ViewHolder {

            private final TextView tvTitle;
            private final TextView tvSummary;
            private final ImageView ivRightArrow;

            private SettingViewHolder(View view) {
                super(view);
                tvTitle = (TextView) view.findViewById(R.id.tv_title);
                tvSummary = (TextView) view.findViewById(R.id.tv_summary);
                ivRightArrow = (ImageView) view.findViewById(R.id.iv_right_arrow);
            }
        }
    }

}
