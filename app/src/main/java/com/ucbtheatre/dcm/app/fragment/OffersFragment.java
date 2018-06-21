package com.ucbtheatre.dcm.app.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ucbtheatre.dcm.app.R;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ucbtheatre.dcm.app.R;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class OffersFragment extends android.support.v4.app.Fragment {


    public OffersFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_left);

        View retVal = inflater.inflate(R.layout.fragment_offers, container, false);

        WebView wv = (WebView) retVal.findViewById(R.id.offersfragmentwv);
        wv.loadUrl("http://delclosemarathon.com/offersjwdgl0r797");

        return retVal;

    }


}