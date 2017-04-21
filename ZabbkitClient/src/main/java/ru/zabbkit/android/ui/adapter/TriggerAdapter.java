package ru.zabbkit.android.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Collections;
import java.util.List;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.ui.adapter.base.BaseListAdapter;
import ru.zabbkit.android.ui.views.viewholder.TriggerViewHolder;
import ru.zabbkit.android.utils.GeneralAbility;
import ru.zabbkit.android.utils.comparator.TriggerComparator;
import ru.zabbkitserver.android.remote.model.Trigger;

/**
 * Created by Alex.Shimborsky on 30.03.2016.
 */
public class TriggerAdapter extends BaseListAdapter {

    @Override
    protected int getLayoutId() {
        return R.layout.item_trigger;
    }

    @Override
    protected RecyclerView.ViewHolder createViewHolder(View view) {
        return new TriggerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TriggerViewHolder viewHolder = (TriggerViewHolder) holder;
        final Trigger trigger = ((List<Trigger>) mObjectsList).get(position);
        if (trigger != null) {
            viewHolder.descriptionView.setText(trigger.getDescription());
            String hostName = "";
            try {
                if (trigger.hosts.size() > 0 && trigger.hosts.get(0).host != null) {
                    hostName = trigger.hosts.get(0).host;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            viewHolder.hostNameView.setText(hostName);

            if (Constants.STATE_OK.equals(trigger.getValue())) {
                viewHolder.stateStroke.setBackgroundResource(R.color.ok_color);
            } else {
                int res = GeneralAbility.getTriggersResourceColor(Integer.valueOf(trigger
                        .getPriority()));
                viewHolder.stateStroke.setBackgroundResource(res);
            }
        }
    }

    @Override
    public void setData(List data) {
        Collections.sort(data, new TriggerComparator());
        super.setData(data);
    }
}
