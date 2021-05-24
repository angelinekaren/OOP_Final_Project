package com.example.plannerproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

// Forget Activity
public class ForgetActivity extends AppCompatActivity {
    private TextInputEditText forgotEmail;
    private String email;
    private FirebaseAuth mAuth;

    // onCreate(): initialize activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);

        // Call changeStatusBar() function
        changeStatusBar();

        // Initialization
        forgotEmail = findViewById(R.id.inputForgotEmail);
        CircularProgressButton forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        // Initialize progress dialog, set message, and set progress style(spin)
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Set an onClick listener to forgot password button
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call validateData() function
                validateData();
            }
        });

    }

    // Function to validate data
    private void validateData() {
        // Get user input of email in the EditText
        email = forgotEmail.getText().toString();
        // Check if the input is empty
        if (email.isEmpty()) {
            // If it is empty, set an error message
            forgotEmail.setError("Field is required!");
        }
        // Else, call forgetPasswordSend() function
        else
        {
            forgetPasswordSend();
        }
    }

    // Function to send forget password instruction to email
    private void forgetPasswordSend() {
        // Send the password reset to email
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // If task is successful,
                if (task.isSuccessful()) {
                    // Create a success message
                    Toast.makeText(ForgetActivity.this, "Check your email to reset your password", Toast.LENGTH_SHORT).show();

                    // Invoke to login activity page
                    Intent intent = new Intent(ForgetActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    // Else, create an error message
                    String error = task.getException().toString();
                    Toast.makeText(ForgetActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Function to change the status bar
    public void changeStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Set windowStatusBar color to white
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    // Function for going back from activity to activity
    public void onBackPressed(View view) {
        // Invoke to login activity page
        Intent intent = new Intent(ForgetActivity.this, LoginActivity.class);
        startActivity(intent);

        // Add a transition animation
        // overridePendingTransition(int enterAnim, int ExitAnim)
        // This function corresponds to the two animation, which in this case: slide_in_left and slide_out_right
        // It is to slide left to LoginActivity page and slide out from ForgetActivity page
        overridePendingTransition(R.anim.slide_in_left,  android.R.anim.slide_out_right);
    }

}