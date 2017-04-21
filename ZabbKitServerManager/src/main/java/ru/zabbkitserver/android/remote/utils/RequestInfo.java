package ru.zabbkitserver.android.remote.utils;

import java.util.Map;

import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

public class RequestInfo {

	public String id;
	public String url;
	public String method;
	public Map<String, Object> params;
	public Class<?> clazz;
	public String auth;
	public Boolean isArray;
    public String httpAuthLogin;
    public String httpAuthPass;
	private AsyncRequestListener listener;
    private boolean isGraphRequest = false;

    public RequestInfo(String id, String url, String auth, String httpAuthLogin,
                       String httpAuthPass, boolean isGraphRequest) {
        this.id = id;
        this.url = url;
        this.auth = auth;
        this.httpAuthLogin = httpAuthLogin;
        this.httpAuthPass = httpAuthPass;
        this.isGraphRequest = isGraphRequest;
    }

	public RequestInfo(String id, String url, String method, Map<String, Object> params, Class<?> clazz, String auth,
			Boolean isArray, String httpAuthLogin, String httpAuthPass) {
		this(id, url, method, params, clazz, auth, isArray, httpAuthLogin, httpAuthPass, null);
	}

	public RequestInfo(String id, String url, String method, Map<String, Object> params, Class<?> clazz, String auth,
			Boolean isArray, String httpAuthLogin, String httpAuthPass, AsyncRequestListener listener) {
		this.id = id;
		this.url = url;
		this.method = method;
		this.params = params;
		this.clazz = clazz;
		this.auth = auth;
		this.isArray = isArray;
		this.listener = listener;
        this.httpAuthLogin = httpAuthLogin;
        this.httpAuthPass = httpAuthPass;
	}

	public void setListener(AsyncRequestListener listener) {
		this.listener = listener;
	}

	public AsyncRequestListener getListener() {
		return listener;
	}

    public boolean isGraphRequest() {
        return isGraphRequest;
    }
}
