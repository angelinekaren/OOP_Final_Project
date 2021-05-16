package com.example.plannerproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

public class ProfileFragment extends Fragment {
    private TextView getEmail;
    private TextView getFullname;
    private ProgressBar progressBar;
    CircularProgressButton updatePasswordButton, getUpdatePasswordButton, getUpdateNameButton, getDeleteButton, deleteButton;
    DatabaseReference ref;
    ProgressDialog pd;
    private String oldPassword, newPassword, newName, deletePassword;
    private TextInputEditText getInputOldPassword, getInputNewPassword, getInputNewName, getDeletePassword;
    AlertDialog dialog;

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

        // Initialize progress dialog
        pd = new ProgressDialog(getActivity());

        profileDetails();

        updatePasswordButton = v.findViewById(R.id.updatePassword);
        getDeleteButton = v.findViewById(R.id.deleteAccountButton);
        progressBar = v.findViewById(R.id.progressBarAccount);

        updatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        getDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccountDialog();
            }
        });
        return v;
    }

    public void profileDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();

            ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

            getEmail.setText("Email: "+ email);

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);

                    getFullname.setText("Fullname: " + user.getFullname());

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    System.out.println(error.toString());
                }
            });
        }
    }

    private void deleteAccountDialog() {

        // Update password dialog (Old Password, New Password)

        // Inflate layout for dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete_account, null);

        getDeletePassword = dialogView.findViewById(R.id.inputDeletePassword);
        deleteButton = dialogView.findViewById(R.id.deleteButton);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set view to dialog
        builder.setView(dialogView);

        dialog = builder.create();
        dialog.show();

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUserAccount();
            }
        });

    }

    private void deleteUserAccount() {
        // Set validation to data
        deletePassword = getDeletePassword.getText().toString().trim();

        if (TextUtils.isEmpty(deletePassword)) {
            Toast.makeText(getActivity(), "Please fill in password for re-confimartion", Toast.LENGTH_SHORT).show();
            return;
        }

        dialog.dismiss();
        deleteAccount(deletePassword);
    }

    private void deleteAccount(String password) {
        pd.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                user.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot appleSnapshot: snapshot.getChildren()) {
                                            appleSnapshot.getRef().removeValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Account deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Failed to delete", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), "Failed to authenticate", Toast.LENGTH_SHORT).show();
            }
        });

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getActivity(), WelcomeActivity.class);
        startActivity(intent);

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
                showFullnameUpdateDialog();
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

        getInputOldPassword = dialogView.findViewById(R.id.inputOldPassword);
        getInputNewPassword = dialogView.findViewById(R.id.inputUpdatePassword);
        getUpdatePasswordButton = dialogView.findViewById(R.id.updatePasswordButton);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set view to dialog
        builder.setView(dialogView);

        dialog = builder.create();
        dialog.show();

        getUpdatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserPassword();
            }
        });
    }

    private void updateUserPassword() {
        // Set validation to data
        oldPassword = getInputOldPassword.getText().toString().trim();
        newPassword = getInputNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword)) {
            Toast.makeText(getActivity(), "Please fill in your current password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(getActivity(), "Please fill in your new password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 8) {
            Toast.makeText(getActivity(), "Required minimum password length should be 8 characters!", Toast.LENGTH_LONG).show();
            return;
        }

        dialog.dismiss();
        updatePassword(oldPassword, newPassword);
    }


    private void updatePassword(String oldPassword, final String newPassword) {
        pd.show();

        // Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Before changing password, re-authenticate the user
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                user.updatePassword(newPassword)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Password updated", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Failed to update", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), "Failed to authenticate", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showFullnameUpdateDialog() {
        // Inflate layout for dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_name, null);

        getInputNewName = dialogView.findViewById(R.id.inputNewName);
        getUpdateNameButton = dialogView.findViewById(R.id.updateNameButton);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set view to dialog
        builder.setView(dialogView);

        dialog = builder.create();
        dialog.show();

        getUpdateNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserName();
            }
        });
    }

    private void updateUserName() {
        // Set validation to data
        newName =getInputNewName.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(getActivity(), "Field is required", Toast.LENGTH_SHORT).show();
            return;
        }

        dialog.dismiss();
        updateNewName(newName);
    }

    private void updateNewName(String fullname) {
        pd.show();

        HashMap<String, Object> result = new HashMap<>();
        result.put("fullname", fullname);

        ref.updateChildren(result)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Successfully updated
                        pd.dismiss();
                        Toast.makeText(getActivity(), "Successfully updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed, dismiss progress dialog
                        pd.dismiss();
                        Toast.makeText(getActivity(), "Error updating", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
