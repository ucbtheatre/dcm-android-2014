package com.ucbtheatre.dcm.app.fragment;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.field.DatabaseField;
import com.ucbtheatre.dcm.app.R;
import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Performance;
import com.ucbtheatre.dcm.app.data.Show;
import com.ucbtheatre.dcm.app.data.Venue;
import com.ucbtheatre.dcm.app.widget.RemoteImageView;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class ShowFragment extends Fragment  {
    public static final String EXTRA_SHOW = "com.ucbtheatre.dcm.app.extra-show";

    public Show show;
    public List<Performance> performances;

    public ShowFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show, container, false);
    }

    public String getShareString(Show show) {
        return String.format("Check out %s at #DCM16", show.name);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        show = (Show) getArguments().getSerializable(EXTRA_SHOW);
        performances = new ArrayList<Performance>();
        try {
            performances = DatabaseHelper.getSharedService().getPerformanceDAO().queryForEq("show_id", show.id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        TextView title =  (TextView) view.findViewById(R.id.fragment_show_title);
        title.setText(show.name);

        TextView blurb = (TextView) view.findViewById(R.id.fragment_show_blurb);
        blurb.setText(show.promo);

        Button favoriteButton = (Button) view.findViewById(R.id.fragment_show_favorite_button);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean newFavoriteValue = !performances.iterator().next().getIsFavorite();
                for(Performance p : performances){
                    p.setIsFavorite(newFavoriteValue);
                    try {
                        DatabaseHelper.getSharedService().getPerformanceDAO().update(p);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                if(newFavoriteValue) {
                    Toast.makeText(getActivity(), "Favorite Added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Favorite Removed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button shareButton = (Button) view.findViewById(R.id.fragment_show_share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getShareString(show));
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share"));
            }
        });

        createImage(view);
        createShowViews(view);
        createCastViews(view);
    }

    protected void createShowViews(View view) {
        LinearLayout showSection = (LinearLayout) view.findViewById(R.id.fragment_show_times);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        if(performances.size() > 0){

            View headerView = inflater.inflate(R.layout.header_layout,showSection,false);
            TextView headerText = (TextView) headerView.findViewById(R.id.header_text);
            headerText.setText("Showtimes");
            showSection.addView(headerView);
        }

        for(int i = 0; i < performances.size(); i++){
            View showView = inflater.inflate(R.layout.listview_show_time, showSection, false);
            TextView time = (TextView) showView.findViewById(R.id.listview_show_time_date);
            time.setText(performances.get(i).getFullTime());

            String locationName = "HAFT";
            Venue v = performances.get(i).venue;
            try {
                DatabaseHelper.getSharedService().getVenueDAO().refresh(v);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            locationName = v.short_name;

            TextView location = (TextView) showView.findViewById(R.id.listview_show_time_location);
            location.setText(locationName);
            showSection.addView(showView);
        }
    }

    protected void createImage(View view) {
        RemoteImageView imageView = (RemoteImageView) view.findViewById(R.id.fragment_show_image);

        if(show.image != null && !show.image.isEmpty()) {
            try {
                imageView.loadURL(new URL(show.image));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    protected void createCastViews(View view) {
        LinearLayout castSection = (LinearLayout) view.findViewById(R.id.fragment_show_cast);
        String[] cast = show.getPerformersArray();

        if(cast.length > 0){
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View headerView = inflater.inflate(R.layout.header_layout,castSection,false);
            TextView headerText = (TextView) headerView.findViewById(R.id.header_text);
            headerText.setText("Cast");
            castSection.addView(headerView);
        }

        for(int i = 0; i < cast.length; i++){
            TextView castView = new TextView(getActivity());
            castView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            castView.setText(cast[i]);
            castView.setTextSize(16);
            castSection.addView(castView);
        }

        //50.63.202.26
    }
}
