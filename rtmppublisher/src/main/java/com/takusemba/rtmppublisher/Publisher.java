package com.takusemba.rtmppublisher;

import android.opengl.GLSurfaceView;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public interface Publisher {

    /**
     * switch camera mode between {@link CameraMode#FRONT} and {@link CameraMode#BACK}
     */
    void switchCamera();

    /**
     * start publishing video and audio data
     */
    void startPublishing();

    /**
     * stop publishing video and audio data.
     */
    void stopPublishing();

    /**
     * @return if the Publisher is publishing data.
     */
    boolean isPublishing();


    class Builder {

        /**
         * Default Values
         */
        public static final int DEFAULT_WIDTH = 720;
        public static final int DEFAULT_HEIGHT = 1280;
        public static final int DEFAULT_AUDIO_BITRATE = 12800;
        public static final int DEFAULT_VIDEO_BITRATE = 3*1280*720;
        public static final CameraMode DEFAULT_MODE = CameraMode.FRONT;
        public static final int FRAME_RATE = 30;

        /**
         * Required Parameters
         */
        private AppCompatActivity activity;
        private GLSurfaceView glView;
        private String url;

        /**
         * Optional Parameters
         */
        private CameraMode mode;
        private int width;
        private int height;
        private int audioBitrate;
        private int videoBitrate;
        private String path;
        private PublisherListener listener;
        private int frameRate;


        /**
         * Constructor of the {@link Builder}
         */
        public Builder(@NonNull AppCompatActivity activity) {
            this.activity = activity;
        }

        /**
         * Set the GLSurfaceView used for preview.
         * this parameter is required
         */
        public Builder setGlView(@NonNull GLSurfaceView glView) {
            this.glView = glView;
            return this;
        }

        /**
         * Set the RTMP url
         * this parameter is required
         */
        public Builder setUrl(@NonNull String url) {
            this.url = url;
            return this;
        }

        /**
         *设置本地录制视频路径
         */
        public Builder setPath(@NonNull String localPath) {
            Log.e("huang","localPath"+localPath);
            this.path = localPath;
            return this;
        }
        /**
         * Set the size of video stream.
         * these parameters are optional
         */
        public Builder setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * 设置视频帧率
         */
        public Builder setFrameRate(int frameRate) {
            this.frameRate = frameRate;
            return this;
        }

        /**
         * Set the audio bitrate used for RTMP Streaming
         * this parameter is optional
         */
        public Builder setAudioBitrate(int audioBitrate) {
            this.audioBitrate = audioBitrate;
            return this;
        }

        /**
         * Set the video bitrate used for RTMP Streaming
         * this parameter is optional
         */
        public Builder setVideoBitrate(int videoBitrate) {
            this.videoBitrate = videoBitrate;
            return this;
        }

        /**
         * Set the {@link CameraMode}
         * this parameter is optional
         */
        public Builder setCameraMode(@NonNull CameraMode mode) {
            this.mode = mode;
            return this;
        }

        /**
         * Set the {@link PublisherListener}
         * this parameter is optional
         */
        public Builder setListener(PublisherListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * @return the created RtmpPublisher
         */
        public RtmpPublisher build() {
            if (activity == null) {
                throw new IllegalStateException("activity should not be null");
            }
            if (glView == null) {
                throw new IllegalStateException("GLSurfaceView should not be null");
            }
            if (TextUtils.isEmpty(url) && TextUtils.isEmpty(path)) {
                throw new IllegalStateException("url or path should not be null");
            }
            if (height <= 0) {
                height = DEFAULT_HEIGHT;
            }
            if (width <= 0) {
                width = DEFAULT_WIDTH;
            }
            if (audioBitrate <= 0) {
                audioBitrate = DEFAULT_AUDIO_BITRATE;
            }
            if (videoBitrate <= 0) {
                videoBitrate = DEFAULT_VIDEO_BITRATE;
            }
            if(frameRate <= 0){
                frameRate = FRAME_RATE;
            }
            if (mode == null) {
                mode = DEFAULT_MODE;
            }
            return new RtmpPublisher(activity, glView, url, path,width, height, audioBitrate, videoBitrate,frameRate, mode, listener);
        }

    }
}
