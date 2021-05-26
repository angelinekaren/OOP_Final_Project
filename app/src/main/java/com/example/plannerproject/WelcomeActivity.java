package com.example.plannerproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {

    // onCreate(): initialize activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Call changeStatusBar() function
        changeStatusBar();
        setContentView(R.layout.activity_welcome);

        // Initialization
        CircularProgressButton loginButton = findViewById(R.id.loginButton);
        CircularProgressButton signUpButton = findViewById(R.id.signUpButton);

        // Initialize firebase auth
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Set the login and signup button to invisible
        loginButton.setVisibility(View.INVISIBLE);
        signUpButton.setVisibility(View.INVISIBLE);

        // If the current user is logged in
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Get current user
                    FirebaseUser currentUser = mAuth.getCurrentUser();

                    if (currentUser != null) {
                        // Invoke to MainActivity page
                        Intent MainActivity = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(MainActivity);
                        WelcomeActivity.this.finish();
                    }
                }
            });

        } else {
            // If the current user is not logged in, show the login button and sign up button
            loginButton.setVisibility(View.VISIBLE);
            signUpButton.setVisibility(View.VISIBLE);
            System.out.println("User not available");
        }

        // Set onClick listener to signup and login button
        signUpButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case(R.id.signUpButton): {
                // Call signupClicked() function
                signupClicked();
                break;
            }
            case(R.id.loginButton) : {
                // Call loginClicked() function
                loginClicked();
                break;
            }
        }
    }

    // Function to change the status bar
    public void changeStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Set StatusBar color to darkBlue color
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.darkBlue));
        }
    }

    public void loginClicked() {
        // Invoke to login activity page
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

        // Transition animation
        // overridePendingTransition(int enterAnim, int ExitAnim)
        // This function corresponds to the two animation, which in this case: slide_in_right and stay
        // It is to remove the old Activity (WelcomeActivity) and start to the new Activity (LoginActivity)
        // slide_in_right from welcome page to login page, and stay in login page
        overridePendingTransition(R.anim.slide_in_right,  R.anim.stay);
    }

    public void signupClicked() {
        // Invoke to register activity page
        Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();

        // Transition animation
        // overridePendingTransition(int enterAnim, int ExitAnim)
        // This function corresponds to the two animation, which in this case: slide_in_right and stay
        // It is to remove the old Activity (WelcomeActivity) and start to the new Activity (RegisterActivity)
        // slide_in_right from welcome page to register page, and stay in register page
        overridePendingTransition(R.anim.slide_in_right,  R.anim.stay);
    }
}