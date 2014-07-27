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

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.github.paradam.infinitepager.InfinitePagerAdapter;

import java.util.ArrayList;

/**
 * <p>A FragmentStatePagerAdapter that will behave as if it has an infinite number of pages, or if the
 * number of pages are less than {@link com.github.paradam.infinitepager.InfinitePagerAdapter#MIN},
 * behave as a normal {@link android.support.v4.app.FragmentStatePagerAdapter FragmentStatePagerAdapter}.<p/>
 *
 * <p>Applications targeting API {@link android.os.Build.VERSION_CODES#DONUT 4} and above should use
 * this class, applications targeting only API {@link android.os.Build.VERSION_CODES#HONEYCOMB_MR2 13}
 * and above using the native Android {@link android.app.Fragment Fragment} class should use instead
 * {@link com.github.paradam.infinitepager.InfiniteFragmentStatePagerAdapter}.</p>
 *
 * <p>Implementation of {@link com.github.paradam.infinitepager.InfinitePagerAdapter} based off the
 * {@link android.support.v4.app.FragmentStatePagerAdapter} that uses a {@link android.support.v4.app.Fragment}
 * to manage each page. This class also handles saving and restoring of fragment's state.<p/>
 *
 * <p>This version of the pager is more useful when there are a large number of pages, working more
 * like a list view.  When pages are not visible to the user, their entire fragment may be destroyed,
 * only keeping the saved state of that fragment. This allows the pager to hold on to much less memory
 * associated with each visited page as compared to {@link com.github.paradam.support.v4.infinitepager.InfiniteFragmentPagerAdapter}
 * at the cost of potentially more overhead when switching between pages.<p/>
 *
 * <p>When using InfiniteFragmentStatePagerAdapter the host ViewPager must have a valid ID set.</p>
 *
 * <p>Subclasses only need to implement {@link #getRelativeItem(int)} and {@link #getRelativeCount()}
 * to have a working adapter. Override {@link com.github.paradam.infinitepager.InfinitePagerAdapter#getRelativePosition(int)}
 * if the fragments have the possibility of changing positions within the ViewPager.</p>
 *
 * @author Adam Parr
 */
public abstract class InfiniteFragmentStatePagerAdapter extends InfinitePagerAdapter {
    /**
     * Tag to show when logging.
     */
    private static final String TAG = "InfiniteFragmentStatePagerAdapter";

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
     * Array list where the fragments saved states are stored.
     */
    private ArrayList<Fragment.SavedState> mSavedState         = new ArrayList<Fragment.SavedState>();
    /**
     * Array list where the fragments are stored.
     */
    private ArrayList<Fragment>            mFragments          = new ArrayList<Fragment>();
    /**
     * SparseArray list where the destroyed fragments are stored.
     *
     * <p>Fragments are only stored in this array in case the PagedAdapter changes the dataset
     * and we need to work out what order the fragments should be in now.</p>
     */
    private SparseArray<Fragment>          mDestroyedFragments = new SparseArray<Fragment>();

    /**
     * The currently set visible Item.
     */
    private Fragment mCurrentPrimaryItem = null;

    /**
     * Constructor that accepts a FragmentManager.
     *
     * @param fm The FragmentManager.
     */
    public InfiniteFragmentStatePagerAdapter(FragmentManager fm) {
        mFragmentManager = fm;
    }

    /**
     * Get the Fragment at the position provided.
     *
     * @param position The Fragment at this position in the PageView to get.
     * @return The Fragment at this location.
     */
    public abstract Fragment getRelativeItem(int position);

    /**
     * Create the page for the given position.  The adapter is responsible for adding the view to
     * the container given here, although it only must ensure this is done by the time it returns
     * from {@link #finishUpdate(android.view.ViewGroup)}.
     *
     * @param container The containing View in which the page will be shown.
     * @param position  The page position to be instantiated.
     * @return Returns an Object representing the new page.  This does not need to be a View, but
     * can be some other container of the page.
     */
    public Object instantiateRelativeItem(ViewGroup container, int position) {
        // If we already have this item instantiated, there is nothing
        // to do. This can happen when we are restoring the entire pager
        // from its saved state, where the fragment manager has already
        // taken care of restoring the fragments we previously had instantiated.
        Fragment f;
        if (mFragments.size() > position) {
            f = mFragments.get(position);
            if (f != null) {
                return f;
            }
        }

        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        f = getRelativeItem(position);

        if (f.getView() != null) {
            mCurTransaction.remove(f);
        }

        if (mSavedState.size() > position) {
            Fragment.SavedState fss = mSavedState.get(position);
            if (fss != null && !f.isAdded()) {
                f.setInitialSavedState(fss);
            } else if (f.isAdded()) {
                Log.e(TAG, "Error when instantiating Fragment: " + f + ", FragmentSavedState: " + fss + ", fragment already added: " + f.isAdded());
            }
        }
        while (mFragments.size() <= position) {
            mFragments.add(null);
        }
        f.setMenuVisibility(false);
        f.setUserVisibleHint(false);
        mFragments.set(position, f);
        mCurTransaction.add(container.getId(), f);

        return f;
    }

    /**
     * Remove a page for the given position.  The adapter is responsible for removing the view from
     * its container, although it only must ensure this is done by the time it returns from {@link
     * #finishUpdate(android.view.ViewGroup)}.
     *
     * @param container The containing View from which the page will be removed.
     * @param position  The page position to be removed.
     * @param object    The same object that was returned by {@link #instantiateItem(android.view.View, int)}.
     */
    public void destroyRelativeItem(ViewGroup container, int position, Object object) {
        Fragment fragment = (Fragment) object;
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
        while (mSavedState.size() <= position) {
            mSavedState.add(null);
            if (mFragments.size() <= position) {
                mFragments.add(null);
            }
        }
        if (fragment != null && fragment.isAdded()) {
            mSavedState.set(position, mFragmentManager.saveFragmentInstanceState(fragment));
        } else {
            mSavedState.set(position, null);
        }
        mDestroyedFragments.put(position, fragment);
        mFragments.set(position, null);
        mCurTransaction.remove(fragment);
    }

    /**
     * Called to inform the adapter of which item is currently considered to be the "primary", that
     * is the one show to the user as the current page.
     *
     * @param container The containing View from which the page will be removed.
     * @param position  The page position that is now the primary.
     * @param object    The same object that was returned by {@link #instantiateItem(android.view.View, int)}.
     */
    public void setRelativePrimaryItem(ViewGroup container, int position, Object object) {
        if (mCurrentPrimaryItem != null) {
            mCurrentPrimaryItem.setMenuVisibility(false);
            mCurrentPrimaryItem.setUserVisibleHint(false);
        }

        mCurrentPrimaryItem = (Fragment) object;
        if (mCurrentPrimaryItem != null) {
            mCurrentPrimaryItem.setMenuVisibility(true);
            mCurrentPrimaryItem.setUserVisibleHint(true);
        }
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
    public void notifyDataSetChanged() {
        onPreNotifyDataSetChange();
        final int count = getRelativeCount();
        final int margin = getMargin();

        ArrayList<Fragment.SavedState> newSavedState = new ArrayList<Fragment.SavedState>(count);
        ArrayList<Fragment> newFragments = new ArrayList<Fragment>(count);
        SparseArray<Fragment> newDestroyedFragments = new SparseArray<Fragment>();

        int length = mFragments.size();

        for (int x = 0; x < length; x++) {
            Fragment fragment = mFragments.get(x);
            boolean fromDestroyed = false;
            if (fragment == null) {
                // Check if there is a destroyed fragment that was in the list, use that if there is.
                fragment = mDestroyedFragments.get(x);
                fromDestroyed = true;
            }
            int position = getItemPosition(fragment);
            switch (position) {
                case POSITION_UNCHANGED:
                        while (newFragments.size() < x + 1) {
                            newFragments.add(null);
                            newSavedState.add(null);
                        }
                        if (!fromDestroyed) {
                            newFragments.set(x, mFragments.get(x));
                        } else {
                            newDestroyedFragments.put(x, fragment);
                        }
                        if (mSavedState.size() > x) {
                            newSavedState.set(x, mSavedState.get(x));
                        }
                    break;
                case POSITION_NONE:
                    // Do not add fragment.
                    if (!fromDestroyed) {
                        if (mCurTransaction == null) {
                            mCurTransaction = mFragmentManager.beginTransaction();
                        }
                        mCurTransaction.remove(mFragments.get(x));
                    }
                    break;
                default:
                    position = (position - margin + count) % count;
                    while (newFragments.size() < position + 1) {
                        newFragments.add(null);
                        newSavedState.add(null);
                    }
                    if (!fromDestroyed) {
                        newFragments.set(position, fragment);
                    } else {
                        newDestroyedFragments.put(position, fragment);
                    }
                    if (mSavedState.size() > x) {
                        newSavedState.set(position, mSavedState.get(x));
                    }
            }
        }

        mSavedState = newSavedState;
        mFragments = newFragments;
        mDestroyedFragments = newDestroyedFragments;

        super.notifyDataSetChanged();
    }

    @Override
    public Parcelable saveState() {
        Bundle state = null;
        if (mSavedState.size() > 0) {
            state = new Bundle();
            Fragment.SavedState[] fss = new Fragment.SavedState[mSavedState.size()];
            mSavedState.toArray(fss);
            state.putParcelableArray("states", fss);
        }
        for (int i = 0; i < mFragments.size(); i++) {
            Fragment f = mFragments.get(i);
            if (f != null && f.isAdded()) {
                if (state == null) {
                    state = new Bundle();
                }
                String key = "f" + i;
                mFragmentManager.putFragment(state, key, f);
            }
        }
        if (state != null) {
            state.putInt("currentPrimaryItem", mFragments.indexOf(mCurrentPrimaryItem));
        }

        return state;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle) state;
            bundle.setClassLoader(loader);
            Parcelable[] fss = bundle.getParcelableArray("states");
            mSavedState.clear();
            mFragments.clear();
            if (fss != null) {
                for (Parcelable ss : fss) {
                    mSavedState.add((Fragment.SavedState) ss);
                }
            }
            int currentPrimaryItem = bundle.getInt("currentPrimaryItem");
            Iterable<String> keys = bundle.keySet();
            for (String key : keys) {
                if (key.startsWith("f")) {
                    int index = Integer.parseInt(key.substring(1));
                    Fragment f = null;
                    try {
                        f = mFragmentManager.getFragment(bundle, key);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                    if (f != null) {
                        while (mFragments.size() <= index) {
                            mFragments.add(null);
                        }
                        f.setMenuVisibility(currentPrimaryItem == index);
                        f.setUserVisibleHint(currentPrimaryItem == index);
                        mFragments.set(index, f);
                    } else {
                        Log.w(TAG, "Bad fragment at key " + key);
                    }
                }
            }
        }
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

    /**
     * <p>Returns the absolute position of object, or {@link #POSITION_NONE} if the reported position
     * of the object as returned by {@link #getRelativeItemPosition(Object)} is {@link #POSITION_NONE}.<p/>
     *
     * <p>Use {@link #getRelativeItemPosition(Object)} to get the position expected by sub-classes.</p>
     *
     * @see #getRelativeItemPosition(Object)
     */
    @Override
    public int getItemPosition(Object object) {
        if (object == null) {
            return POSITION_UNCHANGED;
        }
        int objectPosition = mFragments.indexOf(object); // Current position of the object in the Pager.
        int relativeObjectPosition = POSITION_UNCHANGED;
        int returnedPosition = POSITION_NONE;
        if (objectPosition >= 0) {
            relativeObjectPosition = getRelativeItemPosition(object); // The position of this Object.
        }

        if (relativeObjectPosition == POSITION_UNCHANGED) {
            returnedPosition = objectPosition == -1 ? POSITION_NONE : objectPosition + getMargin();
        } else if (relativeObjectPosition != POSITION_NONE) {
            // The items position has changed, return the new position
            returnedPosition = relativeObjectPosition + getMargin();
        }
        return returnedPosition;
    }

    /**
     * @see #instantiateRelativeItem(android.view.ViewGroup, int)
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final int margin = getMargin();
        if (position < margin - 1 || position > getRelativeCount() + margin) {
            return null;
        }

        return super.instantiateItem(container, position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object != null && ((Fragment) object).getView() == view;
    }

    /**
     * Get the current instantiated list of Fragments used in this FragmentStatePagerAdapter.<br>
     * Call this method after {@link #restoreState(android.os.Parcelable, ClassLoader)} to get the list of
     * currently instantiated Fragments after a restore state.
     *
     * @return An array of Fragments.
     */
    protected final Fragment[] getFragments() {
        return mFragments.toArray(new Fragment[getRelativeCount()]);
    }
}