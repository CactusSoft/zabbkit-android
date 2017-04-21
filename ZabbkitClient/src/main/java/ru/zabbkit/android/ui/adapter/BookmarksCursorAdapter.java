package ru.zabbkit.android.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.zabbkit.android.R;
import ru.zabbkit.android.db.entity.Bookmark;
import ru.zabbkit.android.db.helper.DbHelper;
import ru.zabbkit.android.ui.views.assist.Period;

public class BookmarksCursorAdapter extends CursorAdapter {

	private LayoutInflater mInflater;

	public BookmarksCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final BookmarkViewHolder viewHolder = (BookmarkViewHolder) view
				.getTag();
		final int itemParamNameCol = cursor
				.getColumnIndex(Bookmark.COLUMN_PARAM_NAME);
		final int itemServerNameCol = cursor
				.getColumnIndex(Bookmark.COLUMN_SERVER_NAME);
		final int itemPeriodCol = cursor
				.getColumnIndex(Bookmark.COLUMN_PERIOD);

		viewHolder.itemParamName.setText(cursor.getString(itemParamNameCol));
		viewHolder.itemServerName.setText(cursor.getString(itemServerNameCol));
		final Period[] periods = Period.values();
		viewHolder.itemPeriod.setText(periods[cursor.getInt(itemPeriodCol)]
				.getNameRes());
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		BookmarkViewHolder viewHolder;
		final View view = mInflater.inflate(R.layout.item_bookmark, parent,
				false);
		viewHolder = new BookmarkViewHolder();
		viewHolder.itemParamName = (TextView) view
				.findViewById(R.id.item_param_name);
		viewHolder.itemServerName = (TextView) view
				.findViewById(R.id.server_name);
		viewHolder.itemPeriod = (TextView) view.findViewById(R.id.item_period);
		view.setTag(viewHolder);
		return view;
	}

	static class BookmarkViewHolder {
		TextView itemParamName;
		TextView itemPeriod;
		TextView itemServerName;
	}
}
