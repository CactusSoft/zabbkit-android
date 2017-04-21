package ru.zabbkit.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.push.GcmPrefUtil;
import ru.zabbkit.android.push.GcmUtil;
import ru.zabbkit.android.ui.activity.ServerListActivity;
import ru.zabbkit.android.ui.activity.SlideMenuActivity;
import ru.zabbkit.android.ui.assist.FragmentDataManager;
import ru.zabbkit.android.ui.dialog.DialogHelper;
import ru.zabbkit.android.ui.views.ScrollableFragmentTabHost;
import ru.zabbkit.android.utils.NetworkUtils;

/**
 * Created by Alex.Shimborsky on 30/10/2014.
 */
public class OverviewFragment extends Fragment implements
        DialogHelper.AddCertificateListener,
        View.OnTouchListener {

    private static final int REQUEST_UPDATE_GOOGLE_PLAY_SERVICES = 1;
    private static final String SELECTED_TAB_NUM_KEY = "selected_tab_num_key";
    private View rootView;
    private ScrollableFragmentTabHost mTabHost;

    private static boolean needUpdate = false;

    private final int MENU_SETTING = 50;

    private static Fragment instance = null;

    public static Fragment newInstance() {
        if (instance == null) {
            instance = new OverviewFragment();
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fr_overview, container,
                false);
        setHasOptionsMenu(true);

        initView(savedInstanceState);
        checkGooglePlayServices();
        FlurryAgent.logEvent("Show OverView");

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (needUpdate) {
            needUpdate = false;
            updateCurrentFragment();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add(0, MENU_SETTING, 100, R.string.action_settings).setIcon(R.drawable.ic_menu_servers);
        MenuItemCompat.setShowAsAction(item, 2);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case MENU_SETTING:
                if (NetworkUtils.isNetEnabled(getActivity())) {
                    Intent intent = new Intent(getActivity(),
                            ServerListActivity.class);
                    getActivity().startActivityForResult(intent,
                            Constants.DATA_REQUEST);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView(Bundle savedInstanceState) {

        mTabHost = (ScrollableFragmentTabHost) rootView.findViewById(R.id.over_tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(),
                R.id.over_fragment_content, true);

        View triggerTab = getActivity().getLayoutInflater().inflate(
                R.layout.view_tab_item, null);
        TextView tabItem = (TextView) triggerTab.findViewById(R.id.tabItem);
        tabItem.setText(R.string.triggers);
        tabItem.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_tab_trigger_selector, 0, 0, 0);
        mTabHost.addTab(
                mTabHost.newTabSpec(TriggersFragment.class.getName())
                        .setIndicator(triggerTab), TriggersFragment.class,
                null);

        View dataTab = getActivity().getLayoutInflater().inflate(R.layout.view_tab_item,
                null);
        tabItem = (TextView) dataTab.findViewById(R.id.tabItem);
        tabItem.setText(R.string.data);
        tabItem.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_tab_data_selector, 0, 0, 0);
        mTabHost.addTab(mTabHost.newTabSpec(DataFragment.class.getName())
                .setIndicator(dataTab), DataFragment.class, null);

        View eventTab = getActivity().getLayoutInflater().inflate(R.layout.view_tab_item,
                null);
        tabItem = (TextView) eventTab.findViewById(R.id.tabItem);
        tabItem.setText(R.string.events);
        tabItem.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_tab_event_selector, 0, 0, 0);
        mTabHost.addTab(mTabHost.newTabSpec(EventFragment.class.getName())
                .setIndicator(eventTab), EventFragment.class, null);

        int selectedTab = 0;
        if (savedInstanceState != null) {
            selectedTab = savedInstanceState
                    .getInt(SELECTED_TAB_NUM_KEY, 0);
        }
        mTabHost.setCurrentTab(selectedTab);
    }

    public void markFragmentsAsObsolete() {
        FragmentManager fragmentManager = getChildFragmentManager();

        TriggersFragment triggersFragment = (TriggersFragment) fragmentManager
                .findFragmentByTag(TriggersFragment.class.getName());
        if (triggersFragment != null) {
            triggersFragment.setObsoleteDataFlag();
        }

        DataFragment dataFragment = (DataFragment) fragmentManager
                .findFragmentByTag(DataFragment.class.getName());
        if (dataFragment != null) {
            dataFragment.setObsoleteDataFlag();
        }

        EventFragment eventFragment = (EventFragment) fragmentManager
                .findFragmentByTag(EventFragment.class.getName());
        if (eventFragment != null) {
            eventFragment.setObsoleteDataFlag();
        }
    }

    public void setNeedUpdate() {
        needUpdate = true;
    }

    public void updateCurrentFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        if (mTabHost != null) {
            String selectedTabTag = mTabHost.getCurrentTabTag();
            Fragment currentRunningFragment = fragmentManager
                    .findFragmentByTag(selectedTabTag);
            if (currentRunningFragment != null) {
                ((FragmentDataManager) currentRunningFragment).updateDataSet();
            }
        }
    }

    private void checkGooglePlayServices() {
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (result == ConnectionResult.SUCCESS) {
            registerGcmDevice();
        } else {
            GooglePlayServicesUtil.getErrorDialog(result, getActivity(),
                    REQUEST_UPDATE_GOOGLE_PLAY_SERVICES).show();
        }
    }

    private void registerGcmDevice() {
        String regId = GcmPrefUtil.readRegId(getActivity());
        if (TextUtils.isEmpty(regId)) {
            GcmUtil.register(getActivity().getApplicationContext(), null);
        }
    }

    @Override
    public void resumeRequest(boolean answer) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mTabHost != null) {
            return mTabHost.onTouch(view, motionEvent);
        } else
            return false;
    }
}
