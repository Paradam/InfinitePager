/*
 * Copyright 2013 Adam Parr
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.paradam.infinitepager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;

/**
 * A ViewPager that handles simulating an infinite scrolling list of pages when
 * used in conjunction with an InfinitePagerAdapter. This implementation will
 * only accept a {@link android.support.v4.app.PagerAdapter} that is an instance
 * of {@link InfinitePagerAdapter} as any other instance of PagerAdapter could
 * have undefined behaviour.
 * 
 * <p>This implementation works by listening for {@link OnPageChangeListener} events
 * before passing the call onto any attached OnPageChangeListener objects.
 * Upon receiving an event it checks to see if the page being changed to is beyond
 * the limits of the total number of pages as provided by 
 * {@link InfinitePagerAdapter#getRelativeCount()}, if the page is outside of
 * this range, then the InfinteViewPager will switch to the correct position
 * within the ViewPager once the animation has finished, or when the user begins
 * another drag event (whichever comes first).</p>
 * 
 * <p>InfiniteViewPager is similar to a normal ViewPager except it is limited to the
 * type of PagerAdapter it can accept and for interacting with an InfinitePagerAdapter
 * the use of {@link #getRelativeCurrentItem()}, {@link #setRelativeCurrentItem(int)}
 * and {@link #setRelativeCurrentItem(int, boolean)} is recommended over using their
 * respective methods found in {@link ViewPager}. Otherwise use like for like to keep
 * consistency between an InfiniteViewPager and InfinitePagerAdapter.</p>
 * 
 * <p>Layout manager that allows the user to flip left and right
 * through pages of data.  You supply an implementation of a
 * {@link InfinitePagerAdapter} to generate the pages that the view shows.</p>
 *
 * <p>InfiniteViewPager is most often used in conjunction with {@link android.app.Fragment},
 * which is a convenient way to supply and manage the lifecycle of each page.
 * There are standard adapters implemented for using fragments with the InfiniteViewPager,
 * which cover the most common use cases.  These are
 * {@link com.github.paradam.infinitepager.InfiniteFragmentPagerAdapter} and 
 * {@link com.github.paradam.infinitepager.InfiniteFragmentStatePagerAdapter} for applications
 * targeting API 13 and greater or for applications using the v4 support package
 * {@link com.github.paradam.support.v4.infinitepager.InfiniteFragmentPagerAdapter} and 
 * {@link com.github.paradam.support.v4.infinitepager.InfiniteFragmentStatePagerAdapter};

 * @author Adam Parr
 *
 */
public class InfiniteViewPager extends ViewPager {
	
	/**
	 * A reference to the infinitePageAdapter passed to the super class.
	 */
	private InfinitePagerAdapter infinitePageAdapter;
	
	/**
	 * The external OnPageChangeListener
	 */
	private OnPageChangeListener mOnPageChangeListener;
	
	/**
	 * Constructor that accepts the context in which the ViewPager resides in.
	 * @param context The Context.
	 */
	public InfiniteViewPager(Context context) {
		super(context);
		init();
	}
	
	/**
	 * Constructor that accepts the context in which the ViewPager resides in and any attributes.
	 * @param context The Context.
	 * @param attrs AttributeSet
	 */
	public InfiniteViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	/**
	 * Initialise the InfiniteViewPager.
	 */
	private void init() {
		/*
		 * Set up listener to listen for the moments when the current selected page has been changed
		 * to a page less than InfinitePageAdapter#MARGIN, or larger than
		 * InfinitePageAdapter#getRelativeCount()+ InfinitePageAdapter#MARGIN
		 */
		super.setOnPageChangeListener(new OnPageChangeListener(){
			/**
			 * The page which the {@link #pageScrollThread} will scroll to when the thread is run.<br>
			 * A value of -1 means no page has been set since the last time #pageScrollRunner was run.
			 */
			private int toPage = -1;
			
			/**
			 * A Runnable that will set the Page Adapter {@link #mSectionsPagerAdapter} to the page
			 * identified in {@link #toPage}, before then setting toPage to <code>-1</code>.
			 */
			private final Runnable pageScrollRunner = new Runnable() {
				public synchronized void run() {
					if (toPage>=0) {
						/*
						 * Switch to the real page, don't animate transition to make it
						 * seem that the page is not changed.
						 */
						setRelativeCurrentItem(toPage, false);
						toPage=-1;
					}
				}
			};
			
			@Override
			public void onPageScrollStateChanged(int state){
				switch (state) {
					case ViewPager.SCROLL_STATE_DRAGGING:
						/*
						 *  User has started dragging, quickly change the page to the correct one,
						 *  this may cause the view to jump.
						 */
					case ViewPager.SCROLL_STATE_IDLE: // Animation has finished, switch now.
						if(toPage>=0) {
							post(pageScrollRunner);
						}
				}
				
				if(mOnPageChangeListener!=null){
					mOnPageChangeListener.onPageScrollStateChanged(state);
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){
				if(mOnPageChangeListener!=null){
					mOnPageChangeListener.onPageScrolled(calculateRelative(position), positionOffset, positionOffsetPixels);
				}
			}
			
			@Override
			public void onPageSelected(int position) {
				int calcPos = calculateRelative(position);
				if(position-infinitePageAdapter.getMargin() != calcPos) {
					/*
					 *  If the given position differs from the real position calculated.
					 *  Set #toPage to the page it should be on (if position is one that is
					 *  less than InfiniteViewPager#getMargin()
					 *  or larger than (InfinitePageAdapter#getRelativeCount() +
					 *  InfinitePageAdapter#getMargin()), then store the correct page 
					 *  it should change to when the animation stops to make as smooth
					 *  transition as possible.
					 */
					toPage=calcPos;
				}
				
				if(mOnPageChangeListener!=null){mOnPageChangeListener.onPageSelected(toPage);}
			}
		});
	}
	
	/**
	 * Calculates the relative position of the current page, where page at 
	 * <code>{@link InfinitePagerAdapter#getMargin()}</code> maps to the relative position
	 * of <code>0</code>, and page at <code>{@link InfinitePagerAdapter#getCount()} - 
	 * {@link InfinitePagerAdapter#getMargin()}</code> will map to the relative position
	 * of <code>{@link InfinitePagerAdapter#getRelativeCount()} -1</code>.
	 * @param position The absolute position of the page.
	 * @return The relative position of the page.
	 */
	private int calculateRelative(int position) {
		// Get the number of real pages.
		int length = infinitePageAdapter.getRelativeCount();
		// Calculate the real page the given position should be.
		return length > 0 ? (position-infinitePageAdapter.getMargin()+length)%length : 0;
	}
	
	/**
	 * Get the index of the currently selected page.
	 * @return An integer of the current pages position.
	 */
	public int getRelativeCurrentItem() {
		return calculateRelative(getCurrentItem());
	}

	/**
	 * Change the page of the ViewPager, changes to a item between <code>0</code> and 
	 * {@link InfinitePagerAdapter#getRelativeCount()}.
	 * @param item The item to switch to.
	 */
	public void setRelativeCurrentItem(int item) {
		setRelativeCurrentItem(item,true);
	}
	
	/**
	 * Change the page of the ViewPager, changes to an item between <code>0</code> and 
	 * {@link InfinitePagerAdapter#getRelativeCount()}.
	 * @param item The item to switch to.
	 * @param smoothScroll <tt>true</tt> to animate the scrolling to the page, <tt>false</tt>
	 * to switch directly to the page.
	 */
	public void setRelativeCurrentItem(int item, boolean smoothScroll) {
		int count = infinitePageAdapter.getRelativeCount();
		super.setCurrentItem((count>0?(item%count)+infinitePageAdapter.getMargin():0),smoothScroll);
	}
	
	/**
	 * Set a PagerAdapter that will supply views for this pager as needed.
	 *
	 * @param adapter Adapter to use
	 */
	public void setAdapter(InfinitePagerAdapter infinitePagerAdapter) {
		setAdapter(infinitePagerAdapter,0); // Set the pager adapter to the real page 0.
		// If the ViewPager behaves as an infinite scrolling list, then what would
		// be considered the 0 page is not in the 0 index position.
	}
	
	/**
	 * Set a PagerAdapter that will supply views for this pager as needed.
	 *
	 * @param adapter Adapter to use
	 * @param initialItem The initial item to show.
	 */
	public void setAdapter(InfinitePagerAdapter infinitePagerAdapter, int initialItem) {
		infinitePageAdapter=infinitePagerAdapter;
		super.setAdapter(infinitePageAdapter);
		setRelativeCurrentItem(initialItem,false);
	}
	
	/**
	 * Set a PagerAdapter that will supply views for this pager as needed.
	 *
	 * @param adapter Adapter to use
	 * 
	 * @deprecated A {@link InfinitePagerAdapter} or sub-class of is required
	 * for this class to work as intended.
	 * @see #setAdapter(InfinitePagerAdapter)
	 */
	@Override
	public void setAdapter(android.support.v4.view.PagerAdapter pagerAdapter) {
		setAdapter((InfinitePagerAdapter)pagerAdapter);
	}
	
	/**
	 * Set a listener that will be invoked whenever the page changes or is incrementally
	 * scrolled. See {@link OnPageChangeListener}.
	 *
	 * @param listener Listener to set
	 */
	@Override
	public void setOnPageChangeListener(OnPageChangeListener listener) {
		mOnPageChangeListener = listener;
	}
	
	/**
	 * Get the index position of the current visible item.
	 * 
	 * <p>Use {@link #getRelativeCurrentItem()} to get the position expected by sub-classes.</p>
	 * 
	 * @see #getRelativeCurrentItem()
	 */
	@Override
	public int getCurrentItem() {
		return super.getCurrentItem();
	}
	
	/**
	 * Set the currently selected page. If the ViewPager has already been through its first
	 * layout with its current adapter there will be a smooth animated transition between
	 * the current item and the specified item.
	 * 
	 * <p>Use {@link #setRelativeCurrentItem(int)} to set the currently visible position as
	 * expected from sub-classes.</p>
	 *
	 * @param item Item index to select
	 * 
	 * @see #setRelativeCurrentItem(int)
	 */
	@Override
	public void setCurrentItem(int item) {
		setCurrentItem(item,true);
	}
	
	/**
	 * Set the currently selected page.
	 * 
	 * <p>Use {@link #setRelativeCurrentItem(int, boolean)} to set the currently visible
	 * position as expected from sub-classes.</p>
	 *
	 * @param item Item index to select
	 * @param smoothScroll True to smoothly scroll to the new item, false to transition immediately
	 * 
	 * @see #setRelativeCurrentItem(int, boolean)
	 */
	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		super.setCurrentItem(item,smoothScroll);
	}
}
