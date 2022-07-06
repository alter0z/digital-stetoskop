package com.tensorflow.android.audio.features;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class CleanWavFile {
    int[] type = {0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 1};
    int[] numberOfBytes = {4, 4, 4, 4, 4, 2, 2, 4, 4, 2, 2, 4, 4};
    int chunkSize, subChunk1Size, sampleRate, byteRate, subChunk2Size=1, bytePerSample;
    short audioFomart, numChannels, blockAlign, bitsPerSample=8;
    String chunkID, format, subChunk1ID, subChunk2ID;

    public ByteBuffer ByteArrayToNumber(byte[] bytes, int numOfBytes, int type){
        ByteBuffer buffer = ByteBuffer.allocate(numOfBytes);
        if (type == 0){
            buffer.order(BIG_ENDIAN); // Check the illustration. If it says little endian, use LITTLE_ENDIAN
        }
        else{
            buffer.order(LITTLE_ENDIAN);
        }
        buffer.put(bytes);
        buffer.rewind();
        return buffer;
    }

    public float convertToFloat(byte[] array, int type) {
        ByteBuffer buffer = ByteBuffer.wrap(array);
        if (type == 1){
            buffer.order(LITTLE_ENDIAN);
        }
        return buffer.getShort();
    }

    public float[] ReadingAudioFile(String audioFile) {
        try {
            File file = new File(audioFile);
            // int length = (int) file.length();
            //System.out.println(length);
            InputStream fileInputstream = new FileInputStream(file);
            ByteBuffer byteBuffer;
            for(int i=0; i<numberOfBytes.length; i++){
                byte[] byteArray = new byte[numberOfBytes[i]];
                int r = fileInputstream.read(byteArray, 0, numberOfBytes[i]);
                byteBuffer = ByteArrayToNumber(byteArray, numberOfBytes[i], type[i]);
                if (i == 0) {chunkID =  new String(byteArray); System.out.println(chunkID);}
                if (i == 1) {chunkSize = byteBuffer.getInt(); System.out.println(chunkSize);}
                if (i == 2) {format =  new String(byteArray); System.out.println(format);}
                if (i == 3) {subChunk1ID = new String(byteArray);System.out.println(subChunk1ID);}
                if (i == 4) {subChunk1Size = byteBuffer.getInt(); System.out.println(subChunk1Size);}
                if (i == 5) {audioFomart = byteBuffer.getShort(); System.out.println(audioFomart);}
                if (i == 6) {numChannels = byteBuffer.getShort();System.out.println(numChannels);}
                if (i == 7) {sampleRate = byteBuffer.getInt();System.out.println(sampleRate);}
                if (i == 8) {byteRate = byteBuffer.getInt();System.out.println(byteRate);}
                if (i == 9) {blockAlign = byteBuffer.getShort();System.out.println(blockAlign);}
                if (i == 10) {bitsPerSample = byteBuffer.getShort();System.out.println(bitsPerSample);}
                if (i == 11) {
                    subChunk2ID = new String(byteArray) ;
                    if(subChunk2ID.compareTo("data") == 0) {
                        continue;
                    }
                    else if( subChunk2ID.compareTo("LIST") == 0) {
                        byte[] byteArray2 = new byte[4];
                        r = fileInputstream.read(byteArray2, 0, 4);
                        byteBuffer = ByteArrayToNumber(byteArray2, 4, 1);
                        int temp = byteBuffer.getInt();
                        //redundant data reading
                        byte[] byteArray3 = new byte[temp];
                        r = fileInputstream.read(byteArray3, 0, temp);
                        r = fileInputstream.read(byteArray2, 0, 4);
                        subChunk2ID = new String(byteArray2) ;
                    }
                }
                if (i == 12) {subChunk2Size = byteBuffer.getInt();System.out.println(subChunk2Size);}
            }
            bytePerSample = bitsPerSample/8;
            float value;
            ArrayList<Float> dataVector = new ArrayList<>();
            while (true){
                byte[] byteArray = new byte[bytePerSample];
                int v = fileInputstream.read(byteArray, 0, bytePerSample);
                value = convertToFloat(byteArray,1);
                dataVector.add(value);
                if (v == -1) break;
            }
            float[] data = new float[dataVector.size()];
            for(int i=0;i<dataVector.size();i++){
                data[i] = dataVector.get(i);
            }
//            System.out.println("Total data bytes "+sum);
            return data;
        }
        catch (Exception e){
            System.out.println("Error: "+e);
            float[] f = new float[1];
            return f;
        }
    }

    public byte[] NumericalArray2ByteArray(short[] values){
        ByteBuffer buffer = ByteBuffer.allocate(2 * values.length);
        buffer.order(LITTLE_ENDIAN); // data must be in little endian format
        for (short value : values){
            buffer.putShort(value);
        }
        buffer.rewind();
        return buffer.array();
    }

    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;
    }

//    public void checkExternalMedia(){
//        boolean mExternalStorageAvailable = false;
//        boolean mExternalStorageWriteable = false;
//        String state = Environment.getExternalStorageState();
//
//        if (Environment.MEDIA_MOUNTED.equals(state)) {
//            // Can read and write the media
//            mExternalStorageAvailable = mExternalStorageWriteable = true;
//        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
//            // Can only read the media
//            mExternalStorageAvailable = true;
//            mExternalStorageWriteable = false;
//        } else {
//            // Can't read or write
//            mExternalStorageAvailable = mExternalStorageWriteable = false;
//        }
//        System.out.println("\n\nExternal Media: readable="
//                +mExternalStorageAvailable+" writable="+mExternalStorageWriteable);
//    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void WriteCleanAudioWav(Context context, String newFileName, short[] wavData) {
//        File dir = null;
//        try{
//            checkExternalMedia();
//            File root = android.os.Environment.getExternalStorageDirectory();
////            System.out.println("\nExternal file system root: "+root);
//            dir = new File (root.getAbsolutePath() + "/clean_speech");
//            if (dir.exists() == false){
//                dir.mkdirs();
//                System.out.println("Directory created");
//                System.out.println(dir);
//            }
//            else{
//                System.out.println("Directory exists");
//                System.out.println(dir);
//            }
//        }
//        catch (Exception e){
//            System.out.println("Error "+e);
//        }
        try {
            ContextWrapper contextWrapper = new ContextWrapper(context);
            File audioDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS);
            File files = new File(audioDir,newFileName);
//            File file = new File(dir, "clean_20_sec.wav");
//            if (file.exists()) {
//                System.out.println("YES file exists");
//            }
            //System.out.println(file);
            OutputStream os;
            //System.out.println(newFileName);
            os = new FileOutputStream(files);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream outFile = new DataOutputStream(bos);

            try {
                long mySubChunk1Size = 16;
                int myBitsPerSample= bitsPerSample;
                int myFormat = audioFomart;
                long myChannels = numChannels;
                long mySampleRate = sampleRate;
                long myByteRate = mySampleRate * myChannels * myBitsPerSample/8;
                int myBlockAlign = (int) (myChannels * myBitsPerSample/8);
                System.out.println("Ei porjonto completed");
                byte[] clipData = NumericalArray2ByteArray(wavData);
                System.out.println("Ei porjonto completed 1");
                long myDataSize = clipData.length;
                long myChunk2Size =  myDataSize * myChannels * myBitsPerSample/8;
                long myChunkSize = 36 + myChunk2Size;

                outFile.writeBytes("RIFF");  // 00 - RIFF
                outFile.writeInt(Integer.reverseBytes((int)myChunkSize)); // 04 - how big is the rest of this file?
                outFile.writeBytes("WAVE");                                 // 08 - WAVE
                outFile.writeBytes("fmt ");                                 // 12 - fmt
                outFile.writeInt(Integer.reverseBytes((int)mySubChunk1Size));  // 16 - size of this chunk
                outFile.writeShort(Short.reverseBytes((short)myFormat));     // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
                outFile.writeShort(Short.reverseBytes((short)myChannels));   // 22 - mono or stereo? 1 or 2?  (or 5 or ???)
                outFile.writeInt(Integer.reverseBytes((int)mySampleRate));     // 24 - samples per second (numbers per second)
                outFile.writeInt(Integer.reverseBytes((int)myByteRate));       // 28 - bytes per second
                outFile.writeShort(Short.reverseBytes((short)myBlockAlign)); // 32 - # of bytes in one sample, for all channels
                outFile.writeShort(Short.reverseBytes((short)myBitsPerSample));  // 34 - how many bits in a sample(number)?  usually 16 or 24
                outFile.writeBytes("data");                                 // 36 - data
                outFile.writeInt(Integer.reverseBytes((int)myDataSize));       // 40 - how big is this data chunk

//                outFile.writeBytes("RIFF");                                          // 00 - RIFF
//                outFile.write(intToByteArray((int)myChunkSize), 0, 4);          // 04 - how big is the rest of this file?
//                outFile.writeBytes("WAVE");                                          // 08 - WAVE
//                outFile.writeBytes("fmt ");                                          // 12 - fmt
//                outFile.write(intToByteArray((int)mySubChunk1Size), 0, 4);      // 16 - size of this chunk
//                outFile.write(shortToByteArray((short)myFormat), 0, 2);         // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
//                outFile.write(shortToByteArray((short)myChannels), 0, 2);       // 22 - mono or stereo? 1 or 2?  (or 5 or ???)
//                outFile.write(intToByteArray((int)mySampleRate), 0, 4);         // 24 - samples per second (numbers per second)
//                outFile.write(intToByteArray((int)myByteRate), 0, 4);           // 28 - bytes per second
//                outFile.write(shortToByteArray((short)myBlockAlign), 0, 2);     // 32 - # of bytes in one sample, for all channels
//                outFile.write(shortToByteArray((short)myBitsPerSample), 0, 2);  // 34 - how many bits in a sample(number)?  usually 16 or 24
//                outFile.writeBytes("data");                                          // 36 - data
//                outFile.write(intToByteArray((int)myDataSize), 0, 4);           // 40 - how big is this data chunk
//                byte[] header = new byte[44];
//
//                header[0] = 'R';  // RIFF/WAVE header
//                header[1] = 'I';
//                header[2] = 'F';
//                header[3] = 'F';
//                header[4] = (byte) (myDataSize & 0xff);
//                header[5] = (byte) ((myDataSize >> 8) & 0xff);
//                header[6] = (byte) ((myDataSize >> 16) & 0xff);
//                header[7] = (byte) ((myDataSize >> 24) & 0xff);
//                header[8] = 'W';
//                header[9] = 'A';
//                header[10] = 'V';
//                header[11] = 'E';
//                header[12] = 'f';  // 'fmt ' chunk
//                header[13] = 'm';
//                header[14] = 't';
//                header[15] = ' ';
//                header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
//                header[17] = 0;
//                header[18] = 0;
//                header[19] = 0;
//                header[20] = 1;  // format = 1
//                header[21] = 0;
//                header[22] = (byte) myChannels;
//                header[23] = 0;
//                header[24] = (byte) (mySampleRate & 0xff);
//                header[25] = (byte) ((mySampleRate >> 8) & 0xff);
//                header[26] = (byte) ((mySampleRate >> 16) & 0xff);
//                header[27] = (byte) ((mySampleRate >> 24) & 0xff);
//                header[28] = (byte) (byteRate & 0xff);
//                header[29] = (byte) ((byteRate >> 8) & 0xff);
//                header[30] = (byte) ((byteRate >> 16) & 0xff);
//                header[31] = (byte) ((byteRate >> 24) & 0xff);
//                header[32] = (byte) (2 * 16 / 8);  // block align
//                header[33] = 0;
//                header[34] = (byte) myBitsPerSample;  // bits per sample
//                header[35] = 0;
//                header[36] = 'd';
//                header[37] = 'a';
//                header[38] = 't';
//                header[39] = 'a';
//                header[40] = (byte) (myDataSize & 0xff);
//                header[41] = (byte) ((myDataSize >> 8) & 0xff);
//                header[42] = (byte) ((myDataSize >> 16) & 0xff);
//                header[43] = (byte) ((myDataSize >> 24) & 0xff);
//
//                outFile.write(header, 0, 44);
                outFile.write(clipData);                                         // 44 - the actual data itself - just a long string of numbers
                System.out.println(Arrays.toString(clipData));
            }
            catch (Exception e){
                System.out.println("Error "+e);
            }
            outFile.flush();
            outFile.close();
        }
        catch (Exception e){
            System.out.println("Error: "+e);
        }
    }

//    private static byte[] intToByteArray(int i) {
//        byte[] b = new byte[4];
//        b[0] = (byte) (i & 0x00FF);
//        b[1] = (byte) ((i >> 8) & 0x000000FF);
//        b[2] = (byte) ((i >> 16) & 0x000000FF);
//        b[3] = (byte) ((i >> 24) & 0x000000FF);
//        return b;
//    }
//
//    public static byte[] shortToByteArray(short data) {
//        return new byte[]{(byte)(data & 0xff),(byte)((data >>> 8) & 0xff)};
//    }
}
