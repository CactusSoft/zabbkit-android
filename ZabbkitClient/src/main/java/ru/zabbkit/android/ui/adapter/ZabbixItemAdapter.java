package ru.zabbkit.android.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.ui.activity.GraphActivity;
import ru.zabbkit.android.utils.ZabbixItemUtils;
import ru.zabbkitserver.android.remote.model.Graph;
import ru.zabbkitserver.android.remote.model.ZabbixItem;

/**
 * @author Dmitry.Kalenchuk
 */
public class ZabbixItemAdapter extends BaseAdapter {

    private final List<ZabbixItem> mItems = Collections
            .synchronizedList(new ArrayList<ZabbixItem>());

    private Context mContext;

    public ZabbixItemAdapter(Context context) {
        mContext = context;
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
        if (convertView != null) {
            viewHolder = (ItemDataViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_data, parent, false);
            viewHolder = new ItemDataViewHolder();
            viewHolder.itemName = (TextView) convertView
                    .findViewById(R.id.item_name);
            viewHolder.hostName = (TextView) convertView
                    .findViewById(R.id.host_name);
            viewHolder.value = (TextView) convertView.findViewById(R.id.value);
            viewHolder.graphButton = convertView.findViewById(R.id.graph_button);
            viewHolder.separator = convertView.findViewById(R.id.graph_separator);
        }
        final ZabbixItem item = mItems.get(position);

        List<Graph> graphList = item.getGraphs();
        String graphId = null;
        if (graphList.size() > 0) {
            Graph grph = item.getGraphs().get(0);
            graphId = grph.getGraphId();
            viewHolder.graphButton.setTag(graphId);
            viewHolder.graphButton
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext,
                                    GraphActivity.class);
                            if (v.getTag() != null) {
                                intent.putExtra(Constants.GRAPH_ID,
                                        (String) v.getTag());
                            }
                            intent.putExtra(Constants.HOST_ID, item.getHostId());
                            intent.putExtra(Constants.HOST_NAME, item
                                    .getHosts().get(0).getHost());
                            intent.putExtra(Constants.PARAM_NAME,
                                    getParsedItemName(item));
                            mContext.startActivity(intent);
                        }
                    });
            viewHolder.graphButton.setVisibility(View.VISIBLE);
            viewHolder.separator.setVisibility(View.VISIBLE);
        } else {
            viewHolder.graphButton.setVisibility(View.GONE);
            viewHolder.separator.setVisibility(View.GONE);
        }

        viewHolder.itemName.setText(getParsedItemName(item));
        viewHolder.hostName.setText(item.getHosts().get(0).getHost());
        viewHolder.value.setText(getParsedValue(item));

        convertView.setTag(viewHolder);
        return convertView;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    /**
     * Methods substitutes arguments to the item name and return valid string
     * Implementation based on code from zabbix web fronted implementation:
     * \zabbix-2.0.6\frontends\php\include\items.inc.php
     * <p/>
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

    public void setItems(List<ZabbixItem> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    static class ItemDataViewHolder {
        TextView itemName;
        TextView hostName;
        TextView value;
        View graphButton;
        View separator;
    }

}
