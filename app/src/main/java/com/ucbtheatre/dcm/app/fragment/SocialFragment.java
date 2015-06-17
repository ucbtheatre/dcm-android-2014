package com.ucbtheatre.dcm.app.fragment;


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
public class SocialFragment extends android.support.v4.app.Fragment {

    class SocialLink {
        int color;
        String title;
        String handle;
        String nativeURL;
        String webURL;

        int textColor = Color.WHITE;

        SocialLink(int color, String title, String handle, String nativeURL, String webURL){
            this.color = color;
            this.title = title;
            this.handle = handle;
            this.nativeURL = nativeURL;
            this.webURL = webURL;
        }
    }

    private List<SocialLink> socialLinkList;

    public SocialFragment() {
        socialLinkList = new ArrayList<SocialLink>();

        SocialLink dcmLines = new SocialLink(
                Color.argb(255, 85, 172, 238),
                "Twitter",
                "@DCM_Lines",
                "twitter://user?user_id=2560233571",
                "http://twitter.com/DCM_Lines");
        socialLinkList.add(dcmLines);

        SocialLink twitter = new SocialLink(
                Color.argb(255, 85, 172, 238),
                "Twitter",
                "@UCBTheatreNY",
                "twitter://user?user_id=40349753",
                "http://twitter.com/UCBTheatreNY");
        socialLinkList.add(twitter);

        SocialLink instagram = new SocialLink(
                Color.argb(255, 157, 105, 86),
                "Instagram",
                "@UCBTheatreNY",
                "http://instagram.com/_u/UCBTheatreNY",
                "http://instagram.com/UCBTheatreNY");
        socialLinkList.add(instagram);

        SocialLink facebook = new SocialLink(
                Color.argb(255, 58, 87, 149),
                "Facebook",
                "/UCBTheatreNY",
        //TODO
                "instafooHACK://",
                "http://facebook.com/UCBTheatreNY");
        socialLinkList.add(facebook);

        SocialLink tumblr = new SocialLink(
                Color.argb(255, 54, 70, 93),
                "Tumblr",
                "@ucbcomedy",
                //HACL it appears tumblr doesn't support this on android
                "hackhackhack://",
                "http://ucbcomedy.tumblr.com");
        socialLinkList.add(tumblr);

        SocialLink snapchat = new SocialLink(
                Color.argb(255, 255, 252, 0),
                "Snapchat",
                "@ucbcomedy",
                //TODO
                "com.snapchat.android",
                "http://www.snapchat.com/");
        snapchat.textColor = Color.BLACK;
        socialLinkList.add(snapchat);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View retVal = inflater.inflate(R.layout.fragment_social, container, false);

        LinearLayout linksContainer = (LinearLayout) retVal.findViewById(R.id.fragment_social_links_container);

        for(final SocialLink link : socialLinkList){
            LinearLayout socialView = (LinearLayout) inflater.inflate(R.layout.list_social_view, linksContainer, false);
            socialView.setBackgroundColor(link.color);

            TextView title = (TextView) socialView.findViewById(R.id.list_social_view_title);
            title.setText(link.title);
            title.setTextColor(link.textColor);

            TextView handle = (TextView) socialView.findViewById(R.id.list_social_view_handle);
            handle.setText(link.handle);
            handle.setTextColor(link.textColor);

            linksContainer.addView(socialView);

            socialView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if(link.nativeURL.startsWith("com.")){
                            Intent nativeIntent = getActivity().getPackageManager().getLaunchIntentForPackage(link.nativeURL);
                            startActivity(nativeIntent);
                        } else {
                            Intent nativeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.nativeURL));
                            startActivity(nativeIntent);
                        }
                    } catch (Exception e){
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.webURL));
                        startActivity(webIntent);
                    }
                }
            });
        }

        return retVal;
    }


}
