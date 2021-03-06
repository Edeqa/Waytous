package com.edeqa.waytous.holders.view;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.edeqa.helpers.interfaces.Consumer;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.State;
import com.edeqa.waytous.abstracts.AbstractView;
import com.edeqa.waytous.abstracts.AbstractViewHolder;
import com.edeqa.waytous.helpers.MyUser;
import com.edeqa.waytous.helpers.PreferenceDividerDecoration;
import com.edeqa.waytous.helpers.SettingItem;
import com.edeqa.waytous.helpers.Utils;

import java.util.ArrayList;

import static com.edeqa.waytous.helpers.Events.ACTIVITY_RESUME;
import static com.edeqa.waytous.helpers.Events.CREATE_DRAWER;

/**
 * Created 7/28/17.
 */

@SuppressWarnings("WeakerAccess")
public class SettingsViewHolder extends AbstractViewHolder {

    public static final String SHOW_SETTINGS = "show_settings"; //NON-NLS
    public static final String CREATE_SETTINGS = "create_settings"; //NON-NLS
    public static final String PREPARE_SETTINGS = "prepare_settings"; //NON-NLS
    public static final String PREFERENCES_GENERAL = "general"; //NON-NLS

    private AlertDialog dialog;
    private Toolbar toolbar;
    private SettingsAdapter adapter;
    private SettingItem.Page settingItem;
    private SettingItem.Page currentSettingItem;
    private ArrayList<SettingItem.Page> queue = new ArrayList<>();
    private boolean settingsPrepared = false;

    public SettingsViewHolder(MainActivity context) {
        super(context);
        SettingItem.setSharedPreferences(State.getInstance().getSharedPreferences());
        SettingItem.setContext(context);

        settingItem = new SettingItem.Page(PREFERENCES_GENERAL);
        //noinspection unchecked
        settingItem.setCallback(new Consumer() {
            @Override
            public void accept(Object arg) {
                if(adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }).setTitle(R.string.settings);
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
                adder.add(R.id.drawer_section_miscellaneous, R.string.settings, R.string.settings, R.drawable.ic_settings_black_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        State.getInstance().fire(SHOW_SETTINGS);
                        return false;
                    }
                });
                break;
            case ACTIVITY_RESUME:
                currentSettingItem = settingItem;
                if(!settingsPrepared) {
                    State.getInstance().fire(CREATE_SETTINGS, settingItem);
                    settingsPrepared = true;
                }
                break;
            case SHOW_SETTINGS:
                currentSettingItem = settingItem;
                State.getInstance().fire(PREPARE_SETTINGS, settingItem);
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

        final View content = context.getLayoutInflater().inflate(R.layout.dialog_custom, null);

        context.getLayoutInflater().inflate(R.layout.dialog_custom, null);

        RecyclerView list = content.findViewById(R.id.list_items);

        adapter = new SettingsAdapter(list);

        dialog.setCustomTitle(setupToolbar());
        dialog.setView(content);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
                    if(queue.size() > 0) {
                        currentSettingItem = queue.remove(queue.size()-1);
                        toolbar.setTitle(currentSettingItem.getTitle());
                        adapter.notifyDataSetChanged();
                    } else {
                        dialog.cancel();
//                        dialog = null;
                    }
                    return true;
                }
                return false;
            }
        });
        if(dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }
        dialog.show();
        Utils.resizeDialog(context, dialog, Utils.MATCH_SCREEN, Utils.MATCH_SCREEN);
    }

    private AppBarLayout setupToolbar() {
        AppBarLayout layoutToolbar = (AppBarLayout) context.getLayoutInflater().inflate(R.layout.view_action_bar, null);
        toolbar = layoutToolbar.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        if(toolbar.getNavigationIcon() != null) toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(queue.size() > 0) {
                    currentSettingItem = queue.remove(queue.size()-1);
                    toolbar.setTitle(currentSettingItem.getTitle());
                    adapter.notifyDataSetChanged();
                } else {
                    dialog.dismiss();
                    dialog = null;
                }
            }
        });
        toolbar.setTitle(currentSettingItem.getTitle());
        return layoutToolbar;
    }

    class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingViewHolder> {
        private final LinearLayoutManager layoutManager;

        public SettingsAdapter(RecyclerView list) {
            list.setAdapter(this);
            layoutManager = new LinearLayoutManager(context);

            list.setLayoutManager(layoutManager);
            PreferenceDividerDecoration divider = new PreferenceDividerDecoration(list.getContext());
            list.addItemDecoration(divider);
            list.setItemAnimator(new DefaultItemAnimator());
        }

        @Override
        public SettingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_preference, parent, false);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("CLICKED:"+view.getId()); //NON-NLS
                }
            });
            return new SettingViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final SettingViewHolder holder, int position) {
            final SettingItem item = currentSettingItem.getItems().get(position);

            holder.layoutHeader.setVisibility(View.GONE);
            holder.layoutPreference.setVisibility(View.VISIBLE);
            holder.layoutWidget.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            holder.itemView.setEnabled(true);

            if(item.getMessageHtml() != null) {
                //noinspection deprecation
                Spanned html = Html.fromHtml(item.getMessageHtml());
                holder.tvHeaderSummary.setText(html);
                holder.tvHeaderSummary.setMovementMethod(LinkMovementMethod.getInstance());
                holder.tvHeaderSummary.setVisibility(View.VISIBLE);
                holder.tvSummary.setText(html);
                holder.tvSummary.setMovementMethod(LinkMovementMethod.getInstance());
                holder.tvSummary.setVisibility(View.VISIBLE);
            } else if(item.fetchSummary() != null) {
                holder.tvHeaderSummary.setText(item.fetchSummary());
                holder.tvHeaderSummary.setVisibility(View.VISIBLE);
                holder.tvSummary.setText(item.fetchSummary());
                holder.tvSummary.setVisibility(View.VISIBLE);
            } else {
                holder.tvHeaderSummary.setVisibility(View.GONE);
                holder.tvSummary.setVisibility(View.GONE);
            }

            holder.itemView.setTag(item.getType());
            switch(item.getType()) {
                case SettingItem.GROUP:
                    holder.tvHeaderTitle.setText(item.getTitle());
                    holder.layoutPreference.setVisibility(View.GONE);
                    holder.layoutHeader.setVisibility(View.VISIBLE);
                    holder.itemView.setEnabled(false);
                    break;
                case SettingItem.LABEL:
                    holder.tvTitle.setText(item.getTitle());
                    if(item.getCallback() != null) {
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SettingItem.Label x = (SettingItem.Label) item;
                                x.getCallback().accept(x.fetchId());
                            }
                        });
                    } else {
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SettingItem.Label x = (SettingItem.Label) item;
                                if (x.getIntent() != null) {
                                    context.startActivity(x.getIntent());
                                }
                            }
                        });
                    }
                    break;
                case SettingItem.TEXT:
                    holder.tvTitle.setText(item.getTitle());
//                    holder.tvSummary.setText(((SettingItem.Text)item).fetchSummary());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((SettingItem.Text)item).onClick(new Consumer<String>() {
                                @Override
                                public void accept(String arg) {
                                    System.out.println("CLICKED:"+arg+":"+item); //NON-NLS
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    break;
                case SettingItem.CHECKBOX:
                    holder.ivRightArrow.setVisibility(View.GONE);
                    holder.cbCheckbox.setVisibility(View.VISIBLE);
                    holder.layoutWidget.setVisibility(View.VISIBLE);
                    holder.tvTitle.setText(item.getTitle());
                    holder.cbCheckbox.setChecked(((SettingItem.Checkbox)item).isChecked());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((SettingItem.Checkbox)item).onClick(new Consumer<Boolean>() {
                                @Override
                                public void accept(Boolean arg) {
                                    System.out.println("CLICKED:"+item); //NON-NLS
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    break;
                case SettingItem.LIST:
                    holder.tvTitle.setText(item.getTitle());
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((SettingItem.List)item).onClick(new Consumer<String>() {
                                @Override
                                public void accept(String arg) {
                                    System.out.println("CLICKED:"+item+":"+arg); //NON-NLS
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    break;
                case SettingItem.PAGE:
                    holder.tvHeaderTitle.setText(item.getTitle());
                    holder.cbCheckbox.setVisibility(View.GONE);
                    holder.ivRightArrow.setVisibility(View.VISIBLE);
                    holder.layoutPreference.setVisibility(View.GONE);
                    holder.layoutHeader.setVisibility(View.VISIBLE);
                    holder.layoutWidget.setVisibility(View.VISIBLE);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            queue.add(currentSettingItem);
                            currentSettingItem = (SettingItem.Page) item;
                            toolbar.setTitle(currentSettingItem.getTitle());
                            adapter.notifyDataSetChanged();
                        }
                    });
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return currentSettingItem.getItems().size();
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
                layoutHeader = view.findViewById(R.id.layout_header);
                layoutPreference = view.findViewById(R.id.layout_preference);
                layoutWidget = view.findViewById(R.id.layout_widget);
                tvTitle = view.findViewById(R.id.tv_title);
                tvHeaderTitle = view.findViewById(R.id.tv_header_title);
                tvSummary = view.findViewById(R.id.tv_summary);
                tvHeaderSummary = view.findViewById(R.id.tv_header_summary);
                ivRightArrow = view.findViewById(R.id.iv_right_arrow);
                cbCheckbox = view.findViewById(R.id.cb_checkbox);
            }
        }
    }

}
