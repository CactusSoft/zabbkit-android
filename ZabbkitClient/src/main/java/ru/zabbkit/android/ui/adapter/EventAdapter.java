package ru.zabbkit.android.ui.adapter;

import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.ui.adapter.base.BaseListAdapter;
import ru.zabbkit.android.ui.views.viewholder.EventViewHolder;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.GeneralAbility;
import ru.zabbkit.android.utils.L;
import ru.zabbkit.android.utils.TimeParser;
import ru.zabbkitserver.android.remote.model.Event;
import ru.zabbkitserver.android.remote.model.Trigger;
import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

/**
 * Created by Alex.Shimborsky on 30.03.2016.
 */
public class EventAdapter extends BaseListAdapter implements AsyncRequestListener {

    private final List<EventDate> mEventDateList = Collections.synchronizedList(new ArrayList<EventDate>());
    private final ArrayMap<String, Trigger> mCheckedTriggers = new ArrayMap<String, Trigger>();
    private final List<String> mTriggersInProgress = Collections.synchronizedList(new ArrayList<String>());

    @Override
    protected int getLayoutId() {
        return R.layout.item_event;
    }

    @Override
    protected RecyclerView.ViewHolder createViewHolder(View view) {
        return new EventViewHolder(view);
    }

    @Override
    public void setData(List data) {
        super.setData(data);
        mEventDateList.clear();
        mEventDateList.addAll(fillDates());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        EventViewHolder viewHolder = (EventViewHolder) holder;
        final Event event = ((List<Event>) mObjectsList).get(position);
        if (event != null) {
            if (event.triggers != null && event.triggers.size() > 0) {
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
                if (event.triggers != null && event.triggers.size() > 0) {
                    int triggerBgColor = GeneralAbility
                            .getTriggersResourceColor(Integer.valueOf(event.triggers.get(0).priority));
                    viewHolder.stateStroke.setBackgroundResource(triggerBgColor);
                } else {
                    viewHolder.stateStroke.setBackgroundResource(R.color.not_classified_color);
                }
            }
        }
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

    private ArrayList<EventDate> fillDates() {
        ArrayList<EventDate> eventDates = new ArrayList<EventDate>(mObjectsList.size());
        Map<String, Long> previousDurations = new ArrayMap<String, Long>();
        long previousDate = System.currentTimeMillis() / Constants.MS_IN_SEC;
        for (Event event : (List<Event>) mObjectsList) {
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

    @Override
    public void onCertificateRequest(X509Certificate[] certificate) {
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
