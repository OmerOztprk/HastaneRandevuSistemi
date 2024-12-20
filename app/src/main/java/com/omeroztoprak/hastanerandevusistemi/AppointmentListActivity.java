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
        View updateView = getLayoutInflater().inflate(R.layout.dialog_update_appointment, null);
        EditText nameEditText = updateView.findViewById(R.id.nameEditText);
        EditText surnameEditText = updateView.findViewById(R.id.surnameEditText);
        EditText hospitalEditText = updateView.findViewById(R.id.hospitalEditText);
        EditText dateEditText = updateView.findViewById(R.id.dateEditText);
        EditText timeEditText = updateView.findViewById(R.id.timeEditText);

        String[] details = appointmentDetails.split("\n");

        // Mevcut randevu bilgilerini form alanlarına yerleştirme
        nameEditText.setText(details[1].split(": ")[1]);
        surnameEditText.setText(details[2].split(": ")[1]);
        hospitalEditText.setText(details[3].split(": ")[1]);
        dateEditText.setText(details[4].split(": ")[1]);
        timeEditText.setText(details[5].split(": ")[1]);

        // Güncelleme diyalog kutusunu gösterme
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

                    // Veritabanında güncelleme işlemi yap
                    updateAppointment(tc, values);

                    // Listeyi güncelle
                    adapter.remove(adapter.getItem(position));
                    adapter.insert(String.format("TC: %s\nAd: %s\nSoyad: %s\nHastane: %s\nTarih: %s\nSaat: %s", tc, values.getAsString(SQLiteHelper.COLUMN_NAME), values.getAsString(SQLiteHelper.COLUMN_SURNAME), values.getAsString(SQLiteHelper.COLUMN_HOSPITAL), values.getAsString(SQLiteHelper.COLUMN_DATE), values.getAsString(SQLiteHelper.COLUMN_TIME)), position);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("İptal", null)
                .create().show();
    }

    // Kullanıcıya kısa bir mesaj gösteren metot
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
