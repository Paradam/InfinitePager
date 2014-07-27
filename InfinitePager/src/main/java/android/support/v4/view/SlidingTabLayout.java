/*
 * Copyright (C) 2013 The Android Open Source Project
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

package android.support.v4.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.paradam.infinitepager.InfinitePagerAdapter;
import com.github.paradam.infinitepager.InfiniteViewPager;
import com.github.paradam.infinitepager.R;

/**
 * To be used with ViewPager to provide a tab indicator component which give constant feedback as to
 * the user's scroll progress.
 * <p>
 * To use the component, simply add it to your view hierarchy. Then in your
 * {@link android.app.Activity} or {@link android.support.v4.app.Fragment} call
 * {@link #setViewPager(android.support.v4.view.ViewPager)} providing it the ViewPager this layout is being used for.
 * <p>
 * The colors can be customized in two ways. The first and simplest is to provide an array of colors
 * via {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)}. The
 * alternative is via the {@link SlidingTabLayout.TabColorizer} interface which provides you complete control over
 * which color is used for any individual position.
 * <p>
 * The views used as tabs can be customized by calling {@link #setCustomTabView(int, int)},
 * providing the layout ID of your custom layout.
 *
 * Based off the developer.android.com sample code "SlidingTabsColors" (http://developer.android.com/samples/SlidingTabsColors/index.html)
 * with a few additions.
 */
public class SlidingTabLayout extends HorizontalScrollView implements SlidablePagerTitle, View.OnTouchListener, ViewPager.Decor {

    /**
     * Level of hidability of the TabLayout
     *
     * @see #HIDE_NONE
     * @see #HIDE_AUTO
     * @see #HIDE_PROGRAM
     */
    private int hidable = HIDE_NONE;

    /**
     * The PagerTitleStrip is currently collapsed and hidden at the top of the View.
     */
    private static final int CLOSED       = 0;
    /**
     * The PagerTitleStrip is currently sliding down to become visible to the user.
     */
    private static final int SLIDING_DOWN = 1;
    /**
     * The PagerTitleStrip is currently open and visible to the user.
     */
    private static final int OPEN         = 2;
    /**
     * The PagerTitleStrip is in the process of collapsing to be hidden from the user.
     */
    private static final int SLIDING_UP   = 3;

    /**
     * The state of the PagerTitleStrip.
     *
     * @see #CLOSED
     * @see #SLIDING_DOWN
     * @see #OPEN
     * @see #SLIDING_UP
     */
    private int slideState = OPEN;

    /**
     * A Runnable to be run after a delay that will trigger the PagerTitleStrip to collapse upwards
     * and hide from the user.
     */
    private final Runnable slideUp = new Runnable() {
        @Override
        public void run() {
            slideOut();
        }
    };

    /**
     * The id of the ViewPager this PagerTitleStrip is connected to.
     */
    private int mPagerId;

    /**
     * The duration the tabs will remain visible until they are hidden.
     *
     * Only honored if {@link #hidable} is set to {@link #HIDE_AUTO}. Ignored otherwise.
     */
    private long defaultDisplayTime = DISPLAY_TIME;

    private static final int[] ATTRS = new int[] {
            android.R.attr.textAppearance,
            android.R.attr.textSize,
            android.R.attr.textColor,
            android.R.attr.gravity,
            R.attr.viewPager,
            R.attr.autoHide,
            R.attr.dividerColors,
            R.attr.selectorColors,
            R.attr.displayDuration
    };

    /**
     * Allows complete control over the colors drawn in the tab layout. Set with
     * {@link #setCustomTabColorizer(SlidingTabLayout.TabColorizer)}.
     */
    public interface TabColorizer {

        /**
         * @return return the color of the indicator used when {@code position} is selected.
         */
        int getIndicatorColor(int position);

        /**
         * @return return the color of the divider drawn to the right of {@code position}.
         */
        int getDividerColor(int position);

    }

    /**
     * An observer that is informed when the data for the adapter changes.
     */
    private DataSetObserver dataChangeObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            populateTabStrip();
            SlidingTabLayout.this.post(new Runnable() {
                @Override
                public void run() {
                    scrollToTab(mViewPager instanceof InfiniteViewPager ? ((InfiniteViewPager) mViewPager).getRelativeCurrentItem() : mViewPager.getCurrentItem(), 0);
                }
            });
        }

        @Override
        public void onInvalidated() {
            populateTabStrip();
            SlidingTabLayout.this.post(new Runnable() {
                @Override
                public void run() {
                    scrollToTab(mViewPager instanceof InfiniteViewPager ? ((InfiniteViewPager) mViewPager).getRelativeCurrentItem() : mViewPager.getCurrentItem(), 0);
                }
            });
        }
    };

    private static final int TITLE_OFFSET_DIPS     = 24;
    private static final int TAB_VIEW_PADDING_DIPS = 16;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 12;

    private int mTitleOffset;

    private int mTabViewLayoutId;
    private int mTabViewTextViewId;

    private ViewPager mViewPager;
    private final PageListener mPageListener = new PageListener();

    private final SlidingTabStrip mTabStrip;

    private int mTextAppearance;
    private int mTextSize;
    private int mTextColor;
    private int mGravity;

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTabStrip = new SlidingTabStrip(context, attrs);
        init(context, attrs);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTabStrip = new SlidingTabStrip(context, attrs);
        init(context, attrs);
    }

    /**
     * Initialise the settings provided from the inflated XML.
     * @param context The context the layout was created in.
     * @param attrs The attribute set to get the settings from.
     */
    private void init(Context context, AttributeSet attrs) {
        // Disable the Scroll Bar
        setHorizontalScrollBarEnabled(false);
        // Make sure that the Tab Strips fills this View
        setFillViewport(true);

        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

        addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        final TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
        mTextAppearance = a.getResourceId(0, 0);
        mTextSize = a.getDimensionPixelSize(1, 0);
        if (a.hasValue(2)) {
            mTextColor = a.getColor(2, 0);
        }
        mGravity = a.getInteger(3, Gravity.BOTTOM);

        if (a.hasValue(4)) {
            mPagerId = a.getResourceId(4, 0);
        }

        if (a.hasValue(5)) {
            hidable = a.getInt(5, HIDE_NONE);
        }

        if (a.hasValue(6)) {
            setDividerColors(context.getResources().getIntArray(a.getResourceId(6, 0)));
        }

        if (a.hasValue(7)) {
            setSelectedIndicatorColors(context.getResources().getIntArray(a.getResourceId(7, 0)));
        }

        if (a.hasValue(8)) {
            defaultDisplayTime = a.getInteger(8, (int)DISPLAY_TIME);
        }
        a.recycle();

        setOnTouchListener(actionEventTouchListener);

        super.setBackgroundResource(0);
    }

    private OnTouchListener actionEventTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    SlidingTabLayout.this.removeCallbacks(slideUp);
                    if (hidable == HIDE_AUTO) {
                        SlidingTabLayout.this.postDelayed(slideUp, defaultDisplayTime);
                    }
            }
            return false;
        }
    };

    /**
     * @return The state of the Tab Layouts hidable status.
     *
     * @see #HIDE_NONE
     * @see #HIDE_AUTO
     * @see #HIDE_PROGRAM
     */
    public int isAutoHidable() {
        return hidable;
    }

    /**
     * Set the length in time the view will remain visible for.
     * @param displayTime The number of milliseconds to have the SlidingTabLayout visible for.
     */
    public void setDisplayTime(long displayTime) {
        defaultDisplayTime = displayTime;
    }

    /**
     * Get the number of milliseconds the SlidingTabLayout will remain visible for if {@link #isAutoHidable()}
     * equals {@link #HIDE_AUTO}.
     */
    public long getDisplayTime() {
        return defaultDisplayTime;
    }

    /**
     * Set if this TabLayout can auto hide to conserve less space.
     * <br />
     * Note: Setting this to {@link #HIDE_NONE} can lock the Tab Layout into an open or closed state.
     * @param autoHide The state of the Tab Layouts hidability. {@link #HIDE_NONE} for not hidable at all,
     *                 {@link #HIDE_PROGRAM} for hidable only when {@link #slideIn(long)} or {@link #slideOut(long)}
     *                 are called, or {@link #HIDE_AUTO} for automatic control.
     *
     * @see #HIDE_NONE
     * @see #HIDE_AUTO
     * @see #HIDE_PROGRAM
     */
    public void setAutoHidable(int autoHide) {
        hidable = autoHide;
        if (autoHide == HIDE_NONE) {
            removeCallbacks(slideUp);
        } else if (autoHide == HIDE_AUTO && (slideState == OPEN || slideState == SLIDING_DOWN)) {
            removeCallbacks(slideUp);
            postDelayed(slideUp, defaultDisplayTime);
        }
    }

    @TargetApi (Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void setBackground(Drawable background) {
        mTabStrip.setBackground(background);
    }

    @Override
    public void setBackgroundColor(int color) {
        mTabStrip.setBackgroundColor(color);
    }

    @Override
    public void setBackgroundResource(int resid) {
        mTabStrip.setBackgroundResource(resid);
    }

    /**
     * Set the custom {@link SlidingTabLayout.TabColorizer} to be used.
     *
     * If you only require simple custmisation then you can use
     * {@link #setSelectedIndicatorColors(int...)} and {@link #setDividerColors(int...)} to achieve
     * similar effects.
     */
    public void setCustomTabColorizer(TabColorizer tabColorizer) {
        mTabStrip.setCustomTabColorizer(tabColorizer);
    }

    /**
     * Sets the colors to be used for indicating the selected tab. These colors are treated as a
     * circular array. Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setSelectedIndicatorColors(int... colors) {
        mTabStrip.setSelectedIndicatorColors(colors);
    }

    /**
     * Sets the colors to be used for tab dividers. These colors are treated as a circular array.
     * Providing one color will mean that all tabs are indicated with the same color.
     */
    public void setDividerColors(int... colors) {
        mTabStrip.setDividerColors(colors);
    }

    /**
     * Set the custom layout to be inflated for the tab views.
     *
     * @param layoutResId Layout id to be inflated
     * @param textViewId id of the {@link android.widget.TextView} in the inflated view
     */
    public void setCustomTabView(int layoutResId, int textViewId) {
        mTabViewLayoutId = layoutResId;
        mTabViewTextViewId = textViewId;
    }

    /**
     * Sets the associated view pager. Note that the assumption here is that the pager content
     * (number of tabs and tab titles) does not change after this call has been made.
     */
    public void setViewPager(ViewPager viewPager) {
        if (viewPager == mViewPager) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.setInternalPageChangeListener(null);
            mViewPager.setOnAdapterChangeListener(null);
            mViewPager.setOnTouchListener(null);
            PagerAdapter adapter = mViewPager.getAdapter();
            if (adapter != null) {
                adapter.unregisterDataSetObserver(dataChangeObserver);
            }
        }
        mTabStrip.removeAllViews();
        mViewPager = viewPager;
        if (mViewPager == null) {
            return;
        }

        mViewPager.setInternalPageChangeListener(mPageListener);
        mViewPager.setOnAdapterChangeListener(mPageListener);
        mViewPager.setOnTouchListener(this);

        PagerAdapter adapter = mViewPager.getAdapter();
        if (adapter != null) {
            adapter.registerDataSetObserver(dataChangeObserver);
            populateTabStrip();
            SlidingTabLayout.this.post(new Runnable() {
                @Override
                public void run() {
                    scrollToTab(mViewPager instanceof InfiniteViewPager ? ((InfiniteViewPager) mViewPager).getRelativeCurrentItem() : mViewPager.getCurrentItem(), 0);
                }
            });
        }
        if (hidable == HIDE_AUTO) {
            slideState = OPEN;
            this.setVisibility(View.VISIBLE);
            this.postDelayed(slideUp, defaultDisplayTime);
        }
    }

    /**
     * Create a default view to be used for tabs. This is called if a custom tab view is not set via
     * {@link #setCustomTabView(int, int)}.
     */
    protected TextView createDefaultTabView(Context context) {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, mTextAppearance);
        textView.setGravity(mGravity > 0 ? mGravity : Gravity.CENTER);
        if (mTextSize > 0) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        } else {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
        }
        if (mTextColor != 0) {
            textView.setTextColor(mTextColor);
        }
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // If we're running on Honeycomb or newer, then we can use the Theme's
            // selectableItemBackground to ensure that the View has a pressed state
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            textView.setBackgroundResource(outValue.resourceId);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // If we're running on ICS or newer, enable all-caps to match the Action Bar tab style
            textView.setAllCaps(true);
        }

        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        lp.weight = 1f;
        textView.setLayoutParams(lp);

        return textView;
    }

    /**
     * Find and create a set of TextViews to insert into the TabStrip.
     */
    protected void populateTabStrip() {
        mTabStrip.removeAllViews();
        if (mViewPager != null) {
            final PagerAdapter adapter = mViewPager.getAdapter();
            if (adapter != null) {
                final OnClickListener tabClickListener = new TabClickListener();

                final boolean isInfinitePager = adapter instanceof InfinitePagerAdapter;
                int length = (isInfinitePager ? ((InfinitePagerAdapter) adapter).getRelativeCount() : adapter.getCount());
                for (int i = 0; i < length; i++) {
                    View tabView = null;
                    TextView tabTitleView = null;

                    if (mTabViewLayoutId != 0) {
                        // If there is a custom tab view layout id set, try and inflate it
                        tabView = LayoutInflater.from(getContext()).inflate(mTabViewLayoutId, mTabStrip, false);
                        tabTitleView = (TextView) tabView.findViewById(mTabViewTextViewId);
                    }

                    if (tabView == null) {
                        tabView = createDefaultTabView(getContext());
                    }

                    if (tabTitleView == null && TextView.class.isInstance(tabView)) {
                        tabTitleView = (TextView) tabView;
                    }

                    if (tabTitleView != null) {
                        tabTitleView.setGravity(Gravity.CENTER);
                        tabTitleView.setText(isInfinitePager ? ((InfinitePagerAdapter) adapter).getRelativePageTitle(i) : adapter.getPageTitle(i));
                    }
                    tabView.setOnClickListener(tabClickListener);
                    tabView.setOnTouchListener(actionEventTouchListener);

                    mTabStrip.addView(tabView);
                }
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ViewParent parent = getParent();
        if (mPagerId != 0) {
            View related = ((ViewGroup)parent).findViewById(mPagerId);
            if (!(related instanceof ViewPager)) {
                throw new IllegalStateException(
                        "ViewPager ID is does not refer to ViewPager.");
            }
            parent = (ViewParent) related;
        } else if (!(parent instanceof ViewPager)) {
            // Parent has not been set, will wait for #setViewPager(ViewPager) to be called instead.
            return;
        }

        setViewPager((ViewPager) parent);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setViewPager(null);
    }

    private void scrollToTab(int tabIndex, int positionOffset) {
        final int tabStripChildCount = mTabStrip.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }
        View selectedChild = mTabStrip.getChildAt(tabIndex);
        if (selectedChild != null && (float)positionOffset / selectedChild.getWidth() > 0.5 && tabIndex == tabStripChildCount -1) {
            tabIndex = 0;
            positionOffset = 0;
        }
        selectedChild = mTabStrip.getChildAt(tabIndex);
        if (selectedChild != null) {
            int targetScrollX = selectedChild.getLeft() + positionOffset;

            if (tabIndex > 0 || positionOffset > 0) {
                // If we're not at the first child and are mid-scroll, make sure we obey the offset
                targetScrollX -= mTitleOffset;
            }

            scrollTo(targetScrollX, 0);
        }
    }

    private class TabClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (v == mTabStrip.getChildAt(i)) {
                    if (mViewPager instanceof InfiniteViewPager) {
                        ((InfiniteViewPager)mViewPager).setRelativeCurrentItem(i);
                    } else {
                        mViewPager.setCurrentItem(i);
                    }
                    return;
                }
            }
        }
    }


    /**
     * Slide up the view to remove it from the frame.
     */
    private void slideOut() {
        if (slideState == OPEN) {
            slideState = SLIDING_UP;
            Animation slideOut = AnimationUtils.loadAnimation(this.getContext(), R.anim.abc_slide_out_top);
            slideOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    if (slideState != SLIDING_UP) {
                        slideState = CLOSED;
                        slideIn();
                    } else {
                        slideState = CLOSED;
                    }
                    SlidingTabLayout.this.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            this.startAnimation(slideOut);
        }
    }

    /**
     * Slide down the view so that its contents are visible and can be interacted with.
     */
    private void slideIn() {
        if (slideState == CLOSED) {
            slideState = SLIDING_DOWN;
            Animation slideIn = AnimationUtils.loadAnimation(this.getContext(), R.anim.abc_slide_in_top);
            slideIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    slideState = OPEN;
                }

                @Override
                public void onAnimationStart(Animation animation) {
                    SlidingTabLayout.this.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            this.startAnimation(slideIn);
        }
    }

    /**
     * The last position of the pointer in the Y axis.
     */
    private float downPositionY = -1;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (hidable == HIDE_AUTO) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downPositionY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (downPositionY + MOVE_THRESHOLD < event.getY()) {
                        if (slideState == CLOSED) {
                            slideIn(defaultDisplayTime);
                        }
                    } else if (downPositionY - MOVE_THRESHOLD > event.getY()) {
                        if (slideState == OPEN || slideState == SLIDING_DOWN) {
                            slideOut(0);
                        }
                    }
                    break;
            }
        }
        return false;
    }

    @Override
    public void slideIn(long milliseconds) {
        if (hidable == HIDE_NONE) { return; }
        if (slideState == CLOSED) {
            slideIn();
        } else {
            slideState = OPEN;
        }
        removeCallbacks(slideUp);
        if (milliseconds >= 0 && hidable == HIDE_AUTO) {
            postDelayed(slideUp, milliseconds);
        }
    }

    @Override
    public void slideOut(long milliseconds) {
        if (hidable == HIDE_NONE) { return; }
        if (slideState == CLOSED || slideState == SLIDING_UP) { return; }
        removeCallbacks(slideUp);
        slideState = OPEN;
        if (milliseconds > 0) {
            postDelayed(slideUp, milliseconds);
        } else {
            post(slideUp);
        }
    }

    private class PageListener extends DataSetObserver implements ViewPager.OnPageChangeListener,
            ViewPager.OnAdapterChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (hidable == HIDE_AUTO) {
                if (slideState == CLOSED) {
                    slideIn();
                    SlidingTabLayout.this.removeCallbacks(slideUp);
                    if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                        SlidingTabLayout.this.postDelayed(slideUp, DISPLAY_TIME_SHORT);
                    }
                } else if (slideState == SLIDING_UP) {
                    slideState = OPEN;
                } else {
                    SlidingTabLayout.this.removeCallbacks(slideUp);
                    SlidingTabLayout.this.postDelayed(slideUp, DISPLAY_TIME_SHORT);
                }
            }

            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onViewPagerPageChanged(position, positionOffset);

            View selectedTitle = mTabStrip.getChildAt(position);
            int extraOffset = (selectedTitle != null)
                              ? (int) (positionOffset * selectedTitle.getWidth())
                              : 0;
            scrollToTab(position, extraOffset);
        }

        @Override
        public void onPageSelected(int position) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                mTabStrip.onViewPagerPageChanged(position, 0f);
                scrollToTab(position, 0);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;
        }

        @Override
        public void onAdapterChanged(PagerAdapter oldAdapter, PagerAdapter newAdapter) {
            if (oldAdapter != null) {
                oldAdapter.unregisterDataSetObserver(dataChangeObserver);
            }
            if (newAdapter != null ) {
                newAdapter.registerDataSetObserver(dataChangeObserver);
            }
            populateTabStrip();
        }

        @Override
        public void onChanged() {
            populateTabStrip();
        }
    }
}
