Change Log
===============================================================================

Version 1.0.1 *(2013-13-12)*
----------------------------
**com.github.paradam.infinitepager.InfinitePagerAdapter**
 * Added protected method `onPreNotifyDataSetChanged`, called at the begining
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

Version 1.0.0 *(2013-17-11)*
----------------------------
Initial release.
