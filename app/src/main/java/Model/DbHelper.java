package Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;


/** sql database helper class, adapted from DrBFraser code on youtube. found at https://youtu.be/Aui-kFuXFYE
 * and code at http://www.androidhive.info/2013/09/android-sqlite-database-with-multiple-tables/
 * Created by Taylor Rose on 4/7/2015.
 */
public class DbHelper extends SQLiteOpenHelper {

    // ----------------------------
    // ---------- Constants and Data
    // ----------------------------

    //for logging:
    private static final String TAG = "DbHelper";

    // Database name
    private static final String DATABASE_NAME = "pill_model_database";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String PILL_TABLE = "pills";
    private static final String ALARM_TABLE = "alarms";
    private static final String PILL_ALARM_LINKS = "pill_alarm";

    // Common column name and location
    public static final String KEY_ROWID = "id";
    public static final int COL_ROWID = 0;

    // Pill table columns
    private static final String KEY_PILLNAME = "pillName";

    // Alarm table columns
    private static final String KEY_ALARM_ID = "alarm_id";
    private static final String KEY_INTENT = "intent";
    private static final String KEY_HOUR = "hour";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_AM_PM = "am_pm";
    private static final String KEY_DAY_WEEK = "day_of_week";

    // Pill-Alarm link table columns
    private static final String KEY_PILLTABLE_ID = "pill_id";
    private static final String KEY_ALARMTABLE_ID = "alarm_id";

    // --------------------------------------------------
    // ------- statements to create tables --------------
    // --------------------------------------------------

    // Pill Table : create statement
    private static final String CREATE_PILL_TABLE = "create table " +
            PILL_TABLE + "(" + KEY_ROWID + " integer primary key," +
            KEY_PILLNAME + " text not null" + ")";

    // Alarm Table : create statement
    private static final String CREATE_ALARM_TABLE = "create table " +
            ALARM_TABLE + "(" + KEY_ROWID + " integer primary key," +
            KEY_ALARM_ID + " integer not null," + KEY_INTENT + " text not null," +
            KEY_HOUR + " integer," + KEY_MINUTE + " integer," +
            KEY_AM_PM + " text," + KEY_DAY_WEEK + " integer" + ")";

    // Pill-Alarm link table: create statement
    private static final String CREATE_PILL_ALARM_LINKS_TABLE = "create table " +
            PILL_ALARM_LINKS + "(" + KEY_ROWID + " integer primary key," +
            KEY_PILLTABLE_ID + " integer," + KEY_ALARMTABLE_ID + " integer" + ")";

    // Constructor
    DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // creating tables
        db.execSQL(CREATE_PILL_TABLE);
        db.execSQL(CREATE_ALARM_TABLE);
        db.execSQL(CREATE_PILL_ALARM_LINKS_TABLE);
    }

    @Override
    // TODO: change this so that updating doesn't delete old data
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + PILL_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PILL_ALARM_LINKS);

        // create new tables
        onCreate(db);
    }

    // --------------------------------------------
    // -------------- Individual Table Methods ----
    // --------------------------------------------

    // Create Methods

    public long createPill(Pill pill, long[] alarm_ids){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PILLNAME, pill.getPillName());

        // insert row
        long pill_id = db.insert(PILL_TABLE, null, values);

        // assigning alarms to pill ??
        for (long alarm_id : alarm_ids){
            createPillAlarmLink(pill_id, alarm_id);
        }

        return pill_id;
    }

    public long createAlarm(Alarm alarm){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // TODO: need to serialize intent
        //values.put(KEY_INTENT, alarm.getIntent());
        values.put(KEY_HOUR, alarm.getHour());
        values.put(KEY_MINUTE, alarm.getMinute());
        values.put(KEY_AM_PM, alarm.getAm_pm());
        values.put(KEY_DAY_WEEK, alarm.getDayOfWeek());

        //insert row
        long alarm_id = db.insert(ALARM_TABLE, null, values);

        return alarm_id;
    }

    public long createPillAlarmLink(long pill_id, long alarm_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PILLTABLE_ID, pill_id);
        values.put(KEY_ALARMTABLE_ID, alarm_id);

        //insert row
        long pillAlarmLink_id = db.insert(PILL_ALARM_LINKS, null, values);

        return pillAlarmLink_id;
    }

    // Get Methods

    // get a single pill
    public Pill getPill(long pill_id){
        SQLiteDatabase db = this.getReadableDatabase();

        String dbPill = "select * from " + PILL_TABLE + " where " +
                KEY_ROWID + " = " + pill_id;

        Cursor c = db.rawQuery(dbPill, null);

        if (c != null){
            c.moveToFirst();
        }

        Pill pill = new Pill();
        // tutorial i'm following has setID method, do we need that?
        pill.setPillName(c.getString(c.getColumnIndex(KEY_PILLNAME)));

        return pill;
    }

    //get all pills
    public List<Pill> getAllPills() {
        List<Pill> pills = new ArrayList<Pill>();
        String dbPills = "select * from " + PILL_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(dbPills, null);

        //loop through all rows, add to list
        if (c.moveToFirst()){
            do {
                Pill p = new Pill();
                p.setPillName(c.getString(c.getColumnIndex(KEY_PILLNAME)));

                pills.add(p);
            } while (c.moveToNext());

        }

        return pills;
    }

    // get a single alarm
    public Alarm getAlarm(long alarm_id){
        SQLiteDatabase db = this.getReadableDatabase();

        String dbAlarm = "SELECT * FROM " + ALARM_TABLE + " WHERE "
                + KEY_ALARM_ID + " = " + alarm_id;

        Cursor c = db.rawQuery(dbAlarm, null);

        if (c != null){
            c.moveToFirst();
        }

        Alarm al = new Alarm();
        al.setId(c.getInt(c.getColumnIndex(KEY_ALARM_ID)));
        // TODO: set intent
        //al.setIntent(c.get);
        al.setHour(c.getInt(c.getColumnIndex(KEY_HOUR)));
        al.setMinute(c.getInt(c.getColumnIndex(KEY_MINUTE)));
        al.setDayOfWeek(c.getInt(c.getColumnIndex(KEY_DAY_WEEK)));

        return al;
    }

    // get all Alarms
    public List<Alarm> getAllAlarms(){
        List<Alarm> allAlarms = new ArrayList<Alarm>();
        String selectQuery = "SELECT * FROM " + ALARM_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()){
            do {
                Alarm al = new Alarm();
                al.setId(c.getInt(c.getColumnIndex(KEY_ALARM_ID)));
                // TODO: set intent
                //al.setIntent(c.get);
                al.setHour(c.getInt(c.getColumnIndex(KEY_HOUR)));
                al.setMinute(c.getInt(c.getColumnIndex(KEY_MINUTE)));
                al.setDayOfWeek(c.getInt(c.getColumnIndex(KEY_DAY_WEEK)));

                allAlarms.add(al);
            } while (c.moveToNext());
        }

        return allAlarms;
    }


    public List<Alarm> getAllAlarmsByPill (String pillName){
        List<Alarm> alarmsByPill = new ArrayList<Alarm>();

        String selectQuery = "SELECT * FROM " + ALARM_TABLE + " alarm, "
                + PILL_TABLE + " pill, " + PILL_ALARM_LINKS + " pillAlarm WHERE pill."
                + KEY_PILLNAME + " = '" + pillName + "'" + " AND pill." + KEY_ROWID
                + " = " + "pillAlarm." + KEY_PILLTABLE_ID + " AND alarm." + KEY_ROWID
                + " = " + "pillAlarm." + KEY_ALARM_ID;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()){
            do {
                Alarm al = new Alarm();
                al.setId(c.getInt(c.getColumnIndex(KEY_ALARM_ID)));
                // TODO: set intent
                //al.setIntent(c.get);
                al.setHour(c.getInt(c.getColumnIndex(KEY_HOUR)));
                al.setMinute(c.getInt(c.getColumnIndex(KEY_MINUTE)));
                al.setDayOfWeek(c.getInt(c.getColumnIndex(KEY_DAY_WEEK)));

                alarmsByPill.add(al);
            } while (c.moveToNext());
        }

        return alarmsByPill;
    }

    //TODO: add update and delete methods
    // TODO: create test for database

    //----------------------------------
    //--------- close database method --
    // I don't really understand how this is different than just db.close yet
    //--------------------------------

    public void closeDB(){
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()){
            db.close();
        }
    }
}