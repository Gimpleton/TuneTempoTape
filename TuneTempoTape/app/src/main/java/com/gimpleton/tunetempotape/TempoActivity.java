package com.gimpleton.tunetempotape;
/**
 * Activity is a metronome. Allows the user to playback and stop a Click tone at a given BPM.
 * The user can determine the BPM either through a numberpicker(scroll wheel) or by tapping
 * on the "Tap" button which will calculate the users input into a given BPM.
 */

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.example.gimpleton.tunetempotape.R;

import java.lang.reflect.Field;
import java.math.BigDecimal;


public class TempoActivity extends ActionBarActivity {

    private double BPM;
    private MetronomeAsyncTask metroTask;
    private AppPreferences savePref;
    private long initialTime = 0;
    private int bpmTap;

    /**
     * Code originally from:
     * http://stackoverflow.com/questions/22962075/change-the-text-color-of-numberpicker
     * <p/>
     * Needed to change the color of the text in the numberpicker. This method is used as there
     * is no implementation of this in Android currently.
     *
     * @param numberPicker numberPicker object
     * @param color int value of color
     * @return
     */
    private static boolean setNumberPickerTextColor(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);
                    numberPicker.invalidate();
                    return true;
                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                    Log.w("setNumberPickerTextColor", e);
                }
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tempo);

        //Enable user to navigate home through the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get previous used bpm.
        savePref = new AppPreferences(getApplicationContext());
        BPM = savePref.getBpmPref();

        //Initialize new metronome.
        metroTask = new MetronomeAsyncTask();
        metroTask.setBpm(BPM);

        Button play = (Button) findViewById(R.id.PlayTempoButton);
        Button stop = (Button) findViewById(R.id.StopTempoButton);
        Button tap = (Button) findViewById(R.id.TapTempoButton);

        //Setting up numberpicker.
        final NumberPicker numberPickerBpm = (NumberPicker) findViewById(R.id.numberPickerBpm);
        numberPickerBpm.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        numberPickerBpm.setMaxValue(360);
        numberPickerBpm.setMinValue(1);
        numberPickerBpm.setWrapSelectorWheel(true);
        numberPickerBpm.setValue((int) BPM);
        setNumberPickerTextColor(numberPickerBpm, Color.parseColor("#FFFFFF"));

        //Update text to show user current BPM.
        TextView bpmText = (TextView) findViewById(R.id.textBpm);
        bpmText.setText(Integer.toString((int) BPM) + " BPM");

        //Play button
        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /**
                 * Check if metronome is playing.
                 * Get BPM value from numberpicker and assign.
                 * Start metronome thread.
                 */
                if (!metroTask.checkClick() && metroTask.getStatus() != AsyncTask.Status.RUNNING) {
                    BPM = numberPickerBpm.getValue();
                    metroTask.setBpm(BPM);
                    metroTask.execute();
                }
            }
        });
        //Stop button
        stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Stopping the metronome and initializing a new metronome.
                if (metroTask.checkClick()) {
                    metroTask.stop();
                    metroTask = null;
                    metroTask = new MetronomeAsyncTask();
                }
            }
        });
        //Tap button
        tap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Calling method to calculate bpm.
                calcTap();
                //Make sure that calculated BPM is within numberpicker range.
                if (bpmTap > 0 && bpmTap < 361) {
                    BPM = bpmTap;
                    //Updating BPM text to show viewer current BPM.
                    TextView bpmText = (TextView) findViewById(R.id.textBpm);
                    bpmText.setText(Integer.toString((int) BPM) + " BPM");
                    numberPickerBpm.setValue((int) BPM);
                    //Set BPM in current metronome object as it might be playing.
                    if (metroTask.checkClick()) {
                        metroTask.setBpm(BPM);
                    }
                }
            }

        });
        //Numberpicker wheel
        numberPickerBpm.setOnValueChangedListener(new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int
                    oldVal, int newVal) {
                //Updating BPM text to show viewer current BPM
                TextView bpm = (TextView) findViewById(R.id.textBpm);
                bpm.setText(String.valueOf(newVal) + " BPM");
                BPM = numberPickerBpm.getValue();
                //Set BPM in current metronom object as it might be playing.
                metroTask.setBpm(BPM);
            }
        });
    }

    /**
     * Helper method to calculate the time between each time the user presses
     * the tap button. This is then calculated into a BPM value.
     */
    void calcTap() {

        if (initialTime == 0) {
            initialTime = System.nanoTime();
        } else {
            long endTime = System.nanoTime();
            long diff = endTime - initialTime;
            initialTime = endTime;
            //Using BigDecimal in order to be as precise as possible when calculating time between taps.
            BigDecimal longDiff = new BigDecimal(diff);
            BigDecimal seconds = new BigDecimal("1000000000");
            BigDecimal minutes = new BigDecimal("60");
            BigDecimal one = new BigDecimal("1");
            longDiff = longDiff.divide(seconds);
            longDiff = longDiff.divide(minutes, 10, BigDecimal.ROUND_HALF_UP);
            longDiff = one.divide(longDiff, 10, BigDecimal.ROUND_HALF_UP);

            bpmTap = longDiff.intValue();

        }
    }

    //onStop() called when the user exits activity. Saving BPM preference and nullifying metronome.
    @Override
    protected void onStop() {
        super.onStop();
        savePref = new AppPreferences(getApplicationContext());
        savePref.saveBpm((int) BPM);
        metroTask.stop();
        metroTask = null;
    }

    //onResume() called to initialize new metronome.
    @Override
    protected void onResume() {
        super.onResume();
        metroTask = new MetronomeAsyncTask();
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
            settings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void settings() {
        Intent intent = new Intent(TempoActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}
