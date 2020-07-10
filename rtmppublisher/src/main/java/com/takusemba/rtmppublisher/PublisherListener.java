package com.takusemba.rtmppublisher;

public interface PublisherListener {

    /**
     * Called when {@link Publisher} started publishing
     */
    void onRemoteStarted();

    /**
     * Called when {@link Publisher} stopped publishing
     */
    void onRemoteStopped();

    /**
     * Called when stream is disconnected
     */
    void onDisconnected();

    /**
     * Called when failed to connect
     */
    void onFailedToConnect();


    /**
     * Called when {@link Publisher} started publishing
     */
    void onLocalStarted();

    /**
     * Called when {@link Publisher} stopped publishing
     */
    void onLocalStopped();

}
