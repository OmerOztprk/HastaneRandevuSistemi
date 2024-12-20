package com.omeroztoprak.hastanerandevusistemi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Değişken tanımlamaları
    private Spinner hospitalSpinner, dateSpinner, timeSpinner;
    private EditText patientTC, patientName, patientSurname;
    private Button submitButton, viewAppointmentButton;
    private AppointmentDatabase appointmentDatabase; // Veritabanı nesnesi


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // XML bileşenlerini bağlama
        hospitalSpinner = findViewById(R.id.hospitalSpinner);
        dateSpinner = findViewById(R.id.dateSpinner);
        timeSpinner = findViewById(R.id.timeSpinner);
        patientTC = findViewById(R.id.patientTC);
        patientName = findViewById(R.id.patientName);
        patientSurname = findViewById(R.id.patientSurname);
        submitButton = findViewById(R.id.submitButton);
        viewAppointmentButton = findViewById(R.id.viewAppointmentButton);

        // Veritabanı nesnesini başlatma
        appointmentDatabase = new AppointmentDatabase(this);

        // Spinner'lara veri ekleme
        setupSpinners();

        // Kaydet butonu işlemi
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tc = patientTC.getText().toString();
                String name = patientName.getText().toString();
                String surname = patientSurname.getText().toString();
                String hospital = hospitalSpinner.getSelectedItem().toString();
                String date = dateSpinner.getSelectedItem().toString();
                String time = timeSpinner.getSelectedItem().toString();

                // Basit bir kontrol
                if (tc.isEmpty() || name.isEmpty() || surname.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Lütfen tüm alanları doldurunuz.", Toast.LENGTH_SHORT).show();
                } else {
                    // Veritabanına randevu ekleme
                    appointmentDatabase.addAppointment(tc, name, surname, hospital, date, time);
                    Toast.makeText(MainActivity.this, "Randevu Kaydedildi", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Görüntüleme butonu işlemi (örnek)
        viewAppointmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Randevuları görüntülemek için AppointmentListActivity'ye geçiş
                Intent intent = new Intent(MainActivity.this, AppointmentListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupSpinners() {
        // Hastane listesi
        String[] hospitals = {"Hastane 1", "Hastane 2", "Hastane 3"};
        ArrayAdapter<String> hospitalAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hospitals);
        hospitalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hospitalSpinner.setAdapter(hospitalAdapter);

        // Tarih listesi
        String[] dates = {"01.01.2024", "02.01.2024", "03.01.2024"};
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dates);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(dateAdapter);

        // Saat listesi
        String[] times = {"09:00", "10:00", "11:00", "14:00", "15:00"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);
    }
}
