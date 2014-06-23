package com.ucbtheatre.dcm.app.activity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ucbtheatre.dcm.app.R;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends Activity {

    GridView gridView;

    public class FaceInfo {
        int face;
        String name;

        public FaceInfo(int f, String n){
            face = f;
            name = n;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_left);

        List<FaceInfo> faces = new ArrayList<FaceInfo>();
        faces.add(new FaceInfo(R.drawable.kurt, "Kurt Guenther"));
        faces.add(new FaceInfo(R.drawable.ben, "Ben Ragheb"));

        gridView = (GridView) findViewById(R.id.activity_about_faces);

        gridView.setAdapter( new FacesAdapter(this, 0, faces));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public class FacesAdapter extends ArrayAdapter<FaceInfo> {

        public FacesAdapter(Context context, int resource, List<FaceInfo> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Super inefficient, but only like 5 images always on screen
            FaceInfo faceInfo = getItem(position);
            View retVal = getLayoutInflater().inflate(R.layout.view_face, parent, false);
            ImageView face = (ImageView) retVal.findViewById(R.id.view_face_image);
            face.setImageResource(faceInfo.face);
            TextView name = (TextView) retVal.findViewById(R.id.view_face_name);
            name.setText(faceInfo.name);
            return retVal;
        }
    }
}
