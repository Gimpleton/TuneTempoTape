package com.gimpleton.tunetempotape;

/**
 * Simple class that defines a note.
 */

class Note {
    private final double freq;
    private final int note;

    public Note(int note, double freq) {
        this.freq = freq;
        this.note = note;
    }

    public double getFreq() {
        return freq;
    }

    public int getNote() {
        return note;
    }

}
