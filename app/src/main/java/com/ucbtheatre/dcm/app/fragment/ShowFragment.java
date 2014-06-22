package com.ucbtheatre.dcm.app.fragment;



import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.field.DatabaseField;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ucbtheatre.dcm.app.R;
import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Performance;
import com.ucbtheatre.dcm.app.data.Show;
import com.ucbtheatre.dcm.app.data.Venue;
import com.ucbtheatre.dcm.app.widget.RemoteImageView;

import org.w3c.dom.Text;

import java.io.File;
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
        return String.format("http://delclosemarathon.com/performance/detail/%d check out \"%s\" at #DCM16", show.id, show.name);
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

        if(show.city != null && !show.city.isEmpty()) {
            TextView hometown = (TextView) view.findViewById(R.id.fragment_show_hometown);
            hometown.setVisibility(View.VISIBLE);
            hometown.setText(show.city);
        }

        TextView blurb = (TextView) view.findViewById(R.id.fragment_show_blurb);
        blurb.setText(show.promo);

        final ImageButton favoriteButton = (ImageButton) view.findViewById(R.id.fragment_show_favorite_button);
        if(performances.iterator().next().getIsFavorite()){
            favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite_selected));
        } else {
            favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite_unselected));
        }

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean newFavoriteValue = !performances.iterator().next().getIsFavorite();
                show.setIsFavorite(newFavoriteValue);

                if(newFavoriteValue) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_favorite_added), Toast.LENGTH_SHORT).show();
                    favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite_selected));
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_favorite_removed), Toast.LENGTH_SHORT).show();
                    favoriteButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_favorite_unselected));
                }

            }
        });

        ImageButton shareButton = (ImageButton) view.findViewById(R.id.fragment_show_share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getShareString(show));
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Del Close Marathon 16"); //For a nice email
                shareIntent.setType("text/plain");

                startActivity(Intent.createChooser(shareIntent, "Share"));

                //TODO: image sharing isn't really working, so we're just disabling it.
                //But I thought it might be useful to still have here.
//                if(show.image != null && !show.image.isEmpty()){
//
//                    File cachedFile = ImageLoader.getInstance().getDiscCache().get(show.image);
//
//                    //File imagePath = cachedFile; //new File(getActivity().getFilesDir(), "images");
//                    //File newFile = new File(imagePath, "share_image.png");
//                    Uri contentUri = FileProvider.getUriForFile(getActivity(), "com.ucbtheatre.dcm.android", cachedFile);
//
//                    Intent intent = new Intent();
//                    intent.setAction(Intent.ACTION_SEND);
//                    intent.setType("image/*");
//
//                    List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//                    for (ResolveInfo resolveInfo : resInfoList) {
//                        String packageName = resolveInfo.activityInfo.packageName;
//                        getActivity().grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    }
//
//                    intent.putExtra(Intent.EXTRA_TEXT, getShareString(show));
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                    intent.putExtra(Intent.EXTRA_STREAM, contentUri);
//                    startActivity(Intent.createChooser(intent, "Share"));
//
//                } else {
                //}
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
            castView.setTextSize(18);
            castSection.addView(castView);
        }

        //50.63.202.26
    }
}
