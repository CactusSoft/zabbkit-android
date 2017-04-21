package ru.zabbkit.android.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.Toast;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.ui.activity.GraphActivity;
import ru.zabbkit.android.ui.adapter.DataAdapter;
import ru.zabbkit.android.ui.views.viewholder.OnDataItemClickListener;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.NetworkUtils;
import ru.zabbkit.android.utils.SharedPreferencesEditor;
import ru.zabbkitserver.android.remote.model.ZabbixItem;

/**
 * @author Sergey Tarasevich on 20.02.2013.
 */
public class DataFragment extends BaseListFragment implements OnDataItemClickListener {

    protected CharSequence mDateTimeUpdate;

    private Handler mHandler = new Handler();

    @Override
    public void onRequestSuccess(List<Object> result, Class<?> clazz) {
        if (isResumed()) {
            final List<ZabbixItem> items = new ArrayList<>();
            for (Object itemObj : result) {
                ZabbixItem item = (ZabbixItem) itemObj;
                if (item.getStatus() == ZabbixItem.STATUS_ENABLED) {
                    items.add(item);
                }
            }
            if (mIsDataObsolete) {
                mIsDataObsolete = false;
            }
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    setAdapterData(items);
                    mDateTimeUpdate = DateFormat.format(Constants.DATE_FORMAT,
                            System.currentTimeMillis());
                    dismissDialog();
                    completeListResfresh();
                }
            });
        }
    }

    @Override
    public void onRequestFailure(Exception e, final String message) {
        if (isResumed()) {
            if (mIsDataObsolete) {
                mIsDataObsolete = false;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    completeListResfresh();
                    dismissDialog();
                    showToast(message);
                }
            });
        }
    }

    @Override
    public void onCertificateRequest(X509Certificate[] certificate) {
    }

    @Override
    public void setObsoleteDataFlag() {
        mIsDataObsolete = true;
    }

    @Override
    protected void sendRequest() {
        if (NetworkUtils.isNetEnabled(getActivity())) {
            Communicator.getInstance().getItems(prepareParams(), this);
        } else {
            completeListResfresh();
            dismissDialog();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        mAdapter = new DataAdapter(this);
        return mAdapter;
    }

    @Override
    public void onGraphClick(String tag, String hostName, String hostId, String parsedName) {
        Intent intent = new Intent(getActivity(),
                GraphActivity.class);
        if (tag != null) {
            intent.putExtra(Constants.GRAPH_ID, tag);
        }
        intent.putExtra(Constants.HOST_ID, hostId);
        intent.putExtra(Constants.HOST_NAME, hostName);
        intent.putExtra(Constants.PARAM_NAME, parsedName);
        getActivity().startActivity(intent);
    }

    private Map<String, Object> prepareParams() {
        final Map<String, Object> params = new ArrayMap<String, Object>();
        String hostId = SharedPreferencesEditor.getInstance().getString(
                Constants.PREFS_HOST_ID);
        if (TextUtils.isEmpty(hostId)) {
            hostId = SharedPreferencesEditor.getInstance().getString(
                    Constants.PREFS_HOSTGROUP_ID);
            if (!TextUtils.isEmpty(hostId)) {
                params.put(Constants.REQ_GROUP_IDS, hostId);
            }
        } else {
            params.put(Constants.REQ_HOST_IDS, hostId);
        }
        params.put(Constants.REQ_OUTPUT, Constants.REQ_VAL_DATA);
        params.put(Constants.REQ_SORT_FIELD, "name");
        params.put(Constants.REQ_SELECT_HOSTS, Constants.REQ_VAL_EXTEND);
        params.put(Constants.REQ_SELECT_GRAPHS, Constants.REQ_VAL_EXTEND);

        return params;
    }

    private void completeListResfresh() {
        if (mIsOnRefresh && mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mIsOnRefresh = false;
        }
    }

    private void showToast(String message) {
        Activity context = getActivity();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

}
