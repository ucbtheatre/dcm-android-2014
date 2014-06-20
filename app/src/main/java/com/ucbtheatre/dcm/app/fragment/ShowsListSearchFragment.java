package com.ucbtheatre.dcm.app.fragment;

import android.widget.ArrayAdapter;

import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Show;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kurtguenther.
 */
public class ShowsListSearchFragment extends ShowsListFragment {

    public ShowsListSearchFragment() {}

    protected String mSearchString = "";

    public void setSearchString(String mSearchString, boolean updateResults) {
        this.mSearchString = mSearchString;
        if(updateResults) {
            refreshData();
        }
    }

    protected List<Show> mAllShows;
    protected List<Show> mFilteredShows;

    public String getSearchString() {
        return mSearchString;
    }

    @Override
    public void refreshData() {
        if(mAllShows == null){
            try {
                mAllShows = DatabaseHelper.getSharedService().getShowDAO().queryBuilder().orderBy("sortName", true).query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        mFilteredShows = new ArrayList<Show>();
        for(Show s : mAllShows){
            if(showMatchesFilter(s,getSearchString())){
                mFilteredShows.add(s);
            }
        }

        ArrayAdapter<Show> adpt = (ArrayAdapter) listView.getAdapter();
        adpt.clear();
        adpt.addAll(mFilteredShows);
        adpt.notifyDataSetChanged();
    }

    protected boolean showMatchesFilter(Show show, String filter){
        String newFilter = filter.toLowerCase();

        //Split on anything that isn't a character, so we only match word starts...
        //bit matches "The 8-bit Show" but not "blahbit"
        String[] parts = show.name.toLowerCase().split("\\W+");
        for(String p : parts){
            if(p.startsWith(newFilter)){
                return true;
            }
        }
        return false;

    }

}
