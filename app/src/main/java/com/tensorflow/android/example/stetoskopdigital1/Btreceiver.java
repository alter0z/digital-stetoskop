package com.tensorflow.android.example.stetoskopdigital1;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.ansori.mqtt.MqttClient;
import com.tensorflow.android.R;
import com.tensorflow.android.audio.features.CleanWavFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.S)
public class Btreceiver extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1;
    private int INTERVAL = 10000;
    public static final String TAG_ID = "id";
    public static final String TAG_USERNAME = "username";
    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    TextView connStatus,receiveStatus,intervalStatus;
    Button refresh;
    ListView listViewPairedDevice;
    LinearLayout pane;
    Button btnDisconnect;
    private BluetoothDevice device;
    String data = "samples";
    private byte[] finalData;
    Handler handler = new Handler();
    Runnable runnable;
    private MqttClient client;
    String id, username;
    private boolean isConnected = false;
    private Dialog popup;
//    private TextView fileStatus;

    ArrayAdapter<String> pairedDeviceAdapter;
    private UUID myUUID;
    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetoothrecord);

        // get string intent
        id = getIntent().getStringExtra(TAG_ID);
        username = getIntent().getStringExtra(TAG_USERNAME);

//        Toast.makeText(this, "Pair your device first before using the app", Toast.LENGTH_LONG).show();

        refresh = findViewById(R.id.refresh);
        connStatus = findViewById(R.id.connStatus);
        receiveStatus = findViewById(R.id.recStatus);
        intervalStatus = findViewById(R.id.interval);
        listViewPairedDevice = findViewById(R.id.pairedlist);
//        pane = findViewById(R.id.clearPane);
        btnDisconnect = findViewById(R.id.disconnect);
        ImageButton back = findViewById(R.id.back);
        Button setInterval = findViewById(R.id.setint);
//        fileStatus = findViewById(R.id.writefile);

        popup = new Dialog(this);

        back.setOnClickListener(view -> {
            if (!isConnected) {
                Intent intent = new Intent(this, MainActivity1.class);
                // start your next activity
                intent.putExtra(TAG_ID,id);
                intent.putExtra(TAG_USERNAME,username);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Disconnect first!", Toast.LENGTH_LONG).show();
            }
        });
        setInterval.setOnClickListener(view -> {
            popup.setContentView(R.layout.duration_menu);
            popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            Button min_1,min_5,min_10;
            min_1 = popup.findViewById(R.id.min_1);
            min_5 = popup.findViewById(R.id.min_5);
            min_10 = popup.findViewById(R.id.min_10);
            popup.show();

            min_1.setOnClickListener(v -> {
                INTERVAL = 1000*60;
                intervalStatus.setText("Your data will be sent every 1 minute");
                popup.dismiss();
            });

            min_5.setOnClickListener(v -> {
                INTERVAL = 1000*(60*5);
                intervalStatus.setText("Your data will be sent every 5 minute");
                popup.dismiss();
            });

            min_10.setOnClickListener(v -> {
                INTERVAL = 1000*(60*10);
                intervalStatus.setText("Your data will be sent every 10 minute");
                popup.dismiss();
            });
        });

        // mqtt service object
//        client = new MqttClient(this);

        refresh.setOnClickListener(v -> {
            startActivity(new Intent(Btreceiver.this, Btreceiver.class));
            finish();
//            saveSampleWav(data);
//            saveCleanWav();
        });
        btnDisconnect.setOnClickListener(v -> {
            if (myThreadConnectBTdevice != null) {
                myThreadConnectBTdevice.cancel();
                isConnected = false;
                refresh.setVisibility(View.VISIBLE);
                listViewPairedDevice.setVisibility(View.VISIBLE);
                btnDisconnect.setVisibility(View.GONE);
            }
        });

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            Toast.makeText(this, "Your device does not support Bluetooth", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //using the well-known SPP UUID
        String UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB";
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent;
            if (checkBtPermission()) {
                enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_PERMISSION);
            }
        }

        setup();
    }

    private void setup() {
        if (checkBtPermission()) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                pairedDeviceArrayList = new ArrayList<>();

                pairedDeviceArrayList.addAll(pairedDevices);

                pairedDeviceAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, f(pairedDeviceArrayList));
                listViewPairedDevice.setAdapter(pairedDeviceAdapter);

                listViewPairedDevice.setOnItemClickListener((parent, view, position, id) -> {
                    device = pairedDeviceArrayList.get(position);
                    Toast.makeText(Btreceiver.this, "Connectiong to  " + device.getName(), Toast.LENGTH_LONG).show();

                    connStatus.setText("Connecting to " + device.getName());
                    myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                    myThreadConnectBTdevice.start();
                });
            }
        }
    }

    @Override
    protected void onResume() {
        handler.postDelayed(runnable = () -> {
            handler.postDelayed(runnable,INTERVAL);
            if (isConnected) {
                saveSampleWav(data);
                saveCleanWav();
//                client.getPublish(this,username+"_"+id,data);
            }
        },INTERVAL);
        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

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
            isConnected = false;
        }
    }

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

        private ThreadConnectBTdevice(BluetoothDevice device) {
            if (checkBtPermission()) {
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
//                    textStatus.setText("bluetoothSocket: \n" + bluetoothSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            boolean success = false;
            if (checkBtPermission()) {
                try {
                    bluetoothSocket.connect();
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();

                    runOnUiThread(() -> connStatus.setText("Could not connect to your device!"));

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
                isConnected = true;

                runOnUiThread(() -> {

                    connStatus.setText(connMessage);
                    Toast.makeText(Btreceiver.this, "connection successful", Toast.LENGTH_LONG).show();

                    listViewPairedDevice.setVisibility(View.GONE);
                    btnDisconnect.setVisibility(View.VISIBLE);
                    refresh.setVisibility(View.GONE);
//                    pane.setVisibility(View.VISIBLE);
                });

                startThreadConnected(bluetoothSocket);
            }
        }

        public void cancel() {

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
            OutputStream out;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
                out.write(1024);
            } catch (IOException e) {
                e.printStackTrace();
            }

            connectedInputStream = in;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // continuous data
            while (isConnected) {
                try {
                    bytes = connectedInputStream.read(buffer);

                    final String strReceived = new String(buffer, 0, bytes);
                    Log.v("Data masuk1 : ", strReceived);
                    data = strReceived;

                    runOnUiThread(() -> receiveStatus.setText("Receiving data ..."));

                } catch (IOException e) {
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost";
                    runOnUiThread(() -> {
                        connStatus.setText(msgConnectionLost);
                        receiveStatus.setText("No data Received");
                    });
                }
            }
        }
    }

    private void saveSampleWav(String data) {
        if (isExtStorageWritable() && checkPermission()) {
            try {
                ContextWrapper contextWrapper = new ContextWrapper(this);
                File audioDir = contextWrapper.getExternalFilesDir("sample wav");
//                File files = new File(audioDir,System.currentTimeMillis()+".wav");
                File files = new File(audioDir,"sample.wav");
                FileOutputStream fos = new FileOutputStream(files);
                fos.write(data.getBytes());
                fos.close();
//                fileStatus.setText("saved to "+audioDir.getAbsolutePath());
                Toast.makeText(Btreceiver.this, "saved to "+audioDir.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
//                fileStatus.setText(e.toString());
                Toast.makeText(Btreceiver.this, e.toString(), Toast.LENGTH_LONG).show();
            }
        } else {
//            fileStatus.setText("Cant write data");
            Toast.makeText(Btreceiver.this, "Cant write data", Toast.LENGTH_LONG).show();
        }
    }

    private void saveCleanWav() {
        if (isExtStorageWritable() && checkPermission()) {
            try{
                ContextWrapper contextWrapper = new ContextWrapper(this);
                File file = contextWrapper.getExternalFilesDir("sample wav");
                String audioFileAbsolutePath = file.getAbsolutePath()+"/samples2.wav";
                System.out.println(audioFileAbsolutePath);
                CleanWavFile audioFileProcess = new CleanWavFile();
                float[] audioData = audioFileProcess.ReadingAudioFile(audioFileAbsolutePath);

//                float[] manipulatedAudioData = new float[audioData.length];
            /*
              Assume did some manipulation on the wav file aka over audioData array[] and got new array named manipulatedAudioData[]
            */
                short[] int16 =  float32ToInt16(audioData); // suppose, the new wav file's each sample will be in int16 Format
//                short[] data = (short) audioData;
                audioFileProcess.WriteCleanAudioWav(this,System.currentTimeMillis()+".wav", int16);
            }
            catch (Exception e){
//                fileStatus.setText(e.toString());
                System.out.println("Error: "+e);
            }
        } else {
//            fileStatus.setText("Cant write data");
            Toast.makeText(Btreceiver.this, "Cant write data", Toast.LENGTH_LONG).show();
        }
    }

    public static short[] float32ToInt16(float[] arr){  //int[]
        // logic is built to support the numpy.int16 output
        short[] int16Arr = new short [arr.length];
        for(int i=0; i<arr.length; i++){
            if(arr[i]<0) {
                if (arr[i]>-1) {
                    int16Arr[i] = 0;
                }
                else{
                    int16Arr[i] = (short) Math.ceil(arr[i]);
                }
            }
            else if (arr[i]>0){
                int16Arr[i] = (short) Math.floor(arr[i]);
            }
            else{
                int16Arr[i] = 0;
            }
        }
        return int16Arr;
    }

    private boolean checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_PERMISSION);
        }

        int check = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return check == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isExtStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    protected boolean checkBtPermission() {
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},REQUEST_PERMISSION);
        }

        int check = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
        return check == PackageManager.PERMISSION_GRANTED;
    }
}
