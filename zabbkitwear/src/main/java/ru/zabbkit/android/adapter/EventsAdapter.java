package ru.zabbkit.android.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.wearable.DataMap;

import java.util.ArrayList;

import ru.zabbkit.android.R;
import ru.zabbkit.android.utils.Constants;

/**
 * Created by Alex.Shimborsky on 22/12/2014.
 */
public final class EventsAdapter extends WearableListView.Adapter {
    private ArrayList<DataMap> mDataset;
    private final Context mContext;
    private final LayoutInflater mInflater;

    // Provide a suitable constructor (depends on the kind of dataset)
    public EventsAdapter(Context context, ArrayList<DataMap> dataset) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDataset = dataset;
    }

    public void setDataset(ArrayList<DataMap> dataset) {
        mDataset = dataset;
    }

    // Provide a reference to the type of views you're using
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView descriptionText;
        private ImageView priorityCircleImage;

        public ItemViewHolder(View itemView) {
            super(itemView);
            // find the text view within the custom item's layout
            descriptionText = (TextView) itemView.findViewById(R.id.name);
            priorityCircleImage = (ImageView) itemView.findViewById(R.id.circle);
        }
    }

    // Create new views for list items
    // (invoked by the WearableListView's layout manager)
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // Inflate our custom layout for list items
        return new ItemViewHolder(mInflater.inflate(R.layout.trigger_list_item, null));
    }

    // Replace the contents of a list item
    // Instead of creating new views, the list tries to recycle existing ones
    // (invoked by the WearableListView's layout manager)
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        // retrieve the text view
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        TextView view = itemHolder.descriptionText;
        // replace text contents
        view.setText(mDataset.get(position).getString("description"));
        // replace list item's metadata
        holder.itemView.setTag(position);

        ImageView mCircle = itemHolder.priorityCircleImage;
        int result;
        int prior = Integer.valueOf(mDataset.get(position).getString("priority"));
        switch (prior) {
            case Constants.STATE_NOT_CLASSIFIED:
                result = R.color.not_classified_color;
                break;
            case Constants.STATE_INFORMATION:
                result = R.color.information_color;
                break;
            case Constants.STATE_WARNING:
                result = R.color.warning_color;
                break;
            case Constants.STATE_AVERAGE:
                result = R.color.average_color;
                break;
            case Constants.STATE_HIGH:
                result = R.color.high_color;
                break;
            case Constants.STATE_DISASTER:
                result = R.color.disaster_color;
                break;
            default:
                result = R.color.not_classified_color;
                break;
        }
        ((GradientDrawable) mCircle.getDrawable()).setColor(mContext.getResources().getColor(result));

    }

    // Return the size of your dataset
    // (invoked by the WearableListView's layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}