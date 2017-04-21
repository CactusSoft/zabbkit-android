package ru.zabbkitserver.android.remote.utils;

import android.util.Log;

import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class MyX509TrustManager implements X509TrustManager {

	private static final String TAG = MyX509TrustManager.class.getSimpleName();

	private static final MyX509TrustManager INSTANCE = new MyX509TrustManager();

	private MyX509TrustManager() {
	}

	public static MyX509TrustManager getInstance() {
		return INSTANCE;
	}

	private static X509TrustManager sDefaultTrustManager;
	private static X509TrustManager sLocalTrustManager;

	private static X509Certificate[] sAcceptedIssuers;

	public static void init(KeyStore localKeyStore) {
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init((KeyStore) null);
			sDefaultTrustManager = findX509TrustManager(tmf);
			if (sDefaultTrustManager == null) {
				throw new IllegalStateException("Couldn't find X509TrustManager");
			}

			sLocalTrustManager = new LocalStoreX509TrustManager(localKeyStore);

			List<X509Certificate> allIssuers = new ArrayList<X509Certificate>();
			Collections.addAll(allIssuers, sDefaultTrustManager.getAcceptedIssuers());
			Collections.addAll(allIssuers, sLocalTrustManager.getAcceptedIssuers());

			sAcceptedIssuers = allIssuers.toArray(new X509Certificate[allIssuers.size()]);
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static X509TrustManager findX509TrustManager(TrustManagerFactory tmf) {
		TrustManager tms[] = tmf.getTrustManagers();
		for (final TrustManager tm : tms) {
			if (tm instanceof X509TrustManager) {
				return (X509TrustManager) tm;
			}
		}

		return null;
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		try {
			Log.d(TAG, "checkServerTrusted() with default trust manager...");
			sDefaultTrustManager.checkClientTrusted(chain, authType);
		} catch (CertificateException ce) {
			Log.d(TAG, "checkServerTrusted() with local trust manager...");
			sLocalTrustManager.checkClientTrusted(chain, authType);
		}
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		try {
			Log.d(TAG, "checkServerTrusted() with default trust manager...");
			sDefaultTrustManager.checkServerTrusted(chain, authType);
		} catch (CertificateException ce) {
			Log.d(TAG, "checkServerTrusted() with local trust manager...");
			sLocalTrustManager.checkServerTrusted(chain, authType);
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return sAcceptedIssuers;
	}

	static class LocalStoreX509TrustManager implements X509TrustManager {

		private X509TrustManager trustManager;

		LocalStoreX509TrustManager(KeyStore localTrustStore) throws NoSuchAlgorithmException, KeyStoreException {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(localTrustStore);

			trustManager = findX509TrustManager(tmf);
			if (trustManager == null) {
				throw new IllegalStateException("Couldn't find X509TrustManager");
			}
		}

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			trustManager.checkClientTrusted(chain, authType);
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			trustManager.checkServerTrusted(chain, authType);
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return trustManager.getAcceptedIssuers();
		}
	}
}

