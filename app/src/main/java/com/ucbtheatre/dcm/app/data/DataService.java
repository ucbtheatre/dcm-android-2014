package com.ucbtheatre.dcm.app.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.ucbtheatre.dcm.app.R;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.concurrent.Callable;

/**
 * Created by kurtguenther.
 */
public class DataService {

    private final static String TAG = "DataService";
    private final static String DATA_JSON_URL = "http://api.production.ucbt.net/dcm";

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



    //TODO need logic for when to update
    public boolean shouldUpdate(){
        //TODO remove
        try {
            return DatabaseHelper.getSharedService().getVenueDAO().countOf() == 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }



    public void refreshDataFromServer(final JsonHttpResponseHandler parentHandler){
        DatabaseHelper.getSharedService().clearDatabase();

        Log.d(TAG, "Starting to download schedule");

        final ProgressDialog progressDialog = ProgressDialog.show(context, "Updating Schedule", "downloading schedule");

        client.get(DATA_JSON_URL, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(final JSONObject response) {
                super.onSuccess(response);

                //TODO: this needs to be done on the background thread
                    AsyncTask<JSONObject, String, String> updateSql = new AsyncTask<JSONObject, String, String>() {
                        @Override
                        protected String doInBackground(JSONObject... obj) {
                            try {
                                JSONObject data = obj[0].getJSONObject("data");
                                publishProgress("Updating Venues");
                                processVenues(data.getJSONArray("Venues"));
                                publishProgress("Updating shows");
                                processShows(data.getJSONArray("Shows"));
                                publishProgress("Updating Schedules");
                                processSchedules(data.getJSONArray("Schedules"));
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
                            progressDialog.hide();

                            if(parentHandler != null){
                                parentHandler.onSuccess(response);
                            }
                        }
                    };

                updateSql.execute(response);
            }

            @Override
            public void onFailure(int statusCode,
                                  org.apache.http.Header[] headers,
                                  java.lang.String responseBody,
                                  java.lang.Throwable e) {
                super.onFailure(statusCode, headers, responseBody, e);
                Log.e(TAG, "Schedule failed to download", e);

                //TODO more specific?
                Toast.makeText(context, R.string.error_msg_schedule_download, Toast.LENGTH_LONG).show();
                progressDialog.hide();

                parentHandler.onFailure(statusCode, headers, responseBody, e);
            }
        });

    }

    protected void processSchedules(final JSONArray schedules) throws JSONException, SQLException {

        //Batching!
        try {
            DatabaseHelper.getSharedService().getPerformanceDAO().callBatchTasks(new Callable<Void>() {
                public Void call() throws Exception {
                    for(int i = 0; i < schedules.length(); i++){
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
