package com.gimpleton.tunetempotape;
/**
 * Settings screen which allows the user to set the maximum file size when recording
 * audio. Also contains some info for the user about the app.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.gimpleton.tunetempotape.R;


public class SettingsActivity extends ActionBarActivity {
    private AppPreferences savePref;
    private int newFileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //Enabling the action bar to let the user navigate home.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final SeekBar fileSizeBar = (SeekBar) findViewById(R.id.seekBarFileSize);
        fileSizeBar.setMax(500);
        //Getting the prefered file size.
        savePref = new AppPreferences(getApplicationContext());
        newFileSize = savePref.getSizePref();
        final TextView fileSizeText = (TextView) findViewById(R.id.fileSize);
        //Setting the text on screen to show user previous prefered file size.
        fileSizeText.setText(String.valueOf(newFileSize + " mb"));
        fileSizeBar.setProgress(newFileSize);
        //Seekbar to enable the user to change prefered file size.
        fileSizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress >= 1 && progress <= fileSizeBar.getMax()) {

                        String progressString = String.valueOf(progress);
                        fileSizeText.setText(progressString + " mb"); // the TextView Reference
                        seekBar.setSecondaryProgress(progress);
                        newFileSize = progress;
                    }
                }

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        //Saving the new prefered file size.
        Log.v("onstop", String.valueOf(newFileSize));
        savePref.saveSize(newFileSize);
    }

    protected void onPause() {
        super.onPause();
        Log.v("onpause", String.valueOf(newFileSize));
        savePref.saveSize(newFileSize);
    }

    //Setting up actionbar so that user can reach settings activity.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            savePref.saveSize(newFileSize);
            settings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void settings() {
        Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}
