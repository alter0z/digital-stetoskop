package com.tensorflow.android.example.stetoskopdigital1;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.app.ActionBarActivity;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tensorflow.android.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Btreceiver extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    TextView textStatus;
    Button refresh;
    ListView listViewPairedDevice;
    LinearLayout pane;
    Button btnDisconnect;
    private BluetoothDevice device;
    private boolean isConnected = false;

    ArrayAdapter<String> pairedDeviceAdapter;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetoothrecord);

//        Toast.makeText(this, "Pair your device first before using the app", Toast.LENGTH_LONG).show();

        refresh = findViewById(R.id.refresh);
        textStatus = findViewById(R.id.status);
        listViewPairedDevice = findViewById(R.id.pairedlist);

        pane = findViewById(R.id.clearPane);

        refresh.setOnClickListener(v -> startActivity(new Intent(Btreceiver.this, Btreceiver.class)));
        btnDisconnect = findViewById(R.id.disconnect);
        btnDisconnect.setOnClickListener(v -> {
            if (myThreadConnectBTdevice != null) {
                myThreadConnectBTdevice.cancel();
            }
        });

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this,
                    "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //using the well-known SPP UUID
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onStart() {
        super.onStart();

        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = null;
            if (checkBtPermission()) {
                enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_PERMISSION);
            }
        }

        setup();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void setup() {
        if (checkBtPermission()) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                pairedDeviceArrayList = new ArrayList<BluetoothDevice>();

                pairedDeviceArrayList.addAll(pairedDevices);

                pairedDeviceAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, f(pairedDeviceArrayList));
                listViewPairedDevice.setAdapter(pairedDeviceAdapter);

                listViewPairedDevice.setOnItemClickListener((parent, view, position, id) -> {
                    device = pairedDeviceArrayList.get(position);
                    Toast.makeText(Btreceiver.this,
                            "Connectiong to  " + device.getName()
                            ,
                            Toast.LENGTH_LONG).show();

                    textStatus.setText("start ThreadConnectBTdevice");
                    myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                    myThreadConnectBTdevice.start();
                });
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private ArrayList<String> f(ArrayList<BluetoothDevice> pairedDeviceArrayList) {
        ArrayList<String> list = new ArrayList<>();
        if (checkBtPermission()) {
            for (int e = 0; e < pairedDeviceArrayList.size(); e++) {
                list.add(pairedDeviceArrayList.get(e).getName() + "\n" + pairedDeviceArrayList.get(e).getAddress());
            }

            return list;
        }

        return list;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (myThreadConnectBTdevice != null) {
            myThreadConnectBTdevice.cancel();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION) {
            if (resultCode == Activity.RESULT_OK) {
                setup();
            } else {
                Toast.makeText(this,
                        "BlueTooth NOT enabled",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket) {

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    protected class ThreadConnectBTdevice extends Thread {

        private BluetoothSocket bluetoothSocket;

        @RequiresApi(api = Build.VERSION_CODES.S)
        private ThreadConnectBTdevice(BluetoothDevice device) {
            if (checkBtPermission()) {
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                    textStatus.setText("bluetoothSocket: \n" + bluetoothSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        public void run() {
            boolean success = false;
            if (checkBtPermission()) {
                try {
                    bluetoothSocket.connect();
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();

                    runOnUiThread(() -> textStatus.setText("Could not connect to your device!"));

                    try {
                        bluetoothSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            if(success){
                //connect successful
                final String connMessage = "Connected to "+device.getName();

                runOnUiThread(() -> {

                    textStatus.setText(connMessage);
                    Toast.makeText(Btreceiver.this, "connection successful", Toast.LENGTH_LONG).show();

                    listViewPairedDevice.setVisibility(View.GONE);
                    pane.setVisibility(View.VISIBLE);
                    isConnected = true;
//                    startActivity(new Intent(Btreceiver.this,RekamSuara.class));
//                    finish();
                });

                startThreadConnected(bluetoothSocket);

            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "Disconnecting",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ThreadConnected extends Thread {
        private final InputStream connectedInputStream;

        public ThreadConnected(BluetoothSocket socket) {
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            connectedInputStream = in;
        }

        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Kayaknya ini datanya
            while (isConnected) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    //Log.i("Data masuk : ", String.valueOf(bytes));
                    final String strReceived = new String(buffer, 0, bytes);
                    Log.v("Data masuk1 : ", strReceived);

                    saveAudio(strReceived);

                    runOnUiThread(() -> textStatus.append(strReceived));

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:  "
                            + e.getMessage();
                    runOnUiThread(() -> textStatus.setText(msgConnectionLost));
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.S)
        private void saveAudio(String data) {
            if (isExtStorageWritable() && checkPermission()) {
                try {
                    ContextWrapper contextWrapper = new ContextWrapper(Btreceiver.this);
                    File audioDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS);
                    File files = new File(audioDir,System.currentTimeMillis()+".wav");
                    FileOutputStream fos = new FileOutputStream(files);
                    fos.write(data.getBytes());
                    fos.close();
                    Toast.makeText(Btreceiver.this, "saved to "+audioDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(Btreceiver.this, e.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(Btreceiver.this, "Cant write data", Toast.LENGTH_LONG).show();
            }
        }

        private boolean checkPermission() {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION);
            }

            int check = ContextCompat.checkSelfPermission(Btreceiver.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return check == PackageManager.PERMISSION_GRANTED;
        }

        private boolean isExtStorageWritable() {
            return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    protected boolean checkBtPermission() {
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},REQUEST_PERMISSION);
        }

        int check = ContextCompat.checkSelfPermission(Btreceiver.this, Manifest.permission.BLUETOOTH_CONNECT);
        return check == PackageManager.PERMISSION_GRANTED;
    }
}
