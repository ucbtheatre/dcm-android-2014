package com.ucbtheatre.dcm.app.fragment;



import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ucbtheatre.dcm.app.R;
import com.ucbtheatre.dcm.app.activity.ShowActivity;
import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Performance;
import com.ucbtheatre.dcm.app.data.Show;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class FavoritesFragment extends Fragment {

    StickyListHeadersListView listView;
    View emptyView;

    public FavoritesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Performance.FAVORITE_UPDATE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshData();
            }
        }, filter);

        setHasOptionsMenu(true);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View retVal = inflater.inflate(R.layout.fragment_favorites, container, false);
        retVal.setBackgroundColor(Color.WHITE);

        emptyView = retVal.findViewById(android.R.id.empty);

        listView = (StickyListHeadersListView) retVal.findViewById(R.id.fragment_favorites_list);
        listView.setAdapter(new FavoritesListAdapter(getActivity(), new ArrayList<Performance>()));
        listView.setEmptyView(emptyView);
        refreshData();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            Performance perf = (Performance) adapterView.getItemAtPosition(position);

            Intent showIntent = new Intent(getActivity(), ShowActivity.class);
            showIntent.putExtra(ShowFragment.EXTRA_SHOW, perf.show);
            startActivity(showIntent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Performance perf = (Performance) adapterView.getItemAtPosition(position);

                Show show = perf.show;
                boolean newFavoriteValue = !show.getIsFavorite();
                show.setIsFavorite(newFavoriteValue);

                if(newFavoriteValue) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_favorite_added), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_favorite_removed), Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        return retVal;
    }

    public void refreshData() {
        FavoritesListAdapter adpt = (FavoritesListAdapter) listView.getAdapter();
        try {
            adpt.performances = DatabaseHelper.getSharedService().getPerformanceDAO().queryBuilder().orderBy("start_date", true).where().eq("isFavorite",true).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        adpt.notifyDataSetChanged();
    }

    public class FavoritesListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

        public List<Performance> performances;
        private LayoutInflater inflater;

        public FavoritesListAdapter(Context context, List<Performance> performances) {
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
                convertView = inflater.inflate(R.layout.listview_favorites_show, parent, false);
                holder.showTitle = (TextView) convertView.findViewById(R.id.listview_favorites_show_title);
                holder.time = (TextView) convertView.findViewById(R.id.listview_favorites_show_time);
                holder.location = (TextView) convertView.findViewById(R.id.listview_favorites_show_location);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.showTitle.setText(performances.get(position).toString());
            holder.time.setText(performances.get(position).getStartTime());
            holder.location.setText(performances.get(position).venue.short_name);

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
            TextView location;
        }
    }

}
