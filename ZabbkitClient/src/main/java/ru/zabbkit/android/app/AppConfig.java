package ru.zabbkit.android.app;

import android.util.Log;

/**
 * Created by Sergey.Tarasevich on 28.08.13.
 */
public final class AppConfig {

	public static final boolean DEVELOPER_MODE = true;
	public static final AppMode APP_MODE = AppMode.DEVELOPER;
	public static final int LOG_LEVEL = Log.VERBOSE;

	private AppConfig() {
	}

	public static enum AppMode {
		DEVELOPER, TESTER, PRODUCTION
	}
}
