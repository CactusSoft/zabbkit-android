package ru.zabbkit.android.ui.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.GeneralAbility;
import ru.zabbkit.android.utils.L;
import ru.zabbkit.android.utils.TimeParser;
import ru.zabbkitserver.android.remote.model.Event;
import ru.zabbkitserver.android.remote.model.Trigger;
import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

public class OldEventAdapter extends BaseAdapter implements AsyncRequestListener {

    private final List<Event> mEventList;
    private final List<EventDate> mEventDateList;

    private final ArrayMap<String, Trigger> mCheckedTriggers;
    private final List<String> mTriggersInProgress;

    public OldEventAdapter() {
        mEventList = Collections.synchronizedList(new ArrayList<Event>());
        mEventDateList = Collections.synchronizedList(new ArrayList<EventDate>());

        mCheckedTriggers = new ArrayMap<String, Trigger>();
        mTriggersInProgress = Collections.synchronizedList(new ArrayList<String>());
    }

    @Override
    public int getCount() {
        return mEventList.size();
    }

    @Override
    public Object getItem(int position) {
        return mEventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EventViewHolder viewHolder;

        View row = convertView;
        if (row == null) {
            Context context = parent.getContext();
            assert context != null;
            row = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
            assert row != null;
            viewHolder = new EventViewHolder();
            viewHolder.descriptionView = (TextView) row.findViewById(R.id.event_view);
            viewHolder.dateView = (TextView) row.findViewById(R.id.date_view);
            viewHolder.durationView = (TextView) row.findViewById(R.id.duration_view);
            viewHolder.stateStroke = row.findViewById(R.id.color_stroke);

            row.setTag(viewHolder);
        } else {
            viewHolder = (EventViewHolder) row.getTag();
        }

        final Event event = mEventList.get(position);
        if (event != null) {
            if(event.triggers != null && event.triggers.size() > 0) {
                String eventDescription = defineTriggerDescription(event);
                viewHolder.descriptionView.setText(eventDescription);
            } else {
                viewHolder.descriptionView.setText("");
            }

            if (mEventDateList.get(position) != null) {
                viewHolder.dateView.setText(mEventDateList.get(position).date);
                viewHolder.durationView.setText(TimeParser.parseTime(mEventDateList.get(position).duration));
            } else {
                viewHolder.dateView.setText("");
                viewHolder.durationView.setText("");
            }

            if (Constants.STATE_OK.equals(event.getValue())) {
                viewHolder.stateStroke.setBackgroundResource(R.color.ok_color);
            } else {
                if(event.triggers != null && event.triggers.size() > 0) {
                    int triggerBgColor = GeneralAbility
                            .getTriggersResourceColor(Integer.valueOf(event.triggers.get(0).priority));
                    viewHolder.stateStroke.setBackgroundResource(triggerBgColor);
                } else {
                    viewHolder.stateStroke.setBackgroundResource(R.color.not_classified_color);
                }
            }
        }
        return row;
    }

    private String defineTriggerDescription(Event event) {
        Event.Triggers trigger = event.triggers.get(0);
        String triggerId = trigger.triggerid;
        boolean isTriggerEvent = Integer.valueOf(event.getObject()) == Event.OBJECT_TRIGGER;
        boolean shouldCalculateTriggerDescr = trigger.description.matches(".*\\{.*\\}.*");
        if (isTriggerEvent && shouldCalculateTriggerDescr) {
            Trigger checkedTrigger = mCheckedTriggers.get(triggerId);
            if (checkedTrigger == null) { // is this trigger not checked from server yet
                if (!mTriggersInProgress.contains(triggerId)) { // is request for this trigger not executing
                    Communicator.getInstance().getTriggers(prepareCalculateEventRequest(triggerId), this);
                    mTriggersInProgress.add(triggerId);
                }
            } else {
                trigger.description = checkedTrigger.getDescription();
            }
        }
        return trigger.description;
    }

    private Map<String, Object> prepareCalculateEventRequest(String triggerId) {
        Map<String, Object> params = new ArrayMap<String, Object>();
        params.put(Constants.REQ_TRIGGER_IDS, triggerId);
        params.put(Constants.REQ_OUTPUT, Constants.REQ_VAL_EXTEND);
        params.put(Constants.REQ_EXPAND_DESCRIPTION, true);
        params.put(Constants.REQ_SELECT_HOSTS, Constants.REQ_VAL_EXTEND);
        return params;
    }

    @Override
    public void onRequestFailure(Exception e, String message) {
        L.e("Failed to receive trigger from server");
    }

    @Override
    public void onRequestSuccess(List<Object> result, Class<?> clazz) {
        if (clazz == Trigger.class) {
            for (Object triggerObj : result) {
                Trigger trigger = (Trigger) triggerObj;
                mTriggersInProgress.remove(trigger.getTriggerid());
                mCheckedTriggers.put(trigger.getTriggerid(), trigger);
            }
        }
        notifyDataSetChanged();
    }

    public synchronized void setEventList(List<Event> events) {
        mEventList.clear();
        mEventDateList.clear();

        mEventList.addAll(events);
        mEventDateList.addAll(fillDates());

        notifyDataSetChanged();
    }

    private ArrayList<EventDate> fillDates() {
        ArrayList<EventDate> eventDates = new ArrayList<EventDate>(mEventList.size());
        Map<String, Long> previousDurations = new ArrayMap<String, Long>();
        long previousDate = System.currentTimeMillis() / Constants.MS_IN_SEC;
        for (Event event : mEventList) {
            if (event != null && event.triggers != null && event.triggers.size() > 0) {
                long timestamp = Long.valueOf(event.getClock());
                CharSequence date = DateFormat.format(Constants.DATE_FORMAT, timestamp * Constants.MS_IN_SEC);
                String triggerId = event.triggers.get(0).triggerid;

                long duration;
                if (previousDurations.containsKey(triggerId)) {
                    duration = previousDate - timestamp - previousDurations.get(triggerId);
                    previousDurations.put(triggerId, duration + previousDurations.get(triggerId));
                } else {
                    duration = previousDate - timestamp;
                    previousDurations.put(triggerId, duration);
                }
                eventDates.add(new EventDate(date, duration));
            } else {
                eventDates.add(null);
            }
        }
        return eventDates;
    }

    @Override
    public void onCertificateRequest(X509Certificate[] certificate) {
    }

    private static class EventViewHolder {
        View stateStroke;
        TextView descriptionView;
        TextView dateView;
        TextView durationView;
    }

    private static class EventDate {
        final CharSequence date;
        final long duration;

        public EventDate(CharSequence date, long duration) {
            this.date = date;
            this.duration = duration;
        }
    }
}
