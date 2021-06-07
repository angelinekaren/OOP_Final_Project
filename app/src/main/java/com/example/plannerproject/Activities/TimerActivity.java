package com.example.plannerproject.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.plannerproject.R;
import com.example.plannerproject.ObjectHandlers.SetTime;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Locale;


public class TimerActivity extends AppCompatActivity implements View.OnClickListener {
    private static TextView getTime;
    private Button getStartButton, getResetButton;
    private static CountDownTimer countDownTimer;
    private long endTime;
    private boolean timerRun;
    private static long startTimeMillis;
    private static long timeLeftMillis;
    private int isClicked = 0;
    private FloatingActionButton addTimeButton;
    private ImageView getBackButton;
    private ObjectAnimator anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Call changeStatusBar() function
        changeStatusBar();
        setContentView(R.layout.activity_timer);

        // Initialization
        TextView getTaskName = findViewById(R.id.taskName);
        TextView getDate = findViewById(R.id.timeTask);
        TextView getClock = findViewById(R.id.clockTask);
        ImageView getCalImg = findViewById(R.id.calendarImg);
        ImageView getClockImg = findViewById(R.id.clockImg);

        // If there is no value of dateTime and clockTime key, set the icon calendar and clock to invisible
        if (getIntent().getStringExtra("dateTime") == null && getIntent().getStringExtra("clockTime") == null) {
            getCalImg.setVisibility(View.INVISIBLE);
            getClockImg.setVisibility(View.INVISIBLE);
        }

        // Set the task title, date, and clock by retrieving value from its key
        getTaskName.setText(getIntent().getStringExtra("task"));
        getDate.setText(getIntent().getStringExtra("dateTime"));
        getClock.setText(getIntent().getStringExtra("clockTime"));

        // Initialization
        getTime = findViewById(R.id.countdownTimer);
        getStartButton = findViewById(R.id.startBtn);
        getResetButton = findViewById(R.id.resetBtn);
        addTimeButton = findViewById(R.id.timeSetButton);
        getBackButton = findViewById(R.id.backButton);
        ImageView getAnchor = findViewById(R.id.anchor);

        // Set animation to anchor -> rotate from 0 to 360 degree
        anim = ObjectAnimator.ofFloat(getAnchor, "rotation", 0, 360);
        // Set the duration for each loop to 3 seconds
        anim.setDuration(3000);
        // Repeat animation for infinite time
        anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatMode(ObjectAnimator.RESTART);
        // Call setListener() function
        setListener();
    }

    private void setListener() {
        // Set onClick listener to buttons
        getStartButton.setOnClickListener(this);
        getResetButton.setOnClickListener(this);
        addTimeButton.setOnClickListener(this);
        getBackButton.setOnClickListener(this);
    }

    // Function to start timer
    public void startTimer() {
        // Assign time for how long the timer runs
        endTime = System.currentTimeMillis() + timeLeftMillis;

        // Increment isClicked by 1
        isClicked += 1;
        // If it's the first click, start the animation
        if (isClicked == 1) {
            anim.start();
        }
        // Else, resume the animation
        else {
            anim.resume();
        }

        // Create new instance of CountDownTimer with millisInFuture(long) and countDownInterval(long)
        // CountDownInterval is in millisecond: 1 second for the countdown
        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update timeLeftMillis with the amount of time until its finished
                timeLeftMillis = millisUntilFinished;
                // Call the updateCountdown() function
                updateCountdown();
            }

            @Override
            public void onFinish() {
                // When time is up
                // Set the countDownTimer to null, timerRun as false, and set the start button text to "Start"
                countDownTimer = null;
                timerRun = false;
                getStartButton.setText("Start");
            }
        }.start();
        // Set timerRun to true
        timerRun = true;
    }

    // Function to pause timer
    private void pauseTimer() {
        // If the countDownTimer is running,
        if(countDownTimer != null) {
            // Pause the animation
            anim.pause();
            // Cancel the countdown
            countDownTimer.cancel();
            // Set the countDownTimer to null and timerRun as false
            countDownTimer = null;
            timerRun = false;
        }
    }

    // Set time with inputted one
    public static void setTime(long millisSecond) {
        // Assign it to startTimeMillis
        startTimeMillis = millisSecond;
        // Assign the startTimeMillis to timeLeftMillis
        timeLeftMillis = startTimeMillis;
        // Call updateCountdown() function
        updateCountdown();
    }

    // Save key-value data onStop
    // Save our startTimeMillis to SharedPreference when we leave our app
    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("startTimeMillis", startTimeMillis);
        editor.putLong("millisLeft", timeLeftMillis);
        editor.putBoolean("timerRun", timerRun);
        editor.putLong("endTime", endTime);
        editor.apply();
        // If the countdown timer is running
        if (countDownTimer != null) {
            // Cancel the countdown
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        // onStart: startTimeMillis is 0 and timerRun is set to false
        startTimeMillis = prefs.getLong("startTimeMillis", 0);
        timerRun = prefs.getBoolean("timerRun", false);
        // Call updateCountDown() function
        updateCountdown();
        // If the current timer state is not running
        if (timerRun) {
            // onStart: endTime is set to 0
            endTime = prefs.getLong("endTime", 0);
            // Find the elapsed time (last timeLeftMillis)
            timeLeftMillis = endTime - System.currentTimeMillis();
            // If its finished counting down, initialize timeLeftMillis to 0 and timerRun to false
            if (timeLeftMillis < 0) {
                timeLeftMillis = 0;
                timerRun = false;
                // Call updateCountdown() function
                updateCountdown();
            } else {
                // Else, start the timer again with the saved timeLeftMillis
                startTimer();
            }
        }
    }

    private static void updateCountdown() {
        // Convert milliseconds to hours, minutes, and seconds
        int hours = (int) (timeLeftMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftMillis / 1000) % 60;

        String timeLeft;
        // Format the string to 00:00:00 (2 digits)
        timeLeft = String.format(Locale.getDefault(),
                "%02d:%02d:%02d", hours, minutes, seconds);
        // Set the time
        getTime.setText(timeLeft);
    }

    // Function to change the status bar
    private void changeStatusBar() {
        // Set windowStatusBar color to colorTime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorTime));
        }
    }

    // Function for going back from activity to activity
    public void onBackPressed(View view) {
        // Invoke to home fragment page
        Intent intent = new Intent(TimerActivity.this, HomeFragment.class);
        startActivity(intent);
        finish();

        // Add a transition animation
        // overridePendingTransition(int enterAnim, int ExitAnim)
        // This function corresponds to the two animation, which in this case: slide_in_left and slide_out_right
        // It is to slide left to HomeFragment and slide out from TimerActivity page
        overridePendingTransition(R.anim.slide_in_left,  android.R.anim.slide_out_right);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case (R.id.startBtn): {
                // If its currently not running,
                if (countDownTimer == null && !timerRun) {
                    startTimer(); //start countdown
                    getStartButton.setText("Pause"); //Change Text
                } else {
                    // Else, pause timer and change text to "Start"
                    pauseTimer();
                    getStartButton.setText("Start");
                }
                break;
            }
            case (R.id.resetBtn): {
                // End the animation
                anim.end();
                // Call pauseTimer() function
                pauseTimer();
                // Set the timeLeftMillis with startTimeMillis: 0
                timeLeftMillis = startTimeMillis;
                // Set the start button text to "Start"
                getStartButton.setText("Start");
                // Set time to 00:00:00
                getTime.setText("00:00:00");
                // Set back isClicked to 0
                isClicked = 0;
                break;
            }
            case (R.id.timeSetButton) : {
                // Create new instance of SetTime class
                SetTime.newInstance().show(getSupportFragmentManager(), SetTime.TAG);
                break;
            }
            case (R.id.backButton): {
                // Invoke to MainActivity page
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            }
        }
    }
}