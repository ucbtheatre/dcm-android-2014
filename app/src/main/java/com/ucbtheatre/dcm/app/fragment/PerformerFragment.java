package com.ucbtheatre.dcm.app.fragment;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.stmt.SelectArg;
import com.ucbtheatre.dcm.app.R;
import com.ucbtheatre.dcm.app.activity.ShowActivity;
import com.ucbtheatre.dcm.app.data.DatabaseHelper;
import com.ucbtheatre.dcm.app.data.Performer;
import com.ucbtheatre.dcm.app.data.Show;
import com.ucbtheatre.dcm.app.widget.RemoteImageView;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class PerformerFragment extends Fragment {
    public static final String EXTRA_PERFORMER = "com.ucbtheatre.dcm.app.extra-performer";

    Performer performer;
    List<Show> shows;

    public PerformerFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View retVal = inflater.inflate(R.layout.fragment_performer, container, false);

        performer = (Performer) getActivity().getIntent().getSerializableExtra(EXTRA_PERFORMER);
        try {
            SelectArg name = new SelectArg("%" +performer.toString()+ "%");
            shows = performer.getShows();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        TextView name = (TextView) retVal.findViewById(R.id.fragment_performer_title);
        name.setText(performer.toString());

        ListView listView = (ListView) retVal.findViewById(R.id.fragment_performer_shows_list);
        RemoteImageView photo = new RemoteImageView(getActivity());
        photo.loadURL(performer.getPhotoUrl());
        photo.setLayoutParams(new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        listView.addHeaderView(photo);



        photo.setAdjustViewBounds(true);
        listView.setAdapter(new ArrayAdapter<Show>(getActivity(),android.R.layout.simple_list_item_1,android.R.id.text1, shows));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Show show = (Show) adapterView.getAdapter().getItem(position);
                Intent showIntent = new Intent(getActivity(), ShowActivity.class);
                showIntent.putExtra(ShowFragment.EXTRA_SHOW, show);
                startActivity(showIntent);
            }
        });

        return retVal;
    }


}
