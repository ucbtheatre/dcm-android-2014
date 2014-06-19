package com.ucbtheatre.dcm.app.data;

import android.content.Context;
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
        return true;
    }

    public void refreshDataFromServer(){

        Log.d(TAG, "Starting to download schedule");

        client.get(DATA_JSON_URL, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONObject response) {
                super.onSuccess(response);

                try {
                    JSONObject data = response.getJSONObject("data");

                    processVenues(data.getJSONArray("Venues"));
                    processShows(data.getJSONArray("Shows"));
                    processSchedules(data.getJSONArray("Schedules"));

                } catch (JSONException e) {
                    Log.e(TAG, "Data service returned bad json", e);
                    Toast.makeText(context, R.string.error_msg_schedule_download, Toast.LENGTH_LONG).show();
                } catch (SQLException e) {
                    Log.e(TAG, "Problem saving the sql", e);
                    Toast.makeText(context, R.string.error_msg_schedule_download, Toast.LENGTH_LONG).show();
                }

                Log.d(TAG, "Schedule downloaded successfully");
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
            }
        });

    }

    protected void processSchedules(JSONArray schedules) {

    }

    protected void processShows(JSONArray shows) {

    }

    protected void processVenues(JSONArray venues) throws JSONException, SQLException {
        for(int i = 0; i < venues.length(); i++){
            JSONObject venueJSON = venues.getJSONObject(i);
            Log.d(TAG, "Processing venue " + venueJSON.getString("name"));
            Venue venue = new Venue(venueJSON);
            Dao.CreateOrUpdateStatus status = DatabaseHelper.getSharedService().getVenueDAO().createOrUpdate(venue);
            Log.d(TAG, "Created? " + status.isCreated());
        }
    }

}
