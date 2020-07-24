// IPlaybackService.aidl
package com.jesusmoreira.materialmusic.services;

// AIDL file: Defines interfaces for communicating between activities and your service.
interface IPlaybackService {
    void stop();
    void play();
    void pause();
    boolean openFile(String path);
    long getDuration();
    long getPosition();
    void seek(long pos);
    boolean isPlaying();
}