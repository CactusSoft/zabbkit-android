package ru.zabbkit.android.ui.views;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabWidget;

import ru.zabbkit.android.R;

public class ScrollableFragmentTabHost extends FixedFragmentTabHost implements
		View.OnTouchListener{

	private TabWidget mTabBar;

	private GestureDetector mGestureDetector;

	public ScrollableFragmentTabHost(final Context context) {
		super(context);
		init(context);
	}

	public ScrollableFragmentTabHost(final Context context,
			final AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(final Context context) {
		mGestureDetector = new GestureDetector(context, new GestureListener());
	}

	@Override
	protected final void onAttachedToWindow() {
		super.onAttachedToWindow();
		mTabBar = (TabWidget) findViewById(android.R.id.tabs);
		// set custom divider work incorrect on version <= 3.0
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mTabBar.setDividerDrawable(R.color.divider);
		}
	}

	@Override
	public final boolean onTouch(final View v, final MotionEvent event) {
		mGestureDetector.onTouchEvent(event);
		return false;
	}

    /*
	@Override
	public void onPullEvent(PullToRefreshBase<ListView> refreshView,
			State state, Mode direction) {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabBar
				.getLayoutParams();
		assert lp != null;
		if (state == PullToRefreshBase.State.PULL_TO_REFRESH
				&& (lp.topMargin < 0 || mTabBar.getVisibility() != View.VISIBLE)) {
			lp.topMargin = 0;
			mTabBar.setVisibility(View.VISIBLE);
			requestLayout();
		}
	}
	*/

	private class GestureListener extends
			GestureDetector.SimpleOnGestureListener {

		private static final int SHOW_HIDDEN_TABBAR_VELOCITY = 2000;

		@Override
		public boolean onFling(final MotionEvent e1, final MotionEvent e2,
				final float velocityX, final float velocityY) {
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabBar
					.getLayoutParams();
			assert lp != null;
			if (velocityY > SHOW_HIDDEN_TABBAR_VELOCITY
					&& (lp.topMargin < 0 || mTabBar.getVisibility() != View.VISIBLE)) {
				lp.topMargin = 0;
				mTabBar.setVisibility(View.VISIBLE);
				mTabBar.requestLayout();
			} else if (velocityY < 0 && mTabBar.getVisibility() == View.VISIBLE) {
				mTabBar.setVisibility(View.GONE);
			}
			return false;
		}

		@Override
		public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
				final float distanceX, float distanceY) {
			// catch scroll to bottom
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTabBar
					.getLayoutParams();
			assert lp != null;
			if (mTabBar.getVisibility() == View.VISIBLE) {
				if (distanceY > 0 && e1.getY() > e2.getY()) {
					lp.topMargin -= Math.round(distanceY);
					if (mTabBar.getHeight() <= -lp.topMargin) {
						mTabBar.setVisibility(View.GONE);
					} else {
						mTabBar.requestLayout();
					}
				}
				// following code can cause scroll lags (strange list jumps)
				// else if (distanceY < 0 && e1.getY() < e2.getY() &&
				// lp.topMargin < 0) {
				// lp.topMargin = lp.topMargin - Math.round(distanceY);
				// if (lp.topMargin > 0) {
				// lp.topMargin = 0;
				// }
				// mTabBar.setLayoutParams(lp);
				// mTabBar.requestLayout();
				// }
			}
			return false;
		}
	}
}
