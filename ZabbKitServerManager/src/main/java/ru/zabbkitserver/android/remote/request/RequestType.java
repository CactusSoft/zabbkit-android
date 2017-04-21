package ru.zabbkitserver.android.remote.request;

/**
 * Request types for {@link ZabbKitServerManager}
 *
 * @author Elena.Bukarova
 */
public enum RequestType {

    POST_LOGIN("user.login"), POST_HOSTGROUP("hostgroup.get"), POST_TRIGGER("trigger.get"),
    POST_HOST("host.get"), POST_EVENT("event.get"), POST_LOGOUT("user.logout"),
    POST_ITEM("item.get"), POST_ITEM_OBJECTS("item.getobjects");

    private String mMethod;

    private RequestType(String method) {
        mMethod = method;
    }

    public String getMethod() {
        return mMethod;
    }
}
