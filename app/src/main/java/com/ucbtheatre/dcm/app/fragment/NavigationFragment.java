package com.ucbtheatre.dcm.app.fragment;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ucbtheatre.dcm.app.R;
import com.ucbtheatre.dcm.app.data.Show;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class NavigationFragment extends Fragment {

    public interface NavigationFragmentListener {
        void pushFragment(Fragment fragment);
    }

    Fragment rootFragment;

    public NavigationFragment() {
        // Required empty public constructor
    }

    public NavigationFragment(Fragment rootFragment){
        this.rootFragment = rootFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View retVal = inflater.inflate(R.layout.fragment_navigation, container, false);

        FragmentTransaction trans = getChildFragmentManager().beginTransaction();
        trans.add(R.id.fragment_navigation_fragment, this.rootFragment);
        trans.commit();

        return retVal;
    }


    public void pushFragment(Fragment newFragment) {
        android.support.v4.app.FragmentTransaction trans = getChildFragmentManager().beginTransaction();
        trans.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right);
        Fragment currentFragment = getChildFragmentManager().getFragments().get(0);
        trans.hide(currentFragment);
        trans.add(R.id.fragment_navigation_fragment, newFragment);
        trans.addToBackStack(null);
        trans.commit();
    }

    //TODO: need to save the rootFragment for orientation changes
}
