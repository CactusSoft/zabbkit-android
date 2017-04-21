package ru.zabbkit.android.utils;

import android.os.AsyncTask;
import android.support.v4.util.ArrayMap;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLPeerUnverifiedException;

import ru.zabbkit.android.app.Constants;
import ru.zabbkitserver.android.remote.model.Event;
import ru.zabbkitserver.android.remote.model.Host;
import ru.zabbkitserver.android.remote.model.HostGroup;
import ru.zabbkitserver.android.remote.model.Trigger;
import ru.zabbkitserver.android.remote.model.ZabbixItem;
import ru.zabbkitserver.android.remote.request.AsyncRequestListener;
import ru.zabbkitserver.android.remote.request.CheckerListener;
import ru.zabbkitserver.android.remote.request.RequestType;
import ru.zabbkitserver.android.remote.request.ZabbKitServerManager;
import ru.zabbkitserver.android.remote.utils.RequestInfo;

/**
 * @author Alex.Shimborsky on 16.03.2016.
 */
public final class Communicator {

    private static final SharedPreferencesEditor PREFS_EDITOR = SharedPreferencesEditor
            .getInstance();

    private static final Communicator INSTANCE = new Communicator();

    private Communicator() {
    }

    public static Communicator getInstance() {
        return INSTANCE;
    }

    public void getHost(Map<String, Object> params,
                        AsyncRequestListener listener) {
        RequestInfo requestInfo = new RequestInfo(
                Constants.DEFAULT_SERVER_REQUEST_ID,
                PREFS_EDITOR.getString(Constants.PREFS_URL_FULL),
                RequestType.POST_HOST.getMethod(), params, Host.class,
                PREFS_EDITOR.getString(Constants.PREFS_AUTH), true,
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_LOGIN),
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_PASS));
        requestInfo.setListener(new HandleAuthIssueListener(requestInfo,
                listener));
        ZabbKitServerManager.getInstance().sendRequest(requestInfo);
    }

    public void getServers(Map<String, Object> params,
                           AsyncRequestListener listener) {
        RequestInfo requestInfo = new RequestInfo(
                Constants.DEFAULT_SERVER_REQUEST_ID,
                PREFS_EDITOR.getString(Constants.PREFS_URL_FULL),
                RequestType.POST_HOSTGROUP.getMethod(), params,
                HostGroup.class, PREFS_EDITOR.getString(Constants.PREFS_AUTH),
                true,
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_LOGIN),
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_PASS));
        requestInfo.setListener(new HandleAuthIssueListener(requestInfo,
                listener));
        ZabbKitServerManager.getInstance().sendRequest(requestInfo);
    }

    public void login(final Map<String, Object> params, final String url,
                      final AsyncRequestListener listener) {

        final AuthCheckTask authCheckTask = new AuthCheckTask();
        authCheckTask.url = url;
        authCheckTask.execute(new CheckerListener() {
            @Override
            public void onSuccess(String code, String message) {
                if (code.equals("200")) {
                    String request = null;
                    if (!url.contains(Constants.API_ADDRESS)) {
                        StringBuilder sb = new StringBuilder().append(url).append(Constants.HTTP_POSTFIX);
                        request = sb.toString();
                    } else {
                        request = url;
                    }
                    performLogin(request, params, listener);
                } else {
                    Exception ex = new Exception(message);
                    listener.onRequestFailure(ex, code);
                }
            }

            @Override
            public void onFailed(Exception e, String message) {
                if (e instanceof SSLPeerUnverifiedException) {
                    performLogin(url, params, listener);
                } else if (message != null && message.length() > 0) {
                    listener.onRequestFailure(e, message);
                } else {
                    performLogin(url, params, listener);
                }
            }

            @Override
            public void onFailed(int code, String message, Object result, Class clazz) {
                if (code >= 300 && code < 400 && String.class.equals(clazz)) {
                    String redirectedUrl = (String) result;
                    login(params, redirectedUrl, listener);
                }
            }
        });
    }

    private void performLogin(String url, Map<String, Object> params, AsyncRequestListener listener) {
        RequestInfo requestInfo = new RequestInfo(
                Constants.DEFAULT_SERVER_REQUEST_ID, url,
                RequestType.POST_LOGIN.getMethod(), params, String.class, null,
                false,
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_LOGIN),
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_PASS));
        requestInfo.setListener(new HandleAuthIssueListener(requestInfo,
                listener));
        ZabbKitServerManager.getInstance().sendRequest(requestInfo);
    }

    public void getTriggerHistory(Map<String, Object> params,
                                  AsyncRequestListener listener) {
        RequestInfo requestInfo = new RequestInfo(
                Constants.DEFAULT_SERVER_REQUEST_ID,
                PREFS_EDITOR.getString(Constants.PREFS_URL_FULL),
                RequestType.POST_EVENT.getMethod(), params, Event.class,
                PREFS_EDITOR.getString(Constants.PREFS_AUTH), true,
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_LOGIN),
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_PASS));
        requestInfo.setListener(new HandleAuthIssueListener(requestInfo,
                listener));
        ZabbKitServerManager.getInstance().sendRequest(requestInfo);
    }

    public void logout(Map<String, Object> params, AsyncRequestListener listener) {
        RequestInfo requestInfo = new RequestInfo(
                Constants.DEFAULT_SERVER_REQUEST_ID,
                PREFS_EDITOR.getString(Constants.PREFS_URL_FULL),
                RequestType.POST_LOGOUT.getMethod(), params, Boolean.class,
                PREFS_EDITOR.getString(Constants.PREFS_AUTH), false,
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_LOGIN),
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_PASS));
        requestInfo.setListener(new HandleAuthIssueListener(requestInfo,
                listener));
        ZabbKitServerManager.getInstance().sendRequest(requestInfo);
    }

    public void getTriggers(Map<String, Object> params,
                            AsyncRequestListener listener) {
        RequestInfo requestInfo = new RequestInfo(
                Constants.DEFAULT_SERVER_REQUEST_ID,
                PREFS_EDITOR.getString(Constants.PREFS_URL_FULL),
                RequestType.POST_TRIGGER.getMethod(), params, Trigger.class,
                PREFS_EDITOR.getString(Constants.PREFS_AUTH), true,
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_LOGIN),
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_PASS));
        requestInfo.setListener(new HandleAuthIssueListener(requestInfo,
                listener));
        ZabbKitServerManager.getInstance().sendRequest(requestInfo);
    }

    public void getEvent(Map<String, Object> params,
                         AsyncRequestListener listener) {
        RequestInfo requestInfo = new RequestInfo(
                Constants.DEFAULT_SERVER_REQUEST_ID,
                PREFS_EDITOR.getString(Constants.PREFS_URL_FULL),
                RequestType.POST_EVENT.getMethod(), params, Event.class,
                PREFS_EDITOR.getString(Constants.PREFS_AUTH), true,
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_LOGIN),
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_PASS));
        requestInfo.setListener(new HandleAuthIssueListener(requestInfo,
                listener));
        ZabbKitServerManager.getInstance().sendRequest(requestInfo);
    }

    public void getItems(Map<String, Object> params,
                         AsyncRequestListener listener) {
        RequestInfo requestInfo = new RequestInfo(
                Constants.DEFAULT_SERVER_REQUEST_ID,
                PREFS_EDITOR.getString(Constants.PREFS_URL_FULL),
                RequestType.POST_ITEM.getMethod(), params, ZabbixItem.class,
                PREFS_EDITOR.getString(Constants.PREFS_AUTH), true,
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_LOGIN),
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_PASS));
        requestInfo.setListener(new HandleAuthIssueListener(requestInfo,
                listener));
        ZabbKitServerManager.getInstance().sendRequest(requestInfo);
    }

    public void getGraph(AsyncRequestListener listener, String url) {
        RequestInfo requestInfo = new RequestInfo(Constants.DEFAULT_SERVER_REQUEST_ID, url,
                PREFS_EDITOR.getString(Constants.PREFS_AUTH),
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_LOGIN),
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_PASS), true);
        requestInfo.setListener(new HandleAuthIssueListener(requestInfo, listener));
        ZabbKitServerManager.getInstance().sendRequest(requestInfo);
    }

    public void getItemObjects(Map<String, Object> params,
                               AsyncRequestListener listener) {
        RequestInfo requestInfo = new RequestInfo(
                Constants.DEFAULT_SERVER_REQUEST_ID,
                PREFS_EDITOR.getString(Constants.PREFS_URL_FULL),
                RequestType.POST_ITEM_OBJECTS.getMethod(), params,
                ZabbixItem.class, PREFS_EDITOR.getString(Constants.PREFS_AUTH),
                true,
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_LOGIN),
                PREFS_EDITOR.getString(Constants.PREFS_HTTP_AUTH_PASS));
        requestInfo.setListener(new HandleAuthIssueListener(requestInfo,
                listener));
        ZabbKitServerManager.getInstance().sendRequest(requestInfo);
    }

    /**
     * Wrapper for {@linkplain AsyncRequestListener} which handles
     * "Not authorize user" error, re-login if need and re-send request
     */
    private static final class HandleAuthIssueListener implements
            AsyncRequestListener {

        private final RequestInfo mRequestInfo;
        private final AtomicBoolean isReAuthorization;
        private final Reference<AsyncRequestListener> mListener;

        private final ZabbKitServerManager mServerManager;

        private HandleAuthIssueListener(RequestInfo requestInfo,
                                        AsyncRequestListener listener) {
            mRequestInfo = requestInfo;
            mListener = new WeakReference<AsyncRequestListener>(listener);

            isReAuthorization = new AtomicBoolean();
            mServerManager = ZabbKitServerManager.getInstance();
        }

        @Override
        public void onRequestFailure(Exception e, String message) {
            if (e != null) {
                if ("Not authorized".equals(e.getMessage())) { // Fix for unexpected
                    // "Not authorized user"
                    // error
                    isReAuthorization.set(true);
                    final Map<String, Object> params = new ArrayMap<String, Object>();
                    params.put(Constants.PREFS_USER,
                            PREFS_EDITOR.getString(Constants.PREFS_USER));
                    params.put(Constants.PREFS_PASSWORD,
                            PREFS_EDITOR.getString(Constants.PREFS_PASSWORD));
                    Communicator.getInstance().login(params,
                            PREFS_EDITOR.getString(Constants.PREFS_URL_FULL), this);
                } else {
                    AsyncRequestListener listener = mListener.get();
                    if (listener != null) {
                        listener.onRequestFailure(e, message);
                    }
                }
            } else {
                AsyncRequestListener listener = mListener.get();
                if (listener != null) {
                    listener.onRequestFailure(e, message);
                }
            }
        }

        @Override
        public void onRequestSuccess(List<Object> result, Class<?> clazz) {
            if (isReAuthorization.getAndSet(false)) {
                L.i("Re-authorized. Re-send request...");
                mServerManager.sendRequest(mRequestInfo);
            } else {
                AsyncRequestListener listener = mListener.get();
                if (listener != null) {
                    if (listener instanceof LoginRequestListener) {
                        String url = mRequestInfo.url.replace(Constants.HTTP_POSTFIX, "");
                        ((LoginRequestListener) listener).onRequestSuccess(url, result, clazz);
                    } else {
                        listener.onRequestSuccess(result, clazz);
                    }
                }
            }
        }

        @Override
        public void onCertificateRequest(X509Certificate[] certificate) {
            AsyncRequestListener listener = mListener.get();
            if (listener != null) {
                listener.onCertificateRequest(certificate);
            }
        }
    }

    private class AuthCheckTask extends AsyncTask<CheckerListener, String, Void> {

        public String url;
        private CheckerListener listener;
        private int statusCode = -1;
        private String statusMessage;
        private HttpResponse mResponse;
        private Exception exception = null;

        @Override
        protected Void doInBackground(CheckerListener... checkerListeners) {
            listener = checkerListeners[0];

            HttpPost mRequest = null;
            try {
                mRequest = new HttpPost(url);

                DefaultHttpClient client = new DefaultHttpClient();
                HttpParams httpParameters = client.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, Constants.REQUEST_TIMEOUT * 1000);
                HttpConnectionParams.setSoTimeout(httpParameters, Constants.REQUEST_TIMEOUT * 1000);

                String authLogin = SharedPreferencesEditor.getInstance().getString(Constants.PREFS_HTTP_AUTH_LOGIN);
                String authPass = SharedPreferencesEditor.getInstance().getString(Constants.PREFS_HTTP_AUTH_PASS);
                if (authLogin != null && authPass != null) {
                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY,
                            new UsernamePasswordCredentials(authLogin, authPass));
                    client.setCredentialsProvider(credentialsProvider);
                }

                client.setRedirectHandler(new RedirectHandler() {
                    @Override
                    public boolean isRedirectRequested(HttpResponse httpResponse, HttpContext httpContext) {
                        return false;
                    }

                    @Override
                    public URI getLocationURI(HttpResponse httpResponse, HttpContext httpContext) throws ProtocolException {
                        return null;
                    }
                });

                mResponse = client.execute(mRequest);
                statusCode = mResponse.getStatusLine().getStatusCode();
                statusMessage = mResponse.getStatusLine().getReasonPhrase();
            } catch (IOException e) {
                exception = e;
                publishProgress(e.getMessage());
            } catch (Exception e) {
                publishProgress(e.getMessage());
            } finally {
                if (mRequest != null) {
                    mRequest.abort();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (statusCode > 0) {
                if (statusCode == 412) {
                    //TODO: hardcode
                    statusCode = 200;
                }

                if (statusCode >= 200 && statusCode < 300) {
                    listener.onSuccess(String.valueOf(statusCode), statusMessage);
                } else if (statusCode >= 300 && statusCode < 400) {
                    org.apache.http.Header[] h = mResponse.getHeaders("Location");
                    if (h != null && h.length > 0) {
                        String url = h[0].getValue();
                        url = url.replace(Constants.HTTP_POSTFIX, "");
                        listener.onFailed(statusCode, statusMessage, url, String.class);
                    }
                } else {
                    if (statusMessage.equals(Constants.HTTP_AUTH_FAIL)) {
                        listener.onFailed(new Exception(Constants.HTTP_AUTH_FAIL), String.valueOf(statusCode));
                    } else {
                        listener.onFailed(null, statusMessage);
                    }
                }
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (values != null && values[0] != null) {
                listener.onFailed(exception, values[0]);
            }
        }
    }
}
