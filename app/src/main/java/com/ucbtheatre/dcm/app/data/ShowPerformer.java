package com.ucbtheatre.dcm.app.data;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * Created by kurtguenther on 6/5/15.
 */
public class ShowPerformer implements Serializable {

    public final static String SHOW_ID_FIELD_NAME = "show_id";
    public final static String PERFORMER_ID_FIELD_NAME = "performer_id";

    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(foreign = true, columnName = SHOW_ID_FIELD_NAME)
    Show show;

    @DatabaseField(foreign = true, columnName = PERFORMER_ID_FIELD_NAME)
    Performer performer;

    ShowPerformer(){}

    public ShowPerformer(Show show, Performer p) {
        this.show = show;
        this.performer = p;
    }
}
