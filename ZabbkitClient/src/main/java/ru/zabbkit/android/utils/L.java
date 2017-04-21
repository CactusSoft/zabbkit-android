package ru.zabbkit.android.utils;

import android.util.Log;

import ru.zabbkit.android.app.AppConfig;

/**
 * "Less-word" analog of Android {@link Log logger}
 * 
 * @author Sergey Tarasevich 30.10.2012
 */
public final class L {

	private static final String LOG_FORMAT = "%1$s%n%2$s";
	private static final String WTF = "WTF";

	private static final int VERBOSE = Log.VERBOSE;
	private static final int DEBUG = Log.DEBUG;
	private static final int INFO = Log.INFO;
	private static final int WARN = Log.WARN;
	private static final int ERROR = Log.ERROR;
	private static final int ASSERT = Log.ASSERT;

	private L() {
	}

	public static void v(Throwable e) {
		log(VERBOSE, e, null);
	}

	public static void v(String message, Object... args) {
		log(VERBOSE, null, message, args);
	}

	public static void v(Throwable e, String message, Object... args) {
		log(VERBOSE, e, message, args);
	}

	public static void d(Throwable e) {
		log(DEBUG, e, null);
	}

	public static void d(String message, Object... args) {
		log(DEBUG, null, message, args);
	}

	public static void d(Throwable e, String message, Object... args) {
		log(DEBUG, e, message, args);
	}

	public static void i(Throwable ex) {
		log(INFO, ex, null);
	}

	public static void i(String message, Object... args) {
		log(INFO, null, message, args);
	}

	public static void i(Throwable e, String message, Object... args) {
		log(INFO, e, message, args);
	}

	public static void w(Throwable ex) {
		log(WARN, ex, null);
	}

	public static void w(String message, Object... args) {
		log(WARN, null, message, args);
	}

	public static void w(Throwable e, String message, Object... args) {
		log(WARN, e, message, args);
	}

	public static void e(Throwable e) {
		log(ERROR, e, null);
	}

	public static void e(String message, Object... args) {
		log(ERROR, null, message, args);
	}

	public static void e(Throwable e, String message, Object... args) {
		log(ERROR, e, message, args);
	}

	public static void wtf(Throwable e) {
		log(ASSERT, e, WTF);
	}

	public static void wtf(String message, Object... args) {
		log(ASSERT, null, message, args);
	}

	public static void wtf() {
		log(ASSERT, null, WTF);
	}

	private static void log(int priority, Throwable e, String message,
			Object... args) {
		String fullMessage;
		if (AppConfig.APP_MODE == AppConfig.AppMode.DEVELOPER
				|| priority >= AppConfig.LOG_LEVEL) {
			if (args.length > 0) {
				fullMessage = String.format(message, args);
			} else {
				fullMessage = message;
			}

			String log;
			if (e == null) {
				log = fullMessage;
			} else {
				final String logMessage = fullMessage == null ? e.getMessage()
						: fullMessage;
				final String logBody = Log.getStackTraceString(e);
				log = String.format(LOG_FORMAT, logMessage, logBody);
			}
			Log.println(priority, getTag(), log);
		}
	}

	private static String getTag() {
		final StackTraceElement caller = Thread.currentThread().getStackTrace()[5];
		final String className = caller.getClassName();
		final String simpleClassName = className.substring(
				className.lastIndexOf(".") + 1, className.length());
		return simpleClassName;
	}
}