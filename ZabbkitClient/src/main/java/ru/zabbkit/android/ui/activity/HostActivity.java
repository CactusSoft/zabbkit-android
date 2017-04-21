package ru.zabbkit.android.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.db.entity.Host;
import ru.zabbkit.android.db.provider.DBProvider;
import ru.zabbkit.android.ui.adapter.HostsCursorAdapter;

/**
 * Created by Alex.Shimborsky on 20/10/2014.
 */
public class HostActivity extends ActionBarActivity implements
        AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private HostsCursorAdapter hostsAdapter;

    private ListView mHostsListView;

    private final int ID_MENU = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_hosts);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_logo);

        mHostsListView = (ListView) findViewById(R.id.hosts_listview);
        mHostsListView.setEmptyView(findViewById(R.id.empty_hosts_textview));
        registerForContextMenu(mHostsListView);
        mHostsListView.setOnItemClickListener(this);

        hostsAdapter = new HostsCursorAdapter(HostActivity.this, null, true);
        mHostsListView.setAdapter(hostsAdapter);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.hosts_listview) {
            String[] menuItems = getResources().getStringArray(R.array.list_hosts_actions);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        String[] actions = getResources().getStringArray(R.array.list_hosts_actions);
        if (item.getTitle().equals(actions[0])) {
            AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            int position = contextMenuInfo.position;
            Cursor cursor = hostsAdapter.getCursor();
            cursor.moveToPosition(position);
            removeHost(cursor.getString(cursor.getColumnIndex(Host.COLUMN_URL)));
        }
        return super.onContextItemSelected(item);
    }

    private void removeHost(String hostUrl) {
        getContentResolver().delete(Uri.parse(DBProvider.HOSTS_CONTENT_URI), Host.COLUMN_URL + "=?",
                new String[]{hostUrl});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(0, ID_MENU, 100, R.string.action_add_host).setIcon(R.drawable.ic_menu_servers);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ID_MENU:
                final Intent intent = new Intent(HostActivity.this,
                        AddHostActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(HostActivity.this,
                Uri.parse(DBProvider.HOSTS_CONTENT_URI),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        hostsAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        hostsAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = hostsAdapter.getCursor();
        cursor.moveToPosition(position);
        Intent outputIntent = new Intent();
        outputIntent.putExtra(Constants.HOST_BUNDLE_URL, cursor.getString(cursor.getColumnIndex(Host.COLUMN_URL)));
        outputIntent.putExtra(Constants.HOST_BUNDLE_LOGIN, cursor.getString(cursor.getColumnIndex(Host.COLUMN_LOGIN)));
        outputIntent.putExtra(Constants.HOST_BUNDLE_PASSWORD, cursor.getString(cursor.getColumnIndex(Host.COLUMN_PASSWORD)));
        setResult(RESULT_OK, outputIntent);
        finish();
    }
}
