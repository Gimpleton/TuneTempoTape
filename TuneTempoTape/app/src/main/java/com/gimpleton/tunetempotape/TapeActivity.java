package com.gimpleton.tunetempotape;
/**
 * Activity which allows the user to record uncompressed audio, to a click(metronome) or without
 * and allows playback of the most recent recording. Recorded audio is automatically saved to the users
 * device.
 */

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gimpleton.tunetempotape.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TapeActivity extends ActionBarActivity {
    private String outputFile = null;
    private Button start, stop, play;
    private Chronometer myChronometerPlay;
    private double BPM;
    private AppPreferences savePref;
    private MediaPlayer m;
    private MetronomeAsyncTask metroTask;
    private final Handler audioSize = new Handler();
    private long maxFileSize;
    private recorderAsyncTask recorderTask;
    private final Runnable fileSizeUpdate = new Runnable() {
        /**
         * Creates a new thread and checks the file size every 0.2 seconds of the current recording. Updates the the text on screen to
         * inform the user of how many MB they have used so far to record. Also checks if
         * maximum file size has been reached. If the maximum file size is reached then the recording
         * is stopped and saved.
         */
        public void run() {
            try {
                File file = new File(outputFile);
                long length = file.length();
                //checking file size.
                if (length > maxFileSize && recorderTask != null) {
                    //stopping the recording
                    recorderTask.stop();
                    recorderTask = null;
                    //Updating button states
                    stop.setEnabled(false);
                    play.setEnabled(true);
                    start.setEnabled(true);
                    start.setTextColor(Color.GREEN);
                    play.setTextColor(Color.BLUE);
                    stop.setTextColor(Color.DKGRAY);
                    //Make sure that device scans new media so that recording immediately can be accessed.
                    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    intent.setData(Uri.fromFile(new File(outputFile)));
                    sendBroadcast(intent);
                }
                //Updating file size text.
                length = (length / 1024) / 1024;
                long maxLength = (maxFileSize / 1024) / 1024;
                TextView sizeText = (TextView) findViewById(R.id.SizeTape);
                sizeText.setText(String.valueOf((int) length + " / " + maxLength + " mb"));

            } catch (Exception e) {
                e.printStackTrace();
            }
            audioSize.postDelayed(this, 200);
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tape);
        //Allow user to navigate home with action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initializing the metronome
        metroTask = new MetronomeAsyncTask();

        //Getting bpm and file size preferences from TempoActivity and Settings.
        savePref = new AppPreferences(getApplicationContext());
        BPM = savePref.getBpmPref();
        maxFileSize = savePref.getSizePref() * 1048576; //Also converting to bytes.

        //Updating file size text
        long maxLength = (maxFileSize / 1024) / 1024;
        TextView sizeText = (TextView) findViewById(R.id.SizeTape);
        sizeText.setText(String.valueOf("0 / " + maxLength + " mb"));

        start = (Button) findViewById(R.id.RecTapeButton);
        stop = (Button) findViewById(R.id.StopTapeButton);
        play = (Button) findViewById(R.id.PlayBackTapeButton);

        //Updating button states
        stop.setEnabled(false);
        play.setEnabled(false);
        stop.setTextColor(Color.DKGRAY);
        play.setTextColor(Color.DKGRAY);
        start.setTextColor(Color.GREEN);

        /**
         * Creating and designating file path for audio file. Each audio file has a
         * time stamp derived from Date().
         */

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String currentDateandTime = sdf.format(new Date());
        outputFile = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/recording" + currentDateandTime + ".wav";
    }

    /**
     * onClick method when user presses "Rec" button. Starts the recording.
     *
     */
    public void start(View view) {
        //Creating new thread for recording and starting it.
        try {
            recorderTask = new recorderAsyncTask();
            recorderTask.execute();
            audioSize.postDelayed(fileSizeUpdate, 200);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        //Updating button states.
        start.setEnabled(false);
        stop.setEnabled(true);
        play.setEnabled(false);
        stop.setTextColor(Color.WHITE);
        start.setTextColor(Color.RED);
        play.setTextColor(Color.DKGRAY);

    }

    /**
     * onClick method when user presses "Stop" button. Depending on state the stop button
     * will either stop the recording or stop the playback of previous recording.
     */
    public void stop(View view) {
        //Stop thread for checking file size.
        audioSize.removeCallbacks(fileSizeUpdate);
        //When there is audio being played back.
        if (m != null) {
            //stop audio playback
            m.stop();
            //stop chronometer
            myChronometerPlay.stop();
            //updating button states.
            stop.setEnabled(false);
            play.setEnabled(true);
            start.setEnabled(true);
            start.setTextColor(Color.GREEN);
            play.setTextColor(Color.BLUE);
            stop.setTextColor(Color.DKGRAY);
        }
        //Stop thread for recording
        if (recorderTask != null) {
            recorderTask.stop();
            recorderTask = null;
            //updating button states
            stop.setEnabled(false);
            play.setEnabled(true);
            start.setEnabled(true);
            start.setTextColor(Color.GREEN);
            play.setTextColor(Color.BLUE);
            stop.setTextColor(Color.DKGRAY);
            //Make sure that device scans new media so that recording immediately can be accessed.
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(new File(outputFile)));
            sendBroadcast(intent);

        }
    }

    /**
     * onClick method when user presses "Play" button.
     */
    public void play(View view) throws IllegalArgumentException,
            SecurityException, IllegalStateException, IOException {
        //Initializing Chronometer.
        myChronometerPlay = (Chronometer) findViewById(R.id.DurationTape);
        myChronometerPlay.setBase(SystemClock.elapsedRealtime());
        //Initializing mediaplayer, preparing and starting.
        m = new MediaPlayer();
        m.setDataSource(outputFile);
        m.prepare();
        m.start();
        //Starting chronometer
        myChronometerPlay.start();
        //Updating button states.
        play.setEnabled(false);
        stop.setEnabled(true);
        start.setEnabled(false);
        start.setTextColor(Color.DKGRAY);
        play.setTextColor(Color.DKGRAY);
        stop.setTextColor(Color.WHITE);
        //Listener used to check when playback reaches end.
        m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                //Stop playback and chronometer
                m.stop();
                myChronometerPlay.stop();
                //Updating button states.
                play.setEnabled(true);
                stop.setEnabled(false);
                start.setEnabled(true);
                start.setTextColor(Color.GREEN);
                play.setTextColor(Color.BLUE);
                stop.setTextColor(Color.DKGRAY);

            }
        });

    }

    /**
     * onClick method for when user presses "Click". Starts the metronome.
     */
    public void onButtonClick(View view) {
        //Checking if metronome is currently playing.
        if (metroTask.checkClick()) {
            //Stop playing and initialize new metronome.
            metroTask.stop();
            metroTask = null;
            metroTask = new MetronomeAsyncTask();
            //update button state.
            TextView bpmText = (TextView) findViewById(R.id.ClickTapeButton);
            bpmText.setText("Click");
        } else {
            //Getting users prefered BPM set in TempoActivity.
            BPM = savePref.getBpmPref();
            metroTask.setBpm(BPM);
            //Update button state.
            TextView bpmText = (TextView) findViewById(R.id.ClickTapeButton);
            bpmText.setText(String.valueOf((int) BPM));
            //Start metronome thread.
            if (metroTask.getStatus() != AsyncTask.Status.RUNNING) {
                metroTask.execute();
            }
        }
    }

    /**
     * onStop() when user leaves the activity.
     */
    @Override
    protected void onStop() {
        super.onStop();
        savePref = null;
        //Nullify metronome.
        metroTask.stop();
        metroTask = null;
        audioSize.removeCallbacks(fileSizeUpdate);
        //Updating button states
        stop.setEnabled(false);
        play.setEnabled(false);
        start.setEnabled(true);
        start.setTextColor(Color.GREEN);
        play.setTextColor(Color.DKGRAY);
        stop.setTextColor(Color.DKGRAY);
        //If audio is currently playing then audio is stopped.
        if (m != null) {
            m.stop();
            m = null;
        }
        //If currently recording recording is stopped and saved. User is notified.
        if (recorderTask != null) {
            recorderTask.stop();
            recorderTask = null;

            Toast toast = Toast.makeText(getApplicationContext(), "Recording stopped and saved",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            play.setEnabled(true);
            play.setTextColor(Color.BLUE);
            //If the chronometer is currently running then it is stopped.
            if (myChronometerPlay != null) {
                myChronometerPlay.stop();
                myChronometerPlay.setBase(SystemClock.elapsedRealtime());
                myChronometerPlay = null;
            }
        }
    }

    /**
     * When user returns to the activity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        //Initialize metronome and get file size preferences.
        metroTask = new MetronomeAsyncTask();
        AppPreferences newPref = new AppPreferences(getApplicationContext());
        long newMaxFileSize = newPref.getSizePref() * 1048576;
        long maxLength = (newMaxFileSize / 1024) / 1024;
        //Updating file size text.
        TextView sizeText = (TextView) findViewById(R.id.SizeTape);
        sizeText.setText(String.valueOf("0 / " + maxLength + " mb"));
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
        Intent intent = new Intent(TapeActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * This private class is used to make the recorder run in a separate thread and also make the chronometer run
     * on the UI thread. Unfortunately no improvement in performance is noticed. Chronometer is still lagging during
     * recording. Recording the audio uncompressed is a very heavy operation but the function outweighs the need for the chronometer
     * to run smoothly during recording.
     */

    private class recorderAsyncTask extends AsyncTask<Void, Void, String> {
        private RehearsalAudioRecorder recorder;
        private final Chronometer myChronometer = (Chronometer) findViewById(R.id.DurationTape);

        recorderAsyncTask() {
            recorder = new RehearsalAudioRecorder(RehearsalAudioRecorder.RECORDING_UNCOMPRESSED,
                    MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
            recorder.setOutputFile(outputFile);
            recorder.prepare();
        }

        protected String doInBackground(Void... params) {
            // recorder.start();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recorder.start();
                    myChronometer.start();
                }
            });
            return null;
        }

        public void stop() {
            recorder.stop();
            myChronometer.stop();
            recorder.release();
            recorder = null;
        }


    }
}