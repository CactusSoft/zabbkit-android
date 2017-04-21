package ru.zabbkitserver.android.remote.request;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Listener for {@link ZabbKitServerManager}
 *
 * @author Elena.Bukarova
 */

public interface AsyncRequestListener {

	void onRequestFailure(Exception e, String message);

	void onRequestSuccess(List<Object> result, Class<?> clazz);

	void onCertificateRequest(X509Certificate[] certificate);
}
