package com.ucbtheatre.dcm.app.data;

import com.j256.ormlite.field.DatabaseField;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kurtguenther.
 */
public class Performance implements Serializable {

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
        String display = simpleDateFormat.format(date);
        return display;
    }

    public String getStartTime() {
        Date date = new Date(((long)start_date) * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        String display = simpleDateFormat.format(date);
        return display;
    }




    public String getFullTime() {
        Date start = new Date(((long)start_date) * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE hh:mm");
        String display = simpleDateFormat.format(start);

        display = display + " - ";

        Date end = new Date(((long)end_date) * 1000);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("hh:mm a");
        display = display + simpleDateFormat2.format(end);
        return display;
    }

    public String getStartDay() {
        Date date = new Date(((long)start_date) * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
        String display = simpleDateFormat.format(date);
        return display;
    }
}
