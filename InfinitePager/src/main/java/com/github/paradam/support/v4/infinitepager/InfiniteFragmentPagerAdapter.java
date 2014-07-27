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

package com.github.paradam.support.v4.infinitepager;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.github.paradam.infinitepager.InfinitePagerAdapter;

/**
 * <p>A FragmentPagerAdapter that will behave as if it has an infinite number of pages, or if the
 * number of pages are less than {@link com.github.paradam.infinitepager.InfinitePagerAdapter#MIN},
 * behave as a normal {@link android.support.v4.app.FragmentPagerAdapter FragmentPagerAdapter} <p/>
 *
 * <p>Applications targeting {@link android.os.Build.VERSION_CODES#DONUT} and above should use this
 * class, applications targeting only {@link android.os.Build.VERSION_CODES#HONEYCOMB_MR2 13} and
 * above using the native Android {@link android.app.Fragment Fragment} class should use instead
 * {@link com.github.paradam.infinitepager.InfiniteFragmentPagerAdapter}.</p>
 *
 * <p/> Implementation of {@link com.github.paradam.infinitepager.InfinitePagerAdapter} based off the
 * {@link android.support.v4.app.FragmentPagerAdapter} that represents each page as a{@link android.support.v4.app.Fragment}
 * that is persistently kept in the fragment manager as long as the user can return to the page.<p/>
 *
 * <p>This version of the pager is best for use when there are a handful of typically more static
 * fragments to be paged through, such as a set of tabs. The fragment of each page the user visits
 * will be kept in memory, though its view hierarchy may be destroyed when not visible.  This can
 * result in using a significant amount of memory since fragment instances can hold on to an
 * arbitrary amount of state. For larger sets of pages, consider
 * {@link com.github.paradam.support.v4.infinitepager.InfiniteFragmentStatePagerAdapter}.<p/>
 *
 * <p>When using InfiniteFragmentPagerAdapter the host ViewPager must have a valid ID set.</p>
 *
 * <p>Subclasses only need to implement {@link #getRelativeItem(int)} and {@link #getRelativeCount()}
 * to have a working adapter. Override {@link #getRelativeItemId(int)} if the fragments have the
 * possibility of changing positions within the ViewPager.</p>
 *
 * @author Adam Parr
 */
public abstract class InfiniteFragmentPagerAdapter extends InfinitePagerAdapter {
    /**
     * Tag to show when logging.
     */
    private static final String TAG = "InfiniteFragmentPagerAdapter";

    /**
     * The current Fragment Transaction that is occurring, <code>null</code> if no there is
     * currently no transaction.
     */
    private FragmentTransaction mCurTransaction  = null;
    /**
     * Reference to the FragmentManager.
     */
    private FragmentManager     mFragmentManager = null;

    /**
     * The currently set visible Item.
     */
    private Fragment mCurrentPrimaryItem = null;

    /**
     * Constructor that accepts a FragmentManager.
     *
     * @param fm The FragmentManager.
     */
    public InfiniteFragmentPagerAdapter(FragmentManager fm) {
        mFragmentManager = fm;
    }

    /**
     * <p>Return a unique identifier for the item at the given position.<p/>
     *
     * <p>The default implementation returns the given position. Subclasses should override this
     * method if the positions of items can change.</p>
     *
     * @param position Position within this adapter
     * @return Unique identifier for the item at position
     */
    public long getRelativeItemId(int position) {
        return position;
    }

    /**
     * Get the Fragment at the position provided.
     *
     * @param position The Fragment at this position in the PageView to get.
     * @return The Fragment at this location.
     */
    public abstract Fragment getRelativeItem(int position);

    @Override
    public Object instantiateRelativeItem(ViewGroup container, int position) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        final long itemId = getRelativeItemId(position);

        // Do we already have this fragment?
        String name = makeFragmentName(container.getId(), itemId);
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            mCurTransaction.attach(fragment);
        } else {
            fragment = getRelativeItem(position);
            mCurTransaction.add(container.getId(), fragment, makeFragmentName(container.getId(), itemId));
        }
        if (fragment != mCurrentPrimaryItem) {
            fragment.setMenuVisibility(false);
            fragment.setUserVisibleHint(false);
        }

        return fragment;
    }

    @Override
    public void destroyRelativeItem(ViewGroup container, int position, Object object) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        mCurTransaction.detach((Fragment) object);
    }

    @Override
    public void setRelativePrimaryItem(ViewGroup container, int position, Object object) {
        if (mCurrentPrimaryItem != null) {
            mCurrentPrimaryItem.setMenuVisibility(false);
            mCurrentPrimaryItem.setUserVisibleHint(false);
        }

        Fragment fragment = (Fragment) object;
        if (fragment != null) {
            fragment.setMenuVisibility(true);
            fragment.setUserVisibleHint(true);
        }
        mCurrentPrimaryItem = fragment;
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        if (mCurTransaction != null) {
            try {
                mCurTransaction.commitAllowingStateLoss();
                mCurTransaction = null;
                mFragmentManager.executePendingTransactions();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    /**
     * <p>Return a unique identifier for the item at the given position.<p/>
     *
     * <p>{@link #getRelativeItemId(int)} should be used instead to get the Appropriate item id from
     * the item in the implementing classes list.</p>
     *
     * @param position Position within this adapter
     * @return Unique identifier for the item at position
     */
    public long getItemId(int position) {
        return getRelativeItemId(getRelativePosition(position));
    }

    /**
     * Get the Fragment at the position provided.
     *
     * @param position The Fragment at this position in the PageView to get.
     * @return The Fragment at this location.
     * @see #getRelativeItem(int)
     */
    public Fragment getItem(int position) {
        return getRelativeItem(getRelativePosition(position));
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (object != null && mCurrentPrimaryItem != null && mCurrentPrimaryItem.equals(object)) {
            // We are already on the correct page.
            return;
        }

        super.setPrimaryItem(container, position, object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final int margin = getMargin();
        if (position < margin - 1 || position > getCount() + margin) {
            return null;
        }

        return super.instantiateItem(container, position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object != null && ((Fragment) object).getView() == view;
    }

    /**
     * Create a unique name for the fragment so that it can be retrieved later.
     *
     * @param viewId The id of the ViewContainer that is considered to be the Fragments parent.
     * @param id     The id of the child Fragment.
     * @return A unique name for the Fragment.
     */
    private static String makeFragmentName(int viewId, long id) {
        return TAG + ":switcher:" + viewId + ":" + id;
    }
}