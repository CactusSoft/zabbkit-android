package ru.zabbkit.android.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ru.zabbkit.android.R;
import ru.zabbkit.android.utils.ZabbixItemUtils;
import ru.zabbkitserver.android.remote.model.ZabbixItem;

/**
 * 
 * @author Dmitry.Kalenchuk
 * 
 */
public class ItemAdapter extends BaseAdapter {

	private final List<ZabbixItem> mItems;

	public ItemAdapter(List<ZabbixItem> items) {
		mItems = items;
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int index) {
		return mItems.get(index);
	}

	@Override
	public long getItemId(int index) {
		return index;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemDataViewHolder viewHolder;
		View newView;
		if (convertView != null) {
			newView = convertView;
			viewHolder = (ItemDataViewHolder) newView.getTag();
		} else {
			newView = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.item_data, null);
			viewHolder = new ItemDataViewHolder();
			viewHolder.itemName = (TextView) newView
					.findViewById(R.id.item_name);
			viewHolder.hostName = (TextView) newView
					.findViewById(R.id.host_name);
			viewHolder.value = (TextView) newView.findViewById(R.id.value);
			newView.setTag(viewHolder);
		}

		final ZabbixItem item = mItems.get(position);
		viewHolder.itemName.setText(getParsedItemName(item));
		viewHolder.hostName.setText(item.getHosts().get(0).getHost());
		viewHolder.value.setText(getParsedValue(item));

		return newView;
	}

	static class ItemDataViewHolder {
		TextView itemName;
		TextView hostName;
		TextView value;
	}

	/**
	 * Methods substitutes arguments to the item name and return valid string
	 * Implementation based on code from zabbix web fronted implementation:
	 * \zabbix-2.0.6\frontends\php\include\items.inc.php
	 * 
	 * Example: item name: 'Test item $1, $2, $3' item key: 'test.key[a, b]'
	 * result: 'Test item a, b'
	 * 
	 * @param item
	 * @return parsed item name
	 */
	private String getParsedItemName(ZabbixItem item) {

		String itemName = item.getParsedName();

		if (TextUtils.isEmpty(itemName)) {
			itemName = item.getName();

			// check, is item name contains any arguments
			if (itemName.contains("$")) {
				itemName = ZabbixItemUtils.parseItemName(item);
			}
			item.setParsedName(itemName);
		}
		return itemName;
	}

	/**
	 * Method return item value in human readable format
	 * 
	 * @param item
	 * @return string in format "<value>[<unit>]"
	 */
	private String getParsedValue(ZabbixItem item) {

		String valueString = item.getParsedValue();
		if (TextUtils.isEmpty(valueString)) {

			valueString = ZabbixItemUtils.parseItemValue(item);
			item.setParsedValue(valueString);
		}
		return valueString;
	}

}
