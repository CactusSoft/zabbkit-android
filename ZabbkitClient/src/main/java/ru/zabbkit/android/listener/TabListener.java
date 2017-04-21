package ru.zabbkit.android.listener;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;

import ru.zabbkit.android.R;

/**
 * Created by Alex.Shimborsky on 31/10/2014.
 */
public class TabListener<T extends Fragment> implements
        ActionBar.TabListener {
    private final FragmentActivity mActivity;
    private final String mTag;
    private final Class<T> mClass;
    private Fragment mFragment;

    /**
     * Constructor used each time a new tab is created.
     *
     * @param activity The host Activity, used to instantiate the fragment
     * @param tag      The identifier tag for the fragment
     * @param clz      The fragment's Class, used to instantiate the fragment
     */
    public TabListener(Activity activity, String tag, Class<T> clz) {
            /*
            if (activity instanceof OverviewActivity) {
                mActivity = (OverviewActivity) activity;
            } else {
                mActivity = (AboutProgramActivity) activity;
            }
            */
        mActivity = (FragmentActivity) activity;
        mTag = tag;
        mClass = clz;

        FragmentManager fragmentManager = mActivity
                .getSupportFragmentManager();
        mFragment = fragmentManager.findFragmentByTag(mTag);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        // Check if the fragment is already initialized
        if (mFragment == null) {
            // If not, instantiate and add it to the activity
            mFragment = Fragment.instantiate(mActivity, mClass.getName());
            mFragment.setRetainInstance(true);
            ft.add(R.id.fragment_content, mFragment, mTag);
        } else {
            // If it exists and not attached, simply attach it in order to
            // show it
            ft.attach(mFragment);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
        if (mFragment != null) {
            // Detach the fragment, because another one is being attached
            ft.detach(mFragment);
        }
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {

    }
}
