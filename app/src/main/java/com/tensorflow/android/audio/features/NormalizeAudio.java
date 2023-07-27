package com.tensorflow.android.audio.features;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;

import com.tensorflow.android.example.stetoskopdigital1.Btreceiver;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class NormalizeAudio {
    public void normalize(String sampleAudioFile, String fileName, Context context) {
        short[] audioData1 = null;

        int n = 0;

        try {
            DataInputStream in1;
            in1 = new DataInputStream(new FileInputStream(sampleAudioFile));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            try {

                while ((n = in1.read()) != -1) {
                    bos.write(n);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteBuffer bb = ByteBuffer.wrap(bos.toByteArray());
            bb.order(ByteOrder.LITTLE_ENDIAN);
            ShortBuffer sb = bb.asShortBuffer();
            audioData1 = new short[sb.capacity()];

            for (int i = 0; i < sb.capacity(); i++) {
                audioData1[i] = sb.get(i);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // find the max:
        float max = 0;
        for (int i = 22; i < audioData1.length; i++) {
            if (Math.abs(audioData1[i]) > max)
                max = Math.abs(audioData1[i]);
        }

        System.out.println("" + (Short.MAX_VALUE - max));

        int a, b, c;

        // now find the result, with scaling:
        for (int i = 22; i < audioData1.length; i++) {
            a = audioData1[i];

            c = Math.round(Short.MAX_VALUE * (audioData1[i]) / max);

            if (c > Short.MAX_VALUE)
                c = Short.MAX_VALUE;
            if (c < Short.MIN_VALUE)
                c = Short.MIN_VALUE;


            audioData1[i] = (short) c;

        }

        // to turn shorts back to bytes.
        byte[] end = new byte[audioData1.length * 2];
        ByteBuffer.wrap(end).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(audioData1);

        try {
            ContextWrapper contextWrapper = new ContextWrapper(context);
            File audioDir = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                audioDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS);
            }
            File files = new File(audioDir,System.currentTimeMillis()+"-"+fileName+".wav");
            OutputStream out  = new FileOutputStream(files);

            for (byte value : end) {
                out.write(value);
                out.flush();
            }

            out.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
