package com.example.plannerproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class ProfileFragment extends Fragment {
    private TextView getEmail;
    private TextView getFullname;
    CircularProgressButton updatePasswordButton, getUpdatePasswordButton;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference ref;
    ProgressDialog pd;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        Log.i(TAG, "@onCreateView");
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        getFullname = v.findViewById(R.id.userFullname);
        getEmail = v.findViewById(R.id.userEmail);

        firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("Users").child(uid);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                getEmail.setText("Email: " + user.getEmail());
                getFullname.setText("Fullname: " + user.getFullname());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("The read failed: " + error.getCode());
            }
        });

        updatePasswordButton = v.findViewById(R.id.updatePassword);

        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdatePasswordDialog();
            }
        });


        return v;
    }




    private void showEditProfileDialog() {
        // Dialog options
        String[] options = {"Edit Fullname", "Update Password"};

        // Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set title
        builder.setTitle("Choose Action Below");

        // Set items to dialog (options)
        builder.setItems(options, ((dialog, which) -> {
            if(which==0) {
                // Edit fullname clicked
                pd.setMessage("Editing Fullname");
                showFullnameUpdateDialog("fullname");
            }
            else if(which==1) {
                // Update Password clicked
                pd.setMessage("Updating Password");
                showUpdatePasswordDialog();
            }
        }));
        // Create and show dialog
        builder.create().show();
    }


    private void showUpdatePasswordDialog() {
        // Update password dialog (Old Password, New Password)

        // Inflate layout for dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_password, null);

        final TextInputEditText getInputOldPassword = dialogView.findViewById(R.id.inputOldPassword);
        final TextInputEditText getInputNewPassword = dialogView.findViewById(R.id.inputUpdatePassword);
        getUpdatePasswordButton = dialogView.findViewById(R.id.updatePasswordButton);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set view to dialog
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        getUpdatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set validation to data
                String oldPassword, newPassword;

                oldPassword = getInputOldPassword.getText().toString().trim();
                newPassword = getInputNewPassword.getText().toString().trim();

                if(TextUtils.isEmpty(oldPassword)) {
                    Toast.makeText(getActivity(), "Please fill in your current password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(getActivity(), "Please fill in your new password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(newPassword.length()<8) {
                    Toast.makeText(getActivity(), "Required minimum password length should be 8 characters!", Toast.LENGTH_LONG).show();
                }

                dialog.dismiss();
                updatePassword(oldPassword, newPassword);

            }
        });
    }

    private void updatePassword(String oldPassword, final String newPassword) {
        pd.show();

        firebaseAuth = FirebaseAuth.getInstance();

        // Get current user
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        final String email = user.getEmail();

        // Before changing password, re-authenticate the user
        AuthCredential authCredential = EmailAuthProvider.getCredential(email, oldPassword);

        user.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Updated password failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(getActivity(), "Error authentication", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showFullnameUpdateDialog(String fullname) {
    }


}
