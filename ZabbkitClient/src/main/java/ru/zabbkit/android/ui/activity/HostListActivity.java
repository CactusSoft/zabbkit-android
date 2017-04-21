package ru.zabbkit.android.ui.activity;

import android.annotation.TargetApi;
import android.net.Uri;
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
import java.util.TreeMap;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.db.entity.Bookmark;
import ru.zabbkit.android.db.helper.DbHelper;
import ru.zabbkit.android.db.provider.DBProvider;
import ru.zabbkit.android.ui.adapter.ServerAdapter;
import ru.zabbkit.android.ui.dialog.DialogHelper;
import ru.zabbkit.android.ui.dialog.DialogHelper.AddCertificateListener;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.L;
import ru.zabbkit.android.utils.ServerStorage;
import ru.zabbkit.android.utils.SharedPreferencesEditor;
import ru.zabbkitserver.android.remote.model.Host;
import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

public class HostListActivity extends LoadingDialogActivity implements
        AsyncRequestListener, AddCertificateListener {

    private String mGroupHostId = "";

    private ListView listView;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_server_list);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        listView = (ListView) findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // "All Servers" header
                    SharedPreferencesEditor.getInstance().putString(
                            Constants.PREFS_HOST_ID, null);
                    if (0 < mGroupHostId.length()) {
                        List<Host> hosts = ServerStorage.getInstance().getHosts(
                                mGroupHostId);
                        String[] hostsIds = new String[hosts.size()];
                        long start = System.currentTimeMillis();
                        L.i("Start: " + System.currentTimeMillis());
                        for (int i = 0; i < hosts.size(); i++) {
                            Host host = hosts.get(i);
                            L.i("Host: " + host.getName() + " Id: " + host.getHostid());
                            hostsIds[i] = host.getHostid();
                        }
                        L.i("Finish: " + (System.currentTimeMillis() - start));

                        // Remove unnecessary bookmarks
                        StringBuilder deleteQuery = new StringBuilder(Bookmark.COLUMN_SERVER_ID
                                + " NOT IN (?");
                        for (int i = 1; i < hostsIds.length; i++) {
                            deleteQuery.append(", ?");
                        }
                        deleteQuery.append(") ");
                        getContentResolver().delete(Uri.parse(DBProvider.BOOKMARKS_CONTENT_URI),
                                deleteQuery.toString(), hostsIds);
                    }
                } else {
                    Host host = (Host) listView.getAdapter().getItem(position);
                    SharedPreferencesEditor.getInstance().putString(
                            Constants.PREFS_SERVER_NAME, host.getName());
                    SharedPreferencesEditor.getInstance().putString(
                            Constants.PREFS_HOST_ID, host.getHostid());

                    // Remove unnecessary bookmarks
                    String deleteQuery = Bookmark.COLUMN_SERVER_ID + "<> ? ";
                    String[] deleteQueryArgs = new String[]{host.getHostid()};
                    getContentResolver().delete(Uri.parse(DBProvider.BOOKMARKS_CONTENT_URI),
                            deleteQuery, deleteQueryArgs);
                }

                setResult(RESULT_OK);
                finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        });

        final View allHostView = getLayoutInflater().inflate(
                R.layout.item_server, listView, false);
        assert allHostView != null;
        ((TextView) allHostView.findViewById(R.id.view_server))
                .setText(R.string.all);
        listView.addHeaderView(allHostView);

        String hostName = "";
        final Bundle data = getIntent().getExtras();
        if (data != null) {
            mGroupHostId = data.getString(Constants.REQ_GROUP_IDS);
            hostName = data.getString(Constants.PREFS_HOST_NAME);

            if (ServerStorage.getInstance().getHosts(mGroupHostId).isEmpty()) {
                showDialog();
                Communicator.getInstance().getHost(prepareParams(), this);
            } else {
                listView.setAdapter(new ServerAdapter(ServerStorage.getInstance()
                        .getHosts(mGroupHostId)));
            }
        } else {
            Toast.makeText(this, getString(R.string.no_data),
                    Toast.LENGTH_SHORT).show();
        }
        actionBar.setTitle(hostName);
    }

    private Map<String, Object> prepareParams() {
        final Map<String, Object> params = new ArrayMap<String, Object>();
        params.put(Constants.REQ_OUTPUT, Constants.REQ_VAL_EXTEND);
        params.put(Constants.REQ_GROUP_IDS, mGroupHostId);
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
        Toast.makeText(HostListActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestSuccess(List<Object> result, Class<?> clazz) {
        Map<String, String> mapHost = new TreeMap<String, String>();
        final List<Host> hostList = new ArrayList<Host>();
        for (Object aResult : result) {
            Host host = (Host) aResult;
            hostList.add(host);
            mapHost.put(host.getHostid(), host.getName());
        }
        ServerStorage.getInstance().addHost(mapHost, mGroupHostId);
        listView.setAdapter(new ServerAdapter(hostList));
        dismissDialog();
    }

    @Override
    public void onCertificateRequest(final X509Certificate[] certificate) {
        if (certificate == null) {
            showDialog();
            Communicator.getInstance().getHost(prepareParams(),
                    HostListActivity.this);
        } else {
            DialogHelper.showSslDialog(HostListActivity.this,
                    certificate, HostListActivity.this);
        }
    }

    @Override
    public void resumeRequest(boolean answer) {
        if (answer) {
            Communicator.getInstance().getHost(prepareParams(), this);
        }
    }
}
