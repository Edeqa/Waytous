package ru.wtg.whereaminowserver.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 10/18/16.
 */

public class HtmlGenerator {

    public static final String META = "meta";
    public static final String STYLE = "style";
    public static final String CLASS = "class";
    public static final String DIV = "div";
    public static final String SCRIPT = "script";
    public static final String TITLE = "title";
    public static final String ID = "id";
    public static final String SRC = "src";
    public static final String HTTP_EQUIV = "http-equiv";
    public static final String CONTENT = "content";
    public static final String TABLE = "table";
    public static final String TR = "tr";
    public static final String TH = "th";
    public static final String TD = "td";
    public static final String H1 = "h1";
    public static final String H2 = "h2";
    public static final String H3 = "h3";
    public static final String H4 = "h4";
    public static final String H5 = "h5";
    public static final String H6 = "h6";
    public static final String H7 = "h7";
    public static final String BORDER = "border";
    public static final String COLSPAN = "colspan";
    public static final String ROWSPAN = "rowspan";
    public static final String A = "a";
    public static final String HREF = "href";
    public static final String TARGET = "target";
    public static final String SMALL = "small";
    public static final String LINK = "link";
    public static final String REL = "rel";
    public static final String STYLESHEET = "stylesheet";
    public static final String TYPE = "type";
    public static final String BR = "br";
    ArrayList<String> notClosableTags = new ArrayList<String>(Arrays.asList(new String[]{BR,META}));
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

    public void clear(){
        head = null;
        body = null;
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
            if(!notClosableTags.contains(tag)) {
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

}
