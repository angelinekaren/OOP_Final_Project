package com.example.plannerproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
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
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class ForgetActivity extends AppCompatActivity {
    TextInputEditText forgotEmail;
    CircularProgressButton forgotPasswordButton;
    ProgressDialog progressDialog;
    private String email;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget);
        changeStatusBar();

        forgotEmail = findViewById(R.id.inputForgotEmail);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mAuth = FirebaseAuth.getInstance();

        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

    }

    private void validateData() {
        email = forgotEmail.getText().toString();
        if (email.isEmpty()) {
            forgotEmail.setError("Field is required!");
        }
        else
        {
            forgetPasswordSend();
        }
    }

    private void forgetPasswordSend() {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ForgetActivity.this, "Check your email to reset your password", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ForgetActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    String error = task.getException().toString();
                    Toast.makeText(ForgetActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void changeStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(R.color.welcome_background));
        }
    }

    public void onBackPressed(View view) {
        Intent intent = new Intent(ForgetActivity.this, LoginActivity.class);
        startActivity(intent);

        overridePendingTransition(R.anim.slide_in_left,  android.R.anim.slide_out_right);
    }

}