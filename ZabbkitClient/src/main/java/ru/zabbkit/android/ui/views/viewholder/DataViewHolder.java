package ru.zabbkit.android.ui.views.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ru.zabbkit.android.R;
import ru.zabbkit.android.ui.adapter.base.OnGraphClickListener;

/**
 * Created by Alex.Shimborsky on 30.03.2016.
 */
public class DataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView itemName;
    public TextView hostName;
    public TextView value;
    public View graphButton;
    public View separator;

    public String hostId;

    private OnGraphClickListener mOnGraphClickListener;

    public DataViewHolder(View itemView, OnGraphClickListener onGraphClickListener) {
        super(itemView);

        mOnGraphClickListener = onGraphClickListener;

        itemName = (TextView) itemView.findViewById(R.id.item_name);
        hostName = (TextView) itemView.findViewById(R.id.host_name);
        value = (TextView) itemView.findViewById(R.id.value);
        graphButton = itemView.findViewById(R.id.graph_button);
        separator = itemView.findViewById(R.id.graph_separator);

        graphButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.graph_button:
                if (mOnGraphClickListener != null) {
                    mOnGraphClickListener.onGraphClick(graphButton.getTag().toString(), hostId,
                            hostName.getText().toString(),
                            itemName.getText().toString());
                }
                break;
        }
    }
}
