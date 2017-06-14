
package com.edeqa.waytousserver.holders.admin;

import com.edeqa.waytousserver.helpers.Common;
import com.edeqa.waytousserver.helpers.Constants;
import com.edeqa.waytousserver.helpers.HtmlGenerator;
import com.edeqa.waytousserver.helpers.MyGroup;
import com.edeqa.waytousserver.helpers.RequestWrapper;
import com.edeqa.waytousserver.helpers.Utils;
import com.edeqa.waytousserver.interfaces.Callable1;
import com.edeqa.waytousserver.interfaces.PageHolder;
import com.edeqa.waytousserver.servers.AdminServletHandler;
import com.google.api.client.http.HttpMethods;

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

    private final AdminServletHandler server;
    private HtmlGenerator html;

    @SuppressWarnings("HardCodedStringLiteral")
    public AdminRestHolder(AdminServletHandler server) {
        this.server = server;
    }

    @Override
    public String getType() {
        return HOLDER_TYPE;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @Override
    public boolean perform(RequestWrapper requestWrapper) {

        URI uri = requestWrapper.getRequestURI();

        Common.log(LOG, requestWrapper.getRemoteAddress(), uri.getPath());

        switch(requestWrapper.getRequestMethod()) {
            case HttpMethods.GET:
            case HttpMethods.PUT:
                switch (uri.getPath()) {
                    case "/admin/rest/v1/groups/clean":
                        cleanGroupsV1(requestWrapper);
                        return true;
                    default:
                        actionNotSupported(requestWrapper);
                        return true;
                }
            case HttpMethods.POST:
                switch (uri.getPath()) {
                    case "/admin/rest/v1/group/create":
                        createGroupV1(requestWrapper);
                        return true;
                    case "/admin/rest/v1/group/delete":
                        deleteGroupV1(requestWrapper);
                        return true;
                    case "/admin/rest/v1/group/modify":
                        modifyPropertyInGroupV1(requestWrapper);
                        return true;
                    case "/admin/rest/v1/group/switch":
                        switchPropertyInGroupV1(requestWrapper);
                        return true;
                    case "/admin/rest/v1/user/remove":
                        removeUserV1(requestWrapper);
                        return true;
                    case "/admin/rest/v1/user/switch":
                        switchPropertyForUserV1(requestWrapper);
                        return true;
                    default:
                        actionNotSupported(requestWrapper);
                        return true;
                }
        }

        return false;
    }

    private void cleanGroupsV1(RequestWrapper requestWrapper) {
        try {

            //noinspection HardCodedStringLiteral
            Common.log(LOG, "cleanGroupsV1");

            server.getDataProcessor("v1").validateGroups();

            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.SUCCESS);
            json.put(Constants.REST.MESSAGE, "Clean started.");
            Utils.sendResultJson.call(requestWrapper, json);

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            Utils.sendError.call(requestWrapper, 400, json);
        }

    }

    private void createGroupV1(final RequestWrapper requestWrapper) {
        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = requestWrapper.getRequestBody();
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

            server.getDataProcessor("v1").createGroup(group,
                new Callable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendResultJson.call(requestWrapper, json);
                    }
                }, new Callable1<JSONObject>() {
                    @Override
                    public void call(JSONObject json) {
                        Utils.sendError.call(requestWrapper, 500, json);
                    }
                });

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(requestWrapper, 400, json);
        }
    }

    private void deleteGroupV1(final RequestWrapper requestWrapper) {
        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = requestWrapper.getRequestBody();
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

            server.getDataProcessor("v1").deleteGroup(groupId,new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendResultJson.call(requestWrapper, json);
                }
            }, new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendError.call(requestWrapper, 500, json);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(requestWrapper, 400, json);
        }
    }

    private void removeUserV1(final RequestWrapper requestWrapper) {
        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = requestWrapper.getRequestBody();
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

            server.getDataProcessor("v1").removeUser(groupId,userNumber,new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendResultJson.call(requestWrapper, json);
                }
            }, new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendError.call(requestWrapper, 500, json);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(requestWrapper, 400, json);
        }
    }

    private void switchPropertyInGroupV1(final RequestWrapper requestWrapper) {

        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = requestWrapper.getRequestBody();
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

            server.getDataProcessor("v1").switchPropertyInGroup(groupId,property,new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendResultJson.call(requestWrapper, json);
                }
            }, new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendError.call(requestWrapper, 500, json);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(requestWrapper, 400, json);
        }

    }

    private void switchPropertyForUserV1(final RequestWrapper requestWrapper) {
        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = requestWrapper.getRequestBody();
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

            server.getDataProcessor("v1").switchPropertyForUser(groupId,userNumber,property,value,new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendResultJson.call(requestWrapper, json);
                }
            }, new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendError.call(requestWrapper, 500, json);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(requestWrapper, 400, json);
        }
    }

    private void actionNotSupported(final RequestWrapper requestWrapper) {
        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = requestWrapper.getRequestBody();
            int b;
            while((b = is.read()) != -1) {
                buf.append((char) b);
            }

            is.close();
            options = buf.toString();

            //noinspection HardCodedStringLiteral
            Common.log(LOG, "actionNotSupported:", requestWrapper.getRequestURI().getPath());

            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Action not supported.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(requestWrapper, 400, json);

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(requestWrapper, 400, json);
        }

    }

    private void modifyPropertyInGroupV1(final RequestWrapper requestWrapper) {

        String options = "";
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = requestWrapper.getRequestBody();
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

            server.getDataProcessor("v1").modifyPropertyInGroup(groupId,property,value,new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendResultJson.call(requestWrapper, json);
                }
            }, new Callable1<JSONObject>() {
                @Override
                public void call(JSONObject json) {
                    Utils.sendError.call(requestWrapper, 500, json);
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
            JSONObject json = new JSONObject();
            json.put(Constants.REST.STATUS, Constants.REST.ERROR);
            json.put(Constants.REST.MESSAGE, "Incorrect request.");
            json.put(Constants.REST.REQUEST, options);
            Utils.sendError.call(requestWrapper, 400, json);
        }
    }


}
