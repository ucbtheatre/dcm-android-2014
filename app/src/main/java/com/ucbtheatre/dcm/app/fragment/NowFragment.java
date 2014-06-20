package com.ucbtheatre.dcm.app.fragment;



import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.ucbtheatre.dcm.app.R;

import java.util.ArrayList;
import java.util.Date;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class NowFragment extends NavigableFragment {

    //June 27, 2014, 4 PM EST, pulled from http://www.unixtimestamp.com/
    public static final Date MARATHON_START_DATE = new Date(((long)1403899200) * 1000);

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
    TextView timer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View retVal = inflater.inflate(R.layout.fragment_now, container, false);

        timer = (TextView) retVal.findViewById(R.id.fragment_now_timer);

        timerContainer=  retVal.findViewById(R.id.fragment_now_time_container);
        scheduleContainer = retVal.findViewById(R.id.fragment_now_shows_container);

        return retVal;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI(){
        Date now = new Date();

        if(now.before(MARATHON_START_DATE)) {
            timerContainer.setVisibility(View.VISIBLE);
            scheduleContainer.setVisibility(View.GONE);

            updateClock();

            clock = new Handler();
            updateClockRunnable.run();

        }
        else {
            timerContainer.setVisibility(View.GONE);
            scheduleContainer.setVisibility(View.VISIBLE);

//            //Load up some data for the upcoming.
//            ArrayList<NowPerformance> perfs = Performance.getUpcomingShows(now);
//            NowListAdapter mAdpt = new NowListAdapter(getActivity(), R.layout.list_schedule, R.id.schedule_show_name, perfs);
//
//            StickyListHeadersListView list = (StickyListHeadersListView) getView().findViewById(R.id.now_list);
//            list.setAreHeadersSticky(false);
//            list.setAdapter(mAdpt);
//            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                    Intent displayShow = new Intent(getActivity(), ViewShowFragment.class);
//                    NowPerformance np= (NowPerformance) adapterView.getAdapter().getItem(i);
//                    displayShow.putExtra(ViewShowFragment.SHOW_KEY, np.show_id);
//                    startActivity(displayShow);
//                }
//            });
        }
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

}
