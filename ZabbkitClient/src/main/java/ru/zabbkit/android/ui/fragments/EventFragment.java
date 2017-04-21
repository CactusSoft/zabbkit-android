package ru.zabbkit.android.ui.fragments;

import android.app.Activity;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.ui.adapter.EventAdapter;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.L;
import ru.zabbkit.android.utils.NetworkUtils;
import ru.zabbkit.android.utils.SharedPreferencesEditor;
import ru.zabbkitserver.android.remote.model.Event;

public class EventFragment extends BaseListFragment {

    @Override
    protected void sendRequest() {
        if (NetworkUtils.isNetEnabled(getActivity())) {
            Communicator.getInstance().getEvent(prepareParams(), this);
        } else {
            completeListRefresh();
            dismissDialog();
            mSwipeRefreshLayout.setRefreshing(false);
        }

    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        mAdapter = new EventAdapter();
        return mAdapter;
    }

    private Map<String, Object> prepareParams() {
        final Map<String, Object> params = new ArrayMap<String, Object>();
        String hostId = SharedPreferencesEditor.getInstance().getString(
                Constants.PREFS_HOST_ID);
        if ("".equals(hostId)) {
            hostId = SharedPreferencesEditor.getInstance().getString(
                    Constants.PREFS_HOSTGROUP_ID);
            if (!"".equals(hostId)) {
                params.put(Constants.REQ_GROUP_IDS, hostId);
            }
        } else {
            params.put(Constants.REQ_HOST_IDS, hostId);
        }
        // parameters for value transmit by default
        params.put(Constants.REQ_SOURCE, Constants.REQ_VAL_SOURCE);
        params.put(Constants.REQ_VALUE, Constants.REQ_VAL_VALUE);
        params.put(Constants.REQ_OUTPUT, Constants.REQ_VAL_EXTEND);
        params.put(Constants.REQ_SELECT_TRIGGERS, Constants.REQ_VAL_EXTEND);
        params.put(Constants.REQ_SELECT_HOSTS, Constants.REQ_VAL_EXTEND);
        params.put(Constants.REQ_SORT_FIELD, Constants.REQ_EVENT_ID);
        params.put(Constants.REQ_SORT_ORDER, Constants.REQ_DESC);
        params.put(Constants.REQ_EVENTS_LIMIT, Constants.REQ_VAL_EVENTS_LIMIT);

        final long timestamp = System.currentTimeMillis()
                - Constants.REQ_VAL_EVENT_LOAD_PERIOD;
        params.put(Constants.REQ_TIME_FROM, timestamp / Constants.MS_IN_SEC);
        L.i("Request Params: " + params.toString());
        return params;
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void completeListRefresh() {
        if (mIsOnRefresh) {
            mSwipeRefreshLayout.setRefreshing(false);
            mIsOnRefresh = false;
        }
    }

    @Override
    public void onRequestFailure(Exception e, final String message) {
        if (isResumed()) {
            if (mIsDataObsolete) {
                mIsDataObsolete = false;
            }
            dismissDialog();
            completeListRefresh();
            showToast(message);
        }
    }

    @Override
    public void onRequestSuccess(List<Object> result, Class<?> clazz) {
        if (isResumed()) {
            if (clazz == Event.class) {
                FlurryAgent.logEvent("Refresh events");
                List<Event> events = new ArrayList<Event>();
                for (Object aResult : result) {
                    final Event event = (Event) aResult;
                    events.add(event);
                }
                if (mIsDataObsolete) {
                    mIsDataObsolete = false;
                }
                dismissDialog();
                setAdapterData(events);
                //mAdapter.setEventList(events);
            }
            completeListRefresh();
        }
    }

    @Override
    public void onCertificateRequest(X509Certificate[] certificate) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setObsoleteDataFlag() {
        mIsDataObsolete = true;
    }
}
