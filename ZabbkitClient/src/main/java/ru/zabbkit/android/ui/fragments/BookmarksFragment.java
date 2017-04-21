package ru.zabbkit.android.ui.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.db.entity.Bookmark;
import ru.zabbkit.android.db.provider.DBProvider;
import ru.zabbkit.android.ui.activity.GraphActivity;
import ru.zabbkit.android.ui.adapter.BookmarksCursorAdapter;

public class BookmarksFragment extends Fragment implements
        OnItemClickListener, LoaderCallbacks<Cursor> {

    private BookmarksCursorAdapter mCursorAdapter;
    //private DbHelper dbHelper;

    private ListView mBookmarksListView;
    private View rootView;

    private static Fragment instance = null;

    public static Fragment newInstance() {
        if(instance == null) {
            instance = new BookmarksFragment();
        }
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mCursorAdapter = new BookmarksCursorAdapter(getActivity(), null, true);
        mBookmarksListView.setAdapter(mCursorAdapter);

        mCursorAdapter = new BookmarksCursorAdapter(getActivity(), null, true);
        mBookmarksListView.setAdapter(mCursorAdapter);
        getActivity().getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fr_bookmarks_list, container,
                false);

        mBookmarksListView = (ListView) rootView
                .findViewById(R.id.bookmarks_list);
        mBookmarksListView.setEmptyView(rootView
                .findViewById(R.id.empty_bookmarks_textview));
        mBookmarksListView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,
                            int position, long id) {
        Cursor cursor = mCursorAdapter.getCursor();
        cursor.moveToPosition(position);
        Intent intent = new Intent(getActivity(), GraphActivity.class);
        intent.putExtra(Constants.GRAPH_ID, cursor.getString(cursor.getColumnIndex(Bookmark.COLUMN_GRAPH_ID)));
        intent.putExtra(Constants.HOST_ID, cursor.getString(cursor.getColumnIndex(Bookmark.COLUMN_SERVER_ID)));
        intent.putExtra(Constants.HOST_NAME, cursor.getString(cursor.getColumnIndex(Bookmark.COLUMN_SERVER_NAME)));
        intent.putExtra(Constants.PARAM_NAME, cursor.getString(cursor.getColumnIndex(Bookmark.COLUMN_PARAM_NAME)));
        intent.putExtra(Constants.PERIOD, cursor.getString(cursor.getColumnIndex(Bookmark.COLUMN_PERIOD)));
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new CursorLoader(getActivity(),
                Uri.parse(DBProvider.BOOKMARKS_CONTENT_URI),
                null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

}
