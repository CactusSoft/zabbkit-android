package ru.zabbkit.android.ui.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicBoolean;

import ru.zabbkit.android.R;
import ru.zabbkit.android.ui.adapter.PeriodAdapter;
import ru.zabbkit.android.ui.views.assist.Period;
import ru.zabbkit.android.utils.L;

/**
 * Represents ruler with time periods
 * <p/>
 * Created by Sergey.Tarasevich on 27.08.13.
 */
@SuppressWarnings("deprecation")
public class TimePeriodRuler extends Gallery implements
		AdapterView.OnItemSelectedListener, View.OnTouchListener,
		Handler.Callback {

	private static final int MSG_PERIOD_CHANGED = 1;
	private static final long MSG_TIME_INTERVAL = 500;

	private TextView mPrevActiveView;

	private OnPeriodChangedListener mOnPeriodChangedListener = OnPeriodChangedListener.EMPTY;
	private Handler mHandler;

	private Period mCurPeriod;
	private AtomicBoolean mIsScrolling = new AtomicBoolean();

	public TimePeriodRuler(Context context) {
		super(context);
		init(context);
	}

	public TimePeriodRuler(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TimePeriodRuler(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mHandler = new Handler(this);
		setAdapter(new PeriodAdapter(context, Period.values()));
		setOnItemSelectedListener(this);
		setOnTouchListener(this);
		mCurPeriod = (Period) getSelectedItem();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		L.d("Period Gallery: Item %d selected", position);

		if (view instanceof TextView) {
			TextView curPeriodView = (TextView) view;
			curPeriodView.setTextColor(getResources().getColor(
					R.color.txt_period_active));
			if (mPrevActiveView != null) {
				mPrevActiveView.setTextColor(getResources().getColor(
						R.color.txt_period_inactive));
			}
			mPrevActiveView = curPeriodView;

			if (!mIsScrolling.get()) {
				mHandler.removeMessages(MSG_PERIOD_CHANGED);
				mHandler.sendEmptyMessageDelayed(MSG_PERIOD_CHANGED,
						MSG_TIME_INTERVAL);
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) { // Do nothing
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			L.d("Period Gallery: Start scrolling...");
			mIsScrolling.set(true);
			mHandler.removeMessages(MSG_PERIOD_CHANGED);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			L.d("Period Gallery: ... Stop scrolling");
			mIsScrolling.set(false);
			mHandler.removeMessages(MSG_PERIOD_CHANGED);
			mHandler.sendEmptyMessageDelayed(MSG_PERIOD_CHANGED,
					MSG_TIME_INTERVAL);
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean handleMessage(Message msg) {
		Period period = (Period) getSelectedItem();
		if (period != mCurPeriod) {
			mCurPeriod = period;
			mOnPeriodChangedListener.onPeriodChanged(period);
			L.i("Selected period = "
					+ getResources().getString(period.getNameRes()));
		}
		return true;
	}

	public void setOnPeriodChangedListener(OnPeriodChangedListener listener) {
		mOnPeriodChangedListener = listener;
	}

	public void removeOnPeriodChangedListener(OnPeriodChangedListener listener) {
		if (mOnPeriodChangedListener == listener) {
			mOnPeriodChangedListener = OnPeriodChangedListener.EMPTY;
		}
	}

	public interface OnPeriodChangedListener {

		OnPeriodChangedListener EMPTY = new OnPeriodChangedListener() {
			@Override
			public void onPeriodChanged(Period period) { // Do nothing
			}
		};

		void onPeriodChanged(Period period);
	}
}
