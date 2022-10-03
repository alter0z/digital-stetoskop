Android app that classify heart valve disease with digital stethoscope device

# WAV File Documentation
berikut adalah dokumnetasi untuk membuat clean WAV file.
## Structur WAV
Order  | Offset | Name | Size (bytes) | Data Type | Expected value
------------ | ------------- | --------- | --------------- | ----------------| --------
big | 0 | ChunkID | 4 | String |  "RIFF"
little | 4 | ChunkSize | 4 | int | Denotes the size of the rest of the chunk
big | 8 | Format | 4 | String | "WAVE"
big | 12 | SubChunk1ID | 4 | String | "fmt " (there is a space)
little | 16 | SubChunk1Size | 4 | int | 16 for PCM
little | 20 | AudioFormat | 2 | short | 1
little | 22 | NumChannels | 2 | short | 1 for mono, 2 for stereo
little | 24 | SampleRate | 4 | int | 16000, 44100, etc.
little | 28 | ByteRate | 4 | int | SampleRate * NumChannels * BitsPerSample/8
little | 32 | BlockAlign | 2 | short | NumChannels * BitsPerSample/8
little | 34 | BitsPerSample | 2 | short | 8 bits = 8, 16 bits = 16, etc
big | 36 | SubChunk2ID | 4 | string | "data"
little | 40 | SubChunk2Size | 4 | int | NumSamples * NumChannels * BitsPerSample/8
little | 44 | data | | | the actual data


