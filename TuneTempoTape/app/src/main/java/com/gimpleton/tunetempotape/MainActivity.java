package com.gimpleton.tunetempotape;

/**
 * Activity that first greats the user and displays the three
 * different functions of the app. A staging area for the user.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.gimpleton.tunetempotape.R;


public class MainActivity extends ActionBarActivity {

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);

        Button tuneButton = (Button) findViewById(R.id.TuneMainButton);
        Button tempoButton = (Button) findViewById(R.id.TempoMainButton);
        Button tapeButton = (Button) findViewById(R.id.TapeMainButton);

        //Tune Button
        tuneButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TuneActivity.class);
                startActivity(intent);
            }
        });
        //Tempo Button
        tempoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TempoActivity.class);
                startActivity(intent);
            }
        });
        //Tape button
        tapeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TapeActivity.class);
                startActivity(intent);
            }
        });

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
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }
}
