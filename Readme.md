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
#### Step 3
