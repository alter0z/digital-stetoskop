package com.tensorflow.android.example.stetoskopdigital1;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.tensorflow.android.R;
import com.tensorflow.android.noiseclassifier.MainActivity;


public class MainActivity1 extends AppCompatActivity {

    ImageButton btn_logout;
    TextView txt_id, txt_username;
    String id, username;
    SharedPreferences sharedpreferences;
    ImageView rekamJantung, statusJantung;

    public static final String TAG_ID = "id";
    public static final String TAG_USERNAME = "username";


    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);


        txt_username = (TextView) findViewById(R.id.txt_username);
        btn_logout = (ImageButton) findViewById(R.id.btn_logout);
        ImageView imageView = (ImageView)findViewById(R.id.statusJantung);
        ImageView imageView1 = (ImageView)findViewById(R.id.rekamJantung);
        imageView1.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity1.this, Btreceiver.class);
            finish();
            intent.putExtra(TAG_ID, id);
            intent.putExtra(TAG_USERNAME, username);
            startActivity(intent);
        });
        sharedpreferences = getSharedPreferences(Login.my_shared_preferences, Context.MODE_PRIVATE);

        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity1.this, MainActivity.class);
            finish();
            intent.putExtra(TAG_ID, id);
            intent.putExtra(TAG_USERNAME, username);
            startActivity(intent);
        });
        sharedpreferences = getSharedPreferences(Login.my_shared_preferences, Context.MODE_PRIVATE);

        id = getIntent().getStringExtra(TAG_ID);
        username = getIntent().getStringExtra(TAG_USERNAME);
        Log.i("Username", username);

        txt_username.setText("Halo " + username);



        btn_logout.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            // update login session ke FALSE dan mengosongkan nilai id dan username
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(Login.session_status, false);
            editor.putString(TAG_ID, null);
            editor.putString(TAG_USERNAME, null);
            editor.apply();

            Intent intent = new Intent(MainActivity1.this, Login.class);
            finish();
            startActivity(intent);
        });

    }
}