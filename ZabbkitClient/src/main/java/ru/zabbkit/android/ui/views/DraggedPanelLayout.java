package ru.zabbkit.android.ui.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import ru.zabbkit.android.R;
import ru.zabbkit.android.utils.L;

/**
 * @author Sergey Tarasevich on 12.07.2013.
 */
public class DraggedPanelLayout extends FrameLayout {

	private static DecelerateInterpolator sDecelerator = new DecelerateInterpolator();

	private int panelPeekHeight;
	private int panelHeight;

	private float touchY;
	private boolean touching;
	private boolean opened;
	private VelocityTracker velocityTracker;

	private View slidingPanel;
	private View bottomPanel;
	private View pullEarLayout;
	private View pullEarView;

	private int touchSlop;
	private boolean isBeingDragged;

	// for API < 11
	private float startY;
	private boolean isSliding;

	public DraggedPanelLayout(Context context) {
		super(context);
		init(context);
	}

	public DraggedPanelLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DraggedPanelLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		setWillNotDraw(true);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		if (getChildCount() != 2) {
			throw new IllegalStateException(
					"DraggedPanelLayout must have 2 children!");
		}

		slidingPanel = getChildAt(0);
		bottomPanel = getChildAt(1);

		pullEarLayout = slidingPanel.findViewById(R.id.layout_pull_ear);
		pullEarView = pullEarLayout.findViewById(R.id.pull_ear);
		pullEarLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					finishAnimateToFinalPosition(opened ? -1 : 1);
				} else {
					opened = !opened;
					requestLayout();
					bottomPanel.post(new Runnable() { // need to re-layout
														// pull-to-refresh view
								@Override
								public void run() {
									bottomPanel.requestLayout();
								}
							});
				}
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		L.d("onLayout(%b, %d, %d, %d, %d)", changed, left, top, right, bottom);

		if (panelPeekHeight == 0) { // init panelPeekHeight once
			panelPeekHeight = pullEarLayout.getMeasuredHeight();
		}
		if (panelHeight == 0) {
			panelHeight = slidingPanel.getMeasuredHeight();
		}

		if (opened) {
			int panelMeasuredHeight = slidingPanel.getMeasuredHeight();
			slidingPanel.layout(left, top, right, top + panelMeasuredHeight);
		} else {
			int panelMeasuredHeight = slidingPanel.getMeasuredHeight();
			slidingPanel.layout(left, top - panelMeasuredHeight
					+ panelPeekHeight, right, top + panelPeekHeight);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			bottomPanel.layout(left, slidingPanel.getBottom()
					+ (int) slidingPanel.getTranslationY(), right, bottom);
		} else {
			bottomPanel.layout(left, slidingPanel.getBottom(), right, bottom);
		}
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			touchY = event.getY();
			float touchX = event.getX();
			if (((opened && touchY <= slidingPanel.getBottom() + touchSlop && touchY >= slidingPanel
					.getBottom() - panelPeekHeight - touchSlop) || (!opened && touchY <= slidingPanel
					.getBottom() + touchSlop))
					&& touchX >= pullEarView.getLeft() - touchSlop
					&& touchX <= pullEarView.getRight() + touchSlop) { // do we
																		// start
																		// touch
																		// pull
																		// ear
																		// view?
				startY = touchY;
				isSliding = true;
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE
				&& touchY < slidingPanel.getBottom()) {
			if (Math.abs(touchY - event.getY()) > touchSlop) {
				isBeingDragged = true;
				startDragging(event);
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			isBeingDragged = false;
		}

		return isBeingDragged;
	}

	private void startDragging(MotionEvent event) {
		touchY = event.getY();
		touching = true;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			obtainVelocityTracker();
			velocityTracker.addMovement(event);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (touchY < slidingPanel.getBottom()) {
				obtainVelocityTracker();

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					startDragging(event);
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (touching) {
						velocityTracker.addMovement(event);

						float translation = event.getY() - touchY;
						translation = boundTranslation(translation);

						slidingPanel.setTranslationY(translation);
						bottomPanel.setTop(slidingPanel.getBottom()
								+ (int) slidingPanel.getTranslationY());
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					isBeingDragged = false;
					touching = false;

					velocityTracker.addMovement(event);
					velocityTracker.computeCurrentVelocity(1);
					float velocityY = velocityTracker.getYVelocity();
					velocityTracker.recycle();
					velocityTracker = null;

					finishAnimateToFinalPosition(velocityY);
				}

				return true;
			}
		} else {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				isBeingDragged = false;
				touching = false;

				if (isSliding) {
					if ((opened && startY > event.getY())
							|| (!opened && startY < event.getY())) {
						opened = !opened;
						requestLayout();

						bottomPanel.post(new Runnable() { // need to re-layout
															// pull-to-refresh
															// view
									@Override
									public void run() {
										bottomPanel.requestLayout();
									}
								});
					}
					isSliding = false;
				}
			}
			return true;
		}
		return false;
	}

	private float boundTranslation(float translation) {
		float boundTranslation = translation;
		if (opened) {
			if (boundTranslation > 0) {
				boundTranslation = 0;
			}
			if (Math.abs(boundTranslation) >= slidingPanel.getMeasuredHeight()
					- panelPeekHeight) {
				boundTranslation = -slidingPanel.getMeasuredHeight()
						+ panelPeekHeight;
			}
		} else {
			if (boundTranslation < 0) {
				boundTranslation = 0;
			}
			if (boundTranslation >= slidingPanel.getMeasuredHeight()
					- panelPeekHeight) {
				boundTranslation = slidingPanel.getMeasuredHeight()
						- panelPeekHeight;
			}
		}
		return boundTranslation;
	}

	private void obtainVelocityTracker() {
		if (velocityTracker == null) {
			velocityTracker = VelocityTracker.obtain();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void finishAnimateToFinalPosition(float velocityY) {
		L.d("finishAnimateToFinalPosition(%s)", velocityY);

		final boolean flinging = Math.abs(velocityY) > 0.5;

		boolean opening;
		float distY;
		long duration;

		if (flinging) {
			// If fling velocity is fast enough we continue the motion starting
			// with the current speed
			opening = velocityY > 0;

			distY = calculateDistance(opening);
			duration = Math.abs(Math.round(distY / velocityY));
		} else {
			// If user motion is slow or stopped we check if half distance is
			// traveled and based on that complete the motion
			boolean halfway = Math.abs(slidingPanel.getTranslationY()) >= (panelHeight - panelPeekHeight) / 2.0;
			opening = opened ? !halfway : halfway;

			distY = calculateDistance(opening);
			duration = Math.round(300
					* Math.abs(slidingPanel.getTranslationY())
					/ (panelHeight - panelPeekHeight));
		}

		animatePanel(opening, distY, duration);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public float calculateDistance(boolean opening) {
		float distY;
		if (opened) {
			distY = opening ? -slidingPanel.getTranslationY() : -(panelHeight
					- panelPeekHeight + slidingPanel.getTranslationY());
		} else {
			distY = opening ? (panelHeight - panelPeekHeight - slidingPanel
					.getTranslationY()) : -slidingPanel.getTranslationY();
		}

		return distY;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void animatePanel(final boolean opening, float distY, long duration) {
		ObjectAnimator slidingPanelAnimator = ObjectAnimator.ofFloat(
				slidingPanel, View.TRANSLATION_Y,
				slidingPanel.getTranslationY(), slidingPanel.getTranslationY()
						+ distY);
		ObjectAnimator bottomPanelAnimator = ObjectAnimator
				.ofInt(bottomPanel, "top", bottomPanel.getTop(),
						bottomPanel.getTop() + (int) distY);

		AnimatorSet set = new AnimatorSet();
		set.playTogether(slidingPanelAnimator, bottomPanelAnimator);
		set.setDuration(duration);
		set.setInterpolator(sDecelerator);
		set.addListener(new MyAnimListener(opening));
		set.start();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	class MyAnimListener implements AnimatorListener {

		int oldLayerTypeOne;
		int oldLayerTypeTwo;

		boolean opening;

		public MyAnimListener(boolean opening) {
			super();
			this.opening = opening;
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void onAnimationStart(Animator animation) {
			oldLayerTypeOne = slidingPanel.getLayerType();
			oldLayerTypeTwo = bottomPanel.getLayerType();

			slidingPanel.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			bottomPanel.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		}

		@Override
		public void onAnimationRepeat(Animator animation) { // Do nothing
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			setOpenedState(opening);

			bottomPanel.setTranslationY(0);
			slidingPanel.setTranslationY(0);

			slidingPanel.setLayerType(oldLayerTypeOne, null);
			bottomPanel.setLayerType(oldLayerTypeTwo, null);

			requestLayout();
		}

		@Override
		public void onAnimationCancel(Animator animation) { // Do nothing
		}
	}

	private void setOpenedState(boolean opened) {
		this.opened = opened;
	}

	public boolean isOpened() {
		return opened;
	}

	public void setOpened(boolean opened) {
		if (this.opened != opened) {
			this.opened = opened;
			requestLayout();
		}
	}
}