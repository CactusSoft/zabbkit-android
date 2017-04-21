package ru.zabbkitserver.android.remote.request;

import java.util.HashMap;
import java.util.Map;

import ru.zabbkitserver.android.remote.utils.RequestInfo;

/**
 * Represents RPC HTTP request.
 *
 * @author Elena.Bukarova
 */

public class Request {
    public final int requestId;
    public final String id;
    public final String url;
    public final String methodName;
    public final Map<String, Object> params;
    public final Class<?> clazz;
    public final String auth;
    public final Boolean isArray;
    public final AsyncRequestListener listener;
    public final String httpAuthLogin;
    public final String httpAuthPass;
    public final boolean isGraph;

    /**
     * Constructor
     *
     * @param requestId   - id of request (is assigned independently)
     * @param requestInfo - request info
     */
    public Request(int requestId, RequestInfo requestInfo) {
        this.requestId = requestId;
        this.id = requestInfo.id;
        this.url = requestInfo.url;
        this.methodName = requestInfo.method;
        if (requestInfo.params != null) {
            this.params = new HashMap<String, Object>(requestInfo.params);
        } else {
            this.params = null;
        }
        this.clazz = requestInfo.clazz;
        this.auth = requestInfo.auth;
        this.isArray = requestInfo.isArray;
        this.listener = requestInfo.getListener();
        this.httpAuthLogin = requestInfo.httpAuthLogin;
        this.httpAuthPass = requestInfo.httpAuthPass;
        this.isGraph = requestInfo.isGraphRequest();
    }
}


