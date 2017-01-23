package ru.wtg.whereaminowserver.servers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.wtg.whereaminowserver.helpers.HtmlGenerator;
import ru.wtg.whereaminowserver.helpers.Utils;
import ru.wtg.whereaminowserver.holders.admin.AdminHelpPageHolder;
import ru.wtg.whereaminowserver.holders.admin.AdminHomePageHolder;
import ru.wtg.whereaminowserver.holders.admin.AdminMainPageHolder;
import ru.wtg.whereaminowserver.holders.admin.AdminSettingsPageHolder;
import ru.wtg.whereaminowserver.holders.admin.AdminSummaryPageHolder;
import ru.wtg.whereaminowserver.interfaces.PageHolder;


/**
 * Created 10/5/16.
 */
public class MyHttpAdminServer implements HttpHandler {

    private final LinkedHashMap<String, PageHolder> holders;
    private volatile MyWssServer wssProcessor;

    public MyHttpAdminServer(){

        holders = new LinkedHashMap<String, PageHolder>();

        LinkedList<String> classes = new LinkedList<String>();
        classes.add("AdminCreatePageHolder");
        classes.add("AdminHelpPageHolder");
        classes.add("AdminHomePageHolder");
        classes.add("AdminMainPageHolder");
        classes.add("AdminSettingsPageHolder");
        classes.add("AdminSummaryPageHolder");

        // IntroViewHolder must be registered last
//        classes.put("IntroViewHolder");

        for(String s:classes){
            try {
                //noinspection unchecked
                Class<PageHolder> _tempClass = (Class<PageHolder>) Class.forName("ru.wtg.whereaminowserver.holders.admin."+s);
                Constructor<PageHolder> ctor = _tempClass.getDeclaredConstructor(MyHttpAdminServer.class);
                PageHolder holder = ctor.newInstance(this);
                holders.put(holder.getType(), holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            System.out.println("Admin server requested");

            URI uri = exchange.getRequestURI();
            Map<String, List<String>> query = Utils.splitQuery(uri.toString());

            ArrayList<String> parts = new ArrayList<String>();
            parts.addAll(Arrays.asList(uri.getPath().split("/")));

            HtmlGenerator html;
            AdminMainPageHolder main = (AdminMainPageHolder) holders.get(AdminMainPageHolder.HOLDER_TYPE);
            if (parts.size() > 3 && parts.get(1).equals("admin") && holders.containsKey(parts.get(2)) && parts.get(3).equals("set")) {
                html = holders.get(parts.get(2)).create(query);
            } else if (parts.size() > 2 && parts.get(1).equals("admin") && holders.containsKey(parts.get(2))) {
                main.addPart(parts.get(2));
//                html = holders.get(parts.get(2)).create(query);
                html = holders.get(AdminMainPageHolder.HOLDER_TYPE).create(query);
            } else {
                html = holders.get(AdminMainPageHolder.HOLDER_TYPE).create(query);
            }

            byte[] bytes = html.build().getBytes();
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public MyWssServer getWssProcessor() {
        return wssProcessor;
    }

    public void setWssProcessor(MyWssServer wssProcessor) {
        this.wssProcessor = wssProcessor;
    }
}
