package ru.zabbkit.android.ui.layout;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.zabbkit.android.R;

/**
 * Created by Alex.Shimborsky on 22/12/2014.
 */
public class EventsListItemLayout extends LinearLayout
        implements WearableListView.OnCenterProximityListener {

    private ImageView mCircle;
    private TextView mName;

    private final float mFadedTextAlpha;
    private final int mFadedCircleColor;
    private final int mChosenCircleColor;

    public EventsListItemLayout(Context context) {
        this(context, null);
    }

    public EventsListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventsListItemLayout(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);

        mFadedTextAlpha = 0.2f;
        mFadedCircleColor = getResources().getColor(R.color.grey);
        mChosenCircleColor = getResources().getColor(R.color.blue);
    }

    // Get references to the icon and text in the item layout definition
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // These are defined in the layout file for list items
        // (see next section)
        mCircle = (ImageView) findViewById(R.id.circle);
        mName = (TextView) findViewById(R.id.name);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        mName.setAlpha(1f);
        //((GradientDrawable) mCircle.getDrawable()).setColor(mChosenCircleColor);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        //((GradientDrawable) mCircle.getDrawable()).setColor(mFadedCircleColor);
        mName.setAlpha(mFadedTextAlpha);
    }
}