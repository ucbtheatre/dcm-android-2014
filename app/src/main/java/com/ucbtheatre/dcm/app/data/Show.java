package com.ucbtheatre.dcm.app.data;

import com.j256.ormlite.field.DatabaseField;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by kurtguenther.
 */
public class Show implements Serializable {

    private final static String SEPARATOR = ";";

    @DatabaseField(id=true)
    public int id;

    @DatabaseField
    public String name;

    @DatabaseField
    public String sortName;

    @DatabaseField
    public String promo;

    @DatabaseField
    public String city;

    @DatabaseField
    public String performers;

    @DatabaseField
    public String image;

    public Show(){}

    public Show(JSONObject json) throws JSONException {
        id = json.getInt("id");
        name = json.getString("show_name");
        promo = json.optString("promo_blurb");
        city = json.optString("home_city");
        image = json.optString("image");

        sortName = getSortName();

        JSONArray cast = json.optJSONArray("cast");

        StringBuilder castBuilder = new StringBuilder();
        if(cast != null){
            for(int i = 0; i < cast.length(); i++){
                JSONObject person = cast.getJSONObject(i);
                String firstName = person.optString("first", "");
                String lastName = person.optString("last", "");

                castBuilder.append(firstName);
                castBuilder.append(" ");
                castBuilder.append(lastName);
                castBuilder.append(SEPARATOR);


                //TODO: this should be abstracted elsewhere so we can benefit from batching
                Performer p = new Performer(person);
                try {
                    DatabaseHelper.getSharedService().getPerformerDAO().createOrUpdate(p);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


        performers = castBuilder.toString();
    }

    public void setIsFavorite(boolean isFavorite){
        try {
            List<Performance> performances = DatabaseHelper.getSharedService().getPerformanceDAO().queryForEq("show_id", this);
            for(Performance p : performances){
                p.setIsFavorite(isFavorite);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean getIsFavorite(){
        try {
            List<Performance> performances = DatabaseHelper.getSharedService().getPerformanceDAO().queryForEq("show_id", this);
            return performances.get(0).getIsFavorite();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    protected String getSortName() {

        //Figure out the sorting.
        String retVal = name.toLowerCase();

        if(retVal.startsWith("'")){
            retVal = retVal.substring(Math.min(1, retVal.length()-1));
        }

        if(retVal.startsWith("\"")){
            retVal = retVal.substring(Math.min(1, retVal.length()-1));
        }

        if(retVal.startsWith("the")){
            retVal = retVal.substring(4);
        }

        return retVal;
    }

    public String[] getPerformersArray(){
        if(performers != null){
            return performers.split(SEPARATOR);
        }
        return new String[]{};
    }

    @Override
    public String toString() {
        return name;
    }
}
