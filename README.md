InfiniePager
============

InfinitePager is a library extending the use of the classes
android.support.v4.view.ViewPager and android.support.v4.app.PagerAdapter to
allow the InfiniteViewPager to behave as if there were an infinite number of
Fragments attached to either side of the ViewPager, allowing the user to
continuously scroll in one direction without reaching the end of the list.
The InfinitePagerAdapter requires there to be at least four items before it can
be an infinitely scrollable ViewPager, behaving as an ordinary ViewPager until
such a time the number of items reaches or exceeds four, at which point it will
behave as an infinitely scrollable ViewPager.

The library provides two PagerAdapters that extend InfinitePagerAdapter,
InfiniteFragmentPagerAdapter and InfiniteFragmentStatePagerAdapter which are
based off the support app.FragmentPagerAdapter and app.FragmentStatePagerAdapter
classes respectively, found in the v4 and v13 support packages.

The library offers support to applications using the v4 support package through
the com.github.paradam.support.v4.infinitepager package, with support for
applications using the v13 support package through
com.github.paradam.infinitepager. Both implementations extend
com.github.paradam.infinitepager.InfinitePagerAdapter meaning both versions are
supported in com.github.paradam.infinitepager.InfiniteViewPager.

For a simple example on using the classes see the example using the v4 support
FragmentStatePagerAdapter class in the InfinitePagerDemo project at
com.github.paradam.infinitepager.demo.v4.InfiniteFragmentStatePagerActivity



Developed By
============

    Adam Parr



License
=======

Copyright 2013 Adam Parr

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
