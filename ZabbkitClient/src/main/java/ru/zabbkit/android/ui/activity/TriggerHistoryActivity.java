package ru.zabbkit.android.ui.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.ui.adapter.TriggerHistoryAdapter;
import ru.zabbkit.android.ui.dialog.DialogHelper;
import ru.zabbkit.android.ui.views.DraggedPanelLayout;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.GeneralAbility;
import ru.zabbkit.android.utils.SharedPreferencesEditor;
import ru.zabbkitserver.android.remote.model.Event;
import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

public class TriggerHistoryActivity extends LoadingDialogActivity implements
        AsyncRequestListener, DialogHelper.AddCertificateListener {

    private static final String INFO_PANEL_STATE_TAG = "InfoPanelOpened";
    private String mDateTimeUpdate;
    private boolean mIsOnRefresh;
    private String mTriggerName;
    private String mTriggerUrl;
    private String mTriggerComments;

    private ListView listView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ac_trigger_history);

        FlurryAgent.logEvent("Show Trigger Events");

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.trigger_history);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        requestTriggerHistory();
        showDialog();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.grey);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestTriggerHistory();
                mIsOnRefresh = true;
            }
        });

        listView = (ListView) findViewById(R.id.pull_refresh_list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, Constants.FLURRY_APP_KEY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    private void requestTriggerHistory() {
        Communicator.getInstance().getTriggerHistory(prepareParams(), this);
    }

    private Map<String, Object> prepareParams() {
        final Map<String, Object> params = new ArrayMap<String, Object>();
        final Bundle data = getIntent().getExtras();
        assert data != null;
        mTriggerName = data.getString(Constants.INT_TRIGGER_NAME);
        mTriggerUrl = data.getString(Constants.URL_PARAM);
        mTriggerComments = data.getString(Constants.COMMENTS_PARM);

        params.put(Constants.REQ_EXPAND_DESCRIPTION, true);
        params.put(Constants.REQ_SORT_FIELD, Constants.REQ_EVENT_ID);
        params.put(Constants.REQ_SORT_ORDER, Constants.REQ_DESC);
        params.put(Constants.REQ_VALUE, Constants.REQ_VAL_VALUE);
        params.put(Constants.REQ_TRIGGER_IDS,
                data.getString(Constants.REQ_TRIGGER_IDS));
        params.put(Constants.REQ_OUTPUT, Constants.REQ_VAL_EXTEND);
        params.put(Constants.REQ_SELECT_TRIGGERS, Constants.REQ_VAL_EXTEND);
        params.put(Constants.REQ_SELECT_HOSTS, Constants.REQ_VAL_EXTEND);

        return params;
    }

    @Override
    public void onRequestFailure(Exception e,
                                 String message) {
        dismissDialog();
        final String msg = message;
        Toast.makeText(TriggerHistoryActivity.this, msg,
                Toast.LENGTH_SHORT).show();
        if (mIsOnRefresh) {
            mSwipeRefreshLayout.setRefreshing(false);
            mIsOnRefresh = false;
        }
    }

    @Override
    public void onRequestSuccess(List<Object> result,
                                 Class<?> clazz) {
        FlurryAgent.logEvent("Refresh Trigger Events");

        int threatDegree = 0;
        final List<Event> events = new ArrayList<Event>();
        for (Object aResult : result) {
            final Event event = (Event) aResult;
            events.add(event);
            final int eventPriority = Integer
                    .valueOf((event.triggers != null && !event.triggers.isEmpty()) ? event.triggers.get(0).priority : "0");
            if (eventPriority > threatDegree) {
                threatDegree = eventPriority;
            }
        }

        final TextView threatView = (TextView) findViewById(R.id.threat_view);
        final TextView titleView = (TextView) findViewById(R.id.title_view);
        final TextView urlView = (TextView) findViewById(R.id.link_addr);
        final TextView commentView = (TextView) findViewById(R.id.comment);

        threatView.setText(getString(GeneralAbility.getState(threatDegree)));
        titleView.setText(mTriggerName);
        urlView.setText(mTriggerUrl);
        commentView.setText(mTriggerComments);

        listView.setAdapter(new TriggerHistoryAdapter(events));
        dismissDialog();
        mDateTimeUpdate = DateFormat.getDateTimeInstance().format(new Date());
        SharedPreferencesEditor.getInstance().putString(
                Constants.PREFS_UPDATE_DATE_TRIGGERS, mDateTimeUpdate);

        if (mIsOnRefresh) {
            mSwipeRefreshLayout.setRefreshing(false);
            mIsOnRefresh = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {

            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    public void onCertificateRequest(final X509Certificate[] certificate) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                if (certificate == null) {
                    requestTriggerHistory();
                } else {
                    DialogHelper.showSslDialog(TriggerHistoryActivity.this,
                            certificate, TriggerHistoryActivity.this);
                }
            }
        });
    }

    @Override
    public void resumeRequest(boolean answer) {
        if (answer) {
            requestTriggerHistory();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        DraggedPanelLayout rootView = (DraggedPanelLayout) findViewById(R.id.root);
        savedInstanceState
                .putBoolean(INFO_PANEL_STATE_TAG, rootView.isOpened());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        DraggedPanelLayout rootView = (DraggedPanelLayout) findViewById(R.id.root);
        rootView.setOpened(savedInstanceState.getBoolean(INFO_PANEL_STATE_TAG));
    }
}
