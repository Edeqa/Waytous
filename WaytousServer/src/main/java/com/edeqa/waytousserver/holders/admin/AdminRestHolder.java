
package com.edeqa.waytousserver.holders.admin;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.Utils;
import com.edeqa.waytousserver.interfaces.Callable1;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.MyHttpAdminHandler;
import com.google.api.client.http.HttpMethods;
import com.sun.net.httpserver.HttpExchange;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URI;


/**
 * Created 4/20/2017.
 */

@SuppressWarnings("unused")
public class AdminRestHolder implements PageHolder {

    @SuppressWarnings("HardCodedStringLiteral")
    private static final String HOLDER_TYPE = "rest";
    @SuppressWarnings("HardCodedStringLiteral")
    private static final String LOG = "ARH";

    private final MyHttpAdminHandler server;
    private HtmlGenerator html;

    @SuppressWarnings("HardCodedStringLiteral")
    public AdminRestHolder(MyHttpAdminHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public boolean perform(HttpExchange exchange) {

        URI uri = exchange.getRequestURI();

        Common.log(LOG, exchange.getRemoteAddress(), uri.getPath());

        switch(exchange.getRequestMethod()) {
            case HttpMethods.GET:
                switch (uri.getPath()) {
                    default:
                        break;
                }
                break;
            case HttpMethods.PUT:
                switch (uri.getPath()) {
                    default:
                        break;
                }
                break;
            case HttpMethods.POST:
                switch (uri.getPath()) {
                    case "/admin/rest/v1/group/create":
                        createGroupV1(exchange);
                        return true;
                    case "/admin/rest/v1/group/delete":
                        deleteGroupV1(exchange);
                        return true;
                    case "/admin/rest/v1/group/modify":
                        modifyPropertyInGroupV1(exchange);
                        return true;
                    case "/admin/rest/v1/group/switch":
                        switchPropertyInGroupV1(exchange);
                        return true;
                    case "/admin/rest/v1/user/remove":
                        removeUserV1(exchange);
                        return true;
                    case "/admin/rest/v1/user/switch":
                        switchPropertyForUserV1(exchange);
                        return true;
                    default:
                        break;
                }
                break;
        }

        return false;
    }

    private void createGroupV1(final HttpExchange exchange) {
        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = exchange.getRequestBody();
            int b;
            while((b = is.read()) != -1) {
                buf.append((char) b);
            }

            is.close();
            options = buf.toString();

            //noinspection HardCodedStringLiteral
            Common.log(LOG, "createGroupV1:", options);

            JSONObject json = new JSONObject(options);

            MyGroup group = new MyGroup();
            if(json.has(Constants.REST.GROUP_ID)) group.setId(json.getString(Constants.REST.GROUP_ID));
            if(json.has(Constants.DATABASE.OPTION_REQUIRES_PASSWORD)) group.setRequiresPassword(json.getBoolean(Constants.DATABASE.OPTION_REQUIRES_PASSWORD));
            if(json.has("password")) group.setPassword(json.get("password").toString());
            if(json.has(Constants.DATABASE.OPTION_WELCOME_MESSAGE)) group.setWelcomeMessage(json.getString(Constants.DATABASE.OPTION_WELCOME_MESSAGE));
            if(json.has(Constants.DATABASE.OPTION_PERSISTENT)) group.setPersistent(json.getBoolean(Constants.DATABASE.OPTION_PERSISTENT));
            if(json.has(Constants.DATABASE.OPTION_TIME_TO_LIVE_IF_EMPTY)) {
                try {
                    group.setTimeToLiveIfEmpty(Integer.parseInt(json.getString(Constants.DATABASE.OPTION_TIME_TO_LIVE_IF_EMPTY)));
                } catch (Exception e) {
                    group.setTimeToLiveIfEmpty(15);
                }
            }
            if(json.has(Constants.DATABASE.OPTION_DISMISS_INACTIVE)) group.setDismissInactive(json.getBoolean(Constants.DATABASE.OPTION_DISMISS_INACTIVE));
            if(json.has(Constants.DATABASE.OPTION_DELAY_TO_DISMISS)) {
                try {
                    group.setDelayToDismiss(Integer.parseInt(json.getString(Constants.DATABASE.OPTION_DELAY_TO_DISMISS)));
                } catch(Exception e){
                    group.setDelayToDismiss(300);
                }
            }

            server.getDataProcessor().createGroup(group,
                new Callable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendResultJson.call(exchange, json);
                    }
                }, new Callable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendError.call(exchange, 500, json);
                    }
                });

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(exchange, 400, json);
        }
    }

    private void deleteGroupV1(final HttpExchange exchange) {
        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = exchange.getRequestBody();
            int b;
            while((b = is.read()) != -1) {
                buf.append((char) b);
            }

            is.close();
            options = buf.toString();

            //noinspection HardCodedStringLiteral
            Common.log(LOG, "deleteGroupV1:", options);

            JSONObject json = new JSONObject(options);
            String groupId = json.getString(Constants.REST.GROUP_ID);

            server.getDataProcessor().deleteGroup(groupId,new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendResultJson.call(exchange, json);
                }
            }, new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendError.call(exchange, 500, json);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(exchange, 400, json);
        }
    }

    private void removeUserV1(final HttpExchange exchange) {
        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = exchange.getRequestBody();
            int b;
            while((b = is.read()) != -1) {
                buf.append((char) b);
            }

            is.close();
            options = buf.toString();

            //noinspection HardCodedStringLiteral
            Common.log(LOG, "removeUserV1:", options);

            JSONObject json = new JSONObject(options);
            String groupId = json.getString(Constants.REST.GROUP_ID);
            Long userNumber = Long.parseLong(json.get(Constants.REST.USER_NUMBER).toString());

            server.getDataProcessor().removeUser(groupId,userNumber,new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendResultJson.call(exchange, json);
                }
            }, new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendError.call(exchange, 500, json);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(exchange, 400, json);
        }
    }

    private void switchPropertyInGroupV1(final HttpExchange exchange) {
        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = exchange.getRequestBody();
            int b;
            while((b = is.read()) != -1) {
                buf.append((char) b);
            }

            is.close();
            options = buf.toString();

            //noinspection HardCodedStringLiteral
            Common.log(LOG, "switchPropertyInGroupV1:", options);

            JSONObject json = new JSONObject(options);
            String groupId = json.getString(Constants.REST.GROUP_ID);
            String property = json.getString(Constants.REST.PROPERTY);

            server.getDataProcessor().switchPropertyInGroup(groupId,property,new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendResultJson.call(exchange, json);
                }
            }, new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendError.call(exchange, 500, json);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(exchange, 400, json);
        }

    }

    private void switchPropertyForUserV1(final HttpExchange exchange) {
        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = exchange.getRequestBody();
            int b;
            while((b = is.read()) != -1) {
                buf.append((char) b);
            }

            is.close();
            options = buf.toString();

            //noinspection HardCodedStringLiteral
            Common.log(LOG, "switchPropertyForUserV1:", options);

            JSONObject json = new JSONObject(options);
            String groupId = json.getString(Constants.REST.GROUP_ID);
            Long userNumber = Long.parseLong(json.getString(Constants.REST.USER_NUMBER));
            String property = json.getString(Constants.REST.PROPERTY);
            Boolean value = json.getBoolean(Constants.REST.VALUE);

            server.getDataProcessor().switchPropertyForUser(groupId,userNumber,property,value,new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendResultJson.call(exchange, json);
                }
            }, new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendError.call(exchange, 500, json);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(exchange, 400, json);
        }

    }

    private void modifyPropertyInGroupV1(final HttpExchange exchange) {

        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = exchange.getRequestBody();
            int b;
            while((b = is.read()) != -1) {
                buf.append((char) b);
            }

            is.close();
            options = buf.toString();

            //noinspection HardCodedStringLiteral
            Common.log(LOG, "modifyPropertyInGroupV1:", options);

            JSONObject json = new JSONObject(options);
            String groupId = json.getString(Constants.REST.GROUP_ID);
            String property = json.getString(Constants.REST.PROPERTY);
            String value = json.getString(Constants.REST.VALUE);

            server.getDataProcessor().modifyPropertyInGroup(groupId,property,value,new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendResultJson.call(exchange, json);
                }
            }, new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendError.call(exchange, 500, json);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(exchange, 400, json);
        }
    }


}
