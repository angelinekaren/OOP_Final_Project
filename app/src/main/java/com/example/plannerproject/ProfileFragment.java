package com.example.plannerproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    private TextView getEmail;
    private TextView getFullname;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);

        getFullname = v.findViewById(R.id.userFullname);
        getEmail = v.findViewById(R.id.userEmail);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String uid = firebaseAuth.getCurrentUser().getUid();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("Users").child(uid);

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

        return v;
    }

    private void showEditProfileDialog() {
        // Dialog options
        String options[] = {"Edit Fullname", "Update Password"};

        // Alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set title
        builder.setTitle("Choose Action Below");

        // Set items to dialog (options)
        builder.setItems(options, ((dialog, which) -> {
            switch (which) {
                case 0:
                    // Edit fullname clicked
                    builder.setMessage("Editing Fullname");
                    showFullnameUpdateDialog("fullname");
                    break;
                case 1:
                    // Update Password clicked
                    builder.setMessage("Updating Password");
                    showUpdatePasswordDialog();
            }
        }));
        // Create and show dialog
        builder.create().show();

    }

    private void showUpdatePasswordDialog() {
    }

    private void showFullnameUpdateDialog(String fullname) {
    }


}
