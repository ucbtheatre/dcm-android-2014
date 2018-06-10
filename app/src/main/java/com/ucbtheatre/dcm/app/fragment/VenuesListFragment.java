package com.ucbtheatre.dcm.app.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.internal.zzi;
import com.ucbtheatre.dcm.app.R;

import com.ucbtheatre.dcm.app.activity.VenueActivity;
import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Venue;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class VenuesListFragment extends Fragment {

    enum ViewType{
        MAP_VIEW,
        LIST_VIEW
    }

    public VenuesListFragment() { }

    List<Venue> venues;
    ListView listView;
    MapView mapView;

    private ViewType state;
    public void setViewType(ViewType type){
        state = type;

        switch (type){
            case MAP_VIEW:
                listView.setVisibility(View.INVISIBLE);
                mapView.setVisibility(View.VISIBLE);
                mapView.setEnabled(true);
                break;
            case LIST_VIEW:
                listView.setVisibility(View.VISIBLE);
                mapView.setVisibility(View.INVISIBLE);
                mapView.setEnabled(false);
                break;
        }
    }

    public ViewType getViewType(){
        return state;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            venues = DatabaseHelper.getSharedService().getVenueDAO().queryBuilder().orderBy("short_name",false).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_venues, menu);
        MenuItem mapListIcon = menu.findItem(R.id.action_map_list);
        if(getViewType() == ViewType.MAP_VIEW){
            mapListIcon.setTitle("List");
            mapListIcon.setIcon(R.drawable.ic_list);
        } else {
            mapListIcon.setTitle("Map");
            mapListIcon.setIcon(android.R.drawable.ic_menu_mapmode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_map_list){
            if(getViewType() == ViewType.MAP_VIEW){
                setViewType(ViewType.LIST_VIEW);
            } else {
                setViewType(ViewType.MAP_VIEW);
            }
            getActivity().invalidateOptionsMenu();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showVenue(Venue v){
        Intent showVenueIntent = new Intent(getActivity(), VenueActivity.class);
        showVenueIntent.putExtra(VenueFragment.EXTRA_VENUE, v);
        startActivity(showVenueIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View retVal = inflater.inflate(R.layout.fragment_venues, container, false);
        retVal.setBackgroundColor(Color.WHITE);

        mapView = (MapView) retVal.findViewById(R.id.map);
        listView = (ListView) retVal.findViewById(android.R.id.list);
        listView.setAdapter(new ArrayAdapter<Venue>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, venues));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Venue venue = venues.get(position);
                showVenue(venue);
            }
        });


        mapView.onCreate(savedInstanceState);
        mapView.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.737417, -73.988833), 13));
        for(Venue v : venues){
            mapView.getMap().addMarker(new MarkerOptions()
                    .position(new LatLng(v.lat, v.lng))
                    .title(v.short_name)
                    .snippet(v.address));
        }

        //Handle the venues that are in the same lat/lon
        mapView.getMap().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //else check if it's a dupe
                final Venue venue = getVenueFromMarker(marker);

                List<String> matchingLabels = new ArrayList<String>();
                final List<Venue> matchingVenues = new ArrayList<Venue>();
                for(final Venue other : venues){
                    if(other.lat == venue.lat && other.lng == venue.lng){
                        //Found a match
                        matchingLabels.add(other.short_name);
                        matchingVenues.add(other);

                    }
                }

                if(matchingVenues.size() > 1){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setItems(matchingLabels.toArray(new String[matchingLabels.size()]), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            showVenue(matchingVenues.get(which));
                        }
                    });
                    builder.create().show();
                    return true;
                } else {
                    return false;
                }
            }
        });

        //When you click on an InfoWindow -> show the venue
        mapView.getMap().setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Venue v = getVenueFromMarker(marker);
                showVenue(v);
            }
        });

        //Start on the Map View
        setViewType(ViewType.MAP_VIEW);

        return retVal;
    }

    private Venue getVenueFromMarker(Marker marker){
        for(Venue v : venues){
            if(v.short_name.equalsIgnoreCase(marker.getTitle())){
                return v;
            }
        }

        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
