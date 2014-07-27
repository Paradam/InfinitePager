Change Log
===============================================================================

Version 1.0.2 *(2014-08-27)*
----------------------------
 **Converted to Android Studio Project with Gradle support**
 
 **com.github.paradam.infinitepager.InfiniteFragmentPagerAdapter**
 **com.github.paradam.support.v4.infinitepager.InfiniteFragmentPagerAdapter**
  * Better support for updating FragmentPagerAdapter upon the data changing that
    could cause the ViewPager to show a different order of Fragments or a
    different set of Fragments altogether.
    
 **com.github.paradam.infinitepager.InfiniteViewPager**
  * Support for switching to adjacent Fragment at the other end of the list through
    the `setRelativeCurrentItem` methods.
    
 **android.support.v4.view.SlidablePagerTitle**
 **android.support.v4.view.SlidableTabLayout**
 **android.support.v4.view.SlidableTabStrip**
  * Based off Sample code from developer.android.com; SlidingTabColors
    (https://developer.android.com/samples/SlidingTabsColors/index.html), an
    implementation that supports the InfiniteViewPager and InfinitePagerAdapter.
  * The selected indicator being able to be at both the start and end of the tab list
    when swiping from end to start or start to end in an InfiniteViewPager.
  * SlidingTabLayout can be hidden and shown, with sliding up/down animations.
  * Similar to android.support.v4.view.PagerTabStrip, a `SlidableTabLayout` can be
    used as a child to android.support.v4.view.ViewPager with it being linked to the
    ViewPager automatically without needing to link in Java. Can also be linked by
    assigning `viewPager` to the id of the ViewPager within the same layout parent
    in the XML layout file.

Version 1.0.1 *(2013-12-13)*
----------------------------
**com.github.paradam.infinitepager.InfinitePagerAdapter**
 * Added protected method `onPreNotifyDataSetChanged`, called at the beginning
   of `onNotifyDataSetChanged` or in child classes needing to update the count
   of pages or update the margin in the PagerAdapter before updating the data set.

**com.github.paradam.infinitepager.InfiniteFragmentPagerAdapter**
**com.github.paradam.support.v4.infinitepager.InfiniteFragmentPagerAdapter**
 * Fix: Issue where Fragments would not be restored correctly on Activity
   restore if the Fragment is not currently visible or beside the current visible
   Fragment.
 * `onPreNotifyDataSetChanged` is called at the start of `onNotifyDataSetChanged`
   with the call to the super method moved to the bottom, after the list of
   Fragments are updated to reflect their currnet positions.

Version 1.0.0 *(2013-11-17)*
----------------------------
Initial release.
