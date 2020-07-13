package com.takusemba.rtmppublisher;


import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

public class RtmpPublisher implements Publisher, SurfaceTexture.OnFrameAvailableListener,
        CameraSurfaceRenderer.OnRendererStateChangedListener, LifecycleObserver {


    private GLSurfaceView glView;
    private CameraSurfaceRenderer renderer;
    private CameraClient camera;
    private Streamer streamer;

    private String url;
    private int width;
    private int height;
    private int audioBitrate;
    private int videoBitrate;
    private String path;
    private boolean isPause =true;
    private int frameRate;

    RtmpPublisher(AppCompatActivity activity,
                  GLSurfaceView glView,
                  String url,
                  String path,
                  int width,
                  int height,
                  int audioBitrate,
                  int videoBitrate,
                  int frameRate,
                  CameraMode mode,
                  PublisherListener listener) {

        activity.getLifecycle().addObserver(this);

        this.glView = glView;
        this.url = url;
        this.width = width;
        this.height = height;
        this.audioBitrate = audioBitrate;
        this.videoBitrate = videoBitrate;
        this.path = path;
        this.frameRate = frameRate;
        this.camera = new CameraClient(activity, mode);
        this.streamer = new Streamer(frameRate);
        this.streamer.setMuxerListener(listener);
        glView.setEGLContextClientVersion(2);
        renderer = new CameraSurfaceRenderer();
        renderer.addOnRendererStateChangedLister(streamer.getVideoHandlerListener());
        renderer.addOnRendererStateChangedLister(this);

        glView.setRenderer(renderer);
        glView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    @Override
    public void switchCamera() {
        camera.swap();
    }

    @Override
    public void startPublishing() {
        streamer.open(url,path, width, height);
        glView.postDelayed(new Runnable() {
            @Override
            public void run() {
                glView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        // EGL14.eglGetCurrentContext() should be called from glView thread.
                        final EGLContext context = EGL14.eglGetCurrentContext();
                        glView.post(new Runnable() {
                            @Override
                            public void run() {
                                // back to main thread

                                streamer.startStreaming(context, width, height, audioBitrate, videoBitrate,frameRate);
                            }
                        });
                    }
                });
            }
        },100);

    }

    @Override
    public void stopPublishing() {
        if (streamer.isStreaming()) {
            streamer.stopStreaming();
        }
    }

    @Override
    public boolean isPublishing() {
        return streamer.isStreaming();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume(LifecycleOwner owner) {
        Log.e("huang","onResume");
        if(isPause) {
            isPause = false;
            Camera.Parameters params = camera.open();
            final Camera.Size size = params.getPreviewSize();
            glView.onResume();
            glView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    renderer.setCameraPreviewSize(size.width, size.height);
                }
            });
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(LifecycleOwner owner) {
        Log.e("huang","onStop");
        if(!isPause) {
            if (camera != null) {
                camera.close();
            }
            isPause = true;
            glView.onPause();
            glView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    renderer.pause();
                }
            });
            if (streamer.isStreaming()) {
                streamer.stopStreaming();
            }
        }
    }

    @Override
    public void onSurfaceCreated(SurfaceTexture surfaceTexture) {
        surfaceTexture.setOnFrameAvailableListener(this);
        camera.startPreview(surfaceTexture);
    }

    @Override
    public void onFrameDrawn(int textureId, float[] transform, long timestamp) {
        // no-op
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glView.requestRender();
    }
}
