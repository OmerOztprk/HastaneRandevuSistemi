package com.omeroztoprak.hastanerandevusistemi;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AppointmentListActivity extends AppCompatActivity {

    // SQLiteHelper sınıfından veritabanı işlemleri için gerekli olan dbHelper nesnesi
    private SQLiteHelper dbHelper;

    // Randevuları listelemek için kullanılan ListView
    private ListView appointmentListView;

    // Kullanıcının TC Kimlik Numarasını gireceği EditText
    private EditText tcEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_list);

        // Görünüm bileşenlerini başlatma
        initializeViews();

        // Arama butonuna tıklanması durumunda searchAppointments() metodunu çalıştıran dinleyici
        findViewById(R.id.searchButton).setOnClickListener(v -> searchAppointments());
    }

    // Görünüm bileşenlerini başlatan metod
    private void initializeViews() {
        appointmentListView = findViewById(R.id.appointmentListView);
        tcEditText = findViewById(R.id.tcEditText);
        dbHelper = new SQLiteHelper(this); // Veritabanı yardımcı sınıfını başlatma
    }

    // Kullanıcıdan alınan TC ile randevuları arama metodu
    private void searchAppointments() {
        // Kullanıcının TC Kimlik Numarasını al
        String userTC = tcEditText.getText().toString().trim();

        // TC numarası boşsa kullanıcıya uyarı göster
        if (userTC.isEmpty()) {
            showToast("Lütfen TC Kimlik Numaranızı giriniz.");
            return;
        }

        // Randevuları veritabanından al
        ArrayList<String> appointments = getAppointments(userTC);

        // Eğer randevu bulunamazsa uyarı göster
        if (appointments.isEmpty()) {
            showToast("Bu TC'ye ait randevu bulunmamaktadır.");
        } else {
            // Randevuları liste olarak görüntüle
            displayAppointments(appointments);
        }
    }

    // Veritabanından TC'ye ait randevuları çeken metot
    private ArrayList<String> getAppointments(String userTC) {
        ArrayList<String> appointmentsList = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // Veritabanında, verilen TC numarasına sahip randevuları sorgulama
        Cursor cursor = database.rawQuery("SELECT * FROM " + SQLiteHelper.TABLE_APPOINTMENTS + " WHERE " + SQLiteHelper.COLUMN_TC + " = ?", new String[]{userTC});

        // Her bir randevu için listeye ekleme
        while (cursor.moveToNext()) {
            String appointment = String.format("TC: %s\nAd: %s\nSoyad: %s\nHastane: %s\nTarih: %s\nSaat: %s",
                    cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TC)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_NAME)),  // Ad
                    cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_SURNAME)),  // Soyad
                    cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_HOSPITAL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(SQLiteHelper.COLUMN_TIME)));
            appointmentsList.add(appointment);
        }
        cursor.close();
        database.close();
        return appointmentsList;
    }

    // Randevuları listeleme ve tıklama olaylarını işleme metodu
    private void displayAppointments(ArrayList<String> appointments) {
        // ArrayAdapter ile randevu listesini göstermek
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, appointments);
        appointmentListView.setAdapter(adapter);

        // Bir randevuya tıklandığında seçenekleri gösteren bir diyalog açmak
        appointmentListView.setOnItemClickListener((parent, view, position, id) ->
                showOptionsDialog(appointments.get(position), position, adapter));
    }


    // TC'ye ait bir randevuyu silme metodu
    private void deleteAppointment(String tc) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Veritabanından TC'ye ait randevuyu silme işlemi
        int rowsDeleted = database.delete(SQLiteHelper.TABLE_APPOINTMENTS, SQLiteHelper.COLUMN_TC + " = ?", new String[]{tc});

        // Silme işlemi başarılı mı diye kontrol etme
        showToast(rowsDeleted > 0 ? "Randevu başarıyla silindi." : "Randevu silinemedi.");
        database.close();
    }

    // Randevuyu güncelleme metodu
    private void updateAppointment(String oldTc, ContentValues values) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Veritabanında randevuyu güncelleme işlemi
        int rowsUpdated = database.update(SQLiteHelper.TABLE_APPOINTMENTS, values, SQLiteHelper.COLUMN_TC + " = ?", new String[]{oldTc});

        // Güncelleme işlemi başarılı mı diye kontrol etme
        showToast(rowsUpdated > 0 ? "Randevu başarıyla güncellendi." : "Randevu güncellenemedi.");
        database.close();
    }

    // Randevu seçeneği (güncelleme, silme, iptal) diyalogunu gösteren metot
    private void showOptionsDialog(String appointmentDetails, int position, ArrayAdapter<String> adapter) {
        String[] details = appointmentDetails.split("\n");
        String tc = details[0].split(": ")[1]; // TC numarasını ayrıştır
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Seçim Yapın")
                .setItems(new CharSequence[]{"Güncelle", "Sil", "İptal"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // Güncelleme diyalogunu göster
                            showUpdateDialog(tc, appointmentDetails, position, adapter);
                            break;
                        case 1:
                            // Silme işlemi yap
                            deleteAppointment(tc);
                            adapter.remove(appointmentDetails);
                            adapter.notifyDataSetChanged();
                            break;
                    }
                })
                .create().show();
    }

    // Randevu güncelleme diyalogunu gösteren metot
    private void showUpdateDialog(String tc, String appointmentDetails, int position, ArrayAdapter<String> adapter) {
        // Güncellenmiş formu al
        View updateView = getLayoutInflater().inflate(R.layout.dialog_update_appointment, null);
        Spinner hospitalSpinner = updateView.findViewById(R.id.hospitalSpinner);
        Spinner dateSpinner = updateView.findViewById(R.id.dateSpinner);
        Spinner timeSpinner = updateView.findViewById(R.id.timeSpinner);

        // Seçenekler
        String[] hospitals = {"Hastane 1", "Hastane 2", "Hastane 3"};
        String[] dates = {"01.01.2024", "02.01.2024", "03.01.2024"};
        String[] times = {"09:00", "10:00", "11:00", "14:00", "15:00"};

        // ArrayAdapter ile spinner'ları dolduruyoruz
        ArrayAdapter<String> hospitalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hospitals);
        hospitalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hospitalSpinner.setAdapter(hospitalAdapter);

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dates);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(dateAdapter);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);

        // appointmentDetails metninden randevu bilgilerini ayrıştırma
        String[] details = appointmentDetails.split("\n");

        // Spinner'ları mevcut verilerle güncelleme
        hospitalSpinner.setSelection(getIndex(hospitals, details[3].split(": ")[1]));
        dateSpinner.setSelection(getIndex(dates, details[4].split(": ")[1]));
        timeSpinner.setSelection(getIndex(times, details[5].split(": ")[1]));

        // Güncelleme diyalog kutusunu gösterme
        new AlertDialog.Builder(this)
                .setTitle("Randevu Güncelle")
                .setView(updateView)
                .setPositiveButton("Güncelle", (dialog, which) -> {
                    // Seçilen yeni değerleri almak
                    ContentValues values = new ContentValues();
                    values.put(SQLiteHelper.COLUMN_HOSPITAL, hospitalSpinner.getSelectedItem().toString());
                    values.put(SQLiteHelper.COLUMN_DATE, dateSpinner.getSelectedItem().toString());
                    values.put(SQLiteHelper.COLUMN_TIME, timeSpinner.getSelectedItem().toString());

                    // Veritabanında güncelleme işlemi yap
                    updateAppointment(tc, values);

                    // Listeyi güncelle
                    adapter.remove(adapter.getItem(position));
                    adapter.insert(String.format("TC: %s\nAd: %s\nSoyad: %s\nHastane: %s\nTarih: %s\nSaat: %s",
                            tc, details[1].split(": ")[1], details[2].split(": ")[1],
                            values.getAsString(SQLiteHelper.COLUMN_HOSPITAL),
                            values.getAsString(SQLiteHelper.COLUMN_DATE),
                            values.getAsString(SQLiteHelper.COLUMN_TIME)), position);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("İptal", null)
                .create().show();
    }

    // Helper metod: Bir dizideki öğe için indeksini döndürür
    private int getIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        return 0; // Varsayılan olarak ilk öğe
    }



    // Kullanıcıya kısa bir mesaj gösteren metot
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
