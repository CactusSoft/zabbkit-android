package ru.zabbkit.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import ru.zabbkit.android.R;
import ru.zabbkit.android.ui.adapter.base.BaseListAdapter;
import ru.zabbkit.android.ui.assist.FragmentDataManager;
import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

/**
 * Created by Alex.Shimborsky on 10/12/2014.
 */
public abstract class BaseListFragment extends Fragment
        implements AsyncRequestListener, FragmentDataManager {

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    protected BaseListAdapter mAdapter;

    protected boolean mIsDataObsolete;
    protected boolean mIsOnRefresh;
    protected RecyclerView mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_overview_list, container,
                false);
        assert view != null;

        mList = (RecyclerView) view.findViewById(R.id.list);
        mList.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mList.setLayoutManager(layoutManager);
        mList.setAdapter(getAdapter());

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.grey, android.R.color.white);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sendRequest();
                mIsOnRefresh = true;
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDataSet();
    }

    public void updateDataSet() {
        showDialog();
        sendRequest();
    }

    protected void showDialog() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    protected boolean dismissDialog() {
        mSwipeRefreshLayout.setRefreshing(false);
        return false;
    }

    protected RecyclerView.Adapter getAdapter() {
        return null;
    }

    protected void setAdapterData(List data) {
        mAdapter.setData(data);
    }

    protected abstract void sendRequest();

}
