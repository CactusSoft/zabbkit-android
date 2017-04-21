package ru.zabbkit.android.push;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.utils.L;

/**
 * Helper class used to register device at GCM
 * 
 * @author Sergey Tarasevich 17.07.2013
 */
public final class GcmUtil {

	private GcmUtil() {
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p/>
	 * Stores the registration id, app versionCode in the application's shared
	 * preferences.
	 */
	public static void register(final Context context,
			final GcmRegistrationListener listener) {
		new AsyncTask<Void, Void, Boolean>() {

			private String regId;
			private String devId;
			private Exception occurredException;

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					GcmPrefUtil.saveWaitingForRegistration(context, true);
					regId = GoogleCloudMessaging.getInstance(context).register(
							Constants.SENDER_ID);
					String oldRegId = GcmPrefUtil.readRegId(context);
					if (oldRegId == null) {
						devId = GcmServerUtil.register(regId);
						GcmPrefUtil.saveRegAndDevId(context, regId, devId);
					} else if (!TextUtils.equals(oldRegId, regId)) {
						String devId = GcmPrefUtil.readDevId(context);
						GcmServerUtil.reRegister(devId, oldRegId, regId);
						GcmPrefUtil.saveRegAndDevId(context, regId, devId);
					}
					return true;
				} catch (IOException e) {
					L.e(e);
					occurredException = e;
					GcmPrefUtil.saveRegistrationFail(context, true);
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean success) {
				if (listener != null) {
					if (success) {
						listener.onGcmRegistrationSucceed(regId, devId);
					} else {
						listener.onGcmRegistrationFailed(occurredException);
					}
				}
			}
		}.execute();
	}

	public interface GcmRegistrationListener {

		void onGcmRegistrationSucceed(String regId, String devId);

		void onGcmRegistrationFailed(Exception e);
	}
}