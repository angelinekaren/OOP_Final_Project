package com.example.plannerproject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddNewTask extends BottomSheetDialogFragment {
    public static final String TAG = "addNewTask";
    private EditText getTaskInput;
    private TextView getSetDate;
    private Button getSaveButton;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private Context context;
    private String getDate;
    private String dbDate, dbTask;
    private String id;
    private String taskId;
    private boolean isUpdated = false;

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getTaskInput = view.findViewById(R.id.addTaskTextInput);
        getSetDate = view.findViewById(R.id.setDate);
        getSaveButton = view.findViewById(R.id.saveButton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userUid = user.getUid();

        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Tasks").child(userUid);


        getTaskInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Check if EditText is empty/not
                if(s.toString().equals("")) {
                    getSaveButton.setEnabled(false);
                    getSaveButton.setBackgroundColor(Color.GRAY);
                } else {
                    getSaveButton.setEnabled(true);
                    getSaveButton.setBackgroundColor(getResources().getColor(R.color.register_background));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        updatedTask();

        getSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewCalendar();
            }
        });

        getSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewTask();
            }
        });

    }

    private void updatedTask() {
        final Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdated = true;
            String task = bundle.getString("task");
            taskId = bundle.getString("id");
            String date = bundle.getString("dateTime");

            getSetDate.setText(date);
            getTaskInput.setText(task);

            if (task.length() > 0) {
                getSaveButton.setEnabled(false);
                getSaveButton.setBackgroundColor(Color.GRAY);
            }

        }
    }

    private void setNewTask() {
        String task = getTaskInput.getText().toString();
        id = ref.push().getKey();

        if (isUpdated) {

            HashMap<String, Object> result = new HashMap<>();
            result.put("task", task);
            result.put("dateTime", getDate);

            ref.child(taskId).updateChildren(result)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // Successfully updated
                            Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed
                            Toast.makeText(getActivity(), "Error updating", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            if (task.isEmpty()) {
                Toast.makeText(context, "Required to fill in new task!", Toast.LENGTH_SHORT).show();
            }
            else {
                TaskModel taskModel = new TaskModel(id, task, getDate, 0);

                ref.child(id).setValue(taskModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Task successfully saved!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context, "Failed to saved task", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
        dismiss();
    }

    private void viewCalendar() {
        Calendar calendar = Calendar.getInstance();

        int year, month, day;

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DATE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month += 1;
                getDate = dayOfMonth + "/" + month + "/" + year;
                getSetDate.setText(getDate);
            }
        }, year, month, day);

        datePickerDialog.show();
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        Activity activity = getActivity();
        if(activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener)activity).onDialogClose(dialog);
        }

    }

}


