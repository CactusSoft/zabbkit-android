package ru.zabbkit.android.utils;


/*
 * class for conversion long date to days/hours/minutes/seconds string
 */
public final class TimeParser {

	// such divisors array because: 1 min = 60 sec, 1 hour = 60 min, 1 day = 24
	// hours
	private static final int[] DIVISORS = { 60, 60, 24 };
	private static final String[] UNITS = { "s ", "m ", "h ", "d " };

	private TimeParser() {
	}

	public static String parseTime(long timeSeconds) {
		long leftTimeSeconds = timeSeconds;
		long intPart;
		long fractPart;
		int i = 0;
		do {
			intPart = leftTimeSeconds / DIVISORS[i];
			fractPart = leftTimeSeconds % DIVISORS[i];
			leftTimeSeconds = intPart;
			i++;
		} while (i < DIVISORS.length && intPart > DIVISORS[i]);
		final StringBuilder result = new StringBuilder();
		if (intPart != 0) {
			result.append(intPart).append(UNITS[i]);
		}
		if (fractPart != 0) {
			result.append(fractPart).append(UNITS[i - 1]);
		}
		return result.toString();
	}
}
