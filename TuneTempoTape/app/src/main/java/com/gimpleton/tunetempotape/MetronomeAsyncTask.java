package com.gimpleton.tunetempotape;

import android.os.AsyncTask;

/**
 * Class originally found here from line 283:
 * https://github.com/MasterEx/BeatKeeper/blob/master/src/pntanasis/android/metronome/MetronomeActivity.java
 * <p/>
 * Some code has been edited and removed from the original.
 * Creates a new thread for the metronome to play.
 */

class MetronomeAsyncTask extends AsyncTask<Void, Void, String> {
    private Metronome metronome;

    MetronomeAsyncTask() {
        metronome = new Metronome();
    }

    protected String doInBackground(Void... params) {

        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        metronome.play();
        return null;
    }

    public void stop() {
        metronome.stop();
        metronome = null;
    }

    public void setBpm(double bpm) {
        metronome.setBpm(bpm);
        metronome.calcSilence();
    }

    /**
     * Checks if the metronome is currently playing.
     *
     * @return boolean
     */

    public boolean checkClick() {
        return metronome.checkClickPlay();
    }

}