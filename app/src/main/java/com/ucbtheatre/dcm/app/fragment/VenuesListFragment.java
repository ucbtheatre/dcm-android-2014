package com.ucbtheatre.dcm.app.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.ucbtheatre.dcm.app.R;

import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Venue;


import java.sql.SQLException;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class VenuesListFragment extends NavigableListFragment {

    public VenuesListFragment() { }

    List<Venue> venues;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            venues = DatabaseHelper.getSharedService().getVenueDAO().queryBuilder().orderBy("name",true).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setListAdapter(new ArrayAdapter<Venue>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, venues));
    }


    @Override
    public void onDetach() {
        super.onDetach();
        navigationFragment = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if(navigationFragment != null){
            Venue venue = venues.get(position);

            VenueFragment venueFragment = new VenueFragment();
            Bundle dataBundle = new Bundle();
            dataBundle.putSerializable(VenueFragment.EXTRA_VENUE, venue);
            venueFragment.setArguments(dataBundle);

            venueFragment.setNavigationFragment(navigationFragment);

            navigationFragment.pushFragment(venueFragment);
        }
    }

}
