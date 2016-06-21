package com.ucbtheatre.dcm.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ucbtheatre.dcm.app.R;
import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Performance;
import com.ucbtheatre.dcm.app.data.Performer;
import com.ucbtheatre.dcm.app.data.Show;
import com.ucbtheatre.dcm.app.data.Venue;
import com.ucbtheatre.dcm.app.fragment.ShowFragment;
import com.ucbtheatre.dcm.app.widget.RemoteImageView;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShowActivity extends Activity {

    Show show;
    List<Performance> performances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        show = (Show) getIntent().getSerializableExtra(ShowFragment.EXTRA_SHOW);
        setContentView(R.layout.activity_show);

        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_left);

        performances = new ArrayList<Performance>();
        try {
            performances = DatabaseHelper.getSharedService().getPerformanceDAO().queryForEq("show_id", show.id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        TextView title =  (TextView) findViewById(R.id.fragment_show_title);
        title.setText(show.name);

        if(show.city != null && !show.city.isEmpty()) {
            TextView hometown = (TextView) findViewById(R.id.fragment_show_hometown);
            hometown.setVisibility(View.VISIBLE);
            hometown.setText(show.city);
        }

        TextView blurb = (TextView) findViewById(R.id.fragment_show_blurb);
        blurb.setText(show.promo);

//        final ImageButton favoriteButton = (ImageButton) findViewById(R.id.fragment_show_favorite_button);
//        if(performances.iterator().next().getIsFavorite()){
//            favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite_selected));
//        } else {
//            favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite_unselected));
//        }
//
//        favoriteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                boolean newFavoriteValue = !performances.iterator().next().getIsFavorite();
//                show.setIsFavorite(newFavoriteValue);
//
//                if(newFavoriteValue) {
//                    Toast.makeText(ShowActivity.this, getResources().getString(R.string.toast_favorite_added), Toast.LENGTH_SHORT).show();
//                    favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite_selected));
//                } else {
//                    Toast.makeText(ShowActivity.this, getResources().getString(R.string.toast_favorite_removed), Toast.LENGTH_SHORT).show();
//                    favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite_unselected));
//                }
//
//            }
//        });
//
//        ImageButton shareButton = (ImageButton) findViewById(R.id.fragment_show_share_button);
//        shareButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                shareIntent.putExtra(Intent.EXTRA_TEXT, getShareString(show));
//                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Del Close Marathon 16"); //For a nice email
//                shareIntent.setType("text/plain");
//
//                startActivity(Intent.createChooser(shareIntent, "Share"));
//
//                //TODO: image sharing isn't really working, so we're just disabling it.
//                //But I thought it might be useful to still have here.
////                if(show.image != null && !show.image.isEmpty()){
////
////                    File cachedFile = ImageLoader.getInstance().getDiscCache().get(show.image);
////
////                    //File imagePath = cachedFile; //new File(getActivity().getFilesDir(), "images");
////                    //File newFile = new File(imagePath, "share_image.png");
////                    Uri contentUri = FileProvider.getUriForFile(getActivity(), "com.ucbtheatre.dcm.android", cachedFile);
////
////                    Intent intent = new Intent();
////                    intent.setAction(Intent.ACTION_SEND);
////                    intent.setType("image/*");
////
////                    List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
////                    for (ResolveInfo resolveInfo : resInfoList) {
////                        String packageName = resolveInfo.activityInfo.packageName;
////                        getActivity().grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
////                    }
////
////                    intent.putExtra(Intent.EXTRA_TEXT, getShareString(show));
////                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
////                    intent.putExtra(Intent.EXTRA_STREAM, contentUri);
////                    startActivity(Intent.createChooser(intent, "Share"));
////
////                } else {
//                //}
//            }
//        });

        createImage(this.findViewById(android.R.id.content));
        createShowViews(this.findViewById(android.R.id.content));
        createCastViews(this.findViewById(android.R.id.content));
        createTicketViews(this.findViewById(android.R.id.content));

    }

    protected void createShowViews(View view) {
        LinearLayout showSection = (LinearLayout) view.findViewById(R.id.fragment_show_times);
        LayoutInflater inflater = getLayoutInflater();
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
        List<Performer> cast = null;

        try {
            cast = show.getPerformers();

            if(cast.size() > 0){
                LayoutInflater inflater = getLayoutInflater();
                View headerView = inflater.inflate(R.layout.header_layout,castSection,false);
                TextView headerText = (TextView) headerView.findViewById(R.id.header_text);
                headerText.setText("Cast");
                castSection.addView(headerView);
            }

            for(int i = 0; i < cast.size(); i++){
                TextView castView = new TextView(this);
                castView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                castView.setText(cast.get(i).toString());
                castView.setTextSize(18);
                castSection.addView(castView);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void createTicketViews(View view) {

        boolean isPaidShow = false;
        for(Performance p : performances){
            if(p.ticketsUrl != null && !p.ticketsUrl.isEmpty()){
                isPaidShow = true;
            }
        }

        //No paid shows, no views
        if(!isPaidShow){
            return;
        }

        LinearLayout ticketsSection = (LinearLayout) view.findViewById(R.id.fragment_show_tickets);

        LayoutInflater inflater = getLayoutInflater();
        View headerView = inflater.inflate(R.layout.header_layout,ticketsSection,false);
        TextView headerText = (TextView) headerView.findViewById(R.id.header_text);
        headerText.setText("Tickets");
        ticketsSection.addView(headerView);

        for(int i = 0; i < performances.size(); i++){
            final Performance p = performances.get(i);
            Button buyTicketsButton = new Button(this);
            buyTicketsButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if(performances.size() > 1) {
                buyTicketsButton.setText("Buy a ticket - " + p.getStartTime());
            } else {
                buyTicketsButton.setText("Buy a ticket");
            }
            buyTicketsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent buyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(p.ticketsUrl));
                    startActivity(buyIntent);
                }
            });
            buyTicketsButton.setTextSize(18);
            ticketsSection.addView(buyTicketsButton);
        }
    }

    public String getShareString(Show show) {
        Performance perf = performances.get(0);
        return String.format("Check out \"%s\" at %s, %s %s.  #DCM18 http://www.delclosemarathon.com/calendar", show.name, perf.venue.name, perf.getStartDay(), perf.getStartTime());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show, menu);

        MenuItem favoriteItem = menu.findItem(R.id.action_favorite);
        if(show.getIsFavorite()){
            favoriteItem.setIcon(R.drawable.ic_action_favorite_selected);
        } else {
            favoriteItem.setIcon(R.drawable.ic_action_favorite_unselected);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_favorite){
            boolean newFavoriteValue = !show.getIsFavorite();
            show.setIsFavorite(newFavoriteValue);

            if(newFavoriteValue) {
                Toast.makeText(this, getResources().getString(R.string.toast_favorite_added), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getResources().getString(R.string.toast_favorite_removed), Toast.LENGTH_SHORT).show();
            }

            invalidateOptionsMenu();
        } else {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, getShareString(show));
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Del Close Marathon 18"); //For a nice email
            shareIntent.setType("text/plain");

            startActivity(Intent.createChooser(shareIntent, "Share"));
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }
}
