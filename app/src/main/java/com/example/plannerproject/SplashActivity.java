package com.example.plannerproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends AppCompatActivity {

    // onCreate(): initialize activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Call changeStatusBar() function
        changeStatusBar();
        setContentView(R.layout.activity_splash);

        // New instance of Handler
        final Handler handler = new Handler();
        // postDelayed(): causes the Runnable r to be added to the message queue
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // This invoke to WelcomeActivity page is run after 2 seconds of time elapses
                Intent intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000); // 2 seconds
    }

    // Function to change the status bar
    public void changeStatusBar() {
        // Check if it's running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Set StatusBar color same like darkBlue color
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.darkBlue));
        }
    }
}