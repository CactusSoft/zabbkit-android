package ru.zabbkit.android.push;

import android.os.SystemClock;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;

import ru.zabbkit.android.utils.L;

import static ru.zabbkit.android.app.Constants.GCM_APP_SERVER_URL;

/**
 * Helper class used to communicate with the app server for push notifications.
 * 
 * @author Sergey Tarasevich 17.07.2013
 */
final class GcmServerUtil {

	// 0 = iOS, 1 = Android, 2 = WP7
	private static final String REGISTER_CONTENT_FORMAT = "{type:1, token:'%s'}";
	private static final String RE_REGISTER_CONTENT_FORMAT = "{type:1, id:'%1$s', oldToken:'%2$s', newToken='%3$s'}";

	private static final int BUFFER_SIZE = 8 * 1024;
	private static final String UTF8 = "utf-8";
	private static final char CHAR_NEW_LINE = '\n';

	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLISECONDS = 2000;
	private static final int BACKOFF_ADD_MILLISECONDS = 1000;
	private static final int HTTP_CODES_ERROR_START = 400;
	private static final Random RANDOM = new Random();

	private GcmServerUtil() {
	}

	/**
	 * Register this account/device pair within the server.
	 * 
	 * @param regId
	 *            Registration ID
	 * @return device ID
	 */
	static String register(final String regId) {
		String content = String.format(REGISTER_CONTENT_FORMAT, regId);
		return send(content);
	}

	/**
	 * Re-register this account/device pair within the server.
	 * 
	 * @param devId
	 *            Device ID
	 * @param oldRegId
	 *            previous registration ID
	 * @param newRegId
	 *            Registration ID
	 */
	static void reRegister(final String devId, final String oldRegId,
			final String newRegId) {
		String content = String.format(RE_REGISTER_CONTENT_FORMAT, devId,
				oldRegId, newRegId);
		send(content);
	}

	private static String send(String content) {
		long backOff = BACKOFF_MILLISECONDS
				+ RANDOM.nextInt(BACKOFF_ADD_MILLISECONDS);
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			try {
				return post(GCM_APP_SERVER_URL, content);
			} catch (IOException e) {
				if (i == MAX_ATTEMPTS) {
					L.e(e);
					break;
				}
				SystemClock.sleep(backOff);
				backOff *= 2;
			}
		}
		return null;
	}

	/**
	 * Issue a POST request to the server.
	 * 
	 * @param endpoint
	 *            POST address.
	 * @param content
	 *            Content.
	 * @return Device ID
	 * @throws java.io.IOException
	 *             propagated from POST.
	 */
	private static String post(String endpoint, String content)
			throws IOException {
		URL url;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}
		byte[] bytes = content.getBytes("UTF-8");
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			// post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();
			// handle the response
			int status = conn.getResponseCode();
			if (status >= HTTP_CODES_ERROR_START) {
				throw new IOException("Post failed with error code " + status);
			}
			InputStream is = conn.getInputStream();
			String response = readAndCloseInputStream(is);
			try {
				JSONObject json = new JSONObject(response);
				return json.getString("Id");
			} catch (JSONException e) {
				throw new IOException(
						"Can't parse response to get Device ID from App server: "
								+ response);
			}
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static String readAndCloseInputStream(InputStream is)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is, UTF8),
					BUFFER_SIZE);
			String line;
			while ((line = br.readLine()) != null) {
				if (sb.length() > 0) {
					sb.append(CHAR_NEW_LINE);
				}
				sb.append(line);
			}
			return sb.toString();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}