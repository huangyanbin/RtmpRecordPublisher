package com.takusemba.rtmppublisher;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.nio.ByteBuffer;

class AudioRecorder {

    private AudioRecord audioRecord;
    private final int sampleRate;
    private OnAudioRecorderStateChangedListener listener;

    interface OnAudioRecorderStateChangedListener {
        void onAudioRecorded(byte[] data, int length);

    }

    void setOnAudioRecorderStateChangedListener(OnAudioRecorderStateChangedListener listener) {
        this.listener = listener;
    }

    AudioRecorder(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void start() {
        final int bufferSize =
                AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        audioRecord.startRecording();
        Log.e("huang","AudioRecorder start");
        HandlerThread handlerThread = new HandlerThread("AudioRecorder-record");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                int bufferReadResult;
                byte[] data = new byte[bufferSize];
                //Log.e("huang","AudioRecorder run");
                // keep running... so use a different thread.
                while (isRecording()&& (bufferReadResult = audioRecord.read(data, 0, bufferSize)) > 0) {
                    //Log.e("huang", "AudioRecorder run loop");
                  listener.onAudioRecorded(data, bufferReadResult);
                }
            }
        });
    }

    void stop() {
        Log.e("huang","AudioRecorder stop");
        if (isRecording()) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

    boolean isRecording() {
        return audioRecord != null
                && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
    }
}
