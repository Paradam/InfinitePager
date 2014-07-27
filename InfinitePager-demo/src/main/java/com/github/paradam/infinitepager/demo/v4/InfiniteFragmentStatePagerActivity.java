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

package com.github.paradam.infinitepager.demo.v4;

import com.github.paradam.infinitepager.InfiniteViewPager;
import com.github.paradam.infinitepager.demo.R;
import com.github.paradam.support.v4.infinitepager.InfiniteFragmentStatePagerAdapter;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Demo class for the use of an 
 * {@link com.github.paradam.support.v4.infinitepager.InfiniteFragmentStatePagerAdapter InfiniteFragmentStatePagerAdapter}
 * using the v4 support package.
 * 
 * <p>The activity shows an initial {@link com.github.paradam.infinitepager.InfinitePagerView}
 * of three items, that are initially not infinitely scrollable. A menu item is inflated from
 * an XML resource, that allows the user to add two more Fragments to the list or remove those
 * added Fragments if already added. Upon adding the additional Fragments, the InfiniteViewPager
 * and InfinitePagerAdapter re-check if the number of items match or exceed 
 * {#link com.github.paradam.infinitepager.InfinitePagerAdapter#MIN}, if so behaving as if there
 * were an infinite number of the same Fragments, allowing the user to continuously scroll left or
 * right without reaching the end of the ViewPager. When InfinitePagerAdapter#MIN is not reached
 * the InfiniteViewPager behaves like a normal ViewPager.</p>
 * 
 * @author Adam Parr
 *
 */
public class InfiniteFragmentStatePagerActivity extends FragmentActivity {
	/**
	 * The InfinitePagerAdapter
	 */
	private DemoInfiniteFragmentStatePagerAdapter pagerAdapter;
	private InfiniteViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Create the InfiniitePagerAdapter, providing the support FragmentManager to it.
		pagerAdapter = new DemoInfiniteFragmentStatePagerAdapter(getSupportFragmentManager());
		
		// Get the InfiniteViewPager inflated from the resource at R.layout.activity_main
		viewPager = ((InfiniteViewPager)findViewById(R.id.viewPager)); // Our ViewPager inflated from
		// the XML resource file is an InfiniteViewPager.
		viewPager.setAdapter(pagerAdapter); // Set the adapter for the InfiniteViewPager.
		viewPager.setRelativeCurrentItem(1,false); // Show the middle item initially
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		menu.findItem(R.id.action_show_extras).setVisible(!pagerAdapter.showExtras);
		menu.findItem(R.id.action_hide_extras).setVisible(pagerAdapter.showExtras);
		
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// Get the current selected item.
		switch (item.getItemId()) {
			case R.id.action_show_extras:
				pagerAdapter.showExtras(true);
				break;
			case R.id.action_hide_extras:
				pagerAdapter.showExtras(false);
				break;
			default:
				return super.onMenuItemSelected(featureId, item);
		}
		supportInvalidateOptionsMenu();
		return true;
	}
	
	/**
	 * Extends InfiniteFragmentStatePagerAdapter and implements the needed methods to allow
	 * retrieving and manipulation of the number of Fragments visible in the InfiniteViewPager
	 * this InfiniteFragmentStatePagerAdapter is attached to.
	 * 
	 * @author Adam Parr
	 *
	 */
	public class DemoInfiniteFragmentStatePagerAdapter extends InfiniteFragmentStatePagerAdapter {
		/**
		 * Key to use in saving and restoring the state of the {@link #tabNames} ArrayList.
		 */
		public static final String TAB_NAMES = "DemoInfinatePagerAdapter:tab_names";
		
		/**
		 * Key to use in saving and restoring the state of the boolean value {@link #showExtras}.
		 */
		public static final String SHOW_EXTRA = "DemoInfinatePagerAdapter:show_extra";
		
		/**
		 * The Tab names to the associated fragments in {@link #fragments}.
		 */
		private String[] tabNames = new String[]{"Fragment at position: 0", "Fragment at position: 1", "Fragment at position: 2", "Fragment at position: 3", "Fragment at position: 4"};
		/**
		 * Store all Fragments in this array.
		 */
		private Fragment[] fragments = new Fragment[tabNames.length];
		
		/**
		 * <tt>true</tt> to show the fragments at 3 and 4,
		 * <tt>false</tt> to show only the first three.
		 */
		private boolean showExtras = false;
		
		/**
		 * Constructor that accepts a FragmentManager.
		 * @param fm FragmentManager.
		 */
		public DemoInfiniteFragmentStatePagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Parcelable saveState() {
			Bundle state = new Bundle();
			state.putParcelable("SUPER", super.saveState());
			state.putBoolean(SHOW_EXTRA, showExtras);
			state.putStringArray(TAB_NAMES, tabNames); // Store the title String array to be restored later.
			return state;
		}
		
		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
			tabNames = ((Bundle) state).getStringArray(TAB_NAMES); // Restore the title String array.
			showExtras(((Bundle) state).getBoolean(SHOW_EXTRA, showExtras)); // Update the state.
			
			super.restoreState(((Bundle) state).getParcelable("SUPER"), loader);
			int x = 0;
			for (Fragment f : getFragments()) {
				fragments[x++] = f;
			}
		}
		
		@Override
		public int getRelativeCount() {
			return fragments.length - (showExtras ? 0 : 2);
		}
		
		@Override
		public int getRelativeItemPosition(Object object) {
			int x = 0;
			int length = getRelativeCount();
			for (Fragment f : fragments) {
				if (x >= length) break;
				if (f == object) {
					return x;
				}
				x++;
			}
			return POSITION_NONE;
		}

		@Override
		public Fragment getRelativeItem(int position) {
			/*
			 * Reuse Fragment if it still exists.
			 */
			if(fragments[position] == null) {
				Fragment fragment = new PagerFragment();
				Bundle args = new Bundle();
				args.putString(PagerFragment.TEXT, "Fragment "+position);
				fragment.setArguments(args);
				fragments[position] = fragment;
			}
			return fragments[position];
		}

		@Override
		public CharSequence getRelativePageTitle(int position) {
			return tabNames[position];
		}
		
		public void showExtras(boolean showExtras) {
			this.showExtras = showExtras;
			
			// Alert the PagerAdapter the data has changed and it should update itself
			// to reflect those changes.
			notifyDataSetChanged();
		}
	}
	
	/**
	 * Fragment to instantiate in DemoInfinatePagerAdapter.
	 * 
	 * @author Adam Parr
	 */
	public static class PagerFragment extends Fragment {
		
		/**
		 * Key to use in arguments Bundle to pass a String to the Fragment.
		 */
		public static final String TEXT = "PagerFragment:text";
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment, container, false);
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			((TextView)getView().findViewById(R.id.text)).setText(getArguments().getString(TEXT));
		}
	}
}
