package ru.zabbkit.android.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import ru.zabbkit.android.R;
import ru.zabbkit.android.ui.activity.SlideMenuActivity;
import ru.zabbkit.android.ui.views.ScrollableFragmentTabHost;

/**
 * Created by Alex.Shimborsky on 30/10/2014.
 */
public class AboutFragment extends Fragment {

    private static final String SELECTED_TAB_NUM_KEY = "selected_tab_num_key";

    private int mSelectedTab;
    private ScrollableFragmentTabHost mTabHost;

    private View view;

    private static Fragment instance = null;

    public static Fragment newInstance() {
        if(instance == null) {
            instance = new AboutFragment();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fr_about, container,
                false);

        if (savedInstanceState != null) {
            mSelectedTab = savedInstanceState.getInt(SELECTED_TAB_NUM_KEY, 0);
        }
        initView();
        FlurryAgent.logEvent("About page");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initView() {

        mTabHost = (ScrollableFragmentTabHost) view.findViewById(R.id.about_tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(),
                R.id.fragment_content, true);

        final View aboutCastTab = getActivity().getLayoutInflater().inflate(
                R.layout.view_tab_item, null);
        TextView tabItem = (TextView) aboutCastTab
                .findViewById(R.id.tabItem);
        tabItem.setText(R.string.cast);
        tabItem.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_tab_about_cast_selector, 0, 0, 0);
        mTabHost.addTab(
                mTabHost.newTabSpec(AboutCastFragment.class.getName())
                        .setIndicator(aboutCastTab),
                AboutCastFragment.class, null);

        final View aboutLegendTab = getActivity().getLayoutInflater().inflate(
                R.layout.view_tab_item, null);
        tabItem = (TextView) aboutLegendTab.findViewById(R.id.tabItem);
        tabItem.setText(R.string.legend);
        tabItem.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_tab_about_legend_selector, 0, 0, 0);
        mTabHost.addTab(
                mTabHost.newTabSpec(AboutLegendFragment.class.getName())
                        .setIndicator(aboutLegendTab),
                AboutLegendFragment.class, null);

        mTabHost.setCurrentTab(mSelectedTab);
    }
}
