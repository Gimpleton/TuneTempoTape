package com.gimpleton.tunetempotape;

/**
 * Class that helps with saving and fetching the users shared preferences.
 * In this case the BPM and maximum file size for recording.
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

class AppPreferences {
    private static final String KEY_PREFS_BPM = "BPM";
    private static final String KEY_PREFS_SIZE = "SIZE";
    private static final String APP_SHARED_PREFS = AppPreferences.class.getSimpleName(); //  Name of the file -.xml
    private static final int DEFAULT_BPM = 120;
    private static final int DEFAULT_SIZE = 100;
    private final SharedPreferences sharedPrefs;
    private final SharedPreferences.Editor prefsEditor;

    public AppPreferences(Context context) {
        this.sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = sharedPrefs.edit();
        prefsEditor.apply();
    }

    public int getBpmPref() {
        return sharedPrefs.getInt(KEY_PREFS_BPM, DEFAULT_BPM);
    }

    public void saveBpm(int bpm) {
        prefsEditor.putInt(KEY_PREFS_BPM, bpm);
        prefsEditor.commit();
    }

    public int getSizePref() {
        return sharedPrefs.getInt(KEY_PREFS_SIZE, DEFAULT_SIZE);
    }

    public void saveSize(int size) {
        prefsEditor.putInt(KEY_PREFS_SIZE, size);
        prefsEditor.commit();
    }
}
