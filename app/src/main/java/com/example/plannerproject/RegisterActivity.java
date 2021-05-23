package com.example.plannerproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vishnusivadas.advanced_httpurlconnection.PutData;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText textInputEditTextFullname, textInputEditTextEmail, textInputEditTextPassword;
    private CircularProgressButton registerButton;
    private TextView textViewLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private String fullname, email, password, userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        changeStatusBar();

        // Initialization
        textInputEditTextFullname = findViewById(R.id.fullname);
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);
        registerButton = findViewById(R.id.registerButton);
        textViewLogin = findViewById(R.id.loginText);
        progressBar = findViewById(R.id.progress);

        mAuth = FirebaseAuth.getInstance();

        registerButton.setOnClickListener(this);
        textViewLogin.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.loginText):
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
                break;
            case (R.id.registerButton):
                registerUser();
                break;

        }
    }

    private void registerUser() {
        fullname = textInputEditTextFullname.getText().toString().trim();
        email = textInputEditTextEmail.getText().toString().trim();


        if(TextUtils.isEmpty(fullname)) {
            textInputEditTextFullname.setError("Field is required!");
            textInputEditTextFullname.requestFocus();
            return;
        }
        if(TextUtils.isEmpty(email)) {
            textInputEditTextEmail.setError("Field is required!");
            textInputEditTextEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputEditTextEmail.setError("Please provide valid email address!");
            textInputEditTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)){
            textInputEditTextPassword.setError("Field is required!");
            textInputEditTextPassword.requestFocus();
            return;
        }

        if (password.length() < 8) {
            textInputEditTextPassword.setError("Required minimum password length should be 8 characters!");
            textInputEditTextPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    User user = new User(fullname, email);

                    userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    // Provide the name of the collections in getReference()
                    // .child(FirebaseAuth.getInstance().getCurrentUser().getUid()):
                    // return the user registered ID and set the additional object to the registered user
                    FirebaseDatabase.getInstance().getReference("Users").child(userUid)
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                                mAuth.signOut();
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                progressBar.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration failed! Try again!", Toast.LENGTH_SHORT).show();
                                updateUI(null);
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed! Try again!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    public void updateUI(FirebaseUser account){
        if(account != null){
            Toast.makeText(RegisterActivity.this, "User registration is successful", Toast.LENGTH_SHORT).show();

        }else {
            Toast.makeText(RegisterActivity.this, "Registration failed! Try again!", Toast.LENGTH_SHORT).show();
        }

    }
}