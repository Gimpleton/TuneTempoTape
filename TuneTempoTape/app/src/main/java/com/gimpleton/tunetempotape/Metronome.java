package com.gimpleton.tunetempotape;

/**
 * Class originally found here:
 * https://github.com/MasterEx/BeatKeeper/blob/master/src/pntanasis/android/metronome/Metronome.java
 *
 * Some code has been edited and removed.
 */

class Metronome {

    private final int tick = 250; // samples of tick
    private double bpm;
    private int beat;
    private int silence;
    private boolean play = false;

    private final AudioGenerator audioGenerator = new AudioGenerator(8000);

    public Metronome() {
        audioGenerator.createPlayer();
    }

    public void calcSilence() {
        silence = (int) (((60 / bpm) * 8000) - tick);
    }

    public void play() {
        calcSilence();
        play = true;
        double beatSound = 1000;
        double[] tick =
                audioGenerator.getSineWave(this.tick, 8000, beatSound);
        double silence = 0;
        double[] sound = new double[8000];
        int t = 0, s = 0, b = 0;
        do {
            for (int i = 0; i < sound.length && play; i++) {
                if (t < this.tick) {
                    sound[i] = tick[t];
                    t++;
                } else {
                    sound[i] = silence;
                    s++;
                    if (s >= this.silence) {
                        t = 0;
                        s = 0;
                        b++;
                        if (b > (this.beat - 1))
                            b = 0;
                    }
                }
            }
            audioGenerator.writeSound(sound);
        } while (play);
    }

    public void stop() {
        play = false;
        audioGenerator.destroyAudioTrack();

    }

    public void setBpm(double bpm) {
        this.bpm = bpm;
    }

    public boolean checkClickPlay() {
        return play;
    }
}