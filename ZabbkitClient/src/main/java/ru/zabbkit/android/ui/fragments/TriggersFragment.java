package ru.zabbkit.android.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.ui.activity.TriggerHistoryActivity;
import ru.zabbkit.android.ui.adapter.TriggerAdapter;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.NetworkUtils;
import ru.zabbkit.android.utils.SharedPreferencesEditor;
import ru.zabbkitserver.android.remote.model.Trigger;

/**
 * @author Alex.Shimborsky on 21.09.2013.
 */
public class TriggersFragment extends BaseListFragment implements
        OnItemClickListener {

    private String mDateTimeUpdate;

    private Handler mHandler = new Handler();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void sendRequest() {
        if (NetworkUtils.isNetEnabled(getActivity())) {
            final Map<String, Object> params = new ArrayMap<String, Object>();

            params.put(Constants.REQ_SELECT_HOSTS, new String[]{
                    Constants.REQ_VAL_HOST_ID, Constants.REQ_VAL_HOST});
            params.put(Constants.REQ_SORT_FIELD, Constants.REQ_VAL_DESCRIPTION);
            params.put(Constants.REQ_OUTPUT, Constants.REQ_VAL_EXTEND);
            params.put(Constants.REQ_MONITORED, true);
            params.put(Constants.REQ_EXPAND_DESCRIPTION, true);

            String hostId;

            final Bundle data = getActivity().getIntent().getExtras();
            if (data != null) {
                final boolean isHostGroup = data
                        .getBoolean(Constants.INT_IS_HOST_GROUP);
                hostId = data.getString(Constants.REQ_HOST_IDS);
                if (isHostGroup) {
                    params.put(Constants.REQ_GROUP_IDS, hostId);
                } else {
                    params.put(Constants.REQ_HOST_IDS, hostId);
                }
            } else {
                hostId = SharedPreferencesEditor.getInstance().getString(
                        Constants.PREFS_HOST_ID);
                if (!"".equals(hostId)) {
                    params.put(Constants.REQ_HOST_IDS, hostId);
                } else {
                    hostId = SharedPreferencesEditor.getInstance().getString(
                            Constants.PREFS_HOSTGROUP_ID);
                    if (!"".equals(hostId)) {
                        params.put(Constants.REQ_GROUP_IDS, hostId);
                    }
                }
            }
            Communicator.getInstance().getTriggers(params, this);
        } else {
            completeListResfresh();
            dismissDialog();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        mAdapter = new TriggerAdapter();
        return mAdapter;
    }

    private void completeListResfresh() {
        if (mIsOnRefresh) {
            mSwipeRefreshLayout.setRefreshing(false);
            mIsOnRefresh = false;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestFailure(Exception e, final String message) {
        if (isResumed()) {
            dismissDialog();
            if (mIsDataObsolete) {
                mIsDataObsolete = false;
            }
            if (getActivity() != null) {
                completeListResfresh();
                showToast(message);
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onRequestSuccess(List<Object> result, Class<?> clazz) {
        if (isResumed()) {
            final List<Trigger> triggerList;
            dismissDialog();
            if (clazz == Trigger.class) {
                triggerList = new ArrayList<>();
                for (Object aResult : result) {
                    triggerList.add((Trigger) aResult);
                }

                if (mIsDataObsolete) {
                    mIsDataObsolete = false;
                }
                mDateTimeUpdate = DateFormat.getDateTimeInstance().format(
                        new Date());
                SharedPreferencesEditor.getInstance().putString(
                        Constants.PREFS_UPDATE_DATE_OVERVIEW, mDateTimeUpdate);

                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        //mAdapter.setTriggerList(triggerList);
                        setAdapterData(triggerList);
                        completeListResfresh();
                    }
                });
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (NetworkUtils.isNetEnabled(getActivity())) {
            final Trigger trigger = (Trigger) parent
                    .getItemAtPosition(position);
            final Intent intent = new Intent(getActivity(),
                    TriggerHistoryActivity.class);
            assert trigger != null;
            intent.putExtra(Constants.REQ_TRIGGER_IDS, trigger.getTriggerid());
            intent.putExtra(Constants.INT_TRIGGER_NAME,
                    trigger.getDescription());
            intent.putExtra(Constants.URL_PARAM, trigger.getUrl());
            intent.putExtra(Constants.COMMENTS_PARM, trigger.getComments());
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.slide_in,
                    R.anim.slide_out);
        }
    }

    @Override
    public void onCertificateRequest(X509Certificate[] certificate) {

    }

    @Override
    public void setObsoleteDataFlag() {
        mIsDataObsolete = true;
    }

}
