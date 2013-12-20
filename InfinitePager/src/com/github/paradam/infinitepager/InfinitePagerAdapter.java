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

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * This class extends the support class of {@link PagerAdapter}, as such most
 * of the information for that class is also true for this class and sub-classes
 * with only a couple of changes. 
 * 
 * <p>Base class providing the adapter to populate pages inside of
 * a {@link InfiniteViewPager}.  You will most likely want to use a more
 * specific implementation of this, such as for use with APIs of 13 or above
 * {@link com.github.paradam.InfiniteFragmentPagerAdapter} or
 * {@link com.github.paradam.InfiniteFragmentStatePagerAdapter}. For APIs 4
 * and above or using the {@link android.support.v4.app.Fragment Support Fragment}
 * class, use {@link com.github.paradam.support.v4.InfiniteFragmentPagerAdapter}
 * or {@link com.github.paradam.support.v4.InfiniteFragmentStatePagerAdapter}</p>
 *
 * <p>When you implement an InfinitePagerAdapter, you must override the following methods
 * at minimum:</p>
 * <ul>
 * <li>{@link #instantiateRelitiveItem(ViewGroup, int)}</li>
 * <li>{@link #destroyRelitiveItem(ViewGroup, int, Object)}</li>
 * <li>{@link #getRelitiveCount()}</li>
 * <li>{@link #isViewFromObject(View, Object)}</li>
 * </ul>
 *
 * <p>InfinitePagerAdapter is more general than the adapters used for
 * {@link android.widget.AdapterView AdapterViews}. Instead of providing a
 * View recycling mechanism directly InfiniteViewPager uses callbacks to indicate
 * the steps taken during an update. An InfinitePagerAdapter may implement a
 * form of View recycling if desired or use a more sophisticated method of
 * managing page Views such as Fragment transactions where each page is
 * represented by its own Fragment.</p>
 *
 * <p>InfiniteViewPager associates each page with a key Object instead of working
 * with Views directly. This key is used to track and uniquely identify a given page
 * independent of its position in the adapter. A call to the InfinitePagerAdapter
 * method {@link #startUpdate(ViewGroup)} indicates that the contents of the
 * InfiniteViewPager are about to change. One or more calls to 
 * {@link #instantiateRelativeItem(ViewGroup, int)} and/or 
 * {@link #destroyRelativeItem(ViewGroup, int, Object)} will follow, and the end
 * of an update will be signalled by a call to {@link #finishUpdate(ViewGroup)}.
 * By the time {@link #finishUpdate(ViewGroup) finishUpdate} returns the views
 * associated with the key objects returned by
 * {@link #instantiateRelativeItem(ViewGroup, int) instantiateRelativeItem} should be
 * added to the parent ViewGroup passed to these methods and the views associated
 * with the keys passed to {@link #destroyRelativeItem(ViewGroup, int, Object) 
 * destroyRelativeItem} should be removed. The method 
 * {@link #isViewFromObject(View, Object)} identifies whether a page View is
 * associated with a given key object.</p>
 *
 * <p>A very simple InfinitePagerAdapter may choose to use the page Views themselves
 * as key objects, returning them from {@link #instantiateRelativeItem(ViewGroup, int)}
 * after creation and adding them to the parent ViewGroup. A matching
 * {@link #destroyRelativeItem(ViewGroup, int, Object)} implementation would remove the
 * View from the parent ViewGroup and {@link #isViewFromObject(View, Object)}
 * could be implemented as <code>return view == object;</code>.</p>
 *
 * <p>InfinitePagerAdapter supports data set changes. Data set changes must occur on the
 * main thread and must end with a call to {@link #notifyDataSetChanged()} similar
 * to AdapterView adapters derived from {@link android.widget.BaseAdapter}. A data
 * set change may involve pages being added, removed, or changing position. The
 * InfiniteViewPager will keep the current page active provided the adapter implements
 * the method {@link #getRelativeItemPosition(Object)}.</p>
 * 
 * <p>Sub-classes that do override any of the methods found within
 * {@link android.support.v4.view.PagerAdapter} should call through to the super method
 * where possible or failing that, call through to that methods associated ...Relative...
 * method, using {@link #getRelativePosition(int)} to adjust the position given to the
 * current method to the value the associated Relative method is expecting.</p>
 * 
 * <p>A general rule an be used to determine how the interaction to this class or any
 * sub-classes should occur. If the interacting class is expecting an InfinitePagerAdapter,
 * use the ...Relative... methods in place of the similarly named methods from PagerAdapter
 * class. If the class is expecting any PagerAdapter class (sub-classes should use this
 * method), use the methods accessible from the PagerAdapter class.</p>
 * 
 * <p>While an InfinitePagerAdapter can be used within any ViewPager class, if it is not
 * used within an InfiniteViewPager class it will not behave as an infinitely scrollable
 * list, but instead as a normal PagerAdapter. For the Adapter to behave as an infinitely
 * scrollable list there needs to be at least {@value #MIN} items, fewer and it will behave
 * as a normal PagerAdapter</p>
 * 
 * @author Adam Parr
 *
 */
public abstract class InfinitePagerAdapter extends PagerAdapter {
	/**
	 * The number of additional pages on either side of the list of pages to 
	 * simulate a infinite scrolling action. This is the internal offset to
	 * which the page at index position <tt>0</tt> is internally positioned
	 * within the PagerAdapter.
	 * 
	 * <p>In essence <code>getRelativeItem(i)</code> is the same as 
	 * <code>getItem(i+getMargin())</code>.</p>
	 */
	public static final int MARGIN = 2;
	
	/**
	 * The minimum number of pages needed before the PagerAdapter will behave as
	 * an InfinitePagerAdapter.
	 */
	public static final int MIN = 4;
	
	/**
	 * A page other than the first or last is the primary item.
	 */
	private static final int SWITCHER_OTHER = 0;
	/**
	 * The first page is the primary item.
	 */
	private static final int SWITCHER_FIRST = 1;
	/**
	 * The last page is the primary item.
	 */
	private static final int SWITCHER_LAST = 2;
	
	/**
	 * The InfinitePagerAdapter has not yet been attached to a ViewPager yet.
	 */
	private static final int NOT_SET = -1;
	/**
	 * The ViewPager the InfinitePagerAdapter is attached to is not an InfiniteViewPager.
	 * Do not try to allow the ViewPager to be infinitely scrollable.
	 */
	private static final int NORMAL_ADAPTER = 0;
	/**
	 * The ViewPager the InfinitePagerAdapter is attached to is an InfiniteViewPager.
	 * Allow the ViewPager to scroll infinitely.
	 */
	private static final int INFINITE_ADAPTER = 1;
	
	/**
	 * The number of additional pages per side to simulate.
	 */
	private int margin = MARGIN;
	
	/**
	 * The current relative size of the PagerAdapter
	 */
	private int mCount = -1;
	
	/**
	 * {@link #NOT_SET} is not set, 
	 * {@link #INFINITE_ADAPTER} if the ViewPager as provided through setPrimaryItem(ViewGroup, int Object) is infact
	 * an InfiniteViewPager.
	 * {@link #NORMAL_ADAPTER} otherwise.
	 */
	private int attachedToInfiniteViewPager = NOT_SET;
	
	/**
	 * The state of the Switcher.
	 * 
	 * @see #SWITCHER_OTHER
	 * @see #SWITCHER_FIRST
	 * @see #SWITCHER_LAST
	 */
	private int switcher = SWITCHER_OTHER;
	
	/**
	 * Get the title of the Page at the given position.
	 * @param position The position to get the title of.
	 * @return A CharSequence of the title of the Page.
	 */
	public CharSequence getRelativePageTitle(int position) {
		return null;
	}
	
	/**
	 * Called when the host view is attempting to determine if an item's position
	 * has changed. Returns {@link #POSITION_UNCHANGED} if the position of the given
	 * item has not changed or {@link #POSITION_NONE} if the item is no longer present
	 * in the adapter.
	 *
	 * <p>The default implementation assumes that items will never
	 * change position and always returns {@link #POSITION_UNCHANGED}.
	 *
	 * @param object Object representing an item, previously returned by a call to
	 *			   {@link #instantiateItem(View, int)}.
	 * @return object's new position index from [0, {@link #getRelativeCount()}],
	 *		 {@link #POSITION_UNCHANGED} if the object's position has not changed,
	 *		 or {@link #POSITION_NONE} if the item is no longer present.
	 */
	public int getRelativeItemPosition(Object object) {
		return POSITION_UNCHANGED;
	}
	
	
	/**
	 * Get the actual number of pages in this adapter.
	 * @return The actual number of pages in this adapter.
	 */
	public abstract int getRelativeCount();
	
	/**
	 * Create the page for the given position.  The adapter is responsible
	 * for adding the view to the container given here, although it only
	 * must ensure this is done by the time it returns from
	 * {@link #finishUpdate(ViewGroup)}.
	 *
	 * @param container The containing View in which the page will be shown.
	 * @param position The page position to be instantiated.
	 * @return Returns an Object representing the new page.  This does not
	 * need to be a View, but can be some other container of the page.
	 */
	public abstract Object instantiateRelativeItem(ViewGroup container, int position);
	
	/**
	 * Remove a page for the given position.  The adapter is responsible
	 * for removing the view from its container, although it only must ensure
	 * this is done by the time it returns from {@link #finishUpdate(ViewGroup)}.
	 *
	 * @param container The containing View from which the page will be removed.
	 * @param position The page position to be removed.
	 * @param object The same object that was returned by
	 * {@link #instantiateRelativeItem(View, int)}.
	 */
	public abstract void destroyRelativeItem(ViewGroup container, int position, Object object);
	
	/**
	 * Called to inform the adapter of which item is currently considered to
	 * be the "primary", that is the one show to the user as the current page.
	 *
	 * @param container The containing View from which the page will be removed.
	 * @param position The page position that is now the primary.
	 * @param object The same object that was returned by
	 * {@link #instantiateRelativeItem(View, int)}.
	 */
	public void setRelativePrimaryItem(ViewGroup container, int position, Object object) {
	}
	
	/**
	 * If true, {@link #onPreNotifyDataSetChange()} has been called.
	 */
	private boolean preNotifyCalled = false;
	
	/**
	 * Update the count the number of pages in the PagerAdapter.
	 * 
	 * <p>Call this method before calling the super method {@link #notifyDataSetChanged()}
	 * if the implementing class needs an updated value for the number of items or the
	 * appropriate value of {@link #margin} as returned by {@link #getMargin()}. If this
	 * method is not called, then the relevant functions will be completed when calling
	 * the super implementation of {@link #notifyDataSetChanged()}.</p>
	 */
	protected void onPreNotifyDataSetChange() {
		setCount(getRelativeCount());
		preNotifyCalled = true;
	}
	
	@Override
	public void notifyDataSetChanged() {
		if (!preNotifyCalled) {
			onPreNotifyDataSetChange();
		}
		super.notifyDataSetChanged();
		preNotifyCalled = false;
	}
	
	/**
	 * The number of pages in this adapter including the duplicate leading and ending pages at 
	 * either side of the list to simulate an infinite scrolling list of pages.<br>
	 * 
	 * <p>Use {@link #getRelativeCount()} to get the count managed by sub-classes.</p>
	 * 
	 * @see #getRelativeCount()
	 */
	@Override
	public int getCount() {
		if (mCount <= 0) {
			setCount(getRelativeCount());
		//	notifyDataSetChanged();
		}
		
		return mCount + (margin*2);
	}
	
	/**
	 * Returns the absolute position of object, or {@link #POSITION_NONE} 
	 * if the reported position of the object as returned by 
	 * {@link #getItemPosition(Object)} is {@link #POSITION_NONE}.
	 * 
	 * <p>Use {@link #getRelativeItemPosition(Object)} to get the position
	 * expected by sub-classes.</p>
	 * 
	 * @param object Object representing an item, previously returned by a call to
	 *			   {@link #instantiateItem(View, int)}.
	 * @return object's new position index from [0, {@link #getCount()}],
	 *		 {@link #POSITION_UNCHANGED} if the object's position has not changed,
	 *		 or {@link #POSITION_NONE} if the item is no longer present.
	 * 
	 * @see #getRelativeItemPosition(Object)
	 */
	@Override
	public int getItemPosition(Object object) {
		if (object == null) return POSITION_UNCHANGED;
		int returnedPosition = POSITION_NONE;
		int relativeObjectPosition = getRelativeItemPosition(object); // The position of this Object.
		
		if (relativeObjectPosition == POSITION_UNCHANGED) {
			returnedPosition = POSITION_UNCHANGED;
		} else if (relativeObjectPosition != POSITION_NONE) {
			// The items position has changed, return the new position
			returnedPosition = relativeObjectPosition + margin;
		}
		return returnedPosition;
	}

	/**
	 * Called to inform the adapter of which item is currently considered to
	 * be the "primary", that is the one show to the user as the current page.
	 * 
	 * <p>Use {@link #setRelativePrimaryItem(ViewGroup, int, Object)} to get the
	 * count expected by sub-classes.</p>
	 *
	 * @param container The containing View from which the page will be removed.
	 * @param position The page position that is now the primary.
	 * @param object The same object that was returned by
	 * {@link #instantiateItem(View, int)}.
	 * 
	 * @see #setRelativePrimaryItem(ViewGroup, int, Object)
	 */
	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		setRelativePrimaryItem(container, getRelitivePosition(position), object);
		if (attachedToInfiniteViewPager == NOT_SET) {
			attachedToInfiniteViewPager = container instanceof InfiniteViewPager ? INFINITE_ADAPTER : NORMAL_ADAPTER ;
			setCount(mCount);
		}
		if (position == mCount + margin || position == margin) {
			// Page is first page
			switcher = SWITCHER_FIRST;
		} else if (position == margin -1 || position == mCount + margin -1) {
			// Page is last page
			switcher = SWITCHER_LAST;
		} else {
			switcher = SWITCHER_OTHER;
		}
	}
	
	/**
	 * Create the page for the given position.  The adapter is responsible
	 * for adding the view to the container given here, although it only
	 * must ensure this is done by the time it returns from
	 * {@link #finishUpdate(ViewGroup)}.
	 * 
	 * <p>Use {@link #instantiateRelativeItem(ViewGroup,int) to instantiate the
	 * correct Object as expected by sub-classes.</p>
	 *
	 * @param container The containing View in which the page will be shown.
	 * @param position The page position to be instantiated.
	 * @return Returns an Object representing the new page.  This does not
	 * need to be a View, but can be some other container of the page.
	 * 
	 * @see #instantiateRelativeItem(ViewGroup,int)
	 */
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		return instantiateRelativeItem(container, getRelitivePosition(position));
	}

	/**
	 * Remove a page for the given position.  The adapter is responsible
	 * for removing the view from its container, although it only must ensure
	 * this is done by the time it returns from {@link #finishUpdate(ViewGroup)}.
	 * 
	 * <p>Use {@link #destroyRelativeItem(ViewGroup, int, Object) to destroy the
	 * correct Object as expected by sub-classes.</p>
	 *
	 * @param container The containing View from which the page will be removed.
	 * @param position The page position to be removed.
	 * @param object The same object that was returned by
	 * {@link #instantiateItem(View, int)}.
	 * 
	 * @see #destroyRelativeItem(ViewGroup, int, Object)
	 */
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		int last = mCount + margin -1;
		if(position >= margin-1 && position <= last+1) {
			//                                  First Item (End)            Last Item (End)                                       Last Item (Start)        First Item (Start)
			if (margin > 0 && (((position == mCount + margin || position == last) && switcher == SWITCHER_FIRST) || ((position == margin -1 || position == margin) && switcher == SWITCHER_LAST))) {
				// This object is beside the current active object, it should not be removed.
				return;
			}
			
			if (switcher == SWITCHER_FIRST && margin == 0 && position == 1) {
				// Case where the item at the index position 1 may be requested to be destroyed
				// even when the current selected item is at position 0, ignore this request.
				return;
			}
			
			destroyRelativeItem(container, getRelitivePosition(position), object);
		}
	}

	/**
	 * Get the title of the Page at the given position.
	 * 
	 * <p>Use {@link #getRelativePageTitle(int) to get the
	 * correct Object title as expected by sub-classes.</p>
	 * 
	 * @param position The position to get the title of.
	 * @return A CharSequence of the title of the Page.
	 * 
	 *  @see #getRelativePageTitle(int)
	 */
	@Override
	public CharSequence getPageTitle(int position) {
		return getRelativePageTitle(getRelitivePosition(position));
	}
	
	/**
	 * Returns the proportional width of a given page as a percentage of the
	 * ViewPager's measured width from (0.f-1.f]
	 *
	 * @param position The position of the page requested
	 * @return Proportional width for the given page position
	 * 
	 * @deprecated Having a width different from the default causes problems at the
	 * edges in an InfiniteViewPager when trying to switch to the correct Page.
	 */
	@Override
	public final float getPageWidth(int position) {
		return super.getPageWidth(position);
	}
	
	/**
	 * Set the number of pages the PagerAdapter has initially.
	 * 
	 * @param count The number of pages.
	 */
	private void setCount(int count) {
		mCount = count;
		if(mCount < MIN || attachedToInfiniteViewPager == NORMAL_ADAPTER) {
			margin = 0;
		} else {
			margin = MARGIN;
		}
	}
	
	/**
	 * The internal margin on either side of the real pages.
	 * 
	 * <p>Only sub-classes that directly extend {@link InfinitePagerAdapter} need to use
	 * this method.</p>
	 * 
	 * @return An integer of either 0 or 2;
	 */
	protected int getMargin() {
		return margin;
	}
	
	/**
	 * Return The actual position of the page as implemented by the sub-class.
	 * 
	 * <p>Only sub-classes that directly extend InfinitePagerAdapter need to use
	 * this method.</p>
	 * 
	 * @param position The position to normalise.
	 * @return The actual position of the page as implemented by the sub-class.
	 */
	protected int getRelitivePosition(int position) {
		return (position-margin+mCount)%mCount;
	}
}
