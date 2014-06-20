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

    public static final String FRAGMENT_CLASS = "fragmentClass";
    public static final String FRAGMENT_ARGUMENTS = "fragmentArguments";

    public interface NavigationFragmentListener {
        void pushFragment(Fragment fragment);
    }

    Fragment rootFragment;

    public void setRootFragment(Fragment rootFragment) {
        this.rootFragment = rootFragment;
    }

    public NavigationFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View retVal = inflater.inflate(R.layout.fragment_navigation, container, false);

        //Try to load the saved instance then
        if(rootFragment == null && savedInstanceState != null) {
            Class savedClass = (Class) savedInstanceState.getSerializable(FRAGMENT_CLASS);
            Bundle arguments = savedInstanceState.getBundle(FRAGMENT_ARGUMENTS);
            try {
                rootFragment = (Fragment) savedClass.newInstance();
                rootFragment.setArguments(arguments);

            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if(rootFragment != null) {
            FragmentTransaction trans = getChildFragmentManager().beginTransaction();
            trans.add(R.id.fragment_navigation_fragment, this.rootFragment);
            trans.commit();
        }


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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(FRAGMENT_CLASS, rootFragment.getClass());
        outState.putBundle(FRAGMENT_ARGUMENTS, rootFragment.getArguments());
    }

    //TODO: need to save the rootFragment for orientation changes
}
