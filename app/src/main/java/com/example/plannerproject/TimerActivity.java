package com.example.plannerproject;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimerActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView getTaskName, getDate, getClock;
    private ImageView getCalImg, getClockImg;
    private static TextView getTime;
    private Button getStartButton, getResetButton;
    private static CountDownTimer countDownTimer;
    private long endTime;
    private boolean timerRun;
    private static long startTimeMillis;
    private static long timeLeftMillis;
    private int isClicked = 0;
    private FloatingActionButton addTimeButton;
    private ImageView getBackButton, getAnchor;
    private ObjectAnimator anim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusBar();
        setContentView(R.layout.activity_timer);

        getTaskName = findViewById(R.id.taskName);
        getDate = findViewById(R.id.timeTask);
        getClock = findViewById(R.id.clockTask);
        getCalImg = findViewById(R.id.calendarImg);
        getClockImg = findViewById(R.id.clockImg);

        if (getIntent().getStringExtra("dateTime") == null && getIntent().getStringExtra("clockTime") == null) {
            getCalImg.setVisibility(View.INVISIBLE);
            getClockImg.setVisibility(View.INVISIBLE);
        }

        getTaskName.setText(getIntent().getStringExtra("task"));
        getDate.setText(getIntent().getStringExtra("dateTime"));
        getClock.setText(getIntent().getStringExtra("clockTime"));

        getTime = findViewById(R.id.countdownTimer);
        getStartButton = findViewById(R.id.startBtn);
        getResetButton = findViewById(R.id.resetBtn);
        addTimeButton = findViewById(R.id.timeSetButton);
        getBackButton = findViewById(R.id.backButton);
        getAnchor = findViewById(R.id.anchor);

        // Load animation
        anim = ObjectAnimator.ofFloat(getAnchor, "rotation", 0, 360);
        anim.setDuration(3000);
        anim.setRepeatCount(Animation.INFINITE);
        anim.setRepeatMode(ObjectAnimator.RESTART);

        setListener();
    }

    private void setListener() {
        getStartButton.setOnClickListener(this);
        getResetButton.setOnClickListener(this);
        addTimeButton.setOnClickListener(this);
        getBackButton.setOnClickListener(this);
    }

    public void startTimer() {
        endTime = System.currentTimeMillis() + timeLeftMillis;

        isClicked += 1;
        if (isClicked == 1) {
            anim.start();
        }
        else {
            anim.resume();
        }
        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateCountdown();
            }

            @Override
            public void onFinish() {
                countDownTimer = null;
                timerRun = false;
                getStartButton.setText("Start");
            }
        }.start();
        timerRun = true;
    }


    private void pauseTimer() {
        if(countDownTimer != null) {
            anim.pause();
            countDownTimer.cancel();
            countDownTimer = null;
            timerRun = false;
        }
    }

    public static void setTime(long millisSecond) {
        startTimeMillis = millisSecond;
        timeLeftMillis = startTimeMillis;
        updateCountdown();
    }

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
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        startTimeMillis = prefs.getLong("startTimeMillis", 0);
        timerRun = prefs.getBoolean("timerRun", false);
        updateCountdown();
        if (timerRun) {
            endTime = prefs.getLong("endTime", 0);
            timeLeftMillis = endTime - System.currentTimeMillis();
            if (timeLeftMillis < 0) {
                timeLeftMillis = 0;
                timerRun = false;
                updateCountdown();
            } else {
                startTimer();
            }
        }
    }

    private static void updateCountdown() {
        int hours = (int) (timeLeftMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftMillis / 1000) % 60;

        String timeLeft;
        timeLeft = String.format(Locale.getDefault(),
                "%02d:%02d:%02d", hours, minutes, seconds);
        getTime.setText(timeLeft);
    }

    private void changeStatusBar() {
        // Set windowStatusBar color to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorTime));
        }
    }

    public void onBackPressed(View view) {
        Intent intent = new Intent(TimerActivity.this, HomeFragment.class);
        startActivity(intent);
        finish();

        overridePendingTransition(R.anim.slide_in_left,  android.R.anim.slide_out_right);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case (R.id.startBtn): {
                if (countDownTimer == null && !timerRun) {
                    startTimer(); //start countdown
                    getStartButton.setText("Pause"); //Change Text
                } else {
                    //Else stop timer and change text
                    pauseTimer();
                    getStartButton.setText("Start");
                }
                break;
            }
            case (R.id.resetBtn): {
                anim.end();
                pauseTimer();
                timeLeftMillis = startTimeMillis;
                getStartButton.setText("Start");
                getTime.setText("00:00:00");
                isClicked = 0;
                break;
            }
            case (R.id.timeSetButton) : {
                SetTime.newInstance().show(getSupportFragmentManager(), SetTime.TAG);
                break;
            }
            case (R.id.backButton): {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            }

        }
    }
}