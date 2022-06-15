package com.tensorflow.android.example.stetoskopdigital1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.chibde.visualizer.LineVisualizer;
import com.tensorflow.android.R;
import com.tensorflow.android.example.stetoskopdigital1.Login;
import com.tensorflow.android.noiseclassifier.MainActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class RekamSuara extends AppCompatActivity {
    Button buttontnRekam, btnUpload;
    ImageButton btnBack;
    String id, username;
    public MediaPlayer mediaPlayer;

    public static final String TAG_ID = "id";
    public static final String TAG_USERNAME = "username";



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rekamsuara);


//        mediaPlayer.start();

        btnBack = (ImageButton) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> {
            Intent intent = new Intent(RekamSuara.this, MainActivity1.class);
            finish();
            intent.putExtra(TAG_ID, id);
            intent.putExtra(TAG_USERNAME, username);
            startActivity(intent);
        });
        SharedPreferences sharedpreferences = getSharedPreferences(Login.my_shared_preferences, Context.MODE_PRIVATE);

        id = getIntent().getStringExtra(TAG_ID);
        username = getIntent().getStringExtra(TAG_USERNAME);
    }

    public void lineVisualizstion(View view) {
        LineVisualizer lineVisualizer = findViewById(R.id.visualizerLine);

        lineVisualizer.setVisibility(View.VISIBLE);

        // set a custom color to the line.
        lineVisualizer.setColor(ContextCompat.getColor(this, R.color.material_on_background_emphasis_high_type));

        // set the line width for the visualizer between 1-10 default is  1.
        lineVisualizer.setStrokeWidth(1);

        // Setting the media player to the visualizer.
        lineVisualizer.setPlayer(mediaPlayer.getAudioSessionId());
    }


}
