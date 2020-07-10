package com.takusemba.rtmppublisher;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

public class MuxerData {

    public int track;
    public ByteBuffer buffer;
    public MediaCodec.BufferInfo bufferInfo;

    public MuxerData(int track, ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        this.track = track;
        this.buffer = buffer;
        this.bufferInfo = bufferInfo;
    }
}
