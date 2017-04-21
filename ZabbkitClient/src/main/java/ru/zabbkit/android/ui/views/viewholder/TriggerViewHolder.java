package ru.zabbkit.android.ui.views.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ru.zabbkit.android.R;

/**
 * Created by Alex.Shimborsky on 30.03.2016.
 */
public class TriggerViewHolder extends RecyclerView.ViewHolder {

    public TextView descriptionView;
    public TextView hostNameView;
    public View stateStroke;

    public TriggerViewHolder(View itemView) {
        super(itemView);

        descriptionView = (TextView) itemView.findViewById(R.id.view_trigger);
        hostNameView = (TextView) itemView.findViewById(R.id.view_hostname);
        stateStroke = itemView.findViewById(R.id.color_stroke);
    }

}
