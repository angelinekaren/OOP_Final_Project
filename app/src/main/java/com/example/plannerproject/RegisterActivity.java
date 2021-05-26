package com.example.plannerproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.plannerproject.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

// RegisterActivity class
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText textInputEditTextFullname, textInputEditTextEmail, textInputEditTextPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    // onCreate(): initialize activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Call changeStatusBar() function
        changeStatusBar();

        // Initialization
        textInputEditTextFullname = findViewById(R.id.fullname);
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);
        CircularProgressButton registerButton = findViewById(R.id.registerButton);
        TextView textViewLogin = findViewById(R.id.loginText);
        progressBar = findViewById(R.id.progress);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Set onClick listener
        registerButton.setOnClickListener(this);
        textViewLogin.setOnClickListener(this);
    }

    // Function to change the status bar
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
                // Call registerUser() function
                registerUser();
                break;

        }
    }

    // Function to register user
    private void registerUser() {
        // Get the inputted fullname, email, and password
        String fullname = textInputEditTextFullname.getText().toString().trim();
        String email = textInputEditTextEmail.getText().toString().trim();
        String password = textInputEditTextPassword.getText().toString().trim();

        // If fullname inputs are empty, set error
        if(TextUtils.isEmpty(fullname)) {
            textInputEditTextFullname.setError("Field is required!");
            textInputEditTextFullname.requestFocus();
            return;
        }
        // If email inputs are empty, set error
        if(TextUtils.isEmpty(email)) {
            textInputEditTextEmail.setError("Field is required!");
            textInputEditTextEmail.requestFocus();
            return;
        }
        // If the inpputed email address is not valid, set error
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputEditTextEmail.setError("Please provide valid email address!");
            textInputEditTextEmail.requestFocus();
            return;
        }
        // If password inputs are empty, set error
        if (TextUtils.isEmpty(password)){
            textInputEditTextPassword.setError("Field is required!");
            textInputEditTextPassword.requestFocus();
            return;
        }
        // If password input is less than 8 characters, set error
        if (password.length() < 8) {
            textInputEditTextPassword.setError("Required minimum password length should be 8 characters!");
            textInputEditTextPassword.requestFocus();
            return;
        }
        // Set progress bar to visible
        progressBar.setVisibility(View.VISIBLE);
        // Create user with the inputted email and password
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // If task is successful,
                if(task.isSuccessful()) {
                    // Create user model
                    User user = new User(fullname, email);

                    // Get the current looged in user Uid
                    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    // Provide the name of the collections in getReference()
                    // .child(FirebaseAuth.getInstance().getCurrentUser().getUid()):
                    // return the user registered ID and set the additional object to the registered user
                    // Store it to the database
                    FirebaseDatabase.getInstance().getReference("Users").child(userUid)
                            .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // If task is successful,
                            if (task.isSuccessful()) {
                                // Get the current logged in user
                                FirebaseUser user = mAuth.getCurrentUser();
                                // Call updateUI() function and pass in user as the argument
                                updateUI(user);
                                // Sign the user out
                                mAuth.signOut();
                                // Invoke the LoginActivity page
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                // Set progress bar to gone
                                progressBar.setVisibility(View.GONE);
                            } else {
                                // Call updateUI() function and pass null as the argument
                                updateUI(null);
                                // Set progress bar to gone
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });
                } else {
                    // Create a toast message
                    Toast.makeText(RegisterActivity.this, "Registration failed! Try again!", Toast.LENGTH_SHORT).show();
                    // Set progress bar to gone
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    // Function to updateUI
    public void updateUI(FirebaseUser account){
        // If the user is successfully signed up, create successful toast message
        if(account != null){
            Toast.makeText(RegisterActivity.this, "User registration is successful", Toast.LENGTH_SHORT).show();
        // Else, create failed toast message
        }else {
            Toast.makeText(RegisterActivity.this, "Registration failed! Try again!", Toast.LENGTH_SHORT).show();
        }
    }
}