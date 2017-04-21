package ru.zabbkit.android.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.Window;

import ru.zabbkit.android.app.Constants;
import ru.zabbkit.android.ui.fragments.GraphFragment;

/**
 * Created by Sergey.Tarasevich on 20.08.13.
 */
public class GraphActivity extends ActionBarActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeButtonEnabled(true);
		actionbar.setTitle(getIntent().getExtras().getString(
				Constants.PARAM_NAME));

		final FragmentManager fm = getSupportFragmentManager();
		Fragment f = fm.findFragmentByTag(GraphFragment.TAG);
		final FragmentTransaction ft = fm.beginTransaction();
		if (f == null) {
			f = GraphFragment.newInstance();
			f.setArguments(getIntent().getExtras());
			ft.add(android.R.id.content, f, GraphFragment.TAG);
		} else {
			ft.attach(f);
		}
		ft.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
}
