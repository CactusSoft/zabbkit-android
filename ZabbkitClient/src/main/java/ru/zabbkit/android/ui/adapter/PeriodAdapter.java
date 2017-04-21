package ru.zabbkit.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Arrays;

import ru.zabbkit.android.R;
import ru.zabbkit.android.ui.views.assist.Period;

/**
 * Created by Sergey.Tarasevich on 27.08.13.
 */
public class PeriodAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private Period[] periods;

	public PeriodAdapter(Context context, Period[] periods) {
		inflater = LayoutInflater.from(context);
		this.periods = Arrays.copyOf(periods, periods.length);
	}

	@Override
	public int getCount() {
		return periods.length;
	}

	@Override
	public Object getItem(int position) {
		return periods[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView periodTextView = (TextView) convertView;
        if(periodTextView == null){
            periodTextView = (TextView) inflater.inflate(R.layout.item_period, parent, false);
        }

		Period period = periods[position];
		periodTextView.setText(period.getNameRes());
		periodTextView.setTag(period);
		return periodTextView;
	}
}
