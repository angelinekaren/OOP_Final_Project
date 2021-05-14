package com.example.plannerproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.vishnusivadas.advanced_httpurlconnection.PutData;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class RegisterActivity extends AppCompatActivity {
    TextInputEditText textInputEditTextEmail, textInputEditTextPassword;
    CircularProgressButton registerButton;
    TextView textViewLogin;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        changeStatusBar();

        // Initialization
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);
        registerButton = findViewById(R.id.registerButton);
        textViewLogin = findViewById(R.id.loginText);
        progressBar = findViewById(R.id.progress);

        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;

                email = textInputEditTextEmail.getText().toString().trim();
                password = textInputEditTextPassword.getText().toString().trim();


                if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
                }
                if(TextUtils.isEmpty(email)) {
                    textInputEditTextEmail.setError("Field is required!");
                }
                if (TextUtils.isEmpty(password)){
                    textInputEditTextPassword.setError("Field is required!");
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                progressBar.setVisibility(View.GONE);
                            }
                            else {
                                String error = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Registration failed!" + error, Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }

                        }
                    });
                }



            }
        });
    }

    public void changeStatusBar() {
        // Check if it's running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Set StatusBar color same like register_background color
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.register_background));
        }
    }

    public void loginClicked(View view) {
        // Command to move from this page to LoginActivity
        // Declare Intent object to provides runtime binding between two activities
        // Intent(context, class)
        // context (subclass of context): current activity class, Class: Activity class to start
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();

        // Transition animation
        // overridePendingTransition(int enterAnim, int ExitAnim)
        // This function corresponds to the two animation, which in this case: slide_in_left and slide_out_right
        // It is to slide left to LoginActivity page and slide out from RegisterActivity page
        overridePendingTransition(R.anim.slide_in_left,  android.R.anim.slide_out_right);
    }
}