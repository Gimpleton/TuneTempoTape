package com.gimpleton.tunetempotape.tests;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.Button;

import com.example.gimpleton.tunetempotape.R;
import com.gimpleton.tunetempotape.MainActivity;

/**
 * Created by Gimpleton on 2015-01-12.
 */
public class MainActivityTest extends ActivityUnitTestCase<MainActivity> {

    private MainActivity mActivity;
    private Button tuneButton, tempoButton, tapeButton;
    private Intent mLaunchIntent;

    public MainActivityTest() {
        super(MainActivity.class);
    }
    public void testPreconditions(){
        assertNotNull("mActivity is null", mActivity);
        assertNotNull("tuneButton is null", tuneButton);
        assertNotNull("tempoButton is null", tempoButton);
        assertNotNull("tapeButton is null", tapeButton);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mLaunchIntent = new Intent(getInstrumentation().getTargetContext(), MainActivity.class);
        startActivity(mLaunchIntent, null, null);
        final Button launchTuneButton = (Button) getActivity().findViewById(R.id.TuneMainButton);
        final Button launchTempoButton = (Button) getActivity().findViewById(R.id.TempoMainButton);
        final Button launchTapeButton = (Button) getActivity().findViewById(R.id.TapeMainButton);

    }

    @MediumTest
    public void testActivityWasLaunchedWithIntent(){
        startActivity(mLaunchIntent, null, null);
        final Button launchTuneButton = (Button) getActivity().findViewById(R.id.TuneMainButton);
        launchTuneButton.performClick();

        final Intent launchIntent = getStartedActivityIntent();
        assertNotNull("Intent was null", launchIntent);
        assertTrue(isFinishCalled());
    }

}
