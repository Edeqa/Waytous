package com.edeqa.waytous.holders;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.SettingsActivity;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.SettingItem;
import com.edeqa.waytous.helpers.Utils;
import com.edeqa.waytous.interfaces.Runnable1;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static com.edeqa.waytous.helpers.Events.CREATE_DRAWER;
import static com.edeqa.waytous.helpers.Events.PREPARE_DRAWER;


/**
 * Created 11/24/16.
 */

public class SettingsViewHolder extends AbstractViewHolder {
    private static final String TYPE = "settingsMap";

    public static final String SHOW_SETTINGS = "show_settings";
    public static final String PREPARE_SETTINGS = "prepare_settings";

    private AlertDialog dialog;
    private RecyclerView list;
    private Toolbar toolbar;
    private SettingsAdapter adapter;
    private static Map<String, ArrayList<SettingItem>> settingsMap = new TreeMap<>();
    private ArrayList<SettingItem> settings = new ArrayList<>();

    public SettingsViewHolder(MainActivity context) {
        super(context);
        SettingItem.setSharedPreferences(State.getInstance().getSharedPreferences());
        SettingItem.setContext(context);

        setting.call(new SettingItem.Group("general").setTitle("General"));
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
                menuItem.getSubMenu().add(Menu.NONE, R.string.setting_up, Menu.NONE, context.getString(R.string.setting_up)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        context.startActivity(new Intent(context, SettingsActivity.class));
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
                State.getInstance().fire(PREPARE_SETTINGS, setting);
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

        adapter = new SettingsAdapter(list);
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

     Runnable1<SettingItem> setting = new Runnable1<SettingItem>() {
        @Override
        public void call(SettingItem item) {
            Utils.log(SettingsViewHolder.this,item);
            switch (item.getType()) {
                case SettingItem.GROUP:
                    if(!settingsMap.containsKey(item.getId())) {
                        ArrayList<SettingItem> list = new ArrayList<>();
                        list.add(item);
                        settingsMap.put(item.getId(), list);
                    }
                    break;
                case SettingItem.LABEL:
                case SettingItem.TEXT:
                case SettingItem.CHECKBOX:
                case SettingItem.LIST:
                    ArrayList<SettingItem> list = settingsMap.get("general");
                    String id = item.getGroupId();
                    if(settingsMap.containsKey(id)) {
                        list = settingsMap.get(item.getGroupId());
                    }
                    addUnique(list,item);
                    break;
            }
            settings.clear();
            for(Map.Entry<String,ArrayList<SettingItem>> g: settingsMap.entrySet()) {
                if(g.getValue().size() > 1) {
                    for (SettingItem x : g.getValue()) {
                        settings.add(x);
                    }
                }
            }

            if(adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
        private void addUnique(ArrayList<SettingItem> list, SettingItem item) {
            boolean exists = false;
            for(SettingItem x:list) {
                if(x.fetchId().equals(item.fetchId())) {
                    exists = true;
                    break;
                }
            }
            if(!exists) list.add(item);
        }
    };


    class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder> {

        private final RecyclerView list;
        private final LinearLayoutManager layoutManager;

        public SettingsAdapter(RecyclerView list) {
            this.list = list;
            list.setAdapter(this);
            layoutManager = new LinearLayoutManager(context);

            list.setLayoutManager(layoutManager);
            DividerItemDecoration divider = new DividerItemDecoration(list.getContext(), ((LinearLayoutManager) list.getLayoutManager()).getOrientation());
            list.addItemDecoration(divider);

            list.setItemAnimator(new DefaultItemAnimator());

        }

        @Override
        public SettingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_preference, parent, false);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("CLICKED:"+view.getId());
                }
            });
            return new SettingViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final SettingViewHolder holder, int position) {
            final SettingItem item = settings.get(position);

            holder.layoutHeader.setVisibility(View.GONE);
            holder.layoutWidget.setVisibility(View.GONE);
            holder.layoutPreference.setVisibility(View.VISIBLE);

            if(item.fetchSummary() != null) {
                holder.tvHeaderSummary.setText(item.fetchSummary());
                holder.tvHeaderSummary.setVisibility(View.VISIBLE);
                holder.tvSummary.setText(item.fetchSummary());
                holder.tvSummary.setVisibility(View.VISIBLE);
            } else {
                holder.tvHeaderSummary.setVisibility(View.GONE);
                holder.tvSummary.setVisibility(View.GONE);
            }

            switch(item.getType()) {
                case SettingItem.GROUP:
                    holder.tvHeaderTitle.setText(item.getTitle());
                    holder.layoutPreference.setVisibility(View.GONE);
                    holder.layoutHeader.setVisibility(View.VISIBLE);
                    break;
                case SettingItem.LABEL:
                    holder.tvTitle.setText(item.getTitle());
                    break;
                case SettingItem.TEXT:
                    holder.tvTitle.setText(item.getTitle());
//                    holder.tvSummary.setText(((SettingItem.Text)item).fetchSummary());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((SettingItem.Text)item).onClick(new Runnable1<String>() {
                                @Override
                                public void call(String arg) {
                                    System.out.println("CLICKED:"+arg+":"+item);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    break;
                case SettingItem.CHECKBOX:
                    holder.layoutWidget.setVisibility(View.VISIBLE);
                    holder.tvTitle.setText(item.getTitle());
                    holder.cbCheckbox.setChecked(((SettingItem.Checkbox)item).isChecked());
                    holder.cbCheckbox.setVisibility(View.VISIBLE);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((SettingItem.Checkbox)item).onClick(new Runnable1<Boolean>() {
                                @Override
                                public void call(Boolean arg) {
                                    System.out.println("CLICKED:"+item);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    break;
                case SettingItem.LIST:
                    holder.tvTitle.setText(item.getTitle());
                    break;
                case SettingItem.PAGE:
                    holder.layoutPreference.setVisibility(View.GONE);
                    holder.layoutHeader.setVisibility(View.VISIBLE);
                    holder.ivRightArrow.setVisibility(View.VISIBLE);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return settings.size();
        }

        class SettingViewHolder extends RecyclerView.ViewHolder {

            private final LinearLayout layoutHeader;
            private final LinearLayout layoutPreference;
            private final LinearLayout layoutWidget;
            private final TextView tvTitle;
            private final TextView tvHeaderTitle;
            private final TextView tvSummary;
            private final TextView tvHeaderSummary;
            private final ImageView ivRightArrow;
            private final CheckBox cbCheckbox;

            private SettingViewHolder(View view) {
                super(view);
                layoutHeader = (LinearLayout) view.findViewById(R.id.layout_header);
                layoutPreference = (LinearLayout) view.findViewById(R.id.layout_preference);
                layoutWidget = (LinearLayout) view.findViewById(R.id.layout_widget);
                tvTitle = (TextView) view.findViewById(R.id.tv_title);
                tvHeaderTitle = (TextView) view.findViewById(R.id.tv_header_title);
                tvSummary = (TextView) view.findViewById(R.id.tv_summary);
                tvHeaderSummary = (TextView) view.findViewById(R.id.tv_header_summary);
                ivRightArrow = (ImageView) view.findViewById(R.id.iv_right_arrow);
                cbCheckbox = (CheckBox) view.findViewById(R.id.cb_checkbox);
            }
        }
    }

}
