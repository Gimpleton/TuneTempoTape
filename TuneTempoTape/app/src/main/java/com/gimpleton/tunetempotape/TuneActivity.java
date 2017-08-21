package com.gimpleton.tunetempotape;
/**
 * This activity allows the user to playback a tone of their choice in a chromatic scale in three
 * different octaves.
 */

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
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

public class TuneActivity extends ActionBarActivity implements View.OnClickListener {
    private static final int[] BUTTON_ID = {
            R.id.CButton,
            R.id.CSharpButton,
            R.id.DButton,
            R.id.DSharpButton,
            R.id.EButton,
            R.id.FButton,
            R.id.FSharpButton,
            R.id.GButton,
            R.id.GSharpButton,
            R.id.AButton,
            R.id.ASharpButton,
            R.id.BButton,

    };
    private static final Note[] OCT_ONE = {
            new Note(R.id.CButton, 130.81),
            new Note(R.id.CSharpButton, 138.59),
            new Note(R.id.DButton, 146.83),
            new Note(R.id.DSharpButton, 155.56),
            new Note(R.id.EButton, 164.81),
            new Note(R.id.FButton, 174.61),
            new Note(R.id.FSharpButton, 184.99),
            new Note(R.id.GButton, 195.99),
            new Note(R.id.GSharpButton, 208),
            new Note(R.id.AButton, 220),
            new Note(R.id.ASharpButton, 233),
            new Note(R.id.BButton, 246.94),
    };
    private static final Note[] OCT_TWO = {
            new Note(R.id.CButton, 261.62),
            new Note(R.id.CSharpButton, 277.18),
            new Note(R.id.DButton, 293.66),
            new Note(R.id.DSharpButton, 311.12),
            new Note(R.id.EButton, 329.62),
            new Note(R.id.FButton, 349.22),
            new Note(R.id.FSharpButton, 369.99),
            new Note(R.id.GButton, 391.99),
            new Note(R.id.GSharpButton, 415),
            new Note(R.id.AButton, 440),
            new Note(R.id.ASharpButton, 466),
            new Note(R.id.BButton, 493.88),
    };
    private static final Note[] OCT_THREE = {
            new Note(R.id.CButton, 523.25),
            new Note(R.id.CSharpButton, 554.36),
            new Note(R.id.DButton, 587.33),
            new Note(R.id.DSharpButton, 622.25),
            new Note(R.id.EButton, 659.25),
            new Note(R.id.FButton, 698.45),
            new Note(R.id.FSharpButton, 739.98),
            new Note(R.id.GButton, 783.99),
            new Note(R.id.GSharpButton, 831),
            new Note(R.id.AButton, 880),
            new Note(R.id.ASharpButton, 932),
            new Note(R.id.BButton, 987.76),
    };
    private PitchPlayer notePlayer;
    private int octave = 0;
    private int currentNote;
    private boolean playing = false;
    private Button currentButton;

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
                } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
                    Log.w("setNumberPickerTextColor", e);
                }
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune);
        //Enable user to navigate home with the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        notePlayer = new PitchPlayer();
        //Assigning onClickListeners to all buttons.
        for (int aBUTTON_ID : BUTTON_ID) {
            Button id = (Button) findViewById(aBUTTON_ID);
            id.setOnClickListener(this);
        }
        //Setting up Octave number picker.
        final NumberPicker numberPickerOctave = (NumberPicker) findViewById(R.id.numberPickerOctave);
        numberPickerOctave.setMaxValue(2);
        numberPickerOctave.setMinValue(0);
        numberPickerOctave.setValue(1);
        numberPickerOctave.setDisplayedValues(new String[]{"+1", "0", "-1"});
        numberPickerOctave.setWrapSelectorWheel(true);
        numberPickerOctave.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        setNumberPickerTextColor(numberPickerOctave, Color.parseColor("#FFFFFF"));

        /**
         * Numberpicker for Octave. If tone is currently playing the numberpicker will change octave
         * and start the tone again.
         */

        numberPickerOctave.setOnValueChangedListener(new NumberPicker.
                OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int
                    oldVal, int newVal) {
                switch (numberPickerOctave.getValue()) {

                    case 0: {
                        octave = 1;
                        if (playing) {
                            notePlayer.stop();
                            notePlayer = new PitchPlayer();
                            double freq = getFreq(OCT_THREE, currentNote);
                            notePlayer.audioHandler(freq);
                            updateText(currentButton, freq);
                        }
                        break;
                    }
                    case 1: {
                        octave = 0;
                        if (playing) {
                            notePlayer.stop();
                            notePlayer = new PitchPlayer();
                            double freq = getFreq(OCT_TWO, currentNote);
                            notePlayer.audioHandler(freq);
                            updateText(currentButton, freq);
                        }
                        break;
                    }
                    case 2: {
                        octave = -1;
                        if (playing) {
                            notePlayer.stop();
                            notePlayer = new PitchPlayer();
                            double freq = getFreq(OCT_ONE, currentNote);
                            notePlayer.audioHandler(freq);
                            updateText(currentButton, freq);
                        }
                        break;
                    }
                }
            }
        });
    }

    //onClick used when user presses one of the note buttons.
    public void onClick(final View v) {
        /**
         * Added a handler to let the system sleep for 1 ms in order
         * to let the audiotrack to be properly released in case the user decides to spam
         * tones.
         */
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                /**
                 * Checking if the current tone is the same as previous.
                 * If it is the same then the tone will stop playing.
                 */
                if (playing && currentNote == v.getId()) {
                    Button sameButton = (Button) v;
                    //Updating button state
                    sameButton.setTextColor(Color.WHITE);
                    //stopping the tone and initializing new pitchplayer.
                    notePlayer.stop();
                    notePlayer = new PitchPlayer();
                    playing = false;
                    //If the tone is not the same then the selected tone will be played
                } else {
                    //stopping previous tone and initializing new pitchplayer
                    notePlayer.stop();
                    notePlayer = new PitchPlayer();
                    playing = true;
                    //Updating button states
                    if (currentButton != null) {
                        currentButton.setTextColor(Color.WHITE);
                    }
                    Button newButton = (Button) v;
                    newButton.setTextColor(Color.RED);
                    currentButton = newButton;
                    //Update text to show viewer which note is being currently played
                    currentNote = v.getId();

                    //Switch case used to determine which octave and frequency to send to the audioHandler.
                    switch (octave) {
                        case 1: {
                            double freq = getFreq(OCT_THREE, currentNote);
                            notePlayer.audioHandler(freq);
                            updateText(currentButton, freq);
                            break;
                        }
                        case 0: {
                            double freq = getFreq(OCT_TWO, currentNote);
                            notePlayer.audioHandler(freq);
                            updateText(currentButton, freq);
                            break;
                        }
                        case -1: {
                            double freq = getFreq(OCT_ONE, currentNote);
                            notePlayer.audioHandler(freq);
                            updateText(currentButton, freq);
                            break;
                        }
                    }
                }
            }
        }, 100);
    }


    //Helper method to get the frequency of desired tone.
    double getFreq(Note[] oct, int buttonID) {
        for (Note n : oct) {
            int noteID = n.getNote();
            if (noteID == buttonID) {
                return n.getFreq();
            }
        }
        return 0;
    }

    void updateText(Button b, double freq) {
        TextView noteText = (TextView) findViewById(R.id.noteSelected);
        noteText.setText(b.getText().toString() +
                "\n" + String.valueOf((int)freq) + " Hz");
    }

    //onStop called to stop the tone from being played when activity is exited.
    @Override
    protected void onStop() {
        super.onStop();
        if (notePlayer != null) {
            notePlayer.stop();
            notePlayer = null;
        }
    }

    //onResume called when user comes back to the activity. Initializing a new pitchplayer.
    @Override
    protected void onResume() {
        super.onStop();
        notePlayer = new PitchPlayer();
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
        Intent intent = new Intent(TuneActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

}

