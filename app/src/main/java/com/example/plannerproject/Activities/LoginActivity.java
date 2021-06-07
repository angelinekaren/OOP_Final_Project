package com.example.plannerproject.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.plannerproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

// LoginActivity class
public class LoginActivity extends AppCompatActivity {
    private TextInputEditText textInputEditTextEmail, textInputEditTextPassword;
    private ProgressBar progressBar;
    public static final String TAG = "LOGIN";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // onCreate(): initialize activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Call changeStatusBar() function
        changeStatusBar();
        setContentView(R.layout.activity_login);

        // Initialization
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);
        CircularProgressButton loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progress);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        // Get current user
        FirebaseUser user = mAuth.getCurrentUser();
        // Create a listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // If the current user is logged in, go to MainActivity page
                if (user != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else
                {
                    // Else, current auth state is logged out
                    Log.d(TAG,"AuthStateChanged:Logout");
                }

            }
        };


        // Set an onClick listener to login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call loginUser() function
                loginUser();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        //removeAuthSateListener is used in onStart function for checking purposes, it helps in logging you out.
        mAuth.removeAuthStateListener(mAuthListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            // Remove listener if they are no longer useful
            // For memory, possibility of app crashes, and running time purposes
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // Function to log in user
    private void loginUser(){
        // Define variables with string type
        String email, password;

        // Get input text and assign it to the variables
        email = textInputEditTextEmail.getText().toString().trim();
        password = textInputEditTextPassword.getText().toString().trim();

        // Validate input

        // If email and password inputs are empty, create a message
        if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
        }
        // If email input is empty, set error
        if(TextUtils.isEmpty(email)) {
            textInputEditTextEmail.setError("Field is required!");
        }
        // If password input is empty, set error
        if (TextUtils.isEmpty(password)){
            textInputEditTextPassword.setError("Field is required!");
        }
        else {
            // Set the progress bar to visible
            progressBar.setVisibility(View.VISIBLE);

            // Sign in the current user
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    // If task is successful,
                    if(task.isSuccessful()) {
                        // Create a success message
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        // Invoke to MainActivity page
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);

                        // Set the progress bar to gone
                        progressBar.setVisibility(View.GONE);
                    }
                    else {
                        // Create a failed message
                        Toast.makeText(LoginActivity.this, "Login failed! Try again!", Toast.LENGTH_SHORT).show();
                        // Set the progress bar to gone
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    // Function to change the status bar
    public void changeStatusBar() {
        // Set windowStatusBar color to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    // Function if text is clicked
    public void loginClicked(View view) {
        // Command to move from this page to RegisterActivity
        // Declare Intent object to provides runtime binding between two activities
        // Intent(context, class)
        // context (subclass of context): current activity class, Class: Activity class to start
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();

        // Transition animation
        // overridePendingTransition(int enterAnim, int ExitAnim)
        // This function corresponds to the two animation, which in this case: slide_in_right and stay
        // It is to remove the old Activity (LoginActivity) and start to the new Activity (RegisterActivity)
        // slide_in_right from login page to register page, and stay in register page
        overridePendingTransition(R.anim.slide_in_right,  R.anim.stay);
    }

    // Function for going back from activity to activity
    public void onBackPressed(View view) {
        // Invoke to welcome activity page
        Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();

        // Add a transition animation
        // overridePendingTransition(int enterAnim, int ExitAnim)
        // This function corresponds to the two animation, which in this case: slide_in_left and slide_out_right
        // It is to slide left to WelcomeActivity page and slide out from LoginActivity page
        overridePendingTransition(R.anim.slide_in_left,  android.R.anim.slide_out_right);
    }

    // Function to go to forget password page
    public void goToForgetPassPage(View view) {
        // Invoke to ForgetActivity page
        Intent intent = new Intent(LoginActivity.this, ForgetActivity.class);
        startActivity(intent);
        finish();

        // Transition animation
        // overridePendingTransition(int enterAnim, int ExitAnim)
        // This function corresponds to the two animation, which in this case: slide_in_right and stay
        // It is to remove the old Activity (LoginActivity) and start to the new Activity (ForgetActivity)
        // slide_in_right from login page to forget page, and stay in forget page
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
    }

}

