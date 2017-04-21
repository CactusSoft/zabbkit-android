package ru.zabbkit.android.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.db.entity.Host;
import ru.zabbkit.android.db.provider.DBProvider;
import ru.zabbkit.android.ui.activity.AddHostActivity;
import ru.zabbkit.android.ui.activity.LoginActivity;
import ru.zabbkit.android.ui.adapter.HostsCursorAdapter;

/**
 * Created by Alex.Shimborsky on 23/10/2014.
 */
public class MainHostsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    private HostsCursorAdapter hostsAdapter;
    private ListView mHostsListView;

    private View rootView;

    private final int ID_MENU = 1;

    private static Fragment instance = null;

    public static Fragment newInstance() {
        if(instance == null) {
            instance = new MainHostsFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fr_main_hosts, container,
                false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mHostsListView = (ListView) rootView.findViewById(R.id.hosts_listview);
        mHostsListView.setEmptyView(rootView.findViewById(R.id.empty_hosts_textview));
        registerForContextMenu(mHostsListView);
        mHostsListView.setOnItemClickListener(this);

        hostsAdapter = new HostsCursorAdapter(getActivity(), null, true);
        mHostsListView.setAdapter(hostsAdapter);
        getActivity().getSupportLoaderManager().initLoader(0, null, this);
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
            Cursor cursor = getHostsAdapter().getCursor();
            cursor.moveToPosition(position);
            removeHost(cursor.getString(cursor.getColumnIndex(Host.COLUMN_URL)));
        }
        return super.onContextItemSelected(item);
    }

    private void removeHost(String hostUrl) {
        getActivity().getContentResolver().delete(Uri.parse(DBProvider.HOSTS_CONTENT_URI), Host.COLUMN_URL + "=?",
                new String[]{hostUrl});
    }

    public HostsCursorAdapter getHostsAdapter() {
        return hostsAdapter;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(0, ID_MENU, 100, R.string.action_add_host).setIcon(R.drawable.ic_action_new);
        MenuItemCompat.setShowAsAction(item, 2);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case ID_MENU:
                final Intent intent = new Intent(getActivity(),
                        AddHostActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
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
        Intent outputIntent = new Intent(getActivity(), LoginActivity.class);
        outputIntent.putExtra(Constants.HOST_BUNDLE_URL, cursor.getString(cursor.getColumnIndex(Host.COLUMN_URL)));
        outputIntent.putExtra(Constants.HOST_BUNDLE_LOGIN, cursor.getString(cursor.getColumnIndex(Host.COLUMN_LOGIN)));
        outputIntent.putExtra(Constants.HOST_BUNDLE_PASSWORD, cursor.getString(cursor.getColumnIndex(Host.COLUMN_PASSWORD)));
        getActivity().setResult(Activity.RESULT_OK, outputIntent);
        startActivity(outputIntent);
        getActivity().finish();
    }
}
