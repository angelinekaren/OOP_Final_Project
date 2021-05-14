package com.example.plannerproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.vishnusivadas.advanced_httpurlconnection.PutData;

import org.json.JSONObject;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText textInputEditTextEmail, textInputEditTextPassword;
    CircularProgressButton loginButton;
    TextView textViewRegister, forgetPassText;
    ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        changeStatusBar();
        setContentView(R.layout.activity_login);

        // Initialization
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        textViewRegister = findViewById(R.id.registerText);
        progressBar = findViewById(R.id.progress);
        forgetPassText = findViewById(R.id.forgotPassword);

        mAuth = FirebaseAuth.getInstance();


        loginButton.setOnClickListener(new View.OnClickListener() {
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

                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                progressBar.setVisibility(View.GONE);
                            }
                            else {
                                String error = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Registration failed!" + error, Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
        });


    }


    public void changeStatusBar() {
        // Set windowStatusBar color to white
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
    }

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


    public void onBackPressed(View view) {
        Intent intent = new Intent(LoginActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();

        overridePendingTransition(R.anim.slide_in_left,  android.R.anim.slide_out_right);
    }

    public void goToForgetPassPage(View view) {
        Intent intent = new Intent(LoginActivity.this, ForgetActivity.class);
        startActivity(intent);
        finish();

        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
    }
}

