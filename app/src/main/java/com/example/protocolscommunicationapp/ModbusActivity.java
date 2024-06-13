package com.example.protocolscommunicationapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ghgande.j2mod.modbus.facade.ModbusSerialMaster;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.io.ModbusRTUTransport;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import com.ghgande.j2mod.modbus.util.SerialParameters;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.security.SecureRandom;
import java.util.UUID;

public class ModbusActivity extends AppCompatActivity {

    private Spinner spinnerModbusVariant;
    private EditText editTextIpAddress, editTextPort, editTextComPort, editTextBaudRate, editTextRegisterAddress, editTextValue, editTextDataBits,editTextParityBits,editTextStopBits;
    private RadioButton radioMaster,radioSlave,radioInput,radioHolding,radioCoil;
    private Button buttonConnect, buttonRead, buttonWrite;
    private TextView textViewLogs;
    private MaterialSwitch switchVerboseLogging;
    private LinearLayout tcpSettingsLayout, serialSettingsLayout;
    private ModbusTCPMaster modbusTCP;
    private ModbusSlave modbusSlave;
    private ModbusSerialMaster modbusSerial;
    private SerialParameters serialParameters;
    private int portNumber;
    SecureRandom secureRandom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modbus);

        spinnerModbusVariant = findViewById(R.id.spinnerModbusVariant);
        editTextIpAddress = findViewById(R.id.editTextIpAddress);
        editTextPort = findViewById(R.id.editTextPort);
        editTextComPort = findViewById(R.id.editTextCOMPort);
        editTextBaudRate = findViewById(R.id.editTextBaudRate);
        editTextRegisterAddress = findViewById(R.id.editTextRegisterAddress);
        editTextValue = findViewById(R.id.editTextValue);
        editTextDataBits = findViewById(R.id.editTextDataBits);
        editTextParityBits = findViewById(R.id.editTextParity);
        editTextStopBits = findViewById(R.id.editTextStopBits);
        radioMaster = findViewById(R.id.radioButtonMaster);
        radioSlave = findViewById(R.id.radioButtonSlave);
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonRead = findViewById(R.id.buttonRead);
        buttonWrite = findViewById(R.id.buttonWrite);
        textViewLogs = findViewById(R.id.textViewLogs);
        switchVerboseLogging = findViewById(R.id.switchVerboseLogging);
        tcpSettingsLayout = findViewById(R.id.tcpSettingsLayout);
        serialSettingsLayout = findViewById(R.id.modbusRTUSettingsLayout);
        radioInput = findViewById(R.id.radioButtonInput);
        radioHolding = findViewById(R.id.radioButtonHolding);

        secureRandom = new SecureRandom();

        selectModbusVariant();

        buttonConnect.setOnClickListener(v->{
            connectModbus();
        });

        writeRegisters();
    }

    private void writeRegisters() {
        try {
            if(modbusTCP.isConnected()){
                if(radioSlave.isChecked()){
                    int regAddress = Integer.parseInt(editTextIpAddress.getText().toString().trim());
                    int regValue = Integer.parseInt(editTextValue.getText().toString().trim());
                    modbusSlave = ModbusSlaveFactory.createTCPSlave(portNumber,5);
                    SimpleProcessImage image = new SimpleProcessImage();
                    if(radioHolding.isChecked()){
                        SimpleRegister reg = new SimpleRegister();
                        reg.setValue(regValue);
                        image.setRegister(regAddress,reg);
                    }
                    else if(radioInput.isChecked()){
                        SimpleRegister reg = new SimpleRegister();
                        reg.setValue(regValue);
                        image.setInputRegister(regAddress,reg);
                    }

                    int secureRandomInt = secureRandom.nextInt();
                    modbusSlave.addProcessImage(secureRandomInt,image);
                    modbusSlave.open();
                    modbusSlave.close();
                }
            }
            else if(modbusSerial.isConnected()){
                modbusSlave = ModbusSlaveFactory.createSerialSlave(serialParameters);

            }
        } catch (Exception e){
            logMessage(e.getMessage());
        }
        EIPDeviceEmulator
    }

    private void selectModbusVariant() {
        spinnerModbusVariant.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String protocol = parent.getItemAtPosition(position).toString();
                if (protocol.equals("Modbus RTU") || protocol.equals("Modbus ASCII")) {
                    serialSettingsLayout.setVisibility(View.VISIBLE);
                    tcpSettingsLayout.setVisibility(View.GONE);
                } else if (protocol.equals("Modbus TCP")) {
                    tcpSettingsLayout.setVisibility(View.VISIBLE);
                    serialSettingsLayout.setVisibility(View.GONE);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void connectModbus(){
        if(tcpSettingsLayout.getVisibility() == View.VISIBLE){
            String ipAddress = editTextIpAddress.getText().toString().trim();
            portNumber = Integer.parseInt(editTextPort.getText().toString().trim());

            try {

                modbusTCP = new ModbusTCPMaster(ipAddress,portNumber);
                modbusTCP.connect();
                logMessage("Connected to " + ipAddress + "on PORT" + portNumber);
            } catch (Exception e){
                logMessage(e.getMessage());
            }
        }
        else if(serialSettingsLayout.getVisibility() == View.VISIBLE){

            String comPort = editTextComPort.getText().toString().trim();
            int baudRate = Integer.parseInt(editTextBaudRate.getText().toString().trim());
            int dataBits = Integer.parseInt(editTextDataBits.getText().toString().trim());
            int parityBits = Integer.parseInt(editTextParityBits.getText().toString().trim());
            int stopBits = Integer.parseInt(editTextStopBits.getText().toString().trim());

            try {
                serialParameters = new SerialParameters();
                serialParameters.setPortName(comPort);
                serialParameters.setBaudRate(baudRate);
                serialParameters.setDatabits(dataBits);
                serialParameters.setParity(parityBits);
                serialParameters.setStopbits(stopBits);
                modbusSerial = new ModbusSerialMaster(serialParameters);

                modbusSerial.connect();
                logMessage("Connected to " + comPort);
            } catch (Exception e){
                logMessage(e.getMessage());
            }

        }
    }

    private void logMessage(String message) {
        textViewLogs.append(message + "\n");
    }
}