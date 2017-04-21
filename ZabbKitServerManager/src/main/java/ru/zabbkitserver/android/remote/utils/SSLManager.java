package ru.zabbkitserver.android.remote.utils;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

public class SSLManager {

	private static final String TAG = SSLManager.class.getSimpleName();
	private volatile static SSLManager sInstance;
	private static final String TRUSTSTORE_PASSWORD = "a1enka";
	private static final String KEYSTORE_PASSWORD = "a1enka";
	private static String trustStorePropDefault;
	private static File localTrustStoreFile;
	private KeyStore keyStore;
	private static SSLResource mSSL;

	private SSLManager() {
	}

	public static SSLManager getInstance() {
		if (sInstance == null) {
			synchronized (SSLManager.class) {
				if (sInstance == null) {
					sInstance = new SSLManager();
				}
			}
		}
		return sInstance;
	}

	public static void init(SSLResource ssl) {
		mSSL = ssl;
		localTrustStoreFile = ssl.getBKSFile();
		trustStorePropDefault = System.getProperty("javax.net.ssl.trustStore");
		copyTrustStore();
		MyX509TrustManager.init(SSLManager.getInstance().loadTrustStore());
	}

	public void getCertificates(final HttpsURLConnection conn, final AsyncRequestListener listener) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {

				keyStore = loadTrustStore();
				Certificate[] certs = null;
				try {
					certs = conn.getServerCertificates();
				} catch (SSLPeerUnverifiedException e) {
					// Toast.makeText(mContext, e.getMessage(),
					// Toast.LENGTH_SHORT).show();
				}
				int i = 0;
				X509Certificate[] chain = new X509Certificate[certs.length];
				for (Certificate cert : certs) {
					if (cert instanceof X509Certificate) {
						chain[i] = (X509Certificate) cert;
						i++;
					}
				}
				if (chain != null) {
					try {
						MyX509TrustManager.getInstance().checkServerTrusted(chain, "RSA");
						listener.onCertificateRequest(null);
					} catch (java.security.cert.CertificateException e1) {
						listener.onCertificateRequest(chain);
					}
				}
				return null;
			}
		}.execute();
	}

	private static void copyTrustStore() {
		if (!localTrustStoreFile.exists()) {
			try {
				InputStream in = mSSL.getTrustStore();
				FileOutputStream out = new FileOutputStream(localTrustStoreFile);
				byte[] buff = new byte[1024];
				int read = 0;

				try {
					while ((read = in.read(buff)) > 0) {
						out.write(buff, 0, read);
					}
				} finally {
					in.close();

					out.flush();
					out.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}		
	}

	public KeyStore loadKeyStore() {
		if (keyStore != null) {
			return keyStore;
		}

		try {
			keyStore = KeyStore.getInstance("PKCS12");
			InputStream in = mSSL.getKeyStore();
			try {
				keyStore.load(in, KEYSTORE_PASSWORD.toCharArray());
			} finally {
				in.close();
			}

			return keyStore;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public KeyStore loadTrustStore() {
		try {
			KeyStore localTrustStore = KeyStore.getInstance("BKS");
			InputStream in = new FileInputStream(localTrustStoreFile);
			try {
				localTrustStore.load(in, TRUSTSTORE_PASSWORD.toCharArray());
			} finally {
				in.close();
			}

			return localTrustStore;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static int leInt(byte[] bytes) {
		int offset = 0;
		return ((bytes[offset++] & 0xff) << 0) | ((bytes[offset++] & 0xff) << 8)
				| ((bytes[offset++] & 0xff) << 16) | ((bytes[offset] & 0xff) << 24);
	}

	private static String hashName(X500Principal principal) {
		try {
			byte[] digest = MessageDigest.getInstance("MD5").digest(principal.getEncoded());

			String result = Integer.toString(leInt(digest), 16);
			if (result.length() > 8) {
				StringBuffer buff = new StringBuffer();
				int padding = 8 - result.length();
				for (int i = 0; i < padding; i++) {
					buff.append("0");
				}
				buff.append(result);

				return buff.toString();
			}

			return result;
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
	}

    public void addCertificates(final X509Certificate[] certs) {

        KeyStore localTrustStore = loadTrustStore();
        FileOutputStream out = null;
        try {

            for (X509Certificate cert : certs) {
                String alias = hashName(cert.getSubjectX500Principal());
                localTrustStore.setCertificateEntry(alias, cert);
            }

            out = new FileOutputStream(localTrustStoreFile);
            localTrustStore.store(out, TRUSTSTORE_PASSWORD.toCharArray());
            MyX509TrustManager.init(localTrustStore);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void dumpTrustedCerts() {
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory
					.getDefaultAlgorithm());
			tmf.init((KeyStore) null);
			X509TrustManager xtm = (X509TrustManager) tmf.getTrustManagers()[0];
			StringBuffer buff = new StringBuffer();
			for (X509Certificate cert : xtm.getAcceptedIssuers()) {
				String certStr = "S:" + cert.getSubjectDN().getName() + "\nI:"
						+ cert.getIssuerDN().getName();
				Log.d(TAG, certStr);
				buff.append(certStr + "\n\n");
			}
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
