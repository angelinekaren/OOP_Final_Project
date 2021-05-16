package com.example.plannerproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.transition.CircularPropagation;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class WelcomeActivity extends AppCompatActivity {
    CircularProgressButton loginButton, signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusBar();
        setContentView(R.layout.activity_welcome);

        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginClicked();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupClicked();
            }
        });


    }

    public void changeStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.welcome_background));
        }
    }

    public void loginClicked() {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

        overridePendingTransition(R.anim.slide_in_right,  R.anim.stay);
    }

    public void signupClicked() {
        Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();

        overridePendingTransition(R.anim.slide_in_right,  R.anim.stay);
    }
}