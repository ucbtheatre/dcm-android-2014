package com.ucbtheatre.dcm.app.fragment;

import android.support.v4.app.ListFragment;

/**
 * Created by kurtguenther.
 */
public class NavigableListFragment extends ListFragment {
    protected NavigationFragment navigationFragment;

    public void setNavigationFragment(NavigationFragment navigationFragment) {
        this.navigationFragment = navigationFragment;
    }
}
