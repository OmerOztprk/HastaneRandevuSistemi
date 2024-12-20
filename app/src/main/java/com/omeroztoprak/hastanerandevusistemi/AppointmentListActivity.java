package com.omeroztoprak.hastanerandevusistemi;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AppointmentListActivity extends AppCompatActivity {

    private SQLiteHelper dbHelper;
    private ListView appointmentListView;
    private EditText tcEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_list);

        initializeViews();
        findViewById(R.id.searchButton).setOnClickListener(v -> searchAppointments());
    }

    private void initializeViews() {
        appointmentListView = findViewById(R.id.appointmentListView);
        tcEditText = findViewById(R.id.tcEditText);
        dbHelper = new SQLiteHelper(this);
    }

    private void searchAppointments() {
        String userTC = tcEditText.getText().toString().trim();

        if (userTC.isEmpty()) {
            showToast("Lütfen TC Kimlik Numaranızı giriniz.");
            return;
        }

        ArrayList<String> appointments = getAppointments(userTC);
        if (appointments.isEmpty()) {
            showToast("Bu TC'ye ait randevu bulunmamaktadır.");
        } else {
            displayAppointments(appointments);
        }
    }

    private ArrayList<String> getAppointments(String userTC) {
        ArrayList<String> appointmentsList = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM " + SQLiteHelper.TABLE_APPOINTMENTS + " WHERE " + SQLiteHelper.COLUMN_TC + " = ?", new String[]{userTC});

        while (cursor.moveToNext()) {
            String appointment = String.format("TC: %s\nAd: %s\nSoyad: %s\nHastane: %s\nTarih: %s\nSaat: %s",
                    cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TC)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_SURNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_HOSPITAL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TIME)));
            appointmentsList.add(appointment);
        }
        cursor.close();
        database.close();
        return appointmentsList;
    }

    private void displayAppointments(ArrayList<String> appointments) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appointments);
        appointmentListView.setAdapter(adapter);
        appointmentListView.setOnItemClickListener((parent, view, position, id) ->
                showOptionsDialog(appointments.get(position), position, adapter));
    }

    private void deleteAppointment(String tc) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int rowsDeleted = database.delete(SQLiteHelper.TABLE_APPOINTMENTS, SQLiteHelper.COLUMN_TC + " = ?", new String[]{tc});
        showToast(rowsDeleted > 0 ? "Randevu başarıyla silindi." : "Randevu silinemedi.");
        database.close();
    }

    private void updateAppointment(String oldTc, ContentValues values) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int rowsUpdated = database.update(SQLiteHelper.TABLE_APPOINTMENTS, values, SQLiteHelper.COLUMN_TC + " = ?", new String[]{oldTc});
        showToast(rowsUpdated > 0 ? "Randevu başarıyla güncellendi." : "Randevu güncellenemedi.");
        database.close();
    }

    private void showOptionsDialog(String appointmentDetails, int position, ArrayAdapter<String> adapter) {
        String[] details = appointmentDetails.split("\n");
        String tc = details[0].split(": ")[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seçim Yapın")
                .setItems(new CharSequence[]{"Güncelle", "Sil", "İptal"}, (dialog, which) -> {
                    switch (which) {
                        case 0: showUpdateDialog(tc, appointmentDetails, position, adapter); break;
                        case 1: deleteAppointment(tc); adapter.remove(appointmentDetails); adapter.notifyDataSetChanged(); break;
                    }
                })
                .create().show();
    }

    private void showUpdateDialog(String tc, String appointmentDetails, int position, ArrayAdapter<String> adapter) {
        View updateView = getLayoutInflater().inflate(R.layout.dialog_update_appointment, null);
        EditText nameEditText = updateView.findViewById(R.id.nameEditText);
        EditText surnameEditText = updateView.findViewById(R.id.surnameEditText);
        EditText hospitalEditText = updateView.findViewById(R.id.hospitalEditText);
        EditText dateEditText = updateView.findViewById(R.id.dateEditText);
        EditText timeEditText = updateView.findViewById(R.id.timeEditText);

        String[] details = appointmentDetails.split("\n");
        nameEditText.setText(details[1].split(": ")[1]);
        surnameEditText.setText(details[2].split(": ")[1]);
        hospitalEditText.setText(details[3].split(": ")[1]);
        dateEditText.setText(details[4].split(": ")[1]);
        timeEditText.setText(details[5].split(": ")[1]);

        new AlertDialog.Builder(this)
                .setTitle("Randevu Güncelle")
                .setView(updateView)
                .setPositiveButton("Güncelle", (dialog, which) -> {
                    ContentValues values = new ContentValues();
                    values.put(SQLiteHelper.COLUMN_NAME, nameEditText.getText().toString().trim());
                    values.put(SQLiteHelper.COLUMN_SURNAME, surnameEditText.getText().toString().trim());
                    values.put(SQLiteHelper.COLUMN_HOSPITAL, hospitalEditText.getText().toString().trim());
                    values.put(SQLiteHelper.COLUMN_DATE, dateEditText.getText().toString().trim());
                    values.put(SQLiteHelper.COLUMN_TIME, timeEditText.getText().toString().trim());

                    updateAppointment(tc, values);
                    adapter.remove(adapter.getItem(position));
                    adapter.insert(String.format("TC: %s\nAd: %s\nSoyad: %s\nHastane: %s\nTarih: %s\nSaat: %s", tc, values.getAsString(SQLiteHelper.COLUMN_NAME), values.getAsString(SQLiteHelper.COLUMN_SURNAME), values.getAsString(SQLiteHelper.COLUMN_HOSPITAL), values.getAsString(SQLiteHelper.COLUMN_DATE), values.getAsString(SQLiteHelper.COLUMN_TIME)), position);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("İptal", null)
                .create().show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}