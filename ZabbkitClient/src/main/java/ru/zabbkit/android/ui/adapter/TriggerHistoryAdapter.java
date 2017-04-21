package ru.zabbkit.android.ui.adapter;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.utils.GeneralAbility;
import ru.zabbkit.android.utils.TimeParser;
import ru.zabbkitserver.android.remote.model.Event;

public class TriggerHistoryAdapter extends BaseAdapter {

	private final List<Event> mEventList;
	private final List<TriggerDates> mTriggerDatesList;

	public TriggerHistoryAdapter(List<Event> eventList) {
		mEventList = eventList;
		mTriggerDatesList = fillDates();
	}

	private List<TriggerDates> fillDates() {
		final List<TriggerDates> triggerDatesList = new ArrayList<TriggerHistoryAdapter.TriggerDates>();
		long previousDate = System.currentTimeMillis() / Constants.MS_IN_SEC;

		for (int i = 0; i < mEventList.size(); i++) {
			final Event event = mEventList.get(i);
			if (event != null) {
				final TriggerDates triggerDates = new TriggerDates();
				final String date = DateFormat
						.format(Constants.DATE_FORMAT, Long.valueOf(event.getClock()) * Constants.MS_IN_SEC).toString();
				triggerDates.setDate(date);
				final long timestamp = Long.valueOf(event.getClock());
				final long duration = previousDate - timestamp;
				previousDate = timestamp;
				triggerDates.setDuration(duration);
				triggerDatesList.add(triggerDates);
			}
		}
		return triggerDatesList;
	}

	@Override
	public int getCount() {
		return mEventList.size();
	}

	@Override
	public Event getItem(int position) {
		return mEventList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TriggerViewHolder viewHolder;

		View row = convertView;
		if (row == null) {
			row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trigger_history, parent, false);
			viewHolder = new TriggerViewHolder();
			viewHolder.dateView = (TextView) row.findViewById(R.id.date_view);
			viewHolder.durationView = (TextView) row.findViewById(R.id.duration_view);
			viewHolder.stateStroke = row.findViewById(R.id.color_stroke);
			row.setTag(viewHolder);
		} else {
			viewHolder = (TriggerViewHolder) row.getTag();
		}
		final Event event = mEventList.get(position);
		if (event != null) {
			viewHolder.dateView.setText(mTriggerDatesList.get(position).getDate());
			viewHolder.durationView.setText(TimeParser.parseTime(mTriggerDatesList.get(position).getDuration()));

			if (Constants.STATE_OK.equals(event.getValue())) {
				viewHolder.stateStroke.setBackgroundResource(R.color.ok_color);
			} else {
				final int res = GeneralAbility
						.getTriggersResourceColor(Integer.valueOf(event.triggers.get(0).priority));
				viewHolder.stateStroke.setBackgroundResource(res);
			}
		}
		return row;
	}

	private static class TriggerDates {
		private String date;
		private long duration;

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public long getDuration() {
			return duration;
		}

		public void setDuration(long duration) {
			this.duration = duration;
		}
	}

	static class TriggerViewHolder {
		TextView dateView;
		TextView durationView;
		View stateStroke;
	}
}
