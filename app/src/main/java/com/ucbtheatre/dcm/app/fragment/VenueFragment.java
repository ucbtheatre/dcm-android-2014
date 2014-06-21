package com.ucbtheatre.dcm.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ucbtheatre.dcm.app.R;
import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Performance;
import com.ucbtheatre.dcm.app.data.Venue;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VenueFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VenueFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class VenueFragment extends NavigableFragment implements Serializable {

    public static final String EXTRA_VENUE = "com.ucbtheatre.dcm.app.extra-venue";

    public VenueFragment() {}

    Venue venue;
    List<Performance> performances;

    StickyListHeadersListView listView;
    TextView title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View retVal = inflater.inflate(R.layout.fragment_venue, container, false);
        return retVal;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        venue = (Venue) getArguments().getSerializable(EXTRA_VENUE);

        performances = new ArrayList<Performance>();
        try {
            performances = DatabaseHelper.getSharedService().getPerformanceDAO().queryBuilder().orderBy("start_date", true).where().eq("venue_id",venue.id).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        listView = (StickyListHeadersListView) getView().findViewById(R.id.fragment_venue_shows_list);
        listView.setAdapter(new PerformanceListAdapter(getActivity(), performances));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Performance perf = (Performance) adapterView.getItemAtPosition(position);

                ShowFragment showFragment= new ShowFragment();
                Bundle dataBundle = new Bundle();
                dataBundle.putSerializable(ShowFragment.EXTRA_SHOW, perf.show);
                showFragment.setArguments(dataBundle);

                navigationFragment.pushFragment(showFragment);
            }
        });

        title = (TextView) getView().findViewById(R.id.fragment_venue_title);
        title.setText(venue.name);

        ImageButton mapButton = (ImageButton) getView().findViewById(R.id.fragment_venue_map);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(venue.gmaps));
                startActivity(mapIntent);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    public class PerformanceListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

        private List<Performance> performances;
        private LayoutInflater inflater;

        public PerformanceListAdapter(Context context, List<Performance> performances) {
            this.performances = performances;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return performances.size();
        }

        @Override
        public Object getItem(int position) {
            return performances.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.listview_venue_show, parent, false);
                holder.showTitle = (TextView) convertView.findViewById(R.id.listview_venue_show_title);
                holder.time = (TextView) convertView.findViewById(R.id.listview_venue_show_time);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.showTitle.setText(performances.get(position).toString());
            holder.time.setText(performances.get(position).getStartTime());

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
            String headerText = getHeaderString(performances.get(position));
            holder.text.setText(headerText);
            return convertView;
        }

        protected String getHeaderString(Performance perf) {
            String retVal = perf.getStartDay();
            return retVal;
        }

        @Override
        public long getHeaderId(int position) {
            return getHeaderString(performances.get(position)).hashCode();
        }

        class HeaderViewHolder {
            TextView text;
        }

        class ViewHolder {
            TextView showTitle;
            TextView time;
        }
    }
}
