Android app that classify heart valve disease with digital stethoscope device

# WAV File Documentation
berikut adalah dokumnetasi untuk membuat clean WAV file.
## Structur WAV
Berikut adalah struktur WAV file format
Order  | Offset | Name | Size (bytes) | Data Type | Expected value
------------ | ------------- | --------- | --------------- | ----------------| --------
big | 0 | ChunkID | 4 | String |  "RIFF"
little | 4 | ChunkSize | 4 | int | How big is the rest of this file
big | 8 | Format | 4 | String | "WAVE"
big | 12 | SubChunk1ID | 4 | String | "fmt " (there is a space)
little | 16 | SubChunk1Size | 4 | int | 16 for PCM
little | 20 | AudioFormat | 2 | short | 1 for PCM
little | 22 | NumChannels | 2 | short | 1 for mono, 2 for stereo
little | 24 | SampleRate | 4 | int | 16000, 44100, etc.
little | 28 | ByteRate | 4 | int | SampleRate * NumChannels * BitsPerSample/8
little | 32 | BlockAlign | 2 | short | NumChannels * BitsPerSample/8
little | 34 | BitsPerSample | 2 | short | 8 bits = 8, 16 bits = 16, etc
big | 36 | SubChunk2ID | 4 | string | "data"
little | 40 | SubChunk2Size | 4 | int | data size (data length)
little | 44 | data |-|-| the actual data
## Method
Pada kasus ini metode yang sy gunakan adalah penulisan ulang WAV data dengan beberapa step.
### Step 1
pada step pertama ini data yang ditransmisikan akan disimpan sebagai file WAV tanpa struktur yang jelas.
```
private void saveSampleWav(String data) {
    if (isExtStorageWritable() && checkPermission()) {
        try {
            ContextWrapper contextWrapper = new ContextWrapper(this);
            File audioDir = contextWrapper.getExternalFilesDir("sample wav");
            File files = new File(audioDir,"sample.wav");
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
```
pada code line diatas, data yang ditrasnmisikan disimpan pada direktori sample wav dengan nama file sample.wav
### Step 2
Pada step ke 2 ini, data sample.wav akan di extract terlebih dahulu sebelum ditulis ulang
```
File file = new File(audioFile);
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
    .....
```
Code di atas adalah proses pengekstrakan structure WAV file yang data nya diambil dari method saveSampleWav(). Terdapat beberapa bagian yang dikeluarkan dan di ambl isi setiap byte nya sebanyak ukuran number of bytenya. berikut data number of byte:
```
int[] numberOfBytes = {4, 4, 4, 4, 4, 2, 2, 4, 4, 2, 2, 4, 4};
```
Number of byte adalah data yang bertipe array dari integer dengan membernya merupakan jumlah byte masing masing data dari strcture WAV file.
```
 .....
    if (i == 11) {
        subChunk2ID = new String(byteArray) ;
        if(subChunk2ID.compareTo("data") == 0) {
            continue;
        }
        else if( subChunk2ID.compareTo("LIST") == 0) {
            byte[] byteArray2 = new byte[4];
            byteBuffer = ByteArrayToNumber(byteArray2, 4, 1);
            int temp = byteBuffer.getInt();
            //redundant data reading
            byte[] byteArray3 = new byte[temp];
            subChunk2ID = new String(byteArray2) ;
        }
    }
    if (i == 12) {subChunk2Size = byteBuffer.getInt();System.out.println(subChunk2Size);}
}
```
Code di atas adalah bagian untuk index ke 11 dan 12 untuk ekstraksi subChunk2ID dan subChunk2Sizenya. Pada code di atas terdapat method untuk convert byte array ke number. Berikut adalah method yang digunakan:
```
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
```
setelah itu kita finalisasi untuk data extractionnya.
```
.....
byte[] rawData = new byte[(int) file.length()];
mChunkSize = rawData.length;
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
return data;
.....
```
Pada code di atas terdapat raw data yang isinya adalah byte array yang isinya sebanyak ukuran file sample WAV nya. Dan untuk data jadinya itu harus bertipe array float yang data vectornya itu diisi dengan data byte array yang diconvert menjadi float. berilut adalah method untuk convert datanya:
```
public float convertToFloat(byte[] array, int type) {
    ByteBuffer buffer = ByteBuffer.wrap(array);
    if (type == 1){
        buffer.order(LITTLE_ENDIAN);
    }
    return buffer.getShort();
}
```
Code di atas adalah akhir dari proses data extraction
### Step 3
Pada step ini data yang telah diekstrak akan di tulis kembali menjadi WAV file utuh yang sy namakan clean  WAV. Pertamatama kumpulkan data yang telah diekstrak kedalam variable baru untuk menyusun structure WAV file dan menulisnya kembali. Berikut adalah daftar susunan variable untuk menyusun WAV file:
```
long mySubChunk1Size = 16;
int myBitsPerSample= bitsPerSample;
int myFormat = audioFomart;
long myChannels = numChannels;
long mySampleRate = sampleRate;
long myByteRate = mySampleRate * myChannels * myBitsPerSample/8;
int myBlockAlign = (int) (myChannels * myBitsPerSample/8);
byte[] clipData = NumericalArray2ByteArray(wavData);
byte[] rawData = new byte[clipData.length];
long myDataSize = clipData.length;
long myChunk2Size =  myDataSize * myChannels * myBitsPerSample/8;
long fileSize = 36+rawData.length;
```
Pada pengumpulan data, data tersebut harus mengacu pada tabel structure WAV ytang diatas.
- subChunk1Size itu harus 16 untuk PCM
- bitsPersSample itu isinya 8 untuk 8 bit. Berikut code variablenya:
```
short audioFomart, numChannels, blockAlign, bitsPerSample=8;
```
- format itu harus disamakan dengan data yang telah diekstrak yaitu audio format
- channel itu harus disamakan dengan data yang telah diekstrak yaitu number of channel (1 = mono; 2 = stereo)
- sampel rate itu harus disamakan dengan data yang telah diekstrak yitu sample rate seperti (16000,44100, dll)
- byte rate itu hasik kalkulasi dari sample rate * channel * bits per sample / 8
- block align itu hasil kalkulasi dari channel * bits per sample / 8
- clip data itu adalah hasil konversi data dari wav data kedalam numerical byte array. Berikut adalah method untuk koversinya:
```
public byte[] NumericalArray2ByteArray(short[] values){
    ByteBuffer buffer = ByteBuffer.allocate(2 * values.length);
    buffer.order(LITTLE_ENDIAN); // data must be in little endian format
    for (short value : values){
        buffer.putShort(value);
    }
    buffer.rewind();
    return buffer.array();
}
```
Pada code diatas sy tegaskan bahwa data harus dalam little endian format, karna kita mengacu pada structure WAV bahwa data size itu harus dalam little endian. untuk wav data itu sendiri adalah data dari hasil extraction diatas yang bertipe float32 yang telah di convert ke dalam integer 16 (int16). Berilut adalah method untuk mengkonversinya:
```
public static short[] float32ToInt16(float[] arr){
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
```
- raw data itu adalah data mentah yang kakan kita tulis ulang. raw data ini haurs bertipe array byte yang panjangnya itu adalah panjang dari clip data.
- data size itu merupakan ukuran datanya, yaki isinya adalah panjang dari clip data
- chunk2 size itu adalah hasil kalkulasi dari data size * channel * bits per sample / 8
- terakhir adalah file size. file size ini isinya adalah 36 + panjang dari raw data. 36 ini adalah chunk size dari WAV file. untuk mendapatkan angka 36 ini caranya mengkalkulasi rumus dari chunk size WAV file. Berikut adalah rumus untuk mencarinya:
Pertama kita harus mendapatkan jumlah format. Pada structure, format adalah 4 byte
selanjutnya kita harus mencari fmt. berikut adalah rumus untuk mencari fmt:
```
SubChunk1ID:   4  bytes 
SubChunk1Size: 4  bytes 
SubChunk1:     16 bytes
               --------
               24 bytes
```
Jadi fmt itu jumlah nya adalah 24 byte. Selanjutnya jita kalkulasikan untuk mencari chunk sizenya:
```
Format:         4 bytes 
fmt chunk:     24 bytes 
SubChunk2ID:    4 bytes 
SubChunk2Size:  4 bytes
               ---------------------
               36 bytes
```
Nah rumus diatas adalah rumus untuk mendapatkan chunk size (angka 36). Setelah data terkumpuyl, selanjutnya proses penulisan data dengan mengacu pada struxture WAV. Beriklut adalah proses penulisan datanya:
```
outFile.writeBytes("RIFF");                                     // 00 - RIFF
outFile.writeInt(Integer.reverseBytes((int)fileSize));          // 04 - file size
outFile.writeBytes("WAVE");                                     // 08 - WAVE
outFile.writeBytes("fmt ");                                     // 12 - fmt
outFile.writeInt(Integer.reverseBytes((int)mySubChunk1Size));   // 16 - size of this chunk
outFile.writeShort(Short.reverseBytes((short)myFormat));        // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
outFile.writeShort(Short.reverseBytes((short)myChannels));      // 22 - mono or stereo - 1 or 2
outFile.writeInt(Integer.reverseBytes((int)mySampleRate));      // 24 - samples per second (numbers per second)
outFile.writeInt(Integer.reverseBytes((int)myByteRate));        // 28 - bytes per second
outFile.writeShort(Short.reverseBytes((short)myBlockAlign));    // 32 - block align
outFile.writeShort(Short.reverseBytes((short)myBitsPerSample)); // 34 - bits per sample
outFile.writeBytes("data");                                     // 36 - data
outFile.writeInt(Integer.reverseBytes((int)myDataSize));        // 40 - data size
outFile.write(clipData);                                        // 44 - the actual data itself
```
Untuk penulisan datanya itu harus mengacu pada structure WAV dari mulai offest ke 00 sampai offset ke 44. Setelah data ditulis ulang dan disimpan, data tersebut bisa dugunakan (untuk dipredixt pada kasus project ini)
