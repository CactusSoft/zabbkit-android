package ru.zabbkit.android.utils;

import java.util.Locale;

/**
 * 
 * This class provides methods for convertation values to human readable format
 * 
 * @author Dmitry.Kalenchuk
 * 
 */
public final class ValueConvertUtils {

	public enum ValuesMultiplayers {

		TERA(1e12, "T"), GIGA(1e9, "G"), MEGA(1e6, "M"), KILO(1000, "k");

		private final double multyplier;
		private final String prefix;

		ValuesMultiplayers(double multyplier, String prefix) {
			this.multyplier = multyplier;
			this.prefix = prefix;
		}

		public double multiplier() {
			return multyplier;
		}

		public String prefix() {
			return prefix;
		}

	};

	private ValueConvertUtils() {
	}

	/**
	 * Convert value string and unit to human readable string example: 103000000
	 * B to 103 MB
	 * 
	 * @param value
	 * @param unit
	 * @return string in format "<value> <unit> "
	 */
	public static String convertInteger(long value, String unit) {

		String valueString = String
				.format(Locale.ENGLISH, "%d %s", value, unit);

		final ValuesMultiplayers[] values = ValuesMultiplayers.values();

		for (int i = 0; i < values.length; i++) {
			final long newValue = (long) (value / values[i].multiplier());
			if (newValue > 0) {
				valueString = String.format(Locale.ENGLISH, "%d %s", newValue,
						values[i].prefix() + unit);
				break;
			}
		}
		return valueString;
	}

	/**
	 * Convert value string and unit to human readable string example:
	 * 103800000.00 B to 103.8 MB
	 * 
	 * @param value
	 * @param unit
	 * @return string in format "<value> <unit> "
	 */
	public static String convertFloat(double value, String unit) {

		String valueString = String.format(Locale.ENGLISH, "%.3f %s", value,
				unit);

		final ValuesMultiplayers[] values = ValuesMultiplayers.values();

		for (int i = 0; i < values.length; i++) {
			final double newValue = value / values[i].multiplier();
			if ((long) newValue > 0) {
				valueString = String.format(Locale.ENGLISH, "%.3f %s",
						newValue, values[i].prefix() + unit);
				break;
			}
		}
		return valueString;
	}

}
