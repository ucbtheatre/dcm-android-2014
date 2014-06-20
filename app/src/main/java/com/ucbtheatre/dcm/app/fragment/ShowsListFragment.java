package com.ucbtheatre.dcm.app.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ucbtheatre.dcm.app.R;

import com.ucbtheatre.dcm.app.activity.MainActivity;
import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Show;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ShowsListFragment extends NavigableFragment {


    public StickyListHeadersListView listView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ShowsListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View retVal = inflater.inflate(R.layout.fragment_shows_list, container, false);

        listView = (StickyListHeadersListView) retVal.findViewById(R.id.fragment_shows_list);

        listView.setAdapter(new ShowsListAdapter(getActivity(),0));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Show show = (Show) adapterView.getAdapter().getItem(position);
                if(navigationFragment != null){
                    ShowFragment showFragment = new ShowFragment();
                    Bundle dataBundle = new Bundle();
                    dataBundle.putSerializable(ShowFragment.EXTRA_SHOW, show);
                    showFragment.setArguments(dataBundle);

                    navigationFragment.pushFragment(showFragment);
                }
            }
        });

        refreshData();

        return retVal;
    }

    public void refreshData() {
        try {
            List<Show> shows = DatabaseHelper.getSharedService().getShowDAO().queryBuilder().orderBy("sortName", true).query();
            ArrayAdapter<Show> adpt = (ArrayAdapter) listView.getAdapter();
            adpt.clear();
            adpt.addAll(shows);
            adpt.notifyDataSetChanged();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationFragment = null;
    }


    private class ShowsListAdapter extends ArrayAdapter<Show> implements StickyListHeadersAdapter {
        private LayoutInflater inflater;

        public ShowsListAdapter(Context context, int resource) {
            super(context, resource);
            inflater = LayoutInflater.from(context);
        }

//        @Override
//        public int getCount() {
//            return shows.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return shows.get(position);
//        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                holder.text = (TextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(getItem(position).toString());

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

        protected String getHeaderString(Show show) {
            String retVal = "";
            Character f = show.sortName.subSequence(0, 1).charAt(0);
            if(Character.isLetter(f)) {
                retVal = "" + f;
                retVal = retVal.toUpperCase();
            } else {
                return "#";
            }
            return retVal;
        }

        @Override
        public long getHeaderId(int position) {
            return getHeaderString(getItem(position)).charAt(0);
        }

        class HeaderViewHolder {
            TextView text;
        }

        class ViewHolder {
            TextView text;
        }
    }
}
