package com.ucbtheatre.dcm.app.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import org.json.JSONObject;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

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

    public List<Show> getShows() throws SQLException {

        QueryBuilder<ShowPerformer, Integer> userPostQb = DatabaseHelper.getSharedService().getShowPerformerDAO().queryBuilder();
        userPostQb.selectColumns(ShowPerformer.SHOW_ID_FIELD_NAME);
        userPostQb.where().eq(ShowPerformer.PERFORMER_ID_FIELD_NAME, this);
        QueryBuilder<Show, Integer> postQb = DatabaseHelper.getSharedService().getShowDAO().queryBuilder();
        postQb.where().in("id", userPostQb);
        PreparedQuery<Show> query = postQb.prepare();

        return DatabaseHelper.getSharedService().getShowDAO().query(query);
    }

    public URL getPhotoUrl(){
        try {
            return new URL("http://2292774aeb57f699998f-7c1ee3a3cf06785b3e2b618873b759ef.r47.cf5.rackcdn.com/person_" + id + ".png");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
