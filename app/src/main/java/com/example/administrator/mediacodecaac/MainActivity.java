package com.example.administrator.mediacodecaac;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    public int AudioSource;      //音频采集的输入源
    public int sampleRateInHz;    //采样率
    public int channelConfig;     //通道数的配置
    public int audioFormat;       //数据位宽
    public int mMinBufferSize;    //缓缓缓还 缓  缓 冲区大小
    private int Fram;               //fps
    private int mMaxBufferSize;
    private int framBufferSize;
    private AudioRecord audioRecord;    //音频采集对象
    private audioRead audioread;
    AudioEncoder audioEncoder;
    private Thread thread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AudioSource = MediaRecorder.AudioSource.MIC;
        sampleRateInHz = 48000;
        Fram = 50;
        channelConfig = 2;
        audioFormat = 16/8;
        mMinBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, AudioFormat.CHANNEL_IN_STEREO ,AudioFormat.ENCODING_PCM_16BIT);
        mMaxBufferSize = sampleRateInHz * audioFormat * channelConfig * 1;
        framBufferSize = mMaxBufferSize / Fram;
        //mMinBufferSize = sampleRateInHz * audioFormat * 1 * channelConfig * 2 ; //缓冲区大小，采样率 x 量化位数 x 采样时间 x 通道数
        //audioRecord = new AudioRecord(AudioSource, sampleRateInHz, AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT, mMinBufferSize);
        audioRecord = new AudioRecord(AudioSource, sampleRateInHz, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, framBufferSize);
        audioEncoder = new AudioEncoder(this, MediaFormat.MIMETYPE_AUDIO_AAC,sampleRateInHz, channelConfig);
        audioread = new audioRead(audioRecord, framBufferSize, sampleRateInHz, audioFormat, channelConfig, audioEncoder.getCallBack());
        audioEncoder.startEncoder();
        thread = new Thread(audioread);
        audioRecord.startRecording();
        audioread.flag(1);
        thread.start();
    }

    @Override
    protected void onDestroy() {
        audioEncoder.stopEncoder();
        super.onDestroy();
    }
}
