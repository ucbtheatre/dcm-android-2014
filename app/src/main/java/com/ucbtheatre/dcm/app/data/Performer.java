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

    public Performer(String performerId, String firstName, String lastName) {
        this.id = Integer.parseInt(performerId);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
