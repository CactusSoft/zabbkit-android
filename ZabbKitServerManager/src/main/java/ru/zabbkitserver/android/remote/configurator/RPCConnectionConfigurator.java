package ru.zabbkitserver.android.remote.configurator;

import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import ru.zabbkitserver.android.remote.client.ConnectionConfigurator;
import ru.zabbkitserver.android.remote.request.AsyncRequestListener;
import ru.zabbkitserver.android.remote.utils.MyX509TrustManager;

/**
 * Created by Alex.Shimborsky on 12/12/2014.
 */
public class RPCConnectionConfigurator implements
        ConnectionConfigurator {

    private AsyncRequestListener mRequestListener;

    private  java.security.cert.X509Certificate[] mCerts;

    public RPCConnectionConfigurator(AsyncRequestListener listener) {
        mRequestListener = listener;
    }

    @Override
    public void configure(HttpURLConnection conn) {
        mCerts = null;

        if (conn instanceof HttpsURLConnection) {

            X509TrustManager[] trustAllCerts = new X509TrustManager[]{new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return MyX509TrustManager.getInstance()
                            .getAcceptedIssuers();
                }

                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs,
                        String authType) throws CertificateException {
                    MyX509TrustManager.getInstance().checkClientTrusted(
                            certs, authType);
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs,
                        String authType) throws CertificateException {
                    try {
                        MyX509TrustManager.getInstance()
                                .checkServerTrusted(certs, authType);
                    } catch (CertificateException e) {
                        mCerts = certs;
                        mRequestListener.onCertificateRequest(certs);
                        throw e;
                    }
                }
            }};
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
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                mRequestListener.onRequestFailure(e, e.getMessage());
            }

        }
    }


    public java.security.cert.X509Certificate[] getCertificate(){
        return mCerts;
    }
}