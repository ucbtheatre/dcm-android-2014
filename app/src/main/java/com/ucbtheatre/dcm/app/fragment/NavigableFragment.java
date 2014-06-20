package com.ucbtheatre.dcm.app.fragment;

import android.support.v4.app.Fragment;

/**
 * Created by kurtguenther.
 */
public class NavigableFragment extends Fragment {
    protected NavigationFragment navigationFragment;

    public void setNavigationFragment(NavigationFragment navigationFragment) {
        this.navigationFragment = navigationFragment;
    }
}
