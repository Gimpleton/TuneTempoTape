package com.gimpleton.tunetempotape;

/**
 * Class originally found here:
 * https://github.com/MasterEx/BeatKeeper/blob/master/src/pntanasis/android/metronome/AudioGenerator.java
 * This class generates the audio used for the metronome class.
 */

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

class AudioGenerator {

    private final int sampleRate;
    private AudioTrack audioTrack;

    public AudioGenerator(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    /**
     * @param samples         duration of the generated sine wave
     * @param sampleRate      quality of generated sine wave
     * @param frequencyOfTone the frequency of tone
     * @return an array of doubles that contains the generated sine wave
     */
    public double[] getSineWave(int samples, int sampleRate, double frequencyOfTone) {
        double[] sample = new double[samples];
        for (int i = 0; i < samples; i++) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / frequencyOfTone));
        }
        return sample;
    }

    /**
     * @param samples array of doubles produced by getSineWave.
     * @return 16 bit PCM byte array that has been encoded from the param samples.
     */
    byte[] get16BitPcm(double[] samples) {
        byte[] generatedSound = new byte[2 * samples.length];
        int index = 0;
        for (double sample : samples) {
            // scale to maximum amplitude
            short maxSample = (short) ((sample * Short.MAX_VALUE));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSound[index++] = (byte) (maxSample & 0x00ff);
            generatedSound[index++] = (byte) ((maxSample & 0xff00) >>> 8);

        }
        return generatedSound;
    }

    public void createPlayer() {
        //FIXME sometimes audioTrack isn't initialized
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, sampleRate,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
    }

    public void writeSound(double[] samples) {
        byte[] generatedSnd = get16BitPcm(samples);

        audioTrack.write(generatedSnd, 0, generatedSnd.length);
    }

    public void destroyAudioTrack() {
        audioTrack.pause();
        audioTrack.stop();
        audioTrack.release();
    }

}

