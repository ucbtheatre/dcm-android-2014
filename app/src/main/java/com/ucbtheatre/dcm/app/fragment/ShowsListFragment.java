package com.ucbtheatre.dcm.app.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.ucbtheatre.dcm.app.R;

import com.ucbtheatre.dcm.app.activity.MainActivity;
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
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ShowsListFragment extends Fragment {


    public StickyListHeadersListView listView;

    public ShowsListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View retVal = inflater.inflate(R.layout.fragment_shows_list, container, false);

        listView = (StickyListHeadersListView) retVal.findViewById(R.id.fragment_shows_list);

        listView.setAdapter(new ShowsListAdapter(getActivity(), 0));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Show show = (Show) adapterView.getAdapter().getItem(position);

                Intent showIntent = new Intent(getActivity(), ShowActivity.class);
                showIntent.putExtra(ShowFragment.EXTRA_SHOW, show);
                startActivity(showIntent);
//                if (navigationFragment != null) {
//                    ShowFragment showFragment = new ShowFragment();
//                    Bundle dataBundle = new Bundle();
//                    dataBundle.putSerializable(ShowFragment.EXTRA_SHOW, show);
//                    showFragment.setArguments(dataBundle);
//
//                    navigationFragment.pushFragment(showFragment);
//                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Show show = (Show) adapterView.getAdapter().getItem(position);
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

        //Listen for changes to the favorites
        IntentFilter filter = new IntentFilter();
        filter.addAction(Performance.FAVORITE_UPDATE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshData();
            }
        }, filter);


        refreshData();

        //HACK: this is due to a known error in KitKat, we have to delay the setting of Fast Scroll
        //Otherwise it won't work on the shows page
        //Reference: http://stackoverflow.com/questions/20680329/listview-fast-scroll-bar-not-visible-for-first-time-in-4-4-kitkat
        if (Build.VERSION.SDK_INT >= 19) {
            listView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onGlobalLayout() {
                    listView.setFastScrollEnabled(true);
                    if (Build.VERSION.SDK_INT >= 16) {
                        listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        } else {
            listView.setFastScrollEnabled(true);
        }

        setHasOptionsMenu(true);

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
    }


    private class ShowsListAdapter extends ArrayAdapter<Show> implements StickyListHeadersAdapter, SectionIndexer {
        private LayoutInflater inflater;

        public ShowsListAdapter(Context context, int resource) {
            super(context, resource);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            Show show = getItem(position);

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                holder.text = (TextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(show.toString());

            //set the background based on favorite status
            if(show.getIsFavorite()) {
                convertView.setBackgroundColor(getResources().getColor(R.color.favorite_background));
            } else {
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


        //// SectionIndex methods

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

            String prev = "";
            for(int i = 0; i < getCount(); i ++){
                Show p = getItem(i);
                if(!getHeaderString(p).equals(prev)){
                    prev = getHeaderString(p);
                    indexes.add(prev);
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
            //Hack: not quite sure why we need this here
            //but it was causing index out of bounds exceptions
            //I think it's because the list radically changes in size quickly
            //And the quick scroll menu comes up randomly
            if(position >= getCount()) {
                position = 0;
            }
            Show p = getItem(position);
            return indexes.indexOf(getHeaderString(p));
        }
    }
}
