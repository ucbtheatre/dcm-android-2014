package com.ucbtheatre.dcm.app.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.ucbtheatre.dcm.app.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by kurtguenther.
 */
public class DataService {

    private final static String TAG = "DataService";
    private final static String DATA_JSON_URL = "http://api.ucbcomedy.com/dcm?mode=development";
    private static final String SHARED_PREFERENCES_NAME = "DCM_SHARED_PREFERENCES";
    private static final String LATEST_ETAG_KEY = "LATEST_ETAG_KEY";

    //Static accessor
    private static DataService mSharedService;
    public static DataService getSharedService()
    {
        return mSharedService;
    }

    public static void initialize(Context context)
    {
        DataService api = new DataService();
        api.context = context;
        api.client = new AsyncHttpClient();
        mSharedService = api;
    }

    protected Context context;
    protected AsyncHttpClient client;


    public boolean shouldUpdate(){
        return true;
    }

    public boolean isSilent() {
        try {
            return DatabaseHelper.getSharedService().getPerformanceDAO().queryForAll().size() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    protected List<Performance> getExistingFavorites(){
        List<Performance> retVal = new ArrayList<Performance>();
        try {
            retVal = DatabaseHelper.getSharedService().getPerformanceDAO().queryBuilder().orderBy("start_date", true).where().eq("isFavorite",true).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public RequestHandle refreshDataFromServer(boolean force, final JsonHttpResponseHandler parentHandler){
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Updating Schedule");
        progressDialog.setMessage("Opening");

        //If we already have a database, we make it silent
        if(!isSilent() || force){
            progressDialog.show();
        }

        final List<Performance> existingFavorites = getExistingFavorites();

        Log.d(TAG, "Making schedule fetch request");
        Header[] headers = new Header[0];
        final SharedPreferences sp = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
        String etag = sp.getString(LATEST_ETAG_KEY, null);

        client.removeHeader("If-None-Match");
        if(!force && etag != null){
            Log.d(TAG, "Making request with ETag:" + etag);
            client.addHeader("If-None-Match", etag);
        }

        return client.get(context, DATA_JSON_URL, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, final JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d(TAG, "Download success");
                if (statusCode == 304) {
                    //do nothing
                } else {
                    DatabaseHelper.getSharedService().clearDatabase();

                    //Check for and set eTag
                    for (int i = 0; i < headers.length; i++) {
                        Header h = headers[i];
                        if (h.getName().equalsIgnoreCase("ETag")) {
                            Log.d(TAG, "Saving ETag:" + h.getValue());
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString(LATEST_ETAG_KEY, h.getValue());
                            editor.commit();
                        }
                    }

                    AsyncTask<JSONObject, String, String> updateSql = new AsyncTask<JSONObject, String, String>() {
                        @Override
                        protected String doInBackground(JSONObject... obj) {
                            try {


                                JSONObject data = obj[0].getJSONObject("data");
                                processVoteResponses(data.getJSONArray("Jokes"));
                                publishProgress("First beats");
                                processVenues(data.getJSONArray("Venues"));
                                publishProgress("Second beats");
                                processShows(data.getJSONArray("Shows"));
                                publishProgress("Third beats");
                                processSchedules(data.getJSONArray("Schedules"));

                                for (Performance oldPerformance : existingFavorites) {
                                    Performance newPerformance = DatabaseHelper.getSharedService().getPerformanceDAO().queryForId(oldPerformance.id);
                                    if (newPerformance != null) {
                                        newPerformance.setIsFavorite(true);
                                        DatabaseHelper.getSharedService().getPerformanceDAO().update(newPerformance);
                                    }
                                }

                            } catch (JSONException e) {
                                Log.e(TAG, "Data service returned bad json", e);
                                Toast.makeText(context, R.string.error_msg_schedule_download, Toast.LENGTH_LONG).show();
                            } catch (SQLException e) {
                                Log.e(TAG, "Problem saving the sql", e);
                                Toast.makeText(context, R.string.error_msg_schedule_download, Toast.LENGTH_LONG).show();
                            }

                            return null;
                        }


                        @Override
                        protected void onProgressUpdate(String... values) {
                            super.onProgressUpdate(values);
                            String update = values[0];
                            progressDialog.setMessage(update);
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            super.onPostExecute(s);
                            Log.d(TAG, "Schedule downloaded successfully");
                            Toast.makeText(context, R.string.success_schedule_updated, Toast.LENGTH_LONG).show();
                            progressDialog.hide();

                            if (parentHandler != null) {
                                parentHandler.onSuccess(response);
                            }
                        }
                    };

                    updateSql.execute(response);
                }
            }

            @Override
            public void onSuccess(final int statusCode, final Header[] headers, final String responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
            }

            @Override
            public void onFailure(int statusCode,
                                  org.apache.http.Header[] headers,
                                  java.lang.String responseBody,
                                  java.lang.Throwable e) {
                super.onFailure(statusCode, headers, responseBody, e);

                if (statusCode == 304) {
                    Log.d(TAG, "Schedule already up to date");
                    Toast.makeText(context, R.string.success_msg_schedule_up_to_date, Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "Schedule failed to download", e);
                    Toast.makeText(context, context.getText(R.string.error_msg_schedule_download) + ":" + e.toString(), Toast.LENGTH_LONG).show();
                }

                progressDialog.hide();
                parentHandler.onFailure(statusCode, headers, responseBody, e);
            }
        });

    }

    private void processVoteResponses(JSONArray jokes) throws JSONException, SQLException {
        for(int i = 0; i < jokes.length(); i++){
            String message = jokes.getString(i);
            VoteResponse vr = new VoteResponse(message);
            Dao.CreateOrUpdateStatus status = DatabaseHelper.getSharedService().getVoteResponseDAO().createOrUpdate(vr);
        }
    }

    protected void processSchedules(final JSONArray schedules) throws JSONException, SQLException {

        //Batching!
        try {
            DatabaseHelper.getSharedService().getPerformanceDAO().callBatchTasks(new Callable<Void>() {
                public Void call() throws Exception {
                    for (int i = 0; i < schedules.length(); i++) {
                        JSONObject scheduleJSON = schedules.getJSONObject(i);
                        Performance performance = new Performance(scheduleJSON);
                        Dao.CreateOrUpdateStatus status = DatabaseHelper.getSharedService().getPerformanceDAO().createOrUpdate(performance);
                        //Log.d(TAG, "Created? " + status.isCreated());
                    }
                    return null;
                }
            });
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    protected void processShows(final JSONArray shows) throws JSONException, SQLException {

        try {
            DatabaseHelper.getSharedService().getShowDAO().callBatchTasks(new Callable<Void>() {
                public Void call() throws Exception {
                    for (int i = 0; i < shows.length(); i++) {
                        JSONObject showJSON = shows.getJSONObject(i);
                        //Log.d(TAG, "Processing show " + showJSON.getString("show_name"));
                        Show show = new Show(showJSON);
                        Dao.CreateOrUpdateStatus status = DatabaseHelper.getSharedService().getShowDAO().createOrUpdate(show);
                        //Log.d(TAG, "Created? " + status.isCreated());
                    }

                    return null;
                }
            });
        } catch(Exception e){
            e.printStackTrace();
        }

    }

    protected void processVenues(JSONArray venues) throws JSONException, SQLException {

        for(int i = 0; i < venues.length(); i++){
            JSONObject venueJSON = venues.getJSONObject(i);
            Log.d(TAG, "Processing venue " + venueJSON.getString("name"));
            Venue venue = new Venue(venueJSON);
            Dao.CreateOrUpdateStatus status = DatabaseHelper.getSharedService().getVenueDAO().createOrUpdate(venue);
            //Log.d(TAG, "Created? " + status.isCreated());
        }
    }

}
