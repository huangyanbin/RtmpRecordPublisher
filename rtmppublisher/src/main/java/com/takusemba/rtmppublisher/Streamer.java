package com.takusemba.rtmppublisher;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.opengl.EGLContext;
import android.os.Environment;
import android.util.Log;

import java.nio.ByteBuffer;

class Streamer
        implements VideoHandler.OnVideoEncoderStateListener, AudioHandler.OnAudioEncoderStateListener {

    private VideoHandler videoHandler;
    private AudioHandler audioHandler;
    private Muxer muxer = new Muxer();
    private LocalMuxer localMuxer = new LocalMuxer();

     Streamer() {
        this.videoHandler = new VideoHandler();
        this.audioHandler = new AudioHandler();
    }

    void open(String url, String path,int width, int height) {
        muxer.open(url, width, height);
        localMuxer.start(path);
    }

    void startStreaming(EGLContext context, int width, int height, int audioBitrate,
                        int videoBitrate) {

        //if (muxer.isConnected() || localMuxer.isConnected()) {

            long startStreamingAt = System.currentTimeMillis();
            videoHandler.setOnVideoEncoderStateListener(this);
            audioHandler.setOnAudioEncoderStateListener(this);
            videoHandler.start(width, height, videoBitrate, context, startStreamingAt);
            audioHandler.start(audioBitrate, startStreamingAt);
        //}
    }

    void stopStreaming() {
        videoHandler.stop();
        audioHandler.stop();
        muxer.close();
        localMuxer.release();
    }

    boolean isStreaming() {
        return muxer.isConnected() || localMuxer.isConnected();
    }

    @Override
    public void onPrepareVideo(MediaFormat format) {
        localMuxer.addVideoTrack(format);
    }

    @Override
    public void onVideoDataEncoded(byte[] data, int size, int timestamp) {
        muxer.sendVideo(data, size, timestamp);
    }

    @Override
    public void onVideoDataMediaEncoded(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        localMuxer.putData(LocalMuxer.VIDEO,buffer,bufferInfo);
    }

    @Override
    public void onAudioDataEncoded(byte[] data, int size, int timestamp) {
        muxer.sendAudio(data, size, timestamp);
    }

    @Override
    public void onPrepareAudio(MediaFormat format) {
        localMuxer.addAudioTrack(format);
    }

    @Override
    public void onAudioDataMediaEncoded(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        localMuxer.putData(LocalMuxer.AUDIO,buffer,bufferInfo);
    }

    CameraSurfaceRenderer.OnRendererStateChangedListener getVideoHandlerListener() {
        return videoHandler;
    }

    void setMuxerListener(PublisherListener listener) {
        muxer.setOnMuxerStateListener(listener);
        localMuxer.setOnMuxerStateListener(listener);
    }

}
