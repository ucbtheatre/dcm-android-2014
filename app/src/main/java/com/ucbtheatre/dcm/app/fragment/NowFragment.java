package com.ucbtheatre.dcm.app.fragment;



import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ucbtheatre.dcm.app.R;
import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Performance;
import com.ucbtheatre.dcm.app.data.Venue;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class NowFragment extends Fragment {

    //June 27, 2014, 4 PM EST, pulled from http://www.unixtimestamp.com/ and then EST corrected
    public static final Date MARATHON_START_DATE = new Date(((long)1435334400 + 4*60*60) * 1000);
    //June 29, 2014, 11:30 PM EST, pulled from http://www.unixtimestamp.com/
    public static final Date MARATHON_END_DATE = new Date(((long)1435534200 + 4*60*60) * 1000);

    private Handler clock;
    private Runnable updateClockRunnable = new Runnable() {
        @Override
        public void run() {
            updateClock();
            clock.postDelayed(this, 1000);
        }
    };

    public NowFragment() {}

    View timerContainer;
    View scheduleContainer;
    View endContainer;

    List<View> containers;
    TextView timer;
    StickyListHeadersListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View retVal = inflater.inflate(R.layout.fragment_now, container, false);

        timer = (TextView) retVal.findViewById(R.id.fragment_now_timer);


        containers = new ArrayList<View>();

        timerContainer=  retVal.findViewById(R.id.fragment_now_time_container);
        containers.add(timerContainer);
        scheduleContainer = retVal.findViewById(R.id.fragment_now_shows_container);
        containers.add(scheduleContainer);
        endContainer = retVal.findViewById(R.id.fragment_now_end_container);
        containers.add(endContainer);

        listView = (StickyListHeadersListView) retVal.findViewById(R.id.fragment_now_shows_list);
        listView.setAreHeadersSticky(false);
        listView.setAdapter(new NowListAdapter(getActivity(),0,0, new ArrayList<Performance>()));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                Performance perf = (Performance) adapterView.getItemAtPosition(position);
//
//                ShowFragment showFragment= new ShowFragment();
//                Bundle dataBundle = new Bundle();
//                dataBundle.putSerializable(ShowFragment.EXTRA_SHOW, perf.show);
//                showFragment.setArguments(dataBundle);
//
//                navigationFragment.pushFragment(showFragment);
            }
        });

        setHasOptionsMenu(true);

        return retVal;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void showContainer(View container){
        for(View v : containers){
            if(v != container) {
                v.setVisibility(View.GONE);
            }
        }

        container.setVisibility(View.VISIBLE);
    }

    private void updateUI(){
        Date now = new Date();

        if(now.before(MARATHON_START_DATE)) {

            showContainer(timerContainer);

            updateClock();

            clock = new Handler();
            updateClockRunnable.run();

        } else if (now.after(MARATHON_END_DATE)){
            showContainer(endContainer);
        }
        else {
            showContainer(scheduleContainer);

            //Load up some data for the upcoming.
            List<Performance> perfs = getUpcomingShow(now);

            NowListAdapter adpt = (NowListAdapter) listView.getAdapter();
            adpt.clear();
            adpt.addAll(perfs);
            adpt.notifyDataSetChanged();
        }
    }

    protected List<Performance> getUpcomingShow(Date date){
        ArrayList<Performance> retVal = new ArrayList<Performance>();

        List<Venue> venues = null;
        try {
            venues = DatabaseHelper.getSharedService().getVenueDAO().queryBuilder().orderBy("name",true).query();

            Date now = new Date();
            long nowSeconds = now.getTime() / (long) 1000;
            Date upperLimit = new Date();
            upperLimit.setTime(now.getTime() + (60 * 60 * 4 * 1000));
            long upperSeconds = upperLimit.getTime() / (long) 1000;

            for(Venue v : venues){
                List<Performance> matches = DatabaseHelper.getSharedService().getPerformanceDAO().queryBuilder().limit(6L).orderBy("start_date", true).where().eq("venue_id", v).and().between("end_date", nowSeconds, upperSeconds).query();
                //SELECT * FROM performance p LEFT JOIN show s on p.show_id = s.id where venue_id = ? and end_date > ? ORDER BY end_date LIMIT 2", new String[]{Integer.toString(v.id), Long.toString(start_date)});
                retVal.addAll(matches);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        return retVal;

    }

    private void updateClock(){
        Date now = new Date();

        int secondsBetween = (int) ((MARATHON_START_DATE.getTime() - now.getTime()) / 1000);

        if(secondsBetween < 0){
            clock.removeCallbacks(updateClockRunnable);
            updateUI();
        }

        //TextView days = (TextView) getView().findViewById(R.id.countdown_days);
        int daysVal = (secondsBetween / (60 * 60 * 24));
        //days.setText(formatInt(daysVal));

        //TextView hours = (TextView) getView().findViewById(R.id.countdown_hours);
        int hoursVal = (secondsBetween - daysVal * 60 * 60 * 24) / (60 * 60);
        //hours.setText(formatInt(hoursVal));

        //TextView minutes = (TextView) getView().findViewById(R.id.countdown_minutes);
        int minutesVal = (secondsBetween - daysVal * 60 * 60 * 24 - hoursVal * 60 * 60) / 60;
        //minutes.setText(formatInt(minutesVal));

        //TextView seconds = (TextView) getView().findViewById(R.id.countdown_seconds);
        int secondsVal = (secondsBetween - daysVal * 60 * 60 * 24 - hoursVal * 60 * 60 - minutesVal * 60);
        //seconds.setText(formatInt(secondsVal));

        timer.setText(String.format("%d days\n%d hours\n%d minutes\n%d seconds", daysVal, hoursVal, minutesVal, secondsVal));

    }

    public String formatInt(int val){
        String retVal = Integer.toString(val);
//        if(val < 10 && val >= 0){
//            retVal = "0" + retVal;
//        }
        return retVal;
    }

    @Override
    public void onPause() {
        //Stop the clock when we navigate away (otherwise it fires and gets a null pointer error)
        if(clock != null){
            clock.removeCallbacks(updateClockRunnable);
        }
        super.onPause();
    }

    public class NowListAdapter extends ArrayAdapter<Performance> implements StickyListHeadersAdapter{

        public NowListAdapter(Context context, int resource, int textViewResourceId, List<Performance> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        private class ScheduleHolder{
            TextView time;
            TextView name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                LayoutInflater lf = LayoutInflater.from(getContext());
                convertView = lf.inflate(R.layout.listview_venue_show, parent, false);
                ScheduleHolder holder = new ScheduleHolder();
                holder.name = (TextView) convertView.findViewById(R.id.listview_venue_show_title);
                holder.time = (TextView) convertView.findViewById(R.id.listview_venue_show_time);
                convertView.setTag(holder);
            }

            Performance perf = getItem(position);

            ScheduleHolder sh = (ScheduleHolder) convertView.getTag();
            sh.name.setText(perf.toString());
            sh.time.setText(perf.getStartTime());

            return convertView;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                LayoutInflater lf = LayoutInflater.from(getContext());
                convertView = lf.inflate(R.layout.header_layout, parent, false);
            }
            TextView title = (TextView) convertView.findViewById(R.id.header_text);

            Performance performance = getItem(position);
            title.setText(performance.venue.name);

            return convertView;
        }

        @Override
        public long getHeaderId(int position) {
            Performance performance = getItem(position);
            int venue_id = performance.venue.name.hashCode();
            return venue_id;
        }
    }

}
