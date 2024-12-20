package com.omeroztoprak.hastanerandevusistemi;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.omeroztoprak.hastanerandevusistemi.SQLiteHelper;

public class AppointmentDatabase {

    private SQLiteHelper dbHelper;
    private SQLiteDatabase database;

    public AppointmentDatabase(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    // Veritabanına yeni randevu eklemek için metod
    public void addAppointment(String tc, String name, String surname, String hospital, String date, String time) {
        database = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_TC, tc);
        values.put(SQLiteHelper.COLUMN_NAME, name);
        values.put(SQLiteHelper.COLUMN_SURNAME, surname);
        values.put(SQLiteHelper.COLUMN_HOSPITAL, hospital);
        values.put(SQLiteHelper.COLUMN_DATE, date);
        values.put(SQLiteHelper.COLUMN_TIME, time);

        database.insert(SQLiteHelper.TABLE_APPOINTMENTS, null, values);
        database.close();
    }
}
