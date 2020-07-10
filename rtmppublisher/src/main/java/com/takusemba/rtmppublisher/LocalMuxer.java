package com.takusemba.rtmppublisher;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public class LocalMuxer {

    private MediaMuxer muxer;
    //用于标记是否已经添加视频轨道
    private boolean mVideoTrackReady;
    //用于标记是否已经添加音频轨道
    private boolean mAudioTrackReady;
    //视频轨道
    private  int videoTrackIndex = -1;
    //音频轨道
    private int audioTrackIndex = -1;
    //是否开始
    private boolean mStart;
    //是否使用本地录制
    private boolean isUse = true;
    private final Object lock = new Object();
    //缓存
    private LinkedBlockingQueue<MuxerData> muxerDatas = new LinkedBlockingQueue<>();
    //工作线程 堵塞从队列获取
    private Thread workThread;
    //是否循环
    private volatile boolean loop;

    //视频类型
    public static final int VIDEO = 1;
    //音频类型
    public static final int AUDIO = 2;


    @IntDef({VIDEO, AUDIO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MuxerType {

    }
    private PublisherListener listener;
    final Handler uiHandler = new Handler(Looper.getMainLooper());

  public boolean start(String path){
         isUse = !TextUtils.isEmpty(path);
        if(isUse) {
            Log.e("huang","LocalMuxer start");
            mVideoTrackReady = false;
            mAudioTrackReady = false;
            mStart = false;
            File file = new File(path);
            file.deleteOnExit();
            try {
                muxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                loopData();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
       return false;
   }

   private void loopData(){
       if (loop) {
           throw new RuntimeException("====LocalMuxer线程已经启动===");
       }
       workThread = new Thread("LocalMuxer-thread") {
           @Override
           public void run() {
               //混合器未开启
               synchronized (lock) {
                   try {
                       Log.d("LocalMuxer", "=====媒体混合器等待开启...");
                       lock.wait();
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               }
               while (loop) {
                   try {
                       MuxerData data = muxerDatas.take();
                       int track = -1;
                       if (data.track == VIDEO) {
                           track = videoTrackIndex;
                       } else if(data.track == AUDIO){
                           track = audioTrackIndex;
                       }
                       Log.d("LocalMuxer", "====track: "+track+"    写入混合数据大小 " + data.bufferInfo.size);
                       //添加数据

                       muxer.writeSampleData(track, data.buffer, data.bufferInfo);
                   } catch (Exception e) {
                       Log.e("LocalMuxer", "====写入混合数据失败!" + e.toString());
                       e.printStackTrace();
                   }
               }
               muxerDatas.clear();
               stopMediaMuxer();
               Log.d("LocalMuxer", "====媒体混合器退出...");
           }
       };
       loop = true;
       workThread.start();
   }

   private void ready(){
       synchronized (lock) {
           if (mVideoTrackReady && mAudioTrackReady) {
               Log.e("LocalMuxer", "ready");
               muxer.start();
               mStart = true;
               if(listener != null){
                   uiHandler.post(new Runnable() {
                       @Override
                       public void run() {
                           listener.onLocalStarted();
                       }
                   });

               }
               lock.notify();
           }
       }

   }

   public void addVideoTrack(MediaFormat format){

        if(isUse) {
            try {
                videoTrackIndex = muxer.addTrack(format);
                Log.e("LocalMuxer","addVideoTrack"+ videoTrackIndex);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            mVideoTrackReady = true;
            ready();
        }
   }

    public void addAudioTrack(MediaFormat format){

        if(isUse) {
            try {
                audioTrackIndex = muxer.addTrack(format);
                Log.e("LocalMuxer","addAudioTrack"+ audioTrackIndex);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            mAudioTrackReady = true;
            ready();
        }
    }

    /**
     * 写入数据
     * @param track
     * @param buffer
     * @param bufferInfo
     */
    public void putData(@MuxerType int track, ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo){
        if(isUse && mStart) {
            try {
                bufferInfo.presentationTimeUs = System.nanoTime() / 1000;
                muxerDatas.put(new MuxerData(track,buffer,bufferInfo));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopMediaMuxer() {
        if (isUse && mStart) {
            try {
                muxer.stop();
                muxer.release();
                Log.d("LocalMuxer", "====停止媒体混合器=====");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        isUse = false;
        mStart = false;
    }


    /**
     * 释放
     */
    public void release() {

        if(isUse && mStart) {
            loop  = false;
            Log.e("huang","release");
            if(listener != null){
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onRemoteStopped();
                    }
                });

            }
        }
    }

    public boolean isConnected(){
        return mStart;
    }


    void setOnMuxerStateListener(PublisherListener listener) {
        this.listener = listener;
    }
}
