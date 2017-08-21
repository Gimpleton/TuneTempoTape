package com.gimpleton.tunetempotape;

/**
 * Class originally found here:
 * http://nathanbelue.blogspot.com.tr/2013/01/playing-pitch-in-android-app-with.html
 *
 * Some code has been edited and added
 */

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

class PitchPlayer {


   /*
    * PRIVATE DATA
    */

    // some constants
    private final int sampleRate = 44100;
    private final int minFrequency = 130;
    private final int bufferSize = sampleRate / minFrequency;
    private AudioTrack mAudio;
    private int mSampleCount;
    private final float volume = 0.1f;

    /*
    * PUBLIC METHODS
    */
    // Constructor
    public PitchPlayer() {
        mAudio = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_8BIT,
                bufferSize,
                AudioTrack.MODE_STATIC
        );
        /**
         * Setting the volume of the produced tone and checking current API
         * in order to use appropriate method to use.
         */
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion < android.os.Build.VERSION_CODES.LOLLIPOP) {
            mAudio.setStereoVolume(volume, volume);
        } else {
            setApiVolume();
        }

    }

    /**
     * If the current API is 21 (LOLLIPOP) then this method is
     * used.
     */
    @TargetApi(21)
    void setApiVolume() {
        mAudio.setVolume(volume);
    }

    // Set the frequency
    void setFrequency(double frequency) {
        int x = (int) ((double) bufferSize * frequency / sampleRate); // added
        mSampleCount = (int) ((double) x * sampleRate / frequency); // added

        byte[] samples = new byte[mSampleCount]; // changed from bufferSize

        for (int i = 0; i != mSampleCount; ++i) { // changed from bufferSize
            double t = (double) i * (1.0 / sampleRate);
            double f = Math.sin(t * 2 * Math.PI * frequency);
            samples[i] = (byte) (f * 127);
        }
        mAudio.write(samples, 0, mSampleCount); // changed from bufferSize
    }

    /**
     * Plays the tone and loops it until stop() is called.
     */
    void start() {
        if (mAudio != null) {
            mAudio.reloadStaticData();
            mAudio.setLoopPoints(0, mSampleCount, -1);
            mAudio.play();
        }
    }

    /**
     * Stops the tone from playing.
     */
    public void stop() {
        int checkPlay = mAudio.getPlayState();
        if (checkPlay == 3) {
            mAudio.stop();
            mAudio.flush();
            mAudio.release();
            mAudio = null;
        }
    }

    /**
     * Helper method that can be called to set the frequency of tone and play it.
     *
     * @param freq Frequency of tone.
     */
    public void audioHandler(double freq) {
        setFrequency(freq);
        int checkState = mAudio.getState();
        if (checkState == 1) {
            start();
        }
    }

}