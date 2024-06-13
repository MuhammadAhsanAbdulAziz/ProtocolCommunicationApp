package com.example.protocolscommunicationapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button modbusBtn = findViewById(R.id.modbusBtn);
        Button ethernetBtn = findViewById(R.id.ethernetBtn);

        modbusBtn.setOnClickListener(v -> {
            startActivity(new Intent(this,ModbusActivity.class));
        });

        ethernetBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, EthernetActivity.class));
        });
    }
}