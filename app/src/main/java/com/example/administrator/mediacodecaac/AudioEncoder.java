package com.example.administrator.mediacodecaac;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
/*
* 功能：AAC ADTS 编码
* author：jiangc
* time:2018/5/29
* */
public class AudioEncoder implements audioRead.audioReader{
    private String TAG = "AudioEncoder";
    private Context mContext;
    private MediaCodec mEncodeer;                 //编码器
    private String mType;                         //mime
    private MediaFormat mMediaFormat;            //格式
    private int mSampleRate;                     //采样率
    private int mChannelCount;                   //声道数
    private MediaCodec.BufferInfo mBufferInfo;
    public static boolean EncoderFlag = false;
    private byte[] mFrameByte;
    private FileOutputStream fileOutputStream;

    public AudioEncoder(Context context, String Type, int sampleRate, int channelCount)
    {
        this.mContext = context;
        this.mType = Type;
        this.mSampleRate = sampleRate;
        this.mChannelCount = channelCount;
        try {
            fileOutputStream = new FileOutputStream(new File("/sdcard/test.aac"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    /*初始化编码器*/
    private boolean initEncoder()
    {
        mBufferInfo = new MediaCodec.BufferInfo();
        try {
            mEncodeer = MediaCodec.createEncoderByType(mType);
        } catch (IOException e) {
            Log.e(TAG, "AudioEncoder: " +  "编码器创建失败");
            e.printStackTrace();
            return false;
        }
        mMediaFormat = MediaFormat.createAudioFormat(mType, mSampleRate, mChannelCount);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 64000);
        mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectERLC);
        mEncodeer.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return true;
    }

    /*开始编码*/
    public void startEncoder()
    {
        initEncoder();
        EncoderFlag = true;

        mEncodeer.start();

        new Thread()
        {
            @Override
            public void run() {
                while(EncoderFlag)
                {
                    int outputBufferIndex = mEncodeer.dequeueOutputBuffer(mBufferInfo, 100);
                    if (outputBufferIndex >= 0)
                    {
                        ByteBuffer outPutBuffer = mEncodeer.getOutputBuffer(outputBufferIndex);
                        int length = mBufferInfo.size + 7;
                        if (mFrameByte == null || mFrameByte.length < length)
                        {
                            mFrameByte = new byte[length];
                        }
                        addADTStoPacket(mFrameByte, length);  //添加ADTS头
                        outPutBuffer.get(mFrameByte, 7, mBufferInfo.size); //获得编码后的数据
                        //
                        try {
                            fileOutputStream.write(mFrameByte);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mEncodeer.releaseOutputBuffer(outputBufferIndex, false);
                    }
                }
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /*停止编码*/
    public void stopEncoder()
    {
        EncoderFlag = false;
        mEncodeer.release();
    }

    /*获得PCM*/
    @Override
    public void getPCMData(byte[] bytes) {
        int status = mEncodeer.dequeueInputBuffer(100000);
        if (status == MediaCodec.INFO_TRY_AGAIN_LATER)
        {

        }else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
        {

        }else if (status >= 0)
        {
            ByteBuffer inputBuffer = mEncodeer.getInputBuffer(status);
            inputBuffer.clear();
            inputBuffer.put(bytes);
            inputBuffer.limit(bytes.length);
            mEncodeer.queueInputBuffer(status, 0, bytes.length, System.nanoTime(), 0);
        }
    }

    /**
     * 添加ADTS头
     *
     * @param packet
     * @param packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 3; // 48KHz
        int chanCfg = 2; // CPE

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    public audioRead.audioReader getCallBack()
    {
        return this;
    }
}
