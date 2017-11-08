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

import com.edeqa.helpers.interfaces.Runnable1;
import com.edeqa.waytous.MainActivity;
import com.edeqa.waytous.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created 7/28/17.
 */

@SuppressWarnings("WeakerAccess")
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
    private String messageHtml;
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

    public SettingItem<T> setTitle(String title) {
        this.title = title;
        return this;
    }

    public SettingItem<T> setTitle(int resId) {
        this.title = context.getString(resId);
        return this;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageHtml() {
        return messageHtml;
    }

    public SettingItem<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public SettingItem<T> setMessage(int resId) {
        this.message = context.getString(resId);
        return this;
    }

    public SettingItem<T> setMessageHtml(int resId) {
        this.messageHtml = context.getString(resId);
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

    public SettingItem<T> setId(String id) {
        this.id = id;
        return this;
    }

    public String[] getDepended() {
        return depended;
    }

    public SettingItem<T> setDepended(String[] depended) {
        this.depended = depended;
        return this;
    }

    public SettingItem<T> setInternalId(String internalId) {
        this.internalId = internalId;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public SettingItem<T> setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getInternalId() {
        return internalId;
    }

    @SuppressWarnings("HardCodedStringLiteral")
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

    public SettingItem<T> setPriority(int priority) {
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

    public SettingItem<T> setValue(String value) {
        return this;
    }
    public SettingItem<T> setValue(Integer value) {
        return this;
    }
    public SettingItem<T> setValue(Boolean value) {
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

        public SettingItem<Boolean> setChecked(boolean checked) {
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
        protected Map<String, SettingItem> itemsMap = new LinkedHashMap<>();

        public ArrayList<SettingItem> getItems() {
            return items;
        }

        private ArrayList<SettingItem> items = new ArrayList<>();

        public Group(Item id) {
            super("" + id);
        }

        public Group(String id) {
            super(id);
            setType(GROUP);
        }

        @Override
        public void onClick(Runnable1 runnable) {

        }

        public Group add(SettingItem item) {
            switch (item.getType()) {
                case SettingItem.GROUP:
                case SettingItem.PAGE:
                    if(itemsMap.containsKey(item.fetchId())) {
                        SettingItem current = itemsMap.get(item.fetchId());
                        if(item.getType() == PAGE || item.getType() == GROUP) {
                            for(Map.Entry<String, SettingItem> entry:((Group)item).itemsMap.entrySet())
                            ((Group)current).add(entry.getValue());
                        } else {
                            itemsMap.put(item.fetchId(), item);
                        }
                    } else {
                        if(item.getPriority() > 0) {
                            LinkedHashMap<String, SettingItem> newMap = new LinkedHashMap<>();
                            boolean added = false;

                            for (Map.Entry<String, SettingItem> entry : itemsMap.entrySet()) {
                                if(item.getPriority() > entry.getValue().getPriority()) {
                                    newMap.put(item.fetchId(), item);
                                    added = true;
                                }
                                newMap.put(entry.getKey(),entry.getValue());
                            }
                            if(!added) {
                                newMap.put(item.fetchId(), item);
                            }
                            itemsMap = newMap;
                        } else {
                            itemsMap.put(item.fetchId(), item);
                        }

                    }

                    raw.put(item.fetchId(), item);
                    break;
                case SettingItem.LABEL:
                case SettingItem.TEXT:
                case SettingItem.CHECKBOX:
                case SettingItem.LIST:
                    String id = item.getGroupId();
                    Group group;
                    if(itemsMap.containsKey(id)) {
                        group = (Group) itemsMap.get(id);
                    } else {
                        group = this;
                    }
                    group.itemsMap.put(item.fetchId(), item);
                    raw.put(item.fetchId(), item);
                    break;
            }
            items.clear();
            for(Map.Entry<String,SettingItem> g: itemsMap.entrySet()) {
                items.add(g.getValue());
                if(g.getValue().getType() == GROUP) {
                    for (Map.Entry<String, SettingItem> x : ((Group)g.getValue()).itemsMap.entrySet()) {
                        items.add(x.getValue());
                    }

                }
            }
            if(callback != null) callback.call(this);
            return this;

        }

        @Override
        public SettingItem.Group setTitle(String title) {
            super.setTitle(title);
            return this;
        }

        @Override
        public SettingItem.Group setTitle(int resId) {
            super.setTitle(resId);
            return this;
        }

        @Override
        public SettingItem.Group setPriority(int priority) {
            super.setPriority(priority);
            return this;
        }

    }

    public static class Label extends SettingItem<String> {
        private Intent intent;

        public Label(Item id) {
            super("" + id);
        }

        public Label(String id) {
            super(id);
            setType(LABEL);
        }

        @Override
        public void onClick(Runnable1<String> runnable) {
            if(callback != null) {
                callback.call(id);
            }
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
            final EditText etText = view.findViewById(R.id.et_text);

            etText.setText(value);
            if(getMessage() != null) {
                TextView tvMessage = view.findViewById(R.id.tv_message);
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

    public static class Page extends Group {

        public Page(String id) {
            super(id);
            setType(PAGE);
        }

        public Page(Item id) {
            this("" + id);
        }

        @Override
        public Page add(SettingItem item) {
            super.add(item);
            return this;
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
                hashMap.put("name", x.getKey()); //NON-NLS
                arrayList.add(hashMap);
            }
            return arrayList;
        }

        @Override
        public void onClick(final Runnable1<String> runnable) {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();
            dialog.setTitle(getTitle());

            View view = context.getLayoutInflater().inflate(R.layout.dialog_preference_list, null);
            final ListView list = view.findViewById(android.R.id.list);

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
                TextView tvMessage = view.findViewById(R.id.tv_message);
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

    public interface Item {

    }

}
