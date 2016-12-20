package ru.wtg.whereaminowserver.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 10/18/16.
 */

public class HtmlGenerator {
    private Tag body;
    private Tag head;
    private int level = 0;

    public HtmlGenerator() {


    }

    public Tag addBody(){
        body = new Tag("body");
        return body;
    }

    public Tag getHead(){
        return head;
    }

    public Tag getBody(){
        return body;
    }


    public Tag addHead() {
        head = new Tag("head");
        return head;
    }

    public String build(){
        String res = "<html>";
        res += head.build();
        res += body.build();
        res += "</html>";
        return res;
    }

    public class Tag {
        String tag;
        String text;

        ArrayList<Object> inner = new ArrayList<Object>();
        Map<String,String> properties = new HashMap<String,String>();

        public Tag(String tag){
            this.tag = tag;
        }

        public Tag add(String type){
            Tag n = new Tag(type);
            inner.add(n);
            return n;
        }

        public String build(){
            String res = "\n";
            for(int i=0;i<level;i++) res += "   ";

            res += "<"+tag;

            if(!properties.isEmpty()){
                for(Map.Entry<String,String> x:properties.entrySet()){
                    String key = x.getKey();
                    String value = x.getValue();
                    key = key.replaceAll("\\\"","&quot;");
                    value = value.replaceAll("\\\"","&quot;");
                    res += " "+key+"=\""+ value +"\"";
                }
            }

            res += ">";
            boolean indent = false;
            for(Object x:inner){
                if(x instanceof Tag) {
                    indent = true;
                    level ++;
                    res += ((Tag)x).build();
                    level --;
                } else if(x instanceof String){
                    res += x;
                }
            }
            if(text != null) res += text;
            if(indent) {
                res += "\n";
                for (int i = 0; i < level; i++) res += "   ";
            }
            if(!"br".equals(tag)) {
                res += "</" + tag + ">";
            }
            return res;
        }

        public Tag with(String key,String value){
            properties.put(key,value);
            return this;
        }

        public Tag with(String key,int value){
            properties.put(key,String.valueOf(value));
            return this;
        }

        public Tag with(String text){
            inner.add(text);
            return this;
        }

        public Tag with(Number number){
            inner.add(number.toString());
            return this;
        }

    }

    public void clear(){
        head = null;
        body = null;
    }

}
