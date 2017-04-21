package ru.zabbkit.android.ui.views.assist;

import java.util.Calendar;

import ru.zabbkit.android.R;

/**
 * Represents time period
 * <p/>
 * Created by Sergey.Tarasevich on 26.08.13.
 */
public enum Period {
	_1H(R.string.period_1h, Calendar.HOUR, 1),
	_2H(R.string.period_2h, Calendar.HOUR, 2),
	_3H(R.string.period_3h, Calendar.HOUR, 3),
	_6H(R.string.period_6h, Calendar.HOUR, 6),
	_12H(R.string.period_12h, Calendar.HOUR, 12),
	_1D(R.string.period_1d, Calendar.DAY_OF_MONTH, 1),
	_1W(R.string.period_1w, Calendar.DAY_OF_MONTH, 7),
	_2W(R.string.period_2w, Calendar.DAY_OF_MONTH, 14),
	_1M(R.string.period_1m, Calendar.MONTH, 1),
	_2M(R.string.period_2m, Calendar.MONTH, 2),
	_3M(R.string.period_3m, Calendar.MONTH, 3),
	_6M(R.string.period_6m, Calendar.MONTH, 6),
	_1Y(R.string.period_1y, Calendar.YEAR, 1);

	private final int nameRes;
	private final int calendarField;
	private final int timeAmount;

	Period(int nameRes, int calendarField, int timeAmount) {
		this.nameRes = nameRes;
		this.calendarField = calendarField;
		this.timeAmount = timeAmount;
	}

	public int getNameRes() {
		return nameRes;
	}

	public Calendar getDateBefore(Calendar date) {
		date.add(calendarField, -timeAmount);
		return date;
	}

	public int getCalendarField() {
		return calendarField;
	}

	public int getTimeAmount() {
		return timeAmount;
	}
}
