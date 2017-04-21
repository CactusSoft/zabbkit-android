package ru.zabbkit.android.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import java.util.List;

import ru.zabbkit.android.R;
import ru.zabbkit.android.ui.adapter.base.BaseListAdapter;
import ru.zabbkit.android.ui.adapter.base.OnGraphClickListener;
import ru.zabbkit.android.ui.views.viewholder.DataViewHolder;
import ru.zabbkit.android.ui.views.viewholder.OnDataItemClickListener;
import ru.zabbkit.android.utils.ZabbixItemUtils;
import ru.zabbkitserver.android.remote.model.Graph;
import ru.zabbkitserver.android.remote.model.ZabbixItem;

/**
 * Created by Alex.Shimborsky on 30.03.2016.
 */
public class DataAdapter extends BaseListAdapter implements OnGraphClickListener {

    private OnDataItemClickListener mOnDataItemClickListener;

    public DataAdapter(OnDataItemClickListener onDataItemClickListener) {
        mOnDataItemClickListener = onDataItemClickListener;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.item_data;
    }

    @Override
    protected RecyclerView.ViewHolder createViewHolder(View view) {
        return new DataViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DataViewHolder viewHolder = (DataViewHolder) holder;
        final ZabbixItem item = ((List<ZabbixItem>) mObjectsList).get(position);

        List<Graph> graphList = item.getGraphs();
        String graphId = null;
        if (graphList.size() > 0) {
            Graph grph = item.getGraphs().get(0);
            graphId = grph.getGraphId();
            viewHolder.graphButton.setTag(graphId);
            /*
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
                    */
            viewHolder.graphButton.setVisibility(View.VISIBLE);
            viewHolder.separator.setVisibility(View.VISIBLE);
        } else {
            viewHolder.graphButton.setVisibility(View.GONE);
            viewHolder.separator.setVisibility(View.GONE);
        }

        viewHolder.itemName.setText(getParsedItemName(item));
        viewHolder.hostName.setText(item.getHosts().get(0).getHost());
        viewHolder.hostId = item.getHostId();
        viewHolder.value.setText(getParsedValue(item));
    }

    @Override
    public void onGraphClick(String tag, String hostName, String hostId, String parsedName) {
        if (mOnDataItemClickListener != null) {
            mOnDataItemClickListener.onGraphClick(tag, hostName, hostId, parsedName);
        }
    }

    private String getParsedItemName(ZabbixItem item) {

        String itemName = item.getParsedName();

        if (TextUtils.isEmpty(itemName)) {
            itemName = item.getName();

            if (itemName.contains("$")) {
                itemName = ZabbixItemUtils.parseItemName(item);
            }
            item.setParsedName(itemName);
        }
        return itemName;
    }

    private String getParsedValue(ZabbixItem item) {

        String valueString = item.getParsedValue();
        if (TextUtils.isEmpty(valueString)) {

            valueString = ZabbixItemUtils.parseItemValue(item);
            item.setParsedValue(valueString);
        }
        return valueString;
    }
}
