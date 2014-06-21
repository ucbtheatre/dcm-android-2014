package com.ucbtheatre.dcm.app.data;

import com.j256.ormlite.field.DatabaseField;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by kurtguenther.
 */
public class Venue implements Serializable {

    @DatabaseField(id=true)
    public int id;

    @DatabaseField
    public String name;

    @DatabaseField
    public String short_name;

    @DatabaseField
    public String address;

    @DatabaseField
    public String directions;

    @DatabaseField
    public String image;

    @DatabaseField
    public String gmaps;

    @DatabaseField
    public String url;

    public Venue(){}

    public Venue(JSONObject json) throws JSONException{
        id = json.getInt("id");
        name = json.optString("name");
        short_name = json.optString("short_name");
        address = json.optString("address");
        image = json.optString("image");
        url = json.optString("url");
        gmaps = json.optString("map_url");
    }

    @Override
    public String toString() {
        return name;
    }
}
