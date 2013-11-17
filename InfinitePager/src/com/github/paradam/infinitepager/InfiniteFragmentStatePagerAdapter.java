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

import java.util.ArrayList;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v13.app.FragmentCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * A FragmentPagerAdapter that will behave as if it has an infinite number of pages,
 * or if the number of pages are less than {@link InfinitePagerAdapter#MIN}, behave as a normal 
 * {@link android.support.v13.app.FragmentStatePagerAdapter FragmentStatePagerAdapter}
 * 
 * <p>This class is intended to be used in applications supporting API 
 * {@link Build.VERSION_CODES#HONEYCOMB_MR2 13} and above using the native Android 
 * {@link android.app.Fragment Fragment} class. For applications supporting APIs bellow
 * {@link Build.VERSION_CODES#HONEYCOMB_MR2 13} or using 
 * {@link android.support.v4.app.Fragment support.v4.app.Fragment} should use instead 
 * {@link com.github.paradam.support.v4.infinitepager.InfiniteFragmentPagerAdapter}.</p>
 * 
 * Implementation of {@link InfinitePagerAdapter} based off the
 * {@link android.support.v13.app.FragmentStatePagerAdapter} that uses a {@link Fragment}
 * to manage each page. This class also handles saving and restoring of fragment's state.
 *
 * <p>This version of the pager is more useful when there are a large number
 * of pages, working more like a list view.  When pages are not visible to
 * the user, their entire fragment may be destroyed, only keeping the saved
 * state of that fragment.  This allows the pager to hold on to much less
 * memory associated with each visited page as compared to
 * {@link InfiniteFragmentPagerAdapter} at the cost of potentially more overhead when
 * switching between pages.
 *
 * <p>When using InfiniteFragmentStatePagerAdapter the host ViewPager must have a valid ID set.</p>
 *
 * <p>Subclasses only need to implement {@link #getRelativeItem(int)} and {@link #getRelativeCount()}
 * to have a working adapter. Override {@link InfinitePagerAdapter#getRelativeItemPosition(int)}
 * if the fragments have the possibility of changing positions within the ViewPager.
 * 
 * @author Adam Parr
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public abstract class InfiniteFragmentStatePagerAdapter extends InfinitePagerAdapter {
	/**
	 * Tag to show when logging.
	 */
	private static final String TAG = "InfiniteFragmentStatePagerAdapter";

	/**
	 * The current Fragment Transaction that is occurring, <code>null</code>
	 * if no there is currently no transaction.
	 */
	private FragmentTransaction mCurTransaction = null;
	/**
	 * Reference to the FragmentManager.
	 */
	private FragmentManager mFragmentManager = null;
	
	/**
	 * Array list where the fragments saved states are stored.
	 */
	private ArrayList<Fragment.SavedState> mSavedState = new ArrayList<Fragment.SavedState>();
	/**
	 * Array list where the fragments are stored.
	 */
	private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();

	/**
	 * The currently set visible Item.
	 */
	private Fragment mCurrentPrimaryItem = null;
	
	/**
	 * Constructor that accepts a FragmentManager.
	 * @param fm The FragmentManager.
	 */
	public InfiniteFragmentStatePagerAdapter(FragmentManager fm) {
		mFragmentManager = fm;
	}
	
	/**
	 * Get the Fragment at the position provided.
	 * @param position The Fragment at this position in the PageView to get.
	 * @return The Fragment at this location.
	 */
	public abstract Fragment getRelativeItem(int position);
	
	@Override
	public Object instantiateRelativeItem(ViewGroup container, int position) {
		// If we already have this item instantiated, there is nothing
		// to do. This can happen when we are restoring the entire pager
		// from its saved state, where the fragment manager has already
		// taken care of restoring the fragments we previously had instantiated.
		Fragment f = null;
		if (mFragments.size() > position) {
			f = mFragments.get(position);
			if (f != null) {
				return f;
			}
		}
		
		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
		f = (Fragment) getRelativeItem(position);
		
		if(f.getView() != null) {
			mCurTransaction.remove(f);
		}

		if (mSavedState.size() > position) {
			Fragment.SavedState fss = mSavedState.get(position);
			if (fss != null && !f.isAdded()) {
				f.setInitialSavedState(fss);
			} else if (f.isAdded()) {
				Log.e(TAG,"Error when instantiating Fragment: "+ f +", FragmentSavedState: "+ fss +", fragment already added: "+ f.isAdded());
			}
		}
		while (mFragments.size() <= position) {
			mFragments.add(null);
		}
		FragmentCompat.setMenuVisibility(f, false);
		FragmentCompat.setUserVisibleHint(f, false);
		mFragments.set(position, f);
		mCurTransaction.add(container.getId(), f);

		return f;
	}
	
	@Override
	public void destroyRelativeItem(ViewGroup container, int position, Object object) {
		Fragment fragment = (Fragment)object;

		if (mCurTransaction == null) {
			mCurTransaction = mFragmentManager.beginTransaction();
		}
		while (mSavedState.size() <= position) {
			mSavedState.add(null);
			if (mFragments.size() <= position) {
				mFragments.add(null);
			}
		}
		mSavedState.set(position, mFragmentManager.saveFragmentInstanceState(fragment));
		mFragments.set(position, null);

		mCurTransaction.remove(fragment);
	}

	@Override
	public void setRelativePrimaryItem(ViewGroup container, int position, Object object) {
		if (mCurrentPrimaryItem != null) {
			FragmentCompat.setMenuVisibility(mCurrentPrimaryItem, false);
			FragmentCompat.setUserVisibleHint(mCurrentPrimaryItem, false);
		}
		
		mCurrentPrimaryItem = (Fragment)object;
		if (mCurrentPrimaryItem != null) {
			FragmentCompat.setMenuVisibility(mCurrentPrimaryItem, true);
			FragmentCompat.setUserVisibleHint(mCurrentPrimaryItem, true);
		}
	}
	
	@Override
	public void finishUpdate(ViewGroup container) {
		if (mCurTransaction != null) {
			try {
				mCurTransaction.commitAllowingStateLoss();
				mCurTransaction = null;
				mFragmentManager.executePendingTransactions();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		
		final int count = getRelativeCount();
		final int margin = getMargin();
		
		ArrayList<Fragment.SavedState> newSavedState = new ArrayList<Fragment.SavedState>(count);
		ArrayList<Fragment> newFragments = new ArrayList<Fragment>(count);
		
		int length = mFragments.size();
		
		for(int x = 0; x < length; x++) {
			int position = getItemPosition(mFragments.get(x));
			switch (position) {
				case POSITION_UNCHANGED:
					while (newFragments.size() < x+1) {
						newFragments.add(null);
						newSavedState.add(null);
					}
					newFragments.set(x,mFragments.get(x));
					if(mSavedState.size() > x) {
						newSavedState.set(x,mSavedState.get(x));
					}
					break;
				case POSITION_NONE:
					// Do not add fragment.
					if (mCurTransaction == null) {
						mCurTransaction = mFragmentManager.beginTransaction();
					}
					mCurTransaction.remove(mFragments.get(x));
					break;
				default:
					position = (position - margin + count) % count;
					while (newFragments.size() < position+1) {
						newFragments.add(null);
						newSavedState.add(null);
					}
					newFragments.set(position,mFragments.get(x));
					if(mSavedState.size() > x) {
						newSavedState.set(position,mSavedState.get(x));
					}
			}
		}
		
		mSavedState = newSavedState;
		mFragments = newFragments;
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
		for (int i=0; i<mFragments.size(); i++) {
			Fragment f = mFragments.get(i);
			if (f != null) {
				if (state == null) {
					state = new Bundle();
				}
				String key = "f" + i;
				mFragmentManager.putFragment(state, key, f);
			}
		}
		
		return state;
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		if (state != null) {
			Bundle bundle = (Bundle)state;
			bundle.setClassLoader(loader);
			Parcelable[] fss = bundle.getParcelableArray("states");
			mSavedState.clear();
			mFragments.clear();
			if (fss != null) {
				for (int i=0; i<fss.length; i++) {
					mSavedState.add((Fragment.SavedState)fss[i]);
				}
			}
			Iterable<String> keys = bundle.keySet();
			for (String key: keys) {
				if (key.startsWith("f")) {
					int index = Integer.parseInt(key.substring(1));
					Fragment f = null;
					try {
						f = mFragmentManager.getFragment(bundle, key);
					} catch (Exception e) {e.printStackTrace();}
					if (f != null) {
						while (mFragments.size() <= index) {
							mFragments.add(null);
						}
						FragmentCompat.setMenuVisibility(f, false);
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
	 * @param position The Fragment at this position in the PageView to get.
	 * @return The Fragment at this location.
	 * 
	 * @see #getRelativeItem(int)
	 */
	public Fragment getItem(int position) {
		return getRelativeItem(getRelitivePosition(position));
	}
	
	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		if(object != null && mCurrentPrimaryItem != null && mCurrentPrimaryItem.equals(object)) {
			// We are already on the correct page.
			return;
		}
		
		super.setPrimaryItem(container, position, object);
	}
	
	@Override
	public int getItemPosition(Object object) {
		if (object == null) return POSITION_UNCHANGED;
		int objectPosition = mFragments.indexOf(object); // Current position of the object in the Pager.
		int relativeObjectPosition = POSITION_UNCHANGED;
		int returnedPosition = POSITION_NONE;
		if (objectPosition >= 0 ) {
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
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		final int margin = getMargin();
		if (position < margin-1 || position > getRelativeCount() + margin) {
			return null;
		}
		
		return super.instantiateItem(container, position);
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		if (object != null) {
			return ((Fragment)object).getView() == view;
		}
		return false;
	}
	
	/**
	 * Get the current instantiated list of Fragments used in this FragmentStatePagerAdapter.<br>
	 * Call this method after {@link #restoreState(Parcelable, ClassLoader)} to get the list
	 * of currently instantiated Fragments after a restore state.
	 * @return An array of Fragments.
	 */
	protected final Fragment[] getFragments() {
		return mFragments.toArray(new Fragment[getRelativeCount()]);
	}
}
