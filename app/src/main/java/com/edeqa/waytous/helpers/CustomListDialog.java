package com.edeqa.waytous.helpers;

import android.support.v7.widget.RecyclerView;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;

/**
 * Created 10/21/2017.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class CustomListDialog extends CustomDialog {

    private RecyclerView list;
    private RecyclerView.Adapter adapter;

    public CustomListDialog(MainActivity context) {
        super(context);

        list = content.findViewById(R.id.list_items);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    public RecyclerView.Adapter getAdapter() {
        return adapter;
    }

    public RecyclerView getList() {
        return list;
    }


}
