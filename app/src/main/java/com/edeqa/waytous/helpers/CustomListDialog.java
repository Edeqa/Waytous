package com.edeqa.waytous.helpers;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;

/**
 * Created 8/10/2017.
 */

public class CustomListDialog {
    private final MainActivity context;
    private final View content;
    private final RecyclerView list;
    private int layout;
    private UserMessage.UserMessagesAdapter adapter;
    private int menu;
    private String title;
    private boolean flat;
    private AlertDialog dialog;
    private Toolbar toolbar;
    private SearchView.OnQueryTextListener searchListener;
    private Toolbar.OnMenuItemClickListener onMenuItemClickListener;
    private View footer;
    private View.OnTouchListener ontouchListener;
    private ColorDrawable drawable;

    public CustomListDialog(MainActivity context) {
        this.context = context;
        content = context.getLayoutInflater().inflate(R.layout.dialog_items, null);
        list = (RecyclerView) content.findViewById(R.id.list_items);
    }


    public void show() {

        dialog = new AlertDialog.Builder(context).create();

        getAdapter().setEmptyView(content.findViewById(R.id.tv_placeholder));
//        final LinearLayout layoutFooter = setupFooter(content);

        if (getFooter() != null) {
            ViewGroup placeFooter = (ViewGroup) content.findViewById(R.id.layout_footer);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            getFooter().setLayoutParams(params);

            placeFooter.addView(getFooter());
            placeFooter.setVisibility(View.VISIBLE);
        }

        AppBarLayout layoutToolbar = (AppBarLayout) context.getLayoutInflater().inflate(R.layout.view_action_bar, null);
        toolbar = (Toolbar) layoutToolbar.findViewById(R.id.toolbar);
        dialog.setCustomTitle(layoutToolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, mMode);
        toolbar.setNavigationOnClickListener(navigationOnClickListener);

        if (getMenu() > 0) {
            toolbar.inflateMenu(getMenu());
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
                    return getSearchListener().onQueryTextSubmit(query);
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return getSearchListener().onQueryTextChange(s);
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

        if(getOntouchListener() != null) {
            dialog.getWindow().getDecorView().setOnTouchListener(getOntouchListener());
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

    public int getMenu() {
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


    public SearchView.OnQueryTextListener getSearchListener() {
        return searchListener;
    }

    public void setSearchListener(SearchView.OnQueryTextListener searchListener) {
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

    public void setOntouchListener(View.OnTouchListener ontouchListener) {
        this.ontouchListener = ontouchListener;
    }

    public View.OnTouchListener getOntouchListener() {
        return ontouchListener;
    }

    public ColorDrawable getDrawable() {
        return drawable;
    }


}
