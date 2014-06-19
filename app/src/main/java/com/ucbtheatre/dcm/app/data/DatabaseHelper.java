package com.ucbtheatre.dcm.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
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
    private static final int DATABASE_VERSION = 2;

    //Static accessor
    private static DatabaseHelper mSharedService;
    public static DatabaseHelper getSharedService()
    {
        return mSharedService;
    }

    public static void initialize(Context context)
    {
        DatabaseHelper api = new DatabaseHelper(context);
        mSharedService = api;
    }

    //Data accessor objects
    private Dao<Venue, Integer> venueDAO;


    //Constructor and creation
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, Venue.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int i, int i2) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, Venue.class, true);

            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    public Dao<Venue, Integer> getVenueDAO() throws SQLException {
        if (venueDAO == null) {
            venueDAO = getDao(Venue.class);
        }
        return venueDAO;
    }

    @Override
    public void close() {
        super.close();
        venueDAO = null;
    }
}
