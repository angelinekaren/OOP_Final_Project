package com.example.plannerproject;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.plannerproject.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
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
import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;

// ProfileFragment class: user profile details
public class ProfileFragment extends Fragment implements View.OnClickListener {
    private TextView getEmail;
    private TextView getFullname;
    private DatabaseReference ref;
    private FirebaseUser user;
    private ProgressDialog pd;
    private TextInputEditText getInputOldPassword, getInputNewPassword, getInputNewName, getDeletePassword;
    private AlertDialog dialog;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflating / adding fragment_account.xml view layout for this fragment
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        // Initialization
        getFullname = v.findViewById(R.id.userFullname);
        getEmail = v.findViewById(R.id.userEmail);
        CircularProgressButton updatePasswordButton = v.findViewById(R.id.updatePassword);
        CircularProgressButton getDeleteButton = v.findViewById(R.id.deleteAccountButton);

        // Initialize progress dialog
        pd = new ProgressDialog(getActivity());

        // Initialize firebase auth and get the current logged in user
        user = FirebaseAuth.getInstance().getCurrentUser();
        // Get reference from the database: Users - userUid (current user)
        ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        // Call profileDetails() function
        profileDetails();

        // Set onClick listener
        updatePasswordButton.setOnClickListener(this);
        getDeleteButton.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.updatePassword): {
                // Call showEditProfileDialog() function
                showEditProfileDialog();
                break;
            }
            case (R.id.deleteAccountButton): {
                // Call deleteAccountDialog() function
                deleteAccountDialog();
                break;
            }
        }
    }

    // Function to show profile details
    public void profileDetails() {
        // ValueEventListener to a list of data will return the entire list of data as a single DataSnapshot,
        // which you can then loop over to access individual children
        // Create a value event listener to the database reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get value from user class
                User user = snapshot.getValue(User.class);

                // Get current logged in user email and fullname and set it
                getEmail.setText(String.format("Email: %s", user.getEmail()));
                getFullname.setText(String.format("Fullname: %s", user.getFullname()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Function to show delete account dialog
    private void deleteAccountDialog() {

        // Inflate layout for dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete_account, null);

        // Initialization
        getDeletePassword = dialogView.findViewById(R.id.inputDeletePassword);
        CircularProgressButton deleteButton = dialogView.findViewById(R.id.deleteButton);

        // Create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set view to dialog
        builder.setView(dialogView);

        // Create and show the dialog
        dialog = builder.create();
        dialog.show();

        // Set onClick listener to delete button
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When clicked, call deleteUserAccount() function
                deleteUserAccount();
            }
        });
    }

    // Function to get user input
    private void deleteUserAccount() {
        // Get user input of password
        String deletePassword = getDeletePassword.getText().toString().trim();

        // Check if input is empty
        if (TextUtils.isEmpty(deletePassword)) {
            // If it's empty, create an error message
            Toast.makeText(getActivity(), "Please fill in password for re-confimartion", Toast.LENGTH_SHORT).show();
            return;
        }

        // Dismiss the dialog
        dialog.dismiss();
        // Call deleteAccount() function and pass in the inputted password to the parameter
        deleteAccount(deletePassword);
    }

    // Function to delete the account
    private void deleteAccount(String password) {
        // Show the progress dialog
        pd.show();

        // Get auth credentials from the user (email, password)
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), password);

        // Re-authenticate the user
        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // Delete the user from the database

                // Query the user by ordering them by userID and search which one match with the current user
                Query userQuery = ref.orderByChild("userID").equalTo(user.getUid());
                // Remove value from Users - userUid reference
                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        snapshot.getRef().removeValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                // Initialize database and create reference: Tasks - userUid
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("Tasks").child(user.getUid());
                // Remove value from that reference
                reference.removeValue();

                // Delete the user
                user.delete();

                // Dismiss the progress dialog
                pd.dismiss();

                // Create toast successful message
                Toast.makeText(getActivity(), "Account deleted", Toast.LENGTH_SHORT).show();

                // Sign this user out
                FirebaseAuth.getInstance().signOut();

                // Invoke to LoginActivity page
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);

            }
        })
                // On failure,
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Dismiss the progress dialog
                        pd.dismiss();
                        // Create a toast failed message
                        Toast.makeText(getActivity(), "Failed to authenticate", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Function to show update dialog
    private void showEditProfileDialog() {
        // Dialog options
        String[] options = {"Edit Fullname", "Update Password"};

        // Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set title
        builder.setTitle("Choose Action Below");

        // Set items to dialog (options)
        builder.setItems(options, ((dialog, which) -> {
            if (which == 0) {
                // Edit fullname clicked
                pd.setMessage("Editing Fullname");
                showFullnameUpdateDialog();
            } else if (which == 1) {
                // Update Password clicked
                pd.setMessage("Updating Password");
                showUpdatePasswordDialog();
            }
        }));

        // Create and show dialog
        builder.create().show();
    }

    // Function to show the update password dialog
    private void showUpdatePasswordDialog() {
        // Inflate layout for dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_password, null);

        // Initialization
        getInputOldPassword = dialogView.findViewById(R.id.inputOldPassword);
        getInputNewPassword = dialogView.findViewById(R.id.inputUpdatePassword);
        CircularProgressButton getUpdatePasswordButton = dialogView.findViewById(R.id.updatePasswordButton);

        // Create an alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set view to dialog
        builder.setView(dialogView);

        // Create and show the dialog
        dialog = builder.create();
        dialog.show();

        // Set onClick listener to update password button
        getUpdatePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When clicked, call updateUserPassword() function
                updateUserPassword();
            }
        });
    }

    // Function to get user input password
    private void updateUserPassword() {
        // Get user input of their old password and new password
        String oldPassword = getInputOldPassword.getText().toString().trim();
        String newPassword = getInputNewPassword.getText().toString().trim();

        // Check if the oldPassword EditText is empty
        if (TextUtils.isEmpty(oldPassword)) {
            // If it is empty, create a toast message
            Toast.makeText(getActivity(), "Please fill in your current password", Toast.LENGTH_SHORT).show();
            return;
        }
        // Check if the newPassword EditText is empty
        if (TextUtils.isEmpty(newPassword)) {
            // If it is empty, create a toast message
            Toast.makeText(getActivity(), "Please fill in your new password", Toast.LENGTH_SHORT).show();
            return;
        }
        // Check if the inputted newPassword EditText is less than 8 characters
        if (newPassword.length() < 8) {
            // If it is, create a toast message
            Toast.makeText(getActivity(), "Required minimum password length should be 8 characters!", Toast.LENGTH_LONG).show();
            return;
        }

        // Dismiss the dialog
        dialog.dismiss();
        // Call updatePassword() function and pass in the user input to the parameter (as the arguments)
        updatePassword(oldPassword, newPassword);
    }

    // Function to update the password
    private void updatePassword(String oldPassword, final String newPassword) {
        // Show progress dialog
        pd.show();

        // Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Get auth credentials from the user (email, password)
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

        // Re-authenticate the user
        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // If successful, update user password with new password
                user.updatePassword(newPassword)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                // Dismiss the progress dialog
                                pd.dismiss();
                                // Create toast message
                                Toast.makeText(getActivity(), "Password updated", Toast.LENGTH_SHORT).show();
                            }
                        })
                        // If it failed,
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Dismiss the progress dialog
                                pd.dismiss();
                                // Create toast message
                                Toast.makeText(getActivity(), "Failed to update", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        })
                // On failure,
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Dismiss the progress dialog
                        pd.dismiss();
                        // Create toast message
                        Toast.makeText(getActivity(), "Failed to authenticate", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Show the fullname update dialog
    private void showFullnameUpdateDialog() {
        // Inflate layout for dialog
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_name, null);

        // Initialization
        getInputNewName = dialogView.findViewById(R.id.inputNewName);
        CircularProgressButton getUpdateNameButton = dialogView.findViewById(R.id.updateNameButton);

        // Create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set view to dialog
        builder.setView(dialogView);

        // Create and show the dialog
        dialog = builder.create();
        dialog.show();

        // Set onClick listener to update name button
        getUpdateNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When clicked, call updateUserName() function
                updateUserName();
            }
        });
    }

    // Function to get user input of new name
    private void updateUserName() {
        // Get user input for their new name
        String newName = getInputNewName.getText().toString().trim();

        // Check if the newName EditText is empty
        if (TextUtils.isEmpty(newName)) {
            // If it's empty, create toast message
            Toast.makeText(getActivity(), "Field is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Dismiss the dialog
        dialog.dismiss();
        // Call updateNewName() function and pass in the user fullname input as the argument
        updateNewName(newName);
    }

    // Function to update new name
    private void updateNewName(String fullname) {
        // Show progress dialog
        pd.show();

        // Create and put updated name in a HashMap
        HashMap<String, Object> result = new HashMap<>();
        result.put("fullname", fullname);

        // Pass this data at reference: Users - userUid (current user) and update
        ref.updateChildren(result)
                // On success,
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Successfully updated
                        // Dismiss the progress dialog
                        pd.dismiss();
                        // Create a toast message
                        Toast.makeText(getActivity(), "Successfully updated", Toast.LENGTH_SHORT).show();
                    }
                })
                // On failure
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed
                        // Dismiss progress dialog
                        pd.dismiss();
                        // Create a toast message
                        Toast.makeText(getActivity(), "Error updating", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}