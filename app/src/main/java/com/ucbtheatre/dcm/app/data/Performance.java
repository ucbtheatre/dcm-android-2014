package com.ucbtheatre.dcm.app.data;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.j256.ormlite.field.DatabaseField;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by kurtguenther.
 */
public class Performance implements Serializable {

    public static final String FAVORITE_UPDATE = "com.ucbtheatre.dcm.app.favorite-update";

    @DatabaseField(id=true)
    public int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    public Show show;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    public Venue venue;

    @DatabaseField
    public int start_date;

    @DatabaseField
    public int end_date;

    @DatabaseField
    public int minutes;

    @DatabaseField
    private boolean isFavorite;

    public Performance(){ }

    public Performance(JSONObject json) throws JSONException{
        id = json.getInt("id");
        int show_id = json.getInt("show_id");
        try {
            show = DatabaseHelper.getSharedService().getShowDAO().queryForId(show_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int venue_id = json.getInt("venue_id");
        try {
            venue = DatabaseHelper.getSharedService().getVenueDAO().queryForId(venue_id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        start_date = json.getInt("starttime");
        end_date = json.getInt("endtime");
        minutes = json.getInt("minutes");
    }

    @Override
    public String toString() {
        return show.toString();
    }

    public String getStartDateTime() {
        Date date = new Date(((long)start_date) * 1000);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE hh:mm a");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String display = simpleDateFormat.format(date);
        return display;
    }

    public String getStartTime() {
        Date date = new Date(((long)start_date) * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String display = simpleDateFormat.format(date);
        return display;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
        try {
            DatabaseHelper.getSharedService().getPerformanceDAO().update(this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        DatabaseHelper.getSharedService().notifyFavoriteUpdate(this);
    }

    public boolean getIsFavorite(){
        return this.isFavorite;
    }

    public String getFullTime() {
        Date start = new Date(((long)start_date) * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE hh:mm");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String display = simpleDateFormat.format(start);

        display = display + " - ";

        Date end = new Date(((long)end_date) * 1000);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("hh:mm a");
        simpleDateFormat2.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        display = display + simpleDateFormat2.format(end);
        return display;
    }

    public String getStartDay() {
        Date date = new Date(((long)start_date) * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String display = simpleDateFormat.format(date);
        return display;
    }
}
