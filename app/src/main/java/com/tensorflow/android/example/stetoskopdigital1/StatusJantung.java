package com.tensorflow.android.example.stetoskopdigital1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.tensorflow.android.R;
import com.tensorflow.android.noiseclassifier.MainActivity;


public class StatusJantung extends AppCompatActivity {
    ImageButton btnBack1;
    String id, username;

    public static final String TAG_ID = "id";
    public static final String TAG_USERNAME = "username";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cekstatus);


        btnBack1 = (ImageButton) findViewById(R.id.btnBack1);
        btnBack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StatusJantung.this, MainActivity1.class);
                finish();
                intent.putExtra(TAG_ID, id);
                intent.putExtra(TAG_USERNAME, username);
                startActivity(intent);
            }
        });
        SharedPreferences sharedpreferences = getSharedPreferences(Login.my_shared_preferences, Context.MODE_PRIVATE);

        id = getIntent().getStringExtra(TAG_ID);
        username = getIntent().getStringExtra(TAG_USERNAME);
    }
}
