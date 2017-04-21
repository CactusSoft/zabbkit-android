package ru.zabbkit.android.ui.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.ui.adapter.ServerAdapter;
import ru.zabbkit.android.ui.dialog.DialogHelper;
import ru.zabbkit.android.ui.dialog.DialogHelper.AddCertificateListener;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.ServerStorage;
import ru.zabbkit.android.utils.SharedPreferencesEditor;
import ru.zabbkitserver.android.remote.model.HostGroup;
import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

public class ServerListActivity extends LoadingDialogActivity implements
        AsyncRequestListener, AddCertificateListener {

    protected static final int HOSTLIST_ACTIVITY_START_REQUEST_TAG = 128;

    ListView listView;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_server_list);

        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // "All servers" header
                    SharedPreferencesEditor.getInstance().putString(
                            Constants.PREFS_SERVER_NAME,
                            getString(R.string.all_servers));
                    SharedPreferencesEditor.getInstance().putString(
                            Constants.PREFS_HOSTGROUP_ID, null);
                    SharedPreferencesEditor.getInstance().putString(
                            Constants.PREFS_HOST_ID, null);

                    setResult(RESULT_OK);
                    finish();
                } else {
                    final HostGroup hostGroup = (HostGroup) listView.getAdapter().getItem(
                            position);
                    final String name = hostGroup.getName();
                    final String groupId = hostGroup.getGroupid();
                    SharedPreferencesEditor.getInstance().putString(
                            Constants.PREFS_SERVER_NAME, name);
                    SharedPreferencesEditor.getInstance().putString(
                            Constants.PREFS_HOSTGROUP_ID, groupId);

                    startHostListActivity(name, groupId);
                }
            }
        });

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.server_list);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        final View allHostView = getLayoutInflater().inflate(
                R.layout.item_server, listView, false);
        assert allHostView != null;
        ((TextView) allHostView.findViewById(R.id.view_server))
                .setText(R.string.all);
        listView.addHeaderView(allHostView);

        if (ServerStorage.getInstance().getServers().isEmpty()) {
            showDialog();
            Communicator.getInstance().getServers(prepareParams(), this);
        } else {
            listView.setAdapter(new ServerAdapter(ServerStorage.getInstance()
                    .getServers()));
        }
    }

    private Map<String, Object> prepareParams() {
        final Map<String, Object> params = new ArrayMap<String, Object>();
        params.put(Constants.REQ_OUTPUT, Constants.REQ_VAL_EXTEND);
        params.put(Constants.REQ_MONITORED_HOSTS, true);
        return params;
    }

    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, Constants.FLURRY_APP_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
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
    public void onRequestFailure(Exception e, final String message) {
        dismissDialog();
        Toast.makeText(ServerListActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestSuccess(List<Object> result, Class<?> clazz) {
        final List<HostGroup> hostGroupList = new ArrayList<HostGroup>();
        for (Object aResult : result) {
            final HostGroup hostGroup = (HostGroup) aResult;
            hostGroupList.add(hostGroup);
            ServerStorage.getInstance().addServer(hostGroup.getGroupid(),
                    hostGroup.getName());
        }
        listView.setAdapter(new ServerAdapter(hostGroupList));
        dismissDialog();
    }

    @Override
    protected void onActivityResult(int requestId, int resultId, Intent intent) {
        if (requestId == HOSTLIST_ACTIVITY_START_REQUEST_TAG
                && resultId == RESULT_OK) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void startHostListActivity(String name, String groupId) {
        final Intent intent = new Intent(getApplicationContext(),
                HostListActivity.class);
        intent.putExtra(Constants.REQ_GROUP_IDS, groupId);
        intent.putExtra(Constants.PREFS_HOST_NAME, name);
        startActivityForResult(intent, HOSTLIST_ACTIVITY_START_REQUEST_TAG);
    }

    @Override
    public void onCertificateRequest(final X509Certificate[] certificate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (certificate == null) {
                    showDialog();
                    Communicator.getInstance().getServers(prepareParams(),
                            ServerListActivity.this);
                } else {
                    DialogHelper.showSslDialog(ServerListActivity.this,
                            certificate, ServerListActivity.this);
                }
            }
        });
    }

    @Override
    public void resumeRequest(boolean answer) {
        if (answer) {
            Communicator.getInstance().getServers(prepareParams(), this);
        }
    }
}
