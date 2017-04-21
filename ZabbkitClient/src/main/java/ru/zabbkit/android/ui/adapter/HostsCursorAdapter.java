package ru.zabbkit.android.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.zabbkit.android.R;
import ru.zabbkit.android.db.entity.Host;

/**
 * Created by Alex.Shimborsky on 20/10/2014.
 */
public class HostsCursorAdapter extends CursorAdapter {

    private LayoutInflater mInflater;

    public HostsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        HostViewHolder viewHolder;
        final View view = mInflater.inflate(R.layout.item_host, viewGroup,
                false);
        viewHolder = new HostViewHolder();
        viewHolder.itemName = (TextView) view
                .findViewById(R.id.host_name);
        viewHolder.itemUrl = (TextView) view
                .findViewById(R.id.host_url);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final HostViewHolder viewHolder = (HostViewHolder) view
                .getTag();
        final int itemNameCol = cursor
                .getColumnIndex(Host.COLUMN_NAME);
        final int itemUrlCol = cursor
                .getColumnIndex(Host.COLUMN_URL);

        viewHolder.itemName.setText(cursor.getString(itemNameCol));
        viewHolder.itemUrl.setText(cursor.getString(itemUrlCol));
    }

    static class HostViewHolder {
        TextView itemName;
        TextView itemUrl;
    }
}
