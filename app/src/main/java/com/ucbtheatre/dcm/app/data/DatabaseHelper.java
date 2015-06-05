package com.ucbtheatre.dcm.app.data;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.loopj.android.http.AsyncHttpClient;

import java.sql.SQLException;

/**
 * Created by kurtguenther.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "dcm.db";
    private static final int DATABASE_VERSION = 21;


    //Static accessor
    private static DatabaseHelper mSharedService;


    public static DatabaseHelper getSharedService()
    {
        return mSharedService;
    }

    Context context;

    public static void initialize(Context context)
    {
        DatabaseHelper api = new DatabaseHelper(context);
        mSharedService = api;
    }

    //Data accessor objects
    private Dao<Venue, Integer> venueDAO;
    private Dao<Show, Integer> showDAO;
    private Dao<Performance, Integer> performanceDAO;
    private Dao<Performer, Integer> performerDAO;
    private Dao<ShowPerformer, Integer> showPerformerDAO;

    //Constructor and creation
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, Venue.class);
            TableUtils.createTable(connectionSource, Show.class);
            TableUtils.createTable(connectionSource, Performance.class);
            TableUtils.createTable(connectionSource, Performer.class);
            TableUtils.createTable(connectionSource, ShowPerformer.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int i, int i2) {
        clearDatabase();
    }

    public void clearDatabase(){
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, ShowPerformer.class, true);
            TableUtils.dropTable(connectionSource, Venue.class, true);
            TableUtils.dropTable(connectionSource, Show.class, true);
            TableUtils.dropTable(connectionSource, Performance.class, true);
            TableUtils.dropTable(connectionSource, Performer.class, true);

            // after we drop the old databases, we create the new ones
            onCreate(null, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    public Dao<Venue, Integer> getVenueDAO(){
        if (venueDAO == null) {
            try {
                venueDAO = getDao(Venue.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return venueDAO;
    }
    public Dao<Show, Integer> getShowDAO() {
        if (showDAO == null) {
            try {
                showDAO = getDao(Show.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return showDAO;
    }

    public Dao<Performance, Integer> getPerformanceDAO() {
        if (performanceDAO == null) {
            try {
                performanceDAO = getDao(Performance.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return performanceDAO;
    }

    public Dao<Performer, Integer> getPerformerDAO() {
        if (performerDAO == null) {
            try {
                performerDAO = getDao(Performer.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return performerDAO;
    }

    public Dao<ShowPerformer, Integer> getShowPerformerDAO() {
        if (showPerformerDAO == null) {
            try {
                showPerformerDAO = getDao(ShowPerformer.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return showPerformerDAO;
    }

    @Override
    public void close() {
        super.close();
        venueDAO = null;
    }

    public void notifyFavoriteUpdate(Performance performance){
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Performance.FAVORITE_UPDATE));
    }
}
