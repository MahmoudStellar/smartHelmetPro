package com.team_project.pro_smarthelmet.BluetoothV1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.team_project.pro_smarthelmet.R;

import java.util.ArrayList;
import java.util.List;

import me.aflak.bluetooth.Bluetooth;

public class Scan extends Activity implements Bluetooth.DiscoveryCallback, AdapterView.OnItemClickListener {
    Bluetooth bluetooth;
    ListView listView;
    ArrayAdapter<String> adapter;
    TextView state;
    ProgressBar progress;
    Button scan;
    List<BluetoothDevice> devices;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan);

        listView = findViewById(R.id.scan_list);
        state = findViewById(R.id.scan_state);
        progress = findViewById(R.id.scan_progress);
        scan = findViewById(R.id.scan_scan_again);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        bluetooth = new Bluetooth(this);
        bluetooth.setDiscoveryCallback(this);

        bluetooth.scanDevices();
        progress.setVisibility(View.VISIBLE);
        state.setText("Scanning...");
        listView.setEnabled(false);

        scan.setEnabled(false);
        devices = new ArrayList<>();

        scan.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.clear();
                        scan.setEnabled(false);
                    }
                });

                devices = new ArrayList<>();
                progress.setVisibility(View.VISIBLE);
                state.setText("Scanning...");
                bluetooth.scanDevices();
            }
        });
    }

    private void setText(final String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                state.setText(txt);
            }
        });
    }

    private void setProgressVisibility(final int id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(id);
            }
        });
    }

    @Override
    public void onFinish() {
        setProgressVisibility(View.INVISIBLE);
        setText("Scan finished!");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scan.setEnabled(true);
                listView.setEnabled(true);
            }
        });
    }

    @Override
    public void onDevice(final BluetoothDevice device) {
        devices.add(device);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(device.getAddress() + " - " + device.getName());
            }
        });
    }

    @Override
    public void onPair(BluetoothDevice device) {
        setProgressVisibility(View.INVISIBLE);
        setText("Paired!");
        Intent i = new Intent(Scan.this, Select.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onUnpair(BluetoothDevice device) {
        setProgressVisibility(View.INVISIBLE);
        setText("Paired!");
    }

    @Override
    public void onError(String message) {
        setProgressVisibility(View.INVISIBLE);
        setText("Error: " + message);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        setProgressVisibility(View.VISIBLE);
        setText("Pairing...");
        bluetooth.pair(devices.get(position));
    }
}
