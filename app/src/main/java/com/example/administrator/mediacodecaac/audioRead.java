package com.example.administrator.mediacodecaac;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by jiangc on 17-5-23.
 */
public class audioRead implements Runnable {
    private String TAG = "audioRead";
    private int flag = 0;
    private AudioRecord audioRecord;
    private int sampleRateInHz;
    private int mBuffSize;
    private int audioFormat;
    private int channelConfig;
    private int bufferSizeInBytes;
    private AudioTrack mAudioTrack;
    private int outchannels = 2;
    private audioReader AudioReaderCallbak;

    public audioRead(AudioRecord audioRecord, int framBufferSize, int sampleRateInHz, int audioFormat, int channelConfig, audioReader callbak) {
        this.audioRecord = audioRecord;
        this.mBuffSize = framBufferSize;
        this.sampleRateInHz = sampleRateInHz;
        this.audioFormat = audioFormat;
        this.channelConfig = channelConfig;
        //bufferSizeInBytes = 48000 * 16 * 1 * 2 * 2; //缓冲区大小，采样率 x 量化位数 x 采样时间 x 通道数
        bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes, AudioTrack.MODE_STREAM);
        AudioReaderCallbak = callbak;
    }

    public void outputchannels(int outchannels)
    {
        this.outchannels = outchannels;
    }
    public void flag(int num)
    {
        flag = num;
    }
    @Override
    public void run() {
        int res;
        int cont;
        int lSts;
        int lRet;
        short [] enshort;
        byte [] debyte;

        res = 0;
        cont = 0;
        lSts = 0;
        lRet = 0;
        System.out.println("mBuffSize----->" + mBuffSize);
        byte [] buffer = new byte[mBuffSize];
        enshort = new short[mBuffSize];
        debyte = new byte[mBuffSize];
        while(flag == 1) {
            Log.e(TAG, "run: aldfaldkfaldf");
            res = audioRecord.read(buffer, 0, mBuffSize);
            AudioReaderCallbak.getPCMData(buffer);
            mAudioTrack.write(buffer, 0, buffer.length);
            mAudioTrack.play();
        }
    }
    /*用来获取PCM数据*/
    public interface audioReader{
        public void getPCMData(byte[] bytes);
    }
}
