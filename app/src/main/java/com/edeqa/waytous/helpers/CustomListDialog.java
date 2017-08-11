package com.edeqa.waytous.helpers;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.interfaces.Callable1;

/**
 * Created 8/10/2017.
 */

@SuppressWarnings("WeakerAccess")
public class CustomListDialog {
    private final MainActivity context;
    private View content;
    private RecyclerView list;
    private int layout;
    private UserMessage.UserMessagesAdapter adapter;
    private int menu;
    private String title;
    private boolean flat;
    private AlertDialog dialog;
    private Toolbar toolbar;
    private Callable1<Boolean,String> searchListener;
    private Toolbar.OnMenuItemClickListener onMenuItemClickListener;
    private View footer;
    private View.OnTouchListener onTouchListener;
    private ColorDrawable drawable;
    private int alpha;

    public CustomListDialog(MainActivity context) {
        this.context = context;

        content = context.getLayoutInflater().inflate(R.layout.dialog_items, null);
        list = (RecyclerView) content.findViewById(R.id.list_items);
    }


    public void show() {

        dialog = new AlertDialog.Builder(context).create();
        AppBarLayout layoutToolbar = (AppBarLayout) context.getLayoutInflater().inflate(R.layout.view_action_bar, null);
        dialog.setCustomTitle(layoutToolbar);
        toolbar = (Toolbar) layoutToolbar.findViewById(R.id.toolbar);

        getAdapter().setEmptyView(content.findViewById(R.id.tv_placeholder));

        if (getFooter() != null) {
            ViewGroup placeFooter = (ViewGroup) content.findViewById(R.id.layout_footer);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            getFooter().setLayoutParams(params);

            placeFooter.addView(getFooter());
            placeFooter.setVisibility(View.VISIBLE);
        }

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
        if(toolbar.getNavigationIcon() != null) toolbar.getNavigationIcon().setColorFilter(Color.WHITE, mMode);
        toolbar.setNavigationOnClickListener(getNavigationOnClickListener());

        if (getMenuRes() > 0) {
            toolbar.inflateMenu(getMenuRes());
            toolbar.setOnMenuItemClickListener(getOnMenuItemClickListener());
        }

        if (getSearchListener() != null) {
            final MenuItem searchItem = toolbar.getMenu().findItem(R.id.search_message);
            searchItem.getIcon().setColorFilter(Color.WHITE, mMode);

            final SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (!searchView.isIconified()) {
                        searchView.setIconified(true);
                    }
                    searchItem.collapseActionView();
                    return getSearchListener().call(query);
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return getSearchListener().call(s);
                }
            });
        }

        dialog.setView(content);

        if (isFlat()) {
            drawable = new ColorDrawable(Color.WHITE);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(drawable);
            }
        }
        dialog.show();

        if (isFlat()) {
            Utils.resizeDialog(context, dialog, Utils.MATCH_SCREEN, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        if(getOnTouchListener() != null) {
            dialog.getWindow().getDecorView().setOnTouchListener(getOnTouchListener());
        }

    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public int getLayout() {
        return layout;
    }

    public void setAdapter(UserMessage.UserMessagesAdapter adapter) {
        this.adapter = adapter;
    }

    public UserMessage.UserMessagesAdapter getAdapter() {
        return adapter;
    }

    public void setMenu(int menu) {
        this.menu = menu;
    }

    public int getMenuRes() {
        return menu;
    }

    public void setTitle(String title) {
        this.title = title;
        if(toolbar != null) toolbar.setTitle(title);
    }

    public String getTitle() {
        return title;
    }

    public void setFlat(boolean flat) {
        this.flat = flat;
    }

    public boolean isFlat() {
        return flat;
    }

    public View.OnClickListener getNavigationOnClickListener() {
        return navigationOnClickListener;
    }

    @SuppressWarnings("unused")
    public void setNavigationOnClickListener(View.OnClickListener navigationOnClickListener) {
        this.navigationOnClickListener = navigationOnClickListener;
    }

    private View.OnClickListener navigationOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog.dismiss();
            dialog = null;
        }
    };


    public Callable1<Boolean, String> getSearchListener() {
        return searchListener;
    }

    public void setSearchListener(Callable1<Boolean, String> searchListener) {
        this.searchListener = searchListener;
    }

    public Toolbar.OnMenuItemClickListener getOnMenuItemClickListener() {
        return onMenuItemClickListener;
    }

    public void setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    public View getFooter() {
        return footer;
    }

    public void setFooter(View footer) {
        this.footer = footer;
    }

    public RecyclerView getList() {
        return list;
    }

    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        this.onTouchListener = onTouchListener;
    }

    public View.OnTouchListener getOnTouchListener() {
        return onTouchListener;
    }

    public void setAlpha(int alpha) {
        if(drawable == null) {
            drawable = new ColorDrawable(Color.WHITE);
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(drawable);
            }
        }
        this.alpha = alpha;
        drawable.setAlpha(alpha);
    }

    public int getAlpha() {
        return alpha;
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

    public Menu getMenu() {
        return toolbar.getMenu();
    }

    public void openMenu() {
        if(toolbar != null && menu > 0) {
            toolbar.post(new Runnable() {
                public void run() {
                    toolbar.showOverflowMenu();
                }
            });
        }
    }
}
