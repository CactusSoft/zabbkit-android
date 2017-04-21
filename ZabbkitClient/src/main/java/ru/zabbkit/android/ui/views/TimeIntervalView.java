package ru.zabbkit.android.ui.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.zabbkit.android.R;
import ru.zabbkit.android.ui.views.assist.Period;
import ru.zabbkit.android.utils.L;

/**
 * View represents time interval with navigation possibility
 * <p/>
 * Created by Sergey Tarasevich on 26.08.13.
 */
public final class TimeIntervalView extends RelativeLayout implements
		View.OnClickListener, Handler.Callback {

	private static final int MSG_TIME_INTERVAL_CHANGED = 1;
	private static final String INTERVAL_SEPARATOR = " - ";
	private static final long MSG_TIME_INTERVAL = 500;
	private static final long ONE_MINUTE = 60000;

	private final DateFormat dateFormat = new SimpleDateFormat("dd MMM yyy",
			Locale.ENGLISH);
	private final DateFormat timeFormat = SimpleDateFormat
			.getTimeInstance(SimpleDateFormat.SHORT);

	private TextView mDateIntervalView;
	private TextView mTimeIntervalView;

	private Period mPeriod;
	private Calendar mStartTime;
	private Calendar mEndTime;
	private Calendar mPrevStartTime;
	private Calendar mPrevEndTime;

	private Handler mHandler;
	private OnTimeIntervalChangedListener mListener = OnTimeIntervalChangedListener.EMPTY;

	public TimeIntervalView(Context context) {
		super(context);
		init(context);
	}

	public TimeIntervalView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TimeIntervalView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.view_time_interval, this,
				true);

		mHandler = new Handler(this);

		mDateIntervalView = (TextView) findViewById(R.id.date_interval);
		mTimeIntervalView = (TextView) findViewById(R.id.time_interval);
		View goBackButton = findViewById(R.id.arrow_left);
		View goForwardButton = findViewById(R.id.arrow_right);

		goBackButton.setOnClickListener(this);
		goForwardButton.setOnClickListener(this);

		mPeriod = Period._1H;
		mEndTime = Calendar.getInstance();
		mStartTime = mPeriod.getDateBefore(Calendar.getInstance());
		mPrevStartTime = (Calendar) mStartTime.clone();
		mPrevEndTime = (Calendar) mEndTime.clone();
		refreshTimeLabels();
	}

	@Override
	public void onClick(View v) {
		final int timeAmount;
		switch (v.getId()) {
		case R.id.arrow_left:
			timeAmount = -mPeriod.getTimeAmount();
			break;
		case R.id.arrow_right:
			timeAmount = mPeriod.getTimeAmount();
			break;
		default:
			L.wtf("Unknown view was clicked");
			return;
		}
		final int calendarField = mPeriod.getCalendarField();
		mStartTime.add(calendarField, timeAmount);
		mEndTime.add(calendarField, timeAmount);

		if (mEndTime.after(Calendar.getInstance())) {
			if (System.currentTimeMillis() - mPrevEndTime.getTimeInMillis() > ONE_MINUTE) {
				mEndTime = Calendar.getInstance();
				mStartTime = (Calendar) mEndTime.clone();
				mStartTime.add(calendarField, -timeAmount);
			} else {
				// roll back changes
				mStartTime.add(calendarField, -timeAmount);
				mEndTime.add(calendarField, -timeAmount);
			}
		}
		refreshTimeLabels();
		mHandler.removeMessages(MSG_TIME_INTERVAL_CHANGED);
		mHandler.sendEmptyMessageDelayed(MSG_TIME_INTERVAL_CHANGED,
				MSG_TIME_INTERVAL);
	}

	/**
	 * Sets time period. Not fires {@link OnTimeIntervalChangedListener}
	 * callback.
	 */
	public void setPeriod(Period newPeriod) {
		if (!newPeriod.equals(mPeriod)) {
			mPeriod = newPeriod;
			mStartTime = mPeriod.getDateBefore((Calendar) mEndTime.clone());
			mPrevStartTime = (Calendar) mStartTime.clone();
			refreshTimeLabels();
		}
	}

	/** Sets time period. Fires {@link OnTimeIntervalChangedListener} callback. */
	public void setPeriodAndUpdateInterval(Period newPeriod) {
		if (newPeriod != mPeriod) {
			setPeriod(newPeriod);
			mListener.onTimeIntervalChanged(mPeriod, mStartTime, mEndTime);
		}
	}

	private void refreshTimeLabels() {
		mTimeIntervalView.setVisibility(isShowTimeInterval() ? VISIBLE : GONE);
		final Date startDate = mStartTime.getTime();
		final Date endDate = mEndTime.getTime();

		final String startTimeString = timeFormat.format(startDate);
		final String endTimeString = timeFormat.format(endDate);
		final StringBuilder timeIntervalBuilder = new StringBuilder(
				startTimeString).append(INTERVAL_SEPARATOR).append(
				endTimeString);
		mTimeIntervalView.setText(timeIntervalBuilder.toString());

		final String startDateString = dateFormat.format(startDate);
		final String endDateString = dateFormat.format(endDate);
		final StringBuilder dateIntervalBuilder = new StringBuilder(
				startDateString);
		if (!startDateString.equals(endDateString)) {
			dateIntervalBuilder.append(INTERVAL_SEPARATOR)
					.append(endDateString);
		}
		mDateIntervalView.setText(dateIntervalBuilder.toString());
	}

	public void setOnTimeIntervalChangedListener(
			OnTimeIntervalChangedListener listener) {
		mListener = listener;
	}

	public void removeOnIntervalChangedListener(
			OnTimeIntervalChangedListener listener) {
		if (mListener.equals(listener)) {
			mListener = OnTimeIntervalChangedListener.EMPTY;
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		if (!mStartTime.equals(mPrevStartTime)
				&& !mEndTime.equals(mPrevEndTime)) {
			mListener.onTimeIntervalChanged(mPeriod, mStartTime, mEndTime);
			mPrevStartTime = (Calendar) mStartTime.clone();
			mPrevEndTime = (Calendar) mEndTime.clone();
		}
		return true;
	}

	public Period getPeriod() {
		return mPeriod;
	}

	public Calendar getStartTime() {
		return mStartTime;
	}

	public Calendar getEndTime() {
		return mEndTime;
	}

	/**
	 * Whether show time interval (e.g. "10:00 PM - 11:00 PM") for current
	 * {@linkplain Period period}
	 */
	protected boolean isShowTimeInterval() {
		boolean showTimeInterval;
		switch (mPeriod) {
		case _1H:
		case _2H:
		case _3H:
		case _6H:
		case _12H:
			showTimeInterval = true;
			break;
		case _1D:
		case _1W:
		case _2W:
		case _1M:
		case _2M:
		case _3M:
		case _6M:
		case _1Y:
			showTimeInterval = false;
			break;
		default:
			throw new IllegalArgumentException("Unknown time period");
		}
		return showTimeInterval;
	}

	public interface OnTimeIntervalChangedListener {

		OnTimeIntervalChangedListener EMPTY = new OnTimeIntervalChangedListener() {
			@Override
			public void onTimeIntervalChanged(Period period,
					Calendar startTime, Calendar endTime) { // Do nothing
			}
		};

		void onTimeIntervalChanged(Period period, Calendar startTime,
				Calendar endTime);
	}
}
