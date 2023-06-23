package com.tensorflow.android.example.stetoskopdigital1;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tensorflow.android.R;
import com.tensorflow.android.audio.features.CleanWavFile;
import com.tensorflow.android.audio.features.NormalizeAudio;
import com.tensorflow.android.services.MqttClient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.S)
public class Btreceiver extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 1;
    private int INTERVAL = 5000;
    public static final String TAG_ID = "id";
    public static final String TAG_USERNAME = "username";
    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> pairedDeviceArrayList;
    TextView connStatus, receiveStatus, intervalStatus;
    Button refresh;
    ListView listViewPairedDevice;
    LinearLayout pane;
    Button btnDisconnect;
    private BluetoothDevice device;
    private byte[] data;
    private byte[] finalData;
    Handler handler = new Handler();
    Runnable runnable;
    private static final int SAMPLE_RATE = 8000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private MqttClient client;
    String id, username;
    private boolean isConnected = false;
    private boolean isContinuesData = false;
    private Dialog popup;
    //    private TextView fileStatus;
    public static final String my_shared_preferences = "my_shared_preferences";
    SharedPreferences sharedpreferences;
    ArrayAdapter<String> pairedDeviceAdapter;
    private UUID myUUID;
    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;
    private OutputStream dataOutStream;
    private AudioRecord mAudioRecord;
    private boolean mIsRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetoothrecord);

        // get id & username
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        id = sharedpreferences.getString(TAG_ID, null);
        username = sharedpreferences.getString(TAG_USERNAME, null);

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

        client = new MqttClient(this);

        back.setOnClickListener(view -> {
            if (!isConnected) {
                Intent intent = new Intent(this, MainActivity1.class);
                intent.putExtra(TAG_ID, id);
                intent.putExtra(TAG_USERNAME, username);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Disconnect first!", Toast.LENGTH_LONG).show();
            }
        });
        setInterval.setOnClickListener(view -> {
            popup.setContentView(R.layout.duration_menu);
            popup.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            Button sec_5, sec_10, sec_15;
            sec_5 = popup.findViewById(R.id.sec_5);
            sec_10 = popup.findViewById(R.id.sec_10);
            sec_15 = popup.findViewById(R.id.sec_15);
            popup.show();

            sec_5.setOnClickListener(v -> {
                INTERVAL = 1000 * 5;
                intervalStatus.setText("Your data will be sent every 5 secon");
                popup.dismiss();
            });

            sec_10.setOnClickListener(v -> {
                INTERVAL = 1000 * 10;
                intervalStatus.setText("Your data will be sent every 10 secon");
                popup.dismiss();
            });

            sec_15.setOnClickListener(v -> {
                INTERVAL = 1000 * 15;
                intervalStatus.setText("Your data will be sent every 15 secon");
                popup.dismiss();
            });
        });

        refresh.setOnClickListener(v -> {
//            startActivity(new Intent(Btreceiver.this, Btreceiver.class));
//            finish();
            ContextWrapper contextWrapper = new ContextWrapper(this);
            File externalStorage = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS);
            String audioDirPath = externalStorage.getAbsolutePath();
            client.getPublish("foo/bar/baz", audioDirPath+"/1683819643452.wav");
//            System.out.println("asw "+audioDirPath+"/1683819643452.wav");
        });

        btnDisconnect.setOnClickListener(v -> {
            if (myThreadConnectBTdevice != null) {
                myThreadConnectBTdevice.cancel();
                isConnected = false;
                refresh.setVisibility(View.VISIBLE);
                listViewPairedDevice.setVisibility(View.VISIBLE);
                btnDisconnect.setVisibility(View.GONE);
            }

//            ContextWrapper contextWrapper = new ContextWrapper(this);
//            File externalStorage = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS);
//            String audioDirPath = externalStorage.getAbsolutePath();
//            client.getPublish("php-mqtt/client/test/pasien", audioDirPath+"1683819643452.wav");
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

        checkPermission();

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

//    @Override
//    protected void onResume() {
//        handler.postDelayed(runnable = () -> {
//            handler.postDelayed(runnable,INTERVAL);
//            if (isConnected && isContinuesData) {
//                client.getPublish("php-mqtt/client/test/pasien",data);
////                saveSampleWav(data);
////                saveCleanWav();
////                System.out.println(data);
//            }
//        },INTERVAL);
//        super.onResume();
//    }

//    @Override
//    protected void onPause() {
//        handler.removeCallbacks(runnable);
//        super.onPause();
//    }

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

            if (success) {
                //connect successful
                final String connMessage = "Connected to " + device.getName();
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
        private final FileOutputStream connectedOutputStream;
        private final BluetoothSocket connectedSocket;

        public ThreadConnected(BluetoothSocket socket) {
            InputStream in = null;
            FileOutputStream out = null;
            connectedSocket = socket;

            try {
                in = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (isExtStorageWritable() && checkPermission()) {
                try {
                    ContextWrapper contextWrapper = new ContextWrapper(Btreceiver.this);
//                    File audioDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS);
//                    File files = new File(audioDir,System.currentTimeMillis()+".wav");
                    File audioDir = contextWrapper.getExternalFilesDir("sample wav");
                    File files = new File(audioDir,"sample.wav");
                    out = new FileOutputStream(files);
                    writeWavHeader(out);

                } catch (Exception e) {
                    Toast.makeText(Btreceiver.this, e.toString(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(Btreceiver.this, "Cant write data", Toast.LENGTH_LONG).show();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = 0;
            long audioLength = 0;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            // continuous data
            if (isConnected) {
                audioLength += 44 - 8; // this actually header length - 8
//                isContinuesData = true;
                try {
                    runOnUiThread(() -> receiveStatus.setText("Receiving data"));

                    while (bytesRead  != -1) {
                        bytesRead = connectedInputStream.read(buffer);
                        if (bytesRead != -1) {
                            connectedOutputStream.write(buffer, 0, bytesRead);
//                            connectedOutputStream.write(buffer, 0, bytesRead);
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                            audioLength += bytesRead;
                        }
                        data = byteArrayOutputStream.toByteArray();
                    }
                } catch (IOException e) {
//                    long finalAudioLength = audioLength;
//                    runOnUiThread(() -> receiveStatus.setText(String.valueOf(finalAudioLength+36)));
//                    saveCleanWav();
                    try {
//                        writeWavHeader(connectedOutputStream);
                        ContextWrapper contextWrapper = new ContextWrapper(Btreceiver.this);
                        File file = contextWrapper.getExternalFilesDir("sample wav");
                        String audioFileAbsolutePath = file.getAbsolutePath()+"/sample.wav";
                        NormalizeAudio normalizeAudio = new NormalizeAudio();
                        normalizeAudio.normalize(audioFileAbsolutePath, Btreceiver.this);
                        connectedOutputStream.close();
//                        connectedSocket.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    e.printStackTrace();
                    final String msgConnectionLost = "Connection lost";
                    runOnUiThread(() -> {
                        connStatus.setText(msgConnectionLost);
                        receiveStatus.setText("No data received");
                    });
                }
            }
        }
    }

    private void saveCleanWav() {
        if (isExtStorageWritable() && checkPermission()) {
            try{
                ContextWrapper contextWrapper = new ContextWrapper(this);
                File file = contextWrapper.getExternalFilesDir("sample wav");
                String audioFileAbsolutePath = file.getAbsolutePath()+"/sample.wav";
                System.out.println(audioFileAbsolutePath);
                CleanWavFile audioFileProcess = new CleanWavFile();
                float[] audioData = audioFileProcess.ReadingAudioFile(audioFileAbsolutePath);
                short[] int16 =  float32ToInt16(audioData);
                audioFileProcess.WriteCleanAudioWav(this,System.currentTimeMillis()+".wav", int16);
            }
            catch (Exception e){
                System.out.println("Error: "+e);
            }
        } else {
            Toast.makeText(Btreceiver.this, "Cant write data", Toast.LENGTH_LONG).show();
        }
    }

    public static short[] float32ToInt16(float[] arr) {
        short[] int16Arr = new short [arr.length];
        for(int i=0; i<arr.length; i++){
            if(arr[i]<0) {
                if (arr[i]>-1) {
                    int16Arr[i] = 0;
                } else {
                    int16Arr[i] = (short) Math.ceil(arr[i]);
                }
            } else if (arr[i]>0){
                int16Arr[i] = (short) Math.floor(arr[i]);
            } else{
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

    private void writeWavHeader(FileOutputStream outputStream) throws IOException {
        long audioDataLength = outputStream.getChannel().size() - 44; // Subtract header size
        long overallSize = audioDataLength + 36; // Add header size

        byte[] header = new byte[44];

        // RIFF chunk descriptor
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';

        // Overall size (file size - 8 bytes for RIFF and WAVE tags)
        header[4] = (byte) (overallSize & 0xff);
        header[5] = (byte) ((overallSize >> 8) & 0xff);
        header[6] = (byte) ((overallSize >> 16) & 0xff);
        header[7] = (byte) ((overallSize >> 24) & 0xff);

        // WAVE chunk
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        // fmt sub-chunk
        header[12] = 'f'; // Sub-chunk identifier
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' '; // Chunk size
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // Audio format (PCM = 1)
        header[20] = 1;
        header[21] = 0;

        // Number of channels (2 = stereo)
        header[22] = (byte) (CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_MONO ? 1 : 2);
        header[23] = 0;

        // Sample rate
        header[24] = (byte) (SAMPLE_RATE & 0xff);
        header[25] = (byte) ((SAMPLE_RATE >> 8) & 0xff);
        header[26] = (byte) ((SAMPLE_RATE >> 16) & 0xff);
        header[27] = (byte) ((SAMPLE_RATE >> 24) & 0xff);

        // Byte rate (Sample rate * Number of channels * Bits per sample / 8)
        int byteRate = SAMPLE_RATE * (CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_MONO ? 1 : 2) * (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT ? 2 : 1);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);

        // Block align (Number of channels * Bits per sample / 8)
        header[32] = (byte) ((CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_MONO ? 1 : 2) * (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT ? 2 : 1));
        header[33] = 0;

        // Bits per sample
        header[34] = (byte) (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT ? 16 : 8);
        header[35] = 0;

        // data sub-chunk
        header[36] = 'd'; // Sub-chunk identifier
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a'; // Chunk size
        header[40] = (byte) (audioDataLength & 0xff);
        header[41] = (byte) ((audioDataLength >> 8) & 0xff);
        header[42] = (byte) ((audioDataLength >> 16) & 0xff);
        header[43] = (byte) ((audioDataLength >> 24) & 0xff);

        outputStream.write(header);
    }

//    private void writeWavHeader(FileOutputStream outputStream) throws IOException {
//        long audioDataLength = outputStream.getChannel().size() - 44; // Subtract header size
//        long overallSize = audioDataLength + 36; // Add header size
//
//        byte[] header = new byte[44];
//
//        // RIFF chunk descriptor
//        header[0] = 'R';
//        header[1] = 'I';
//        header[2] = 'F';
//        header[3] = 'F';
//
//        // Overall size (file size - 8 bytes for RIFF and WAVE tags)
//        header[4] = (byte) (overallSize & 0xff);
//        header[5] = (byte) ((overallSize >> 8) & 0xff);
//        header[6] = (byte) ((overallSize >> 16) & 0xff);
//        header[7] = (byte) ((overallSize >> 24) & 0xff);
//
//        // WAVE chunk
//        header[8] = 'W';
//        header[9] = 'A';
//        header[10] = 'V';
//        header[11] = 'E';
//
//        // fmt sub-chunk
//        header[12] = 'f'; // Sub-chunk identifier
//        header[13] = 'm';
//        header[14] = 't';
//        header[15] = ' '; // Chunk size
//        header[16] = 16;
//        header[17] = 0;
//        header[18] = 0;
//        header[19] = 0;
//        // Audio format (PCM = 1)
//        header[20] = 1;
//        header[21] = 0;
//
//        // Number of channels (2 = stereo)
//        header[22] = (byte) (CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1);
//        header[23] = 0;
//
//        // Sample rate
//        header[24] = (byte) (SAMPLE_RATE & 0xff);
//        header[25] = (byte) ((SAMPLE_RATE >> 8) & 0xff);
//        header[26] = (byte) ((SAMPLE_RATE >> 16) & 0xff);
//        header[27] = (byte) ((SAMPLE_RATE >> 24) & 0xff);
//
//        // Byte rate (Sample rate * Number of channels * Bits per sample / 8)
//        int byteRate = SAMPLE_RATE * (CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1) * (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT ? 2 : 1);
//        header[28] = (byte) (byteRate & 0xff);
//        header[29] = (byte) ((byteRate >> 8) & 0xff);
//        header[30] = (byte) ((byteRate >> 16) & 0xff);
//        header[31] = (byte) ((byteRate >> 24) & 0xff);
//
//        // Block align (Number of channels * Bits per sample / 8)
//        header[32] = (byte) ((CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1) * (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT ? 2 : 1));
//        header[33] = 0;
//
//        // Bits per sample
//        header[34] = (byte) (8);
//        header[35] = 0;
//
//        // data sub-chunk
//        header[36] = 'd'; // Sub-chunk identifier
//        header[37] = 'a';
//        header[38] = 't';
//        header[39] = 'a'; // Chunk size
//        header[40] = (byte) (audioDataLength & 0xff);
//        header[41] = (byte) ((audioDataLength >> 8) & 0xff);
//        header[42] = (byte) ((audioDataLength >> 16) & 0xff);
//        header[43] = (byte) ((audioDataLength >> 24) & 0xff);
//
//        outputStream.write(header);
//    }
}
