package com.ucbtheatre.dcm.app.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.ucbtheatre.dcm.app.R;
import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Performance;
import com.ucbtheatre.dcm.app.data.Performer;
import com.ucbtheatre.dcm.app.data.Show;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by kurtguenther.
 */
public class PerformersListFragment extends NavigableFragment {

    protected String mSearchString = "";

    public void setSearchString(String mSearchString, boolean updateResults) {
        this.mSearchString = mSearchString;
        if(updateResults) {
            refreshData();
        }
    }

    public String getSearchString() {
        return mSearchString;
    }

    List<Performer> mAllPerformers;
    List<Performer> mFilteredPerformers;

    StickyListHeadersListView listView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View retVal = inflater.inflate(R.layout.fragment_performers_list, container, false);
        listView = (StickyListHeadersListView) retVal.findViewById(R.id.fragment_performers_list);

        listView.setAdapter(new PerformersListAdapter(getActivity(), 0));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Performer perf = (Performer) adapterView.getItemAtPosition(position);

                PerformerFragment performerFragment= new PerformerFragment();
                Bundle dataBundle = new Bundle();
                dataBundle.putSerializable(PerformerFragment.EXTRA_PERFORMER, perf);
                performerFragment.setArguments(dataBundle);
                performerFragment.setNavigationFragment(navigationFragment);

                navigationFragment.pushFragment(performerFragment);
            }
        });

        listView.setFastScrollEnabled(true);

        return retVal;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshData();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationFragment = null;
    }

    public void refreshData() {
        if(mAllPerformers == null){
            try {
                mAllPerformers = DatabaseHelper.getSharedService().getPerformerDAO().queryBuilder().orderBy("firstName", true).query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        mFilteredPerformers = new ArrayList<Performer>();
        for(Performer p : mAllPerformers){
            if(performerMatchesFilter(p, getSearchString())){
                mFilteredPerformers.add(p);
            }
        }

        ArrayAdapter<Performer> adpt = (ArrayAdapter) listView.getAdapter();
        adpt.clear();
        adpt.addAll(mFilteredPerformers);
        adpt.notifyDataSetChanged();
    }

    protected boolean performerMatchesFilter(Performer performer, String filter){
        String newFilter = filter.toLowerCase();
        if(filter.contains(" "))
        {
            return performer.toString().toLowerCase().startsWith(newFilter);
        }
        return performer.firstName.toLowerCase().startsWith(newFilter) || performer.lastName.toLowerCase().startsWith(newFilter);
    }

    public class PerformersListAdapter extends ArrayAdapter<Performer> implements StickyListHeadersAdapter, SectionIndexer {

        private LayoutInflater inflater;

        public PerformersListAdapter(Context context, int resource) {
            super(context, resource);
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            TextView nameView = (TextView) convertView.findViewById(android.R.id.text1);
            nameView.setText(getItem(position).toString());

            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                holder = new HeaderViewHolder();
                convertView = inflater.inflate(R.layout.header_layout, parent, false);
                holder.text = (TextView) convertView.findViewById(R.id.header_text);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            //set header text as first char in name
            String headerText = getHeaderString(getItem(position));
            holder.text.setText(headerText);
            return convertView;
        }

        protected String getHeaderString(Performer performer) {
            String retVal = "" + performer.firstName.charAt(0);
            return retVal;
        }

        @Override
        public long getHeaderId(int position) {
            return getHeaderString(getItem(position)).hashCode();
        }

        private ArrayList<String> indexes;
        private ArrayList<Integer> calculatedPositions;

        @Override
        public Object[] getSections() {
            if(indexes == null){
                indexes = new ArrayList<String>();
            }
            return indexes.toArray();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();


            indexes = new ArrayList<String>();
            calculatedPositions = new ArrayList<Integer>();

            char prev = 0;
            for(int i = 0; i < getCount(); i ++){
                Performer p = getItem(i);
                if(p.firstName.charAt(0) != prev){
                    prev = p.firstName.charAt(0);
                    indexes.add(prev + "");
                    calculatedPositions.add(i);
                }
            }
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return calculatedPositions.get(sectionIndex);
        }

        @Override
        public int getSectionForPosition(int position) {
            Performer p = getItem(position);
            return indexes.indexOf("" + p.firstName.toUpperCase().charAt(0));
        }

        class HeaderViewHolder {
            TextView text;
        }


    }
}
