package com.ucbtheatre.dcm.app.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ucbtheatre.dcm.app.R;
import com.ucbtheatre.dcm.app.activity.ShowActivity;
import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Performance;
import com.ucbtheatre.dcm.app.data.Show;
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
public class VenueFragment extends Fragment implements Serializable {

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

        venue = (Venue) getActivity().getIntent().getSerializableExtra(EXTRA_VENUE);

        performances = new ArrayList<Performance>();

        //Listen for changes to the favorites
        IntentFilter filter = new IntentFilter();
        filter.addAction(Performance.FAVORITE_UPDATE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshData();
            }
        }, filter);


        listView = (StickyListHeadersListView) getView().findViewById(R.id.fragment_venue_shows_list);
        listView.setAdapter(new PerformanceListAdapter(getActivity(), performances));
        refreshData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Performance perf = (Performance) adapterView.getItemAtPosition(position);

                if(!perf.show.isNotRealShow()) {
                    Intent showIntent = new Intent(getActivity(), ShowActivity.class);
                    showIntent.putExtra(ShowFragment.EXTRA_SHOW, perf.show);
                    startActivity(showIntent);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Performance perf = (Performance) adapterView.getItemAtPosition(position);

                Show show = perf.show;
                if(!show.isNotRealShow()) {
                    boolean newFavoriteValue = !show.getIsFavorite();
                    show.setIsFavorite(newFavoriteValue);

                    if (newFavoriteValue) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_favorite_added), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_favorite_removed), Toast.LENGTH_SHORT).show();
                    }
                }

                return !show.isNotRealShow();
            }
        });


        title = (TextView) getView().findViewById(R.id.fragment_venue_title);
        title.setText(venue.short_name);

        TextView address = (TextView) getView().findViewById(R.id.fragment_venue_address);
        address.setText(venue.address);
    }

    protected void refreshData() {
        try {
            performances = DatabaseHelper.getSharedService().getPerformanceDAO().queryBuilder().orderBy("start_date", true).where().eq("venue_id",venue.id).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PerformanceListAdapter adpt = (PerformanceListAdapter) listView.getAdapter();
        adpt.performances = performances;
        adpt.notifyDataSetChanged();
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

            //set the background based on favorite status
            if(performances.get(position).getIsFavorite()) {
                convertView.setBackgroundColor(getResources().getColor(R.color.favorite_background));
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }

            //Handle theatre cleanings
            if(performances.get(position).show.isNotRealShow()){
                holder.showTitle.setTextColor(Color.WHITE);
                holder.time.setTextColor(Color.WHITE);
                convertView.setBackgroundColor(getResources().getColor(R.color.theatre_cleaning_background));
            } else {
                holder.showTitle.setTextColor(Color.BLACK);
                holder.time.setTextColor(Color.BLACK);
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }


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
