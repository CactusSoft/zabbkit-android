package ru.zabbkitserver.android.remote.request;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import ru.zabbkitserver.android.remote.client.JSONRPC2Session;
import ru.zabbkitserver.android.remote.client.JSONRPC2SessionException;
import ru.zabbkitserver.android.remote.client.JSONRPC2SessionOptions;
import ru.zabbkitserver.android.remote.configurator.RPCConnectionConfigurator;
import ru.zabbkitserver.android.remote.utils.HttpAuth;
import ru.zabbkitserver.android.remote.utils.MyX509TrustManager;
import ru.zabbkitserver.android.remote.utils.RequestInfo;
import ru.zabbkitserver.android.remote.utils.UrlParams;

/**
 * Class for communication with server (send request, getting response)
 *
 * @author Elena.Bukarova 12.02.2013
 */

public class ZabbKitServerManager {

    private static final String TAG = ZabbKitServerManager.class
            .getSimpleName();

    private static final ZabbKitServerManager INSTANCE = new ZabbKitServerManager();
    private int mRequestId = 0;
    private Map<Integer, Request> mRequests = new ConcurrentHashMap<Integer, Request>();
    private Map<String, JSONRPC2Session> mSessions = new ConcurrentHashMap<String, JSONRPC2Session>();

    private X509TrustManager[] trustAllCerts;

    private ZabbKitServerManager() {
    }

    public static ZabbKitServerManager getInstance() {
        return INSTANCE;
    }

    public void sendRequest(RequestInfo requestInfo) {
        Request request = new Request(mRequestId, requestInfo);
        addRequest(request);
        performRequests(mRequestId);
        mRequestId++;
    }

    private void addRequest(Request request) {
        mRequests.put(mRequestId, request);
    }

    private void removeRequest(int id) {
        mRequests.remove(id);
    }

    private void performRequests(int requestId) {
        if (!mRequests.isEmpty()) {
            final Request request = mRequests.get(requestId);
            if (request.isGraph) {
                new GraphRequestTask().execute(request);
            } else {
                JSONRPC2Session jsonRpcSession;
                if (!mSessions.containsKey(request.url)) {
                    jsonRpcSession = new JSONRPC2Session(request);
                    JSONRPC2SessionOptions options = jsonRpcSession
                            .getOptions();
                    options.setConnectTimeout(10 * 1000);
                    options.trustAllCerts(false);
                    jsonRpcSession.setOptions(options);
                    jsonRpcSession
                            .setConnectionConfigurator(new RPCConnectionConfigurator(
                                    request.listener));
                    mSessions.put(request.url, jsonRpcSession);
                } else {
                    jsonRpcSession = mSessions.get(request.url);
                }

                new RPCRequestTask(jsonRpcSession).execute(request);
            }
        }
    }

    private class GraphRequestTask extends
            AsyncTask<Request, Response, Boolean> {

        public static final int ONE_SECOND = 1000;
        public static final int READ_TIMEOUT = 10 * ONE_SECOND;
        public static final int CONNECT_TIMEOUT = 15 * ONE_SECOND;

        private List<Object> graphBitmapList = new ArrayList<Object>();
        private AsyncRequestListener listener;
        private boolean success = false;
        Request mRequest = null;

        @Override
        protected Boolean doInBackground(Request... requests) {
            InputStream is = null;
            mRequest = requests[0];
            listener = mRequest.listener;

            try {
                URL url = new URL(mRequest.url);
                URLConnection conn = (HttpURLConnection) url
                        .openConnection();

                HttpAuth digestAuth = new HttpAuth();
                conn = digestAuth.tryDigestAuthentication((HttpURLConnection) conn, mRequest.httpAuthLogin, mRequest.httpAuthPass,
                        null, null);

                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                ((HttpURLConnection) conn).setRequestMethod("GET");
                conn.setDoInput(true);

                if (mRequest.auth.length() > 0) {
                    conn.addRequestProperty(UrlParams.AUTH, mRequest.auth);
                }

                if (conn instanceof HttpsURLConnection) {

                    if (trustAllCerts == null) {
                        createTrust();
                    }
                    try {
                        SSLContext sc = SSLContext.getInstance("SSL");
                        sc.init(null, trustAllCerts,
                                new java.security.SecureRandom());
                        HttpsURLConnection httpsConn = (HttpsURLConnection) conn;
                        httpsConn.setSSLSocketFactory(sc.getSocketFactory());
                        httpsConn.setHostnameVerifier(new HostnameVerifier() {
                            @Override
                            public boolean verify(String hostname,
                                                  SSLSession session) {
                                return true;
                            }
                        });
                    } catch (NoSuchAlgorithmException e) {
                        Log.e(TAG, e.getMessage(), e);
                    } catch (KeyManagementException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }

                conn.connect();

                is = conn.getInputStream();
                if (is != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    graphBitmapList.add(bitmap);
                    success = true;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            removeRequest(mRequest.requestId);
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (success) {
                listener.onRequestSuccess(graphBitmapList, null);
            } else {
                listener.onRequestFailure(null, "");
            }
        }
    }

    private void createTrust() {
        trustAllCerts = new X509TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return MyX509TrustManager.getInstance()
                        .getAcceptedIssuers();
            }

            @Override
            public void checkClientTrusted(
                    X509Certificate[] certs,
                    String authType) throws CertificateException {
                MyX509TrustManager.getInstance().checkClientTrusted(
                        certs, authType);
            }

            @Override
            public void checkServerTrusted(
                    X509Certificate[] certs,
                    String authType) throws CertificateException {
                try {
                    MyX509TrustManager.getInstance()
                            .checkServerTrusted(certs, authType);
                } catch (CertificateException e) {
                    throw e;
                }
            }
        }};
    }

    private class RPCRequestTask extends
            AsyncTask<Request, Response, Boolean> {

        // JSONRPC error codes
        public static final int INVALID_PARAMS = -32602;

        private Class<?> mClass;
        private JSONRPC2Session mJsonRpcSession;
        private AsyncRequestListener mListener;

        private List<Object> mObjectList;
        private Exception mOccurredException;
        private String mErrorMessage;

        private X509Certificate[] certs;

        RPCRequestTask(JSONRPC2Session jsonRpcSession) {
            mJsonRpcSession = jsonRpcSession;
        }

        @Override
        protected Boolean doInBackground(Request... params) {
            Request request = params[0];
            JSONRPC2Request jsonRpcRequest = new JSONRPC2Request(
                    request.methodName, request.params, request.id);

            if (request.auth != null) {
                jsonRpcRequest.appendNonStdAttribute(UrlParams.AUTH,
                        request.auth);
            }
            mClass = request.clazz;
            mListener = request.listener;
            String JSONStr = jsonRpcRequest.toJSONString();
            String ReqStr = jsonRpcRequest.toString();

            Boolean success = sendRequest(jsonRpcRequest, request.isArray);
            removeRequest(request.requestId);
            return success;
        }

        private boolean sendRequest(JSONRPC2Request jsonRpcRequest,
                                    boolean isArray) {
            boolean success = false;
            try {
                JSONRPC2Response response = mJsonRpcSession
                        .send(jsonRpcRequest);
                JSONRPC2Error error = response.getError();
                if (error == null) {
                    mObjectList = parseJson(response, mClass, isArray);
                    success = true;
                } else {
                    mOccurredException = error;
                    mErrorMessage = error.getCode() == INVALID_PARAMS ? (String) error
                            .getData() : error.getMessage();
                }
            } catch (JSONRPC2SessionException e) {
                RPCConnectionConfigurator configurator = (RPCConnectionConfigurator) mJsonRpcSession.getConnectionConfigurator();
                certs = configurator.getCertificate();

                Log.e(TAG, e.getMessage(), e);
                mOccurredException = e;
                mErrorMessage = e.getMessage();
            } catch (Exception e) {
                Log.d(TAG, e.getMessage(), e);
                mOccurredException = e;
            } catch (OutOfMemoryError e) {
                Log.d(TAG, e.getMessage(), e);
                mOccurredException = null;
                mErrorMessage = null;
            }
            return success;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private List<Object> parseJson(JSONRPC2Response response,
                                       Class<?> clazz,
                                       Boolean isArray) {
            Gson gson = new Gson();
            List objectList = new ArrayList();
            if (isArray) {
                JSONObject jsonObject = response.toJSONObject();
                JSONArray jsonArray = (JSONArray) jsonObject.get("result");

                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject item = (JSONObject) jsonArray.get(i);
                    objectList.add(gson.fromJson(item.toJSONString(), clazz));
                }
            } else {
                Object modelObject = gson.fromJson(response.getResult()
                        .toString(), clazz);
                objectList.add(modelObject);
            }
            return objectList;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mListener.onRequestSuccess(mObjectList, mClass);
            } else {
                if (certs != null) {
                    mListener.onCertificateRequest(certs);
                } else {
                    mListener.onRequestFailure(mOccurredException, mErrorMessage);
                }
            }
        }
    }
}
