package ru.zabbkit.android.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Helper class used to work with preferences about GCM data
 * 
 * @author Sergey Tarasevich 17.07.2013
 */
public final class GcmPrefUtil {

	private static final String PREFS_FILE_NAME = "zabbkit-gcm-prefs";

	private static final String PREF_REGISTRATION_ID = "registrationId"; // Registration
																			// ID
																			// at
																			// GCM
	private static final String PREF_DEVICE_ID = "deviceId"; // Device ID at App
																// server
	private static final String PREF_APP_VERSION = "appVersion";
	private static final String PREF_WAITING_FOR_REGISTRATION = "waitingForRegistration";
	private static final String PREF_REGISTRATION_FAIL = "RegistrationFail";

	private GcmPrefUtil() {
	}

	private static SharedPreferences getPrefs(Context context) {
		return context.getSharedPreferences(PREFS_FILE_NAME,
				Context.MODE_PRIVATE);
	}

	public static String readRegId(Context context) {
		final SharedPreferences prefs = getPrefs(context);
		final int registeredVersion = prefs.getInt(PREF_APP_VERSION,
				Integer.MIN_VALUE);
		final int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			clearRegAndDevIds(context);
		}
		return prefs.getString(PREF_REGISTRATION_ID, null);
	}

	static void saveRegAndDevId(Context context, String regId, String devId) {
		final int appVersion = getAppVersion(context);
		getPrefs(context).edit().putString(PREF_REGISTRATION_ID, regId)
				.putString(PREF_DEVICE_ID, devId)
				.putInt(PREF_APP_VERSION, appVersion).apply();
	}

	static void clearRegAndDevIds(Context context) {
		final SharedPreferences prefs = getPrefs(context);
		prefs.edit().remove(PREF_REGISTRATION_ID).remove(PREF_APP_VERSION)
				.remove(PREF_DEVICE_ID).apply();
	}

	public static String readDevId(Context context) {
		return getPrefs(context).getString(PREF_DEVICE_ID, null);
	}

	public static boolean readWaitingForRegistration(Context context) {
		return getPrefs(context).getBoolean(PREF_WAITING_FOR_REGISTRATION,
				false);
	}

	static void saveWaitingForRegistration(Context context, boolean waiting) {
		getPrefs(context).edit()
				.putBoolean(PREF_WAITING_FOR_REGISTRATION, waiting).apply();
	}

	public static boolean readRegistrationFail(Context context) {
		return getPrefs(context).getBoolean(PREF_REGISTRATION_FAIL, false);
	}

	static void saveRegistrationFail(Context context, boolean fail) {
		getPrefs(context).edit().putBoolean(PREF_REGISTRATION_FAIL, fail)
				.apply();
	}

	private static int getAppVersion(Context context) {
		try {
			final PackageManager pm = context.getPackageManager();
			assert pm != null;
			final PackageInfo packageInfo = pm.getPackageInfo(
					context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
	}
}
