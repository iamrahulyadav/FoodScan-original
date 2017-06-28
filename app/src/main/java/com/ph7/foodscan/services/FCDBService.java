package com.ph7.foodscan.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sony on 05-08-2016.
 */
public class FCDBService extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 7;

    // Database Name
    private static final String DATABASE_NAME = "FC_MY_TESTS";

    private static final String SC = "scan_complete";
    private static final String AS = "analyse_start";
    private static final String AC = "analyse_complete";
    private static final String AR = "analyse_result";

    // Database creation sql statement
    private static final String CREATE_TESTS_TBL_STMT = "Create table tests(\n" +
            "id Integer Primary Key Autoincrement NOT NULL , \n" +
            "test_id Text NOT NULL, \n" +
            "test_name Text NOT NULL,\n" +
            "test_note Text,\n" +
            "test_location Text,\n" +
            "model_ids Text,\n"+
            "collection_id Text,\n"+
            "test_scan_result Text,\n" +
            "scan_count Integer,\n" +
            "test_status Integer NOT NULL DEFAULT(0),\n" +
            "create_datetime Text NOT NULL,\n" +
            "expire Text NOT NULL ,\n" +
            "imgs_path Text ,\n" +
            "timestamp Text NOT NULL,\n" +
            SC +" Integer NOT NULL DEFAULT(0) ,\n" +
            AS+" Integer NOT NULL DEFAULT(0) ,\n" +
            AC+" Integer NOT NULL DEFAULT(0) ,\n" +
            AR+" Integer NOT NULL DEFAULT(0)\n" +
            ")";

    public FCDBService(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TESTS_TBL_STMT);
    }



    public Cursor getAllTests() {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("Select * from tests ORDER BY timestamp DESC", null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cursor;
    }

    public Cursor getAnalysedTestsById(String test_id) {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase().rawQuery("Select * from tests where test_id='"+test_id+"'", null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cursor;
    }

    public boolean saveTestScan(String test_id,String test_name,String test_note,String test_location,
                                String model_ids, String collection_id,String test_scan_result,int scan_count,
                                String create_datetime,String expire,String imgs_path,String timestamp)
    {
        ContentValues values = new ContentValues(8);
        values.put("test_id",	test_id);
        values.put("test_name", test_name);
        values.put("test_note", test_note);
        values.put("test_location", test_location);
        values.put("model_ids", model_ids);
        values.put("collection_id", collection_id);
        values.put("test_scan_result", test_scan_result);
        values.put("scan_count", scan_count);
        values.put("create_datetime", create_datetime);
        values.put("expire", expire);
        values.put("imgs_path", imgs_path);
        values.put("timestamp", timestamp);
        values.put(AS, 1);
        values.put(SC, 1);
        if(getWritableDatabase().insert("tests",null, values)== -1){
            return false ;
        }
        else
        {
            return true;
        }
    }

    public boolean updateTestScanToAnalyse(String test_id,String content,String model_ids, String collection_id,String create_datetime,String timestamp)
    {
        try {

            ContentValues values = new ContentValues();
            values.put("test_status", 1);
            values.put("test_scan_result", content);
            values.put(AC, 1);
            if(!model_ids.trim().isEmpty()) values.put("model_ids", model_ids);
            if(!collection_id.trim().isEmpty()) values.put("collection_id", collection_id);
          //  values.put("create_datetime", create_datetime);
           // values.put("timestamp", timestamp);
            // updating row
            if(getWritableDatabase().update("tests", values,"test_id = ?",
                    new String[]{String.valueOf(test_id)})== -1)
            {
                return false ;
            }
            else
            {
                return true ;
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }

    }


    public boolean updateResultStatus(String test_id)
    {
        try {

            ContentValues values = new ContentValues();
            values.put(AR, 1);

            if(getWritableDatabase().update("tests", values,"test_id = ?",
                    new String[]{String.valueOf(test_id)})== -1)
            {
                return false ;
            }
            else{
                return true ;
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tests");
        onCreate(db);
    }
}
