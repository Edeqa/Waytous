package com.edeqa.waytous.helpers;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.interfaces.Runnable1;

import java.util.Map;

/**
 * Created 7/28/17.
 */

public abstract class SettingItem<T> {

    public static final int GROUP = 0;
    public static final int TEXT = 1;
    public static final int CHECKBOX = 2;
    public static final int LIST = 3;
    public static final int LABEL = 4;
    public static final int PAGE = 5;


    private static SharedPreferences sharedPreferences;
    private static MainActivity context;

    private String internalId;
    protected String id;
    private String groupId;
    private String title;
    private String message;
    private int type;
    private String[] depended;
    protected Runnable1<T> callback;

    public static void setSharedPreferences(SharedPreferences sharedPreferences) {
        SettingItem.sharedPreferences = sharedPreferences;
    }

    public static void setContext(MainActivity context) {
        SettingItem.context = context;
    }

    public SettingItem(String id) {
        this.id = id;
    }

    public String fetchId() {
        if(getInternalId() != null) return getInternalId();
        return getId();
    }

    public String getTitle() {
        return title;
    }

    public SettingItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public SettingItem setMessage(String message) {
        this.message = message;
        return this;
    }

    public int getType() {
        return type;
    }

    protected void setType(int type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public SettingItem setId(String id) {
        this.id = id;
        return this;
    }

    public String[] getDepended() {
        return depended;
    }

    public SettingItem setDepended(String[] depended) {
        this.depended = depended;
        return this;
    }

    public SettingItem setInternalId(String internalId) {
        this.internalId = internalId;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public SettingItem setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getInternalId() {
        return internalId;
    }

    @Override
    public String toString() {
        return "SettingItem{" +
                "internalId='" + internalId + '\'' +
                ", id='" + id + '\'' +
                ", groupId='" + groupId + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", type=" + type +
                '}';
    }

    public abstract void onClick(Runnable1<T> runnable);

    public String fetchSummary() {
        return getMessage();
    }

    public Runnable1<T> getCallback() {
        return callback;
    }

    public SettingItem setCallback(Runnable1<T> callback) {
        this.callback = callback;
        return this;
    }

    public static class Checkbox extends SettingItem<Boolean> {
        private boolean checked;

        public Checkbox(String id) {
            super(id);
            setType(CHECKBOX);
            checked = sharedPreferences.getBoolean(id, false);
        }

        public boolean isChecked() {
            return checked;
        }

        public SettingItem setChecked(boolean checked) {
            this.checked = checked;
            return this;
        }

        @Override
        public void onClick(Runnable1<Boolean> runnable) {
            checked = !checked;
            sharedPreferences.edit().putBoolean(id, checked).apply();
            if(callback != null) callback.call(checked);
            if(runnable != null) runnable.call(checked);
        }
    }

    public static class Group extends SettingItem {
        public Group(String id) {
            super(id);
            setType(GROUP);
        }

        @Override
        public void onClick(Runnable1 runnable) {

        }
    }

    public static class Label extends SettingItem {
        public Label(String id) {
            super(id);
            setType(LABEL);
        }

        @Override
        public void onClick(Runnable1 runnable) {

        }

    }

    public static class Text extends SettingItem<String> {
        private String value;

        public Text(String id) {
            super(id);
            setType(TEXT);
            value = sharedPreferences.getString(id, null);
        }

        @Override
        public void onClick(final Runnable1<String> runnable) {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();
            dialog.setTitle(getTitle());

            View view = context.getLayoutInflater().inflate(R.layout.dialog_preference_text, null);
            final EditText etText = (EditText) view.findViewById(R.id.et_text);

            etText.setText(value);
            if(getMessage() != null) {
                TextView tvMessage = (TextView) view.findViewById(R.id.tv_message);
                tvMessage.setText(getMessage());
                tvMessage.setVisibility(View.VISIBLE);
            }

            dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    value = etText.getText().toString();
                    if(value.length() == 0) {
                        sharedPreferences.edit().remove(id).apply();
                        value = null;
                    } else {
                        sharedPreferences.edit().putString(id, value).apply();
                    }
                    if(callback != null) callback.call(value);
                    if(runnable != null) runnable.call(value);
                }
            });

            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            dialog.setView(view);
            dialog.show();
        }

        @Override
        public String fetchSummary() {
            if (value != null) return value;
            return getMessage();
        }

    }

    public static class Page extends SettingItem {
        public Page(String id) {
            super(id);
            setType(PAGE);
        }

        @Override
        public void onClick(Runnable1 runnable) {

        }

    }

    public static class List extends SettingItem<Map<String,String>> {

        public List(String id) {
            super(id);
            setType(LIST);
        }

        @Override
        public void onClick(Runnable1<Map<String, String>> runnable) {

        }


        private Map<String,String> values;

        public Map<String, String> getValues() {
            return values;
        }

        public SettingItem setValues(Map<String, String> values) {
            this.values = values;
            return this;
        }

    }


}
