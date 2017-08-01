package com.edeqa.waytous.helpers;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;
import com.edeqa.waytous.interfaces.Runnable1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created 7/28/17.
 */

public class SettingItem<T> {

    public static final int GROUP = 0;
    public static final int TEXT = 1;
    public static final int CHECKBOX = 2;
    public static final int LIST = 3;
    public static final int LABEL = 4;
    public static final int PAGE = 5;


    private static SharedPreferences sharedPreferences;
    private static MainActivity context;

    private static Map<String,SettingItem> raw = new HashMap<>();

    private String internalId;
    protected String id;
    private String groupId;
    private String title;
    private String message;
    private int type;
    private String[] depended;
    protected Runnable1<T> callback;
    private int priority;

    public static void setSharedPreferences(SharedPreferences sharedPreferences) {
        SettingItem.sharedPreferences = sharedPreferences;
    }

    public static void setContext(MainActivity context) {
        SettingItem.context = context;
    }

    protected SettingItem(String id) {
        this.id = id;
        this.priority = 50;
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

    public SettingItem setTitle(int resId) {
        this.title = context.getString(resId);
        return this;
    }

    public String getMessage() {
        return message;
    }

    public SettingItem setMessage(String message) {
        this.message = message;
        return this;
    }

    public SettingItem setMessage(int resId) {
        this.message = context.getString(resId);
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

    public void onClick(Runnable1<T> runnable) {

    }

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

    public int getPriority() {
        return priority;
    }

    public SettingItem setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public void update(String id, String value) {
        if(raw.containsKey(id)) {
            raw.get(id).setValue(value);
        }
    }

    public void update(String id, Boolean value) {
        if(raw.containsKey(id)) {
            raw.get(id).setValue(value);
        }
    }

    public void update(String id, int value) {
        if(raw.containsKey(id)) {
            raw.get(id).setValue(value);
        }
    }

    public SettingItem setValue(String value) {
        return this;
    }
    public SettingItem setValue(Integer value) {
        return this;
    }
    public SettingItem setValue(Boolean value) {
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

        @Override
        public SettingItem.Checkbox setValue(Boolean value) {
            setChecked(value);
            return this;
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
        private Intent intent;
        public Label(String id) {
            super(id);
            setType(LABEL);
        }

        @Override
        public void onClick(Runnable1 runnable) {

        }

        public Intent getIntent() {
            return intent;
        }

        public SettingItem.Label setIntent(Intent intent) {
            this.intent = intent;
            return this;
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

        public SettingItem.Text setValue(String value) {
            this.value = value;
            return this;
        }

    }

    public static class Page extends SettingItem {
        private Map<String, ArrayList<SettingItem>> itemsMap = new LinkedHashMap<>();

        public ArrayList<SettingItem> getItems() {
            return items;
        }

        private ArrayList<SettingItem> items = new ArrayList<>();


        public Page(String id) {
            super(id);
            setType(PAGE);
        }

        @Override
        public void onClick(Runnable1 runnable) {

        }

        public Page add(SettingItem item) {
            switch (item.getType()) {
                case SettingItem.GROUP:
                case SettingItem.PAGE:
                    if(!itemsMap.containsKey(item.getId())) {
                        ArrayList<SettingItem> list = new ArrayList<>();
                        list.add(item);
                        if(item.getPriority() > 0) {
                            LinkedHashMap<String, ArrayList<SettingItem>> newMap = new LinkedHashMap<>();
                            boolean added = false;

                            for (Map.Entry<String, ArrayList<SettingItem>> entry : itemsMap.entrySet()) {
                                if(item.getPriority() > entry.getValue().get(0).getPriority()) {
                                    newMap.put(item.getId(), list);
                                    added = true;
                                }
                                newMap.put(entry.getKey(),entry.getValue());
                            }
                            if(!added) {
                                newMap.put(item.getId(), list);
                            }
                            itemsMap = newMap;
                        } else {
                            itemsMap.put(item.getId(), list);
                        }
                        raw.put(item.fetchId(), item);
                    }
                    break;
                case SettingItem.LABEL:
                case SettingItem.TEXT:
                case SettingItem.CHECKBOX:
                case SettingItem.LIST:
//                    ArrayList<SettingItem> list = itemsMap.get("general");
                    ArrayList<SettingItem> list;
                    String id = item.getGroupId();
                    if(itemsMap.containsKey(id)) {
                        list = itemsMap.get(item.getGroupId());
                    } else {
                        list = new ArrayList<SettingItem>();
                        itemsMap.put(item.getGroupId(), list);
                        raw.put(item.fetchId(), item);
                    }
                    addUnique(list,item);
                    break;
            }
            items.clear();
            for(Map.Entry<String,ArrayList<SettingItem>> g: itemsMap.entrySet()) {
                for (SettingItem x : g.getValue()) {
                    items.add(x);
//                    if(x.getType() == SettingItem.PAGE) break;
                }
            }
            if(callback != null) callback.call(this);
            return this;

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

        @Override
        public SettingItem.Page setTitle(String title) {
            super.setTitle(title);
            return this;
        }

        @Override
        public SettingItem.Page setTitle(int resId) {
            super.setTitle(resId);
            return this;
        }

        @Override
        public SettingItem.Page setPriority(int priority) {
            super.setPriority(priority);
            return this;
        }

    }

    public static class List extends SettingItem<String> {

        private String value;
        private Map<String,String> items = new LinkedHashMap<>();
        private Runnable1<String> onItemSelectedCallback;

        public List(String id) {
            super(id);
            setType(LIST);
            value = sharedPreferences.getString(id, null);
        }

        public SettingItem.List setItems(Map<String, String> items) {
            this.items = items;
            return this;
        }

        public SettingItem.List add(String value, String label) {
            items.put(label, value);
            return this;
        }

        public SettingItem.List add(String value, int resId) {
            items.put(context.getString(resId), value);
            return this;
        }

        public Set<String> fetchLabels() {
            return items.keySet();
        }

        public String fetchValue(String label) {
            return items.get(label);
        }

        public SettingItem.List setValue(String value) {
            this.value = value;
            return this;
        }

        private ArrayList<HashMap<String, String>> fetchList() {
            ArrayList<HashMap<String,String>> arrayList=new ArrayList<>();
            for(Map.Entry<String,String> x: items.entrySet()) {
                HashMap<String,String> hashMap=new HashMap<>();
                hashMap.put("name", x.getKey());
                arrayList.add(hashMap);
            }
            return arrayList;
        }

        @Override
        public void onClick(final Runnable1<String> runnable) {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();
            dialog.setTitle(getTitle());

            View view = context.getLayoutInflater().inflate(R.layout.dialog_preference_list, null);
            final ListView list = (ListView) view.findViewById(android.R.id.list);

            final ArrayList<String> labels = new ArrayList<>(fetchLabels());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice, labels);
            list.setAdapter(adapter);
            list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            list.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(onItemSelectedCallback != null) {
                        value = fetchValue(labels.get(position));
                        onItemSelectedCallback.call(value);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if(value != null) {
                for(int i = 0; i < labels.size(); i++) {
                    if(value.equals(fetchValue(labels.get(i)))) list.setItemChecked(i, true);
                }
            } else {
                list.setItemChecked(0, true);
            }

            if(getMessage() != null) {
                TextView tvMessage = (TextView) view.findViewById(R.id.tv_message);
                tvMessage.setText(getMessage());
                tvMessage.setVisibility(View.VISIBLE);
            }

            dialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    value = fetchValue(labels.get(list.getCheckedItemPosition()));
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
            if (value != null) {
                for(Map.Entry<String,String> x:items.entrySet()) {
                    if(value.equals(x.getValue())) return x.getKey();
                }
                return getMessage();
            }
            return getMessage();
        }

        public SettingItem.List setOnItemSelectedCallback(Runnable1<String> onItemSelectedCallback) {
            this.onItemSelectedCallback = onItemSelectedCallback;
            return this;
        }
    }


}
