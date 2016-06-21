package com.ucbtheatre.dcm.app.data;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * Created by kurtguenther on 6/21/16.
 */
public class VoteResponse implements Serializable {
    @DatabaseField(generatedId=true)
    public int id;

    @DatabaseField
    public String message;

    public VoteResponse() {
    }

    public VoteResponse(String message){
        this.message = message;
    }
}
