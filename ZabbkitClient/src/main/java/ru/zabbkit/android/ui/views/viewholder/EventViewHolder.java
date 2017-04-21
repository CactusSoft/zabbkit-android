package ru.zabbkit.android.ui.views.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ru.zabbkit.android.R;

/**
 * Created by Alex.Shimborsky on 30.03.2016.
 */
public class EventViewHolder extends RecyclerView.ViewHolder {

    public View stateStroke;
    public TextView descriptionView;
    public TextView dateView;
    public TextView durationView;

    public EventViewHolder(View itemView) {
        super(itemView);

        descriptionView = (TextView) itemView.findViewById(R.id.event_view);
        dateView = (TextView) itemView.findViewById(R.id.date_view);
        durationView = (TextView) itemView.findViewById(R.id.duration_view);
        stateStroke = itemView.findViewById(R.id.color_stroke);
    }

}
