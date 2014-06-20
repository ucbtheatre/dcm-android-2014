package com.ucbtheatre.dcm.app.data;

import com.j256.ormlite.field.DatabaseField;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by kurtguenther.
 */
public class Performer implements Serializable {

    @DatabaseField(id=true)
    public int id;

    @DatabaseField
    public String firstName;

    @DatabaseField
    public String lastName;

    public Performer() {}

    public Performer(JSONObject json){
        firstName = json.optString("first","");
        lastName = json.optString("last", "");
        id = this.toString().hashCode();
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
