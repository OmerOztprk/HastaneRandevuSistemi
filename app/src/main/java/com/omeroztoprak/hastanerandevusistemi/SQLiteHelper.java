package com.omeroztoprak.hastanerandevusistemi;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RandevuDB"; // Veritabanı adı
    private static final int DATABASE_VERSION = 1; // Versiyon

    // Tablo adı ve sütunlar
    public static final String TABLE_APPOINTMENTS = "appointments";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TC = "tc";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SURNAME = "surname";
    public static final String COLUMN_HOSPITAL = "hospital";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";

    // SQL komutları
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_APPOINTMENTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TC + " TEXT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_SURNAME + " TEXT, " +
                    COLUMN_HOSPITAL + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_TIME + " TEXT);";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Veritabanı oluşturulurken tabloyu oluşturuyoruz
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    // Veritabanı sürümü yükseltildiğinde tabloyu güncelliyoruz
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        onCreate(db);
    }

}
