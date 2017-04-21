package ru.zabbkit.android.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import ru.zabbkit.android.R;
import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.push.GcmPrefUtil;
import ru.zabbkit.android.push.GcmUtil;
import ru.zabbkit.android.ui.adapter.MenuAdapter;
import ru.zabbkit.android.ui.fragments.AboutFragment;
import ru.zabbkit.android.ui.fragments.BookmarksFragment;
import ru.zabbkit.android.ui.fragments.MainHostsFragment;
import ru.zabbkit.android.ui.fragments.NotificationsFragment;
import ru.zabbkit.android.ui.fragments.OverviewFragment;
import ru.zabbkit.android.utils.Communicator;
import ru.zabbkit.android.utils.SharedPreferencesEditor;
import ru.zabbkitserver.android.remote.request.AsyncRequestListener;

/**
 * Created by Alex.Shimborsky on 29/10/2013.
 */
public class SlideMenuActivity extends ActionBarActivity
        implements AsyncRequestListener {

    private static final int REQUEST_UPDATE_GOOGLE_PLAY_SERVICES = 1;
    private final int MENU_OVERVIEW = 0;
    private final int MENU_BOOKMARKS = 1;
    private final int MENU_NOTIFICATIONS = 2;
    private final int MENU_HOSTS = 3;
    private final int MENU_ABOUT = 4;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;

    private int currentFragment = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_slide_menu);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.app_name,
                R.string.app_name
        );

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new MenuAdapter(SlideMenuActivity.this, R.layout.drawer_list_item));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawers();
                if (position == 5) {
                    FlurryAgent.logEvent("Logout pressed");
                    logout();
                    finish();
                    Intent loginIntent = new Intent(SlideMenuActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                } else {
                    currentFragment = position;
                    showFragment();
                }

            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.overview);

        showFragment();
        checkGooglePlayServices();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.DATA_REQUEST && resultCode == RESULT_OK) {
            if (getCurrentFragmentPosition() == MENU_OVERVIEW) {
                OverviewFragment overviewFragment = (OverviewFragment) getSupportFragmentManager().
                        findFragmentByTag(getFragmentTag(0));
                if (overviewFragment.isVisible()) {
                    overviewFragment.markFragmentsAsObsolete();
                    overviewFragment.setNeedUpdate();
                }
            }
        }
    }

    private void checkGooglePlayServices() {
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (result == ConnectionResult.SUCCESS) {
            registerGcmDevice();
        } else {
            GooglePlayServicesUtil.getErrorDialog(result, this,
                    REQUEST_UPDATE_GOOGLE_PLAY_SERVICES).show();
        }
    }

    private void registerGcmDevice() {
        String regId = GcmPrefUtil.readRegId(this);
        if (TextUtils.isEmpty(regId)) {
            GcmUtil.register(getApplicationContext(), null);
        }
    }

    public void logout() {
        SharedPreferencesEditor.getInstance().removeValue(Constants.PREFS_HTTP_AUTH_LOGIN);
        SharedPreferencesEditor.getInstance().removeValue(Constants.PREFS_HTTP_AUTH_PASS);

        final Map<String, Object> params = new ArrayMap<String, Object>();
        Communicator.getInstance().logout(params, this);
        SharedPreferencesEditor.getInstance().removeValue(Constants.PREFS_AUTH);
        SharedPreferencesEditor.getInstance().putBoolean(
                Constants.PREFS_IS_AUTHORIZED, false);
    }

    private void showFragment() {
        Fragment fragment = null;
        if (currentFragment == -1) {
            fragment = OverviewFragment.newInstance();
            currentFragment = MENU_OVERVIEW;
        } else {
            switch (currentFragment) {
                case MENU_OVERVIEW:
                    getSupportActionBar().setTitle(R.string.overview);
                    fragment = OverviewFragment.newInstance();
                    break;
                case MENU_BOOKMARKS:
                    getSupportActionBar().setTitle(R.string.bookmarks);
                    fragment = BookmarksFragment.newInstance();
                    break;
                case MENU_NOTIFICATIONS:
                    getSupportActionBar().setTitle(R.string.notifications);
                    fragment = NotificationsFragment.newInstance();
                    break;
                case MENU_HOSTS:
                    getSupportActionBar().setTitle(R.string.hosts);
                    fragment = MainHostsFragment.newInstance();
                    break;
                case MENU_ABOUT:
                    getSupportActionBar().setTitle(R.string.about_program);
                    fragment = AboutFragment.newInstance();
                    break;
            }
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment, getFragmentTag(currentFragment));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onRequestFailure(Exception e, final String message) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(SlideMenuActivity.this, message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestSuccess(List<Object> result, Class<?> clazz) {

    }

    @Override
    public void onCertificateRequest(X509Certificate[] certificate) {

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            finish();
        }
    }

    public int getCurrentFragmentPosition() {
        return currentFragment;
    }

    public String getCurrentFragment() {
        return getFragmentTag(currentFragment);
    }

    private String getFragmentTag(int position) {
        switch (position) {
            case MENU_OVERVIEW:
                return "overview";
            case MENU_BOOKMARKS:
                return "bookmarks";
            case MENU_NOTIFICATIONS:
                return "notifications";
            case MENU_HOSTS:
                return "hosts";
            case MENU_ABOUT:
                return "about";
        }
        return null;
    }
}
