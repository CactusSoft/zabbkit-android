package ru.zabbkit.android.ui.views;

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TabHost;

import java.util.ArrayList;

/**
 *
 * 
 *      Special TabHost that allows the use of {@link Fragment} objects for its
 *      tab content. When placing this in a view hierarchy, after inflating the
 *      hierarchy you must call {@link #setup(Context, FragmentManager, int)} to
 *      complete the initialization of the tab host.
 * 
 *      <p>
 *      Here is a simple example of using a FragmentTabHost in an Activity:
 * 
 *      {@sample
 *      development/samples/Support4Demos/src/com/example/android/supportv4/app/
 *      FragmentTabs.java complete}
 * 
 *      <p>
 *      This can also be used inside of a fragment through fragment nesting:
 * 
 *      {@sample
 *      development/samples/Support4Demos/src/com/example/android/supportv4/app/
 *      FragmentTabsFragmentSupport.java complete}
 */
public class FixedFragmentTabHost extends TabHost implements
		TabHost.OnTabChangeListener {

	private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
	private FrameLayout mRealTabContent;
	private Context mContext;
	private FragmentManager mFragmentManager;
	private int mContainerId;
	private TabHost.OnTabChangeListener mOnTabChangeListener;
	private TabInfo mLastTab;
	private boolean mAttached;

	private boolean mIsRetanedFragments = false;

	static final class TabInfo {
		private final String tag;
		private final Class<?> clss;
		private final Bundle args;
		private Fragment fragment;

		TabInfo(final String tag, final Class<?> clss, final Bundle args) {
			this.tag = tag;
			this.clss = clss;
			this.args = args;
		}
	}

	static class DummyTabFactory implements TabHost.TabContentFactory {
		private final Context mContext;

		public DummyTabFactory(final Context context) {
			mContext = context;
		}

		@Override
		public View createTabContent(final String tag) {
			View v = new View(mContext);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}
	}

	static class SavedState extends BaseSavedState {
		String curTab;

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(final Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(final int size) {
				return new SavedState[size];
			}
		};

		SavedState(final Parcelable superState) {
			super(superState);
		}

		private SavedState(final Parcel in) {
			super(in);
			curTab = in.readString();
		}

		@Override
		public void writeToParcel(final Parcel out, final int flags) {
			super.writeToParcel(out, flags);
			out.writeString(curTab);
		}

		@Override
		public String toString() {
			return "FragmentTabHost.SavedState{"
					+ Integer.toHexString(System.identityHashCode(this))
					+ " curTab=" + curTab + "}";
		}
	}

	public FixedFragmentTabHost(final Context context) {
		// Note that we call through to the version that takes an AttributeSet,
		// because the simple Context construct can result in a broken object!
		super(context, null);
		initFragmentTabHost(context, null);
	}

	public FixedFragmentTabHost(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		initFragmentTabHost(context, attrs);
	}

	private void initFragmentTabHost(final Context context,
			final AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs,
				new int[] { android.R.attr.inflatedId }, 0, 0);
		mContainerId = a.getResourceId(0, 0);
		a.recycle();

		super.setOnTabChangedListener(this);

		/*****
		 * HERE COMMENT CODE BECAUSE findViewById(android.R.id.tabs) EVERY TIME
		 * IS NULL WE HAVE OWN LAYOUT
		 ******/
	}

	/**
	 * @deprecated Don't call the original TabHost setup, you must instead call
	 *             {@link #setup(Context, FragmentManager)} or
	 *             {@link #setup(Context, FragmentManager, int)}.
	 */
	@Override
	@Deprecated
	public final void setup() {
		throw new IllegalStateException(
				"Must call setup() that takes a Context and FragmentManager");
	}

	public final void setup(final Context context, final FragmentManager manager) {
		super.setup();
		mContext = context;
		mFragmentManager = manager;
		ensureContent();
	}

	public void setup(final Context context, final FragmentManager manager,
			final int containerId) {
		super.setup();
		mContext = context;
		mFragmentManager = manager;
		mContainerId = containerId;
		ensureContent();
		mRealTabContent.setId(containerId);

		// We must have an ID to be able to save/restore our state. If
		// the owner hasn't set one at this point, we will set it ourself.
		if (getId() == View.NO_ID) {
			setId(android.R.id.tabhost);
		}
	}

	/**
	 * Added by Dmitry.Kalenchuk for
	 * 
	 * @param context
	 * @param manager
	 * @param containerId
	 */
	public final void setup(final Context context,
			final FragmentManager manager, final int containerId,
			final boolean isREtainedFragments) {
		setup(context, manager, containerId);
		mIsRetanedFragments = isREtainedFragments;
	}

	private void ensureContent() {
		if (mRealTabContent == null) {
			mRealTabContent = (FrameLayout) findViewById(mContainerId);
			if (mRealTabContent == null) {
				throw new IllegalStateException(
						"No tab content FrameLayout found for id "
								+ mContainerId);
			}
		}
	}

	@Override
	public final void setOnTabChangedListener(final OnTabChangeListener l) {
		mOnTabChangeListener = l;
	}

	public final void addTab(final TabHost.TabSpec tabSpec,
			final Class<?> clss, final Bundle args) {
		tabSpec.setContent(new DummyTabFactory(mContext));
		String tag = tabSpec.getTag();

		TabInfo info = new TabInfo(tag, clss, args);

		if (mAttached) {
			// If we are already attached to the window, then check to make
			// sure this tab's fragment is inactive if it exists. This shouldn't
			// normally happen.
			info.fragment = mFragmentManager.findFragmentByTag(tag);
			if (info.fragment != null && !info.fragment.isDetached()) {
				FragmentTransaction ft = mFragmentManager.beginTransaction();
				ft.detach(info.fragment);
				ft.commit();
			}
		}

		mTabs.add(info);
		addTab(tabSpec);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		String currentTab = getCurrentTabTag();

		// Go through all tabs and make sure their fragments match
		// the correct state.
		FragmentTransaction ft = null;
		for (int i = 0; i < mTabs.size(); i++) {
			TabInfo tab = mTabs.get(i);
			tab.fragment = mFragmentManager.findFragmentByTag(tab.tag);
			if (tab.fragment != null && !tab.fragment.isDetached()) {
				if (tab.tag.equals(currentTab)) {
					// The fragment for this tab is already there and
					// active, and it is what we really want to have
					// as the current tab. Nothing to do.
					mLastTab = tab;
				} else {
					// This fragment was restored in the active state,
					// but is not the current tab. Deactivate it.
					if (ft == null) {
						ft = mFragmentManager.beginTransaction();
					}
					ft.detach(tab.fragment);
				}
			}
		}

		// We are now ready to go. Make sure we are switched to the
		// correct tab.
		mAttached = true;
		ft = doTabChanged(currentTab, ft);
		if (ft != null) {
			ft.commit();
			mFragmentManager.executePendingTransactions();
		}
	}

	@Override
	protected final void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mAttached = false;
	}

	@Override
	protected final Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.curTab = getCurrentTabTag();
		return ss;
	}

	@Override
	protected final void onRestoreInstanceState(final Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		setCurrentTabByTag(ss.curTab);
	}

	@Override
	public final void onTabChanged(final String tabId) {
		if (mAttached) {
			FragmentTransaction ft = doTabChanged(tabId, null);
			if (ft != null) {
				ft.commit();
			}
		}
		if (mOnTabChangeListener != null) {
			mOnTabChangeListener.onTabChanged(tabId);
		}
	}

	private FragmentTransaction doTabChanged(final String tabId,
			FragmentTransaction ft) {
		TabInfo newTab = null;
		for (int i = 0; i < mTabs.size(); i++) {
			TabInfo tab = mTabs.get(i);
			if (tab.tag.equals(tabId)) {
				newTab = tab;
			}
		}
		if (newTab == null) {
			throw new IllegalStateException("No tab known for tag " + tabId);
		}
		FragmentTransaction fragmentTransaction = ft;
		if (!newTab.equals(mLastTab)) {
			if (fragmentTransaction == null) {
				fragmentTransaction = mFragmentManager.beginTransaction();
			}
			if (mLastTab != null) {
				if (mLastTab.fragment != null) {
					fragmentTransaction.detach(mLastTab.fragment);
				}
			}
			if (newTab.fragment == null) {
				newTab.fragment = Fragment.instantiate(mContext,
						newTab.clss.getName(), newTab.args);
				newTab.fragment.setRetainInstance(mIsRetanedFragments);
				fragmentTransaction.add(mContainerId, newTab.fragment,
						newTab.tag);
			} else {
				fragmentTransaction.attach(newTab.fragment);
			}
			mLastTab = newTab;
		}
		return fragmentTransaction;
	}

    public int getTabsCount(){
        return mTabs.size();
    }
}