package com.ucbtheatre.dcm.app.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Iterator;
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

        JSONObject cast = json.optJSONObject("cast");

        if(cast != null){
            Iterator keys = cast.keys();
            while(keys.hasNext()){
                String performerId = (String) keys.next();
                JSONObject performer = (JSONObject) cast.get(performerId);
                String firstName = performer.optString("first");
                String lastName = performer.optString("last");

                Performer p = new Performer(performerId, firstName, lastName);
                try {
                    DatabaseHelper.getSharedService().getPerformerDAO().createOrUpdate(p);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                ShowPerformer sp = new ShowPerformer(this, p);
                try {
                    DatabaseHelper.getSharedService().getShowPerformerDAO().create(sp);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

//            for(int i = 0; i < cast.length(); i++){
//                JSONObject person = cast.getJSONObject(i);
//                String firstName = person.optString("first", "");
//                String lastName = person.optString("last", "");
//
//                DatabaseHelper.getSharedService().getPerformerDAO().createOrUpdate(p);
//
//                //TODO: this should be abstracted elsewhere so we can benefit from batching
//                Performer p = new Performer(person);
//                if(p.firstName.length() > 0) { // Some bad data
//
//                }
//            }
        }
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

    public List<Performer> getPerformers() throws SQLException {

        QueryBuilder<ShowPerformer, Integer> userPostQb = DatabaseHelper.getSharedService().getShowPerformerDAO().queryBuilder();
        userPostQb.selectColumns(ShowPerformer.PERFORMER_ID_FIELD_NAME);
        userPostQb.where().eq(ShowPerformer.SHOW_ID_FIELD_NAME, this);
        QueryBuilder<Performer, Integer> postQb = DatabaseHelper.getSharedService().getPerformerDAO().queryBuilder();
        postQb.where().in("id", userPostQb);
        PreparedQuery<Performer> query = postQb.prepare();

        return DatabaseHelper.getSharedService().getPerformerDAO().query(query);
    }

//    /**
//     * Build our query for Post objects that match a User.
//     */
//    private PreparedQuery<Post> makePostsForUserQuery() throws SQLException {
//        // build our inner query for UserPost objects
//        QueryBuilder<UserPost, Integer> userPostQb = userPostDao.queryBuilder();
//        // just select the post-id field
//        userPostQb.selectColumns(UserPost.POST_ID_FIELD_NAME);
//        SelectArg userSelectArg = new SelectArg();
//        // you could also just pass in user1 here
//        userPostQb.where().eq(UserPost.USER_ID_FIELD_NAME, userSelectArg);
//
//        // build our outer query for Post objects
//        QueryBuilder<Post, Integer> postQb = postDao.queryBuilder();
//        // where the id matches in the post-id from the inner query
//        postQb.where().in(Post.ID_FIELD_NAME, userPostQb);
//        return postQb.prepare();
//    }
//
//    private List<Post> lookupPostsForUser(User user) throws SQLException {
//        if (postsForUserQuery == null) {
//            postsForUserQuery = makePostsForUserQuery();
//        }
//        postsForUserQuery.setArgumentHolderValue(0, user);
//        return postDao.query(postsForUserQuery);
//    }
//
//
//
//    /**
//     * Build our query for Post objects that match a User.
//     */
//    private PreparedQuery<Post> makePostsForUserQuery() throws SQLException {
//
//    }

    @Override
    public String toString() {
        return name;
    }
}
