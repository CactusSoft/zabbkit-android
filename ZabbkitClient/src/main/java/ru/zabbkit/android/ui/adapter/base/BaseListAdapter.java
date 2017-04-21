package ru.zabbkit.android.ui.adapter.base;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ru.zabbkit.android.R;

/**
 * Created by Alex.Shimborsky on 30.03.2016.
 */
public abstract class BaseListAdapter extends RecyclerView.Adapter {

    protected List mObjectsList = new ArrayList();

    public void setData(List data) {
        mObjectsList.clear();
        mObjectsList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mObjectsList == null ? 0 : mObjectsList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(getLayoutId(), parent, false);

        RecyclerView.ViewHolder vh = createViewHolder(v);
        return vh;
    }

    protected abstract int getLayoutId();

    protected abstract RecyclerView.ViewHolder createViewHolder(View view);
}
