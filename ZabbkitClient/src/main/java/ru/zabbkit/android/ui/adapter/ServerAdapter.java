package ru.zabbkit.android.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ru.zabbkit.android.R;
import ru.zabbkitserver.android.remote.model.HostBase;

public class ServerAdapter extends BaseAdapter {

	private final List<? extends HostBase> mHostList;

	public ServerAdapter(List<? extends HostBase> hostGroupList) {
		mHostList = hostGroupList;
	}

	@Override
	public int getCount() {
		return mHostList.size();
	}

	@Override
	public Object getItem(int position) {
		return mHostList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_server, parent, false);
		}

		final HostBase hostGroup = mHostList.get(position);
		if (hostGroup != null) {
			((TextView) row.findViewById(R.id.view_server)).setText(hostGroup.getName());
		}
		return row;
	}
}
