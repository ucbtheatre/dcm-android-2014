package com.ucbtheatre.dcm.app.activity;

import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.ucbtheatre.dcm.app.R;
import com.ucbtheatre.dcm.app.data.DataService;
import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Show;
import com.ucbtheatre.dcm.app.fragment.FavoritesFragment;
import com.ucbtheatre.dcm.app.fragment.NavigationFragment;
import com.ucbtheatre.dcm.app.fragment.NowFragment;
import com.ucbtheatre.dcm.app.fragment.ShowFragment;
import com.ucbtheatre.dcm.app.fragment.ShowsListFragment;
import com.ucbtheatre.dcm.app.fragment.VenuesListFragment;

import org.json.JSONObject;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener {

    public static final int SEARCH_REQUEST_CODE = 0x01;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager mViewPager;
    protected Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the services
        DatabaseHelper.initialize(this);
        DataService.initialize(this);
        if(DataService.getSharedService().shouldUpdate()){
            Log.d(MainActivity.class.getName(), "Refreshing data");
            DataService.getSharedService().refreshDataFromServer(new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(JSONObject response) {
                    super.onSuccess(response);

                    mViewPager.getAdapter().notifyDataSetChanged();
                }
            });
        } else {
            Log.d(MainActivity.class.getName(), "Not refreshing data");
        }


        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_main_refresh) {
            Log.d(MainActivity.class.getName(), "Refreshing data");
            DataService.getSharedService().refreshDataFromServer(new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(JSONObject response) {
                    super.onSuccess(response);

                    mViewPager.getAdapter().notifyDataSetChanged();
                }
            });
            return true;
        } else if(id == R.id.menu_main_about){
            Intent aboutIntent = new Intent(this, AboutActivity.class);
            startActivity(aboutIntent);
        } else if(id == R.id.menu_main_search){
            Intent searchIntent = new Intent(this, SearchActivity.class);
            startActivityForResult(searchIntent, SEARCH_REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> frags = fm.getFragments();
        if(frags != null) {
            currentFragment = fm.getFragments().get(tab.getPosition());
            Log.d(MainActivity.class.getName(), "Switching to tab " + Integer.toString(tab.getPosition()));
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public String[] titles = new String[]{
                getResources().getString(R.string.title_section_now),
                getResources().getString(R.string.title_section_shows),
                getResources().getString(R.string.title_section_venues),
                getResources().getString(R.string.title_section_favorites)};

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0: {
                    NowFragment list = new NowFragment();
                    NavigationFragment retVal = new NavigationFragment();

                    retVal.setRootFragment(list);
                    list.setNavigationFragment(retVal);
                    return retVal;
                }
                case 1: {
                    ShowsListFragment list = new ShowsListFragment();
                    NavigationFragment retVal = new NavigationFragment();

                    retVal.setRootFragment(list);
                    list.setNavigationFragment(retVal);
                    return retVal;
                }
                case 2: {
                    VenuesListFragment list = new VenuesListFragment();
                    NavigationFragment retVal = new NavigationFragment();

                    retVal.setRootFragment(list);
                    list.setNavigationFragment(retVal);
                    return retVal;
                }
                case 3: {
                    FavoritesFragment list = new FavoritesFragment();
                    NavigationFragment retVal = new NavigationFragment();

                    retVal.setRootFragment(list);
                    list.setNavigationFragment(retVal);
                    return retVal;
                }
            }
            return null;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            return titles[position];
        }
    }


    @Override
    public void onBackPressed() {

        // if there is a fragment and the back stack of this fragment is not empty,
        // then emulate 'onBackPressed' behaviour, because in default, it is not working
        if(currentFragment != null){
            FragmentManager childFm = currentFragment.getChildFragmentManager();
            if (childFm.getBackStackEntryCount() > 0) {
                childFm.popBackStack();
                return;
            }
        } else {
            //HACK: this assumes the current Fragment is the first if it's null
            //This is because the tabs are set before the fragments made
            FragmentManager fm = getSupportFragmentManager();
            Fragment current = fm.getFragments().get(0);

            FragmentManager childFm = current.getChildFragmentManager();
            if (childFm.getBackStackEntryCount() > 0) {
                childFm.popBackStack();
                return;
            }
        }

        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

}
